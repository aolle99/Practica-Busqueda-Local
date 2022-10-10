import IA.Energia.Central;
import IA.Energia.Cliente;
import IA.Energia.VEnergia;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.List;


public class CentralsEnergiaSuccessorFunction implements SuccessorFunction {
    ArrayList<Central> centrals;
    ArrayList<Cliente> clients;
    ArrayList<Successor> successors;
    CentralsEnergiaBoard board;

    private void swapConsumidors() {
        for (int id_client1 = 0; id_client1 < clients.size(); ++id_client1) {
            Cliente client1 = clients.get(id_client1);
            if (!board.assignedToZero(id_client1)) {
                int id_central1 = board.getCentralAssignada(id_client1);
                for (int id_client2 = id_client1 + 1; id_client2 < clients.size(); ++id_client2) {
                    Cliente client2 = clients.get(id_client2);
                    if (!board.assignedToZero(id_client2)) {
                        int id_central2 = board.getCentralAssignada(id_client2);
                        if (id_central1 != id_central2 && id_central1 != -1 && id_central2 != -1) {
                            CentralsEnergiaBoard estat_successor = new CentralsEnergiaBoard(board);
                            Central central1 = centrals.get(id_central1);
                            Central central2 = centrals.get(id_central2);
                            //Recalcula els MW lliures de la central
                            double mwLliures1 = estat_successor.getMwLliuresCentrals().get(id_central1);
                            double mwLliures2 = estat_successor.getMwLliuresCentrals().get(id_central2);
                            mwLliures1 += client1.getConsumo() + client1.getConsumo() * VEnergia.getPerdida(CentralsEnergiaBoard.getDistancia(client1, central1));
                            mwLliures2 += client2.getConsumo() + client2.getConsumo() * VEnergia.getPerdida(CentralsEnergiaBoard.getDistancia(client2, central2));
                            mwLliures1 -= client2.getConsumo() + client2.getConsumo() * VEnergia.getPerdida(CentralsEnergiaBoard.getDistancia(client2, central1));
                            mwLliures2 -= client1.getConsumo() + client1.getConsumo() * VEnergia.getPerdida(CentralsEnergiaBoard.getDistancia(client1, central2));
                            if (mwLliures1 >= 0 && mwLliures2 >= 0) {
                                estat_successor.getMwLliuresCentrals().set(id_central1, mwLliures1);
                                estat_successor.getMwLliuresCentrals().set(id_central2, mwLliures2);
                                estat_successor.getAssignacionsConsumidors().set(id_client1, id_central2);
                                estat_successor.getAssignacionsConsumidors().set(id_client2, id_central1);

                                String action = "Swap consumidor " + id_client1 + " de la central " + id_central1 + " amb consumidor " + id_client2 + " de la central " + id_central2;
                                successors.add(new Successor(action, estat_successor));
                            }
                        }
                    }
                }
            }
        }
    }

    private void moveConsumidors() {
        for (int id_client = 0; id_client < clients.size(); ++id_client) {
            Cliente client = clients.get(id_client);
            int id_central = board.getCentralAssignada(id_client);
            for (int id_central2 = 0; id_central2 <= centrals.size(); ++id_central2) {
                if (id_central2 == centrals.size() && id_central != -1) { // MOURE CLIENT FORA (assignar-li zero MW)
                    if (client.getContrato() == Cliente.NOGARANTIZADO && !board.assignedToZero(id_client)) { //Comprovem que el consumidor no estigui garantitzat
                        CentralsEnergiaBoard estat_successor = new CentralsEnergiaBoard(board);
                        Central central = centrals.get(id_central);
                        //Recalcula els MW lliures de la central
                        double mwLliures = estat_successor.getMwLliuresCentrals().get(id_central);
                        mwLliures += client.getConsumo() + client.getConsumo() * VEnergia.getPerdida(CentralsEnergiaBoard.getDistancia(client, central));
                        // Actualitza els MW lliures de la central i l'assignació del consumidor
                        estat_successor.getMwLliuresCentrals().set(id_central, mwLliures);
                        estat_successor.getAssignacionsConsumidors().set(id_client, -1);
                        estat_successor.setNoAssignat(id_client);
                        // creem el successor
                        String action = "Treure consumidor " + id_client + " de la central " + id_central;
                        successors.add(new Successor(action, estat_successor));
                    }
                } else { // MOURE CLIENT DE CENTRAL
                    if (id_central != id_central2 && id_central != -1 && id_central2 != -1) {
                        CentralsEnergiaBoard estat_successor = new CentralsEnergiaBoard(board);
                        Central central = centrals.get(id_central);
                        Central central2 = centrals.get(id_central2);
                        // Recalcula els MW lliures de les centrals
                        double mwLliuresOld = estat_successor.getMwLliuresCentrals().get(id_central);
                        double mwLliuresNew = estat_successor.getMwLliuresCentrals().get(id_central2);
                        mwLliuresOld += client.getConsumo() + client.getConsumo() * VEnergia.getPerdida(CentralsEnergiaBoard.getDistancia(client, central));
                        mwLliuresNew -= client.getConsumo() + client.getConsumo() * VEnergia.getPerdida(CentralsEnergiaBoard.getDistancia(client, central2));
                        if (mwLliuresNew >= 0) {
                            // Actualitza els MW lliures de les centrals i l'assignació del consumidor
                            estat_successor.getMwLliuresCentrals().set(id_central, mwLliuresOld);
                            estat_successor.getMwLliuresCentrals().set(id_central2, mwLliuresNew);
                            estat_successor.getAssignacionsConsumidors().set(id_client, id_central2);

                            // creem el successor
                            String action = "Canviar consumidor " + id_client + " de la central " + id_central + " a la central " + id_central2;
                            successors.add(new Successor(action, estat_successor));
                        }
                    }
                }
            }
        }
    }

    public List getSuccessors(Object state) {
        centrals = CentralsEnergiaBoard.getCentrals();
        clients = CentralsEnergiaBoard.getClients();
        successors = new ArrayList<>();
        board = (CentralsEnergiaBoard) state;

        //Swap de dos consumidors de central
        swapConsumidors();

        // Canviar un consumidor de central
        moveConsumidors();
        System.out.println("Successors: " + successors.size());
        return successors;
    }
}
