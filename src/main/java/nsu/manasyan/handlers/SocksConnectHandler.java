package nsu.manasyan.handlers;

import nsu.manasyan.models.Connection;
import nsu.manasyan.socks.SocksConnectRequest;
import nsu.manasyan.socks.SocksConnectResponse;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import static nsu.manasyan.socks.SocksParser.*;

public class SocksConnectHandler extends Handler{
    private static final byte NO_AUTHENTICATION = 0x00;

    private static final byte NO_COMPARABLE_METHOD = (byte) 0xFF;

    public SocksConnectHandler(Connection connection) {
        super(connection);
    }

    @Override
    public void handle(SelectionKey selectionKey) throws IOException {
        Connection connection = getConnection();
        var outputBuffer = connection.getOutputBuffer();
        outputBuffer.clear();
        read(selectionKey);

        SocksConnectRequest ConnectRequest = parseConnect(outputBuffer);
        SocksConnectResponse connectResponse = new SocksConnectResponse();
        if(!checkMethods(ConnectRequest.getMethods()))
            connectResponse.setMethod(NO_COMPARABLE_METHOD);

        var inputBuffer = connection.getInputBuffer();
        inputBuffer.put(connectResponse.toByteArr());
//        inputBuffer.flip();

        selectionKey.interestOpsOr(SelectionKey.OP_WRITE);
        selectionKey.attach(new SocksRequestHandler(connection));
    }

    private static boolean checkMethods(byte[] methods){
        for(var method : methods){
            if(method == NO_AUTHENTICATION)
                return true;
        }

        return false;
    }
}
