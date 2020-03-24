package utils;

import java.io.Serializable;

/**
 * Classe personalizzata per la gestione dei file all'interno del file system distribuito
 */
public class MyFileType implements Serializable {
    private String name;
    private String type;
    private long size;
    private String location;

    /**
     * Costruttore con parametri della classe
     * @param Name nome del file
     * @param Type tipo del file: File/Dir
     * @param Size dimensione in bytes del file
     * @param Location percorso del file
     */
    public MyFileType(String Name, String Type, long Size, String Location){
        name = Name;
        type = Type;
        size = Size;
        location = Location;
    }
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



}
