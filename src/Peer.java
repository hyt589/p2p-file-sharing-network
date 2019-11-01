import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A master thread of the main program
 */
public class Peer extends Thread {

    private PeerClient client;
    private PeerServerMain serverMainThread;

    private boolean isConnected = false;
    private long heartBeatInterval = 10*1000;

    private final String CONNECT = "(?i)connect";
    private final String GET = "((?i)get) +[^ ]+";
    private final String LEAVE = "(?i)leave";
    private final String EXIT = "(?i)(exit|quit)";

    @Override
    public void run() {
        System.out.println("Starting peer...");
        serverMainThread = new PeerServerMain();
        FileSender fileSender = new FileSender();
        serverMainThread.start();
        fileSender.start();
        client = new PeerClient();
        try {
            commandLineInterface();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method reads commands from standard in and respond accordingly
     * @throws IOException
     */
    private void commandLineInterface() throws IOException {
        System.out.println("Command line interface is now available. Please type commands below.");
        while (true) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String input = reader.readLine();
            if (input.matches(CONNECT)) {
                client.connectToNeighbors();
                isConnected = true;
                HeartBeat heartBeat = new HeartBeat();
                heartBeat.start();
            } else if (input.matches(GET)) {
                String filename = input.split(" +")[1];
                client.getFile(filename);
            } else if (input.matches(LEAVE)) {
                client.closeConnections();
                serverMainThread.stopListening();
                serverMainThread.closeConnections();
                isConnected = false;
            } else if (input.matches(EXIT)) {
                System.out.println("Bye.");
                System.exit(0);
            } else {
                System.err.println("Invalid command: " + input);
            }
        }
    }

    /**
     * Periodically perform heart beat check
     */
    private class HeartBeat extends Thread {
        @Override
        public void run() {
            while (isConnected) {
                client.checkConnectionStatus();
                try {
                    Thread.sleep(heartBeatInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
