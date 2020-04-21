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
        for(int j=0; j<slaveServerCaches.size(); j++){
            if(!slaveServerCaches.get(j).getName().equals(ser.getSlaveServers().get(j).getName())){
                utils.error_printer("È stato rilevato un guasto nel server: "+slaveServerCaches.get(j).getName());

                //reconnecter thread
                ReconnecterThread reconnecter = new ReconnecterThread(slaveServerCaches.get(j), ser);
                reconnecter.start();
                //devo uscire subito dal ciclo, altrimenti vado outofbounds
                break;
            }
        }
    }


    public void run() {

        while (true) {
            int i = 0;

            try {
                ArrayList<ServerInterface> slaveServers = ser.getSlaveServers();
                for (ServerInterface slave : slaveServers) {
                    try {

                        //serve per vedere se effettivamente ho ottenuto una connessione all'oggetto funzionante
                        slave.getName();
                        i++;
                    } catch (ConnectIOException e) {
                        manageServerCrashed(i, ser);

                    } catch (ConnectException e) {
                        manageServerCrashed(i, ser);
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
