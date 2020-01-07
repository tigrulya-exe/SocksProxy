package nsu.manasyan.handlers;

import nsu.manasyan.models.Connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ConnectHandler extends Handler{
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
        selectionKey.interestOpsOr(SelectionKey.OP_READ);
    }
}
