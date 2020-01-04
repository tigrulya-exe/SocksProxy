package nsu.manasyan;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/*
* в днс резолвере должна быть мапа из [ строка хостнейма - ченел клиента ].
* в прокси должна быть мапа из [ инет сокет адрес - байт буфер, откуда будет чтение ].
* каждый класс Connection содержит в качетсве полей две ссылки на значения этой мапы и ченел второго
* участника соединения.
* */

public class Proxy {
    private final int proxyPort;

    public Proxy(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void start(){
        try(Selector selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

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
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void select(Selector selector) throws IOException {
        while (true){
            selector.select();

            var readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext()){
                var readyKey = iterator.next();
                iterator.remove();
                handleSelectionKey(readyKey);
            }
        }
    }

    private void handleSelectionKey(SelectionKey selectionKey){
        if(selectionKey.isAcceptable()){

        }
    }

    private void addNewConnection(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
    }
}
