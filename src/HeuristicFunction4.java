/*
 * Created on Sep 12, 2004
 *
 */

import aima.search.framework.HeuristicFunction;

/**
 * @author Ravi Mohan
 */
public class HeuristicFunction4 implements HeuristicFunction {
    /**
     * Obté els MW ocupats aplicant un pes.
     *
     * @param state Solució actual
     * @return MW ocupats aplicant un pes
     */
    public double getHeuristicValue(Object state) {
        Board board = (Board) state;
        return -board.getMWOcupatsAmbPes();
    }

}