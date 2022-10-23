/*
 * Created on Sep 12, 2004
 *
 */

import aima.search.framework.HeuristicFunction;

/**
 * @author Ravi Mohan
 */
public class HeuristicFunction2 implements HeuristicFunction {
    /**
     * Retorna els MW lliures de les centrals
     *
     * @param state Solució actual
     * @return MW lliures de les centrals
     */
    public double getHeuristicValue(Object state) {
        Board board = (Board) state;
        return board.getTotalMWLliures();
    }

}