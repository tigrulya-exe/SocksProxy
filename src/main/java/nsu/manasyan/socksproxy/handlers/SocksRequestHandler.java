package nsu.manasyan.socksproxy.handlers;

import nsu.manasyan.socksproxy.models.Connection;
import nsu.manasyan.socksproxy.socks.SocksResponse;
import nsu.manasyan.socksproxy.dns.DnsService;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import static nsu.manasyan.socksproxy.handlers.ConnectHandler.connectToTarget;
import static nsu.manasyan.socksproxy.socks.SocksParser.parseRequest;

public class SocksRequestHandler extends SocksHandler {
    private static final byte DOMAIN_NAME_TYPE = 0x03;

    private static final int NO_ERROR = 0;

    public SocksRequestHandler(Connection connection) {
        super(connection);
    }

    @Override
    public void handle(SelectionKey selectionKey) throws IOException {
        var outputBuffer = getConnection().getOutputBuffer();
//        outputBuffer.clear();

        read(selectionKey);
        var request = parseRequest(outputBuffer);
        if (request == null)
            return;

        var parseError = request.getParseError();

        if(parseError != NO_ERROR){
            onError(selectionKey, parseError);
            return;
        }

        if(request.getAddressType() == DOMAIN_NAME_TYPE){
            DnsService dnsService = DnsService.getInstance();
            dnsService.resolveName(request,selectionKey);
            return;
        }

        connectToTarget(selectionKey, request.getAddress());
    }

    public static void onError(SelectionKey selectionKey, byte error) {
        var handler = (Handler) selectionKey.attachment();
        var connection = handler.getConnection();

        putErrorResponseIntoBuf(selectionKey, connection, error);
        selectionKey.attach(new SocksErrorHandler(connection));
    }

    public static void putErrorResponseIntoBuf(SelectionKey selectionKey, Connection connection,  byte error) {
        var response = new SocksResponse();
        response.setReply(error);

        var inputBuff = connection.getInputBuffer();
        inputBuff.put(response.toByteBufferWithoutAddress());
        connection.getOutputBuffer().clear();
        selectionKey.interestOpsOr(SelectionKey.OP_WRITE);
    }
}
