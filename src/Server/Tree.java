package Server;

import javax.management.NotificationEmitter;
import java.io.Serializable;
import java.util.ArrayList;
import utils.utils;


public class Tree implements Serializable {

    public static class Node implements Serializable{
        public String path;
        public String dirName;
        public ArrayList<Node> childs = new ArrayList<>();

        public Node(String Path, String Name){
            this.path = Path;
            this.dirName = Name;
        }
    }


    private Node root;
    private static Node searchResult = null;
    private ArrayList<String> dirs = new ArrayList<>();

    public Tree(Node Root){
        root = Root;

    }

    public Node getRoot(){return root;}

    public void insert(String Path, String Name){
        Node newNode = new Node(Path, Name);
        //System.out.println("prima del find, sto cercando "+ Path+" "+ utils.pathWithoutLast(Path));

        Node parent = find(root, utils.pathWithoutLast(Path));
        //System.out.println("Dopo find, parent: "+parent.path);
        if(parent != null) {
            if (!utils.contains(parent.childs, newNode)) {
                parent.childs.add(newNode);
                dirs.add(Path);
                // System.out.println("inserito nell'albero: " + Name + " percorso: " + Path);
            }
        }
    }

    public ArrayList<String> getDirs(){
        return dirs;
    }


    public void trasverseTree(){
        trasverseTree(root);
        System.out.println("");
    }


    private void trasverseTree(Node node){
        if(node != null){
            System.out.println("Path: "+node.path);
            for(Node el: node.childs){
                trasverseTree(el);
            }
        }
    }

    public void deleteNode(String path){
        deleteNode_int(path);
    }

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


    public void init(){
        dirs.clear();
        init(root);
    }

    private void init(Node node){
        if(node != null){
            dirs.add(node.path);
            for(Node el: node.childs){
                init(el);
            }
        }
    }

    public void updateDir(){
        init();
    }


    private Node find(Node rootNode, String Path){
        getNode(rootNode, Path);
        Node res = searchResult;
        searchResult = null;
        return res;

    }


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


    public boolean checkTree(Tree fileSystemTree){
        ArrayList<String> dirsExt = fileSystemTree.getDirs();
        if(dirs.equals(dirsExt))
            return true;
        else
            return false;

    }


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
     * Aggiunge un nuovo prefisso al nodo e a tutti i suoi figli
     * @param prefix prefisso da aggiungere
     * @param node riferimento al nodo
     */
    public void correct_path(String prefix, Node node){

        correct_path(node, prefix);

    }

    private void correct_path(Node node, String path){
        path += "/"+node.dirName;
        //System.out.println("setto il percorso: "+path);
        node.path = path;
        for(Node child: node.childs){
            correct_path(child, path);
        }
    }


}
