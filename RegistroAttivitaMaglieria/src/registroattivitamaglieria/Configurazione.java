
package registroattivitamaglieria;

import java.io.Serializable;

class Configurazione implements Serializable{
    private String operatore;
    private String ipServerLog;
    private String portServerLog;
    private String usernameDB;
    private String passwordDB;
    private String ipDB;
    private String portDB;

    public String getOperatore() {
        return operatore;
    }

    public String getIpServerLog() {
        return ipServerLog;
    }

    public String getPortServerLog() {
        return portServerLog;
    }

    public String getUsernameDB() {
        return usernameDB;
    }

    public String getPasswordDB() {
        return passwordDB;
    }

    public String getIpDB() {
        return ipDB;
    }

    public String getPortDB() {
        return portDB;
    }
}
