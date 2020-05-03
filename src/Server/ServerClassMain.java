package Server;

import utils.utils;

import java.net.Inet4Address;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Main della classe ServerClass
 *
 * @throws RemoteException
 */
public class ServerClassMain {

    public static void main(String args[]) throws RemoteException {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            List<Inet4Address> ips = utils.getInet4Addresses();
            if (ips.size() >= 1) {
                String myIp = ips.get(0).toString().substring(1);
                System.setProperty("java.rmi.server.hostname", myIp);
                System.out.println("Inserisci il nome del server: ");
                Scanner in = new Scanner(System.in);
                String name = in.nextLine();

                // String name = utils.getMacAddresses();
                ServerClass ser = new ServerClass(name);
                Naming.rebind("//" + myIp + "/" + name, ser);
                System.out.println();
                System.out.println(name + " bindato nel registry");
                System.out.println("Indirizzo ip bindato: " + myIp);
                HashMap<String, String> config = utils.toHashMap(System.getProperty("user.home")+"/.config/MyDFS/configMyDFS.txt");
                String sharedDir = config.get("SHDIR_MyDFS");
                ser.setSharedDir(sharedDir);

//            ArrayList<MyFileType> res = ser.ls_func(ser.getSharedDir(), true);
//            for(MyFileType f:res){
//                System.out.println("Name: "+f.getName());
//                System.out.println("Size: "+ f.getSize());
//            }
            } else {
                utils.error_printer("Non sei connesso ad una rete locale");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
