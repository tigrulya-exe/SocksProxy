package nsu.manasyan.socksproxy;

public class ArgResolver {
    private final static int PORT_INDEX = 0;

    private final static int ARGS_COUNT = 1;

    public static int resolve(String[] args) throws IllegalArgumentException{
        if(args.length != ARGS_COUNT){
            throw new IllegalArgumentException();
        }

        return Integer.parseInt(args[PORT_INDEX]);
    }

    public static String getUsage(){
        return "java -jar socksProxy.jar <port>";
    }
}
