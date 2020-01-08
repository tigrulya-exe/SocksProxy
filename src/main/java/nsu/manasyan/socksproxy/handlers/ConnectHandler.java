package nsu.manasyan.socksproxy.handlers;

import nsu.manasyan.socksproxy.models.Connection;
import nsu.manasyan.socksproxy.socks.SocksResponse;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ConnectHandler extends Handler{
    private static final int ANY_PORT = 0;

    public ConnectHandler(Connection connection) {
        super(connection);
    }

    @Override
    public void handle(SelectionKey selectionKey) throws IOException {
        var socketChannel = (SocketChannel) selectionKey.channel();
        var handler = (Handler) selectionKey.attachment();
        var connection = handler.getConnection();

        socketChannel.finishConnect();

        selectionKey.attach(new ForwardHandler(connection));
        selectionKey.interestOpsAnd(~SelectionKey.OP_CONNECT);
        selectionKey.interestOpsOr(SelectionKey.OP_READ);
    }

    public static SocketChannel initTargetSocket(Connection clientConnection,
                                                 SelectionKey selectionKey, InetSocketAddress targetAddress) throws IOException {
        var targetSocket = SocketChannel.open();
        targetSocket.bind(new InetSocketAddress(ANY_PORT));
        targetSocket.configureBlocking(false);

        var targetConnection = new Connection(clientConnection.getObservableInputBuffer(),
                clientConnection.getObservableOutputBuffer());

        targetSocket.connect(targetAddress);
        var connectHandler = new ConnectHandler(targetConnection);

        clientConnection.setAssociate(targetSocket);
        targetConnection.setAssociate((SocketChannel) selectionKey.channel());

        var key = targetSocket.register(selectionKey.selector(), SelectionKey.OP_CONNECT, connectHandler);
        targetConnection.registerBufferListener(() -> key.interestOpsOr(SelectionKey.OP_WRITE));

        return targetSocket;
    }

    public static void connectToTarget(SelectionKey clientKey, InetSocketAddress targetAddress) throws IOException {
        var handler = (Handler) clientKey.attachment();
        var clientConnection = handler.getConnection();
        var targetSocketChannel = initTargetSocket(clientConnection, clientKey, targetAddress);

        putResponseIntoBuf(clientConnection, targetSocketChannel);
        clientKey.interestOpsOr(SelectionKey.OP_WRITE);
        clientKey.attach(new ForwardHandler(clientConnection));
        clientConnection.getOutputBuffer().clear();
    }

    private static void putResponseIntoBuf(Connection connection, SocketChannel socketChannel) throws IOException {
        var socketAddress = (InetSocketAddress) socketChannel.getLocalAddress();

        var response = new SocksResponse();
        var address = InetAddress.getLocalHost().getAddress();

        response.setBoundIp4Address(address);
        response.setBoundPort((short) socketAddress.getPort());

        var inputBuff = connection.getInputBuffer();
        inputBuff.put(response.toByteBuffer());
    }
}
