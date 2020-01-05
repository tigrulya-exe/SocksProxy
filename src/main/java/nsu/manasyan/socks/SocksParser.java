package nsu.manasyan.socks;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

// тут надо быть увереным, что коннект пришел полностью
public class SocksParser {
    public static SocksConnectRequest parseConnect(ByteBuffer byteBuffer){
        SocksConnectRequest connect = new SocksConnectRequest();
        connect.setVersion(byteBuffer.get());
        connect.setnMethods(byteBuffer.get());
        byteBuffer.get(connect.getMethods());
//        byteBuffer.get(connect.getMethods(), 0, connect.getnMethods());
        return connect;
    }

    public static SocksRequest parseRequest(ByteBuffer byteBuffer){
        SocksRequest request = new SocksRequest();
        request.setVersion(byteBuffer.get());

        byte command = byteBuffer.get();
        if(command != 0x01){
            // todo send unsupported
        }
        request.setCommand(command);
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
                // async resolve dns name;
                return;
        }

        // todo send unsupported
        System.out.println("Unsupported address type");
    }

    private static String getDomainName(ByteBuffer byteBuffer){
        byte nameLength = byteBuffer.get();
        byte[] nameBytes = new byte[nameLength];
        byteBuffer.get(nameBytes);

        return new String(nameBytes, StandardCharsets.UTF_8);
    }
}
