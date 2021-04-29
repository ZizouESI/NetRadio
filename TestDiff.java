import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;




public class TestDiff {
    public static void main(String[] args) {
        try {
            MulticastSocket multicastSocket = new MulticastSocket(5151);
            multicastSocket.joinGroup(InetAddress.getByName("225.10.20.30"));
            byte[] arrayOfByte = new byte[160];
            DatagramPacket datagramPacket = new DatagramPacket(arrayOfByte, arrayOfByte.length);
            while(true){
                multicastSocket.receive(datagramPacket);
                String str = new String(datagramPacket.getData(),0,datagramPacket.getLength());
                System.out.println("J'ai recu  : "  +str);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
