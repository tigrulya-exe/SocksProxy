package nsu.manasyan.models;

import java.nio.ByteBuffer;

public class ObservableByteBuffer {
    private ByteBuffer byteBuffer;

    public interface BufferListener{
        void onUpdate();
    }

    private BufferListener bufferListener;

    public ObservableByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public void notifyListener(){
        bufferListener.onUpdate();
    }

    public void registerBufferListener(ObservableByteBuffer.BufferListener bufferListener){
        this.bufferListener = bufferListener;
    }
}
