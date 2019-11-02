import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * This thread sends a message through the socket and waits for a response;
 * will timeout after period of time if no response are received
 */
public class PeerClientThread extends Thread{

    private final int TIME_OUT = 30*1000;

    private Socket socket;
    private String msg;
    private boolean timedOut = false;
    private Query hit = null;
    private String echo = null;

    public PeerClientThread(Socket socket, String msg) {
        this.socket = socket;
        this.msg = msg;
    }

    @Override
    public void run() {
        try {
            socket.setSoTimeout(TIME_OUT);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Sending \"" + msg + "\"to " + socket.getRemoteSocketAddress().toString());
            out.writeBytes(msg + "\n");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = in.readLine();
            if (response.equals("Alive")) {
                echo = response;
                return;
            }
            System.out.println(socket.getRemoteSocketAddress().toString() + " replied: " + response);
            Query r = new Query(response);
            if (r.type == QueryType.R) {
                hit = r;
            }
        } catch (SocketTimeoutException sto) {
            System.err.println("Query timed out.");
            timedOut = true;
        } catch (IOException | QueryFormatException e) {
            e.printStackTrace();
        }
    }


    public Query getHit() {
        return hit;
    }

    public boolean isTimedOut() {
        return timedOut;
    }

    public String getEcho() {
        return echo;
    }

    public void closeSocket() throws IOException {
        socket.close();
    }
}
