package utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Classe helper per l'utilizzo dei vari comandi
 */
public class Helper {

    /**
     * Costruttore di default della classe
     */
    public Helper() {
        super();
    }

    /**
     * Metodo principale della classe, per ogni comando è presente un manuale in formato .txt, i vari manuali si trovano
     * nella directory "manualDoc" . Il nome dei manuali deve corrispondere esattamente al nome del comando. Es. comando
     * ls - ls.txt . Il metodo va semplicemente a stampare il contenuto del file
     *
     * @param cmdName
     * @throws IOException
     */
    public void print(String cmdName) throws IOException {
//        if(cmdName.equals("ls")){
//            System.out.println("Resistituisce i file nella directory corrente");
//            System.out.println("Uso: ls");
//        }
//        else if(cmdName.equals("cd")){
//            System.out.println("Cambia la directory corrente");
//            System.out.println("Uso:");
//            System.out.println("cd <dir_name>");
//        }
//        else if (cmdName.equals("rm")){
//            System.out.println("rimuove un file/directory specificato");
//            System.out.println("Uso:");
//            System.out.println("rm <file_name>");
//            System.out.println("Opzioni: -rf -> per rimuovere le directory");
//            System.out.println("Uso:");
//            System.out.println("rm -rf <dir_name>");
//        }
//        else if(cmdName.equals("cp")){
//            System.out.println("copia un file locale nel filesystem distribuito");
//            System.out.println("Uso:");
//            System.out.println("cp <path_to_file> <file_system_path>");
//            System.out.println("Opzioni: -rm -> copia un file da filesystem distribuito a locale");
//            System.out.println("Uso:");
//            System.out.println("cp -rm <remote_file_path> <local_path>");
//        }
        try {
            BufferedReader in = new BufferedReader(new FileReader("../manualDoc/" + cmdName + ".txt"));

            String line;
            System.out.println("");
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println("");
            in.close();
        }catch (FileNotFoundException e){
            utils.error_printer("Il comando digitato non esiste!");
        }
    }

}
