import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

public class Reception implements Runnable {

    public Diffuseur diffuseur;
    public Reception(Diffuseur diffuseur) {
        this.diffuseur = diffuseur;
    }

    public void run(){
        ServerSocket serverSocket=null;
        try {
            serverSocket = new ServerSocket(this.diffuseur.port);
        } catch (IOException ex) {
            System.out.println("Problème dans server socket");
            
            ex.printStackTrace();
            return;
        }
        while(true){
            try {
                Socket socket = serverSocket.accept();
                DiffReceptTCP drt = new DiffReceptTCP(socket,this.diffuseur);
                Thread thr = new Thread(drt);
                thr.start();
            } catch (Exception e) {
                
                e.printStackTrace();
                return;
            }
        }
        
        
    }

}
