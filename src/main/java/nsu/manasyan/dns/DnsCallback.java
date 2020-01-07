package nsu.manasyan.dns;

import java.io.IOException;

public interface DnsCallback {
    void onResponse() throws IOException;
}
