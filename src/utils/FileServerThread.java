package utils;

import Server.ServerInterface;

import java.awt.image.ColorConvertOp;
import java.io.IOException;
import java.security.SignatureException;

/**
 * Classe di supporto per il file transfer system, estende la classe Thread. Al posto di istanziare un FileServer si istanzierà
 * un thread di questa classe, in questo modo si potranno gestire trasferimenti multipli di file da più client
 */
public class FileServerThread extends Thread{
    /**
     * porta utilizzata per il trasferimento del file
     */
    private int port;
    /**
     * percorso del nuovo file trasferito
     */
    private String path;
    /**
     * riferimento al fileServer che si occupa del trasferimento di file
     */
    private FileServer fs;
    /**
     * flag che indica alla classe se essere verbose o meno
     */
    private boolean verbose = true;

    /**
     * Dimensione del file che verrà trasferito
     */
    private long size;


    /**
     * Costruttore con parametri della classe
     * @param Port numero di porta in cui mettere in ascolto il thread
     * @param Path percorso di destinazione del file, verrà passato alla classe FIleServer che si occupa del vero e proprio
     *             trasferimento
     * @throws IOException
     */
    public FileServerThread(int Port, String Path, long Size) throws IOException {
        port = Port;
        path = Path;
        size = Size;
        fs =  new FileServer(verbose);
    }

    /**
     * Costruttore con parametri della classe
     * @param Port numero di porta in cui mettere in ascolto il thread
     * @param Path percorso di destinazione del file, verrà passato alla classe FIleServer che si occupa del vero e proprio
     *             trasferimento
     * @param Verbose flag che indica alla classe se essere verbose o meno
     * @throws IOException
     */
    public FileServerThread(int Port, String Path, boolean Verbose, long Size) throws IOException {
        port = Port;
        path = Path;
        verbose = Verbose;
        size = Size;
        fs = new FileServer(verbose);

    }

    /**
     * Metodo che va a chiamare il corrispettivo metodo per il setting del percorso del file di destinazione nel file server.
     * Server per iniziare il trasferimento di un nuovo file.
     * @param Path percorso del file di destinazione
     *
     */
    public void setPath(String Path, long Size, boolean Verbose){
        path = Path;
        size = Size;
        verbose = Verbose;
        fs.setPath(path,size, verbose);


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
            fs.bind(port, path, size);
            fs.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
