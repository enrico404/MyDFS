package Client;

import Server.ServerInterface;
import Server.ServerManagerInterface;
import utils.MyFileType;
import utils.ConsoleColors;
import utils.Helper;


import java.io.*;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;

import utils.utils;
import utils.FileClient;
import utils.FileServerThread;
import utils.Converter;

import javax.swing.*;


/**
 * Classe client principale. Il client mantiene lo stato, in questo modo il server risulta essere più semplice e non ha
 * bisogno di manternere le informazioni per i vari client. Grazie a questo approccio si va a favorire la scalabilità del
 * sistema e maggiore efficienza.
 */
public class ClientClass implements Serializable {
    /**
     * percorso in cui si trova il client all'interno del file-system
     */
    private String currentPath = "";
    /**
     * porta utilizzata per il trasferimento di file dal client verso il server
     */
    private int port = 6668;
    /**
     * porta utilizzata per il trasferimento di file dal server verso il client
     */
    private int port2 = 6669;
    /**
     * riferimento al FileClient, serve per mandare i file
     */
    private FileClient fc = null;
    /**
     *  riferimento al FileServer, serve per ricevere i file
     */
    private FileServerThread thread = null;



    /**
     * Costruttore per la classe client
     *
     * @param ser nodeManager del cluster
     */
    public ClientClass(ServerManagerInterface ser) throws RemoteException {
        super();
        currentPath = ser.getSharedDir();
    }

    /**
     * Ritorna il path corrente in cui si trova il client all'interno del cluster
     *
     * @return currentPath, variabile che mantiene il path del client
     */
    public String getCurrentPath() {
        return currentPath;
    }

    /**
     * Ritorna il riferimento al client del file transfer system
     *
     * @return fc, riferimento al file client tansfer
     */
    public FileClient getFileClient() {
        return fc;
    }

    /**
     * Metodo che restituisce l'ip del client, in questo modo si agevola l'interazione con l'utente.
     * L'interfaccia presa in considerazione di default è la 0
     *
     * @return ip, stringa contenente l'ip del client in formato IpV4
     * @throws SocketException
     */
    public String getIp() throws SocketException {
        String ip = utils.getInet4Addresses().get(0).toString().substring(1);
        return ip;
    }

    /**
     * Metodo per la stampa dei file/directory nel cluster relativi al path corrente del client.
     * Si è deciso di non calcolare in tempo reale la dimensione delle directory poichè ralllenterebbe troppo il tempo
     * di esecuzione del comando. Anche il comando ls di linux utilizza lo stesso approccio.
     *
     * @param ser         riferimento al nodeManager
     * @param dirCapacity flag per indicare se si vuole anche calcolare la capacità delle directory o meno
     * @throws RemoteException
     */
    public void ls_func(ServerManagerInterface ser, boolean dirCapacity, boolean verbose, boolean hread) throws RemoteException {
        ArrayList<MyFileType> res = ser.ls_func(currentPath, dirCapacity);
        System.out.println("");
        if (!hread) {
            for (MyFileType file : res) {
                if (file.getType().equals("File")) {
                    if (verbose)
                        System.out.println(file.getName() + "     | Type: " + file.getType() + "     | Size (bytes): " + file.getSize() + " | location: " + file.getLocation());
                    else
                        System.out.print(file.getName() + "   ");
                } else {
                    if (verbose)
                        System.out.println(ConsoleColors.GREEN + file.getName() + ConsoleColors.RESET + "     | Type: " + file.getType() + "     | Size (bytes): " + file.getSize() + " | location: - ");
                    else
                        System.out.print(ConsoleColors.GREEN + file.getName() + "   " + ConsoleColors.RESET);
                }
            }
            System.out.println("");
            System.out.println("");

        } else {
            //human readable size
            String hsize = null;
            for (MyFileType file : res) {
                hsize = Converter.byte_to_humanS(file.getSize());
                if (file.getType().equals("File")) {
                    if (verbose)
                        System.out.println(file.getName() + "     | Type: " + file.getType() + "     | Size: " + hsize + " | location: " + file.getLocation());
                    else
                        System.out.print(file.getName() + "   ");
                } else {
                    if (verbose)
                        System.out.println(ConsoleColors.GREEN + file.getName() + ConsoleColors.RESET + "     | Type: " + file.getType() + "     | Size: " + hsize + " | location: - ");
                    else
                        System.out.print(ConsoleColors.GREEN + file.getName() + "   " + ConsoleColors.RESET);
                }
            }
        }
        System.out.println("");
        System.out.println("");
    }

    /**
     * Metodo per la creazione di una directory all'interno del cluster. La gestione del file system all'interno del cluster
     * viene spiegata nella documentazione della classe "ServerManager"
     *
     * @param path percorso alla nuova directory
     * @return true se la directory è creata con successo all'interno del cluster, false altrimenti.
     * @throws RemoteException
     * @see Server.ServerManager
     */
    public boolean mkdir(String path) throws RemoteException {
        File f = new File(path);
        if (f.mkdir()) {
            //System.out.println("Directory creata "+ f.getName());
            return true;
        }
        return false;
    }


    /**
     * Metodo per spostarsi all'interno del filesystem del cluster. È ispirato al comando "cd" di Linux.
     *
     * @param ser  riferimento al nodeManger del cluster
     * @param path percorso alla cartella in cui ci si vuole spostare
     * @return true se il comando è stato eseguito con successo, false altrimenti
     * @throws RemoteException
     */
    public boolean cd_func(ServerManagerInterface ser, String path) throws RemoteException {
        if (path.equals("..") && !(currentPath.equals(ser.getSharedDir()))) {
            String dirs[] = currentPath.split("/");
            currentPath = "";
            for (int i = 0; i < dirs.length - 1; i++) {
                if (i < dirs.length - 2) {
                    currentPath += dirs[i] + '/';
                } else   //ultima iterazione
                    currentPath += dirs[i];
            }
            return true;
        } else {
            if (!path.equals("..")) {
                path = utils.cleanString(path, this);
                boolean exists = ser.checkExists(path);
                if (exists) {
                    currentPath = path;
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Metodo per la cancellazione di un file all'interno del cluster
     *
     * @param ser   riferimento al nodeManager del cluster
     * @param paths percorsi ai file da cancellare
     * @return true se la cancellazione  dei file è stata eseguita con successo, false altrimenti
     * @throws RemoteException
     */
    public boolean rm_func(ServerManagerInterface ser, ArrayList<String> paths) throws RemoteException {
        for (String path : paths) {
            //se fallisce la cancellazione
            if (!(ser.rm_func(currentPath + '/' + path))) {
                utils.error_printer("Fallita l'eliminazione di " + path);
                return false;
            }
            System.out.println("File " + path + " eliminato con successo!");
        }

        return true;
    }

    /**
     * Metodo per la cancellazione ricorsiva delle directory all'interno del fle system distribuito
     *
     * @param ser   riferimento al nodeManager del cluster
     * @param paths percorso alla directory da eliminare
     * @return true se l'eliminazione della directory avviene con successo, false altrimenti.
     * @throws RemoteException
     */
    public boolean rm_func_rec(ServerManagerInterface ser, ArrayList<String> paths) throws RemoteException {
        for (String path : paths) {
            //se fallisce la cancellazione
            if (!(ser.rm_func_rec(currentPath + '/' + path))) {
                utils.error_printer("Fallita l'eliminazione di " + path);
                return false;
            }
            System.out.println("" + path + " eliminato con successo!");
        }

        return true;
    }

    /**
     * Metodo per la copia di un file da file system locale al cluster
     *
     * @param ser        riferimento al nodeManager del cluster
     * @param localPath  percorso al file locale all'interno del sistema
     * @param remotePath percorso in cui verrà copiato il file, si può specificare anche il nome. Es. ./dir1/fileName
     * @return true se la copia avviene con successo
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean cp_func(ServerManagerInterface ser, String localPath, String remotePath) throws IOException {
        //System.out.println("loc: "+localPath);
        //System.out.println("rem: "+remotePath);
        //recupero indice dello slave più libero
        int slaveIndex = ser.freerNodeChooser();
        // recupero il reference al nodo slave
        ServerInterface slave = ser.getSlaveNode(slaveIndex);
        //System.out.println("nodo scelto: "+ slave.getName());
        slave.startFileServer(port, remotePath);

        fc = new FileClient(port, slave.getIp());
        localPath = utils.cleanString(localPath, this);
        fc.send(localPath, true);
        return true;

    }


    /**
     * Overloading della funzione cp_func, va a gestire anche i seguenti casi di copia:
     * <ul>
     *     <li>remoto (directory) - locale </li>
     *     <li>remoto (file) - locale</li>
     *     <li>locale (directory) - remoto</li>
     * </ul>
     * con "remoto" si fa diretto riferimento al cluster
     *
     * @param ser     riferimento al nodeManager del cluster
     * @param path1   percorso al file/directory sorgente
     * @param path2   percorso di destinazione
     * @return true se la copia avviene con successo, false altrimenti
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean cp_func(ServerManagerInterface ser, String path1, String path2, String cmd, boolean verbose) throws IOException, InterruptedException {

        if (ParamParser.checkParam(cmd, "-rm")) {


            // caso copia ricorsiva di directory remota
            if (ser.checkExists(path1)) {
                System.out.println("Inizio copia ricorsiva ");
                //il client in questo caso diventa il server ricevitore di file (FileServer) e il server diventa il FileClient
                if (thread == null) {
                    if (verbose)
                        thread = new FileServerThread(port2, path2, true);
                    else
                        thread = new FileServerThread(port2, path2, false);
                    thread.start();
                }
                thread.setPath(path2);
                //recursiveCopy_remote(path1, path2, slave);
                recursiveCopy_remote(path1, path2, ser);
            } else {
                utils.error_printer("la directory \"" + utils.getFileName(path1) + "\" non esiste!");
            }
            //}

        } else if (ParamParser.checkParam(cmd,"-r")) {
            // caso copia ricorsiva directory (da locale a remoto)

            File f = new File(path1);
            if (f.exists() && f.isDirectory()) {

                //System.out.println("Prova creazione directory su slave");
                //slave.mkdir("/home/enrico404/shDir/dirCopy");
                System.out.println("Inizio la copia ricorsiva di " + f.getName());

                recursiveCopy(f, ser, path1, path2);

            } else {
                utils.error_printer("La directory specificata non esiste!");
            }

        } else if (ParamParser.checkParam(cmd, "-m")) {
            // caso remoto (solo file)


            // path1 è il path del file remoto e path2 è il path del file in locale
            String location = ser.getFileLocation(path1);
            if (location != null) {
                ServerInterface slave = ser.getSlaveNode(location);
                //il client in questo caso diventa il server ricevitore di file (FileServer) e il server diventa il FileClient
                if (thread == null) {
                    if (verbose)
                        thread = new FileServerThread(port2, path2, true);
                    else
                        thread = new FileServerThread(port2, path2, false);
                    thread.start();
                }
                thread.setPath(path2);

                slave.startFileClient(port2, getIp(), path1);


            } else {
                utils.error_printer("Il file \"" + utils.getFileName(path1) + "\" non esiste");
                return false;
            }

        }
        return true;
    }

    /**
     * Metodo per la copia ricorsiva di directory da locale a remoto, viene utilizzato dalla funzione "cp_func"
     *
     * @param f          riferimento al file/directory da copiare
     * @param ser        riferimento al nodeManger del cluster
     * @param localPath  path sorgente del file i-esimo da copiare, inizialmente contiene il percorso alla directory
     * @param remotePath path di destinazione del file i-esimo copiato
     * @throws IOException
     * @throws InterruptedException
     */
    public void recursiveCopy(File f, ServerManagerInterface ser, String localPath, String remotePath) throws IOException, InterruptedException {
        if (f.isDirectory()) {
//            int slaveIndex = ser.freerNodeChooser();
//            ServerInterface slave = ser.getSlaveNode(slaveIndex);
            for (ServerInterface slave : ser.getSlaveServers()) {
                slave.mkdir(remotePath);
            }
            for (File sub : f.listFiles()) {
                String localPathNew = localPath + '/' + sub.getName();
                String remotePathNew = remotePath + '/' + sub.getName();
                recursiveCopy(sub, ser, localPathNew, remotePathNew);
            }
        } else {

            cp_func(ser, localPath, remotePath);
        }

    }


    /**
     * Metodo per la copia ricorsiva di directory da remoto a locale, viene utilizzato dalla funzione "cp_func"
     *
     * @param remotePath path della directory da copiare, viene poi aggiornato iterazione per iterazione
     *                   con i path ai vari file
     * @param clientPath path del file i-esimo di destinazione sul client
     * @param sm         riferimento al nodeManager del cluster
     * @throws IOException
     */
    public void recursiveCopy_remote(String remotePath, String clientPath, ServerManagerInterface sm) throws IOException {
        //System.out.println("RemotePath: "+ remotePath);
        //System.out.println("Client path: "+clientPath);
        for (ServerInterface slave : sm.getSlaveServers()) {
            boolean flag = slave.isDirectory(remotePath);
            thread.setPath(clientPath);
            if (flag) {

                mkdir(clientPath);
                for (File sub : slave.listFiles(remotePath)) {
                    String remotePathNew = remotePath + '/' + sub.getName();
                    String clientPathNew = clientPath + '/' + sub.getName();
                    recursiveCopy_remote(remotePathNew, clientPathNew, sm);
                }
            } else {
                //copia del singolo file
                if (slave.checkExists(remotePath))
                    slave.startFileClient(port2, getIp(), remotePath);
            }

        }

    }


    /**
     * Metodo per la stampa su standard output i nodi a cui il serverManager è attualmente connesso
     *
     * @param ser   Riferimento al serverManager
     * @param cmd Stringa contenente il comando
     * @throws RemoteException
     */
    public void sview_func(String cmd, ServerManagerInterface ser) throws RemoteException, SocketException {
        System.out.println("Sono connesso con i seguenti data nodes: ");
        System.out.println("");
        for (ServerInterface slave : ser.getSlaveServers()) {
            System.out.println("Name: " + slave.getName() + " |  ip: " + slave.getIp());

            if(ParamParser.checkParam(cmd, "-h")){
                String freeSpace_hS = Converter.byte_to_humanS(slave.getFreeSpace());
                System.out.println(ConsoleColors.CYAN + "Spazio disponibile: " + freeSpace_hS + ConsoleColors.RESET);
            }else{
                //caso di default senza parametri
                System.out.println(ConsoleColors.CYAN + "Spazio disponibile: " + slave.getFreeSpace() + ConsoleColors.RESET);
            }

        }
        System.out.println("");
    }


    /**
     * Metodo che stampa su standard output informazioni utili riguardo il cluster, come ad esempio la capacità e lo spazio
     * disponibile
     *
     * @param cmd stringa del comando in ingresso
     * @param ser   riferimento al serverManager
     * @throws RemoteException
     */
    public void du_func(String cmd, ServerManagerInterface ser) throws RemoteException {

        if (ParamParser.checkParam(cmd, "-h")) {
            float freeSpace_h = Converter.byte_to_human(ser.getFreeSpace());
            float clusterSpace_h = Converter.byte_to_human(ser.getClusterCapacity());

            String freeSpace_hS = Converter.byte_to_humanS(ser.getFreeSpace());
            String clusterSpace_hS = Converter.byte_to_humanS(ser.getClusterCapacity());

            System.out.println("");
            System.out.println(ConsoleColors.CYAN + "Capacità disponibile del cluster: " + freeSpace_hS);
            System.out.println("Capacità massima del cluster: " + clusterSpace_hS);
            System.out.println("");

            float perc = (freeSpace_h * 100) / clusterSpace_h;

            System.out.println("Spazio disponibile in percentuale: " + perc + "%" + ConsoleColors.RESET);
        } else{
            //caso di default senza parametri
            System.out.println("");
            System.out.println(ConsoleColors.CYAN + "Capacità disponibile del cluster: " + ser.getFreeSpace());
            System.out.println("Capacità massima del cluster: " + ser.getClusterCapacity());
            System.out.println("");

            float perc = (ser.getFreeSpace() * 100) / (float) ser.getClusterCapacity();

            System.out.println("Spazio disponibile in percentuale: " + perc + "%" + ConsoleColors.RESET);
        }
        System.out.println("");


    }


    /**
     * Metodo che si occupa di aprire un file all'interno del cluster. La logica è molto semplice, viene creata una
     * copia temporanea del file nel filesystem locale al client. Successivamente il file viene aperto con il comando
     * standard di UNIX per aprire i file (xdg-open)
     *
     * @param param path del file da aprire
     * @param ser   riferimento al serverManager
     * @param cmd Stringa contenente il comando
     * @return true se il file viene aperto correttamente, false altrimenti
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean open(String[] param, ServerManagerInterface ser, String cmd) throws IOException, InterruptedException {
        if (param.length == 2) {
            String filePath = param[1];
            //il file che sto cercando di aprire deve essere un file e non una directorys
            //pulisco il path
            filePath = utils.cleanString(filePath, this);

            if (ser.getFileType(filePath).equals("File")) {

                //settando la directory tmp non mi devo preoccupare di cancellare il file, se ne occuperà il sistema operativo
                //quando lo ritiene più opportuno
                String tmpFile = "/tmp/" + utils.getFileName(filePath);

                //lo sto trattando come se fosse una copia remota da cluster a client

                if (!(cp_func(ser, filePath, tmpFile, cmd, false))) {
                    utils.error_printer("Errore nella copia del file!");
                    System.err.println("");
                    return false;
                }

                //una volta copiato il file lo posso aprire
                Runtime.getRuntime().exec("xdg-open " + tmpFile);
                return true;
            }
        } else {
            return false;
        }
        utils.error_printer("Errore, stai cercando di aprire una directory!");
        return false;
    }

    /**
     * Metodo il cui dato un path di destinazione in ingresso genera il path finale.
     * NB: il path in ingresso deve essere assoluto oppure '.'  . Si consiglia di pulirlo prima con la funzione
     * utils.cleanString()
     *
     * @param sourcePath path sorgente in ingresso
     * @param destPath path di destinazione in ingresso
     * @param ser      riferimento al serverManager
     * @return path di destinazione modificato
     * @throws RemoteException
     */
    private String genDestPath(String sourcePath, String destPath, ServerManagerInterface ser) throws RemoteException {
        String[] tmp = sourcePath.split("/");
        String lastEl = tmp[tmp.length - 1];
        String name = lastEl.substring(0, lastEl.length());
        if (destPath.equals(".")) {
            //se il secondo parametro è un . devo creare un file sul server con lo stesso nome
            destPath = this.getCurrentPath() + "/" + name;
        } else {
            // tutti gli altri casi, c'è da gestire il caso in cui ho il nome della directory/file oppure non
            //ce l'ho e devo creare una directory con lo stesso nome
            String full_path = destPath + "/" + name;
            if (ser.checkExists(destPath)) {
                destPath = full_path;
            }
        }
        return destPath;

    }


    /**
     * Metodo che gestisce l'help dei vari comandi. Fa uso della classe "Helper" per la gestione del manuale dei vari comandi
     *
     * @param param contiene il nome del comando per cui si vuole chiamare l'help
     * @throws IOException
     * @see Helper
     */
    public void help(String[] param) throws IOException {
        if (param.length == 1) {
            System.out.println("");
            System.out.println("Comandi: ");
            System.out.println("");
            System.out.println("ls: restituisce i file nella directory corrente");
            System.out.println("cd: cambia la directory corrente");
            System.out.println("rm: rimuove file dal filesystem distribuito");
            System.out.println("cp: copia file locali nel filesystem distribuito e viceversa");
            System.out.println("mkdir: crea una directory nel file system distribuito");
            System.out.println("mv: muove file/directory all'interno del file system distribuito");
            System.out.println("du: mostra la disponibilità del cluster");
            System.out.println("sview: mostra i nodi a cui il serverManager è connesso");
            System.out.println("open: apri un file del filesystem distribuito");
            System.out.println("exit: serve per smontare il cluster dal sistema");
            System.out.println();
            System.out.println();
            System.out.println("Digita 'help <command_name>' per ottenere l'aiuto in linea del comando");
            System.out.println();
        } else {
            Helper helper = new Helper();
            helper.print(param[1]);
        }
    }


    /**
     * Main della classe "ClientCLass" gestisce l'input dei vari comandi e va a richiamare i metodi relativi gestiti dalla
     * classe CLientClass
     *
     * @param args argomenti da linea di comando, contiene l'indirizzo ip del serverManager (nodeManager) nel seguente formato
     *             //serverManager_ip/ServerManager
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {
        if (args.length == 1) {
            String serverAdd = args[0];
            try {
                ServerManagerInterface ser = (ServerManagerInterface) Naming.lookup(serverAdd);
                System.out.println(ConsoleColors.GREEN + "Connesso al cluster correttamente!" + ConsoleColors.RESET);
                ClientClass client = new ClientClass(ser);
                boolean exit = false;
                while (!exit) {
                    System.out.flush();
                    System.out.println("Inserisci comando...");
                    System.out.println(ConsoleColors.CYAN + "Path: " + client.getCurrentPath() + ConsoleColors.RESET);
                    System.out.println("");
                    System.out.print('>');
                    Scanner in = new Scanner(System.in);
                    String ins = in.nextLine();


                    if (ins.startsWith("ls")) {

                        if (ParamParser.checkParam(ins, "-l")) {
                            client.ls_func(ser, false, true, false);
                        }
                        else if(ParamParser.checkParam(ins, "-d")){
                                client.ls_func(ser, true, false, false);
                        }
                          //nel caso a un parametro il -h è inutile a causa del comporamento di default

                        else if(ParamParser.checkParam(ins, "-ld"))
                            client.ls_func(ser, true, true, false);

                        else if(ParamParser.checkParam(ins, "-lh"))
                            client.ls_func(ser, false, true, true);

                        //se sbaglio ad inserire un opzione il comando non viene lanciato
                        else if(ParamParser.checkParam(ins, "-ldh"))
                            client.ls_func(ser, true, true, true);

                        else {
                            //comportamento di default senza parametri
                            client.ls_func(ser, false, false, false);
                        }

                    } else if (ins.startsWith("help")) {
                        String[] param = ins.split(" ");
                        client.help(param);
                    } else if (ins.startsWith("cd")) {
                        String[] param = ins.split(" ");
//                    for(int i=0; i<param.length; i++){
//                        System.out.println(param[i]);
//                    }
                        String path = param[1];
                        if (!(client.cd_func(ser, path))) {
                            utils.error_printer("La directory \"" + path + "\" non esiste!");
                        }
                    } else if (ins.startsWith("rm")) {
                        String[] param = ins.split(" ");
                        if (!(param[1].startsWith("-"))) {
                            ArrayList<String> paths = new ArrayList<String>();
                            //recupero i percorsi dei vari file
                            for (int i = 1; i < param.length; i++) {
                                paths.add(param[i]);
                            }

                            if (!(client.rm_func(ser, paths))) {
                                utils.error_printer("Uno dei file \"" + paths + "\" non esiste oppure è una directory!" +
                                        "\n (Per cancellare le directory usa l'opzione -rf )");

                            }
                        } else {
                            if (ParamParser.checkParam(ins, "-rf")) {
                                //NB: funziona con questo nome solo perchè è l'unica opzione, se aggiungi opzioni rinominalo
                                //di lunghezza al massimo 1 l'opzione, ad esempio -r. Poichè le opzioni vengono separate
                                //carattere per carattere e controllati che gli array siano uguali
                                ArrayList<String> paths = new ArrayList<String>();
                                //recupero i percorsi dei vari file

                                int startIndex = ParamParser.getParamNumbers(ins);
                                for (int i = startIndex; i < param.length; i++) {
                                    paths.add(param[i]);
                                }

                                if (!(client.rm_func_rec(ser, paths))) {
                                    utils.error_printer("Errore nella cancellazione");

                                }

                            }

                        }
                    } else if (ins.startsWith("cp")) {

                        String[] param = ins.split(" ");
                        String originalDPath = param[param.length - 1];

                        //caso: client->slave, cp senza opzioni
                        if (!(ins.startsWith("cp -"))) {
                            for (int i = 1; i < param.length - 1; i++) {
                                param[i] = utils.cleanString(param[i], client);

                                param[param.length - 1] = utils.cleanString(param[param.length - 1], client);
                                param[param.length - 1] = client.genDestPath(param[i], param[param.length - 1], ser);
                                // System.out.println("passo i parametri :"+param[1]+" "+ param[2]);
                                if (!(client.cp_func(ser, param[i], param[param.length - 1]))) {
                                    utils.error_printer("Errore nella copia del file!");
                                }
                                System.err.println("");
                                param[param.length - 1] = originalDPath;
                            }
                        }
                        // casi: slave->client oppure client(directory)->server
                        else if ((ParamParser.checkParam(ins,"-m") || ParamParser.checkParam(ins,"-r"))) {

                            int startIndex = ParamParser.getParamNumbers(ins);
                            for (int i = startIndex; i < param.length - 1; i++) {
                                param[i] = utils.cleanString(param[i], client);
                                param[param.length - 1] = utils.cleanString(param[param.length - 1], client);


                                param[param.length - 1] = client.genDestPath(param[i], param[param.length - 1], ser);


                                if (!(client.cp_func(ser, param[i], param[param.length - 1], ins, true))) {
                                    utils.error_printer("Errore nella copia del file!");
                                }
                                System.err.println("");
                                param[param.length - 1] = originalDPath;
                            }

                        }
                        //casi:  slave(directory)->client
                        else if (ParamParser.checkParam(ins,"-rm")) {

                            int startIndex = ParamParser.getParamNumbers(ins);
                            param[startIndex] = utils.cleanString(param[startIndex], client);
                            param[startIndex+1] = utils.cleanString(param[startIndex+1], client);

                            if (!(param[startIndex].startsWith("/"))) {
                                utils.error_printer("Devi specificare un path assoluto sul client!");
                                continue;
                            } else {
//                                String[] tmp =  param[3].split("/");
//                                String lastEl = tmp[tmp.length-1];
//                                String dirName =  lastEl.substring(0, lastEl.length());
//                                param[4] = param[4]+"/"+dirName;
                                param[startIndex+1] = client.genDestPath(param[startIndex], param[startIndex+1], ser);
                            }


                            if (!(client.cp_func(ser, param[startIndex], param[startIndex+1], ins, true))) {
                                utils.error_printer("Errore nella copia del file!");
                            }
                            System.err.println("");

                        } else if (ParamParser.checkParam(ins,"-i")) {
                            //caso solo cp -i


                            int startIndex = ParamParser.getParamNumbers(ins);
                            for (int i = startIndex; i < param.length - 1; i++) {
                                param[i] = utils.cleanString(param[i], client);
                                param[param.length - 1] = utils.cleanString(param[param.length - 1], client);
                                param[param.length - 1] = client.genDestPath(param[i], param[param.length - 1], ser);


                                // if (!(client.cp_func(ser, param[i], param[param.length-1], true))) {
                                if (!(ser.cp_func(param[i], param[param.length - 1]))) {
                                    utils.error_printer("Errore nella copia del file!");
                                }
                                System.err.println("");
                                param[param.length - 1] = originalDPath;
                            }


                        } else if (ParamParser.checkParam(ins,"-ri")) {
                            //caso cp -i -r o cp -r -i
                            param[param.length-1] = utils.cleanString(param[param.length-1], client);
                            System.out.println("Dest pathj: "+ param[param.length-1]);


                            int startIndex = ParamParser.getParamNumbers(ins);
                            for (int i = startIndex; i < param.length - 1; i++) {
                                param[i] = utils.cleanString(param[i], client);
                                param[param.length - 1] = utils.cleanString(param[param.length - 1], client);
                                param[param.length - 1] = client.genDestPath(param[i], param[param.length - 1], ser);
                                System.out.println("Dest path2: "+ param[param.length-1]);

                                System.out.println("Inizio la copia ricorsiva di " + param[i]);
                                ser.recursiveCopyInt(param[i], param[param.length - 1]);


                                param[param.length - 1] = originalDPath;
                            }


                        } else {
                            utils.error_printer("Errore nella sintassi del comando! Digita 'help cp' per vedere la sintassi del comando");
                        }

                    } else if (ins.startsWith("mkdir")) {
                        String[] param = ins.split(" ");

                        if (ser.mkdir(param, client.getCurrentPath())) {
                            System.out.println("Directory creata/e con successo");
                        }

                    } else if (ins.startsWith("mv")) {
                        String[] param = ins.split(" ");
                        param[1] = utils.cleanString(param[1], client);
                        param[2] = utils.cleanString(param[2], client);
                        String loc1 = ser.getFileLocation(param[1]);
                        String loc2 = ser.getFileLocation(param[2]);
                        boolean exists = true;
                        if (loc1 == null) {
                            utils.error_printer("Il file/directory " + param[1] + " non esiste! ");
                            System.err.println("");
                            exists = false;
                        }
                        if (loc2 == null) {
                            utils.error_printer("la directory " + param[2] + " non esiste! ");
                            System.err.println("");
                            exists = false;
                        }
                        if (exists)
                            ser.move(param[1], param[2], loc1);

                    }

                    else if (ins.startsWith("du")) {
                        client.du_func(ins, ser);
                    }

                    else if (ins.startsWith("sview")) {
                        client.sview_func(ins, ser);

                    }

                    else if (ins.startsWith("open")) {
                        String[] param = ins.split(" ");
                        if (!(client.open(param, ser, ins))) {
                            utils.error_printer("Errore nell'apertura del file \n");
                        }

                    }

                    else if (ins.equals("exit")) {
                        exit = true;
                    }

                    else {
                        utils.error_printer("comando digitato non esiste!");
                    }
                }

                if (client.getFileClient() != null)
                    client.getFileClient().closeConnection();


            } catch (RemoteException e) {
                e.printStackTrace();

            } catch (NotBoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println(ConsoleColors.RED + "Formato del comando non valido!" + ConsoleColors.RESET);
        }

    }


}
