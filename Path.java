package cst;

import java.util.LinkedList;

/**
 * Created by mayezhou on 16/7/2.
 */
public class Path implements Cloneable {
    public LinkedList<Node> nodes;
    public boolean finished;
    public boolean contradictory;
    private int index;

    public Path() {
        nodes = new LinkedList<>();
        finished = false;
        contradictory = false;
        index = 0;
    }

    public Path clone() {
        Path newPath = new Path();
        newPath.nodes.addAll(this.nodes);
        newPath.index = this.index;
        return newPath;
    }

    public void add(Node e) {
        nodes.add(e);
    }

    public Node get() {
        if (index > nodes.size() - 1) {
            finished = true;
            return null;
        }
        return nodes.get(index++);
    }

    /**
     * check whether the node has a contradictory value on a path
     */
    public boolean check(Node node) {
        int max = nodes.indexOf(node);
        for (int i = 0; i < max; i++) {
            Node e = nodes.get(i);
            if (e.proposition.equals(node.proposition)
                    && (e.value != node.value)) {
                contradictory = true;
                return false;
            }
        }
        return true;
    }

    public boolean isFinished() {
        if (contradictory || index == nodes.size()) {
            finished = true;
        }
        return finished;
    }
}
