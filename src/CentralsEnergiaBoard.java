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
        mwLliuresCentrals = new ArrayList<>(centrals.size());

    }

    public void generarClients(int ncl, double[] propc, double propg) throws Exception {
        int seed = myRandom.nextInt();
        clients = new Clientes(ncl, propc, propg, seed);
        assignacionsConsumidors = new ArrayList<>(clients.size());

    }

    private static double getDistancia(Cliente cliente, Central central) {
        int x = cliente.getCoordX() - central.getCoordX();
        int y = cliente.getCoordY() - central.getCoordY();
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    private int asignarCentral(int i, int j, Cliente client, int caso) {
        int tries = 0;
        Central central = centrals.get(j);
        boolean assignat = false;
        while (!assignat && j < centrals.size()) {
            double mwLliures = mwLliuresCentrals.get(j);
            if (mwLliuresCentrals.get(j)==null) mwLliuresCentrals.set(j, central.getProduccion());
            mwLliures -= client.getConsumo() + client.getConsumo() * VEnergia.getPerdida(getDistancia(client, central));
            if (mwLliures >= 0) {
                assignacionsConsumidors.set(i, j);
                mwLliuresCentrals.set(j, mwLliures);
                assignat = true;
            } else {
                if (caso == 0) {
                    j += 1;
                }
                else if(caso == 1) {
                    if(tries < 10) {
                        j = new Random().nextInt(centrals.size());
                        ++tries;
                    } else {
                        return -1;
                    }
                }
                central = centrals.get(j);
            }
        }
        //No queden centrals
        if (!assignat) {
            if (caso == 0) assignacionsConsumidors.set(i, j - 1);
            else if (caso == 1) assignacionsConsumidors.set(i, myRandom.nextInt(centrals.size()));
            consumidorsZero.add(i);
        }
        return j;
    }

    public void generarEstatInicial(int tipus) {
        // Generem consumidors
        switch (tipus) {
            case 0:
                int j = 0;
                ArrayList<Integer> clientsNoGarantitzats = new ArrayList<>();
                Central central = centrals.get(j);
                mwLliuresCentrals.set(j, central.getProduccion());

                for (int i = 0; i < clients.size(); ++i) {
                    Cliente client = clients.get(i);
                    if (client.getContrato() == Cliente.GARANTIZADO) {
                        j = asignarCentral(i, j, client, 0);
                        if (j == -1) {
                            generarEstatInicial(1);
                            return;
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
                            generarEstatInicial(1);
                            return;
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
        for (int i=0; i<centrals.size(); ++i) {
            // Suma dels costos de les centrals que estan enceses
            Central central = centrals.get(i);
            if (mwLliuresCentrals.get(i)!=null &&central.getProduccion() != mwLliuresCentrals.get(i)) {
                // Sumar costos
                try {
                    cost +=VEnergia.getCosteMarcha(central.getTipo());
                    cost +=VEnergia.getCosteProduccionMW(central.getTipo());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
            else {
                try {
                    cost+=VEnergia.getCosteParada(central.getTipo());
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
