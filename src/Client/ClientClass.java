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



/**
* Classe client principale. Il client mantiene lo stato, in questo modo il server risulta essere più semplice e non ha
* bisogno di manternere le informazioni per i vari client. Grazie a questo approccio si va a favorire la scalabilità del
* sistema e maggiore efficienza.
 */
public class ClientClass implements Serializable{
    private String currentPath = "";
    private int port = 6668;
    private int port2 = 6669;
    private FileClient fc = null ;
    private FileServerThread thread=null;

    /**
    * Costruttore per la classe client
    * @param ser nodeManager del cluster
     */
    public ClientClass(ServerManagerInterface ser) throws RemoteException {
        super();
        currentPath = ser.getSharedDir();
    }

    /**
    * Ritorna il path corrente in cui si trova il client all'interno del cluster
    * @return currentPath, variabile che mantiene il path del client
     */
    public String getCurrentPath() {
        return currentPath;
    }

    /**
     * Ritorna il riferimento al client del file transfer system
     * @return fc, riferimento al file client tansfer
     */
    public FileClient getFileClient(){return fc;}

    /**
     * Metodo che restituisce l'ip del client, in questo modo si agevola l'interazione con l'utente.
     * L'interfaccia presa in considerazione di default è la 0
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
     * @param ser riferimento al nodeManager
     * @param dirCapacity flag per indicare se si vuole anche calcolare la capacità delle directory o meno
     * @throws RemoteException
     */
    public void ls_func(ServerManagerInterface ser, boolean dirCapacity) throws RemoteException {
        ArrayList<MyFileType> res = ser.ls_func(currentPath, dirCapacity);
        System.out.println("");
        for(MyFileType file: res ){
            if(file.getType().equals("File"))
                System.out.println(file.getName()+"     | Type: "+ file.getType() + "     | Size (bytes): "+ file.getSize()+ " | location: "+ file.getLocation());
            else
                System.out.println(ConsoleColors.GREEN +file.getName()+ ConsoleColors.RESET+"     | Type: "+ file.getType() + "     | Size (bytes): "+ file.getSize()+ " | location: - ");
        }
        System.out.println();
    }

    /**
     * Metodo per la creazione di una directory all'interno del cluster. La gestione del file system all'interno del cluster
     * viene spiegata nella documentazione della classe "ServerManager"
     * @see Server.ServerManager
     * @param path percorso alla nuova directory
     * @return true se la directory è creata con successo all'interno del cluster, false altrimenti.
     * @throws RemoteException
     */
    public boolean mkdir(String path) throws RemoteException{
        File f = new File(path);
        if(f.mkdir()) {
            System.out.println("Directory creata "+ f.getName());
            return true;}
        return  false;
    }


    /**
     * Metodo per spostarsi all'interno del filesystem del cluster. È ispirato al comando "cd" di Linux.
     * @param ser riferimento al nodeManger del cluster
     * @param path percorso alla cartella in cui ci si vuole spostare
     * @return true se il comando è stato eseguito con successo, false altrimenti
     * @throws RemoteException
     */
    public boolean cd_func(ServerManagerInterface ser, String path) throws RemoteException {
        if (path.equals("..") && !(currentPath.equals(ser.getSharedDir()))){
            String dirs[] = currentPath.split("/");
            currentPath = "";
            for (int i=0; i<dirs.length-1; i++){
                if (i<dirs.length-2) {
                    currentPath += dirs[i] + '/';
                }else   //ultima iterazione
                    currentPath += dirs[i];
            }
            return true;
        } else {
            path = utils.cleanString(path, this);
            System.out.println(path);
            boolean exists = ser.checkExists(path);
            if (exists) {
                currentPath = path;
                return true;
            }
            return false;
        }
    }

    /**
     * Metodo per la cancellazione di un file all'interno del cluster
     * @param ser riferimento al nodeManager del cluster
     * @param paths percorsi ai file da cancellare
     * @return true se la cancellazione  dei file è stata eseguita con successo, false altrimenti
     * @throws RemoteException
     */
    public boolean rm_func(ServerManagerInterface ser, ArrayList<String> paths) throws RemoteException{
        for(String path: paths){
            //se fallisce la cancellazione
            if(!(ser.rm_func(currentPath+'/'+path))){
                System.out.println("Fallita l'eliminazione di "+ path);
                return false;
            }
            System.out.println("File "+path+" eliminato con successo!");
        }

        return true;
    }

    /**
     * Metodo per la cancellazione ricorsiva delle directory all'interno del fle system distribuito
     * @param ser riferimento al nodeManager del cluster
     * @param paths percorso alla directory da eliminare
     * @return true se l'eliminazione della directory avviene con successo, false altrimenti.
     * @throws RemoteException
     */
    public boolean rm_func_rec(ServerManagerInterface ser, ArrayList<String> paths) throws RemoteException{
        for(String path: paths){
            //se fallisce la cancellazione
            if(!(ser.rm_func_rec(currentPath+'/'+path))){
                System.err.println("Fallita l'eliminazione di "+ path);
                return false;
            }
            System.out.println(""+path+" eliminato con successo!");
        }

        return true;
    }

    /**
     * Metodo per la copia di un file da file system locale al cluster
     * @param ser riferimento al nodeManager del cluster
     * @param localPath percorso al file locale all'interno del sistema
     * @param remotePath percorso in cui verrà copiato il file, si può specificare anche il nome. Es. ./dir1/fileName
     * @return true se la copia avviene con successo
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean cp_func(ServerManagerInterface ser, String localPath, String remotePath) throws IOException, InterruptedException {
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
        fc.send(localPath);
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
     * @param ser riferimento al nodeManager del cluster
     * @param path1 percorso al file/directory sorgente
     * @param path2 percorso di destinazione
     * @param options opzioni per differenziare i vari casi
     * @return true se la copia avviene con successo, false altrimenti
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean cp_func(ServerManagerInterface ser, String path1, String path2, ArrayList<String> options) throws IOException, InterruptedException {
        String[] optionsArr = new String[options.size()];
        optionsArr = options.toArray(optionsArr);


        if (utils.contains(optionsArr, "-rm") && utils.contains(optionsArr, "-r")){

            //String location = ser.getFileLocation(path1);
            //if(location != null) {
                //ServerInterface slave = ser.getSlaveNode(location);
            if(ser.checkExists(path1)) {
                System.out.println("Inizio copia ricorsiva ");
                //il client in questo caso diventa il server ricevitore di file (FileServer) e il server diventa il FileClient
                if (thread == null) {
                    thread = new FileServerThread(port2, path2);
                    thread.start();
                }
                thread.setPath(path2);
                //recursiveCopy_remote(path1, path2, slave);
                recursiveCopy_remote(path1, path2, ser);
            }else {
                System.err.println(ConsoleColors.RED+"la directory \""+ utils.getFileName(path1)+ "\" non esiste!"+ConsoleColors.RESET);
            }
            //}

        }
        else if(utils.contains(optionsArr, "-r")){

            File f = new File(path1);
            if(f.exists()){

                //System.out.println("Prova creazione directory su slave");
                //slave.mkdir("/home/enrico404/shDir/dirCopy");
                System.out.println("Inizio la copia ricorsiva di "+f.getName());
                recursiveCopy(f, ser, path1, path2);

            }
        }
        else if (utils.contains(optionsArr, "-rm")){
            // path1 è il path del file remoto e path2 è il path del file in locale
            String location = ser.getFileLocation(path1);
            if(location != null){
                ServerInterface slave = ser.getSlaveNode(location);
                //il client in questo caso diventa il server ricevitore di file (FileServer) e il server diventa il FileClient
                if (thread == null) {
                    thread = new FileServerThread(port2, path2);
                    thread.start();
                }
                thread.setPath(path2);

                slave.startFileClient(port2, getIp(), path1);


            }else {
                System.err.println(ConsoleColors.RED+"Il file \""+utils.getFileName(path1)+"\" non esiste"+ConsoleColors.RESET);
                return false;
            }

        }
        return true;
    }

    /**
     * Metodo per la copia ricorsiva di directory da locale a remoto, viene utilizzato dalla funzione "cp_func"
     * @param f riferimento al file/directory da copiare
     * @param ser riferimento al nodeManger del cluster
     * @param localPath path sorgente del file i-esimo da copiare, inizialmente contiene il percorso alla directory
     * @param remotePath path di destinazione del file i-esimo copiato
     * @throws IOException
     * @throws InterruptedException
     */
    public void recursiveCopy(File f ,ServerManagerInterface ser, String localPath, String remotePath) throws IOException, InterruptedException {
        if (f.isDirectory()){
//            int slaveIndex = ser.freerNodeChooser();
//            ServerInterface slave = ser.getSlaveNode(slaveIndex);
            for (ServerInterface slave: ser.getSlaveServers()){
                slave.mkdir(remotePath);
            }
            for(File sub: f.listFiles()){
                String localPathNew = localPath+'/'+sub.getName();
                String remotePathNew = remotePath+'/'+sub.getName();
                recursiveCopy(sub, ser, localPathNew, remotePathNew);
            }
        }else {

            cp_func(ser, localPath, remotePath);
        }

    }

    /**
     * Metodo per la copia ricorsiva di directory da remoto a locale, viene utilizzato dalla funzione "cp_func"
     * @param remotePath path della directory da copiare, viene poi aggiornato iterazione per iterazione
     *                   con i path ai vari file
     * @param clientPath path del file i-esimo di destinazione sul client
     * @param sm riferimento al nodeManager del cluster
     * @throws IOException
     */
    public void recursiveCopy_remote(String remotePath, String clientPath, ServerManagerInterface sm) throws IOException {
            //System.out.println("RemotePath: "+ remotePath);
            //System.out.println("Client path: "+clientPath);
            for(ServerInterface slave: sm.getSlaveServers()) {
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
                    if(slave.checkExists(remotePath))
                        slave.startFileClient(port2, getIp(), remotePath);
                }

            }

    }


    /**
     * Metodo per la stampa su standard output i nodi a cui il serverManager è attualmente connesso
     * @param ser Riferimento al serverManager
     * @throws RemoteException
     */
    public void sview_func(ServerManagerInterface ser) throws RemoteException, SocketException {
        System.out.println("Sono connesso con i seguenti data nodes: ");
        System.out.println("");
        for(ServerInterface slave: ser.getSlaveServers()){
            System.out.println("Name: "+slave.getName()+ " |  ip: "+slave.getIp());
            System.out.println(ConsoleColors.CYAN+"Spazio disponibile: "+slave.getFreeSpace()+ConsoleColors.RESET);
        }
        System.out.println("");
    }


    /**
     * Metodo che stampa su standard output informazioni utili riguardo il cluster, come ad esempio la capacità e lo spazio
     * disponibile
     * @param param parametri opzionali in ingresso
     * @param ser riferimento al serverManager
     * @throws RemoteException
     */
    public void du_func(String [] param, ServerManagerInterface ser) throws RemoteException {
        if(param.length == 1){
            System.out.println("");
            System.out.println(ConsoleColors.CYAN+"Capacità disponibile del cluster: " + ser.getFreeSpace());
            System.out.println("Capacità massima del cluster: " + ser.getClusterCapacity());
            System.out.println("");
            float perc = (ser.getFreeSpace()*100)/(float)ser.getClusterCapacity();
            System.out.println("Spazio disponibile in percentuale: "+ perc+"%"+ConsoleColors.RESET);
            System.out.println("");
        }
        else if(param.length == 2) {
            if (param[1].equals("-h")) {
                float gb_divisor = 1024*1024*1024;
                float freeSpace_h = ser.getFreeSpace() / gb_divisor;
                float clusterSpace_h = ser.getClusterCapacity() / gb_divisor;
                System.out.println("");
                System.out.println(ConsoleColors.CYAN+"Capacità disponibile del cluster: " + freeSpace_h + " GB");
                System.out.println("Capacità massima del cluster: " + clusterSpace_h + " GB");
                System.out.println("");
                float perc = (freeSpace_h*100)/clusterSpace_h;
                System.out.println("Spazio disponibile in percentuale: "+ perc+"%"+ConsoleColors.RESET);
                System.out.println("");
            }
        }
    }

    /**
     * Metodo che gestisce l'help dei vari comandi. Fa uso della classe "Helper" per la gestione del manuale dei vari comandi
     * @param param contiene il nome del comando per cui si vuole chiamare l'help
     * @throws IOException
     * @see Helper
     */
    public void help(String[] param) throws IOException {
        if(param.length==1) {
            System.out.println("");
            System.out.println("Comandi: ");
            System.out.println("");
            System.out.println("ls: restituisce i file nella directory corrente");
            System.out.println("cd: cambia la directory corrente");
            System.out.println("rm: rimuove un file dal filesystem");
            System.out.println("cp: copia un file locale nel filesystem distribuito");
            System.out.println("mkdir: crea una directory nel file system distribuito");
            System.out.println("mv: muove file/directory all'interno del file system distribuito");
            System.out.println("du: mostra la disponibilità del cluster");
            System.out.println("sview: mostra i nodi a cui il serverManager è connesso");
            System.out.println("exit: serve per smontare il cluster dal sistema");
            System.out.println();
        }else {
            Helper helper = new Helper();
            helper.print(param[1]);
        }
    }





    /**
     * Main della classe "ClientCLass" gestisce l'input dei vari comandi e va a richiamare i metodi relativi gestiti dalla
     * classe CLientClass
     * @param args argomenti da linea di comando, contiene l'indirizzo ip del serverManager (nodeManager) nel seguente formato
     *             //serverManager_ip/ServerManager
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {
        if (args.length == 1) {
            String serverAdd = args[0];

            try {
                ServerManagerInterface ser = (ServerManagerInterface) Naming.lookup(serverAdd);
                System.out.println(ConsoleColors.GREEN+"Connesso al cluster correttamente!"+ConsoleColors.RESET);
                ClientClass client = new ClientClass(ser);
                boolean exit = false;
                while (!exit) {
                    System.out.println("Inserisci comando...");
                    System.out.println(ConsoleColors.CYAN+"Path: " + client.getCurrentPath()+ConsoleColors.RESET);
                    System.out.println("");
                    System.out.print('>');
                    Scanner in = new Scanner(System.in);
                    String ins = in.nextLine();


                    if (ins.startsWith("ls")) {
                        String[] param = ins.split(" ");
                        if(param.length == 2){
                            if(param[1].equals("-a")){
                                client.ls_func(ser, true);
                            }
                        }
                        else {
                            client.ls_func(ser, false);
                        }
                    }


                    else if (ins.startsWith("help")) {
                        String[] param = ins.split(" ");
                        client.help(param);
                    }


                    else if (ins.startsWith("cd")) {
                        String[] param = ins.split(" ");
//                    for(int i=0; i<param.length; i++){
//                        System.out.println(param[i]);
//                    }
                        String path = param[1];
                        if (!(client.cd_func(ser, path))) {
                            System.err.println(ConsoleColors.RED+"La directory \"" + path + "\" non esiste!"+ConsoleColors.RESET);
                        }
                    }


                    else if (ins.startsWith("rm")){
                        String[] param = ins.split(" ");
                        if (!(param[1].startsWith("-"))) {
                            ArrayList<String> paths = new ArrayList<String>();
                            //recupero i percorsi dei vari file
                            for (int i = 1; i < param.length; i++) {
                                paths.add(param[i]);
                            }

                            if (!(client.rm_func(ser, paths))) {
                                System.err.println(ConsoleColors.RED+"Uno dei file \"" + paths + "\" non esiste oppure è una directory!"+ConsoleColors.RESET);
                                System.err.println("(Per cancellare le directory usa l'opzione -rf )");
                            }
                        }else {
                            if (utils.contains(param, "-rf", 1)){
                                ArrayList<String> paths = new ArrayList<String>();
                                //recupero i percorsi dei vari file
                                for (int i = 2; i < param.length; i++) {
                                    paths.add(param[i]);
                                }

                                if (!(client.rm_func_rec(ser, paths))) {
                                    System.err.println(ConsoleColors.RED+"Errore nella cancellazione"+ConsoleColors.RESET);

                                }

                            }

                        }
                    }


                    else if (ins.startsWith("cp")){
                        String[] param = ins.split(" ");

                        //caso: client->slave
                        if(param.length == 3) {
                            param[1] = utils.cleanString(param[1], client);
                            param[2] = utils.cleanString(param[2], client);

                           if(param[2].equals(".")){
                               //se il secondo parametro è un . devo creare un file sul server con lo stesso nome
                               String[] tmp =  param[1].split("/");
                               String lastEl = tmp[tmp.length-1];
                               String fileName =  lastEl.substring(0, lastEl.length());
                               param[2] = client.getCurrentPath()+"/"+fileName;
                           }else{
                               // caso in cui il secondo parametro è il path assoluto alla cartella
                               String[] tmp =  param[1].split("/");
                               String lastEl = tmp[tmp.length-1];
                               String fileName =  lastEl.substring(0, lastEl.length());
                               param[2] = param[2]+"/"+fileName;
                           }
                            if (!(client.cp_func(ser, param[1], param[2]))) {
                                System.err.println(ConsoleColors.RED+"Errore nella copia del file!"+ConsoleColors.RESET);
                            }
                            System.err.println("");

                        }
                        // casi: slave->client oppure client(directory)->server
                        else if(param.length == 4){
                            param[2] = utils.cleanString(param[2], client);
                            param[3] = utils.cleanString(param[3], client);

                            if(param[3].equals(".")){
                                String[] tmp =  param[2].split("/");
                                String lastEl = tmp[tmp.length-1];
                                String dirName =  lastEl.substring(0, lastEl.length());
                                param[3] = client.getCurrentPath()+"/"+dirName;
                            }else {
                                String[] tmp =  param[2].split("/");
                                String lastEl = tmp[tmp.length-1];
                                String dirName =  lastEl.substring(0, lastEl.length());
                                param[3] = param[3]+"/"+dirName;
                            }



                            System.out.println("Local path1:"+param[2]);
                            System.out.println("Remote path2:"+param[3]);

                            if (utils.contains(param, "-rm", 1) || utils.contains(param, "-r", 1) ){
                                ArrayList<String> options = new ArrayList<String>();
                                options.add(param[1]);
                                if (!(client.cp_func(ser, param[2], param[3], options))) {
                                    System.err.println(ConsoleColors.RED+"Errore nella copia del file!"+ConsoleColors.RESET);
                                }
                                System.err.println("");
                            }

                        }
                        //casi:  slave(directory)->client
                        else if (param.length == 5){
                            param[3] = utils.cleanString(param[3], client);
                            param[4] = utils.cleanString(param[4], client);

                            if(!(param[4].startsWith("/"))){
                                System.err.println(ConsoleColors.RED+"Devi specificare un path assoluto sul client!"+ConsoleColors.RESET);
                                continue;
                            }else {
                                String[] tmp =  param[3].split("/");
                                String lastEl = tmp[tmp.length-1];
                                String dirName =  lastEl.substring(0, lastEl.length());
                                param[4] = param[4]+"/"+dirName;
                            }
                            System.out.println("remote path1:"+param[3]);
                            System.out.println("client path2:"+param[4]);


                            if ((utils.contains(param, "-rm", 1) && utils.contains(param, "-r", 2)) || (utils.contains(param, "-rm", 2) && utils.contains(param, "-r", 1)) ){
                                ArrayList<String> options = new ArrayList<String>();
                                options.add(param[1]);
                                options.add(param[2]);
                                if (!(client.cp_func(ser, param[3], param[4], options))) {
                                    System.err.println(ConsoleColors.RED+"Errore nella copia del file!"+ConsoleColors.RESET);
                                }
                                System.err.println("");
                            }
                        }
                        else {
                            System.err.println(ConsoleColors.RED+"Errore nella sintassi del comando! Digita 'help cp' per vedere la sintassi del comando"+ConsoleColors.RESET);
                        }

                    }



                    else if (ins.startsWith("mkdir")){
                        String[] param = ins.split(" ");

                        if(ser.mkdir(param, client.getCurrentPath())) {
                            System.out.println("Directory creata/e con successo");
                        }

                    }



                    else if (ins.startsWith("mv")){
                        String[] param = ins.split(" ");
                        param[1] = utils.cleanString(param[1], client);
                        param[2] = utils.cleanString(param[2], client);
                        String loc1 = ser.getFileLocation(param[1]);
                        String loc2 = ser.getFileLocation(param[2]);
                        boolean exists = true;
                        if (loc1 == null){
                            System.err.println(ConsoleColors.RED+"Il file/directory "+param[1]+ " non esiste! "+ConsoleColors.RESET);
                            System.err.println("");
                            exists = false;
                        }
                        if (loc2 == null){
                            System.err.println(ConsoleColors.RED+"la directory "+param[2]+ " non esiste! "+ConsoleColors.RESET);
                            System.err.println("");
                            exists = false;
                        }
                        if(exists)
                            ser.move(param[1],param[2],loc1);

                    }



                    else if (ins.startsWith("du")){
                        String[] param = ins.split(" ");
                        client.du_func(param, ser);
                    }

                    else if(ins.equals("sview")){
                        client.sview_func(ser);

                    }

                    else if (ins.equals("exit")) {
                        exit = true;
                    }
                    else {
                        System.err.println(ConsoleColors.RED+"Il comando digitato non esiste!"+ConsoleColors.RESET);
                    }
                }

                if(client.getFileClient() != null)
                    client.getFileClient().closeConnection();


            } catch (RemoteException e) {
                e.printStackTrace();

            } catch (NotBoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException | InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            System.err.println(ConsoleColors.RED+"Formato del comando non valido!"+ConsoleColors.RESET);
        }

    }


}
