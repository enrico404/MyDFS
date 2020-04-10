package utils;


import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Lato client del file transfer system, si fa uso dei socket tpc per il trasfermento dei file. La connessione è quindi di
 * livello 4 tra i due host. Vedi classe "FileServer" per maggiori informazioni.
 *
 * @see FileServer
 */
public class FileClient {
    /**
     * riferimento al socket per trasferire i file
     */
    private SocketChannel sock;

    /**
     * Costruttore con parametri del FileClient
     *
     * @param Port numero di porta in cui aprire il socket tcp
     * @param ip   stringa contenent l'ip in formato IpV4 del server a cui inviare i dati
     * @throws IOException
     */
    public FileClient(int Port, String ip) throws IOException {
        if (sock == null) {
            sock = SocketChannel.open();
            sock.connect(new InetSocketAddress(ip, Port));
        }
    }

    /**
     * Metodo per inviare i file verso il server del file transfer system
     *
     * @param filePath percorso sorgente del file da inviare
     * @throws IOException
     */
    public void send(String filePath, boolean verbose) {
        FileChannel inChannel = null;
        try {
            Path path = Paths.get(filePath);
            inChannel = FileChannel.open(path);

            ByteBuffer buffer = ByteBuffer.allocate(4096);
            int read = 0;
            long total = 0;
            long before = System.currentTimeMillis();
            float elapsedTime = 0;
            long after = 0;

            while ((read = inChannel.read(buffer)) > 0) {
                buffer.flip();
                sock.write(buffer);
                after = System.currentTimeMillis();
                elapsedTime = (after - before);
                total += read;
                buffer.clear();
                if (verbose)
                    if (elapsedTime > 0 && System.currentTimeMillis() % 60 == 0) {
                        System.out.print("\rTransfer speed: " + Converter.byte_to_humanS(total / (elapsedTime / 1000)) + "/S");
                    }
            }

            if (verbose) {
                System.out.println("");
                System.out.println("Trasferimento di: \"" + utils.getFileName(filePath) + "\" completato in: " + elapsedTime / 1000 + " Secondi");
            }

            sock.close();
            inChannel.close();

        } catch (FileNotFoundException e) {
            utils.error_printer("File non trovato " + filePath);
        } catch (IOException e) {
            utils.error_printer("Errore nella scrittura del file");
        } finally {
            if (inChannel != null) {
                try {
                    inChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (sock != null) {
                try {
                    sock.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    /**
     * Metodo per la chiusura del socket
     *
     * @throws IOException
     */
    public void closeConnection() throws IOException {
        sock.close();
    }
}
