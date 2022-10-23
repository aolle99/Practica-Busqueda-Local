/*
 * Created on Sep 12, 2004
 *
 */

import aima.search.framework.HeuristicFunction;

/**
 * @author Ravi Mohan
 */
public class HeuristicFunction3 implements HeuristicFunction {
    /**
     * Obte els MW obtinguts utilitzant la formula de l'entropia
     *
     * @param state Solució actual
     * @return MW obtinguts
     */
    public double getHeuristicValue(Object state) {
        Board board = (Board) state;
        return board.getMWEntropia();
    }
}