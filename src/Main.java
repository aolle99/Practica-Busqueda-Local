import aima.search.framework.HeuristicFunction;
import aima.search.framework.SuccessorFunction;

import java.util.Random;



public class Main {
    static final int MAX_TRIES = 10000;
    static Random myRandom;
    static CentralsEnergiaBoard board;

    public static void main(String[] args) {
        try {
            myRandom = new Random();
            board = new CentralsEnergiaBoard();
            board.generarCentrals(new int[]{5, 10, 25}, myRandom.nextInt());
            board.generarClients(1000, new double[]{0.25, 0.30, 0.45}, 0.75, myRandom.nextInt());
            boolean generat = false;
            int tries = 0;
            while (!generat && tries < MAX_TRIES) {
                generat = board.generarEstatInicial(0);
                tries++;
            }
            if (tries == MAX_TRIES) throw new Exception("No s'ha pogut generar l'estat inicial");
            board.printResultat();
            hillClimbing();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void hillClimbing() {
        //Declarem variables
        CentralsEnergiaSearcher searcher;
        HeuristicFunction heuristic = null;
        SuccessorFunction operators = new CentralsEnergiaSuccessorFunction();

        int heu = 5;
        switch (heu) {
            case 1 -> heuristic = new HeuristicFunction1();
            case 2 -> heuristic = new HeuristicFunction2();
            case 3 -> heuristic = new HeuristicFunction3();
            case 4 -> heuristic = new HeuristicFunction4();
            case 5 -> heuristic = new HeuristicFunction5();
        }
        try {
            searcher = new CentralsEnergiaSearcher(board, operators, heuristic);
            searcher.executeSearch();
        }
        catch (Exception e){
            System.err.println(e.getMessage());
        }
    }
}
