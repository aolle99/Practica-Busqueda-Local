/*
 * Created on Sep 12, 2004
 *
 */

import aima.basic.XYLocation;
import aima.search.eightpuzzle.EightPuzzleBoard;
import aima.search.framework.HeuristicFunction;

/**
 * @author Ravi Mohan
 */
public class HeuristicFunction2 implements HeuristicFunction {

    public double getHeuristicValue(Object state) {

        CentralsEnergiaBoard board = (CentralsEnergiaBoard) state;
        return board.getMWLliures();
    }

}