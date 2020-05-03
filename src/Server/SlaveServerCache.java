package Server;

/**
 * Classe che gestisce i dati appartenenti ad un server
 */
public class SlaveServerCache {
    /**
     * Stringa contenente il nome del server
     */
    private String name;
    /**
     * Stringa contenente l'ip del server
     */
    private String ip;

    /**
     * Costruttore con parametri della classe
     * @param name nome del server
     * @param ip ip del server
     */
    public SlaveServerCache(String name, String ip) {
        this.name = name;
        this.ip = ip;
    }

    /**
     * Getter dell'attributo nome
     * @return stringa contenente il nome del server
     */
    public String getName() {
        return name;
    }

    /**
     * Getter dell'attributo ip
     * @return stringa contenente l'ip del server
     */
    public String getIp() {
        return ip;
    }
}
