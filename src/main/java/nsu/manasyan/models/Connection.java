package nsu.manasyan.models;

import java.nio.ByteBuffer;

public class Connection {
    // сюда пишет клиент
    private ObservableByteBuffer outputBuffer;

    // отсюда читает
    private ObservableByteBuffer inputBuffer;

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

    // for socks connect/request response
    public void notifySelf(){
        inputBuffer.notifyListener();
    }
}
