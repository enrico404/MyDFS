package Server;

import utils.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.*;

public class ReconnecterThread extends Thread {

    private SlaveServerCache serverCache;
    private ServerManagerInterface ser;

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
                //se non provoca eccezioni funziona e lo riaggiungo alla lista di slave servers
                ser.getSlaveServers().add(slave);
                ser.reloadFileSystemTree();
                //devo rendere il server consistente
                ser.consistency_check();
                //esco dal ciclo perchè mi sono riconnesso correttamente
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
