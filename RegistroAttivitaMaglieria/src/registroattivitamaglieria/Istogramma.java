
package registroattivitamaglieria;

import java.time.LocalDate;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;

class Istogramma extends BarChart<String,Number>{

    public Istogramma(Axis<String> xAxis, Axis<Number> yAxis) {
        super(xAxis, yAxis);
        int annoCorrente = LocalDate.now().getYear();
        
        setTitle("Sommario Buste Paga");
        
        xAxis.setLabel("Mese");       
        yAxis.setLabel("Stipendio");
        xAxis.setAnimated(false); //01
        yAxis.setAnimated(false);
        
        XYChart.Series<String,Number> series1 = new XYChart.Series();
        XYChart.Series<String,Number> series2 = new XYChart.Series();
        XYChart.Series<String,Number> series3 = new XYChart.Series();
        
        series1.setName(annoCorrente+""); 
        series2.setName(annoCorrente-1+"");
        series3.setName(annoCorrente-2+"");

        for(Integer i=0; i<12; i++){
            series1.getData().add(new XYChart.Data(GUI.mesi[i], 0));
            series2.getData().add(new XYChart.Data(GUI.mesi[i], 0));
            series3.getData().add(new XYChart.Data(GUI.mesi[i], 0));
        }
        getData().addAll(series3, series2, series1);
        aggiorna();
    }
    
    public final void aggiorna(){
        Integer[][] somme = AttivitaMaglieriaDB.estraiBustePaga();
        ObservableList<XYChart.Series<String,Number>> serie = getData();
        for(Integer i=0; i<12; i++){
            serie.get(0).getData().get(i).setYValue(somme[0][i]);//02
            serie.get(1).getData().get(i).setYValue(somme[1][i]);
            serie.get(2).getData().get(i).setYValue(somme[2][i]);
        }
    }
}

/*
Note:
01) Bug grafico delle ordinate una volta cambiata scala dei valori, disattivando
    le animazioni funziona normalmente.

02) Per ogni mese e anno aggiorna il valore delle Y tramite la matrice somme[x][y]
    dove x = 0 (annoCorrente-2) e y sono i mesi.
*/
