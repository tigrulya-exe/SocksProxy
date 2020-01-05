package nsu.manasyan.models;

import java.nio.ByteBuffer;

public class SocksConnectResponse {
    private byte version = 0x05;

    private byte method = 0x00;

    public void setMethod(byte method) {
        this.method = method;
    }

    public byte[] toByteArr(){
        return new byte[]{version, method};
    }
}
