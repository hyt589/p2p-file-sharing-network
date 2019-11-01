import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String msg = in.readLine();
            Query query = new Query(msg);
            if (query.type == QueryType.Q) {
                handleQ(query);
            } else if (query.type == QueryType.E) {
                handleE(query);
            }
        } catch (IOException | QueryFormatException e) {
            e.printStackTrace();
        }
    }

    private void handleQ(Query query) throws QueryFormatException, IOException {
        DataOutputStream out = new DataOutputStream(client.getOutputStream());
        String filename = query.msgList.get(0);
        String clientAddress = client.getRemoteSocketAddress().toString();
        List<SocketInfo> neighbors = config.getNeighbors().stream()
                .filter(socketInfo -> !socketInfo.getIP().equals(clientAddress))
                .collect(Collectors.toList());
        if (config.getSharing().contains(filename)) {
            Query response = new Query(QueryType.R, Arrays.asList(IPChecker.ip() + ":"
                    + config.getPeerConfig().get("file_port"), filename));
            out.writeBytes(response.toString() + "\n");
        }else {
            Query hit = null;
            List<PeerClientConnection> clients = neighbors.stream()
                    .map(info -> {
                        try {
                            return new PeerClientConnection(new Socket(info.getIP(), info.getPORT()), query.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }).filter(Objects::nonNull)
                    .collect(Collectors.toList());
            clients.forEach(PeerClientConnection::start);
            clients.forEach(client -> {
                try {
                    client.join();
                    client.closeSocket();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            });
            for (PeerClientConnection client :
                    clients) {
                if (!client.isTimedOut() && Objects.nonNull(client.getHit())) {
                    hit = client.getHit();
                    out.writeBytes(hit.toString()); //pass the query hit back to client
                    break;
                }
            }
        }
    }

    private void handleE(Query query) throws IOException, QueryFormatException {
        DataOutputStream out = new DataOutputStream(client.getOutputStream());
        String response = new Query(QueryType.E, Collections.singletonList("Alive")).toString();
    }
}
