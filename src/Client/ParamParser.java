package Client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe che si occupa del parsing dei parametri dei vari comandi
 */
public class ParamParser {

    /**
     * Metodo principale di parsing, in particolare data in input una qualsiasi stringa restituisce un array contenente
     * solamente i vari parametri. Ad esempio "cp -rm -r source dest" restituisce ["m","r"]  ordinato in maniera alfabetica
     *
     * @param cmd stringa comando
     * @return array di caratteri ordinato in maniera alfabetica
     */
    private static ArrayList<String> parse(String cmd) {
        //la regex è giusta
        Pattern pattern = Pattern.compile("(-[a-z]*)");
        Matcher matcher = pattern.matcher(cmd);
        String options = "";
        ArrayList<String> separatedOptions = new ArrayList<>();
        String[] param;
        while (matcher.find()) {
            options+=matcher.group(0);
        }
        param = options.split("[-]");

        String[] everyChar;
        for (int i = 0; i < param.length; i++) {
            everyChar = param[i].split("");
            for (int j = 0; j < everyChar.length; j++) {
                if (!everyChar[j].equals(" "))
                    separatedOptions.add(everyChar[j]);

            }
        }
        //ordino i parametri in maniera alfabetica
        separatedOptions.sort(new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return s.compareTo(t1);
            }
        });

        return separatedOptions;
    }

    /**
     * Metodo che ritorna il numero dei gruppi di parametri dato un comando. Ad es: ls -lh -d ritornerà 2, perchè sono
     * stati specificati esplicitamente due gruppi. Questo metodo è utile nei metodi che utilizzano un numero di path variabile
     * @param cmd stringa contenente il comando completo
     * @return numero di gruppi di parametri
     */
    public static int getParamNumbers(String cmd){
        //la regex è giusta
        Pattern pattern = Pattern.compile("(-[a-z]*)");
        Matcher matcher = pattern.matcher(cmd);
        String options = "";
        String[] param;
        while (matcher.find()) {
            options+=matcher.group(0);
        }
        param = options.split("[-]");

        return param.length;
    }

    /**
     * Interfaccia pubblica della classe, data una stringa comando e una stringa di opzioni da cercare, restituisce true
     * se le opzioni specificate sono all'interno del comando
     *
     * @param command      stringa comando
     * @param optionString stringa opzioni specificata
     * @return true se le opzioni specificate sono all'interno del comando
     */
    public static boolean checkParam(String command, String optionString) {

        //parso il comando dato in input
        ArrayList<String> orderedOptions = parse(command);
        ArrayList<String> optionToCheck = parse(optionString);

//            System.out.println("command: ");
//            for(String el:orderedOptions){
//                System.out.println(el);
//            }
//            System.out.println("option to check: ");
//            for(String el:optionToCheck){
//                System.out.println(el);
//            }

        if (orderedOptions.equals(optionToCheck))
            return true;
        return false;

    }


}
