import java.net.Socket;

public class PeerClientConnection extends Thread {

    private Socket sock;

    public PeerClientConnection(Socket sock) {
        this.sock = sock;
    }
}
