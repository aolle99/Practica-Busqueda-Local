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

    public Searcher(Board central, SuccessorFunction operators, HeuristicFunction heuristic) {
        problem = new Problem(central, operators, new GoalTest(), heuristic);
        search = new HillClimbingSearch();
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
            agent = new SearchAgent(problem,search);
            d2=new Date();
            a = Calendar.getInstance();
            b = Calendar.getInstance();
            a.setTime(d1);
            b.setTime(d2);

            temps = b.getTimeInMillis() - a.getTimeInMillis();

            System.out.println(temps);

            //printInstrumentation(agent.getInstrumentation());
            //printActions(agent.getActions());
            Board board = (Board) search.getGoalState();
            board.printResultat();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private static void printInstrumentation(Properties properties) {
        for (Object o : properties.keySet()) {
            String key = (String) o;
            String property = properties.getProperty(key);
            System.out.println(key + " : " + property);
        }

    }

    private static void printActions(List actions) {
        for (Object o : actions) {
            String action = (String) o;
            System.out.println(action);
        }
    }

}
