import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

public class PeerClient {

    private Config config = new Config();

    private List<SocketInfo> socketInfoList = config.getNeighbors();

    private List<Socket> sockets;

    public void connectToNeighbors() {
        sockets = socketInfoList.stream().map(info -> {
            System.out.println("Connecting to " + info.getIP());
            return PeerClient.createClientSocket(info.getIP(), info.getPORT());
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void closeConnections() {
        sockets.forEach(socket -> {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void getFile(String filename) {
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
            if (Objects.nonNull(hit)) {
                String ip = hit.msgList.get(0).split(":")[0];
                int port = Integer.parseInt(hit.msgList.get(0).split(":")[1]);
                int localPort = Integer.parseInt(config.getPeerConfig().get("file_port"));
                Socket fileSocket = new Socket(ip, port, InetAddress.getByName(IPChecker.ip()), localPort);
                FileReceiver fileReceiver = new FileReceiver(fileSocket, filename);
                fileReceiver.start();
            }
        } catch (QueryFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    public void checkConnectionStatus() {
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

    public static Socket createClientSocket(String remoteIp, int remotePort) {
        Config config = new Config();
        Socket socket = null;
        for (int i = Integer.parseInt(config.getPeerConfig().get("client_port_min")); i <= Integer.parseInt(config.getPeerConfig().get("client_port_max")); i++) {
            try {
                InetAddress localAddr = InetAddress.getByName(IPChecker.ip());
                socket = new Socket(remoteIp, remotePort, localAddr, i);
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return socket;
    }

}
