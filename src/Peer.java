public class Peer extends Thread {

    @Override
    public void run() {
        System.out.println("Starting peer...");
        PeerServerMain serverMainThread = new PeerServerMain();
        serverMainThread.start();
        PeerClient client = new PeerClient();
    }
}
