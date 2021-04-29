import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class Envoi implements Runnable{
    public Diffuseur diffuseur;

    public Envoi(Diffuseur diffuseur) {
        this.diffuseur =diffuseur;
    }
    public void run(){
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(this.diffuseur.addressDiff, this.diffuseur.portDiff);
            DatagramPacket datagramPacket ;
            String cmd="";
            int index=0;
            while(true){
                Thread.sleep(1000L);
                cmd = "DIFF " +Standard.standardiserNumMsg(index)+ " ";
                String msg=this.diffuseur.msgs.getNextMessage();
                cmd = cmd + msg;
                cmd = cmd + "\r\n";
                byte[] bytes = cmd.getBytes();
                datagramPacket= new DatagramPacket(bytes, bytes.length,inetSocketAddress);
                datagramSocket.send(datagramPacket);
                synchronized(this.diffuseur.dernier){
                    String oldm= "OLDM " + Standard.standardiserNumMsg(index) + " ";
                    oldm= oldm + msg;
                    oldm = oldm +"\r\n";
                    this.diffuseur.dernier.add(oldm);
                    if(this.diffuseur.dernier.size() > 1000)
                        this.diffuseur.dernier.remove(0);
                }
                index = (index+1) % 10000;
            }
        
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
