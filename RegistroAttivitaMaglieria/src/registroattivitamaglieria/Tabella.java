
package registroattivitamaglieria;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

final class Tabella extends TableView<Commissione>{
    private LocalDate dataSelezionata;
    private ObservableList<Commissione> commissioni;
    
    private Callback<TableColumn<Commissione,LocalDate>,TableCell<Commissione,LocalDate>> giorno = 
            (TableColumn<Commissione, LocalDate> param) -> new DateEditingCell("dd"); //01
    
    private Callback<TableColumn<Commissione,String>,TableCell<Commissione,String>> stringa = 
            (TableColumn<Commissione, String> param) -> new EditingCellStr("\\w*","\\w");
    
    private Callback<TableColumn<Commissione,Integer>,TableCell<Commissione,Integer>> numCapi = 
            (TableColumn<Commissione, Integer> param) -> new EditingCellInt();
    
    private Callback<TableColumn<Commissione,Double>,TableCell<Commissione,Double>> valutaEuro = 
            (TableColumn<Commissione, Double> param) -> new EditingCellDbl();
    
    private Callback<TableColumn<Commissione,String>,TableCell<Commissione,String>> tempo = 
            (TableColumn<Commissione, String> param) -> new EditingCellStr("\\d\\d:[0-5][0-9]:[0-5][0-9]","\\d|:");
    
    private Callback<TableColumn<Commissione,LocalDate>,TableCell<Commissione,LocalDate>> ritiro = 
            (TableColumn<Commissione, LocalDate> param) -> new DateEditingCell("dd/MM/yyyy");
    
    Tabella(){
        setEditable(true);
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); //02
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        TableColumn<Commissione, Date> dataColonna = new TableColumn<>("Data");
        aggiungiColonna(dataColonna, "data", giorno, false);
        
        TableColumn<Commissione, String> articoloColonna = new TableColumn<>("Articolo");
        aggiungiColonna(articoloColonna, "articolo", stringa, false);
        
        TableColumn<Commissione, String> operazioneColonna = new TableColumn<>("Operazione");
        aggiungiColonna(operazioneColonna, "operazione", stringa, false);
        
        TableColumn<Commissione, Integer> numCapiColonna = new TableColumn<>("N°Capi");
        aggiungiColonna(numCapiColonna, "numCapi", numCapi, true);
        
        TableColumn<Commissione, Double> prezzoColonna = new TableColumn<>("Prezzo");
        aggiungiColonna(prezzoColonna, "prezzo", valutaEuro, true);
        
        TableColumn<Commissione, Double> tempoColonna = new TableColumn<>("Tempo");
        aggiungiColonna(tempoColonna, "tempo", tempo, true);
        
        TableColumn<Commissione, Date> ritiroColonna = new TableColumn<>("Ritiro");
        aggiungiColonna(ritiroColonna, "ritiro", ritiro, true);
        
        TableColumn<Commissione, Date> totaleColonna = new TableColumn<>("Totale");
        aggiungiColonna(totaleColonna, "totale", valutaEuro, false);
        
        dataSelezionata = LocalDate.now();
        aggiorna();
    }
    
    private <T> void aggiungiColonna(TableColumn<Commissione,T> colonna, String proprCol, Callback casella, boolean editabile){
        colonna.setCellValueFactory(new PropertyValueFactory<>(proprCol));
        colonna.setStyle("-fx-alignment: CENTER");
        colonna.setCellFactory(casella);     
        colonna.setEditable(editabile);
        getColumns().add(colonna);
    }
    
    public void selezionaAnno(int anno){
        dataSelezionata = dataSelezionata.withYear(anno);
        aggiorna();
    }
    
    public void selezionaMese(int mese){
        dataSelezionata = dataSelezionata.withMonth(mese);
        aggiorna();
    }
    
    public void aggiorna(){
        commissioni = AttivitaMaglieriaDB.estraiCommissioni(dataSelezionata.getYear(),dataSelezionata.getMonthValue());
        setItems(commissioni);
        getSortOrder().add(getColumns().get(0)); //03
        getSelectionModel().clearSelection(); //04
    }
    
    public void aggiungiAttivita(LocalDate dt, String art, String opr){
        getSelectionModel().clearSelection();

        Commissione com;
        com = new Commissione(dt,art,opr,0,0.0,"00:00:00",LocalDate.now(),0.0);
        
        AttivitaMaglieriaDB.rimuoviCommissione(com);
        aggiorna();
        AttivitaMaglieriaDB.caricaCommissione(com);
        
        boolean ins = (com.getData().getMonthValue() == dataSelezionata.getMonthValue()) &&
                (com.getData().getYear() == dataSelezionata.getYear());
        if(ins){
            commissioni.add(com);
            scrollTo(com);
            getSelectionModel().select(com);
            sort();
        }
    }
    
    public synchronized void aggiungiAttivita(Commissione com){ //05
        rimuoviAttivita(com);
        aggiorna();
        AttivitaMaglieriaDB.caricaCommissione(com);

        boolean ins = (com.getData().getMonthValue() == dataSelezionata.getMonthValue()) &&
                (com.getData().getYear() == dataSelezionata.getYear());
        if(ins){
            commissioni.add(com);
            scrollTo(com);
            getSelectionModel().select(com);
            sort();
        }
    }
        
    public void rimuoviAttivitaSelezionate(){
        ObservableList<Commissione> items = getSelectionModel().getSelectedItems();
        if(items.isEmpty()) return;
        
        for(Commissione c : items){
            AttivitaMaglieriaDB.rimuoviCommissione(c);
        }
        commissioni.removeAll(items);
        getSelectionModel().clearSelection();
    }
    
    public void rimuoviAttivita(Commissione com){
        AttivitaMaglieriaDB.rimuoviCommissione(com);
        commissioni.remove(com);
        getSelectionModel().clearSelection();
    }
    
    class EditingCellStr extends TableCell<Commissione, String> { //06

        private TextField textField;
        private String oldVal;
        private String pattern;
        private String charAccepted;
        
        
        private EditingCellStr(String p, String c) {
            pattern = p;
            charAccepted = c;
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null); //07
                setGraphic(textField); //08
                textField.requestFocus();
                textField.selectAll();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText(getString());
            setGraphic(null);
        }
        
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(item);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }

        private void createTextField() {
            oldVal = getString();
            textField = new TextField(oldVal);
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.setOnAction((e) -> commitEdit(textField.getText()));
            textField.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                if (!newValue) {
                    if (!textField.getText().matches(pattern)){
                        textField.setText(oldVal);
                    }
                    System.out.println("Commiting " + textField.getText());
                    commitEdit(textField.getText());
                }
            });
            
            textField.textProperty().addListener( //09
                    (ObservableValue<? extends String> observable, String oldValue, String newValue)-> {
                if (!newValue.matches(charAccepted + "*")) {
                    textField.setText(newValue.replaceAll("[^" + charAccepted + "]", ""));
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem();
        }
    }
    
    class EditingCellInt extends TableCell<Commissione, Integer> {

        private TextField textField;

        private EditingCellInt() {}

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.requestFocus();
                textField.selectAll();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText(getItem().toString());
            setGraphic(null);
        }

        @Override
        public void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText("");
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.setOnAction((e) -> commitEdit(Integer.parseInt(textField.getText())));
            textField.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                if (!newValue) {
                    System.out.println("Commiting " + textField.getText());
                    commitEdit(Integer.parseInt(textField.getText()));
                }
            });
            textField.textProperty().addListener(
                    (ObservableValue<? extends String> observable, String oldValue, String newValue)-> {
                if (!newValue.matches("\\d*")) {
                    textField.setText(oldValue);
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }
    
    class EditingCellDbl extends TableCell<Commissione, Double> {

        private TextField textField;

        private EditingCellDbl() {}

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.requestFocus();
                textField.selectAll();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            
            setText(getString());
            setGraphic(null);
        }

        @Override
        public void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText("");
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }

        private void createTextField() {
            textField = new TextField();
            estraiCifre(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.setOnAction((e) -> {commitEdit(getDouble());});
            textField.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                if (!newValue) {
                    System.out.println("Commiting " + getDouble());
                    commitEdit(getDouble());
                }
            });
            textField.textProperty().addListener(
                    (ObservableValue<? extends String> observable, String oldValue, String newValue)-> {
                if (!newValue.matches("\\d*")) {
                    estraiCifre(newValue);
                }
            });
        }
        
        private void estraiCifre(String value){
            value = value.replaceFirst(",", ".");
            textField.setText(value.replaceAll("[^\\d.]", ""));
        }
        
        public double getDouble(){
            String txt = textField.getText();
            return Double.parseDouble(txt.equals("")?"0":txt);
        }

        private String getString() {
            return getItem() == null ? "" : String.format("%.2f €",getItem());
        }
    }
    
    class DateEditingCell extends TableCell<Commissione, LocalDate> { //10

        private DatePicker datePicker;
        private final DateTimeFormatter formatter;
        
        private DateEditingCell(String format) {
            formatter = DateTimeFormatter.ofPattern(format);
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createDatePicker();
                setText(null);
                setGraphic(datePicker);
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText(getDate().format(formatter));
            setGraphic(null);
        }

        @Override
        public void updateItem(LocalDate item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (datePicker != null) {
                        datePicker.setValue(getDate());
                    }
                    setText(null);
                    setGraphic(datePicker);
                } else {
                    setText(getDate().format(formatter));
                    setGraphic(null);
                }
            }
        }

        private void createDatePicker() {
            datePicker = new DatePicker(getDate());
            datePicker.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            datePicker.setOnAction((e) -> {
                System.out.println("Committed: " + datePicker.getValue().toString());
                commitEdit(datePicker.getValue());
            });
        }

        private LocalDate getDate() {
            return getItem() == null ? LocalDate.now() : getItem();
        }
    }
}

/*
Note:
(01)    file:///C:/prg/javafx2docs/api/javafx/scene/control/TableCell.html
        Per rendere editabili le celle della tabella è necessario estendere la classe
        TableCell e fare l'override dei suoi metodi: startEdit(),cancelEdit() e updateItem.
        file:///C:/prg/javafx2docs/api/javafx/scene/control/ListView.html#cellFactoryProperty()
        Per poter utilizzare delle celle diverse da quella di default bisogna modificare la
        cellFactory (fabbrica di celle) tramite oggetto che implementa l'interfaccia funzionale Callback<P,R>
        avente come (unico) metodo R call(P param) che restituisce la nuova cella di tipo R.

(02)    Vincola il dimensionamento delle colonne alla larghezza della tabella.

(03)    Utilizza l'algoritmo di default per il sorting degli elementi in base ai valori
        della prima colonna (Data).

(04)    Ogni volta che gli elementi cambiano all'interno della tabella la selezione
        deve essere resettata perchè potrebbe selezionare righe non più presenti.

(05)    Overloading del metodo che consente di mantenere un riferimento all'oggetto inserito,
        utile per la classe SimulatoreQRCode che modifica il tempo come se fosse un cronometro.
        Sincronizzato perché più thread lo utilizzano.

(06)    E' una TableCell che accetta solo caratteri che soddisfano la regex charAccepted e
        che cambia il valore della cella solo se il nuovo valore è conforme al pattern altrimenti
        rimette oldVal.

(07)    file:///C:/prg/javafx2docs/api/javafx/scene/control/Labeled.html#setText(java.lang.String)
        Modifica il testo all'interno della cella (diverso da quello del TextField).

(08)    Inserisce la grafica del Nodo specificato nell'argomento all'interno della TableCell.

(09)    Quando cambia il testo all'interno del TextField esegue quel codice e verifica
        che rispetti il vincolo sui caratteri tramite regex.

(10)    Consente di utilizzare un DatePicker per selezionare la data di ritiro e
        di formattarla secondo un formato scelto.
*/