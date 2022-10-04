import IA.Energia.*;

import java.util.*;

public class CentralsEnergiaBoard {

    private static ArrayList<Integer> assignacionsConsumidors;
    private static ArrayList<Double> mwLliuresCentrals;
    private static Set<Integer> consumidorsZero;
    private static ArrayList<Central> centrals;
    private static ArrayList<Cliente> clients;

    private Random myRandom;

    public CentralsEnergiaBoard() {
        consumidorsZero = new HashSet<>();
        myRandom = new Random();
    }

    public void generarCentrals(int[] tipos_centrales) throws Exception {
        int seed = myRandom.nextInt();
        centrals = new Centrales(tipos_centrales, seed);
        mwLliuresCentrals = new ArrayList<>();
        for (Central central : centrals) {
            mwLliuresCentrals.add(central.getProduccion());
            System.out.println(central.getProduccion());
        }
    }

    public void generarClients(int ncl, double[] propc, double propg) throws Exception {
        int seed = myRandom.nextInt();
        clients = new Clientes(ncl, propc, propg, seed);
        assignacionsConsumidors = new ArrayList<>(Collections.nCopies(clients.size(), null));
    }

    private static double getDistancia(Cliente cliente, Central central) {
        int x = cliente.getCoordX() - central.getCoordX();
        int y = cliente.getCoordY() - central.getCoordY();
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    private int asignarCentral(int i, int j, Cliente client, int caso) {
        int tries = 0;
        boolean assignat = false;
        while (central_id < centrals.size() && !assignat) {
            Central central = centrals.get(central_id);
            double mwLliures = mwLliuresCentrals.get(central_id);
            System.out.println("Central:" + central_id); //+ " - mwLliures:" + mwLliures);
            System.out.println("Client:" + client_id + " - Consum: " + client.getConsumo());
            mwLliures -= client.getConsumo() + client.getConsumo() * VEnergia.getPerdida(getDistancia(client, central));
            if (mwLliures >= 0) {
                assignacionsConsumidors.set(i, j);
                mwLliuresCentrals.set(j, mwLliures);
                assignat = true;
            } else {
                if (caso == 0) {
                    central_id += 1;
                }
                else if(caso == 1) {
                    if(tries < 10) {
                        central_id = new Random().nextInt(centrals.size());
                        ++tries;
                    } else {
                        if (client.getContrato() == Cliente.GARANTIZADO) return -1;
                    }
                }
            }
        }
        //No queden centrals
        if (!assignat) {
            if (caso == 0) assignacionsConsumidors.set(i, j - 1);
            else if (caso == 1) assignacionsConsumidors.set(i, myRandom.nextInt(centrals.size()));
            consumidorsZero.add(i);
        }
        return central_id;
    }

    public Boolean generarEstatInicial(int tipus) {
        switch (tipus) {
            case 0:
                int j = 0;
                ArrayList<Integer> clientsNoGarantitzats = new ArrayList<>();

                for (int i = 0; i < clients.size(); ++i) {
                    Cliente client = clients.get(i);
                    if (client.getContrato() == Cliente.GARANTIZADO) {
                        System.out.println("Client garantitzat" + i);
                        j = asignarCentral(i, j, client, 0);
                        if (j == -1) {
                            return false;
                        }
                    } else {
                        clientsNoGarantitzats.add(i);
                    }
                }
                for (int i = 0; i < clientsNoGarantitzats.size(); ++i) {
                    Cliente client = clients.get(clientsNoGarantitzats.get(i));
                    j = asignarCentral(i, j, client, 0);
                }
                break;
            case 1:
                int ncentrals = centrals.size();
                ArrayList<Integer> clientsNoGarantitzatsRandom = new ArrayList<>();
                for (int i = 0; i < clients.size(); ++i) {
                    Cliente client = clients.get(i);
                    if (client.getContrato() == Cliente.GARANTIZADO) {
                        int random = myRandom.nextInt(centrals.size());
                        if (asignarCentral(i, random, client, 1) == -1) {
                            return false;
                        }
                    } else {
                        clientsNoGarantitzatsRandom.add(i);
                    }
                }
                for (int i = 0; i < clientsNoGarantitzatsRandom.size(); ++i) {
                    Cliente client = clients.get(clientsNoGarantitzatsRandom.get(i));
                    int random = new Random().nextInt(ncentrals);
                    asignarCentral(i, random, client, 1);
                }
                break;
        }
        return true;
    }

    public boolean isGoal() {
        //Una central no té més demanda de la que pot produir (la suma dels consumidors assignats <= producció màxima central).

        // Si una central està encesa (té algun client assignat), llavors genera tota la producció (tot el cost).

        // Un client només té assignada una central

        // Un client només pot ser servit per una central, i aquesta li ha de donar tota la seva demanda.

        // Els clients de servei garantit, han de ser servits sempre
        return true;
    }

    public double getCostCentrals() {

        double cost = 0;
        for (int i = 0; i < centrals.size(); ++i) {
            // Suma dels costos de les centrals que estan enceses
            Central central = centrals.get(i);
            if (mwLliuresCentrals.get(i) != null &&central.getProduccion() != mwLliuresCentrals.get(i)) {
                // Sumar costos
                try {
                    cost += VEnergia.getCosteMarcha(central.getTipo());
                    cost += VEnergia.getCosteProduccionMW(central.getTipo());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
            else {
                try {
                    cost += VEnergia.getCosteParada(central.getTipo());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return cost;
    }


    public double getCostConsumidors() {
        double cost = 0;
        for (int i=0; i<clients.size(); ++i) {
            Cliente client = clients.get(i);
            if (!consumidorsZero.contains(i)){
                if (client.getContrato()== Cliente.GARANTIZADO){
                    try {
                        cost-=VEnergia.getTarifaClienteGarantizada(client.getTipo());
                    }catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    try {
                        cost-=VEnergia.getTarifaClienteNoGarantizada(client.getTipo());
                    }catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            else {
                try {
                    cost+=VEnergia.getTarifaClientePenalizacion(client.getTipo());
                }catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        }
        return cost;
    }


    public double getMWLliures(){

        return mwLliuresCentrals.stream().reduce(0.0, Double::sum);

        /*double mwLliuresTotals = 0;
        for (int i = 0; i < mwLliuresCentrals.size(); i++){
            mwLliuresTotals += mwLliuresCentrals.get(i);
        }
        return mwLliuresTotals;*/
    }

}
