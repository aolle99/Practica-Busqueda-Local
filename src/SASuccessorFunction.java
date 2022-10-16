import IA.Energia.Central;
import IA.Energia.Cliente;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class SASuccessorFunction implements SuccessorFunction {
    ArrayList<Central> centrals;
    ArrayList<Cliente> clients;
    ArrayList<Successor> successors;
    Board board;

    private static Random myRandom;

    private void swapConsumidors() {
        int client1_id = myRandom.nextInt(clients.size());
        int client2_id = myRandom.nextInt(clients.size());
        int central1_id = board.getAssignacioCentral(client1_id);
        int central2_id = board.getAssignacioCentral(client2_id);
        if (board.canSwap(client1_id, client2_id, central1_id, central2_id)) {
            Board estat_successor = new Board(board);
            estat_successor.swap(client1_id, client2_id, central1_id, central2_id);
            String action = "Swap consumidor " + client1_id + " de la central " + central1_id + " amb consumidor " + client2_id + " de la central " + central2_id;
            successors.add(new Successor(action, estat_successor));
        }
    }

    private void moveConsumidors() {
        int id_client = myRandom.nextInt(clients.size());
        int id_central = myRandom.nextInt(centrals.size());
        int old_central = board.getAssignacioCentral(id_client);
        if (board.canMove(id_client, id_central, old_central)) {
            Board estat_successor = new Board(board);
            estat_successor.move(id_client, id_central, old_central);
            String action = "Mou consumidor " + id_client + " a la central " + id_central;
            successors.add(new Successor(action, estat_successor));
        }
    }

    public List<Successor> getSuccessors(Object state) {
        centrals = Board.getCentrals();
        clients = Board.getClients();
        successors = new ArrayList<>();
        myRandom = new Random();
        board = (Board) state;
        int operator = myRandom.nextInt(2);

        while (successors.isEmpty()) {
            if (operator == 0) {
                //Swap de dos consumidors de central
                swapConsumidors();
            } else {
                // Canviar un consumidor de central
                moveConsumidors();
            }
        }
        return successors;
    }
}
