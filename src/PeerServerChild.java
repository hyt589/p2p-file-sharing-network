import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Each PeerServerChild thread handles one connection.
 * It repeatedly listens for messages and responds to messages accordingly.
 */
public class PeerServerChild extends Thread {

    private Socket client;

    private Config config = new Config();

    public PeerServerChild(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        System.out.println(client.getRemoteSocketAddress().toString());
        try {
            while (true) {
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String msg = in.readLine();
                Query query = new Query(msg);
                System.out.println("Received: " + msg + " from " + client.getRemoteSocketAddress().toString());
                if (query.type == QueryType.Q) {
                    handleQ(query);
                } else if (query.type == QueryType.E) {
                    handleE(query);
                }
            }
        } catch (IOException | QueryFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle the in coming Q query. Reply an R query to the sender if this is a hit; or forward to other peers otherwise
     * @param query - incoming query
     * @throws QueryFormatException
     * @throws IOException
     */
    private void handleQ(Query query) throws QueryFormatException, IOException {
        DataOutputStream out = new DataOutputStream(client.getOutputStream());
        String filename = query.msgList.get(0);
        String clientAddress = client.getRemoteSocketAddress().toString();
        List<SocketInfo> neighbors = config.getNeighbors().stream()
                .filter(socketInfo -> !socketInfo.getIP().equals(clientAddress)) //filter out the sender
                .collect(Collectors.toList());
        if (config.getSharing().contains(filename)) { //this peer has the file, return hit
            System.out.println("Query hit. This peer has the file: " + filename);
            Query response = new Query(QueryType.R, Arrays.asList(IPChecker.ip() + ":"
                    + config.getPeerConfig().get("file_sender_port"), filename));
            out.writeBytes(response.toString() + "\n");
        }else { //forward the query to other neighbors
            System.out.println("No hit. Forwarding the query to neighbors");
            Query hit = null;
            List<PeerClientThread> clients = neighbors.stream()
                    .map(info -> {
                        return new PeerClientThread(PeerClient.createClientSocket(info.getIP(), info.getPORT()), query.toString());
                    })
                    .collect(Collectors.toList());
            clients.forEach(PeerClientThread::start);
            clients.forEach(client -> {
                try {
                    client.join();
                    client.closeSocket();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            });
            for (PeerClientThread client :
                    clients) {
                if (!client.isTimedOut() && Objects.nonNull(client.getHit())) {
                    hit = client.getHit();
                    out.writeBytes(hit.toString()); //pass the query hit back to client
                    break;
                }
            }
        }
    }

    /**
     * Reply to a heart beat check
     * @param query
     * @throws IOException
     * @throws QueryFormatException
     */
    private void handleE(Query query) throws IOException, QueryFormatException {
        DataOutputStream out = new DataOutputStream(client.getOutputStream());
        String response = new Query(QueryType.E, Collections.singletonList("Alive")).toString();
        System.out.println("Replying to heart beat check: " + response);
        out.writeBytes(response + "\n");
    }
}
