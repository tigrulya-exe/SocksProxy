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

    public int read(SelectionKey selectionKey) throws IOException {
        var handler = (Handler) selectionKey.attachment();
        var socket = (SocketChannel) selectionKey.channel();
        var connection = handler.getConnection();
        var outputBuffer = connection.getOutputBuffer();

        if(!isReadyToRead(outputBuffer))
            return 0;

        int readCount = socket.read(outputBuffer);

        if(readCount < 0) {
            connection.shutdown();
            selectionKey.interestOps(0);
            checkConnectionClose(socket);
        }

        System.out.println("READ: " + readCount);

        return readCount;
    }

    private boolean isReadyToRead(ByteBuffer buffer){
        return buffer.position() < BUFF_LENGTH / 2;
    }

    public int write(SelectionKey selectionKey) throws IOException {
        var inputBuffer = connection.getInputBuffer();
        var socketChannel = (SocketChannel) selectionKey.channel();

        inputBuffer.flip();
        int writtenCount = socketChannel.write(inputBuffer);
        System.out.println("WRITTEN: " + writtenCount);

        int remaining = inputBuffer.remaining();
        if(remaining == 0){
            selectionKey.interestOps(SelectionKey.OP_READ);
            checkAssociate(socketChannel);
            inputBuffer.clear();
        }

        if(writtenCount == 0){
            System.out.println("kl");
        }

        return remaining;
    }

    public static int getBuffLength() {
        return BUFF_LENGTH;
    }

    private void checkConnectionClose(SocketChannel socketChannel) throws IOException {
        if(connection.isReadyToClose()){
            socketChannel.close();
            connection.closeAssociate();
        }
    }

    private void checkAssociate(SocketChannel socketChannel) throws IOException {
        if(connection.isAssociateShutDown()){
            socketChannel.shutdownOutput();
            System.out.println("OUTPUT SHUT DOWN");
        }
    }
}
