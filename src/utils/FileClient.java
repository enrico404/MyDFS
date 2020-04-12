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
     * Metodo per inviare i file verso il server del file transfer system. Per il trasferimento di file si usa
     * JAVA.NIO con i metodi transferTO e transferFrom, si delega quindi il carico del trasferimento dei file al sistema
     * operativo, se il sistema presenta un meccanismo di DMA, il trasferimento sarà il più rapido possibile e senza alcun
     * carico sulla CPU
     *
     * @param filePath percorso sorgente del file da inviare
     * @param verbose flag che indica alla classe se essere verbosa o meno
     * @param size dimensione del file da mandare
     * @throws IOException
     */
    public void send(String filePath, boolean verbose, long size) {
        FileChannel inChannel = null;
        try {
            Path path = Paths.get(filePath);
            inChannel = FileChannel.open(path);

            //ByteBuffer buffer = ByteBuffer.allocate(4096);
            long read = 0;
            long total = 0;
            long before = System.currentTimeMillis();
            float elapsedTime = 0;
            long after = 0;
            long bufferDim = 4096;
            while ((read = inChannel.transferTo(total, bufferDim, sock)) > 0) {
                //buffer.flip();
                //sock.write(buffer);

                total += read;
                if (verbose) {
                    after = System.currentTimeMillis();
                    elapsedTime = (after - before);

                    //  buffer.clear();

                    if (elapsedTime > 0 && System.currentTimeMillis() % 100 == 0) {
                        System.out.print("\rTransfer speed: " + Converter.byte_to_humanS(total / (elapsedTime / 1000)) + "/S\t | " +
                                "Scaricati: "+Converter.byte_to_humanS(total)+" / "+ Converter.byte_to_humanS(size));
                    }
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
