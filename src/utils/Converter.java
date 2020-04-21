package utils;

/**
 * Classe che si occupa di convertire valori in altre unità di misura
 */
public class Converter {
    static float gb_divisor = 1024*1024*1024;
    static float mega_divisor = 1024*1024;
    static float kilo_divisor = 1024;
    /**
     * Metodo per convertire i byte in gigabyte, se il valore in input è negativo si stampa un errore e si ritorna 0 per
     * sicurezza
     * @param value valore in input da convertire
     * @return valore convertito
     */
    public static float byte_to_human(float value){
        float result = 0;
        if(value >= 0)
            if(value > kilo_divisor && value <= mega_divisor) {
                result = value / kilo_divisor;
            }
            if(value > mega_divisor && value <= gb_divisor) {
                result = value / mega_divisor;
            }
            if(value > gb_divisor) {
                result = value / gb_divisor;
            }
        else
            utils.error_printer("Stai cercando di convertire un valore negativo!");
        return result;
    }

    /**
     * Simile al metodo precedente, ma ritorna una stringa con l'unità di misura al posto dei float,
     * per rendere la lettura migliore all'utente
     * @param value valore in input da convertire
     * @return valore convertito
     */
    public static String byte_to_humanS(float value){
        float result = 0;
        String res=value+"";
        if(value >= 0) {
            if (value > kilo_divisor && value <= mega_divisor) {
                result = value / kilo_divisor;
                res = String.format("%.4g",result) + " KB";
            }
            if (value > mega_divisor && value <= gb_divisor) {
                result = value / mega_divisor;
                res = String.format("%.4g",result )+ " MB";
            }
            if (value > gb_divisor) {
                result = value / gb_divisor;
                res = String.format("%.4g",result) + " GB";
            }
        }
        else
            utils.error_printer("Stai cercando di convertire un valore negativo!");
        return res;
    }

}
