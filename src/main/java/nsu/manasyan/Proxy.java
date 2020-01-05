package nsu.manasyan;

import nsu.manasyan.handlers.ConnectHandler;
import nsu.manasyan.handlers.Handler;
import nsu.manasyan.models.Connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
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

    private Map<SocketAddress, ByteBuffer> buffers = new HashMap<>();

//    private Set<SocketAddress> unconnectedUsers = new HashSet<>();

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
        if(selectionKey.isAcceptable()){
            addNewConnection(selectionKey, selector);
        }
        else if (selectionKey.isReadable()){
            handleRead(selectionKey);
        }
    }

    private void handleRead(SelectionKey selectionKey) throws IOException {
        Handler handler = (Handler) selectionKey.attachment();
        SocketChannel socket = (SocketChannel) selectionKey.channel();
        Connection connection = handler.getConnection();

        if(!isReadyToRead(connection))
            return;

        socket.read(connection.getInputBuffer());

        boolean addressResolved = handler.handle(selectionKey);
        if(!addressResolved){
            // call dns service
            return;
        }

        socket.write(connection.getOutputBuffer());
    }

    // todo check
    private boolean isReadyToRead(Connection connection){
        return connection.getInputBuffer().limit() < BUFF_LENGTH / 2;
    }

    private void addNewConnection(SelectionKey selectionKey, Selector selector) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        var socketAddress = socketChannel.getRemoteAddress();
        buffers.put(socketAddress, ByteBuffer.allocate(BUFF_LENGTH));

        Connection connection = new Connection(buffers.get(socketAddress),
                ByteBuffer.allocate(BUFF_LENGTH));

        ConnectHandler connectHandler = new ConnectHandler(connection);
        socketChannel.register(selector, SelectionKey.OP_READ, connectHandler);
    }
}