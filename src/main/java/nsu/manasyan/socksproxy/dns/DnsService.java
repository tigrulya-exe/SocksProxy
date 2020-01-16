package nsu.manasyan.socksproxy.dns;

import nsu.manasyan.socksproxy.handlers.ConnectHandler;
import nsu.manasyan.socksproxy.handlers.Handler;
import nsu.manasyan.socksproxy.models.FiniteTreeMap;
import nsu.manasyan.socksproxy.socks.SocksRequest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Map;

import org.xbill.DNS.*;

import static nsu.manasyan.socksproxy.handlers.SocksRequestHandler.onError;

public class DnsService {
    private static final int DNS_SERVER_PORT = 53;

    private static final byte HOST_UNREACHABLE_ERROR = 0x04;

    private static final int BUF_SIZE = 1024;

    private static final int CACHE_SIZE = 256;
    
    private int messageId = 0;

    private DatagramChannel socket;

    private InetSocketAddress dnsServerAddress;
    
    private Handler dnsResponseHandler;

    // key - dns message id
    private Map<Integer, DnsMapValue> unresolvedNames = new HashMap<>();

    // key - hostname, value - ip
    private FiniteTreeMap<String, String> dnsCache = new FiniteTreeMap<>(CACHE_SIZE);

    private static class SingletonHelper{
        private static final DnsService dnsService = new DnsService();
    }
    public static DnsService getInstance() {
        return SingletonHelper.dnsService;
    }

    private DnsService() {
        String[] dnsServers = ResolverConfig.getCurrentConfig().servers();
        this.dnsServerAddress = new InetSocketAddress(dnsServers[0], DNS_SERVER_PORT);
    }

    public void setSocket(DatagramChannel socket) {
        this.socket = socket;
        initResponseHandler();
    }

    public void registerSelector(Selector selector) throws ClosedChannelException {
        socket.register(selector, SelectionKey.OP_READ, dnsResponseHandler);
    }

    public void resolveName(SocksRequest request, SelectionKey selectionKey) throws IOException {
        try {
            var name = request.getDomainName();
            var cachedAddress = dnsCache.get(name + ".");
            if(cachedAddress != null){
                connectToTarget(cachedAddress, selectionKey, request.getTargetPort());
                return;
            }

            System.out.println("New domain name to resolve: " + request.getDomainName());
            var mapValue = new DnsMapValue(selectionKey, request.getTargetPort());
            var query = getQuery(name);
            var queryBytes = query.toWire();

            unresolvedNames.put(query.getHeader().getID(), mapValue);
            socket.send(ByteBuffer.wrap(queryBytes), dnsServerAddress);
        } catch (TextParseException exc){
            onError(selectionKey, HOST_UNREACHABLE_ERROR);
            exc.printStackTrace();
        }
    }
    
    private void initResponseHandler() {
        dnsResponseHandler = new Handler(null) {
            @Override
            public void handle(SelectionKey selectionKey) throws IOException {
                var byteBuffer = ByteBuffer.allocate(BUF_SIZE);
                if(socket.receive(byteBuffer) == null){
                    return;
                }

                var response = new Message(byteBuffer.flip());
                var answers = response.getSectionArray(Section.ANSWER);

                int responseId = response.getHeader().getID();
                var unresolvedName = unresolvedNames.get(responseId);
                if(answers.length == 0){
                    onError(unresolvedName.getSelectionKey(), HOST_UNREACHABLE_ERROR);
                    return;
                }

                var hostname = response.getQuestion().getName().toString();
                System.out.println(hostname + " resolved");

                var address = answers[0].rdataToString();
                dnsCache.put(hostname, address);
                connectToTarget(address, unresolvedName.getSelectionKey(), unresolvedName.getTargetPort());
                unresolvedNames.remove(responseId);
            }
        };
    }

    private void connectToTarget(String address, SelectionKey selectionKey, int port) throws IOException {
        var socketAddress = new InetSocketAddress(address, port);
        ConnectHandler.connectToTarget(selectionKey, socketAddress);
    }

    private Message getQuery(String domainName) throws TextParseException {
        var header = new Header(messageId++);
        header.setFlag(Flags.RD);
        header.setOpcode(0);

        var message = new Message();
        message.setHeader(header);

        var record = Record.newRecord(new Name(domainName + "."), Type.A, DClass.IN);
        message.addRecord(record, Section.QUESTION);

        return message;
    }
}
