package nsu.manasyan.socks;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class SocksRequest {
    private byte version;

    private byte command;

    private byte addressType;

    private byte[] ip4Address = new byte[4];

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

    public byte[] getIp4Address() {
        return ip4Address;
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

    public InetSocketAddress getAddress() throws UnknownHostException {
        return new InetSocketAddress(InetAddress.getByAddress(ip4Address), targetPort);
    }
}
