import IA.Energia.Central;
import IA.Energia.Cliente;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class HCSuccessorFunction implements SuccessorFunction {
    ArrayList<Central> centrals;
    ArrayList<Cliente> clients;
    ArrayList<Successor> successors;
    Board board;
    int debug = 0;

    HCSuccessorFunction(int debug) {
        this.debug = debug;
    }

    private void swapConsumidors() {
        for (int client1_id = 0; client1_id < clients.size(); ++client1_id) {
            for (int client2_id = client1_id + 1; client2_id < clients.size(); ++client2_id) {
                int central1_id = board.getAssignacioCentral(client1_id);
                int central2_id = board.getAssignacioCentral(client2_id);
                if (board.canSwap(client1_id, client2_id, central1_id, central2_id)) {
                    Board estat_successor = new Board(board);
                    estat_successor.swap(client1_id, client2_id, central1_id, central2_id);
                    String action = "Swap consumidor " + client1_id + " de la central " + central1_id + " amb consumidor " + client2_id + " de la central " + central2_id;
                    successors.add(new Successor(action, estat_successor));
                }
            }
        }
    }

    private void moveConsumidors() {
        for (int id_client = 0; id_client < clients.size(); ++id_client) {
            for (int id_central = 0; id_central < centrals.size(); ++id_central) {
                ferMoveConsumidors(id_client, id_central);
            }
            ferMoveConsumidors(id_client, centrals.size());
        }
    }

    private void ferMoveConsumidors(int id_client, int id_central) {
        int old_central = board.getAssignacioCentral(id_client);
        boolean canMove;
        if (id_central == centrals.size()) canMove = board.canMoveExclosa(id_client, old_central);
        else canMove = board.canMove(id_client, id_central, old_central);
        if (canMove) {
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
        board = (Board) state;
        Date d1 = null, d2;
        Calendar a, b;
        if (debug == 1) {
            d1 = new Date();
        }
        //Swap de dos consumidors de central
        swapConsumidors();

        // Canviar un consumidor de central
        moveConsumidors();
        if (debug == 1) {
            d2 = new Date();
            a = Calendar.getInstance();
            b = Calendar.getInstance();
            assert d1 != null;
            a.setTime(d1);
            b.setTime(d2);

            long temps = b.getTimeInMillis() - a.getTimeInMillis();

            System.out.println("S'han generat " + successors.size() + " successors en " + temps + " ms");
        }

        return successors;
    }
}
