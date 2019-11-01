import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class PeerServerMain extends Thread {

    private final String QUERY_PORT = "query_port";

    private ServerSocket serverSocket;

    private Config config = new Config();

    private List<PeerServerChild> subThreads = new ArrayList<>();

    private boolean keepListening = true;

    @Override
    public void run() {
        System.out.println("Peer server is running.");
        try {
            serverSocket = new ServerSocket(Integer.parseInt(config.getPeerConfig().get(QUERY_PORT)));
            while (keepListening) {
                Socket connection = serverSocket.accept();
                PeerServerChild child = new PeerServerChild(connection);
                subThreads.add(child);
                child.start();
            }
            subThreads.forEach(thread -> {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopListening() {
        keepListening = false;
    }
}
