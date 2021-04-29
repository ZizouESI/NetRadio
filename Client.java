import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


public class Client {

    public String id;
    public String addresseDiff;
    public int portDiff;
    public String addresseDiffTCP;
    public int port;
    public ArrayList<String> listMessages;


    public Client(String nomFicMsgs , String nomFicConfigCl){
        try {
            File fic = new File(nomFicConfigCl);
            BufferedReader bufferedReader=new BufferedReader(new FileReader(fic));
            String line=bufferedReader.readLine();
            if(line == null || line.length() != 8){
                System.out.println("Mauvais fichier de configuration pour le client!");
                System.exit(0);
            }
            this.id=line;
            line=bufferedReader.readLine();
            if(line == null){
                System.out.println("Mauvais fichier de configuration pour le client!");
                System.exit(0);
            }
            this.addresseDiff = Standard.standardiserIp(line);
            line=bufferedReader.readLine();
            if(line == null){
                System.out.println("Mauvais fichier de configuration pour le client!");
                System.exit(0);
            }
            this.portDiff = Integer.parseInt(line);
            if(this.portDiff<1024 || this.portDiff > 9999){
                System.out.println("Mauvais fichier de configuration pour le client!");
                System.exit(0);
            }
            line=bufferedReader.readLine();
            if(line == null){
                System.out.println("Mauvais fichier de configuration pour le client!");
                System.exit(0);
            }
            this.addresseDiffTCP=Standard.standardiserIp(line);
            line=bufferedReader.readLine();
            if(line == null){
                System.out.println("Mauvais fichier de configuration pour le client!");
                System.exit(0);
            }
            this.port = Integer.parseInt(line);
            if(this.port<1024 || this.port > 9999){
                System.out.println("Mauvais fichier de configuration pour le client!");
                System.exit(0);
            }
            
            this.listMessages = new ArrayList<>();
            File ficMsgCl = new File(nomFicMsgs);
            bufferedReader = new BufferedReader(new FileReader(ficMsgCl));
            line = bufferedReader.readLine();
            while(line != null){
                this.listMessages.add(Standard.standardiserMsg(line));
                line = bufferedReader.readLine();
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Mauvais fichier de configuration pour le client!");
            System.exit(0);
        }
        
    }

    public void connexHandler() {
        BufferedReader br= new BufferedReader(new InputStreamReader(System.in));
        Random r = new Random();
        try {
            
            while(true){
                String line = br.readLine();
                
                if(line.equals("M")){
                    Socket socket = new Socket(this.addresseDiffTCP, this.port);
                    System.out.println("Adresse diff TCP : " + this.addresseDiffTCP);
                    System.out.println("port :" + this.port);
                    PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String msg = this.listMessages.get(r.nextInt(this.listMessages.size()));
                    String fullmsg = "MESS " + this.id + " " + Standard.standardiserMsg(msg) + "\r\n";
                    System.out.println("message depuis client : "+fullmsg);
                    printWriter.print(fullmsg);
                    printWriter.flush();

                    String res = "";
                    
                    while(res.length()<2 || !res.substring(res.length() -2, res.length()).equals("\r\n")){
                        
                        char c=(char) bufferedReader.read();
                        
                        res = res + c;
                        //System.out.println(res);
                    }
                   
                    if(! res.equals("ACKM\r\n")){
                        System.out.println("Erreur : Pas d'ACKM");
                    }else{
                        System.out.println("ACKM recu .");
                    }
                    
                    printWriter.close();
                    bufferedReader.close();
                    socket.close();
                    
                }else if(line.equals("L")){
                    boolean stop = false;
                    int nb=0;
                    while(!stop){
                        System.out.println("Veuillez introduire un nombre entre 0 et 999");
                        line = br.readLine();
                        stop  = true;
                        try {
                            nb=Integer.parseInt(line);
                        } catch (NumberFormatException e) {
                            System.out.println("Format incorrect!");
                            stop = false;
                        }
                        if(nb < 0 || nb > 999){
                            System.out.println("Numéro n'appartient pas à [0,999]");
                            stop = false;
                        }
                    }
                    
                    System.out.println(nb);
                    Socket socket1=new Socket(this.addresseDiffTCP,this.port);
                    PrintWriter printWriter=new PrintWriter(new OutputStreamWriter(socket1.getOutputStream()));
                    BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(socket1.getInputStream()));
                    
                    String msg = "LAST " + Standard.standardiserNumMsg3(nb)+"\r\n";
                    
                    System.out.println(msg);
                    printWriter.print(msg);
                    printWriter.flush();
                    stop = false;
                    String res="";
                    while(!stop){
                        res="";
                        while(res.length()<2 || !res.substring(res.length()-2,res.length()).equals("\r\n")){
                            char c =(char) bufferedReader.read();
                            res = res + c;
                        } 
                        //System.out.println(res);
                        if(res.equals("ENDM\r\n")){
                            System.out.println("ENDM recu");
                            stop = true;
                        }else{
                            checkOLDMess(res);
                            System.out.println("Recu : "+ res.substring(0, 4)+" "+res.substring(5,9)+" "+res.substring(10, 18)+" " + Standard.restituerMsg(res.substring(19, 159)));
                        }
                    }
                    socket1.close();
                
                }else if(line.equals("LIST")){
                    Socket sc= new Socket("127.0.0.1",5858);
                    BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(sc.getInputStream()));
                    PrintWriter printWriter=new PrintWriter(new OutputStreamWriter(sc.getOutputStream()));
                    String msg="LIST\r\n";
                    printWriter.print(msg);
                    printWriter.flush();
                    String res="";
                    while((res=bufferedReader.readLine()) != null){
                        System.out.println(res);
                    }
                    sc.close();
                }
            }
        } catch (Exception e) {
        
        }
    }
    
    public void checkOLDMess(String res) {
        if(res.length() != 161){
            System.out.println("Erreur : la taille du message est non respectée");
        }else if(!res.subSequence(0, 4).equals("OLDM")){
            System.out.println("Erreur : le message ne commence pas par OLDM");
        }else if(res.charAt(4) != ' '||res.charAt(9) != ' '||res.charAt(18) != ' '){
            System.out.println("Erreur : les espaces sont mal positionés");
        }else if(!res.substring(159,161).equals("\r\n")){
            System.out.println("Erreur : les messages ne se termine correctement");
        }
    }

    public static void main(String[] args) {
        
        if(args.length != 1){
            System.out.println("Vous devez fournir un fichier de configuration pour le client");
            System.exit(0);
        }
        Client client = new Client("msgscl.txt", args[0]);
        //Lancement du thread responsable des requetes 
        try{
            ClientRequests clReq  = new ClientRequests(client);
            Thread th= new Thread(clReq);
            th.start();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(0);
        }

        //handler des communications
        client.connexHandler();
    }

    
}
