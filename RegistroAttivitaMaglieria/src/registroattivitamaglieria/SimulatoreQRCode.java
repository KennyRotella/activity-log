
package registroattivitamaglieria;

import java.io.ObjectInputStream;
import java.net.*;
import java.time.LocalDate;
import java.util.*;
import javafx.application.Platform;

class SimulatoreQRCode implements Runnable{
    
    private Commissione inLavorazione;
    private String QRCode;
    private String tempo;
    private Timer cronometro;
    private long inizio;

    @Override
    public void run(){
        String c;
        String[] args;
        ServerSocket servs = null;
        
        try {
            servs = new ServerSocket(7070);
            servs.setSoTimeout(5000);   //01
        } catch(Exception exc){}
        
        while(!Thread.interrupted()){ //02
            try{
                Socket sd = servs.accept();
                
                ObjectInputStream din = new ObjectInputStream(sd.getInputStream());
                c = (String) din.readObject();//03

                if(inizio == 0){
                    args = c.split(":");
                    nuovaLavorazione(LocalDate.now(), args[0], args[1]);
                    cronometro = new Timer();
                    cronometro.scheduleAtFixedRate(new CronometroTask(), 0, 1000);//04
                    QRCode = c;
                    System.out.println("iniziato " + c);
                } else if(c.equals(QRCode)){ //05
                    cronometro.cancel();
                    Platform.runLater(new AggiornaCommissione());//06
                    inizio = 0;
                    System.out.println("finito " + c);
                }
                sd.close();
            } catch (Exception e){}
        }
        
        try {
            servs.close();  
        } catch(Exception exc) {}

        if(cronometro != null) //07
            cronometro.cancel();
    }

    public void nuovaLavorazione(LocalDate l, String a, String o){
        Commissione com = new Commissione(l,a,o,0,0,"00:00:00",LocalDate.now(),0);
        inLavorazione = com;
        tempo = "00:00:00";
        inizio = new Date().getTime();
        
        inLavorazione.tempoProperty().removeListener(inLavorazione.tempoListener); //08
        inLavorazione.tempoProperty().addListener((observable, oldValue, newValue) ->{
            GUI.stats.calcolaTempoMedio();
            GUI.stats.calcolaTempoTotale();
        });
        
        Platform.runLater(new AggiornaCommissione());

        LogNavigazioneClient.inviaEventoXML(new EventoNavigazione("QRCODE"));
    }

    public class CronometroTask extends TimerTask { //09

        @Override
        public void run(){
            long ore, minuti, secondi;
            secondi = (new Date().getTime() - inizio)/1000;

            ore = secondi/3600;
            secondi = secondi%3600;

            minuti = secondi/60;
            secondi = secondi%60;

            tempo = String.format("%02d:%02d:%02d",ore,minuti,secondi);
            Platform.runLater(new AggiornaCommissione());
        }
    }
    
    public class AggiornaCommissione implements Runnable {
        public void run(){
            inLavorazione.tempoProperty().setValue(tempo);
            GUI.tab.aggiungiAttivita(inLavorazione);
        }
    }
}

/*
Note:
01) Ogni 5 secondi che non riceve richieste il ServerSocket non rimane bloccato in
    ascolto e verifica la condizione del while per terminare quando l'applicazione
    viene chiusa.

02) Quando il programma viene chiuso, il main della classe GUI manda un interruzione.

03) Riceve una stringa nel formato "Articolo:Operazione" per simulare un QRCode. In 
    realtà potrebbe essere qualsiasi codice a scansione.

04) file:///C:/prg/java8docs/api/java/util/Timer.html#scheduleAtFixedRate-java.util.TimerTask-java.util.Date-long-
    schedula un TimerTask ogni secondo.

05) Alla fine di una lavorazione l'operatore scansiona lo stesso codice e il Sistema salva 
    il tempo cronometrato.

06) Solo threads di JavaFX possono modificare elementi grafici, questa funzione permette
    di mettere in esecuzione Thread per conto di JavaFX.

07) In caso di chiusura accidentale del programma termina il thread del cronometro se 
    era ancora in esecuzione.

08) Il listener originale invia eventi XML quando cambia il tempo, intaserebbe il Log
    con eventi non necessari.

09) Usando le date in millisecondi a partire dal 1970 calcola il tempo passato dall'inizio
    e lo converte in stringa.
*/

