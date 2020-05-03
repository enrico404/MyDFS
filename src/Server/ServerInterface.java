package Server;
import Client.ClientClass;
import utils.FileServerThread;
import utils.MyFileType;

import java.beans.PropertyEditorManager;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.rmi.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Interfaccia offerta dagli oggetti remoti di tipo "ServerClass"
 *
 * @see ServerClass
 */
public interface ServerInterface extends Remote {
    String getName() throws RemoteException;
    ArrayList<MyFileType> ls_func(String path) throws RemoteException;
    ArrayList<MyFileType> ls_func(String path, boolean dirCapacity) throws RemoteException;
    boolean rm_func(String path) throws RemoteException;
    boolean open() throws RemoteException;
    String getSharedDir() throws RemoteException;
    void setSharedDir(String path) throws RemoteException;
    boolean checkExists(String path) throws RemoteException;
    boolean rm_func_rec(String path) throws RemoteException;
    long getFreeSpace() throws RemoteException;
    long getCapacity() throws RemoteException;
    String getIp() throws RemoteException, SocketException;
    boolean startFileClient(int port, String ip, String path) throws IOException;
    boolean mkdir(String path) throws IOException;
    boolean isDirectory(String path) throws RemoteException;
    File[] listFiles(String path) throws RemoteException;
    boolean move(String path1, String path2) throws IOException;
    boolean startFileServer(int port, String path, long size) throws IOException;
    void updateFileSystemTree(String path, boolean delete) throws IOException, RemoteException;
    void updateFileSystemTree_move(String path1, String path2) throws IOException, RemoteException;
    Tree getFileSystemTree() throws RemoteException;
    boolean correct(Tree FileSystemTree) throws IOException;



}
