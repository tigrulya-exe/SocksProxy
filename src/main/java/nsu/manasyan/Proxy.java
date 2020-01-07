package nsu.manasyan;

import nsu.manasyan.handlers.AcceptHandler;
import nsu.manasyan.handlers.Handler;
import nsu.manasyan.dns.DnsService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
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
                try {
                    var readyKey = iterator.next();
                    iterator.remove();
                    if(readyKey.isValid())
                        handleSelectionKey(readyKey);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    private void handleSelectionKey(SelectionKey selectionKey) throws IOException {
        Handler handler = (Handler) selectionKey.attachment();

        if (selectionKey.isWritable()) {
            handler.write(selectionKey);
        }
        handler.handle(selectionKey);
    }

}