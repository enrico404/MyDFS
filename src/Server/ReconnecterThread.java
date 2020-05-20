package Server;

import Client.ClientClass;
import utils.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.*;

/**
 * Thread specifico che si occupa di provare a ristabilire ogni secondo la connessione con il data nodes crashato.
 * Se ci riesce e il nodo torna online, ne viene controllata la consistenza del file system, se è rimasto indietro
 * viene aggiornato secondo il file system del servber Manager
 */
public class ReconnecterThread extends Thread {

    /**
     * dati del server crashato: ip e nome
     */
    private SlaveServerCache serverCache;
    /**
     * riferimento al server manager
     */
    private ServerManagerInterface ser;

    /**
     * Costruttore con parametri della classe
     * @param ServerCache cache del server crashato
     * @param Ser riferimento al server manager
     */
    public ReconnecterThread(SlaveServerCache ServerCache, ServerManagerInterface Ser) {
        serverCache = ServerCache;
        ser = Ser;
    }

    public void run() {

        while (true) {

            try {

                ServerInterface slave = (ServerInterface) Naming.lookup(serverCache.getIp());
                //serve per vedere se effettivamente ho ottenuto una connessione all'oggetto funzionante
                slave.getName();
                System.out.println("Mi sono riconnesso con: "+serverCache.getName());
                //se non provoca eccezioni funziona e lo riaggiungo alla lista di slave servers se non è già presente
                boolean contains = false;
                for(ServerInterface sl: ser.getSlaveServers()){
                    if(sl.getName().equals(slave.getName())){
                        contains = true;
                    }
                }
                //se non c'è lo aggiungo e controllo la consistenza
                if(!contains) {
                    ser.getSlaveServers().add(slave);
                    ser.reloadFileSystemTree();
                    //devo rendere il server consistente
                    ser.consistency_check();
                    //esco dal ciclo perchè mi sono riconnesso
                }
                break;

            } catch (ConnectIOException e) {
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
            } catch (IOException e) {
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
