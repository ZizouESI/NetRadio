import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class TestRedirect {
    public static void main(String[] args) {
        FileOutputStream f;
        try {
            f = new FileOutputStream("/dev/pts/0");
            System.setOut(new PrintStream(f));
            System.out.println("\naaaaaa");
            System.out.println("\naaaaaa");
            System.out.println("\naaaaaa");
            Thread.sleep(1000L);
            System.out.println("\naaaaaa");
            Thread.sleep(1000L);
            System.out.println("\naaaaaa");
            Thread.sleep(1000L);
            System.out.println("\naaaaaa");
        } catch (FileNotFoundException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
}
