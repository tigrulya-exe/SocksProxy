package nsu.manasyan.socks;

public class SocksConnectRequest {
    private byte version;

    private byte nMethods;

    private byte[] methods;

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getnMethods() {
        return nMethods;
    }

    public void setnMethods(byte nMethods) {
        this.nMethods = nMethods;
        this.methods = new byte[nMethods];
    }

    public byte[] getMethods() {
        return methods;
    }

    public void setMethods(byte[] methods) {
        this.methods = methods;
    }
}
