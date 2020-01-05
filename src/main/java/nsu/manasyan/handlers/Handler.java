package nsu.manasyan.handlers;

import nsu.manasyan.models.Connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public abstract class Handler {
    private static final int BUFF_LENGTH = 8192;

    private Connection connection;

    public Handler(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    abstract public void handle(SelectionKey selectionKey) throws IOException;

    public void read(SelectionKey selectionKey) throws IOException {
        Handler handler = (Handler) selectionKey.attachment();
        SocketChannel socket = (SocketChannel) selectionKey.channel();
        Connection connection = handler.getConnection();

        if(!isReadyToRead(connection))
            return;

        // todo think about moving read to handlers
        socket.read(connection.getInputBuffer());
    }

    private boolean isReadyToRead(Connection connection){
        return connection.getInputBuffer().limit() < BUFF_LENGTH / 2;
    }

    public void write(SelectionKey selectionKey) throws IOException {
        ByteBuffer outputBuffer = connection.getOutputBuffer();
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        int writeCount = socketChannel.write(outputBuffer);

        if(writeCount == outputBuffer.remaining()){
            selectionKey.interestOps(SelectionKey.OP_READ);
            outputBuffer.clear();
        } else {
            outputBuffer.position(outputBuffer.position() + writeCount);
        }
    }
}
