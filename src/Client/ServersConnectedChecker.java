package Client;

import Server.ServerManagerInterface;
import utils.utils;

import java.rmi.RemoteException;

public class ServersConnectedChecker extends Thread{

    private ServerManagerInterface ser;

    public ServersConnectedChecker(ServerManagerInterface Ser){
        ser = Ser;

    }

    public void run(){

        while(true){

            try {
                if(ser.getSlaveServers().size() == 0){
                    utils.error_printer("Fallimento totale del sistema! Contattare immediatamente l'amministratore di sistema!");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }


            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }


}
