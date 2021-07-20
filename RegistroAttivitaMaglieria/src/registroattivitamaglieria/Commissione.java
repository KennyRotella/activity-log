package registroattivitamaglieria;

import java.time.LocalDate;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;

public class Commissione {
    private final SimpleObjectProperty<LocalDate> data;
    private final SimpleStringProperty articolo;
    private final SimpleStringProperty operazione;
    private final SimpleIntegerProperty numCapi;
    private final SimpleDoubleProperty prezzo;
    private final SimpleStringProperty tempo;
    private final SimpleObjectProperty<LocalDate> ritiro;
    private final SimpleDoubleProperty totale;
    
    public final ChangeListener tempoListener = (observable, oldValue, newValue) ->{
          LogNavigazioneClient.inviaEventoXML(new EventoNavigazione("MODIFICA_TEMPO"));
          AttivitaMaglieriaDB.caricaCommissione(this);
          GUI.stats.calcolaTempoMedio();
          GUI.stats.calcolaTempoTotale();
    };
    
    public final ChangeListener numCapiListener = (observable, oldValue, newValue) ->{
            LogNavigazioneClient.inviaEventoXML(new EventoNavigazione("MODIFICA_NUMCAPI"));
            AttivitaMaglieriaDB.caricaCommissione(this);
            GUI.istogramma.aggiorna();
            GUI.stats.calcolaTotMese();
    };
            
    public final ChangeListener prezzoListener = (observable, oldValue, newValue) ->{
            LogNavigazioneClient.inviaEventoXML(new EventoNavigazione("MODIFICA_PREZZO"));
            AttivitaMaglieriaDB.caricaCommissione(this);
            GUI.istogramma.aggiorna();
            GUI.stats.calcolaTotMese();
    };
            
    public final ChangeListener ritiroListener = (observable, oldValue, newValue) ->{
            LogNavigazioneClient.inviaEventoXML(new EventoNavigazione("MODIFICA_RITIRO"));
            AttivitaMaglieriaDB.caricaCommissione(this);
    };
              
    public Commissione(LocalDate d, String a, String o, int n, double p, String t, LocalDate r, double tot) {
        data = new SimpleObjectProperty<>(d);
        articolo = new SimpleStringProperty(a);
        operazione = new SimpleStringProperty(o);
        numCapi = new SimpleIntegerProperty(n);
        prezzo = new SimpleDoubleProperty(p);
        tempo = new SimpleStringProperty(t);
        ritiro = new SimpleObjectProperty<>(r);
        totale = new SimpleDoubleProperty(tot);

        totale.bind(prezzo.multiply(numCapi));

        numCapi.addListener(numCapiListener);

        prezzo.addListener(prezzoListener);

        tempo.addListener(tempoListener);

        ritiro.addListener(ritiroListener);
    }

        public final LocalDate getData(){return data.get();}
        public final void setData(LocalDate value){data.set(value);}
        public SimpleObjectProperty dataProperty() {return data;}

        public final String getArticolo(){return articolo.get();}
        public final void setArticolo(String value){articolo.set(value);}
        public SimpleStringProperty articoloProperty() {return articolo;}

        public final String getOperazione(){return operazione.get();}
        public final void setOperazione(String value){operazione.set(value);}
        public SimpleStringProperty operazioneProperty() {return operazione;}

        public final int getNumCapi(){return numCapi.get();}
        public final void setNumCapi(int value){numCapi.set(value);}
        public SimpleIntegerProperty numCapiProperty() {return numCapi;}

        public final double getPrezzo(){return prezzo.get();}
        public final void setPrezzo(double value){prezzo.set(value);}
        public SimpleDoubleProperty prezzoProperty() {return prezzo;}

        public final String getTempo(){return tempo.get();}
        public final void setTempo(String value){tempo.set(value);}
        public SimpleStringProperty tempoProperty() {return tempo;}

        public final LocalDate getRitiro(){return ritiro.get();}
        public final void setRitiro(LocalDate value){ritiro.set(value);}
        public SimpleObjectProperty ritiroProperty() {return ritiro;}

        public final double getTotale(){return totale.get();}
        public final void setTotale(double value){totale.set(value);}
        public SimpleDoubleProperty totaleProperty() {return totale;}
}
