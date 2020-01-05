package nsu.manasyan.socks;

import nsu.manasyan.models.SocksConnectRequest;
import nsu.manasyan.models.SocksRequest;

import java.nio.ByteBuffer;

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
            // send unsupported
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
                request.setIp4Address(byteBuffer.getInt());
                return;
            case 0x03:
                // async resolve dns name;
                return;
        }

        // send unsupported
        System.out.println("Unsupported address type");
    }
}
