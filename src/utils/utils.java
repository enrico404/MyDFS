package utils;

import Client.ClientClass;
import Server.Tree;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

/**
 * Classe contenente una serie di metodi statici utili, che vengono utilizzati da tutte le classi del programma
 */
public class utils {

    /**
     * Metodo per ottenere gli indirizzi ip della macchina. Ogni interfaccia della macchina ha il suo ip
     * @return lista contenente gli indirizzi ip in formato IpV4
     * @throws SocketException
     */
    public static List<Inet4Address> getInet4Addresses() throws SocketException{
        List<Inet4Address> ipList = new ArrayList<Inet4Address>();
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for(NetworkInterface netIn: Collections.list(nets)){
            Enumeration<InetAddress> inetAddresses = netIn.getInetAddresses();
            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                    ipList.add((Inet4Address) inetAddress);
                }
            }
        }
            return ipList;
    }

    /**
     * Metodo per ottenere l'indirizzo MAC della macchina
     * @return stringa contenente il mac address
     * @throws SocketException
     */
    public static String getMacAddresses() throws SocketException {
        StringBuilder sb = new StringBuilder();
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netIn : Collections.list(nets)) {
            byte[] mac = netIn.getHardwareAddress();
            if (mac != null) {
                for (int i = 0; i < mac.length; i++) {
                    sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                }
            }
        }
        return sb.toString();
    }

    /**
     * Metodo per controllare se un array contiene una determinata stringa
     * @param array array di stringhe in input
     * @param val Stringa da cercare nell'array
     * @return ritorna true se l'array contiene la stringa val
     */
    public static boolean contains(String[] array, String val){
        for(String el: array){
            if(el.equals(val)) return true;
        }
        return false;
    }

    /**
     * Metodo per controllare se un array contiene una determinata stringa in una certa posizione
     * @param array array di stringhe in input
     * @param val Stringa da cercare nell'array
     * @param index indice in cui la stringa viene cercata
     * @return ritorna true se l'array contiene la stringa alla posizione indicata dall'indice
     */
    public static boolean contains(String[] array, String val, int index){
        if (array.length > index){
            if(array[index].equals(val)) return true;
        }
        return false;
    }

    /**
     * Metodo per controllare se un array di Nodi, contiene il nodo specificato
     * @param array array di nodi
     * @param node nodo da cercare
     * @return true se lo contiene, false altrimenti
     */
    public static boolean contains(ArrayList<Tree.Node> array, Tree.Node node){
        for(Tree.Node n: array){
            if(n.path.equals(node.path)){
                return true;
            }
        }
        return false;
    }

    /**
     * Metodo che dato un percorso in input restituisce il nome del file. Es. /home/xxx/prova.txt -- prova.txt
     * @param path percorso del file in input
     * @return stringa contenente il nome del file
     */
    public static String getFileName(String path){
        String[] splittedPath = path.split("/");
        if(splittedPath.length>0)
            return splittedPath[splittedPath.length-1];
        else
            return "";
    }

    /**
     * Metodo per la pulizia di una stringa contenente un path. Dato un input un percorso in un qualsiasi formato classico
     * restituisce il path in formato assoluto
     * @param str stringa in input
     * @param client istanza del client
     * @return stringa contenente il percorso in formato assoluto
     */
    public static String cleanString(String str, ClientClass client){
        String cleanedStr = str;
        if(cleanedStr.startsWith("'")){
            cleanedStr = cleanedStr.substring(1, cleanedStr.length()-1);
        }
        //se è uguale a . non devo fare niente, va gestito nel client, devo ricavare il nome dal path1
        if(cleanedStr.equals(".")){
            return cleanedStr;
        }

        if(cleanedStr.equals("..")){
            cleanedStr =  pathWithoutLast(client.getCurrentPath());
        }

        if(cleanedStr.startsWith("./")){
            cleanedStr = cleanedStr.substring(1, cleanedStr.length());
            cleanedStr = client.getCurrentPath()+cleanedStr;

        }
        //se è il nome di un file es. p2.txt, quindi percorso relativo, lo devo trasformare in assoluto
        if(!(cleanedStr.startsWith("/"))){
            cleanedStr = client.getCurrentPath()+"/"+cleanedStr;
        }

        return cleanedStr;

    }

    /**
     * Metodo per la stampa a console degli errori, la stampa è colorata di rosso
     * @param error stringa contenente l'errore
     */
    public static void error_printer(String error){
        System.err.println(ConsoleColors.RED+error+ConsoleColors.RESET);
    }


    /**
     * Metodo per ottenere il percorso senza l'ultimo file/directory
     * @param fullPath percorso completo
     * @return nuovo percorso
     */
    public static String pathWithoutLast(String fullPath){
        String[] list = fullPath.split("/");
        if(list.length>0) {
            String newPath = "";

            for (int i = 0; i < list.length - 1; i++) {
                newPath += list[i] + "/";
            }
            return newPath;
        }else
            return "";

    }

//    public static String relative_to_absolute(String path, String currentPath){
//        // dato path relativo ./xxx/file.txt, restituisce path assoluto al file, tolto il nome del file es /home/user/xxxx
//        path = path.substring(1, path.length());
//        path = currentPath+path;
//        String[] tmpArray = path.split("/");
//        String tmp = "";
//        for(int j=0; j<tmpArray.length-1; j++){
//            tmp += tmpArray[j]+"/";
//        }
//        return tmp;
//    }

}
