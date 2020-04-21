package Server;

import utils.utils;

import java.rmi.*;
import java.util.ArrayList;

public class AsyncServersChecker extends Thread {

    private ServerManagerInterface ser;

    public AsyncServersChecker(ServerManagerInterface Ser){
        ser = Ser;
    }


    private void manageServerCrashed(int i, ServerManagerInterface ser) throws RemoteException {
        ser.getSlaveServers().remove(i);

        ArrayList<SlaveServerCache> slaveServerCaches = ser.getSlaveServerCaches();
        ArrayList<String> slaveServerNames = new ArrayList<>();
        for(ServerInterface slave: ser.getSlaveServers()){
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

                    } catch (ConnectIOException e) {
                        manageServerCrashed(j, ser);

                    } catch (ConnectException e) {
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
