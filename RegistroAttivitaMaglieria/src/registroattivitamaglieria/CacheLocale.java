
package registroattivitamaglieria;

import javax.xml.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.*;
import org.xml.sax.*;
import javax.xml.validation.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import com.thoughtworks.xstream.XStream;
import java.nio.file.*;
import java.time.LocalDate;
import javafx.scene.control.*;

public class CacheLocale {
    private static Configurazione conf;
    
    public static boolean valida() {
        try {  
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Document d = db.parse(new File("conf.xml"));
            Schema s = sf.newSchema(new StreamSource(new File("conf.xsd")));
            s.newValidator().validate(new DOMSource(d));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            if (e instanceof SAXException) 
                System.out.println("Errore di validazione: " + e.getMessage());
            else
                System.out.println(e.getMessage());
            return false;
        }
        return true;
    }
    
    public static void importaConf(){
        XStream xs = new XStream();
        String x;
        try {
            x = new String(Files.readAllBytes(Paths.get("conf.xml")));
            conf = (Configurazione)xs.fromXML(x);
            AttivitaMaglieriaDB.configura();
        } catch (Exception e) {}
    }
    
    public static void importaInput(DatePicker data, ComboBox art, ComboBox ope){
        LocalDate da = null;
        int ar = -1;
        int op = -1;
        try ( FileInputStream fin = new FileInputStream("data.bin");
              ObjectInputStream oin = new ObjectInputStream(fin); ){
          da = (LocalDate) oin.readObject();  
        } catch (IOException | ClassNotFoundException ex) { 
            System.out.println("errore: impossibile importare la data!");
        }
        try ( FileInputStream fin = new FileInputStream("articolo.bin");
              ObjectInputStream oin = new ObjectInputStream(fin); ){
          ar = (int) oin.readObject();  
        } catch (IOException | ClassNotFoundException ex) { 
            System.out.println("errore: impossibile importare l'articolo!");
        }
        try ( FileInputStream fin = new FileInputStream("operazione.bin");
              ObjectInputStream oin = new ObjectInputStream(fin); ){
          op = (int) oin.readObject();  
        } catch (IOException | ClassNotFoundException ex) { 
            System.out.println("errore: impossibile importare l'operazione!");
        }
        data.setValue(da);
        art.getSelectionModel().select(ar);
        ope.getSelectionModel().select(op);
    }
    
    public static void esportaInput(LocalDate d,Integer a,Integer o){ 
        try(FileOutputStream fout = new FileOutputStream("data.bin");
              ObjectOutputStream oout = new ObjectOutputStream(fout);) {
          oout.writeObject(d);               
        } catch (IOException ex) {
            System.out.println("errore: impossibile conservare l'articolo!");
        }
        
        try(FileOutputStream fout = new FileOutputStream("articolo.bin");
              ObjectOutputStream oout = new ObjectOutputStream(fout);) {
          oout.writeObject(a);               
        } catch (IOException ex) {
            System.out.println("errore: impossibile conservare l'operazione!");
        }
        
        try(FileOutputStream fout = new FileOutputStream("operazione.bin");
              ObjectOutputStream oout = new ObjectOutputStream(fout);) {
          oout.writeObject(o);               
        } catch (IOException ex) {
            System.out.println("errore: impossibile conservare la data!");
        }
    }
    
    public static String getOperatore()    {return conf.getOperatore();}
    public static String getIpServerLog()  {return conf.getIpServerLog();}
    public static String getPortServerLog(){return conf.getPortServerLog();}
    public static String getUsernameDB()   {return conf.getUsernameDB();}
    public static String getPasswordDB()   {return conf.getPasswordDB();}
    public static String getIpDB()         {return conf.getIpDB();}
    public static String getPortDB()       {return conf.getPortDB();}
}




