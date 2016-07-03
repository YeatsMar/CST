package cst;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Created by mayezhou on 16/7/2.
 */
public class Path implements Cloneable {
    public LinkedList<Node> nodes;
    public boolean processed;
    public boolean finished;
    public boolean contradictory;
    private int index;
    private ListIterator<Node> nodeListIterator;
    private LinkedHashSet<Node> reducesNodes;

    public Path() {
        nodes = new LinkedList<>();
        processed = false;
        finished = false;
        contradictory = false;
        nodeListIterator = nodes.listIterator();
        index = 0;
        reducesNodes = new LinkedHashSet<>();
    }

    public Path clone() {
        Path newPath = new Path();
        newPath.nodes.addAll(this.nodes);
        newPath.reducesNodes.addAll(this.reducesNodes);
        return newPath;
    }

    public void add(Node e) {
        nodes.add(e);
    }

    public Node get() {
        if (nodes.size() < 1) {
            finished = true;
            return null;
        }
        Node result = nodes.poll();
        reducesNodes.add(result);
        return result;
    }

    public boolean check(Node node) {
        for (Node e :
                reducesNodes) {
            if (e.proposition.equals(node.proposition)
                    && (e.value != node.value)) {
                contradictory = true;
                return false;
            }
        }
        return true;
    }

    public boolean isFinished() {
        if (contradictory || nodes.size() <= 0) {
            finished = true;
        }
        return finished;
    }
}
