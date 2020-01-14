package nsu.manasyan.socksproxy;

import nsu.manasyan.socksproxy.handlers.AcceptHandler;
import nsu.manasyan.socksproxy.handlers.Handler;
import nsu.manasyan.socksproxy.dns.DnsService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;

public class Proxy {
    private final int proxyPort;

    public Proxy(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void start(){
        try(Selector selector = Selector.open();
            var serverSocketChannel = ServerSocketChannel.open()) {

            var datagramSocket = DatagramChannel.open();
            datagramSocket.configureBlocking(false);

            var dnsService = DnsService.getInstance();
            dnsService.setSocket(datagramSocket);
            dnsService.registerSelector(selector);

            initServerSocketChannel(serverSocketChannel, selector);
            select(selector);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initServerSocketChannel(ServerSocketChannel serverSocketChannel,
                                        Selector selector) throws IOException {
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(proxyPort));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT,
                new AcceptHandler(serverSocketChannel));
    }

    private void select(Selector selector) throws IOException {
        while (true) {
            selector.select();
            var readyKeys = selector.selectedKeys();
            var iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                var readyKey = iterator.next();
                try {
                    iterator.remove();
                    if(readyKey.isValid())
                        handleSelectionKey(readyKey);
                } catch (IOException exception) {
                    closeConnection(readyKey);
                } catch (CancelledKeyException exc){
                }
            }
        }
    }

    private void closeConnection(SelectionKey selectionKey) throws IOException {
        var handler = (Handler) selectionKey.attachment();
        var connection = handler.getConnection();
        var firstSocket = (SocketChannel) selectionKey.channel();

        try {
            System.out.println("Socket closed: " + firstSocket.getRemoteAddress());
            firstSocket.close();
            connection.closeAssociate();
        } catch (ClosedChannelException cce){
            System.out.println(cce.getLocalizedMessage());
        }
    }

    private void handleSelectionKey(SelectionKey selectionKey) throws IOException {
        Handler handler = (Handler) selectionKey.attachment();

        if (selectionKey.isWritable()) {
            handler.write(selectionKey);
        }

        // not only writable
        if(selectionKey.isValid() && selectionKey.readyOps() != SelectionKey.OP_WRITE)
            handler.handle(selectionKey);
        }
}