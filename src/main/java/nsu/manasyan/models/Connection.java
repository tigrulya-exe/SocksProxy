package nsu.manasyan.models;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Connection {
    private ByteBuffer writeBuffer;

    private ByteBuffer readBuffer;

    private SocketChannel userToNotify;

    public Connection(ByteBuffer writeBuffer, ByteBuffer readBuffer) {
        this.writeBuffer = writeBuffer;
        this.readBuffer = readBuffer;
    }

    public void setUserToNotify(SocketChannel userToNotify) {
        this.userToNotify = userToNotify;
    }

    public ByteBuffer getWriteBuffer() {
        return writeBuffer;
    }

    public ByteBuffer getReadBuffer() {
        return readBuffer;
    }

    public void notifyUser(Selector selector) throws ClosedChannelException {
        if(userToNotify != null)
            userToNotify.register(selector, SelectionKey.OP_WRITE);
    }
}
