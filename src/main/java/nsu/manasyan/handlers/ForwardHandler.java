package nsu.manasyan.handlers;

import nsu.manasyan.models.Connection;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public class ForwardHandler extends Handler {
    public ForwardHandler(Connection connection) {
        super(connection);
    }

    @Override
    public void handle(SelectionKey selectionKey) throws IOException {
        System.out.println("FORWARD");
        Connection connection = ((Handler) selectionKey.attachment()).getConnection();
        read(selectionKey);
        connection.notifyBufferListener();
    }
}
