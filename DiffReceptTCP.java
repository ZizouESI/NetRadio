import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class DiffReceptTCP implements Runnable{
    public Socket socket;
    public Diffuseur diffuseur;
    public DiffReceptTCP(Socket socket, Diffuseur diffuseur) {
        this.socket =socket;
        this.diffuseur=diffuseur;
    }
    public static boolean checkMsg(String msg){
        if (msg.length() != 156 && msg.length() != 10){
            System.out.println("Erreur : la taille du message n'est pas respectée");
            return false;
        }
        if (msg.length() == 156 ){
            if(!msg.substring(0, 4).equals("MESS")){
                System.out.println("Erreur : le message ne commence pas par MESS !");
                return false;
            }
            if(msg.charAt(4) != ' ' || msg.charAt(13) != ' '){
                System.out.println("Erreur : les espaces!");
                return false;
            }
        }else if (msg.length() == 10){
            if(!msg.substring(0, 4).equals("LAST")){
                System.out.println("Erreur : le message ne commence pas par LAST !");
                return false;
            }
            String nb_str = msg.substring(5, 8);
            try{
                Integer.parseInt(nb_str);
            }catch(NumberFormatException e){
                System.out.println("Erreur : nb n'est pas un entier!");
                return false;
            }
            if(msg.charAt(4) != ' '){
                System.out.println("Erreur : les espaces!");
                return false;
            }
        }
        return true;
    }
    
    public void run() {
        try{
            
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            String msg="";
            
            while(msg.length() < 2 || !msg.substring(msg.length()-2, msg.length()).equals("\r\n")){
                
                char c= (char) bufferedReader.read();
                msg = msg + c;
                
            }
            //msg=bufferedReader.readLine();
            //System.out.println(msg);
            if(! checkMsg(msg)){
                bufferedReader.close();
                printWriter.close();
                this.socket.close();
            }else if(msg.substring(0, 4).equals("MESS")){
                this.diffuseur.msgs.ajouterMsg(msg.substring(5,154));
                printWriter.print("ACKM\r\n");
                printWriter.flush();
            }else if(msg.substring(0, 4).equals("LAST")){
                int nb=Integer.parseInt(msg.substring(5, 8));
                synchronized(this.diffuseur.dernier){
                    
                    for(int i=0;i<nb && i< this.diffuseur.dernier.size();i++){
                        String lmsg= this.diffuseur.dernier.get(this.diffuseur.dernier.size()-1-i); 
                        //System.out.println(lmsg);
                        printWriter.print(lmsg);
                        printWriter.flush();
                    }
                }
                
                printWriter.print("ENDM\r\n");
                printWriter.flush();
            
            }else{
                System.out.println("Message recu :"+msg);
            }
            bufferedReader.close();
            printWriter.close();
            this.socket.close();
        }catch(Exception e){
            e.printStackTrace();
            return;
        }  
        
        
    }

}
