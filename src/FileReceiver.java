import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

public class FileReceiver extends Thread {

    private final String DIR = "~/p2p/download/";

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
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeBytes(msg + "\n");
            byte[] bytes = socket.getInputStream().readAllBytes();
            Path path = Paths.get(DIR + filename);
            Files.write(path, bytes, StandardOpenOption.CREATE_NEW);
        } catch (QueryFormatException | IOException e) {
            e.printStackTrace();
        }
    }
}
