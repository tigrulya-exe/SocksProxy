package nsu.manasyan.models;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Connection {
    // user write to
    private ObservableByteBuffer outputBuffer;

    // user read from
    private ObservableByteBuffer inputBuffer;

    private SocketChannel secondUser;

    public Connection(ObservableByteBuffer outputBuffer, ObservableByteBuffer inputBuffer) {
        this.outputBuffer = outputBuffer;
        this.inputBuffer = inputBuffer;
    }

    public Connection(int buffLength) {
        this.inputBuffer = new ObservableByteBuffer(ByteBuffer.allocate(buffLength));
        this.outputBuffer = new ObservableByteBuffer(ByteBuffer.allocate(buffLength));
    }

    public ByteBuffer getOutputBuffer() {
        return outputBuffer.getByteBuffer();
    }

    public void setSecondUser(SocketChannel secondUser) {
        this.secondUser = secondUser;
    }

    public ByteBuffer getInputBuffer() {
        return inputBuffer.getByteBuffer();
    }

    public ObservableByteBuffer getObservableOutputBuffer() {
        return outputBuffer;
    }

    public ObservableByteBuffer getObservableInputBuffer() {
        return inputBuffer;
    }

    public void registerBufferListener(ObservableByteBuffer.BufferListener bufferListener){
        inputBuffer.registerBufferListener(bufferListener);
    }

    public void notifyBufferListener(){
        outputBuffer.notifyListener();
    }

    public void closeSecondUser() throws IOException {
        System.out.println("SECOND CLOSED");
        if(secondUser != null)
            secondUser.close();
    }
}
