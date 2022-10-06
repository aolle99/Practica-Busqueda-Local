import IA.Energia.Central;
import IA.Energia.Cliente;
import IA.Energia.VEnergia;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.List;


public class CentralsEnergiaSuccessorFunction implements SuccessorFunction {

    public List getSuccessors(Object state) {
        ArrayList<Central> centrals = CentralsEnergiaBoard.getCentrals();
        ArrayList<Cliente> clients = CentralsEnergiaBoard.getClients();
        ArrayList<Successor> successors = new ArrayList<>();
        CentralsEnergiaBoard board = (CentralsEnergiaBoard) state;


        //Treure un consumidor amb prioritat no garantizada d’una central
        /*for (int id_client = 0; id_client < clients.size(); ++id_client) {
            Cliente client = clients.get(id_client);
            if (client.getContrato() == Cliente.NOGARANTIZADO && !board.isNoAssignat(id_client)) {
                int id_central = board.getCentralAssignada(id_client);
                CentralsEnergiaBoard estat_successor = new CentralsEnergiaBoard(board);
                Central central = centrals.get(id_central);
                double mwLliures = estat_successor.getMwLliuresCentrals().get(id_central);
                mwLliures += client.getConsumo() + client.getConsumo() * VEnergia.getPerdida(CentralsEnergiaBoard.getDistancia(client, central));
                estat_successor.getMwLliuresCentrals().set(id_central, mwLliures);
                estat_successor.getAssignacionsConsumidors().set(id_client, -1);
                estat_successor.setNoAssignat(id_client);
                String action = "Treure consumidor " + id_client + " de la central " + id_central;
                successors.add(new Successor(action,estat_successor));
            }
        } */

        //Swap de dos consumidors de central
        for (int id_client1 = 0; id_client1 < clients.size(); ++id_client1) {
            Cliente client1 = clients.get(id_client1);
            if (!board.isNoAssignat(id_client1)) {
                int id_central1 = board.getCentralAssignada(id_client1);
                for (int id_client2 = id_client1 + 1; id_client2 < clients.size(); ++id_client2) {
                    Cliente client2 = clients.get(id_client2);
                    if (!board.isNoAssignat(id_client2)) {
                        int id_central2 = board.getCentralAssignada(id_client2);
                        if (id_central1 != id_central2) {
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

        // Canviar un consumidor de central
        for (int id_client = 0; id_client < clients.size(); ++id_client) {
            Cliente client = clients.get(id_client);
            if (!board.isNoAssignat(id_client)) {
                int id_central = board.getCentralAssignada(id_client);
                for (int id_central2 = 0; id_central2 <= centrals.size(); ++id_central2) {
                    if (id_central2 == centrals.size()) { // CENTRAL NO ASSIGNADA
                        if (client.getContrato() == Cliente.NOGARANTIZADO && !board.isNoAssignat(id_client)) { //Comprovem que el consumidor no estigui garantitzat
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
                    } else { // CENTRAL CONCRETA
                        if (id_central != id_central2) {
                            CentralsEnergiaBoard estat_successor = new CentralsEnergiaBoard(board);
                            Central central = centrals.get(id_central);
                            Central central2 = centrals.get(id_central2);
                            // Recalcula els MW lliures de les centrals
                            double mwLliures = estat_successor.getMwLliuresCentrals().get(id_central);
                            double mwLliures2 = estat_successor.getMwLliuresCentrals().get(id_central2);
                            mwLliures += client.getConsumo() + client.getConsumo() * VEnergia.getPerdida(CentralsEnergiaBoard.getDistancia(client, central));
                            mwLliures2 -= client.getConsumo() + client.getConsumo() * VEnergia.getPerdida(CentralsEnergiaBoard.getDistancia(client, central2));
                            if (mwLliures2 >= 0) {
                                // Actualitza els MW lliures de les centrals i l'assignació del consumidor
                                estat_successor.getMwLliuresCentrals().set(id_central, mwLliures);
                                estat_successor.getMwLliuresCentrals().set(id_central2, mwLliures2);
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
        return successors;

    }

}
