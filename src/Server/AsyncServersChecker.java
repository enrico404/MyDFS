package Server;

import Client.ClientClass;
import utils.utils;

import java.rmi.*;
import java.util.ArrayList;

/**
 * Thread che si occupa di controllare lo stato dei data nodes. Se uno di questi crasha viene riportato all'utente.
 * Il sistema continua a funzionare anche se ci sono diversi data nodes crashati.
 * Se sono presenti n data nodes, si supportano fino ad n-1 crash.
 * Ogni secondo si prova a ristabilire la connessione con i data nodes crashati. Se viene ristabilita viene eseguito
 * un controllo sulla consistenza del nodo.
 */
public class AsyncServersChecker extends Thread {

    /**
     * Riferimento al serverManager
     */
    private ServerManagerInterface ser;

    /**
     * Costruttore con parametri della classe
     * @param Ser riferimento al server manager
     */
    public AsyncServersChecker(ServerManagerInterface Ser){
        ser = Ser;
    }


    /**
     * Metodo interno per gestire il crash di un data node. Se viene rilevato il crash, si lancia un secondo thread
     * che tenta di ristabilire la connessione ogni secondo.
     * @param i indice del data node crashato
     * @param ser riferimento al server manager
     * @throws RemoteException
     */
    private void manageServerCrashed(int i, ServerManagerInterface ser) throws RemoteException {
        ser.getSlaveServers().remove(i);

        ArrayList<SlaveServerCache> slaveServerCaches = ser.getSlaveServerCaches();
        ArrayList<String> slaveServerNames = new ArrayList<>();

        for(ServerInterface slave: ser.getSlaveServers()){
            if(!utils.contains(slaveServerNames, slave.getName()))
                slaveServerNames.add(slave.getName());
        }


        for(SlaveServerCache slaveCache: slaveServerCaches){
            if(!utils.contains(slaveServerNames, slaveCache.getName())){
                utils.error_printer("È stato rilevato un guasto nel server: "+slaveCache.getName());
                //reconnecter thread
                ReconnecterThread reconnecter = new ReconnecterThread(slaveCache, ser);
                reconnecter.start();
            }
        }

    }


    public void run() {

        while (true) {


            try {
                ArrayList<ServerInterface> slaveServers = ser.getSlaveServers();

                for(int j=0; j<slaveServers.size(); j++){
                    try {

                        //serve per vedere se effettivamente ho ottenuto una connessione all'oggetto funzionante, se non
                        //funziona viene lanciata una delle due eccezioni
                        slaveServers.get(j).getName();

                    } catch (ConnectException | ConnectIOException e) {
                        manageServerCrashed(j, ser);

                    }


                }
            } catch (RemoteException e) { }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }




        }
    }

}
