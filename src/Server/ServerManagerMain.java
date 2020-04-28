package Server;

import utils.utils;

import java.net.Inet4Address;
import java.rmi.*;

import java.util.ArrayList;
import java.util.List;
import Client.ParamParser;
import utils.FileServerThread;


/**
 * Main della classe serverManager
 *
 * @throws RemoteException
 */
public class ServerManagerMain {

    public static void main(String args[]) {

        ArrayList<String> ipArr = new ArrayList<String>();

        String commandString = "";
        for (String cmd : args) {
            commandString += cmd;
        }

        String primarySerIP = "";
        String secondarySerIP = "";
        //sono un serverManager secondario
        if (ParamParser.checkParam(commandString, "-p")) {


            for (int i = 0; i < args.length; i++) {
                // il prossimo parametro è l'ip del server primario
                if (args[i].equals("-p")) {
                    //se viene specificato un ip dopo il parametro
                    if (i < args.length - 1) {
                        i++;
                        primarySerIP = args[i];
                        continue;
                    } else {
                        utils.error_printer("Non è stato specificato l'ip del serverManager primario!");
                    }
                }
                ipArr.add(args[i]);
            }

        }

        //sono un serverManager primario
        else if (ParamParser.checkParam(commandString, "-s")) {

            for (int i = 0; i < args.length; i++) {
                // il prossimo parametro è l'ip del server primario
                if (args[i].equals("-s")) {
                    //se viene specificato un ip dopo il parametro
                    if (i < args.length - 1) {
                        i++;
                        secondarySerIP = args[i];
                        continue;
                    } else {
                        utils.error_printer("Non è stato specificato l'ip del serverManager secondario!");
                    }
                }
                ipArr.add(args[i]);
            }
        }
        //non ho specificato niente, modalità di esecuzione come serverManager singolo
        else {
            for (String ip : args) {
                ipArr.add(ip);
            }
        }

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            List<Inet4Address> ips = utils.getInet4Addresses();
            if (ips.size() >= 1) {
                String myIp = ips.get(0).toString().substring(1);
                System.setProperty("java.rmi.server.hostname", myIp);
                ServerManager serM = new ServerManager("ServerManager", ipArr);
                Naming.rebind("//" + myIp + "/ServerManager", serM);
                System.out.println();
                System.out.println("ServerManager bindato nel registry");
                System.out.println("Indirizzo ip bindato: " + myIp);


                int port1 = 6660;


                if (!secondarySerIP.equals("") || !primarySerIP.equals("")) {


                    String fileSystemTreePath = System.getProperty("user.home") + "/.config/MyDFS/fileSystemTree";


                    //logica nuova: il primario manda periodicamente al secondario se è attivo, quando il primario crasha
                    // e il secondario si attiva, all'avvio recupero il file dal secondario, se sono diversi uso il secondario

                    if (!secondarySerIP.equals("")) {
//                        FileServerThread receiverThread = new FileServerThread(port1, fileSystemTreePath, false, 100);
//                        receiverThread.start();
                        //ho un secondario settato

                        String backupServerIp = "//"+secondarySerIP+"/ServerManager";
                        ServerManagerInterface backupServer =  (ServerManagerInterface) Naming.lookup(backupServerIp);

                        serM.setBackupServer(backupServer);

                        SecondaryServerUpdater updater = new SecondaryServerUpdater(secondarySerIP, port1);
                        updater.start();

                    }

                    if (!primarySerIP.equals("")) {
                        FileServerThread receiverThread = new FileServerThread(port1, fileSystemTreePath, false, 100);
                        receiverThread.start();
//                        SecondaryServerUpdater updater = new SecondaryServerUpdater(primarySerIP, port1);
//                        updater.start();

                    }


                }
                //caso in cui non nessuno dei due e quindo sono nella modalità di funzionamento a serverManager singolo
                //non c'è da fare niente
                else { }

                for (String ip : ipArr) {
                    System.out.println(ip);
                }


                if (!primarySerIP.equals(""))
                    Thread.sleep(1000);
                //serM.selShared_dir(System.getProperty("user.home") + "/shDir");
                //connetto il serverManger ai vari dataServer specificati
                if (!serM.connectToDataServers()) {
                    utils.error_printer("Errore nei data nodes");
                    return;
                }
                serM.asyncServersChecking();


                //serM.balance();
            } else {
                System.err.println("Non sei connesso ad una rete locale");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
