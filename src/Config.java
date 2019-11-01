import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the configuration data loaded from the config files
 */
@SuppressWarnings("unchecked")
public class Config extends HashMap<String, Object> {

    /**
     * Load all config data in to this instance upon instantiation
     */
    public Config() {
        this.put("peer", ConfigLoader.loadPeerConfig());
        this.put("neighbors", ConfigLoader.loadNeighborList());
        this.put("sharing", ConfigLoader.loadSharingList());
    }

    /**
     *
     * @return Port arrangements of this peer
     */
    public Map<String, String> getPeerConfig() {
        return (Map<String, String>) get("peer");
    }

    /**
     *
     * @return neighbor ip address and their ports
     */
    public List<SocketInfo> getNeighbors() {
        return (List<SocketInfo>) get("neighbors");
    }

    /**
     *
     * @return a list of file this peer is sharing across the network
     */
    public List<String> getSharing() {
        return (List<String>) get("sharing");
    }


}
