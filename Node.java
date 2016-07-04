package cst;

/**
 * Created by mayezhou on 16/7/2.
 */
public class Node {
    public String proposition;
    private boolean reduced;
    public boolean value;
    public boolean used;

    public Node(String proposition, boolean value) {
        this.proposition = proposition;
        this.value = value;
        reduced = false;
        used = false;
    }

    //key: used attribute should be separate
    public Node clone() {
        Node ano = new Node(this.proposition, this.value);
        ano.reduced = this.reduced;
        return ano;
    }

    public String getInfo() {
        String result = "";
        if (value) {
            result += "T";
        } else {
            result += "F";
        }
        result += "\t"+proposition;
        return result;
    }

    public boolean isReduced() {
        return reduced;
    }

    public void setReduced(boolean reduced) {
        this.reduced = reduced;
    }
}
