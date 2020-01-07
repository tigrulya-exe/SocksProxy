package nsu.manasyan.handlers;

import nsu.manasyan.models.Connection;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ErrorHandler extends Handler {
    public ErrorHandler(Connection connection) {
        super(connection);
    }

    @Override
    public void handle(SelectionKey selectionKey) throws IOException {}

    @Override
    public int write(SelectionKey selectionKey) throws IOException {
        int remaining = super.write(selectionKey);
        if(remaining == 0){
            var socket = (SocketChannel) selectionKey.channel();
            socket.close();
        }

        return remaining;
    }
}
