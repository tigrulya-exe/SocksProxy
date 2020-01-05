package nsu.manasyan;

import nsu.manasyan.handlers.SocksConnectHandler;
import nsu.manasyan.handlers.Handler;
import nsu.manasyan.models.Connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

/*
* в днс резолвере должна быть мапа из [ строка хостнейма - ченел клиента ].
* в прокси должна быть мапа из [ инет сокет адрес - байт буфер, куда будет запись ].
* каждый класс Connection содержит в качетсве полей две ссылки на значения этой мапы и ченел второго
* участника соединения.
* */

public class Proxy {
    private final int proxyPort;

    private static final int BUFF_LENGTH = 8192;

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
        while (true) {
            selector.select();

            var readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                try {
                    var readyKey = iterator.next();
                    iterator.remove();
                    handleSelectionKey(readyKey, selector);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    private void handleSelectionKey(SelectionKey selectionKey, Selector selector) throws IOException {
        if(!selectionKey.isValid()){
            return;
        }
        Handler handler = (Handler) selectionKey.attachment();

        if(selectionKey.isAcceptable()){
            addNewConnection(selectionKey, selector);
        } else if (selectionKey.isWritable()) {
            handler.write(selectionKey);
        }
        handler.handle(selectionKey);
    }

    // todo move to AcceptHandler
    private void addNewConnection(SelectionKey selectionKey, Selector selector) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
//        var socketAddress = socketChannel.getRemoteAddress();
//        buffers.put(socketAddress, ByteBuffer.allocate(BUFF_LENGTH));

//        Connection connection = new Connection(buffers.get(socketAddress),
//                ByteBuffer.allocate(BUFF_LENGTH));
        Connection connection = new Connection(BUFF_LENGTH);
        SocksConnectHandler connectHandler = new SocksConnectHandler(connection);

        var key = socketChannel.register(selector, SelectionKey.OP_READ, connectHandler);
        connection.registerBufferListener(() -> key.interestOpsOr(SelectionKey.OP_WRITE));
    }

}