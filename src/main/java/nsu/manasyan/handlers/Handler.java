package nsu.manasyan.handlers;

import nsu.manasyan.models.Connection;

import javax.crypto.spec.PSource;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

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

        if(!isReadyToRead(connection))
            return 0;

        var outputBuffer = connection.getOutputBuffer();
        int readCount = socket.read(outputBuffer);

        if(readCount < 0) {
            throw new IOException("Socket closed");
        }

        System.out.println("READ: " + readCount);

        return readCount;
    }

    private boolean isReadyToRead(Connection connection){
        return connection.getInputBuffer().position() < BUFF_LENGTH / 2;
    }

    public int write(SelectionKey selectionKey) throws IOException {
        ByteBuffer inputBuffer = connection.getInputBuffer();
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        inputBuffer.flip();
        int writtenCount = socketChannel.write(inputBuffer);
        System.out.println("WRITTEN: " + writtenCount);

        int remaining = inputBuffer.remaining();
        if(remaining == 0){
            selectionKey.interestOps(SelectionKey.OP_READ);
            inputBuffer.clear();
        }

        return remaining;
    }

    public static int getBuffLength() {
        return BUFF_LENGTH;
    }
}
