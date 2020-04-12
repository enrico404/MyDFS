package utils;

import java.io.Serializable;

/**
 * Classe personalizzata per la gestione dei file all'interno del file system distribuito
 */
public class MyFileType implements Serializable {
    /**
     * nome del file
     */
    private String name;
    /**
     * tipo del file (File/Dir)
     */
    private String type;
    /**
     * dimensione in byte del file
     */
    private long size;
    /**
     * locazione fisica del file, contiene il nome del data nodes in cui si trova
     */
    private String location;

    /**
     * Percorso assoluto al file
     */
    private String path;

    /**
     * Costruttore con parametri della classe
     * @param Name nome del file
     * @param Type tipo del file: File/Dir
     * @param Size dimensione in bytes del file
     * @param Location percorso del file
     */
    public MyFileType(String Name, String Type, long Size, String Location, String Path){
        name = Name;
        type = Type;
        size = Size;
        location = Location;
        path = Path;
    }

    /**
     * Getter dell'attributo name
     * @return stringa contente il nome del file
     */
    public String getName() {
        return name;
    }

    /**
     * Setter dell'attributo name
     * @param name nome del file
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter dell'attributo tipo
     * @return stringa contenente il tipo di file
     */
    public String getType() {
        return type;
    }

    /**
     * Setter dell'attributo type
     * @param type Stringa contenente il tipo di file: Dir/File
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Getter dell'attributo size
     * @return dimensione in bytes del file
     */
    public long getSize() {
        return size;
    }

    /**
     * Setter dell'attributo size
     * @param size dimensione del file
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * Getter dell'attributo location
     * @return locazione del file
     */
    public String getLocation() {
        return location;
    }

    /**
     * Setter dell'attributo location
     * @param location stringa contenente la locazione del file
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Getter dell'attributo path
     * @return stringa contenente il percorso assoluto al file
     */
    public String getPath(){
        return path;
    }



}
