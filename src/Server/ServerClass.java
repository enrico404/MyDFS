package Server;

import utils.MyFileType;
import utils.utils;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import utils.FileClient;
import utils.FileServerThread;
import utils.ConsoleColors;

/**
 * Classe comune dei Data Nodes. Ogni Data Node ha un nome e una directory condivisa, essa deve avere lo stesso nome
 * tra tutti i Data nodes in questo modo si avrà una gestione corretta dei dati. Ogni nodo presenta anche un FileClient
 * che si occupa del trasferimento di dati "verso" qualcun'altro. È presente inoltre anche un FileServer che si occupa
 * di ricevere i dati. Attraverso questi due componenti è possibile effettuare operazioni come:
 * <ul>
 *     <li>Copia di file da client a data node</li>
 *     <li>Copia di file da data node a client</li>
 * </ul>
 * <p>
 * Maggiori informazioni nelle classi rispettive
 *
 * @see FileClient
 * @see FileServerThread
 */
public class ServerClass extends UnicastRemoteObject implements ServerInterface {
    /**
     * Attributo contenente il nome del server
     */
    private String name = "";
    /**
     * directory condivisa con gli altri server, deve essere la stessa per tutti i server
     */
    private String sharedDir = "";
    /**
     * riferimento al FileServer, serve per ricevere i file
     */
    private FileServerThread thread = null;
    /**
     * riferimento al FileClient, serve per mandare i file
     */
    private FileClient fc = null;

    private Tree fileSystemTree = null;


    /**
     * Costruttore di default, va semplicemente a settare il nome del data node
     *
     * @param Name nome del nodo
     * @throws RemoteException
     */
    public ServerClass(String Name) throws RemoteException {
        super();
        name = Name;

        FileInputStream f = null;
        ObjectInputStream in = null;

        try {
            f = new FileInputStream(System.getProperty("user.home") + "/.config/MyDFS/fileSystemTreeSlave");
            in = new ObjectInputStream(f);
            fileSystemTree = (Tree) in.readObject();



        } catch (FileNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally{
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(f != null){
                try {
                    f.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(fileSystemTree == null){
                    Tree.Node root = new Tree.Node("/", "root");
                    fileSystemTree = new Tree(root);
                    fileSystemTree.init();
            }

        }
    }


    /**
     * Metodo che si occupa di fare l'update del filesystem tree interno.
     * @param path
     * @throws IOException
     * @throws RemoteException
     */
    @Override
    public void updateFileSystemTree(String path, boolean delete) throws IOException, RemoteException {

        if(fileSystemTree == null){
            Tree.Node root = new Tree.Node("/", "root");
            fileSystemTree = new Tree(root);
            fileSystemTree.init();
        }
       // System.out.println("Aggiorno filesystemtree: "+path);
        FileOutputStream fout = new FileOutputStream(System.getProperty("user.home") + "/.config/MyDFS/fileSystemTreeSlave");
        ObjectOutputStream out = new ObjectOutputStream(fout);
      //  System.out.println("Dopo apertura");
        if(!delete)
            fileSystemTree.insert(path, utils.getFileName(path));
        else
            fileSystemTree.deleteNode(path);
        out.writeObject(fileSystemTree);
       // System.out.println("oggetto salvato");
        out.close();
        fout.close();

    }

    /**
     * Metodo che si occupa di fare l'update del filesystem tree interno in caso di movimento di directory
     * @param path1 percorso iniziale
     * @param path2 nuovo percorso
     * @throws IOException
     * @throws RemoteException
     */
    @Override
    public void updateFileSystemTree_move(String path1, String path2) throws IOException, RemoteException {

        if(fileSystemTree == null){
            Tree.Node root = new Tree.Node("/", "root");
            fileSystemTree = new Tree(root);
            fileSystemTree.init();
        }

        FileOutputStream fout = new FileOutputStream(System.getProperty("user.home") + "/.config/MyDFS/fileSystemTreeSlave");
        ObjectOutputStream out = new ObjectOutputStream(fout);
        fileSystemTree.move(path1, path2);
        out.writeObject(fileSystemTree);
        out.close();
        fout.close();

    }


    /**
     * Metodo per la correzione del tree di directory nel nodo slave. Devo gestire sia i casi di creazione, rimozione e
     * movimento di directory. "listDirs" è la lista di directory aggiornata.
     * @param FileSystemTree fileSystemTree del serverManager aggiornato
     * @return true in caso di correzione con succcesso
     * @throws IOException
     */
    public boolean correct(Tree FileSystemTree) throws IOException {
        boolean modified = false;
        ArrayList<String> listDirs = FileSystemTree.getDirs();
        //ciclo per vedere se ho directory nuove da aggiungere
        for(String dir: listDirs){
            String realPath = getSharedDir()+dir;
            if(!checkExists(realPath)) {
                System.out.println("la directory "+ dir+" non esiste, la creo");
                mkdir(realPath);
                modified = true;
            }
        }

        String delPath = "";
        //ciclo per vedere se devo rimuovere delle directory nello slave
        for(String dir: fileSystemTree.getDirs()){
            //se la directory dello slave non è presente nel fileSystemTree del serverManager, la devo eliminare
            if(!utils.contains(FileSystemTree.getDirs(), dir)){
                String realPath = getSharedDir()+dir;
                rm_func_rec(realPath);
                delPath = dir;
                modified = true;
                break;
            }
        }
        if(!delPath.equals(""))
            updateFileSystemTree(delPath, true);
        return modified;
    }

    /**
     * Getter dell'attributo name
     *
     * @return stringa contenente il nome del nodo
     */
    public String getName() {
        return name;
    }

    /**
     * Getter del path alla directory condivisa
     *
     * @return stringa contenente il path alla directory condivisa del nodo
     */
    public String getSharedDir() throws RemoteException{
        return sharedDir;
    }

    /**
     * Setter del path alla directory condivisa
     *
     * @param path percorso alla directory condivisa
     * @throws RemoteException
     */
    @Override
    public void setSharedDir(String path) throws RemoteException {
        sharedDir = path;
        System.out.println("Directory condivisa settata con successo!");
        System.out.println("Percorso: " + sharedDir);
    }

    /**
     * Getter dell'indirizzo ip del nodo
     *
     * @return Stringa contenente l'indirizzo ip del nodo in formato IpV4
     * @throws RemoteException
     * @throws SocketException
     */
    public String getIp() throws RemoteException, SocketException {
        String ip = utils.getInet4Addresses().get(0).toString().substring(1);
        return ip;
    }


    /**
     * Metodo per controlare l'esistenza di un file/directory all'interno del nodo, è utilizzato in diverse funzioni come
     * mv e cp
     *
     * @param path percorso al file/directory da controllare
     * @return true se il file/directory esiste, false altrimenti
     * @throws RemoteException
     */
    @Override
    public boolean checkExists(String path) throws RemoteException {
        File tmpDir = new File(path);
        boolean exists = tmpDir.exists();
        if (exists) {
            return true;

        }
        return false;
    }

    /**
     * Metodo helper per controllare se un file è una directory o meno
     *
     * @param path percorso al file/directory
     * @return true se il file è una directory, false altrimenti
     * @throws RemoteException
     */
    @Override
    public boolean isDirectory(String path) throws RemoteException {
        File f = new File(path);
        if (f.isDirectory()) return true;
        return false;
    }


    /**
     * Metodo che ritorna una lista di file se il percorso passato come parametro è una directory
     *
     * @param path percorso alla directory (relativo/assoluto)
     * @return la lista di file se è una directory il percorso dato in input, null altrimenti
     * @throws RemoteException
     */
    @Override
    public File[] listFiles(String path) throws RemoteException {
        File f = new File(path);
        if (f.isDirectory()) return f.listFiles();
        return null;
    }

    /**
     * @param path percorso in cui si vuole eseguire il comando
     * @return lista di file presenti in quel percorso
     * @throws RemoteException
     * @deprecated
     */
    @Override
    @Deprecated
    public ArrayList<MyFileType> ls_func(String path) throws RemoteException {
        ArrayList<MyFileType> result = new ArrayList<MyFileType>();
        if (sharedDir != null) {

            File[] files = new File(path).listFiles();

            for (File file : files) {
                String type;
                if (file.isFile()) {
                    type = "File";
                } else {
                    type = "Dir";
                }
                MyFileType f = new MyFileType(file.getName(), type, file.length(), name, file.getAbsolutePath());
                result.add(f);

            }
        } else {
            utils.error_printer("Errore nella condivisione della directory");
        }
        return result;
    }

    /**
     * Metodo ls relativo al data node, va a restituire una lista di file con associato anche il tipo e la dimensione.
     * Viene utilizzato il tipo "MyFileType" poichè c'erano dei problemi nel passare una lista di file normale, il descrittore
     * dei file passato non è completo per un qualche motivo o si perdevano informazioni.
     *
     * @param path        percorso in cui si vuole eseguire il comando
     * @param dirCapacity parametro booleano che dice se calcolare la dimensione delle directory opppure no, solitamente
     *                    non coviene calcolarlo per questioni di efficienza
     * @return
     * @throws RemoteException
     */
    @Override
    public ArrayList<MyFileType> ls_func(String path, boolean dirCapacity) throws RemoteException {
        ArrayList<MyFileType> result = new ArrayList<MyFileType>();
        if (dirCapacity) {
            if (sharedDir != null) {

                File[] files = new File(path).listFiles();

                for (File file : files) {
                    String type;
                    long size;
                    if (file.isFile()) {
                        type = "File";
                        size = file.length();
                    } else {
                        type = "Dir";
                        size = getDirSize(file);
                    }
                    MyFileType f = new MyFileType(file.getName(), type, size, name, file.getAbsolutePath());
                    result.add(f);

                }
            } else {
                utils.error_printer("Errore nella condivisione della directory");

            }
        } else {

            if (sharedDir != null) {

                File[] files = new File(path).listFiles();

                for (File file : files) {
                    String type;
                    if (file.isFile()) {
                        type = "File";
                    } else {
                        type = "Dir";
                    }
                    MyFileType f = new MyFileType(file.getName(), type, file.length(), name, file.getAbsolutePath());
                    result.add(f);

                }
            } else {
                utils.error_printer("Errore nella condivisione della directory");
            }

        }
        return result;
    }

    /**
     * Metodo interno ricorsivo per andare a calcolare la dimensione effettiva di una directory, utilizzarlo con attenzione,
     * se le directory presentano un numero molto elevato di file potrebbe avere problemi di performance
     *
     * @param f riferimento alla directory
     * @return dimensione della directory in formato long
     */
    private long getDirSize(File f) {
        long size = 0;
        if (f.isDirectory()) {
            for (File file : f.listFiles()) {
                if (file.isDirectory()) {
                    size += getDirSize(file);
                } else {
                    size += file.length();
                }

            }
        } else {
            size += f.length();
        }
        return size;

    }

    /**
     * Metodo rm parte del data node, si va ad eliminare effettivamente il file se questo esiste all'interno di questo
     * nodo.
     *
     * @param path stringa contenente il percorso assoluto al file
     * @return true in caso di successo, false altrimenti
     * @throws RemoteException
     */
    @Override
    public boolean rm_func(String path) throws RemoteException {
        File f = new File(path);
        //se il file esiste
        if (f.exists()) {
            if(f.isDirectory()){
                if (f.delete()) {
                    System.out.println("file " + path + " eliminato con successo");
                    return true;
                }
            }
            else if (f.delete()) {
                System.out.println("file " + path + " eliminato con successo");
                return true;
            }

        } else {
            utils.error_printer("Il file " + path + " non esiste!");
        }
        return false;
    }

    /**
     * Metodo interno per la cancellazione ricorsiva di directory
     *
     * @param f
     */
    private void recursiveDelete(File f) {
        if (f.isDirectory()) {
            for (File sub : f.listFiles()) {
                recursiveDelete(sub);
            }
        }
        f.delete();
    }

    /**
     * Seconda parte del metodo per la cancellazione ricorsiva di directory lato data node, va semplicemente a controllare
     * se il file che si sta cancellando esiste prima iniziare con la ricorsione
     *
     * @param path percorso alla directory
     * @return true in caso di cancellazione senza errori, false altrimenti
     * @throws RemoteException
     */
    @Override
    public boolean rm_func_rec(String path) throws RemoteException {
        File f = new File(path);
        //se il file esiste
        if (f.exists()) {
            recursiveDelete(f);
            System.out.println(path + " eliminato con successo!");
            return true;
        }
        return false;
    }

    /**
     * Metodo mv lato data node, si basa sul metodo renameTo della classe File, infatti per come è organizzato il file-system
     * di linux non è necessario effettura nessuno spostamento fisico del file, ma basta cambiargli il campo corretto nell'header
     * del file
     *
     * @param path1 percorso al file/directory
     * @param path2 nuovo percorso
     * @return true in caso di successo, false altrimenti
     * @throws IOException
     */
    @Override
    public boolean move(String path1, String path2) throws IOException {
        File fileToMove = new File(path1);
        return fileToMove.renameTo(new File(path2));
    }

    /**
     * Metodo lato data node per ottenere lo spazio disponibile nel nodo
     *
     * @return spazio disponibile utilizzabile in formato long
     * @throws RemoteException
     */
    @Override
    public long getFreeSpace() throws RemoteException {
        File f = new File(sharedDir);
        long freeSpace = f.getUsableSpace();
        //System.out.println("Spazio libero sul nodo : " + name + " " + freeSpace);
        return freeSpace;
    }

    /**
     * Metodo lato data node per ottenere la capacità totale del nodo, si restituisce la capacità totale dell'hard disk
     * del nodo. Il sistema operativo scelto va a diminure la capacità del nodo.
     *
     * @return capacità del nodo in formato long
     * @throws RemoteException
     */
    @Override
    public long getCapacity() throws RemoteException {
        File f = new File(sharedDir);
        long capacity = f.getTotalSpace();
        return capacity;
    }

    /**
     * Metodo per l'apertura di un file, deve essere ancora implementato, l'idea è quella di effettuare una copia temporanea
     * del file in locale e poi aprirlo con gli strumenti di default che offre la macchina
     *
     * @return
     * @throws RemoteException
     */
    @Override
    public boolean open() throws RemoteException {
        return false;
    }


    /**
     * Metodo che fa partire il server's thread relativo al sistema di file transfer
     *
     * @param port numero di porta nel quale aprire il socket tcp per il trasferimento di file
     * @param path percorso in cui verrà scritto il nuovo file
     * @return true in caso di successo
     * @throws IOException
     */
    @Override
    public boolean startFileServer(int port, String path, long size) throws IOException {
        if (thread == null) {
            thread = new FileServerThread(port, path, size);
            thread.start();

        }
        thread.setPath(path, size, false);

        return true;
    }

    /**
     * Metodo per l'invio di file verso altri data node oppure verso altri client
     *
     * @param port numero di porta in cui aprire la connessione tcp per l'invio del file
     * @param ip   ip in formato IpV4 a cui mandare il file, è necessario che questo sia all'interno della stessa rete locale
     *             e raggiungibile
     * @param path percorso del file da inviare
     * @return true in caso di successo, false altrimenti
     * @throws IOException
     */
    @Override
    public boolean startFileClient(int port, String ip, String path) throws IOException {
        File f = new File(path);
        if (!(f.isDirectory())) {
            fc = new FileClient(port, ip);
            fc.send(path, true, f.length());
            return true;
        }
        return false;
    }

    /**
     * Metodo per la creazione di una directory nel nodo, ci si basa sul metodo offerto dalla classe File
     *
     * @param path percorso della nuova directory
     * @return true in caso di successo, false altrimenti
     * @throws RemoteException
     */
    @Override
    public boolean mkdir(String path) throws IOException {
        File f = new File(path);
        String relativePath = path.substring(sharedDir.length());
        //System.out.println("path: "+path+" exists: "+ f.exists());
        if(!f.exists()) {
            if (f.mkdir()) {
                updateFileSystemTree(relativePath, false);
                return true;
            }
        }
        return false;
    }

    /**
     * Getter dell'attributo fileSystemTree
     * @return fileSystemTree
     */
    @Override
    public Tree getFileSystemTree(){
        return fileSystemTree;
    }


}
