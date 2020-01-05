package nsu.manasyan.socks;

import nsu.manasyan.models.*;

import java.net.InetSocketAddress;

import static nsu.manasyan.socks.SocksParser.*;

public class SocksHandler {



//    public static void handleConnect(Connection connection){
//        SocksConnectRequest ConnectRequest = parseConnect(connection.getInputBuffer());
//        SocksConnectResponse connectResponse = new SocksConnectResponse();
//        if(!checkMethods(ConnectRequest.getMethods()))
//            connectResponse.setMethod(NO_COMPARABLE_METHOD);
//
//        var outputBuffer = connection.getOutputBuffer();
//        outputBuffer.put(connectResponse.toByteArr());
//    }

//    public static boolean handleRequest(Connection connection, InetSocketAddress socketAddress){
//        SocksRequest request = parseRequest(connection.getInputBuffer());
//        if(request.getAddressType() == DOMAIN_NAME_TYPE){
//            return false;
//        }
//
//        sendResponse(connection, socketAddress);
//        return true;
//    }

//    public static void sendResponse(Connection connection, InetSocketAddress socketAddress){
//        SocksResponse response = new SocksResponse();
//        var address = socketAddress.getAddress().getAddress();
//        response.setBoundIp4Address(address);
//        response.setBoundPort((short) socketAddress.getPort());
//    }

//    private static boolean checkMethods(byte[] methods){
//        for(var method : methods){
//            if(method == NO_AUTHENTICATION)
//                return true;
//        }
//
//        return false;
//    }


}
