import java.awt.Dimension;
import java.awt.Font;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ClientRequests implements Runnable{

    public Client cl;

    public ClientRequests(Client cl){
        this.cl=cl;
    }

    @Override
    public void run() {
        try {
            MulticastSocket multicastSocket = new MulticastSocket(this.cl.portDiff);
            multicastSocket.joinGroup(InetAddress.getByName(this.cl.addresseDiff));
            byte[] msg = new byte[161];
            DatagramPacket datagramPacket=new DatagramPacket(msg, msg.length);
            /*FileOutputStream f=new FileOutputStream("/dev/pts/0");
            System.setOut(new PrintStream(f));
            System.out.println("\n");*/
            //Lancement d'une fenêtre ou afficher les messages
            JFrame jFrame = new JFrame("NetRadio App "+this.cl.addresseDiff+"/"+this.cl.portDiff);
            JTextArea jTextArea = new JTextArea("");
            jTextArea.setFont(new Font("TimesRoman",0,13));
            jTextArea.setLineWrap(true);
            jTextArea.setWrapStyleWord(true);
            jTextArea.setEditable(false);
            jFrame.add(jTextArea);
            JScrollPane jScrollPane=new JScrollPane(jTextArea);
            jScrollPane.setVerticalScrollBarPolicy(22);
            jScrollPane.setPreferredSize(new Dimension(300,300));
            jFrame.add(jScrollPane);
            jFrame.setSize(1000,500);
            jFrame.setVisible(true);
            while(true){
                multicastSocket.receive(datagramPacket);
                String str = new String(datagramPacket.getData(),0,datagramPacket.getLength());
                if(checkClReqMsg(str)){
                    String m = "";
                    m= m + str.substring(5,9);
                    m= m +"/";
                    m= m + str.substring(10, 18);
                    m= m + ": ";
                    m= m + Standard.restituerMsg(str.substring(19, 159)) + "\n";
                    //System.out.println(m);
                    jTextArea.insert(m,0);
                    
                }else{
                    System.out.println("Erreur dans le message recu !");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    public static boolean checkClReqMsg(String str) {
        if(str.length() != 161){
            System.out.println("Erreur dans la longeure du message recu !");
            return false;
        
        }else if(!str.substring(0,4).equals("DIFF")){
            System.out.println("Erreur , le message ne commence pas par DIFF");
            return false;
        }else if(!str.substring(159,161).equals("\r\n")){
            System.out.println("Erreur , le message ne se termine pas correctement");
            return false;
        }else if(str.charAt(4) == ' ' && str.charAt(9) == ' ' && str.charAt(18) == ' '){
            return true;
        }else{
            System.out.println("Erreur dans le reception de message");
            return false;
        }
    }

    
}
