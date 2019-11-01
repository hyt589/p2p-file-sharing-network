public class Peer extends Thread {

    @Override
    public void run() {
        System.out.println("Starting peer...");
        PeerServerMain serverMainThread = new PeerServerMain();
        FileSender fileSender = new FileSender();
        serverMainThread.start();
        fileSender.start();
        PeerClient client = new PeerClient();
    }
}
