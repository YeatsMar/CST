package cst;

import parser.Parser;
import parser.Result;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by mayezhou on 16/7/2.
 */
public class CST {
    public ArrayList<Path> paths;
    private Parser parser;
    private Scanner scanner;
    private LinkedList<Node> premise;
    private PrintWriter output;

    public CST() {
        parser = Parser.getParser();
        paths = new ArrayList<>();
        premise = new LinkedList<>();
    }

    /**
     * initialize and grow, the only public api
     */
    public void build(String filename) {
        try {
            scanner = new Scanner(new File("input/" + filename));
            output = new PrintWriter(new File("output/" + filename));
            String proposition = scanner.nextLine();
            Node root = new Node(proposition, false);
            Path p = new Path();
            p.add(root);
            paths.add(p);
            int n = scanner.nextInt();//if no premise, will throw NoSuchElementException
            scanner.nextLine();
            for (int i = 0; i < n; i++) {
                premise.add(new Node(scanner.nextLine(), true));
            }
            grow();
            buildCounterexample();
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchElementException e) {
            grow();
            buildCounterexample();
            output.close();
        }
    }

    /**
     * key of building a CST
     */
    private void grow() {
        while (!isFinished()) {
            reduce();
            closePath();
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
            a = a.clone();
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
                output.println("not well defined!");
                output.close();
                System.exit(0);
            }
            if (!e.used)
                output.println(e.getInfo());
            if (e.isReduced()) {
                e.used = true;
                continue;
            }
            if (!p.check(e)) {
                e.used = true;
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
                        break;
                    case "imply":
                        Path clonePath = clonePath(p, pathListIterator);
                        p.add(new Node(result.operand1, false));
                        clonePath.add(new Node(result.operand2, true));
                        break;
                    case "eq":
                        Path clonepath = clonePath(p, pathListIterator);
                        p.add(new Node(result.operand1, true));
                        p.add(new Node(result.operand2, true));
                        clonepath.add(new Node(result.operand1, false));
                        clonepath.add(new Node(result.operand2, false));
                        break;
                    case "not":
                        p.add(new Node(result.operand1, !e.value));
                        break;
                    default:
                        System.out.println("Proposition parsing error!");
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
                        break;
                    case "not":
                        p.add(new Node(result.operand1, !e.value));
                        break;
                    default:
                        System.out.println("Proposition parsing error!");
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

    /**
     * used when an atomic tableau has two branches
     */
    private Path clonePath(Path path, ListIterator<Path> pathListIterator) {
        Path another = path.clone();
        pathListIterator.add(another);
//        pathListIterator.previous();
        return another;
    }

    /**
     * remove contradictory paths
     */
    private void closePath() {
        ListIterator<Path> pathIterator = paths.listIterator();
        while (pathIterator.hasNext()) {
            Path p = pathIterator.next();
            if (p.contradictory) {
                pathIterator.remove();
            }
        }
    }

    /**
     * Bonus here: print T nodes on each contradictory path
     */
    private void buildCounterexample() {
        if (paths.size() > 0)
            output.println("\n\n\nWe can build counterexamples as follows:");
        int i = 0;
        for (Path path :
                paths) {
            if (path.contradictory)
                continue;
            output.println("Case " + (i++) + ":");
            LinkedHashSet<String> trueNodes = new LinkedHashSet<>();
            for (Node e :
                    path.nodes) {
                if (parser.isLetter(e.proposition)
                        && e.value)
                    trueNodes.add(e.proposition);
            }
            for (String e :
                    trueNodes) {
                output.println(e);
            }
        }
        if (i == 0)
            output.println("Valid, so no counterexample.");
    }
}
