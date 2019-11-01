import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
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

            }
        } catch (IOException | QueryFormatException e) {
            e.printStackTrace();
        }
    }

    private void handleQ(Query query) throws QueryFormatException, IOException {
        DataOutputStream out = new DataOutputStream(client.getOutputStream());
        String filename = query.msgList.get(0);
        String clientAddress = client.getRemoteSocketAddress().toString();
        List<String> ipList = config.getNeighbors().stream().map(socketInfo -> socketInfo.getIP())
                .filter(ip -> !ip.equals(clientAddress))
                .collect(Collectors.toList());
        if (config.getSharing().contains(filename)) {
            Query response = new Query(QueryType.R, Arrays.asList(IPChecker.ip() + ":" + config.getPeerConfig().get("file_port"), filename));
            out.writeBytes(response.toString() + "\n");
        }else {
            //start a client thread for each ip in ipList
        }
    }
}
