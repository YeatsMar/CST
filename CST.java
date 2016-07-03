package cst;

import parser.Parser;
import parser.Result;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Scanner;

/**
 * Created by mayezhou on 16/7/2.
 */
public class CST {
    public ArrayList<Path> paths;
    private Parser parser;
    private Scanner scanner;
    private LinkedList<Node> premise;

    public CST() {
        parser = Parser.getParser();
        paths = new ArrayList<>();
        premise = new LinkedList<>();
    }

    public static void main(String[] args) {
        CST tree = new CST();
        tree.build("input/1.txt");
    }

    public void build(String filename) {
        try {
            scanner = new Scanner(new File(filename));
            String proposition = scanner.nextLine();
            Node root = new Node(proposition, false);
            Path p = new Path();
            p.add(root);
            paths.add(p);
            int n = scanner.nextInt();
            scanner.nextLine();
            for (int i = 0; i < n; i++) {
                premise.add(new Node(scanner.nextLine(), true));
            }
            grow();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void grow() {
        while (!isFinished()) {
            reduce();
            introduce();
        }
    }

    /**
     * introduce a new node which is one of premise into list
     */
    private void introduce() {
        if (premise.size() <= 0)
            return;
        Node a = premise.poll();
        for (Path p :
                paths) {
            p.add(a);
        }
    }

    private void reduce() {
        ListIterator<Path> pathListIterator = paths.listIterator();
        while (pathListIterator.hasNext()) {
            Path p = pathListIterator.next();
            if (p.contradictory)
                continue;
            Node e = p.get();
            if (e == null) //this path is finished
                return;
            if (!parser.isWellDefine(e.proposition)) {
                System.out.println("not well defined!");
                System.exit(0);
            }
            if (!e.used)
                System.out.println(e.getInfo());
            if (e.isReduced()) {
                e.used = true;
                continue;
            }
            if (!check(p, e)) {
                closePath();
                continue;
            }
            if (parser.isLetter(e.proposition)) {
                e.used = true;
                continue;
            }
            Node e1 = e.clone();
            p.add(e1);
            Result result = parser.split(e.proposition);
            if (parser.isUnaryOperator(e.proposition)) {
                Node subNode = new Node(result.operand1, !e.value);
                p.add(subNode);
            } else if (e.value) {//binary connective
                switch (result.operator) {
                    case "and":
                        p.add(new Node(result.operand1, true));
                        p.add(new Node(result.operand2, true));
                        break;
                    case "or":
                        Path bro = clonePath(p, pathListIterator);
                        p.add(new Node(result.operand1, true));
                        bro.add(new Node(result.operand2, true));
                        bro.processed = true;
                        break;
                    case "imply":
                        Path clonePath = clonePath(p, pathListIterator);
                        p.add(new Node(result.operand1, false));
                        clonePath.add(new Node(result.operand2, true));
                        clonePath.processed = true;
                        break;
                    case "eq":
                        Path clonepath = clonePath(p, pathListIterator);
                        p.add(new Node(result.operand1, true));
                        p.add(new Node(result.operand2, true));
                        clonepath.add(new Node(result.operand1, false));
                        clonepath.add(new Node(result.operand2, false));
                        clonepath.processed = true;
                        break;
                }
            } else {
                switch (result.operator) {
                    case "or":
                        p.add(new Node(result.operand1, false));
                        p.add(new Node(result.operand2, false));
                        break;
                    case "and":
                        Path bro = clonePath(p, pathListIterator);
                        p.add(new Node(result.operand1, false));
                        bro.add(new Node(result.operand2, false));
                        bro.processed = true;
                        break;
                    case "imply":
                        p.add(new Node(result.operand1, true));
                        p.add(new Node(result.operand2, false));
                        break;
                    case "eq":
                        Path clonepath = clonePath(p, pathListIterator);
                        p.add(new Node(result.operand1, true));
                        p.add(new Node(result.operand2, false));
                        clonepath.add(new Node(result.operand1, false));
                        clonepath.add(new Node(result.operand2, true));
                        clonepath.processed = true;
                        break;
                }
            }
            e.used = true;
            e1.setReduced(true);
        }
    }

    /**
     * check whether a path is finished
     */
    private boolean isFinished() {
        if (paths.size() <= 0)
            return true;
        boolean result = true;
        for (Path p :
                paths) {
            if (!p.isFinished()) {
                result = false;//as long as a path is not finished
                break;
            }
        }
        return result;
    }

    //used when an atomic tableau has two branches
    private Path clonePath(Path path, ListIterator<Path> pathListIterator) {
        Path another = path.clone();
        pathListIterator.add(another);
//        pathListIterator.previous();
        return another;
    }

    /**
     * check whether the node has a contradictory value on a path
     */
    private boolean check(Path path, Node node) {
        for (Node e :
                path.nodes) {
            if (e.proposition.equals(node.proposition)
                    && (e.value != node.value)) {
                path.contradictory = true;
                return false;
            }
        }
        return true;
    }

    private void closePath() {
        ListIterator<Path> pathIterator = paths.listIterator();
        while (pathIterator.hasNext()) {
            Path p = pathIterator.next();
            if (p.contradictory) {
                pathIterator.remove();
            }
        }
    }
}