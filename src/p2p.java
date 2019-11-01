/**
 * This is the main class of this peer
 */
public class p2p {

    public static void main(String[] args) throws InterruptedException {
        Peer peer = new Peer();
        peer.start();
        peer.join();
    }
}
