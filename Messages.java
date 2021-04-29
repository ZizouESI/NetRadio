import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class Messages {

    ArrayList<String> listMessages = new ArrayList<String>();
    int indice = -1;
    public Messages(String nomFicMsgs, String idDiff) {
        try {
            File fic = new File(nomFicMsgs);
            BufferedReader buffReader = new BufferedReader(new FileReader(fic));
            
            String line = buffReader.readLine();
            while (line != null){
                this.listMessages.add(idDiff + " " +Standard.standardiserMsg(line));
                line = buffReader.readLine();
            }

            buffReader.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public synchronized String getNextMessage(){
        this.indice = (this.indice + 1) % this.listMessages.size();
        return this.listMessages.get(this.indice);
    }
    public synchronized void ajouterMsg(String msg){
        this.listMessages.add(this.indice +1, msg);
    }
    public static void main(String[] args) {
        Messages msgs = new Messages("msgs.txt", "idDiffZizou");
        for(int i=0;i<10; i++)
            System.out.println(msgs.getNextMessage());
    }

}
