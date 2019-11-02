import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

public class PeerClient {

    private Config config = new Config();

    private List<SocketInfo> socketInfoList = config.getNeighbors();

    private List<Socket> sockets;

    private long sleepTime = 2*1000;

    /**
     * Connect to all neighbors by creating sockets using the information in socket infos
     */
    public void connectToNeighbors() {
        sockets = socketInfoList.stream().map(info -> {
            System.out.println("Connecting to " + info.getIP());
            Socket connectionSocket = PeerClient.createClientSocket(info.getIP(), info.getPORT());
            System.out.println("Success");
            return connectionSocket;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Close connections to all neighbors
     */
    public void closeConnections() {
        sockets.forEach(socket -> {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Initiate a file request across the network
     * @param filename
     */
    public void getFile(String filename) {
        if (config.getSharing().contains(filename)) {
            System.out.println("This peer already has this file");
            return;
        }
        try {
            String query = new Query(QueryType.Q, Collections.singletonList(filename)).toString();
            List<PeerClientThread> connectionThreads = sockets.stream().map(socket ->
                    new PeerClientThread(socket, query)).collect(Collectors.toList());
            connectionThreads.forEach(PeerClientThread::start);
            connectionThreads.forEach(thread ->{
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            Query hit = null;
            int count = 0;
            while (count < 3 && connectionThreads.stream().allMatch(o -> Objects.isNull(o.getHit()))) {
                Thread.sleep(sleepTime);
            }
            for (PeerClientThread thread :
                    connectionThreads) {
                if (!thread.isTimedOut() && Objects.nonNull(thread.getHit())) {
                    hit = thread.getHit();
                    break;
                }
            }
            if (Objects.nonNull(hit)) {
                String ip = hit.msgList.get(0).split(":")[0];
                int port = Integer.parseInt(hit.msgList.get(0).split(":")[1]);
                int localPort = Integer.parseInt(config.getPeerConfig().get("file_receiver_port"));
                Socket fileSocket = new Socket(ip, port, InetAddress.getByName(IPChecker.ip()), localPort);
                FileReceiver fileReceiver = new FileReceiver(fileSocket, filename);
                fileReceiver.start();
            }else {
                System.out.println("Could not find the file");
            }
        } catch (QueryFormatException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Perform a heart beat check
     */
    public void checkConnectionStatus() {
        String query = "Heart beat check";
        Map<Socket, PeerClientThread> sockThreadMap = sockets.stream()
                .collect(Collectors.toMap(socket -> socket,
                        socket -> new PeerClientThread((Socket) socket, query)));
        sockThreadMap.values().forEach(PeerClientThread::start);
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
        deadSockets.forEach(socket -> {
            System.out.println(socket.getRemoteSocketAddress().toString() + " did not reply to status check");
            try {
                socket.close(); //close dead sockets
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * A wrapper method to create new sockets with only the assigned port numbers
     * @param remoteIp - the remote ip address to connect to
     * @param remotePort - remote peer's port
     * @return a socket inside the assigned port range that connects to the remote peer or null if socket creation failed
     */
    public static Socket createClientSocket(String remoteIp, int remotePort) {
        Config config = new Config();
        Socket socket = null;
        for (int i = Integer.parseInt(config.getPeerConfig().get("client_port_min")); i <= Integer.parseInt(config.getPeerConfig().get("client_port_max")); i++) {
            try {
                InetAddress localAddr = InetAddress.getByName(IPChecker.ip());
                socket = new Socket(remoteIp, remotePort, localAddr, i);
                break;
            } catch (BindException e) {
                System.err.println("Port "+ i + " already in use.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (Objects.isNull(socket)) {
            System.out.println("Connection failed.");
        }
        return socket;
    }

}
