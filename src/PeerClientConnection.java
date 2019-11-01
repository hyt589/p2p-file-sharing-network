import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class PeerClientConnection extends Thread{

    private final int TIME_OUT = 30*1000;

    private Socket socket;
    private String msg;
    private boolean timedOut = false;
    private Query hit = null;
    private Query echo = null;

    public PeerClientConnection(Socket socket, String msg) {
        this.socket = socket;
        this.msg = msg;
    }

    @Override
    public void run() {
        try {
            socket.setSoTimeout(TIME_OUT);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeBytes(msg + "\n");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = in.readLine();
            Query r = new Query(response);
            if (r.type == QueryType.R) {
                hit = r;
            }else if (r.type == QueryType.E) {

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

    public Query getEcho() {
        return echo;
    }

    public void closeSocket() throws IOException {
        socket.close();
    }
}
