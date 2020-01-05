package nsu.manasyan.models;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Connection {
    private ByteBuffer outputBuffer;

    private ByteBuffer inputBuffer;

    private SocketChannel userToNotify;

    public Connection(ByteBuffer outputBuffer, ByteBuffer inputBuffer) {
        this.outputBuffer = outputBuffer;
        this.inputBuffer = inputBuffer;
    }

    public Connection(ByteBuffer outputBuffer){
        this.outputBuffer = outputBuffer;
    }

    public void setInputBuffer(ByteBuffer inputBuffer) {
        this.inputBuffer = inputBuffer;
    }

    public void setUserToNotify(SocketChannel userToNotify) {
        this.userToNotify = userToNotify;
    }

    public ByteBuffer getOutputBuffer() {
        return outputBuffer;
    }

    public ByteBuffer getInputBuffer() {
        return inputBuffer;
    }

    public void notifyUser(Selector selector) throws ClosedChannelException {
        if(userToNotify != null)
            userToNotify.register(selector, SelectionKey.OP_WRITE);
    }
}
