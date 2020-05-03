package utils;


import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.EnumSet;

import utils.utils;

/**
 * Lato server del file transfer system, si fa uso del multithreading. In particolare si va ad istanziare un thread che
 * si mette in ascolto di nuove connessioni. Ogni volta che una connessione viene accettata si inizia il trasferimento
 * di un nuovo file. Se si vuol per esempio trasferire un file dall'host A all'host B il sistema andrà istanziato nel seguente
 * modo:   A (FileClient) - B(FileServer) . il file verrà quindi trasferito dall'host A all'host B. Questo è il funzionamento
 * base, ma in realtà si fa uso anche della classe FileServerThread, che permette di istanziare più server alla volta, in questo
 * modo è possibile avere connessioni multiple e trasferimenti di file multipli con più client alla volta.
 *
 * @see FileServerThread
 */
public class FileServer {
    /**
     * riferimento al socket per trasferire i file
     */
    private ServerSocketChannel servSock = ServerSocketChannel.open();
    /**
     * percorso del nuovo file trasferito, può contenere anche il nuovo nome
     */
    private String path;
    /**
     * flag che indica alla classe se essere verbose o meno
     */
    private boolean verbose;

    /**
     * Dimensione del file che verrà trasferito
     */
    private long size;


    /**
     * Costruttore con parametri della classe, è possibile specificare se la classe deve essere verbosa o meno
     *
     * @param Verbose flag che indica alla classe se essere verbose
     * @throws IOException
     */
    public FileServer(boolean Verbose) throws IOException {
        super();
        verbose = Verbose;

    }



    /**
     * Metodo per il binding di un'istanza del fileServer con un client. Se il socket è già bindato, viene solo settato
     * nuovamente il path
     *
     * @param Port numero di porta in cui bindare il server con il client
     * @param Path percorso in cui verrà trasferito il file
     * @return true in caso di successo
     * @throws IOException
     */
    public boolean bind(int Port, String Path, long Size) throws IOException {
        if (!(servSock.socket().isBound())) {
            servSock.socket().bind(new InetSocketAddress(Port));
        }
        path = Path;
        size = Size;
        return true;
    }

    /**
     * Metodo per settare il percorso di destinazione in cui verrrà salvato il file. Se volessi quindi trasferire due
     * file differenti, basterà cambiare il percorso per iniziare il nuovo trasferimento.
     *
     * @param Path percorso in cui verrà trasferito il file
     */
    public void setPath(String Path, long Size, boolean Verbose) {
        path = Path;
        size=Size;
        verbose = Verbose;
    }

    /**
     * Metodo per far partire il thread e metterlo in ascolto di nuove connessioni. Per il trasferimento di file si usa
     * JAVA.NIO con i metodi transferTO e transferFrom, si delega quindi il carico del trasferimento dei file al sistema
     * operativo, se il sistema presenta un meccanismo di DMA, il trasferimento sarà il più rapido possibile e senza alcun
     * carico sulla CPU
     *
     * @see FileServerThread
     */
    public void start() {
        FileChannel outChannel = null;

        while (true) {


            try {
                //System.out.println("In attesa...");
                SocketChannel sock = servSock.accept();
                //System.out.print("\rAccepted connection.. " + sock);
                Path OutPath = Paths.get(path);
                outChannel = FileChannel.open(OutPath, EnumSet.of(StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE
                ));


                //ByteBuffer buffer = ByteBuffer.allocate(4096);
                long read = 0;
                long before = System.currentTimeMillis();
                long total = 0;
                float elapsedTime = 0;
                long after = 0;

                // This method is potentially much more efficient than a simple loop that reads from the source channel and writes to this channel.
                // Many operating systems can transfer bytes directly from the source channel into the filesystem cache without actually copying them
                // it uses the DMA
                long bufferDim = 4096;
                while ((read = outChannel.transferFrom(sock, total, bufferDim)) > 0) {
                    // buffer.flip();
                    // outChannel.write(buffer);

                    total += read;
                    if (verbose) {
                        after = System.currentTimeMillis();
                        elapsedTime = (after - before);

                        // buffer.clear();

                        if (elapsedTime > 0 && System.currentTimeMillis() % 100 == 0) {
                            System.out.print("\rTransfer speed: " + Converter.byte_to_humanS(total / (elapsedTime / 1000)) + "/S | " +
                                    "Scaricati: "+Converter.byte_to_humanS(total)+" / "+ Converter.byte_to_humanS(size)+" | File: "+utils.getFileName(path));

                        }
                    }
                }

                if (verbose) {
                    System.out.println("");
                    System.out.println("Trasferimento di: \"" + utils.getFileName(path) + "\" completato in: " + elapsedTime / 1000 + " Secondi");

                }

                if(verbose) {
                    System.out.println("File: " + path + " traferito con successo");
                    System.out.println("");
                }
                outChannel.close();
                sock.close();

            }catch (NoSuchFileException e){

            }
            catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (outChannel != null) {
                    try {
                        outChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }


        }
    }


}
