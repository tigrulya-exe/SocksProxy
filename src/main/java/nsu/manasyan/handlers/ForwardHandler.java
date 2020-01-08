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
        Connection connection = ((Handler) selectionKey.attachment()).getConnection();

        int readCount = read(selectionKey);
        if (readCount != 0){
            connection.notifyBufferListener();
        }
    }

}
