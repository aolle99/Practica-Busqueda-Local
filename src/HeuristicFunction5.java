/*
 * Created on Sep 12, 2004
 *
 */
import aima.search.framework.HeuristicFunction;

/**
 * @author Ravi Mohan
 */
public class HeuristicFunction5 implements HeuristicFunction {
    /**
     * Obté l'energia perduda a causa de la distància
     *
     * @param state estat actual
     * @return energia perduda
     */
    public double getHeuristicValue(Object state) {
        Board board = (Board) state;
        return board.getEnergiaPerdudaPerDistancia();
    }
}