import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PeerClient {

    private Config config = new Config();

    private List<PeerClientConnection> connections;

    /**
     * Connect to the socket described by SocketInfo
     * @param info
     * @return Socket object if connection is success, or null if otherwise
     */
    private Socket connectToSocket(SocketInfo info){
        try {
            return new Socket(info.getIP(),info.getPORT());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void connectAllNeighbors(){
        connections = config.getNeighbors().stream().map(this::connectToSocket)
                .filter(Objects::nonNull)
                .map(PeerClientConnection::new)
                .collect(Collectors.toList());
        connections.forEach(PeerClientConnection::start);
    }
}
