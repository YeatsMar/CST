package cst;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Created by mayezhou on 16/7/2.
 */
public class Path implements Cloneable{
    public LinkedList<Node> nodes;
    public boolean processed;
    public boolean finished;
    public boolean contradictory;
    private int index;
//    private ListIterator

    public Path() {
        nodes = new LinkedList<>();
        processed = false;
        finished = false;
        contradictory = false;
//        iterator = nodes.listIterator();
    }

    public Path clone() {
        Path newPath = new Path();
        for (Node e:
             this.nodes) {
            newPath.add(e);
        }
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
        return nodes.get(index++);
    }

    public boolean isFinished() {
        if (contradictory || nodes.size() <= 0) {
            finished = true;
        }
        return finished;
    }
}
