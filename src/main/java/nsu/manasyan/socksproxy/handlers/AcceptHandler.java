package nsu.manasyan.socksproxy.handlers;

import nsu.manasyan.socksproxy.models.Connection;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

public class AcceptHandler extends Handler {
    private ServerSocketChannel serverSocketChannel;

    public AcceptHandler(ServerSocketChannel serverSocketChannel) {
        super(null);
        this.serverSocketChannel = serverSocketChannel;
    }

    @Override
    public void handle(SelectionKey selectionKey) throws IOException {
        var socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        var connection = new Connection(getBuffLength());
        var connectHandler = new SocksConnectHandler(connection);

        var key = socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ, connectHandler);
        connection.registerBufferListener(() -> key.interestOpsOr(SelectionKey.OP_WRITE));

        System.out.println("New connection: " + socketChannel.getRemoteAddress());
    }
}
