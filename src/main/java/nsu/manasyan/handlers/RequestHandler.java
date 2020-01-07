package nsu.manasyan.handlers;

import nsu.manasyan.models.Connection;
import nsu.manasyan.socks.SocksRequest;
import nsu.manasyan.socks.SocksResponse;
import nsu.manasyan.dns.DnsService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
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
        Connection connection = getConnection();
        var outputBuffer = connection.getOutputBuffer();
        outputBuffer.clear();
        read(selectionKey);

        SocksRequest request = parseRequest(outputBuffer);

        if(request.getParseError() != 0x00){
            return;
        }

        if(request.getAddressType() == DOMAIN_NAME_TYPE){
            DnsService dnsService = DnsService.getInstance();
            dnsService.resolveName(request,selectionKey);
            return;
        }
        connectToTarget(selectionKey, request.getAddress());
    }

    public static void connectToTarget(SelectionKey clientKey, InetSocketAddress targetAddress) throws IOException {
        var handler = (Handler) clientKey.attachment();

        System.out.println("CONNECT");
        var clientConnection = handler.getConnection();
        var targetSocketChannel = initTargetSocket(clientConnection, clientKey, targetAddress);

        putResponseIntoBuf(clientConnection, targetSocketChannel);
        clientKey.interestOpsOr(SelectionKey.OP_WRITE);
        clientKey.attach(new ForwardHandler(clientConnection));
        clientConnection.getOutputBuffer().clear();
    }

    public static SocketChannel initTargetSocket(Connection clientConnection,
                                                 SelectionKey selectionKey, InetSocketAddress targetAddress) throws IOException {
        SocketChannel targetSocket = SocketChannel.open();
        targetSocket.bind(new InetSocketAddress(ANY_PORT));
        targetSocket.configureBlocking(false);

        Connection targetConnection = new Connection(clientConnection.getObservableInputBuffer(),
                clientConnection.getObservableOutputBuffer());

        SelectionKey key;
        targetSocket.connect(targetAddress);
        ConnectHandler connectHandler = new ConnectHandler(targetConnection);

        clientConnection.setSecondUser(targetSocket);
        targetConnection.setSecondUser((SocketChannel) selectionKey.channel());

        key = targetSocket.register(selectionKey.selector(), SelectionKey.OP_CONNECT, connectHandler);
        targetConnection.registerBufferListener(() -> key.interestOpsOr(SelectionKey.OP_WRITE));


        return targetSocket;
    }

    public static void putResponseIntoBuf(Connection connection, SocketChannel socketChannel) throws IOException {
        var socketAddress = (InetSocketAddress) socketChannel.getLocalAddress();

        SocksResponse response = new SocksResponse();
        var address = InetAddress.getLocalHost().getAddress();

        response.setBoundIp4Address(address);
        response.setBoundPort((short) socketAddress.getPort());

        var inputBuff = connection.getInputBuffer();
        inputBuff.put(response.toByteBuffer());
    }

    public void putErrorResponseIntoBuf(Connection connection) throws IOException {
        SocksResponse response = new SocksResponse();


        var inputBuff = connection.getInputBuffer();
        inputBuff.put(response.toByteBuffer());
    }
}
