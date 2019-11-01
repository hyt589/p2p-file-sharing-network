import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

/**
 * Upon receiving a query hit, this class initiate a thread that sends a file request to the target peer, and download
 * the file if connection is successful
 */
public class FileReceiver extends Thread {

    private final String DIR = "./download/";

    private Socket socket;
    private String filename;

    public FileReceiver(Socket socket, String filename) {
        this.socket = socket;
        this.filename = filename;
    }

    @Override
    public void run() {
        try {
            String msg = new Query(QueryType.T, Collections.singletonList(filename)).toString();
            System.out.println("Requesting " + filename + " from " + socket.getRemoteSocketAddress().toString());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeBytes(msg + "\n");
            byte[] bytes = socket.getInputStream().readAllBytes();
            Path path = Paths.get(DIR + filename);
            Files.write(path, bytes, StandardOpenOption.CREATE_NEW);
            System.out.println("Download successful");
            socket.close();
        } catch (QueryFormatException | IOException e) {
            e.printStackTrace();
        }
    }
}
