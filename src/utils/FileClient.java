package utils;

import java.io.*;
import java.net.Socket;

/**
 * Lato client del file transfer system, si fa uso dei socket tpc per il trasfermento dei file. La connessione è quindi di
 * livello 4 tra i due host. Vedi classe "FileServer" per maggiori informazioni.
 *
 * @see FileServer
 */
public class FileClient {
    private Socket sock = null;

    /**
     * Costruttore con parametri del FileClient
     * @param Port numero di porta in cui aprire il socket tcp
     * @param ip stringa contenent l'ip in formato IpV4 del server a cui inviare i dati
     * @throws IOException
     */
    public FileClient(int Port, String ip) throws IOException {
        if(sock == null) {
            sock = new Socket();
            sock.setReuseAddress(true);
            sock = new Socket(ip, Port);
        }
    }

    /**
     * Metodo per inviare i file verso il server del file transfer system
     * @param filePath percorso sorgente del file da inviare
     * @throws IOException
     */
    public void send(String filePath) throws IOException {
        File f = new File(filePath);
        FileInputStream in = new FileInputStream(f);
        DataOutputStream out = new DataOutputStream(sock.getOutputStream());
        byte[] tmp = new byte[4096];
        int read;
        while((read = in.read(tmp)) > 0 ){
            out.write(tmp, 0, read);
        }
        out.flush();
        in.close();
        out.close();



    }

    /**
     * Metodo per la chiusura del socket
     * @throws IOException
     */
    public void closeConnection() throws IOException {
        sock.close();
    }
}
