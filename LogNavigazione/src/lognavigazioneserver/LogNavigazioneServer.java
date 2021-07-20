package lognavigazioneserver;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class LogNavigazioneServer{
  
    public static void main(String[] args) {
        while(true){
            try ( ServerSocket servs = new ServerSocket(8080);
                Socket sd = servs.accept(); 
                DataInputStream din = new DataInputStream(sd.getInputStream());
                OutputStream os = new FileOutputStream(new File("log.xml"), true);
            ) {
                String x = din.readUTF();
                if(valida(x)){
                    x += "\n\n";
                    os.write(x.getBytes());
                }
                System.out.println(x);
            } catch (Exception e){}
        }
    }
    
    public static boolean valida(String log) {
        try {  
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Document d = db.parse(new ByteArrayInputStream(log.getBytes(StandardCharsets.UTF_8)));
            Schema s = sf.newSchema(new StreamSource(new File("log.xsd")));
            s.newValidator().validate(new DOMSource(d));
            return true;
        } catch (Exception e) {
            if (e instanceof SAXException) 
                System.out.println("Errore di validazione: " + e.getMessage());
            else
                System.out.println(e.getMessage());
            return false;
        }
    }
}
