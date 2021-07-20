
import java.io.ObjectOutputStream;
import java.net.Socket;

public class LettoreQRcode{
  
    public static void main(String[] args) {
        String c = new String(args[0]+":"+args[1]);
        try ( ObjectOutputStream dout =
              new ObjectOutputStream( (new Socket("localhost",7070) ).getOutputStream())
        ) { 
            dout.writeObject(c);
        } catch (Exception exc) {}
    }
}