package Client;

import Server.ServerInterface;
import Server.ServerManagerInterface;
import utils.MyFileType;
import utils.ConsoleColors;
import utils.Helper;
import java.io.*;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;

import utils.utils;
import utils.FileClient;
import utils.FileServerThread;
import utils.Converter;

/**
 * Main della classe "ClientCLass" gestisce l'input dei vari comandi e va a richiamare i metodi relativi gestiti dalla
 * classe CLientClass
 *
 * @throws IOException
 */
public class ClientClassMain {

    public static void main(String args[]) throws IOException {

        try {
            ServerManagerInterface ser = null;
            ServerManagerInterface backupSer = null;
            ArrayList<ServerManagerInterface> serverManagers = new ArrayList<>();
            for (int i = 0; i < args.length; i++) {
                String serverAdd = args[i];
                try {
                    ser = (ServerManagerInterface) Naming.lookup(serverAdd);
                    ser.getIp();
                    //se non genera eccezzioni lo aggiungo all'array di server Managers
                    serverManagers.add(ser);
                } catch (Exception e) {}

            }
            if (serverManagers.size() == 0) {
                utils.error_printer("Non è stato trovato un server manager valido!");
                return;
            }
            ClientClass client = null;
            try {
                client = new ClientClass(serverManagers);
            }catch (Exception e){utils.error_printer("Errore nella connessione con il serverManager");}
            System.out.println(ConsoleColors.GREEN_BOLD + "Connesso al cluster correttamente!" + ConsoleColors.RESET);
            System.out.println("ServerManager ip: "+ client.getSer().getIp());
            //creo un thread che controlla che il client sia connesso almeno ad un data node
            ServersConnectedChecker serverStatusChecker = new ServersConnectedChecker(client);
            serverStatusChecker.start();

            boolean exit = false;
            while (!exit) {
                try {
                    System.out.flush();
                    System.out.println("Inserisci comando...");
                    if (client.getCurrentPath().equals(""))
                        System.out.println(ConsoleColors.BLUE_BOLD + "Path: /" + ConsoleColors.RESET);
                    else
                        System.out.println(ConsoleColors.BLUE_BOLD + "Path: " + client.getCurrentPath() + ConsoleColors.RESET);
                    System.out.println("");
                    System.out.print('>');
                    Scanner in = new Scanner(System.in);
                    String ins = in.nextLine();


                    if (ins.startsWith("ls") || ins.equals("ll")) {
                        //ll è un alias ad ls -l
                        if (ParamParser.checkParam(ins, "-l") || ins.startsWith("ll")) {
                            client.ls_func(client.getSer(), false, true, false);
                        } else if (ParamParser.checkParam(ins, "-d")) {
                            client.ls_func(client.getSer(), true, false, false);
                        }
                        //nel caso a un parametro il -h è inutile a causa del comporamento di default

                        else if (ParamParser.checkParam(ins, "-ld"))
                            client.ls_func(client.getSer(), true, true, false);

                        else if (ParamParser.checkParam(ins, "-lh"))
                            client.ls_func(client.getSer(), false, true, true);

                            //se sbaglio ad inserire un opzione il comando non viene lanciato
                        else if (ParamParser.checkParam(ins, "-ldh"))
                            client.ls_func(client.getSer(), true, true, true);

                        else {
                            //comportamento di default senza parametri
                            client.ls_func(client.getSer(), false, false, false);
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
                        if (!(client.cd_func(client.getSer(), path))) {
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

                            if (!(client.rm_func(client.getSer(), paths))) {
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

                                if (!(client.rm_func_rec(client.getSer(), paths))) {
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
                                param[param.length - 1] = client.genDestPath(param[i], param[param.length - 1], client.getSer());
                                // System.out.println("passo i parametri :"+param[1]+" "+ param[2]);

                                //controllo esistenza del file all'interno della funzione
                                if (!(client.cp_func(client.getSer(), param[i], param[param.length - 1]))) {
                                    utils.error_printer("Errore nella copia del file!");
                                }
                                System.err.println("");
                                param[param.length - 1] = originalDPath;
                            }
                        }
                        // casi: slave->client oppure client(directory)->server
                        else if ((ParamParser.checkParam(ins, "-m") || ParamParser.checkParam(ins, "-r"))) {

                            int startIndex = ParamParser.getParamNumbers(ins);
                            for (int i = startIndex; i < param.length - 1; i++) {
                                param[i] = utils.cleanString(param[i], client);
                                param[param.length - 1] = utils.cleanString(param[param.length - 1], client);

                                if (ParamParser.checkParam(ins, "-m"))
                                    param[param.length - 1] = client.genDestPath(param[i], param[param.length - 1], client.getSer()) + "/" + utils.getFileName(param[i]);
                                else
                                    param[param.length - 1] = client.genDestPath(param[i], param[param.length - 1], client.getSer());

                                //il controllo dell'esistenza del file è dentro la funzione, per tutti i casi
                                if (!(client.cp_func(client.getSer(), param[i], param[param.length - 1], ins, true))) {
                                    utils.error_printer("Errore nella copia del file!");
                                }
                                System.err.println("");
                                param[param.length - 1] = originalDPath;
                            }

                        }
                        //casi:  slave(directory)->client
                        else if (ParamParser.checkParam(ins, "-rm")) {

                            int startIndex = ParamParser.getParamNumbers(ins);
                            param[startIndex] = utils.cleanString(param[startIndex], client);
                            param[startIndex + 1] = utils.cleanString(param[startIndex + 1], client) + "/" + utils.getFileName(param[startIndex]);

                            if (!(param[startIndex].startsWith("/"))) {
                                utils.error_printer("Devi specificare un path assoluto sul client!");
                                continue;
                            } else {
//                                String[] tmp =  param[3].split("/");
//                                String lastEl = tmp[tmp.length-1];
//                                String dirName =  lastEl.substring(0, lastEl.length());
//                                param[4] = param[4]+"/"+dirName;
                                param[startIndex + 1] = client.genDestPath(param[startIndex], param[startIndex + 1], client.getSer());
                            }


                            //controllo esistenza all'interno della funzione
                            if (!(client.cp_func(client.getSer(), param[startIndex], param[startIndex + 1], ins, true))) {
                                utils.error_printer("Errore nella copia del file!");
                            }
                            System.err.println("");

                        } else if (ParamParser.checkParam(ins, "-i")) {
                            //caso solo cp -i


                            int startIndex = ParamParser.getParamNumbers(ins);
                            for (int i = startIndex; i < param.length - 1; i++) {
                                param[i] = utils.cleanString(param[i], client);
                                param[param.length - 1] = utils.cleanString(param[param.length - 1], client);
                                param[param.length - 1] = client.genDestPath(param[i], param[param.length - 1], client.getSer());

                                //controllo esistenza all'interno
                                // if (!(client.cp_func(ser, param[i], param[param.length-1], true))) {
                                if (!(client.getSer().cp_func(param[i], param[param.length - 1]))) {
                                    utils.error_printer("Errore nella copia del file!");
                                }
                                System.err.println("");

                                param[param.length - 1] = originalDPath;
                            }


                        } else if (ParamParser.checkParam(ins, "-ri")) {
                            //caso cp -i -r o cp -r -i
                            param[param.length - 1] = utils.cleanString(param[param.length - 1], client);
                            //System.out.println("Dest pathj: "+ param[param.length-1]);


                            int startIndex = ParamParser.getParamNumbers(ins);
                            for (int i = startIndex; i < param.length - 1; i++) {
                                param[i] = utils.cleanString(param[i], client);
                                param[param.length - 1] = utils.cleanString(param[param.length - 1], client);
                                param[param.length - 1] = client.genDestPath(param[i], param[param.length - 1], client.getSer());


                                System.out.println("Inizio la copia ricorsiva di " + param[i]);

//                                    System.out.println("source path: "+ param[i]);
//                                    System.out.println("Dest path: "+ param[param.length-1]);

                                if (client.getSer().checkExists(param[i])) {
                                    client.getSer().recursiveCopyInt(param[i], param[param.length - 1]);
                                } else
                                    utils.error_printer("Errore nella copia della directory, controlla che i percorsi siamo corretti!");

                                param[param.length - 1] = originalDPath;
                            }


                        } else {
                            utils.error_printer("Errore nella sintassi del comando! Digita 'help cp' per vedere la sintassi del comando");
                        }

                    } else if (ins.startsWith("mkdir")) {
                        String[] param = ins.split(" ");

                        if (client.getSer().mkdir(param, client.getCurrentPath())) {
                            System.out.println("Directory creata/e con successo");
                        }

                    } else if (ins.startsWith("mv")) {
                        String[] param = ins.split(" ");
                        param[1] = utils.cleanString(param[1], client);
                        param[2] = utils.cleanString(param[2], client);
                        String loc1 = client.getSer().getFileLocation(param[1]);
                        String loc2 = client.getSer().getFileLocation(param[2]);
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
//                            System.out.println(param[1]);
//                            System.out.println(param[2]);
                        if (exists)
                            client.getSer().move(param[1], param[2], loc1);

                    } else if (ins.startsWith("du")) {
                        client.du_func(ins, client.getSer());
                    } else if (ins.startsWith("sview")) {
                        client.sview_func(ins, client.getSer());

                    } else if (ins.startsWith("open")) {
                        String[] param = ins.split(" ");
                        if (!(client.open(param, client.getSer(), ins))) {
                            utils.error_printer("Errore nell'apertura del file \n");
                        }

                    } else if (ins.equals("exit")) {
                        exit = true;
                    } else {
                        utils.error_printer("comando digitato non esiste!");
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    utils.error_printer("Controlla la sintassi del comando (help <command_name>");
                }
            }

            if (client.getFileClient() != null)
                client.getFileClient().closeConnection();


        } catch (Exception e) {
            utils.error_printer("Errore nella comunicazione con il server manager");
            e.printStackTrace();
        }



    }
}
