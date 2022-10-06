/*
 * Created on Sep 12, 2004
 *
 */

import aima.search.framework.HeuristicFunction;

/**
 * @author Ravi Mohan
 */
public class HeuristicFunction3 implements HeuristicFunction {

    public double getHeuristicValue(Object state) {
        CentralsEnergiaBoard board = (CentralsEnergiaBoard) state;
        return board.getMWEntropia();
    }
}