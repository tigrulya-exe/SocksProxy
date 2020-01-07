package nsu.manasyan;

import org.xbill.DNS.ResolverConfig;

import java.util.Arrays;

public class Main {
    // todo tmp
    public static void main(String[] args) {
        try {
            Proxy proxy = new Proxy(Integer.parseInt(args[0]));
            proxy.start();
        } catch (Exception exc){
            System.out.println("Wrong arguments");
            exc.printStackTrace();
        }
    }
}
