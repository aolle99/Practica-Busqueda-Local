import aima.search.framework.*;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;

import java.util.*;

public class CentralsEnergiaSearcher {

    Problem problem;
    Search search;
    SearchAgent agent;
    String propietats;
    String accions;
    long temps;
    String nodesexp;

    public CentralsEnergiaSearcher(CentralsEnergiaBoard central, SuccessorFunction operators, HeuristicFunction heuristic){
        problem = new Problem(central, operators, new CentralsEnergiaGoalTest(), heuristic);
        search = new HillClimbingSearch();
    }

    public CentralsEnergiaSearcher(CentralsEnergiaBoard central, CentralsEnergiaSuccessorFunction operators, HeuristicFunction heuristic, int it, int pit, int k, double lbd) {
        problem = new Problem(central, operators, new CentralsEnergiaGoalTest(), heuristic);
        search = new SimulatedAnnealingSearch(it, pit, k, lbd);
    }

    public void executeSearch(){

        try {
            Date d1,d2;
            Calendar a,b;

            d1=new Date();
            agent = new SearchAgent(problem,search);
            d2=new Date();

            a= Calendar.getInstance();
            b= Calendar.getInstance();
            a.setTime(d1);
            b.setTime(d2);

            temps=b.getTimeInMillis()-a.getTimeInMillis();

            System.out.println(temps+" ms");

            printInstrumentation(agent.getInstrumentation());
            printActions(agent.getActions());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setInstrumentation(Properties properties)
    {
        propietats = "";
        propietats += "Temps de cerca: "+temps+" ms\n";
        Iterator keys = properties.keySet().iterator();
        if (keys.hasNext()) {
            String key = (String) keys.next();
            String property = properties.getProperty(key);
            propietats += "Nodes expandits: "+property+"\n";
            nodesexp=property;
        }
    }
    public String getPropietats(){
        return propietats;
    }

    private void setAccions(List actions)
    {
        accions = "";
        for (Object o : actions) {
            String action = (String) o;
            accions = accions.concat(action + "\n");
        }
    }

    private static void printInstrumentation(Properties properties) {
        Iterator keys = properties.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String property = properties.getProperty(key);
            System.out.println(key + " : " + property);
        }

    }

    private static void printActions(List actions) {
        for (int i = 0; i < actions.size(); i++) {
            String action = (String) actions.get(i);
            System.out.println(action);
        }
    }

    /*public void fitxerResultats(ConnectatBoard board,int estat_inicial,String algoritme,int operadors,int heuristic,int k,int iter,int passos_iter, double lambda){
        try {
            File fitxer = new File("resultats.txt");
            boolean nou=!fitxer.exists();
            fitxer.createNewFile();
            FileWriter out = new FileWriter(fitxer,true);
            if (nou){
                //noms de variables
                out.write("N\tM\tncentrals\tnrepetidors\testat_inicial\tmaxrep\talpha\tbeta\tgamma\talgoritme\toperadors\theuristic\tk\titer\tpassos_iter\tlambda\ttemps\tnodes_exp\terror_inicial\terror_final\trepet_usats\n");
            }
            ConnectatBoard board_final = getEstatFinal();
            if (k==-1){
                // Ser� HillClimbing, no volem variables de SA
                out.write(""+board.getN()+"\t"+board.getM()+"\t"+board.getNCentrals()+"\t"+board.getNRepetidors()+"\t"+estat_inicial+"\t"+board.getMaxNumRepetidors()+"\t"+board.getAlfa()+"\t"+board.getBeta()+"\t"+board.getGamma()+"\t"+algoritme+"\t"+operadors+"\t"+heuristic+"\t\t\t\t\t"+temps+"\t"+nodesexp+"\t"+board.getErrorTotal()+"\t"+board_final.getErrorTotal()+"\t"+board_final.getNumRepetidors()+"\n");
            }
            else
                out.write(""+board.getN()+"\t"+board.getM()+"\t"+board.getNCentrals()+"\t"+board.getNRepetidors()+"\t"+estat_inicial+"\t"+board.getMaxNumRepetidors()+"\t"+board.getAlfa()+"\t"+board.getBeta()+"\t"+board.getGamma()+"\t"+algoritme+"\t"+operadors+"\t"+heuristic+"\t"+k+"\t"+iter+"\t"+passos_iter+"\t"+lambda+"\t"+temps+"\t"+nodesexp+"\t"+board.getErrorTotal()+"\t"+board_final.getErrorTotal()+"\t"+board_final.getNumRepetidors()+"\n");

            out.close();
        } catch (Exception e){
            System.err.println("No s'ha pogut escriure el fitxer de resultats.");
            System.err.println(e.toString());
        }
    }*/

}
