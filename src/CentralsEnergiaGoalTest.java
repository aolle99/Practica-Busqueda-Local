
import aima.search.framework.GoalTest;

public class CentralsEnergiaGoalTest implements GoalTest {
    public boolean isGoalState(Object state) {
        CentralsEnergiaBoard board = (CentralsEnergiaBoard) state;
        //return board.isGoal();
        return true;
    }

}
