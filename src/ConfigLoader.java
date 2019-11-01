import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigLoader {

    private static final String CONFIG_PEER = "./config_peer.txt";
    private static final String CONFIG_NEIGHBORS = "./config_neighbors.txt";
    private static final String CONFIG_SHARING = "./config_sharing.txt";

    public static Map<String, String> loadPeerConfig() {
        try {
            Path path = Paths.get(CONFIG_PEER);
            List<String> lines = Files.readAllLines(path);
            Map<String, String> peerConfig = lines.stream().map(line -> line.split("="))
                    .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));
            return peerConfig;
        } catch (IOException e) {
            System.err.println("Error loading peer config");
            return new HashMap<>();
        }
    }

    public static List<SocketInfo> loadNeighborList() {
        try {
            Path path = Paths.get(CONFIG_NEIGHBORS);
            List<String> lines = Files.readAllLines(path);
            List<SocketInfo> socketInfoList = lines.stream().map(line -> line.split(","))
                    .map(arr -> new SocketInfo(arr[0], arr[1]))
                    .collect(Collectors.toList());
            return socketInfoList;
        } catch (IOException e) {
            System.err.println("Error loading neighbor list");
            return new ArrayList<>();
        }
    }

    public static List<String> loadSharingList() {
        try {
            Path path = Paths.get(CONFIG_SHARING);
            List<String> lines = Files.readAllLines(path);
            return lines;
        } catch (IOException e) {
            System.err.println("Error loading sharing list");
            return new ArrayList<>();
        }
    }
}
