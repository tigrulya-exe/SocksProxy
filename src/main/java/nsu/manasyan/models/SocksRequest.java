package nsu.manasyan.models;

public class SocksRequest {
    private byte version;

    private byte command;

    private byte addressType;

    private int ip4Address;

    private String domainName;

    private short targetPort;

    public void setVersion(byte version) {
        this.version = version;
    }

    public void setCommand(byte command) {
        this.command = command;
    }

    public void setAddressType(byte addressType) {
        this.addressType = addressType;
    }

    public void setIp4Address(int ip4Address) {
        this.ip4Address = ip4Address;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public void setTargetPort(short targetPort) {
        this.targetPort = targetPort;
    }

    public byte getAddressType() {
        return addressType;
    }
}
