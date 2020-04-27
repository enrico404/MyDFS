package Server;

import javax.management.NotificationEmitter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

import utils.utils;

/**
 * Classe che implementa un albero per la gestione del fileSystem distribuito. Contiene tutti i metodi principali per la gestione
 * dell'albero. Ogni nodo è una directory del filesystem.
 */
public class Tree implements Serializable {

    /**
     * Classe nodo. È l'elemento base dell'albero. Un nodo può essere interno oppure un nodo foglia.
     */
    public static class Node implements Serializable{
        /**
         * Percorso della directory
         */
        public String path;
        /**
         * Nome della directory. Esempio: /dirprova/prova   ha il nome "prova"
         */
        public String dirName;
        /**
         * Figli del nodo
         */
        public ArrayList<Node> childs = new ArrayList<>();


        /**
         * Costruttore con parametri della classe Node
         * @param Path percorso della directory
         * @param Name nome della directory
         */
        public Node(String Path, String Name){
            this.path = Path;
            this.dirName = Name;
        }
    }

    /**
     * radice dell'albero
     */
    private Node root;
    /**
     * Attributo contenente il nodo risultato di una ricerca se effettuata
     */
    private static Node searchResult = null;

    /**
     * Array contenente tutti i nodi dell'abero, serve per semplificare determinate operazioni
     */
    private ArrayList<String> dirs = new ArrayList<>();

    /**
     * Costruttore con parametri della classe Tree
     * @param Root radice dell'abero
     */
    public Tree(Node Root){
        root = Root;

    }

    /**
     * Getter della radice dell'albero
     * @return radice dell'albero
     */
    public Node getRoot(){return root;}

    /**
     * Metodo per inserire un nuovo nodo nell'albero
     * @param Path percorso del nuovo nodo
     * @param Name node del nodo
     */
    public void insert(String Path, String Name){
        Node newNode = new Node(Path, Name);
        //System.out.println("prima del find, sto cercando "+ Path+" "+ utils.pathWithoutLast(Path));
        if(!utils.contains(dirs, Path)) {
            Node parent = find(root, utils.pathWithoutLast(Path));
            //System.out.println("Dopo find, parent: "+parent.path);
            if (parent != null) {
                if (!utils.contains(parent.childs, newNode)) {
                    parent.childs.add(newNode);
                    dirs.add(Path);
                    // System.out.println("inserito nell'albero: " + Name + " percorso: " + Path);
                }
            }
        }
    }

    /**
     * Getter dell'array dirs
     * @return array contenente i nodi dell'albero
     */
    public ArrayList<String> getDirs(){
        return dirs;
    }

    /**
     * Interfaccia del metodo trasverse tree ricorsivo
     */
    public void trasverseTree(){
        trasverseTree(root);
        System.out.println("");
    }


    /**
     * Metodo interno ricorsivo per visitare l'albero
     * @param node nodo di partenza
     */
    private void trasverseTree(Node node){
        if(node != null){
            System.out.println("Path: "+node.path);
            for(Node el: node.childs){
                trasverseTree(el);
            }
        }
    }

    /**
     * Interfaccia pubblica del metodo deleteNode
     * @param path percorso del nodo da eliminare
     */
    public void deleteNode(String path){
        deleteNode_int(path);
    }

    /**
     * Metodo per eliminare un nodo dell'albero e tutti i suoi figli. Viene fatto anche l'update dell'attributo dirs
     * @param path percorso del nodo da cancellare
     */
    private void deleteNode_int(String path){
        //prendo il padre e cancello collegamento
        Node parent = find(root, utils.pathWithoutLast(path));
        if(parent != null){
            for(Node child: parent.childs){
                if(child.path.equals(path)){
                    parent.childs.remove(child);
                    updateDir();
                    break;
                }
            }
        }

//        System.out.println("------Dopo eliminazione-------");
//        trasverseTree();
//        System.out.println(dirs);
    }

    /**
     * interfaccia pubblica del metodo interno ricorsivo "init"
     */
    public void init(){
        dirs.clear();
        init(root);
    }

    /**
     * Metodo per inizializzare l'array dirs a partire da un nodo
     * @param node nodo di partenza su cui costruire l'array, di solito è la radice
     */
    private void init(Node node){
        if(node != null){
            dirs.add(node.path);
            for(Node el: node.childs){
                init(el);
            }
        }
    }

    /**
     * metodo per eseguire l'update dell'array "dirs"
     */
    public void updateDir(){
        init();
    }


    /**
     * Metodo per ricercare un nodo nell'albero in maniera ricorsiva. Si basa sul metodo "getNode"
     * @param rootNode nodo radice
     * @param Path percorso del nodo da cercare
     * @return nodo trovato, se vale null non è stato trovato niente
     */
    private Node find(Node rootNode, String Path){
        getNode(rootNode, Path);
        Node res = searchResult;
        searchResult = null;
        return res;

    }


    /**
     * Metodo che ritorna dell'albero dato il path
     * @param rootNode nodo radice da cui parte la ricerca
     * @param Path percorso del nodo da cercare
     */
    private void getNode(Node rootNode, String Path){
            //System.out.println(rootNode.path+" "+ Path);
            String tmp = rootNode.path.substring(0, rootNode.path.length()-1);
            // gestisco il / finale nel caso lo avessi, ci sono 4 casi differenti che posso avere
            if(rootNode.path.equals(Path) || rootNode.path.equals(Path.substring(0, Path.length()-1)) || tmp.equals(Path.substring(0, Path.length()-1)) || tmp.equals(Path)){
                searchResult = rootNode;
            }
            for(Node el: rootNode.childs){
                getNode(el, Path);
            }

    }


    /**
     * Metodo per controllare se due alberi sono uguali tra loro. Utilizzo il formato array per semplificare il confronto.
     * Vado a contare gli elementi uguali. Se il numero di elementi uguali è pari alla dimensione di entrambi gli array,
     * allora è possibile stabilire che gli array sono uguali.
     * @param fileSystemTree
     * @return
     */
    public boolean checkTree(Tree fileSystemTree){
        ArrayList<String> dirsExt = fileSystemTree.getDirs();
        int equalsEl = 0;
        for(int i=0; i<this.getDirs().size(); i++){
            for(int j=0; j<dirsExt.size(); j++){
                if(this.getDirs().get(i).equals(dirsExt.get(j))){
                    equalsEl ++;
                }
            }
        }

//        System.out.println("Eq:"+equalsEl);
//        System.out.println("serM size: "+this.getDirs().size());
//        System.out.println("Slave size: "+dirsExt.size());
        if(equalsEl == this.getDirs().size() && equalsEl == dirsExt.size()){
            return true;
        }
        else
            return false;

    }


    /**
     * Metodo che sposta internamente un nodo dell'albero
     * @param path1 percorso del nodo da spostare
     * @param path2 percorso finale, da cui ricavo il padre
     */
    public void move(String path1, String path2){
        Node parent_node2 = find(root, utils.pathWithoutLast(path2));

//        System.out.println("path1: "+path1+" path2: "+path2);
//        System.out.println("Nodi: node1 "+node1.path+ " parent_node2: "+parent_node2.path);
        Node parent_node1 = find(root, utils.pathWithoutLast(path1));
        //rimuovo il figlio
        if((parent_node1!= null) && (parent_node1.childs.size() > 0) ){
            for (Node child : parent_node1.childs) {
                if (child.path.equals(path1)) {
                    parent_node1.childs.remove(child);
                    //correggo il path del figlio
                    correct_path(parent_node2.path, child);
                    //aggiungo il nuovo figlio a node2
                    parent_node2.childs.add(child);
                    //    System.out.println("Ho rimosso: "+child.path+" da: "+parent_node1.path+" e l'ho messo come figlio a: "+parent_node2.path);
                    //faccio l'update della lista delle directory
                    updateDir();
                    break;
                }
            }
        }


//        System.out.println("------Dopo movimento-------");
//        trasverseTree();
//        System.out.println(dirs);

    }

    /**
     * Interfaccia pubblica del metodo "correct_path"
     * @param prefix prefisso da aggiungere
     * @param node riferimento al nodo
     */
    public void correct_path(String prefix, Node node){

        correct_path(node, prefix);
    }


    /**
     * Metodo che aggiunge un nuovo prefisso al nodo e a tutti i suoi figli
     * @param node nodo da modificare di partenza
     * @param path prefisso da aggiungere
     */
    private void correct_path(Node node, String path){
        if(path.equals("/")){
            path ="/"+ node.dirName;
        }else
            path += "/"+node.dirName;
        //System.out.println("setto il percorso: "+path);
        node.path = path;
        for(Node child: node.childs){
            correct_path(child, path);
        }
    }


}
