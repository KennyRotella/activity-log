
package registroattivitamaglieria;

import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.text.*;
import java.time.LocalDate;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class GUI extends Application {
    static String[] mesi = new String[]{
        "Gennaio",
        "Febbraio",
        "Marzo",
        "Aprile",
        "Maggio",
        "Giugno",
        "Luglio",
        "Agosto",
        "Settembre",
        "Ottobre",
        "Novembre",
        "Dicembre"
    };
    
    //Elementi Grafici
    private HBox header;
    private HBox subHeader;
    public static Tabella tab;
    private HBox barraInserimento;
    private HBox barraStatistiche;
    static Istogramma istogramma;
    private static DatePicker addData;   
    private static ComboBox<String> addArticolo;
    private static ComboBox<String> addOperazione;
    
    //Strutture Dati
    public static Statistiche stats;
    private ObservableList<String> operazioni;
    private ObservableList<String> articoli;
    
    public static void main(String[] args) {
        if(CacheLocale.valida())
            CacheLocale.importaConf();
        else return;
        
        Thread QRCode = new Thread(new SimulatoreQRCode());
        QRCode.start();
        
        LogNavigazioneClient.inviaEventoXML(new EventoNavigazione("AVVIO"));
        launch(args);
        LogNavigazioneClient.inviaEventoXML(new EventoNavigazione("TERMINE"));
        
        LocalDate da = addData.getValue();
        Integer ar = addArticolo.getSelectionModel().getSelectedIndex();
        Integer op = addOperazione.getSelectionModel().getSelectedIndex();
        CacheLocale.esportaInput(da,ar,op);
        QRCode.interrupt();
    }
    
    @Override
    public void start(Stage primaryStage) {
        
        primaryStage.setTitle("Registro Maglieria");
        
        operazioni = AttivitaMaglieriaDB.estraiOperazioni();
        articoli = AttivitaMaglieriaDB.estraiArticoli();
        stats = new Statistiche();
        
        creaHeader();
        
        tab = new Tabella();
        
        creaBarraInserimento();
        
        creaBarraStatistiche();
        
        istogramma = new Istogramma(new CategoryAxis(),new NumberAxis());
        
        stats.calcolaTotMese();
        stats.calcolaTempoMedio();
        stats.calcolaTempoTotale();
        
        VBox layoutInterfaccia = new VBox();
        layoutInterfaccia.setAlignment(Pos.TOP_LEFT);
        layoutInterfaccia.setPadding(new Insets(0, 25, 25, 25));
        layoutInterfaccia.
                getChildren().
                addAll(header, subHeader, tab, barraInserimento, barraStatistiche, istogramma);
        
        Scene scene = new Scene(layoutInterfaccia);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public void creaBarraStatistiche(){
        Label stat1 = new Label("Totale Mese: ");
        stat1.setFont(Font.font("Tahoma", FontWeight.BOLD, 15));
        
        TextField stat1Bind = new TextField();
        stat1Bind.setFont(Font.font("Tahoma", FontWeight.NORMAL, 15));
        stat1Bind.setEditable(false);
        stat1Bind.setMaxWidth(100);
        stat1Bind.textProperty().bind(stats.totMeseProperty());
        
        Label stat2 = new Label("Tempo Medio: ");
        stat2.setFont(Font.font("Tahoma", FontWeight.BOLD, 15));
        stat2.setPadding(new Insets(0, 0, 0, 20));
        
        TextField stat2Bind = new TextField();
        stat2Bind.setFont(Font.font("Tahoma", FontWeight.NORMAL, 15));
        stat2Bind.setEditable(false);
        stat2Bind.setMaxWidth(100);
        stat2Bind.textProperty().bind(stats.tempoMedProperty());
        
        Label stat3 = new Label("Tempo Totale: ");
        stat3.setFont(Font.font("Tahoma", FontWeight.BOLD, 15));
        stat3.setPadding(new Insets(0, 0, 0, 20));
        
        TextField stat3Bind = new TextField();
        stat3Bind.setFont(Font.font("Tahoma", FontWeight.NORMAL, 15));
        stat3Bind.setEditable(false);
        stat3Bind.setMaxWidth(100);
        stat3Bind.textProperty().bind(stats.tempoTotProperty());
        
        barraStatistiche = new HBox();
        barraStatistiche.getChildren().addAll(stat1, stat1Bind, stat2, stat2Bind, stat3, stat3Bind);
        barraStatistiche.setAlignment(Pos.CENTER);
        barraStatistiche.setPadding(new Insets(10, 0, 10, 0));
    }
    
    public void creaBarraInserimento(){
        barraInserimento = new HBox();
        barraInserimento.setAlignment(Pos.CENTER_RIGHT);
        
        Region space2 = new Region();
        space2.setMinWidth(30);
        
        addData = new DatePicker();
        addData.setPromptText("Data");
        addData.setPrefWidth(120);
        
        addArticolo = new ComboBox<>(articoli);
        addArticolo.setPromptText("Articolo");
        addArticolo.setPrefWidth(150);
        
        addOperazione = new ComboBox<>(operazioni);
        addOperazione.setPromptText("Operazione");
        addOperazione.setPrefWidth(150);
        
        Button addButton = new Button("Aggiungi");
        addButton.setOnAction(e -> {
            LocalDate data = addData.getValue();
            String articolo = addArticolo.getValue();
            String operazione = addOperazione.getValue();
            
            addData.setValue(null);
            addArticolo.setValue(null);
            addOperazione.setValue(null);
            
            if(data != null && articolo != null && operazione != null){
                tab.aggiungiAttivita(data,articolo,operazione);
                stats.calcolaTempoMedio();
                LogNavigazioneClient.inviaEventoXML(new EventoNavigazione("AGGIUNTO"));
            }
        });

        Button removeButton = new Button("Rimuovi");
        removeButton.setOnAction(e -> {
            tab.rimuoviAttivitaSelezionate();
            stats.calcolaTotMese();
            stats.calcolaTempoMedio();
            stats.calcolaTempoTotale();
            istogramma.aggiorna();
            LogNavigazioneClient.inviaEventoXML(new EventoNavigazione("RIMOSSO"));
        });
        
        barraInserimento.getChildren().addAll(
                addData, addArticolo, addOperazione, space2, addButton, removeButton);
        
        CacheLocale.importaInput(addData,addArticolo,addOperazione);
    }
    
    public void creaHeader(){
        Text titoloScena = new Text("Registro Attività Maglieria");
        titoloScena.setFont(Font.font("Arial Black", FontWeight.NORMAL, 50));
        
        header = new HBox();
        header.getChildren().add(titoloScena);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 15, 0));
        
        Label userName = new Label("Operatore: " + CacheLocale.getOperatore());
        userName.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        
        Region space1 = new Region();
        space1.setMinWidth(300);
        
        Label dataCorr = new Label("Data Corrente: ");
        dataCorr.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        
        ChoiceBox<String> mese = new ChoiceBox<>();
        for(String i: mesi){
            mese.getItems().add(i);
        }
        
        int annoCorrente = LocalDate.now().getYear();
        ChoiceBox<Integer> anno = new ChoiceBox<>();
        for(Integer i=annoCorrente; i>2000; i--){
            anno.getItems().add(i);
        }
        
        mese.getSelectionModel().select(LocalDate.now().getMonthValue()-1);
        anno.getSelectionModel().select(0);
        
        mese.getSelectionModel().selectedIndexProperty().addListener(
                (v, oldValue, newValue) -> aggiornaMese(newValue));
        anno.getSelectionModel().selectedItemProperty().addListener(
                (v, oldValue, newValue) -> aggiornaAnno(newValue));
        
        subHeader = new HBox();
        subHeader.getChildren().addAll(userName, space1, dataCorr, mese, anno);
        subHeader.setAlignment(Pos.CENTER);
        subHeader.setPadding(new Insets(0, 0, 5, 0));
    }
        
    public void aggiornaMese(Number nuovoValore){
        LogNavigazioneClient.inviaEventoXML(new EventoNavigazione("MESE"));
        
        tab.selezionaMese(nuovoValore.intValue()+1);
        
        stats.calcolaTotMese();
        stats.calcolaTempoMedio();
        stats.calcolaTempoTotale();
    }
    
    public void aggiornaAnno(Integer nuovoValore){
        LogNavigazioneClient.inviaEventoXML(new EventoNavigazione("ANNO"));

        tab.selezionaAnno(nuovoValore);

        stats.calcolaTotMese();
        stats.calcolaTempoMedio();
        stats.calcolaTempoTotale();
    }
    
    public class Statistiche {
    
        private final SimpleStringProperty totMese;
        private final SimpleStringProperty tempoMed;
        private final SimpleStringProperty tempoTot;

        Statistiche(){
            totMese = new SimpleStringProperty();
            tempoMed = new SimpleStringProperty();
            tempoTot = new SimpleStringProperty();
        }

        public SimpleStringProperty totMeseProperty() {
            return totMese;
        }
        public void setTotMese(String value){
            totMese.set(value);
        }

        public SimpleStringProperty tempoMedProperty() {
            return tempoMed;
        }
        public void setTempoMed(String value){
            tempoMed.set(value);
        }

        public SimpleStringProperty tempoTotProperty() {
            return tempoTot;
        }
        public void setTempoTot(String value){
            tempoTot.set(value);
        }

        public void calcolaTotMese(){
            if(tab.getItems().isEmpty()) return;

            double sum = 0;
            for(Commissione c : tab.getItems()){
                sum += c.getTotale();
            }
            totMese.set(String.format("%.2f €",sum));
        }

        public void calcolaTempoMedio(){
            if(tab.getItems().isEmpty()) return;

            int sum = 0;
            int ore, minuti, secondi;
            String[] tempo;
            for(Commissione c : tab.getItems()){
                tempo = c.getTempo().split(":");

                ore = Integer.parseInt(tempo[0]);
                minuti = Integer.parseInt(tempo[1]);
                secondi = Integer.parseInt(tempo[2]);

                sum += ore*3600 + minuti*60 + secondi;
            }
            sum /= tab.getItems().size();

            ore = sum/3600;
            sum = sum%3600;

            minuti = sum/60;
            secondi = sum%60;
            tempoMed.set(String.format("%02d:%02d:%02d",ore,minuti,secondi));
        }

        public void calcolaTempoTotale(){
            if(tab.getItems().isEmpty()) return;

            int sum = 0;
            int ore, minuti, secondi;
            String[] tempo;

            for(Commissione c : tab.getItems()){
                tempo = c.getTempo().split(":");

                ore = Integer.parseInt(tempo[0]);
                minuti = Integer.parseInt(tempo[1]);
                secondi = Integer.parseInt(tempo[2]);

                sum += ore*3600 + minuti*60 + secondi;
            }

            ore = sum/3600;
            sum = sum%3600;

            minuti = sum/60;
            secondi = sum%60;
            tempoTot.set(String.format("%02d:%02d:%02d",ore,minuti,secondi));
        }
    }
    
//    public void riempi(){ utilizzato per popolare il database
//        for(int i=2017;i<2021;i++){
//            for(int j=1;j<=12;j++){
//                double budget = Math.random()*900+800;
//                double somma = 0;
//                while(somma < budget){
//                    int giorno = (int)(Math.floor(Math.random()*28+1));
//                    LocalDate d = LocalDate.of(i, j, giorno);
//                    int op = (int)(Math.floor(Math.random()*12));
//                    int art = (int)(Math.floor(Math.random()*10));
//                    double prezzo = Math.round((Math.random()*4+2)*10)/10.0;
//                    int numCapi = (int)Math.floor(Math.random()*20)+10;
//                    
//                    int hh = (int)(Math.random()*2+numCapi/10);
//                    int mm = (int)(Math.random()*60);
//                    int ss = (int)(Math.random()*60);
//                    String tempo = String.format("%02d:%02d:%02d",hh,mm,ss);
//                    LocalDate r = d.plusDays((long)(Math.random()*7));
//                    somma += numCapi*prezzo;
//                    Commissione com = new Commissione(d,operazioni.get(op),articoli.get(art),numCapi,prezzo,tempo,r,0);
//                    AttivitaMaglieriaDB.caricaCommissione(com);
//                    System.out.println(d + "*" + operazioni.get(op) + "*"+ articoli.get(art) + "*"+ prezzo + "*"+ numCapi + "*"+ tempo + "*"+ r);
//                }
//            }
//        }
//    }
}


