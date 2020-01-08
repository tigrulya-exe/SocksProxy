package nsu.manasyan.socksproxy.socks;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

// тут надо быть увереным, что коннект пришел полностью
public class SocksParser {
    private static final byte WRONG_ADDRESS_TYPE = 0x08;

    private static final byte WRONG_COMMAND = 0x07;

    public static SocksConnectRequest parseConnect(ByteBuffer byteBuffer){
        byteBuffer.flip();

        SocksConnectRequest connect = new SocksConnectRequest();
        connect.setVersion(byteBuffer.get());
        connect.setnMethods(byteBuffer.get());
        byteBuffer.get(connect.getMethods());
//        byteBuffer.clear();
        return connect;
    }

    public static SocksRequest parseRequest(ByteBuffer byteBuffer){
        byteBuffer.flip();

        SocksRequest request = new SocksRequest();
        request.setVersion(byteBuffer.get());

        byte command = byteBuffer.get();
        if(command != 0x01){
            request.setParseError(WRONG_COMMAND);
        }

        request.setCommand(command);
        byteBuffer.get();
        checkAddressType(byteBuffer.get(), byteBuffer, request);
        request.setTargetPort(byteBuffer.getShort());
        return request;
    }

    private static void checkAddressType(byte addressType, ByteBuffer byteBuffer, SocksRequest request){
        request.setAddressType(addressType);

        switch (addressType){
            case 0x01:
                byteBuffer.get(request.getIp4Address());
                return;
            case 0x03:
                request.setDomainName(getDomainName(byteBuffer));
                return;
        }

        request.setParseError(WRONG_ADDRESS_TYPE);
    }

    private static String getDomainName(ByteBuffer byteBuffer){
        byte nameLength = byteBuffer.get();
        byte[] nameBytes = new byte[nameLength];
        byteBuffer.get(nameBytes);

        return new String(nameBytes, StandardCharsets.UTF_8);
    }
}
