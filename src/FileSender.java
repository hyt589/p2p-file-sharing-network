import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSender extends Thread {
    private ServerSocket serverSocket;
    private Config config = new Config();

    private final String FILE_PORT = "file_port";
    private final String FILE_PATH = "./project1-files/";


    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(Integer.parseInt(config.getPeerConfig().get(FILE_PORT)));
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Incoming file request from " + client.getRemoteSocketAddress());
                SenderThread thread = new SenderThread(client);
                thread.start();
                thread.join();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class SenderThread extends Thread {
        Socket socket;

        public SenderThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message = in.readLine();
                Query query = new Query(message);
                String filename = query.msgList.get(0);
                System.out.println(socket.getRemoteSocketAddress() + " requested " + filename);
                Path path = Paths.get(FILE_PATH + filename);
                byte[] bytes = Files.readAllBytes(path);
                socket.getOutputStream().write(bytes);
                in.close();
                socket.close();
            } catch (IOException | QueryFormatException e) {
                e.printStackTrace();
            }
        }
    }

}
