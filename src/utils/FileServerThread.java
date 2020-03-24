package utils;

import Server.ServerInterface;

import java.io.IOException;

/**
 * Classe di supporto per il file transfer system, estende la classe Thread. Al posto di istanziare un FileServer si istanzierà
 * un thread di questa classe, in questo modo si potranno gestire trasferimenti multipli di file da più client
 */
public class FileServerThread extends Thread{
    int port;
    String path;
    FileServer fs = new FileServer();

    /**
     * Costruttore con parametri della classe
     * @param Port numero di porta in cui mettere in ascolto il thread
     * @param Path percorso di destinazione del file, verrà passato alla classe FIleServer che si occupa del vero e proprio
     *             trasferimento
     * @throws IOException
     */
    public FileServerThread(int Port, String Path) throws IOException {
        port = Port;
        path = Path;

    }

    /**
     * Metodo che va a chiamare il corrispettivo metodo per il setting del percorso del file di destinazione nel file server.
     * Server per iniziare il trasferimento di un nuovo file.
     * @param Path percorso del file di destinazione
     *
     */
    public void setPath(String Path){
        path = Path;
        fs.setPath(path);
    }

    /**
     * Getter dell'attributo path
     * @return Stringa contenente il percorso in cui verrà salvato il nuovo file
     */
    public String getPath(){
        return path;
    }

    /**
     * Codice che viene eseguito allo start del thread
     */
    public void run(){
        try {
            fs.bind(port, path);
            fs.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
