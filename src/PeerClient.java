import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PeerClient {

    private Config config = new Config();

    private List<SocketInfo> socketInfoList = config.getNeighbors();

    private List<Socket> sockets;

    private void connectToNeighbors() {
        sockets = socketInfoList.stream().map(info -> {
            try {
                return new Socket(info.getIP(), info.getPORT());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private void closeConnections() {
        sockets.forEach(socket -> {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void getFile(String filename) {
        try {
            String query = new Query(QueryType.Q, Collections.singletonList(filename)).toString();
            List<PeerClientConnection> connectionThreads = sockets.stream().map(socket ->
                    new PeerClientConnection(socket, query)).collect(Collectors.toList());
            connectionThreads.forEach(PeerClientConnection::start);
            connectionThreads.forEach(thread ->{
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            Query hit = null;
            for (PeerClientConnection thread :
                    connectionThreads) {
                if (!thread.isTimedOut() && Objects.nonNull(thread.getHit())) {
                    hit = thread.getHit();
                    break;
                }
            }
        } catch (QueryFormatException e) {
            e.printStackTrace();
        }
    }

    private void checkConnectionStatus() {
        try {
            String query = new Query(QueryType.E, Collections.singletonList("Check connection")).toString();
            List<PeerClientConnection> connectionThreads = sockets.stream().map(socket ->
                    new PeerClientConnection(socket, query)).collect(Collectors.toList());
            connectionThreads.forEach(PeerClientConnection::start);
            connectionThreads.forEach(thread ->{
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } catch (QueryFormatException e) {
            e.printStackTrace();
        }
    }

}
