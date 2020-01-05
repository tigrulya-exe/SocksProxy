package nsu.manasyan.handlers;

import nsu.manasyan.models.Connection;
import nsu.manasyan.models.SocksConnectRequest;
import nsu.manasyan.models.SocksConnectResponse;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import static nsu.manasyan.socks.SocksParser.*;

public class ConnectHandler extends Handler{
    private static final byte SOCKS_VERSION = 0x05;

    private static final byte NO_AUTHENTICATION = 0x00;

    private static final byte NO_COMPARABLE_METHOD = (byte) 0xFF;

    public ConnectHandler(Connection connection) {
        super(connection);
    }

    @Override
    public boolean handle(SelectionKey selectionKey) throws IOException {
        Connection connection = getConnection();

        SocksConnectRequest ConnectRequest = parseConnect(connection.getInputBuffer());
        SocksConnectResponse connectResponse = new SocksConnectResponse();
        if(!checkMethods(ConnectRequest.getMethods()))
            connectResponse.setMethod(NO_COMPARABLE_METHOD);

        var outputBuffer = connection.getOutputBuffer();
        outputBuffer.put(connectResponse.toByteArr());
        return true;
    }

    private static boolean checkMethods(byte[] methods){
        for(var method : methods){
            if(method == NO_AUTHENTICATION)
                return true;
        }

        return false;
    }
}
