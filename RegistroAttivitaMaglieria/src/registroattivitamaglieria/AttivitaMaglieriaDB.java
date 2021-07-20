package registroattivitamaglieria;

import java.sql.*;
import java.time.LocalDate;
import javafx.collections.*;

public class AttivitaMaglieriaDB {
    private static String operatore;
    private static String usernameDB;
    private static String passwordDB;
    private static String ipDB;
    private static String portDB;
    
    public static void configura(){
        operatore    = CacheLocale.getOperatore();
        usernameDB   = CacheLocale.getUsernameDB();
        passwordDB   = CacheLocale.getPasswordDB();
        ipDB         = CacheLocale.getIpDB();
        portDB       = CacheLocale.getPortDB();
    }
    
    public static ObservableList<Commissione> estraiCommissioni(int anno, int mese){
        ObservableList<Commissione> list = FXCollections.observableArrayList();
        try (Connection co = DriverManager.getConnection("jdbc:mysql://"+ipDB+":"+portDB+"/maglieria",usernameDB,passwordDB);
             Statement st = co.createStatement();
            ){ 
            ResultSet rs = st.executeQuery("SELECT * FROM maglieria.commissione "
                    + "WHERE operatore = '"+operatore+"' AND YEAR(data) = "+anno+" AND MONTH(data) = "+mese+";");
            while(rs.next()) {
                LocalDate d = rs.getDate("data").toLocalDate();
                String a = rs.getString("articolo");
                String o = rs.getString("operazione");
                int n = rs.getInt("numCapi");
                double p = rs.getDouble("prezzo");
                String t = rs.getString("tempo");
                LocalDate r = rs.getDate("ritiro").toLocalDate();
                double tot = p*n;
                
                list.add(new Commissione(d,a,o,n,p,t,r,tot));
            }
        } catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return list;
    }
    
    public static void caricaCommissione(Commissione comm){
        try(Connection co = DriverManager.getConnection("jdbc:mysql://"+ipDB+":"+portDB+"/maglieria",usernameDB,passwordDB);
            Statement st = co.createStatement();
        ){
            LocalDate data = comm.getData();
            String articolo = comm.getArticolo();
            String operazione = comm.getOperazione();
            int numCapi = comm.getNumCapi();
            double prezzo = comm.getPrezzo();
            String tempo = comm.getTempo();
            LocalDate ritiro = comm.getRitiro();

            st.executeUpdate("INSERT INTO `maglieria`.`commissione` (`operatore`, `data`, `articolo`, `operazione`, `numCapi`, `prezzo`, `tempo`, `ritiro`) "
                    + "VALUES ('" + operatore + "', '"+data+"', '"+articolo+"', '"+operazione+"', '"+numCapi+"', '"+prezzo+"', '"+tempo+"', '"+ritiro+"')"
                    + "ON DUPLICATE KEY UPDATE operazione = '"+operazione+"', numCapi = "+numCapi+", prezzo = "+prezzo+", tempo = '"+tempo+"', ritiro = '"+ritiro+"';");
            
        } catch (SQLException e){
            System.err.println(e.getMessage());
        }
    }
    
    public static void rimuoviCommissione(Commissione comm){
        try(Connection co = DriverManager.getConnection("jdbc:mysql://"+ipDB+":"+portDB+"/maglieria",usernameDB,passwordDB);
            Statement st = co.createStatement();)
        {
            LocalDate data = comm.getData();
            String articolo = comm.getArticolo();
            String operazione = comm.getOperazione();

            st.executeUpdate("DELETE FROM `maglieria`.`commissione` "
                    + "WHERE `operatore`='" + operatore + "' and`data`='"+data+"' and`articolo`='"+articolo+"' and`operazione`='"+operazione+"';");
            
        } catch (SQLException e){
            System.err.println(e.getMessage());
        }
    }
    
    public static ObservableList<String> estraiOperazioni(){
        ObservableList<String> listaOperazioni = FXCollections.observableArrayList();
        try(Connection co = DriverManager.getConnection("jdbc:mysql://"+ipDB+":"+portDB+"/maglieria",usernameDB,passwordDB);
            Statement st = co.createStatement();)
        {
            ResultSet rs = st.executeQuery("SELECT * FROM maglieria.operazione;");
            while(rs.next()){
                listaOperazioni.add(rs.getString("operazione"));
            }
        } catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return listaOperazioni;
    }
    
    public static ObservableList<String> estraiArticoli(){
        ObservableList<String> listaArticoli = FXCollections.observableArrayList();
        try(Connection co = DriverManager.getConnection("jdbc:mysql://"+ipDB+":"+portDB+"/maglieria",usernameDB,passwordDB);
            Statement st = co.createStatement();){
            ResultSet rs = st.executeQuery("SELECT * FROM maglieria.articolo;");
            while(rs.next()){
                listaArticoli.add(rs.getString("articolo"));
            }
        } catch(SQLException e){
            System.err.println(e.getMessage());
        }
        return listaArticoli;
    }
    
    public static Integer[][] estraiBustePaga(){
        Integer[][] somme = new Integer[3][];
        somme[0] = new Integer[12];
        somme[1] = new Integer[12];
        somme[2] = new Integer[12];
        for(int i=0; i<12; i++){
            somme[0][i] = 0;
            somme[1][i] = 0;
            somme[2][i] = 0;
        }
        
        try(Connection co = DriverManager.getConnection("jdbc:mysql://"+ipDB+":"+portDB+"/maglieria",usernameDB,passwordDB);
            Statement st = co.createStatement();){
            ResultSet rs = st.executeQuery("SELECT YEAR(data) AS anno,MONTH(data) AS mese,SUM(prezzo*numCapi) AS somma\n" +
                                            "FROM maglieria.commissione\n" +
                                            "WHERE operatore = '"+operatore+"' AND YEAR(data) = YEAR(CURDATE()) OR YEAR(data) = YEAR(CURDATE())-1 OR YEAR(data) = YEAR(CURDATE())-2\n" +
                                            "GROUP BY YEAR(data), MONTH(data);");
        int year = LocalDate.now().getYear()-2;
            while(rs.next()){
                somme[rs.getInt("anno")-year][rs.getInt("mese")-1] = rs.getInt("somma");
            }
        } catch(SQLException e){
            System.err.println(e.getMessage());
        }
        return somme;
    }
}
