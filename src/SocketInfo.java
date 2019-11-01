/**
 * This class contains the necessary ip address and port number to construct a socket
 */
public class SocketInfo {

    private final String IP;

    private final int PORT;

    public SocketInfo(String ip, String port) {
        this.IP = ip;
        this.PORT = Integer.parseInt(port);
    }

    public String getIP() {
        return IP;
    }

    public int getPORT() {
        return PORT;
    }
}
