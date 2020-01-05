package nsu.manasyan.handlers;

import nsu.manasyan.models.Connection;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public abstract class Handler {
    private Connection connection;

    public Handler(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    abstract public boolean handle(SelectionKey selectionKey) throws IOException;
}
