import IA.Energia.Central;
import IA.Energia.Cliente;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class CentralsEnergiaSuccessorFunction implements SuccessorFunction {
    ArrayList<Central> centrals;
    ArrayList<Cliente> clients;
    ArrayList<Successor> successors;
    CentralsEnergiaBoard board;

    private void swapConsumidors() {
        for (int client1_id = 0; client1_id < clients.size(); ++client1_id) {
            int central1_id = board.getAssignacioCentral(client1_id);
            for (int client2_id = client1_id + 1; client2_id < clients.size(); ++client2_id) {
                int central2_id = board.getAssignacioCentral(client2_id);
                if (board.canSwap(client1_id, client2_id)) {
                    CentralsEnergiaBoard estat_successor = new CentralsEnergiaBoard(board);
                    estat_successor.swap(client1_id, client2_id);
                    String action = "Swap consumidor " + client1_id + " de la central " + central1_id + " amb consumidor " + client2_id + " de la central " + central2_id;
                    successors.add(new Successor(action, estat_successor));
                }
            }
        }
    }

    private void moveConsumidors() {
        for (int id_client = 0; id_client < clients.size(); ++id_client) {
            for (int id_central = 0; id_central < centrals.size(); ++id_central) {
                if (board.canMove(id_client, id_central)) {
                    CentralsEnergiaBoard estat_successor = new CentralsEnergiaBoard(board);
                    estat_successor.move(id_client, id_central);
                    String action = "Mou consumidor " + id_client + " a la central " + id_central;
                    successors.add(new Successor(action, estat_successor));
                }
            }
        }
    }

    public List<Successor> getSuccessors(Object state) {
        centrals = CentralsEnergiaBoard.getCentrals();
        clients = CentralsEnergiaBoard.getClients();
        successors = new ArrayList<>();
        board = (CentralsEnergiaBoard) state;
        Date d1, d2;
        Calendar a, b;

        d1 = new Date();
        //Swap de dos consumidors de central
        swapConsumidors();

        // Canviar un consumidor de central
        moveConsumidors();

        d2 = new Date();
        a = Calendar.getInstance();
        b = Calendar.getInstance();
        a.setTime(d1);
        b.setTime(d2);


        long temps = b.getTimeInMillis() - a.getTimeInMillis();
        System.out.println("Successors: " + successors.size() + " en " + temps + " ms");
        return successors;
    }
}
