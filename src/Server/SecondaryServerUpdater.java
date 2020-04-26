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

        FileClient fc;
        String fileSystemTreePath = System.getProperty("user.home") + "/.config/MyDFS/fileSystemTree";


        while (true) {
            try {

                this.sleep(550);
                File f = new File(fileSystemTreePath);
                if (f.exists()) {

                    fc = new FileClient(port, secondaryServerIP);
                    fc.send(fileSystemTreePath, false, f.length());


                }


            } catch (InterruptedException e) {

            } catch (IOException e) {

            }
        }


    }
}

