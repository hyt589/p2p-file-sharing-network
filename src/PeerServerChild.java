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

    private long sleepTime = 3*1000;

    public PeerServerChild(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        while (true) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String msg = in.readLine();
                Integer idBeforeQuery = Query.getCount().get();
                Objects.requireNonNull(msg);
                if (msg.equals("Heart beat check")) {
                    System.out.println("Heart beat check from " + client.getRemoteSocketAddress().toString());
                    DataOutputStream out = new DataOutputStream(client.getOutputStream());
                    out.writeBytes("Alive\n");
                    continue;
                }
                System.out.println(msg);
                Query query = new Query(msg);
                System.out.println("Received: " + msg + " from " + client.getRemoteSocketAddress().toString());
                if (query.type == QueryType.Q) {
                    handleQ(query, idBeforeQuery);
                }
            } catch (IOException | QueryFormatException e) {
                e.printStackTrace();
            } catch (NullPointerException npe) {
                //do nothing
            }
        }
    }

    /**
     * Handle the in coming Q query. Reply an R query to the sender if this is a hit; or forward to other peers otherwise.
     * Query id is checked before forwarding.
     * @param query - incoming query
     * @throws QueryFormatException
     * @throws IOException
     */
    private void handleQ(Query query, Integer id) throws QueryFormatException, IOException {
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
            if (query.id.equals(id)) {

                System.out.printf("Query id:%d is equal to current query count %d%n", query.id, Query.getCount().get());
                System.out.println("Abort forwarding to avoid broadcast storm");
                return;//do not forward if query id is smaller than Query.count

            }
            Query hit = null;
            System.out.println("No hit. Forwarding the query to neighbors");
            List<PeerClientThread> clients = neighbors.stream()
                    .map(info -> {
                        return new PeerClientThread(PeerClient.createClientSocket(info.getIP(), info.getPORT()), query.toString());
                    })
                    .collect(Collectors.toList());
            clients.forEach(PeerClientThread::start);
            while (clients.stream().allMatch(o -> Objects.isNull(o.getHit()))) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for (PeerClientThread clientThread :
                    clients) {
                System.out.println("Hit: " + (Objects.nonNull(clientThread.getHit()) ? clientThread.getHit() : "null"));
                if (Objects.nonNull(clientThread.getHit())) {
                    hit = clientThread.getHit();
                    System.out.println("Got a hit: " + hit.toString());
                    out.writeBytes(hit.toString()+ "\n"); //pass the query hit back to client
                    break;
                }
            }
            new CloseSocketThread(clients).start();
        }
    }


    private class CloseSocketThread extends Thread{

        List<PeerClientThread> threads;

        public CloseSocketThread(List<PeerClientThread> threads) {
            this.threads = threads;
        }

        @Override
        public void run() {
            threads.forEach(thread ->{
                try {
                    thread.join();
                    thread.closeSocket();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

}
