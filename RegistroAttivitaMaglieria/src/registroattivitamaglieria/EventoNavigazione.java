package registroattivitamaglieria;

import java.io.Serializable;
import java.net.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class EventoNavigazione implements Serializable{
    private String nomeApplicazione;
    private String ipClient;
    private String timestamp;
    private String evento;
    
    EventoNavigazione(String e){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        nomeApplicazione = "Registro Attività Maglieria";
        try{
            InetAddress inetAddress = InetAddress.getLocalHost(); //01
            ipClient = inetAddress.getHostAddress();
        } catch(Exception exc){}
        timestamp = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(formatter);
        evento = e;
    }
}

/*
Note:
01) file:///C:/prg/java8docs/api/index.html
*/