
package registroattivitamaglieria;

import com.thoughtworks.xstream.XStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class LogNavigazioneClient {
    public static void inviaEventoXML(EventoNavigazione e) {
        XStream xs = new XStream(); 
        xs.aliasPackage("", "registroattivitamaglieria"); //01
        xs.useAttributeFor(EventoNavigazione.class, "evento");
        String x = xs.toXML(e);
        String portServerLog = CacheLocale.getPortServerLog();
        String ipServerLog = CacheLocale.getIpServerLog();
        try ( DataOutputStream dout =
              new DataOutputStream( (new Socket(ipServerLog,Integer.parseInt(portServerLog)) ).getOutputStream())
        ){ 
            dout.writeUTF(x);
        } catch (IOException exc) {System.out.println("Server Log non raggiungibile");}
    }
}
/*
Note:
01) Rimuove dal tag iniziale la specifica del package a cui appartiene la classe.
*/
