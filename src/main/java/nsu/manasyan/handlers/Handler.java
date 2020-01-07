package nsu.manasyan.handlers;

import nsu.manasyan.models.Connection;

import javax.crypto.spec.PSource;
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

    public int read(SelectionKey selectionKey) throws IOException {
        Handler handler = (Handler) selectionKey.attachment();
        SocketChannel socket = (SocketChannel) selectionKey.channel();
        Connection connection = handler.getConnection();

//        if(!isReadyToRead(connection))
//            return;

        var outputBuffer = connection.getOutputBuffer();
        int readCount = socket.read(outputBuffer);
        outputBuffer.flip();

        return readCount;
    }

    private boolean isReadyToRead(Connection connection){
        return connection.getInputBuffer().limit() < BUFF_LENGTH / 2;
    }

    public void write(SelectionKey selectionKey) throws IOException {
        ByteBuffer inputBuffer = connection.getInputBuffer();
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        socketChannel.write(inputBuffer);

        if(inputBuffer.remaining() == 0){
            selectionKey.interestOps(SelectionKey.OP_READ);
            inputBuffer.clear();
        }
    }

    public static int getBuffLength() {
        return BUFF_LENGTH;
    }
}
