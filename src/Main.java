import aima.search.framework.HeuristicFunction;
import aima.search.framework.SuccessorFunction;

import java.util.Random;



public class Main {
    static final int MAX_TRIES = 100;
    static Random myRandom;
    static CentralsEnergiaBoard board;

    public static void main(String[] args) {
        try {
            myRandom = new Random();
            board = new CentralsEnergiaBoard();
            board.generarCentrals(new int[]{5, 10, 25},myRandom.nextInt());
            board.generarClients(1000, new double[]{0.25, 0.30, 0.45}, 0.75,myRandom.nextInt());
            boolean generat = false;
            int tries = 0;
            while (!generat && tries < MAX_TRIES) {
                generat = board.generarEstatInicial(0);
                tries++;
            }
            if (tries==MAX_TRIES) throw new Exception("No s'ha pogut generar l'estat inicial");
            hillClimbing();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void hillClimbing() {
        //Declarem variables
        CentralsEnergiaSearcher searcher;
        HeuristicFunction heuristic = null;
        SuccessorFunction operators = new CentralsEnergiaSuccessorFunction1();

        int heu = 0;
        switch (heu) {
            case 0:
                heuristic = new HeuristicFunction1();
                break;
            case 1:
                heuristic = new HeuristicFunction2();
                break;
            case 2:
                heuristic = new HeuristicFunction3();
                break;
            case 3:
                heuristic = new HeuristicFunction4();
                break;
            case 4:
                heuristic = new HeuristicFunction5();
                break;
            case 5:
                heuristic = new HeuristicFunction6();
                break;
        }

        try {
            //searcher = new CentralsEnergiaSearcher(board, operators, heuristic);
            //searcher.executeSearch();
        }
        catch (Exception e){
            System.err.println(e.toString());
        }
    }
}
