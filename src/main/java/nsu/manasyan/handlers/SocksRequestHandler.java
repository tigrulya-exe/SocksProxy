package nsu.manasyan.handlers;

import nsu.manasyan.models.Connection;
import nsu.manasyan.socks.SocksResponse;
import nsu.manasyan.dns.DnsService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static nsu.manasyan.handlers.ConnectHandler.connectToTarget;
import static nsu.manasyan.socks.SocksParser.parseRequest;

public class SocksRequestHandler extends SocksHandler {
    private static final byte DOMAIN_NAME_TYPE = 0x03;

    private static final int NO_ERROR = 0;

    public SocksRequestHandler(Connection connection) {
        super(connection);
    }

    @Override
    public void handle(SelectionKey selectionKey) throws IOException {
        var connection = getConnection();
        var outputBuffer = connection.getOutputBuffer();
        outputBuffer.clear();

        read(selectionKey);
        var request = parseRequest(outputBuffer);
        var parseError = request.getParseError();

        if(parseError != NO_ERROR){
            putErrorResponseIntoBuf(selectionKey, parseError);
            selectionKey.attach(new SocksErrorHandler(connection));
            return;
        }

        if(request.getAddressType() == DOMAIN_NAME_TYPE){
            DnsService dnsService = DnsService.getInstance();
            dnsService.resolveName(request,selectionKey);
            return;
        }

        connectToTarget(selectionKey, request.getAddress());
    }


    public void putErrorResponseIntoBuf(SelectionKey selectionKey, byte error) throws IOException {
        var connection = getConnection();
        SocksResponse response = new SocksResponse();
        response.setReply(error);

        var inputBuff = connection.getInputBuffer();
        inputBuff.put(response.toByteBuffer());
        connection.getOutputBuffer().clear();
        selectionKey.interestOpsOr(SelectionKey.OP_WRITE);
    }
}
