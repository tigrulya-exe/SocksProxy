package nsu.manasyan.dns;

import nsu.manasyan.handlers.ConnectHandler;
import nsu.manasyan.handlers.SocksRequestHandler;
import nsu.manasyan.socks.SocksRequest;

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

public class DnsService {
    private static final int DNS_SERVER_PORT = 53;

    private int messageId = 0;

    private DatagramChannel socket;

    private InetSocketAddress dnsServerAddress;

    // key - dns message id
    private Map<Integer, DnsMapValue> unresolvedNames = new HashMap<>();

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
    }

    public void registerSelector(Selector selector) throws ClosedChannelException {
        DnsCallback callback = this::onResponse;
        socket.register(selector, SelectionKey.OP_READ, callback);
    }

    public void resolveName(SocksRequest request, SelectionKey selectionKey) throws IOException {
        try {
            String name = request.getDomainName();
            DnsMapValue mapValue = new DnsMapValue(selectionKey, request.getTargetPort());
            Message query = getQuery(name);
            byte[] queryBytes = query.toWire();
            unresolvedNames.put(query.getHeader().getID(), mapValue);

            // todo add selectionKey field to dns server communication handler class
            socket.send(ByteBuffer.wrap(queryBytes), dnsServerAddress);
        } catch (TextParseException exc){
            // todo send err response to socks client
            exc.printStackTrace();
        }
    }

    private void onResponse() throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8400);
        socket.read(byteBuffer);

        Message response = new Message(byteBuffer);
        Record[] answers = response.getSectionArray(Section.ANSWER);
        if(answers.length == 0){
            // todo send err response to socks client
        }

        // todo tmp
        int responseId = response.getHeader().getID();
        String address = answers[0].rdataToString();

        var unresolvedName = unresolvedNames.get(responseId);
        InetSocketAddress socketAddress = new InetSocketAddress(address, unresolvedName.getTargetPort());
        ConnectHandler.connectToTarget(unresolvedName.getSelectionKey(), socketAddress);
        unresolvedNames.remove(responseId);
    }

    private Message getQuery(String domainName) throws TextParseException {
        Header header = new Header(messageId++);
        header.setFlag(Flags.RD);
        header.setOpcode(0);

        Message message = new Message();
        message.setHeader(header);

        Record record = Record.newRecord(new Name(domainName), Type.A, DClass.IN);
        message.addRecord(record, Section.QUESTION);

        return message;
    }
}
