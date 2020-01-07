package nsu.manasyan;

import nsu.manasyan.handlers.AcceptHandler;
import nsu.manasyan.handlers.Handler;
import nsu.manasyan.dns.DnsService;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;

/*
* в днс резолвере должна быть мапа из [ строка хостнейма - ченел клиента ].
* надо придумать как связать клиента и таргет, чтобы их закрывать одновременно (например хранить сокет ченелы опонента)
* */

public class Proxy {
    private final int proxyPort;

    public Proxy(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void start(){
        try(Selector selector = Selector.open();
            DatagramChannel datagramSocket = DatagramChannel.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            DnsService dnsService = DnsService.getInstance();
            datagramSocket.configureBlocking(false);
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
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                var readyKey = iterator.next();
                try {
                    iterator.remove();
                    if (readyKey.isValid())
                        handleSelectionKey(readyKey);
                } catch (IOException exception) {
                    closeConnection(readyKey);
                }
            }
        }
    }

    private void closeConnection(SelectionKey selectionKey) throws IOException {
        var handler = (Handler) selectionKey.attachment();
        var connection = handler.getConnection();
        var firstSocket = (SocketChannel) selectionKey.channel();

        // todo tmp
        firstSocket.close();
        connection.closeSecondUser();
    }

    private void handleSelectionKey(SelectionKey selectionKey) throws IOException {
        Handler handler = (Handler) selectionKey.attachment();

        if (selectionKey.isWritable()) {
            handler.write(selectionKey);
        }
        // not only writable
        if(selectionKey.readyOps() != SelectionKey.OP_WRITE)
            handler.handle(selectionKey);
        }

}