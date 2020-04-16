package Server;

import java.io.Serializable;
import java.util.ArrayList;

public class Tree implements Serializable {
    static class Node {
        String path;
        String dirName;
        ArrayList<Node> childs = new ArrayList<>();

        public Node(String Path, String Name){
            this.path = path;
            this.dirName = Name;
        }
    }

    public void insert(Node node, String Path, String Name){
        Node newNode = new Node(Path, Name);
        node.childs.add(newNode);
        System.out.println("inserito nell'albero: "+ Name+" percorso: "+Path);
    }

    public void trasverseTree(Node node){
        if(node != null){
            System.out.println("Nome: "+ node.dirName+ " Path: "+node.path);
            for(Node el: node.childs){
                trasverseTree(el);
            }
        }
    }


}
