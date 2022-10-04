import IA.Energia.Central;
import IA.Energia.Cliente;
import aima.search.eightpuzzle.EightPuzzleBoard;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class CentralsEnergiaSuccessorFunction1 implements SuccessorFunction {

    public List getSuccessors(Object state) {
        Set<Integer> consumidorsZero;
        ArrayList<Central> centrals;
        ArrayList<Cliente> clients;
        ArrayList successors = new ArrayList();
        CentralsEnergiaBoard board = (CentralsEnergiaBoard) state;

        //Treure un consumidor amb prioritat no garantizada d’una central
        //for (int i = 0; i < )
        return successors;

    }

    private void copyOf(EightPuzzleBoard board) {

    }

}
