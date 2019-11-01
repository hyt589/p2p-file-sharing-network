import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

public class PeerClient {

    private Config config = new Config();

    private List<SocketInfo> socketInfoList = config.getNeighbors();

    private List<Socket> sockets;

    private void connectToNeighbors() {
        sockets = socketInfoList.stream().map(info -> {
            try {
                System.out.println("Connecting to " + info.getIP());
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
            //TODO: start file receiver here
        } catch (QueryFormatException e) {
            e.printStackTrace();
        }
    }

    private void checkConnectionStatus() {
        try {
            String query = new Query(QueryType.E, Collections.singletonList("Check connection")).toString();
            Map<Socket, PeerClientConnection> sockThreadMap = sockets.stream()
                    .collect(Collectors.toMap(socket -> socket,
                            socket -> new PeerClientConnection((Socket) socket, query)));
            sockThreadMap.values().forEach(PeerClientConnection::start);
            sockThreadMap.values().forEach(thread -> {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            List<Socket> liveSockets = sockThreadMap.entrySet().stream()
                    .filter(entry -> Objects.nonNull(entry.getValue().getEcho()))
                    .map(Map.Entry::getKey).collect(Collectors.toList());
            List<Socket> deadSockets = sockThreadMap.entrySet().stream()
                    .filter(entry -> Objects.isNull(entry.getValue().getEcho()))
                    .map(Map.Entry::getKey).collect(Collectors.toList());
            liveSockets.forEach(socket -> System.out.println(socket.getRemoteSocketAddress().toString() + " is connected"));
            deadSockets.forEach(socket ->
                    System.out.println(socket.getRemoteSocketAddress().toString() + " did not reply to status check"));
        } catch (QueryFormatException e) {
            e.printStackTrace();
        }
    }

}
