import aima.search.framework.HeuristicFunction;
import aima.search.framework.SuccessorFunction;

import java.util.Random;



public class Main {
    static Random myRandom;
    static CentralsEnergiaBoard board;

    public static void main(String[] args) {
        int i=1000;
        while(i>0){
            i--;
            try {
                hillClimbing();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void hillClimbing() throws Exception {
        //Declarem variables
        CentralsEnergiaSearcher searcher;
        SuccessorFunction operators = null;
        HeuristicFunction heuristic = null;

        myRandom = new Random();
        board = new CentralsEnergiaBoard();
        board.generarCentrals(new int[]{1, 2, 3});
        board.generarClients(1000, new double[]{0.25, 0.30, 0.45}, 0.75);
        boolean generat = false;
        while (!generat) {
            generat = board.generarEstatInicial(1);
        }

        int op = myRandom.nextInt(3);
        switch (op) {
            case 0:
                operators = new CentralsEnergiaSuccessorFunction1();
                break;
            case 1:
                operators = new CentralsEnergiaSuccessorFunction2();
                break;
            case 2:
                operators = new CentralsEnergiaSuccessorFunction3();
                break;
        }

        int heu = myRandom.nextInt(6);
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
            searcher = new CentralsEnergiaSearcher(board, operators, heuristic);
            searcher.executeSearch();
        }
        catch (Exception e){
            System.err.println(e.toString());
        }
    }
}