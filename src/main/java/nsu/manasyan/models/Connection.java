package nsu.manasyan.models;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Connection {
    // user write to
    private ObservableByteBuffer outputBuffer;

    // user read from
    private ObservableByteBuffer inputBuffer;

    private SocketChannel associate;

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

    public void setAssociate(SocketChannel associate) {
        this.associate = associate;
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

    public void closeAssociate() throws IOException {
        System.out.println("SECOND CLOSED");
        if(associate != null)
            associate.close();
    }

    public void shutdown(){
        outputBuffer.shutdown();
    }

    public boolean isAssociateShutDown(){
        return inputBuffer.isReadyToClose();
    }

    public boolean isReadyToClose(){
        return outputBuffer.isReadyToClose() && inputBuffer.isReadyToClose();
    }

    private String name = "Client ";

    public void setName() {
        this.name = "Server ";
    }

    public String getName() {
        return name;
    }
}
