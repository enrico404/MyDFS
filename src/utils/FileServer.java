package utils;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ScatteringByteChannel;

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
    private ServerSocket servSock = new ServerSocket();
    String path;
    public FileServer() throws IOException {
        super();
    }

    /**
     * Metodo per il binding di un'istanza del fileServer con un client. Se il socket è già bindato, viene solo settato
     * nuovamente il path
     * @param Port numero di porta in cui bindare il server con il client
     * @param Path percorso in cui verrà trasferito il file
     * @return true in caso di successo
     * @throws IOException
     */
    public boolean bind(int Port, String Path) throws IOException {
        if(!(servSock.isBound())){
            servSock.bind(new InetSocketAddress(Port));
        }
        path = Path;
        return true;
    }

    /**
     * Metodo per settare il percorso di destinazione in cui verrrà salvato il file. Se volessi quindi trasferire due
     * file differenti, basterà cambiare il percorso per iniziare il nuovo trasferimento.
     * @param Path percorso in cui verrà trasferito il file
     */
    public void setPath(String Path){
        path = Path;
    }

    /**
     * Metodo per far partire il thread e metterlo in ascolto di nuove connessioni
     *
     * @see FileServerThread
     */
    public void start(){
        DataInputStream in = null;
        FileOutputStream out = null;
        while (true) {
            try {
                Socket sock = servSock.accept();
                System.out.println("Accepted connection.. " + sock);
                //sSystem.out.println("path:"+ path);
                in = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
                out = new FileOutputStream(path);
                int read;
                byte[] temp = new byte[4096];
                System.out.println("Tranfering a file.. ");
                while ((read = in.read(temp)) > 0) {
                    out.write(temp, 0, read);
                }
                System.out.println("File transferred: "+ path);
                out.flush();

            }catch (FileNotFoundException e){
                System.err.println(ConsoleColors.RED+"Path di destinazione errato!"+ConsoleColors.RESET);

            }
            catch (IOException e){
                System.err.println(ConsoleColors.RED+"Errore nel socket del server"+ConsoleColors.RESET);
            }finally {
                if(in != null){
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null){
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


}
