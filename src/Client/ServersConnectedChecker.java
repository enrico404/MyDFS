package Client;

import Server.ServerManager;
import Server.ServerManagerInterface;
import utils.utils;

import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ServersConnectedChecker extends Thread {

    private ClientClass client;

    public ServersConnectedChecker(ClientClass client) {
        this.client = client;
    }

    public void run() {

        boolean onSecondary = false;
        String primaryIp = "";
        String secondaryIp = "";
        while (true) {

            try {
                if (client.getSer().getSlaveServers().size() == 0) {
                    utils.error_printer("Fallimento totale del sistema! Contattare immediatamente l'amministratore di sistema! (tutti i data nodes falliti)");
                }

                //se sono sul primario e ho un server secondario
                if (!onSecondary && client.getBackupSer() != null) {
                    try {
                        primaryIp = client.getSer().getIp();
                        secondaryIp = client.getBackupSer().getIp();
                    }catch (Exception e){}
                }

                if (onSecondary && client.getBackupSer() != null) {
                    //tentativo di riconnessione al primario..
                    try {


                        ServerManagerInterface ser = (ServerManagerInterface) Naming.lookup("//" + primaryIp + "/ServerManager");
                        ser.getIp();
                        //se non causa errori vuol dire che ora funziona e posso riconnettermi al primario
                        utils.success_printer("Primario nuovamente online! Mi riconnetto.");
                        System.out.println("");
                        System.out.print(">");
                        //swappo nuovamente i due server
                        ServerManagerInterface backupSer = client.getSer();
                        client.setSer(ser);
                        client.setBackupSer(backupSer);
                        onSecondary = false;

                    } catch (Exception e) { }
                }


            } catch (Exception e) {
                //si verifica se il serverManager crasha, in questo caso devo connettere il client al serverManger secondario
                //se disponibile
                try {
                    if (client.getBackupSer() != null) {
                        utils.error_printer("ServerManager primario fallito, passo al secondario");
                        System.out.println("");
                        System.out.print(">");

                        ServerManagerInterface ser = null;

                        ser = (ServerManagerInterface) Naming.lookup("//" + secondaryIp + "/ServerManager");
                        ser.getIp();


                        //swappo i due server
                        ServerManagerInterface oldPrimary = client.getSer();
                        client.setSer(ser);
                        client.setBackupSer(oldPrimary);
                        onSecondary = true;

                    }
                } catch (Exception e2) {
                    utils.error_printer("Fallimento totale del sistema! Contattare immediatamente l'amministratore di sistema! (tutti serverManager falliti)");
                }


            }


            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }


}
