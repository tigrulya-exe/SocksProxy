package nsu.manasyan.handlers;

import nsu.manasyan.models.Connection;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ForwardHandler extends Handler {
    public ForwardHandler(Connection connection) {
        super(connection);
    }

    @Override
    public void handle(SelectionKey selectionKey) throws IOException {
        System.out.println("FORWARD");
        Connection connection = ((Handler) selectionKey.attachment()).getConnection();
        // todo tmp
        if(read(selectionKey) == 0){
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            socketChannel.close();
        }
        connection.notifyBufferListener();
    }
}
