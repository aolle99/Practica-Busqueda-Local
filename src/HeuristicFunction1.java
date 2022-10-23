/*
 * Created on Sep 12, 2004
 *
 */

import aima.search.framework.HeuristicFunction;

/**
 * @author Ravi Mohan
 */
public class HeuristicFunction1 implements HeuristicFunction {
    /**
     * Retorna el benefici obtingut per la solució actual
     *
     * @param state Solució actual
     * @return Benefici obtingut
     */
    public double getHeuristicValue(Object state) {
        Board board = (Board) state;
        return -board.getBenefici();
    }

}