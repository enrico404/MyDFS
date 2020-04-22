package Server;

import utils.FileClient;
import utils.FileServerThread;
import utils.utils;

import java.awt.desktop.SystemEventListener;
import java.io.File;
import java.io.IOException;

public class SecondaryServerUpdater extends Thread {

    private String secondaryServerIP;
    private int port;

    public SecondaryServerUpdater(String ip, int Port) {
        secondaryServerIP = ip;
        port = Port;
    }

    public void run() {
        try {
            FileClient fc;
            String fileSystemTreePath = System.getProperty("user.home") + "/.config/MyDFS/fileSystemTree";


            while (true) {

                File f = new File(fileSystemTreePath);
                if (f.exists()) {
                    System.out.println("prova");
                    fc = new FileClient(port, secondaryServerIP);
                    fc.send(fileSystemTreePath, true, f.length());

                }

                Thread.sleep(1000);
            }


        } catch (InterruptedException e) {

        } catch (IOException e) {

        }
    }
}

