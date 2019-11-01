import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Config extends HashMap<String, Object> {

    public Config() {
        this.put("peer", ConfigLoader.loadPeerConfig());
        this.put("neighbors", ConfigLoader.loadNeighborList());
        this.put("sharing", ConfigLoader.loadSharingList());
    }

    public Map<String, String> getPeerConfig() {
        return (Map<String, String>) get("peer");
    }

    public List<SocketInfo> getNeighbors() {
        return (List<SocketInfo>) get("neighbors");
    }

    public List<String> getSharing() {
        return (List<String>) get("sharing");
    }


}
