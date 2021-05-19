import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;



/**
 * Diffuseur
 */
public class Diffuseur {
    /*Attributs de la classe Diffuseur*/

    
    public String idDiff; //identifiant du diffuseur
    public int port; // port de reception
    public String addressDiff; // adresse de diffusion
    public int portDiff; // port de diffusion
    public Messages msgs; // messages à diffuser
    public ArrayList<String> dernier = new ArrayList<String>();

    /* Constructeurs de la classe Diffuseur */
    public Diffuseur(String nomFicMsgs , String nomFicConfigDiff){
        try{
            //initialisation des attributs
            this.initAtt(nomFicConfigDiff);
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("Mauvaise configuration");
            System.exit(0);
        }
        this.msgs = new Messages(nomFicMsgs,this.idDiff);
    }

    //initialisation des attributs 
    public void initAtt(String nomFicConfigDiff) throws Exception{
        //ouverture du fichier de configuration
        File fic = new File(nomFicConfigDiff);
        BufferedReader buffReader = new BufferedReader(new FileReader(fic));
        
        //récupération des 4 paramètres du diffuseur
        //id
        String line = buffReader.readLine();
        if( line == null || line.length() != 8){
            System.out.println("Mauvais fichier de configuration diffuseur  !");
            System.exit(0);
        }
        this.idDiff = line ;

        //addressDiff 
        line = buffReader.readLine();
        if( line == null){
            System.out.println("Mauvais fichier de configuration diffuseur  !");
            System.exit(0);
        }
        this.addressDiff = Standard.standardiserIp(line);
        
        //portDiff
        line = buffReader.readLine();
        if( line == null){
            System.out.println("Mauvais fichier de configuration diffuseur  !");
            System.exit(0);
        }
        this.portDiff = Integer.parseInt(line);
        if(this.portDiff < 1024 || this.portDiff > 9999){
            System.out.println("Numéro de port non disponible  !");
            System.exit(0);
        }

        //port
        line = buffReader.readLine();
        if( line == null){
            System.out.println("Mauvais fichier de configuration diffuseur  !");
            System.exit(0);
        }
        this.port  = Integer.parseInt(line);
        if(this.port < 1024 || this.port > 9999){
            System.out.println("Numéro de port non disponible  !");
            System.exit(0);
        }

        buffReader.close();
    }

    public void affichage(){
        System.out.println("----------Diffuseur------------");
        System.out.println("Identifiant : "+ this.idDiff);
        System.out.println("Addresse de diffusion : "+ this.addressDiff);
        System.out.println("Port de diffusion : " + this.portDiff);
        System.out.println("Port d'écoute : " + this.port);
        System.out.println("-------------------------------");
    }
    public void envoie(){
        Envoi send = new Envoi(this);
        Thread thsend = new Thread(send);
        thsend.start();
    }
    public void reception(){
        Reception recv= new Reception(this);
        Thread threcv = new Thread(recv);
        threcv.start();
    }
    public void enregest() {
        //lancement d'un thread (anonymous class)
        Thread enr = new Thread(){
            public void run(){
               
                try {
                    Socket sc= new Socket("127.0.0.1",5858);
                    BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(sc.getInputStream()));
                    PrintWriter printWriter=new PrintWriter(new OutputStreamWriter(sc.getOutputStream()));
                    String msg="REGI "+ idDiff + " " + addressDiff + " " + portDiff + " 127.000.000.001 "+ port +"\r\n";
                    printWriter.print(msg);
                    printWriter.flush();
                    String res=bufferedReader.readLine();
                    System.out.println(res);
                    sc.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        enr.start();
    }
    public static void main(String[] args) {
        if(args.length != 1){
            System.out.println("Vous devez fournir un nom du fichier de configuration pour le diffuseur.");
            System.exit(0);
        }
        //Début de diffusion
        Diffuseur diffuseur = new Diffuseur("msgs.txt", args[0]);
        //petit affichage
        diffuseur.affichage();

        //enregistrement auprès du gestionnaire
        diffuseur.enregest();

        //envoi de messages
        diffuseur.envoie();
        
        //reception de messages
        diffuseur.reception();
    }

    
}