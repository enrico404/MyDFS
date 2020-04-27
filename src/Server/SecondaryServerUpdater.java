package Server;

import utils.FileClient;
import utils.FileServerThread;
import utils.utils;

import java.awt.desktop.SystemEventListener;
import java.io.File;
import java.io.IOException;

/**
 * Thread che si occupa di aggiornare il file system del nodo di backup se presente . Semplicemente viene copiato il file system
 * tree del server manager primario nel secondario ogni 550ms
 */
public class SecondaryServerUpdater extends Thread {

    /**
     * Stringa contenente l'ip del server manager secondario
     */
    private String secondaryServerIP;
    /**
     * porta in cui il file client si connette per trasferire il file
     */
    private int port;


    /**
     * Costruttore di default della classe
     * @param ip ip del server manager secondario
     * @param Port numero di porta
     */
    public SecondaryServerUpdater(String ip, int Port) {
        secondaryServerIP = ip;
        port = Port;
    }

    public void run() {

        FileClient fc;
        String fileSystemTreePath = System.getProperty("user.home") + "/.config/MyDFS/fileSystemTree";


        while (true) {
            try {

                this.sleep(550);
                File f = new File(fileSystemTreePath);
                if (f.exists()) {

                    fc = new FileClient(port, secondaryServerIP);
                    fc.send(fileSystemTreePath, false, f.length());


                }


            } catch (InterruptedException e) {

            } catch (IOException e) {

            }
        }


    }
}

