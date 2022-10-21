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

    public Searcher(Board central, SuccessorFunction operators, HeuristicFunction heuristic, int debugLevel) {
        problem = new Problem(central, operators, new GoalTest(), heuristic);
        search = new HillClimbingSearch();
        debug = debugLevel;
    }

    public Searcher(Board central, SuccessorFunction operators, HeuristicFunction heuristic, int it, int pit, int k, double lbd) {
        problem = new Problem(central, operators, new GoalTest(), heuristic);
        search = new SimulatedAnnealingSearch(it, pit, k, lbd);
    }

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


    private static void printInstrumentation(Properties properties, int debug) {
        for (Object o : properties.keySet()) {
            String key = (String) o;
            String property = properties.getProperty(key);
            if (debug == 1) System.out.println(key + " : " + property);
            else System.out.println(property);
        }

    }

    private static void printActions(List actions) {
        for (Object o : actions) {
            String action = (String) o;
            System.out.println(action);
        }
    }

}
