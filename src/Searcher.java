import aima.search.framework.*;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class Searcher {

    Problem problem;
    Search search;
    SearchAgent agent;
    long temps;

    int debug;

    /**
     * Constructor per al cas de Hill Climbing
     *
     * @param board      Estat inicial del problema
     * @param operators  Operadors del problema
     * @param heuristic  Heurística del problema
     * @param debugLevel Nivell de debug
     */
    public Searcher(Board board, SuccessorFunction operators, HeuristicFunction heuristic, int debugLevel) {
        problem = new Problem(board, operators, new GoalTest(), heuristic);
        search = new HillClimbingSearch();
        debug = debugLevel;
    }

    /**
     * Constructor per al cas de Simulated Annealing
     *
     * @param board     Estat inicial del problema
     * @param operators Operadors del problema
     * @param heuristic Heurística del problema
     * @param it        Número d'iteracions
     * @param pit       Número de passos per iteració
     * @param k         Temperatura inicial
     * @param lbd       Factor de reducció de la temperatura
     */
    public Searcher(Board board, SuccessorFunction operators, HeuristicFunction heuristic, int it, int pit, int k, double lbd) {
        problem = new Problem(board, operators, new GoalTest(), heuristic);
        search = new SimulatedAnnealingSearch(it, pit, k, lbd);
    }

    /**
     * S'encarrega de llençar la cerca i de mostrar els resultats
     */
    public void executeSearch() {

        try {
            Date d1, d2;
            Calendar a, b;

            d1 = new Date();
            agent = new SearchAgent(problem, search);
            d2 = new Date();
            a = Calendar.getInstance();
            b = Calendar.getInstance();
            a.setTime(d1);
            b.setTime(d2);

            temps = b.getTimeInMillis() - a.getTimeInMillis();

            if (debug == 3 || debug == 1) {
                if (debug == 1) System.out.println("Temps: " + temps + " ms");
                else System.out.println(temps);
            }

            if (debug == 4 || debug == 1) {
                if (debug == 1) printInstrumentation(agent.getInstrumentation(), 1);
                else printInstrumentation(agent.getInstrumentation(), 3);

            }
            //printActions(agent.getActions());
            Board board = (Board) search.getGoalState();
            board.printResultat(debug);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Imprimeix els resultats de la cerca
     *
     * @param properties Propietats de la cerca
     * @param debug      Nivell de debug
     */
    private static void printInstrumentation(Properties properties, int debug) {
        for (Object o : properties.keySet()) {
            String key = (String) o;
            String property = properties.getProperty(key);
            if (debug == 1) System.out.println(key + " : " + property);
            else System.out.println(property);
        }

    }

    /**
     * Imprimeix els passos de la cerca
     *
     * @param actions Llista d'accions
     */
    private static void printActions(List actions) {
        for (Object o : actions) {
            String action = (String) o;
            System.out.println(action);
        }
    }

}
