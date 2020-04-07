package Server;

import Client.ClientClass;
import utils.FileServerThread;
import utils.MyFileType;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Interfaccia offerta dagli oggetti remoti di tipo "ServerManager"
 * @see ServerManager
 */
public interface ServerManagerInterface extends Remote {
    ArrayList<MyFileType> ls_func(String path) throws RemoteException;
    ArrayList<MyFileType> ls_func(String path, boolean dirCapacity) throws RemoteException;
    String getSharedDir() throws RemoteException;
    ArrayList<ServerInterface> getSlaveServers() throws RemoteException;
    boolean selShared_dir(String path) throws RemoteException;
    boolean checkExists(String s) throws RemoteException;
    boolean rm_func(String path) throws RemoteException;
    boolean rm_func_rec(String path) throws RemoteException;
    boolean cp_func() throws RemoteException;
    ServerInterface getSlaveNode(int index) throws RemoteException;
    ServerInterface getSlaveNode(String name) throws RemoteException;
    int freerNodeChooser() throws RemoteException;
    String getIp() throws RemoteException, SocketException;
    String getFileLocation(String path1) throws RemoteException;
    boolean move(String path1, String path2,String loc1) throws IOException, InterruptedException;
    boolean mkdir(String[] path, String currentPath) throws RemoteException;
    long getFreeSpace() throws RemoteException;
    long getClusterCapacity() throws RemoteException;
    String getFileType(String path) throws RemoteException;
}
