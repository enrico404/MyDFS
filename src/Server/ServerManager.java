package Server;

import utils.MyFileType;
import utils.utils;

import java.io.*;
import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import utils.FileClient;
import Client.ParamParser;
import utils.FileServerThread;


/**
 * L'architettura del sistema è ispirata a quella di Hadoop nella sua prima versione, la più semplice.
 * L'architettura è divisa in due livelli:
 * <ul>
 *     <li>
 *         Client - ServerManager
 *     </li>
 *     <li>
 *         ServerManager - ServerClass
 *     </li>
 * </ul>
 * Il ServerManager prende anche il nome di "Node Master", il suo compito è quello di interagire direttamente con il client
 * e gestire la distribuzione del carico di lavoro che viene affidato ai vari Data Node (ServerClass). Svolge quindi un lavoro
 * da controllore. L'intera architettura è basata sulla tipologia Master - Slave. Il Node Manger svolge quindi la funzione di
 * Master e i vari Data Nodes svolgono la funzione di Slaves.
 * <p>
 * Gli slave nodes (ServerClass) si occupano invece di tenere memorizzati i dati. Gli slave nodes condividono tutti la stessa
 * struttura di directory, in questo modo è possibile bilanciare equamente lo spazio allocato su ogni slave.
 * <p>
 * Il serverManager è in grado di gestire un numero corposo di client allo stesso tempo e un numero di slave illimitato. Più slave
 * saranno presenti nel cluster, più sarà lo spazio a disposizion disponibile. La scalabilità orrizzontale del sistema è quindi una proprietà
 * di questa particolare tipologia di architettura.
 */

public class ServerManager extends UnicastRemoteObject implements ServerManagerInterface {
    /**
     * Attributo contenente il nome del server
     */
    private String name = "";
    /**
     * array degli ip dei nodi slave
     */
    private ArrayList<String> ipArray;
    /**
     * Percorso della directory condivisa con gli altri server
     */
    private String sharedDir = "";
    /**
     * Array contenente tutti i riferimenti ai nodi slave che gestisce
     */
    private ArrayList<ServerInterface> slaveServers = new ArrayList<ServerInterface>();

    /**
     * Array che contiene dati (nome e ip) degli slaves a cui il serverManager è connesso
     */
    private ArrayList<SlaveServerCache> slaveServerCaches = new ArrayList<>();

    /**
     * porta utilizzata per il trasferimento di file interni al cluster
     */
    private int port = 6770;

    /**
     * struttura dati contenente l'albero delle directory che devono avere tutti i data nodes
     */
    private Tree fileSystemTree = null;

    private String primarySerIp = "";

    private String secondarySerIp = "";

    public ServerManagerInterface backupServer;


    /**
     * Costruttore con parametri del nodeManager
     *
     * @param Name    nome del nodeManger
     * @param IpArray lista di indirizzi ip dei nodi slave
     * @throws RemoteException
     */
    public ServerManager(String Name, ArrayList<String> IpArray) throws RemoteException {
        super();
        name = Name;
        ipArray = IpArray;


        FileInputStream f = null;
        ObjectInputStream in = null;
        try {
            f = new FileInputStream(System.getProperty("user.home") + "/.config/MyDFS/fileSystemTree");
            in = new ObjectInputStream(f);
            fileSystemTree = (Tree) in.readObject();


        } catch (EOFException |FileNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (f != null) {
                try {
                    f.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (fileSystemTree == null) {
                Tree.Node root = new Tree.Node("/", "root");
                fileSystemTree = new Tree(root);
                fileSystemTree.init();
            }

        }


    }

    public ServerManagerInterface getBackupServer() {
        return backupServer;
    }

    public void setBackupServer(ServerManagerInterface backupServer) {
        this.backupServer = backupServer;
    }

    public Tree getFileSystemTree() throws RemoteException{
        return fileSystemTree;
    }

    public void setFileSystemTree(Tree fileSystemTree) throws RemoteException{
        this.fileSystemTree = fileSystemTree;
    }

    public String getPrimarySerIp() throws RemoteException{
        return primarySerIp;
    }

    public void setPrimarySerIp(String primarySerIp) throws RemoteException{
        this.primarySerIp = primarySerIp;
    }

    public String getSecondarySerIp() throws RemoteException{
        return secondarySerIp;
    }

    public void setSecondarySerIp(String secondarySerIp) throws RemoteException {
        this.secondarySerIp = secondarySerIp;
    }

    public void reloadFileSystemTree() throws RemoteException{
        FileInputStream f = null;
        ObjectInputStream in = null;
        try {
            f = new FileInputStream(System.getProperty("user.home") + "/.config/MyDFS/fileSystemTree");
            in = new ObjectInputStream(f);
            fileSystemTree = (Tree) in.readObject();
//            System.out.println("File system tree ricaricato");
//            fileSystemTree.trasverseTree();

        } catch (EOFException |FileNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Getter del nome del serverManager
     *
     * @return
     */
    @Override
    public String getName() throws RemoteException {
        return name;
    }

    /**
     * Setter del nome del serverManager
     *
     * @param name
     */
    @Override
    public void setName(String name) throws RemoteException {
        this.name = name;
    }

    /**
     * Getter dell'array slaveServerCaches
     *
     * @return array contenente il nome degli slave servers a cui il serverManager è connesso
     */
    @Override
    public ArrayList<SlaveServerCache> getSlaveServerCaches() throws RemoteException {
        return slaveServerCaches;
    }


    /**
     * Funzione per il check della consistenza delle directory tra i vari data nodes, viene eseguita subito dopo la connessione
     * con i data nodes
     *
     * @return true se si è riusciti a rendere consistenti tra di loro tutti i nodi
     */
    @Override
    public boolean consistency_check() throws IOException {

        for (ServerInterface slave : slaveServers) {
            if (slave.getFileSystemTree() != null) {
                // System.out.println("Devo correggere: "+ !fileSystemTree.checkTree(slave.getFileSystemTree()));

//                System.out.println("slave: "+ slave.getName());
//                System.out.println(slave.getFileSystemTree().getDirs());

//                System.out.println("Slave:");
//                System.out.println(slave.getFileSystemTree().getDirs());
//                System.out.println("ServerManager:");
//                System.out.println(fileSystemTree.getDirs());
//                System.out.println(fileSystemTree.getDirs().size());
               // System.out.println("");
                if (!fileSystemTree.checkTree(slave.getFileSystemTree())) {
                    System.out.println("Il server: " + slave.getName() + " non è stato rilevato consistente");
                    System.out.println(slave.getFileSystemTree().getDirs());
                    System.out.println("Correzione in corso...");

                    while (slave.correct(fileSystemTree)) {
                        System.out.println("Correzzione effettuata");
                    }
                    System.out.println("La correzione del nodo è stata completata con successo!");
                    System.out.println(slave.getFileSystemTree().getDirs());
                }
            }
        }
        return true;
    }


    /**
     * Funzione interna per la connessione del ServerManager ai vari nodi slave. Chiamato solo in fase di accensione del
     * ServerManager
     *
     * @throws RemoteException
     * @throws NotBoundException
     * @throws MalformedURLException
     */
    private boolean connectToDataServers() throws IOException, NotBoundException {

        int i = 0;
        for (String ip : ipArray) {
            try {
                ServerInterface ser = (ServerInterface) Naming.lookup(ip);
                slaveServers.add(ser);
                //serve per vedere se effettivamente ho ottenuto una connessione all'oggetto funzionante
                ser.getName();

                SlaveServerCache cache = new SlaveServerCache(ser.getName(), ip);
                slaveServerCaches.add(cache);
                i++;
            } catch (ConnectIOException e) {
                utils.error_printer("È stato rilevato un guasto nel server: " + ip);
                System.out.println(slaveServers);
                slaveServers.remove(i);


            } catch (ConnectException e) {
                utils.error_printer("È stato rilevato un guasto nel server: " + ip);
                slaveServers.remove(i);
            }

        }

        reloadFileSystemTree();

        try {

            getBackupServer().reloadFileSystemTree();
            System.out.println("Secondario:");
            System.out.println(getBackupServer().getFileSystemTree().getDirs());
            System.out.println("Primario");
            System.out.println(fileSystemTree.getDirs());
            //se sono diversi il file system tree secondario diventa il primario
            if(!fileSystemTree.checkTree(getBackupServer().getFileSystemTree())){
                fileSystemTree = getBackupServer().getFileSystemTree();
                fileSystemTree.updateDir();
                saveFileSystemTree();


            }
        }catch (Exception e){}

        if (fileSystemTree != null) {
            System.out.println("Il file system tree è il seguente: ");
            fileSystemTree.trasverseTree();
        }
        System.out.println("file system tree dirs:");
        System.out.println(fileSystemTree.getDirs());
        System.out.println("Slave servers a cui sono connesso:");
        for (ServerInterface slave : slaveServers) {
            System.out.println(slave.getName());
        }
        if (!consistency_check()) {
            return false;
        }

        return true;
    }

    /**
     * Metodo per il checking dello stato degli slave nodes, se uno slave node crasha, questo viene rimosso dalla lista
     * di servers a cui il ServerManager è connesso e riportato all'utente.
     *
     * @throws RemoteException
     */
    @Override
    public void asyncServersChecking() throws RemoteException {
        AsyncServersChecker threadChecker = new AsyncServersChecker(this);
        threadChecker.start();
    }


    /**
     * Metodo che ritorna l'indice del nodo slave con più spazio libero sul disco. Meccanismo di loadBalancing greedy.
     *
     * @return indice del nodo slave più libero
     */
    public int freerNodeChooser() throws RemoteException {
        long maxSpace = slaveServers.get(0).getFreeSpace();
        int indexMax = 0;
        if (slaveServers.size() > 1) {
            for (int i = 1; i < slaveServers.size(); i++) {
                if (maxSpace < slaveServers.get(i).getFreeSpace()) {
                    maxSpace = slaveServers.get(i).getFreeSpace();
                    indexMax = i;
                }

            }
        }
        return indexMax;
    }

    @Override
    public String getFileType(String path) throws RemoteException {
        String loc = getFileLocation(path);
        ServerInterface slave = getSlaveNode(loc);
        if (slave.isDirectory(path)) {
            return "Dir";
        } else
            return "File";
    }

    /**
     * Metodo il quale dato in input l'indice, restituisce il riferimento al nodo slave appartentende all'array slaveServers
     *
     * @param index indice del nodo slave
     * @return riferimento al nodo slave
     */
    public ServerInterface getSlaveNode(int index) {
        return slaveServers.get(index);
    }

    /**
     * Meotodo il quale dato in input il nome del nodo slave, restituisce un riferimento al data node con tale nome
     *
     * @param name nome del nodo da cercare
     * @return riferimento al nodo se esiste nell'array dei nodi slave istanziati, null altrimenti
     * @throws RemoteException
     */
    public ServerInterface getSlaveNode(String name) throws RemoteException {
        for (ServerInterface slave : slaveServers) {
            if (slave.getName().equals(name)) return slave;
        }
        return null;
    }

    /**
     * Metodo get per l'attributo slaveServers
     *
     * @return riferimento all'array slaveServer
     */
    public ArrayList<ServerInterface> getSlaveServers() {
        return slaveServers;
    }

    /**
     * Metodo che ritorna l'ip di questo nodo in formato ipV4
     *
     * @return stringa contenente l'indirizzo ip in formato ipV4
     * @throws RemoteException
     * @throws SocketException
     */
    @Override
    public String getIp() throws RemoteException, SocketException {
        String ip = utils.getInet4Addresses().get(0).toString().substring(1);
        return ip;
    }

    /**
     * Metodo interno per verificare se data una lista di file questa contiene un file con il nome specificato nel secondo parametro
     *
     * @param lista lista di file
     * @param name  nome che si sta cercando
     * @return true se esiste all'interno della lista un file con quel nome, false altrimenti
     */
    private boolean contains(ArrayList<MyFileType> lista, String name) {
        for (MyFileType el : lista) {
            if (el.getName().equals(name)) return true;
        }
        return false;
    }

    /**
     * Metodo ls relativo al ServerManager, viene chiamato dal metodo ls che risiede sul client. IN questa particolare
     * versione la capacità delle directory non viene calcolata per motivi di performance del comando
     *
     * @param path percorso su cui si sta eseguendo il comando
     * @return lista di file che stanno su tutti gli slave in quel determinato percorso
     * @throws RemoteException
     */
    @Override
    public ArrayList<MyFileType> ls_func(String path) throws RemoteException {
        String realPath;
        ArrayList<MyFileType> totFiles = new ArrayList<MyFileType>();
        for (ServerInterface slave : slaveServers) {
            //se esiste la cartella nello slave devo scrivere i suoi file
            realPath = slave.getSharedDir() + path;
            if (slave.checkExists(realPath)) {
                //System.out.println("Scrivo file dello slave: "+ slave.getName());
                ArrayList<MyFileType> tmpList = new ArrayList<MyFileType>();
                tmpList = slave.ls_func(realPath, false);
                for (MyFileType f : tmpList) {
                    if (!(contains(totFiles, f.getName())))
                        totFiles.add(f);

                }
            }
        }
        return totFiles;
    }

    /**
     * Overloading del metodo ls precedente, in questa versione si va a chiedere anche la capacità delle directory
     *
     * @param path        percorso in cui voglio eseguire il comando
     * @param dirCapacity variabile booleana che indica se voglio o meno la capacità
     * @return lista di file/directory presenti in quel percorso sui vari nodi slave
     * @throws RemoteException
     */
    @Override
    public ArrayList<MyFileType> ls_func(String path, boolean dirCapacity) throws RemoteException {
        String realPath;
        ArrayList<MyFileType> totFiles = new ArrayList<MyFileType>();
        for (ServerInterface slave : slaveServers) {
            realPath = slave.getSharedDir() + path;
            //se esiste la cartella nello slave devo scrivere i suoi file
            if (slave.checkExists(realPath)) {
                for (MyFileType f : slave.ls_func(realPath, dirCapacity)) {
                    if (!(contains(totFiles, f))) {
                        totFiles.add(f);
                    } else {
                        //se è già presente vuol dire che è una directory e devo sommare le capacità
                        if (f.getType().equals("Dir")) {
                            if (dirCapacity)
                                unifyCapacity(totFiles, f);

                        }
                    }
                }

                //totFiles.addAll(slave.ls_func(path, dirCapacity));
            }
        }
        return totFiles;
    }

    private boolean unifyCapacity(ArrayList<MyFileType> array, MyFileType f) {
        for (MyFileType file : array) {
            if (file.getName().equals(f.getName())) {
                file.setSize(file.getSize() + f.getSize());
                return true;
            }
        }
        return false;
    }

    /**
     * Funzione privata per verificare se all'interno dell'array è presente un file con lo stesso nome
     *
     * @param array array di file in input
     * @param f     file da controllare
     * @return true se è già presente, false altrimenti
     */
    private boolean contains(ArrayList<MyFileType> array, MyFileType f) {
        for (MyFileType file : array) {
            if (file.getName().equals(f.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Metodo get per lìattributo sharedDIr
     *
     * @return valore dell'attributo sharedDir
     * @deprecated
     */
    @Deprecated
    @Override
    public String getSharedDir() {
        return sharedDir;
    }

    /**
     * Metodo per settare la directory condivisa sui vari nodi slave
     *
     * @param path percorso della directory condivisa (il percorso deve essere comune per tutti i nodi slave)
     * @return true se il percorso viene settato con successo
     * @throws RemoteException
     * @deprecated
     */
    @Deprecated
    @Override
    public boolean selShared_dir(String path) throws RemoteException {
        sharedDir = path;
        System.out.println("Directory condivisa settata con successo!");
        System.out.println("Percorso: " + sharedDir);
        return true;
    }

    /**
     * Metodo per verificare l'esistenza di un determinato file/cartella all'interno dei nodi slave
     *
     * @param path percorso al file/cartella da verificare
     * @return true se esiste il file/directory, false altrimenti
     * @throws RemoteException
     */
    @Override
    public boolean checkExists(String path) throws RemoteException {
        for (ServerInterface slave : slaveServers) {
            String realPath = slave.getSharedDir() + path;
            //se esiste la cartella nello slave mi ci posso spostare dentro
            if (slave.checkExists(realPath)) return true;
        }
        return false;
    }


    /**
     * Metodo rm relativo al ServerManager, viene chiamato dai vari client per l'eliminazioni di file dal cluster.
     * Si occupa di andare a verificare l'esistenza del file e poi viene chiamata l'apposita funzione dello slave che contiene
     * quel file per l'eliminazione
     *
     * @param path percorso del file da eliminare
     * @return true in caso di successo, false altrimenti
     * @throws RemoteException
     */
    @Override
    public boolean rm_func(String path) throws IOException {
        for (ServerInterface slave : slaveServers) {
            String realPath = slave.getSharedDir() + path;
            //se esiste il file nello slave, il file è supposto univoco
            if (slave.checkExists(realPath)) {
                updateFileSystemTree(path, true);
                if (slave.rm_func(realPath))
                    return true;
            }

        }
        System.err.println("Cancellazione del file fallita!");
        return false;
    }

    /**
     * Metodo per l'eliminazione ricorsiva di una directory presente nel cluster, fa uso della funzione "rm_func"
     *
     * @param path percorso della directory da eliminare
     * @return true in caso di successo, false altrimenti
     * @throws RemoteException
     */
    @Override
    public boolean rm_func_rec(String path) throws IOException {
        boolean err_canc = false;
        for (ServerInterface slave : slaveServers) {
            String realPath = slave.getSharedDir() + path;
            //se esiste il file nello slave, il file è supposto univoco
            if (slave.checkExists(realPath)) {
                //aggiorno fileSystemTree serverManager
                updateFileSystemTree(path, true);
                //aggiorno fileSystemTree del nodo slave
                slave.updateFileSystemTree(path, true);
                if (!(slave.rm_func_rec(realPath)))
                    err_canc = true;
            }

        }
        if (err_canc == true) {
            System.err.println("Cancellazione fallita!");
            return false;
        } else
            return true;
    }

    /**
     * Metodo cp relativo al ServerManager, per la copia di file tra i vari data nodes
     *
     * @param localPath  path file sorgente
     * @param remotePath path file destinazione
     * @return true in caso di successo
     * @throws RemoteException
     */
    @Override
    public boolean cp_func(String localPath, String remotePath) throws IOException {

        if (!checkExists(remotePath)) {
            //recupero indice dello slave più libero
            int slaveIndex = freerNodeChooser();
            // recupero il reference al nodo slave
            ServerInterface slave = getSlaveNode(slaveIndex);
            //System.out.println("nodo scelto: "+ slave.getName());

            //se è un trasferimento di file interno al file system remoto
            if (checkExists(localPath)) {
                String realRemotePath = slave.getSharedDir() + remotePath;
                slave.startFileServer(port, realRemotePath, getFile(localPath).getSize());
                String loc = getFileLocation(localPath);
                //System.out.println("locazione: "+loc);
                ServerInterface ClSlave = getSlaveNode(loc);
                String realLocalPath = ClSlave.getSharedDir() + localPath;
                //  System.out.println("localpath: "+realLocalPath+" remote: "+realRemotePath);
                ClSlave.startFileClient(port, slave.getIp(), realLocalPath);
                return true;

            }
        }
        return false;
    }

    /**
     * Metodo per la copia ricorsiva di directory tra i dataNodes
     *
     * @param clientPath path dir sorgente
     * @param serverPath path di destinazione
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void recursiveCopyInt(String clientPath, String serverPath) throws IOException {
        for (ServerInterface slave : getSlaveServers()) {
            String realClientPath = slave.getSharedDir() + clientPath;
            String realServerPath;
//            System.out.println("real client path : "+realClientPath);
            if (slave.isDirectory(realClientPath) && slave.checkExists(realClientPath)) {
                String[] param = new String[1];
                param[0] = serverPath;
                //System.out.println("Creo directory : "+serverPath);
                //ser.mkdir(param, this.getCurrentPath());
                realServerPath = slave.getSharedDir() + serverPath;
//                System.out.println("Creo directory : "+realServerPath);
                updateFileSystemTree(serverPath, false);
                slave.mkdir(realServerPath);
                for (File sub : slave.listFiles(realClientPath)) {
                    String newClientPath = clientPath + '/' + sub.getName();
                    String newSerPath = serverPath + '/' + sub.getName();
                    recursiveCopyInt(newClientPath, newSerPath);
                }
            } else {
//                System.out.println("copio: "+clientPath+" in: "+ serverPath);
                //i path vengono modificati all'interno di cp_func
                cp_func(clientPath, serverPath);
            }

        }
    }


    /**
     * Metodo che restitusìisce la locazione (nome del nodo slave) di un file/directory dato il percorso
     *
     * @param path1 percorso del file/directory
     * @return stringa contenente il nome del data node nel quale il file è presente
     * @throws RemoteException
     */
    @Override
    public String getFileLocation(String path1) throws RemoteException {
        String location = "";
        for (ServerInterface slave : slaveServers) {
            //se esiste la cartella nello slave devo scrivere i suoi file
            String realPath = slave.getSharedDir() + path1;
            if (slave.checkExists(realPath)) {
                location = slave.getName();
            }
        }
        return location;
    }

    /**
     * Deprecato, metodo per la copia di file nel nodo slave, ora la copia avviene attraverso l'apertura di un socket diretto
     * tra slave e client
     *
     * @param slave riferimento al nodo slave
     * @param path1 percorso del file/directory sorgente
     * @param path2 percorso di destinazione
     * @return true in caso di successo, false altrimenti
     * @throws IOException
     * @throws InterruptedException
     * @deprecated
     */
    @Deprecated
    public boolean cp_func_slave(ServerInterface slave, String path1, String path2) throws IOException, InterruptedException {
        slave.startFileServer(port, path2, getFile(path1).getSize());
        FileClient fc = new FileClient(port, slave.getIp());
        fc.send(path1, false, 0);
        return true;
    }

    /**
     * Metodo per la copia ricorsiva di directory , deprecato!!
     *
     * @param f          riferimento al file/directory
     * @param localPath  path i-esimo al file da copiare
     * @param remotePath path di destinazione i-esimo
     * @throws IOException
     * @throws InterruptedException
     * @deprecated
     */
    @Deprecated
    public void recursiveCopy(File f, String localPath, String remotePath) throws IOException, InterruptedException {
        for (ServerInterface slave : slaveServers) {
            if (f.isDirectory()) {
                slave.mkdir(remotePath);
                for (File sub : f.listFiles()) {
                    String localPathNew = localPath + '/' + sub.getName();
                    String remotePathNew = remotePath + '/' + sub.getName();
                    recursiveCopy(sub, localPathNew, remotePathNew);
                }
            } else {
                if (checkExists(localPath))
                    cp_func_slave(slave, localPath, remotePath);
            }
        }

    }

    /**
     * Metodo move del ServerManager, si va ad identificare il nodo contenente il file/directory specificata
     * e si cambia il percorso. Non c'è un vero e proprio trasferimento di file, ma solo un cambio di posizione
     * all'interno del file system locale, cambiando il nome. Ad es. ./dir1/file1.txt diventa  ./dir2/file1.txt
     *
     * @param path1 percorso sorgente al file/directory che si vuole spostare
     * @param path2 percorso di destinazione del file/directory
     * @param loc1  nome del nodo su cui il file si trova
     * @return true in caso di successo, false altrimenti
     * @throws IOException
     * @throws InterruptedException
     */

    @Override
    public boolean move(String path1, String path2, String loc1) throws IOException, InterruptedException {
        String realPath1, realPath2;

        MyFileType f1 = getFile(path1);
        MyFileType f2 = getFile(path2);

//        System.out.println("path1: "+path1);
//        System.out.println("path2: "+path2);
        //System.out.println(f1.getType()+ " " +f2.getType());
        if (f1.getType().equals("File") && f2.getType().equals("Dir")) {
            //si recupera il nodo slave e si cmabia il percorso dei file/directory
            // es : s1 -> s1
            String fileName = utils.getFileName(path1);
            path2 += "/" + fileName;
            if (path1 != path2) {
                ServerInterface slave = getSlaveNode(loc1);
                realPath1 = slave.getSharedDir() + path1;
                realPath2 = slave.getSharedDir() + path2;
                //aggiorno fileSystemTree di serverManager e slave
                updateFileSystemTree_move(path1, path2);
                slave.updateFileSystemTree_move(path1, path2);

                slave.move(realPath1, realPath2);
            }

        } else if (f1.getType().equals("Dir") && f2.getType().equals("Dir")) {

            String fileName = utils.getFileName(path1);
            path2 += "/" + fileName;
            //la copia ricorsiva non è più necessaria, devo solo cambiare i nomi come nel
            //caso precendente
            //System.out.println("Inizio la copia ricorsiva di "+f1.getName());
            //recursiveCopy(f1, path1, path2);
            for (ServerInterface slave : slaveServers) {
                realPath1 = slave.getSharedDir() + path1;
                realPath2 = slave.getSharedDir() + path2;
                if (path1 != path2) {
                    //aggiorno fileSystemTree di serverManager e slave
                    updateFileSystemTree_move(path1, path2);
                    slave.updateFileSystemTree_move(path1, path2);

                    slave.move(realPath1, realPath2);
                }
            }


        }

        else {
            System.err.println("Errore nella sintassi del comando");
            return false;

        }


        return true;
    }

    /**
     * Metodo che si occupa di fare l'update del filesystem tree interno.
     *
     * @param path
     * @throws IOException
     * @throws RemoteException
     */
    @Override
    public void updateFileSystemTree(String path, boolean delete) throws IOException, RemoteException {

        if (fileSystemTree == null) {
            Tree.Node root = new Tree.Node("/", "root");
            fileSystemTree = new Tree(root);
            fileSystemTree.init();
        }

        String fTreePath = System.getProperty("user.home") + "/.config/MyDFS/fileSystemTree";
        FileOutputStream fout = new FileOutputStream(fTreePath);
        ObjectOutputStream out = new ObjectOutputStream(fout);
        if (!delete)
            fileSystemTree.insert(path, utils.getFileName(path));
        else
            fileSystemTree.deleteNode(path);
        out.writeObject(fileSystemTree);
        out.close();
        fout.close();


    }

    @Override
    public void saveFileSystemTree() throws IOException, RemoteException{
        if (fileSystemTree == null) {
            Tree.Node root = new Tree.Node("/", "root");
            fileSystemTree = new Tree(root);
            fileSystemTree.init();
        }

        String fTreePath = System.getProperty("user.home") + "/.config/MyDFS/fileSystemTree";
        FileOutputStream fout = new FileOutputStream(fTreePath);
        ObjectOutputStream out = new ObjectOutputStream(fout);
        out.writeObject(fileSystemTree);
        out.close();
        fout.close();

    }


    /**
     * Metodo che si occupa di fare l'update del filesystem tree interno in caso di movimento di directory
     *
     * @param path1 percorso iniziale
     * @param path2 nuovo percorso
     * @throws IOException
     * @throws RemoteException
     */
    @Override
    public void updateFileSystemTree_move(String path1, String path2) throws IOException, RemoteException {

        if (fileSystemTree == null) {
            Tree.Node root = new Tree.Node("/", "root");
            fileSystemTree = new Tree(root);
            fileSystemTree.init();
        }

        String fTreePath = System.getProperty("user.home") + "/.config/MyDFS/fileSystemTree";
        FileOutputStream fout = new FileOutputStream(fTreePath);
        ObjectOutputStream out = new ObjectOutputStream(fout);
        fileSystemTree.move(path1, path2);
        out.writeObject(fileSystemTree);
        out.close();
        fout.close();

        System.out.println("File system modificato in : ");
        fileSystemTree.trasverseTree();
    }


    /**
     * Metodo per la creazione di directory sui nodi slave, dato in input il nome di una/più directory viene creata la stessa
     * directory per ogni slave nodes, in questo modo si mantiene una struttura del file system coerente per ogni nodo
     * e si riesce a scalare lo spazio di memorizzazione molto meglio
     *
     * @param param       percorsi delle nuove directory che si vogliono creare
     * @param currentPath percorso attuale in cui si trova il client all'interno del file system
     * @return true i caso di successo, false altrimenti
     * @throws RemoteException
     */
    @Override
    public boolean mkdir(String[] param, String currentPath) throws IOException {


        String realPath = "";
        for (int i = 1; i < param.length; i++) {

            if (param[i].startsWith("./")) {
                //caso ./dir/new_dir

                //dato path relativo trovo l'assoluto tolto nome della nuova dir -> /home/user/dir
                param[i] = param[i].substring(1, param[i].length());
                param[i] = currentPath + param[i];
                String[] tmpArray = param[i].split("/");
                String tmp = "";
                for (int j = 0; j < tmpArray.length - 1; j++) {
                    tmp += tmpArray[j] + "/";
                }

//                String loc = getFileLocation(tmp);
//                ServerInterface slave = getSlaveNode(loc);
                for (ServerInterface slave : slaveServers) {
                    realPath = slave.getSharedDir() + param[i];
                    if(slave.mkdir(realPath))
                        updateFileSystemTree(currentPath + "/" + param[i], false);
                }
            } else {
                //caso in cui scrivo solo il nome della cartella
                for (ServerInterface slave : slaveServers) {
                    realPath = slave.getSharedDir() + currentPath;
                    if (slave.checkExists(realPath)) {
                        realPath = realPath + "/" + param[i];
                        //System.out.println(currentPath+ "/" + param[i]);
                        if(slave.mkdir(realPath))
                            updateFileSystemTree(currentPath + "/" + param[i], false);
                    }
                }
            }
        }

        return true;
    }

    /**
     * Metodo per ottenere la totalità dello spazio allocabile per tutti i nodi slave. Ad esempio se ho 2 slave
     * s1 = 1GB free
     * s2 = 2GB free
     * spazio totale = 1+2 = 3GB
     *
     * @return spazio totale disponibile in formato long
     * @throws RemoteException
     */
    @Override
    public long getFreeSpace() throws RemoteException {
        long freeSpace = 0;
        for (ServerInterface slave : slaveServers) {
            freeSpace += slave.getFreeSpace();
        }
        return freeSpace;
    }

    /**
     * Metodo per il calcolo della capacità totale del cluster, non va a tenere in considerazione lo spazio già
     * allocato sui vari nodi slave, ma da una stima abbastanza precisa sulla capacità totale.
     *
     * @return capacità totale del cluster in formato long
     * @throws RemoteException
     */
    @Override
    public long getClusterCapacity() throws RemoteException {
        long totCapacity = 0;
        for (ServerInterface slave : slaveServers) {
            totCapacity += slave.getCapacity();
        }
        return totCapacity;
    }

    /**
     * Metodo per il balancing dello spazio allocato sul cluster, nella versione attuale non viene utilizzato. Si potrebbe
     * integrare in futuro nel sistema
     *
     * @return true in caso di successo
     * @throws RemoteException
     */
    // metodo per il balancing del cluster, non più necessario
    public boolean balance() throws RemoteException {
//        ArrayList<MyFileType> totFiles = ls_func(sharedDir, true);
//        totFiles.sort(new Comparator<MyFileType>() {
//            @Override
//            public int compare(MyFileType t0, MyFileType t1) {
//                return Long.compare(t0.getSize(), t1.getSize());
//            }
//        }.reversed());
//
//
//        for (MyFileType f: totFiles){
//            System.out.println("Name: "+f.getName());
//            System.out.println("Size: "+f.getSize());
//        }
        return true;
    }

    /**
     * Funzione che restituisce un riferimento al file, dato input il path (non reale al file, ma già processato dalla funzione cleanString)
     *
     * @param path percorso in formato /dir1/...
     * @return riferimento al file
     * @throws RemoteException
     */
    @Override
    public MyFileType getFile(String path) throws RemoteException {

        String name = utils.getFileName(path);
        String newPath = utils.pathWithoutLast(path);
        ArrayList<MyFileType> allFiles = ls_func(newPath);

        for (MyFileType f : allFiles) {
            if (f.getName().equals(name)) {
                //System.out.println("File trovato: "+f.getName()+" dimensione: "+ f.getSize());
                return f;
            }
        }
        //se non esiste ritorno file vuoto
        return new MyFileType("shDir", "Dir", 4096, "-", "-");
    }

    /**
     * Main della classe serverManager
     *
     * @param args lista di indirizzi ip dei vari nodi slave appartenenti al cluster
     * @throws RemoteException
     */
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
