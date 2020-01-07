package nsu.manasyan.handlers;

import nsu.manasyan.models.Connection;
import nsu.manasyan.socks.SocksRequest;
import nsu.manasyan.socks.SocksResponse;
import nsu.manasyan.dns.DnsService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import static nsu.manasyan.socks.SocksParser.parseRequest;

public class RequestHandler extends Handler {
    private static final byte DOMAIN_NAME_TYPE = 0x03;

    private static final int ANY_PORT = 0;

    public RequestHandler(Connection connection) {
        super(connection);
    }

    @Override
    public void handle(SelectionKey selectionKey) throws IOException {
        read(selectionKey);
        Connection connection = getConnection();

        SocksRequest request = parseRequest(connection.getOutputBuffer());
        if(request.getAddressType() == DOMAIN_NAME_TYPE){
            DnsService dnsService = DnsService.getInstance();
            dnsService.resolveName(request,selectionKey);
            return;
        }
        connectToTarget(selectionKey, request.getAddress());
    }

    public static void connectToTarget(SelectionKey selectionKey, InetSocketAddress targetAddress) throws IOException {
        var handler = (Handler) selectionKey.attachment();
        var connection = handler.getConnection();
        var selector = selectionKey.selector();

        var targetSocketChannel = initTargetSocket(connection, selector, targetAddress);
        putResponseIntoBuf(connection, targetSocketChannel);
    }

    public static SocketChannel initTargetSocket(Connection clientConnection,
                                                 Selector selector, InetSocketAddress targetAddress) throws IOException {
        SocketChannel targetSocket = SocketChannel.open();
        targetSocket.bind(new InetSocketAddress(ANY_PORT));
        targetSocket.configureBlocking(false);

        Connection targetConnection = new Connection(clientConnection.getObservableInputBuffer(),
                clientConnection.getObservableOutputBuffer());

        SelectionKey key;
        System.out.println(targetAddress);
        targetSocket.connect(targetAddress);
        ConnectHandler connectHandler = new ConnectHandler(targetConnection);
        key = targetSocket.register(selector, SelectionKey.OP_CONNECT, connectHandler);
        targetConnection.registerBufferListener(() -> key.interestOpsOr(SelectionKey.OP_WRITE));

        return targetSocket;
    }

    public static void putResponseIntoBuf(Connection connection, SocketChannel socketChannel) throws IOException {
        var socketAddress = (InetSocketAddress) socketChannel.getLocalAddress();

        SocksResponse response = new SocksResponse();
        var address = InetAddress.getLocalHost().getAddress();

        System.out.println(address.length);
        System.out.println(Arrays.toString(address));

        response.setBoundIp4Address(address);
        response.setBoundPort((short) socketAddress.getPort());

        var inputBuff = connection.getInputBuffer();
        inputBuff.put(response.toByteBuffer());
        // limit -> pos, pos -> 0
        inputBuff.flip();
        connection.notifySelf();
    }
}
