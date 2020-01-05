package nsu.manasyan.handlers;

import nsu.manasyan.models.Connection;
import nsu.manasyan.models.SocksRequest;
import nsu.manasyan.models.SocksResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static nsu.manasyan.socks.SocksParser.parseRequest;

public class RequestHandler extends Handler {
    private static final byte DOMAIN_NAME_TYPE = 0x03;

    public RequestHandler(Connection connection) {
        super(connection);
    }

    @Override
    public boolean handle(SelectionKey selectionKey) throws IOException {
        Connection connection = getConnection();

        SocksRequest request = parseRequest(connection.getInputBuffer());
        if(request.getAddressType() == DOMAIN_NAME_TYPE){
            return false;
        }

        sendResponse(connection, selectionKey);
        return true;
    }

    public static void sendResponse(Connection connection, SelectionKey selectionKey) throws IOException {
        var socketChannel = (SocketChannel) selectionKey.channel();
        var socketAddress = (InetSocketAddress) socketChannel.getLocalAddress();

        SocksResponse response = new SocksResponse();
        var address = socketAddress.getAddress().getAddress();
        response.setBoundIp4Address(address);
        response.setBoundPort((short) socketAddress.getPort());

        var outputBuffer = connection.getOutputBuffer();
        outputBuffer.rewind();
        outputBuffer.put(response.toByteBuffer().array());
        outputBuffer.flip();
    }

}
