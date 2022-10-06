import IA.Energia.*;

import java.util.*;

public class CentralsEnergiaBoard {

    private static ArrayList<Integer> assignacionsConsumidors;
    private static ArrayList<Double> mwLliuresCentrals;
    private static Set<Integer> consumidorsZero;
    private static ArrayList<Central> centrals;
    private static ArrayList<Cliente> clients;

    private Random myRandom;
    private static int assignats=0;

    public CentralsEnergiaBoard() {
        consumidorsZero = new HashSet<>();
        myRandom = new Random();
    }

    public void generarCentrals(int[] tipos_centrales, int seed) throws Exception {
        centrals = new Centrales(tipos_centrales, seed);
        mwLliuresCentrals = new ArrayList<>();
        for (Central central : centrals) {
            mwLliuresCentrals.add(central.getProduccion());
        }
    }

    public void generarClients(int ncl, double[] propc, double propg,int seed) throws Exception {
        clients = new Clientes(ncl, propc, propg, seed);
        assignacionsConsumidors = new ArrayList<>(Collections.nCopies(clients.size(), null));
    }

    private static double getDistancia(Cliente cliente, Central central) {
        int x = cliente.getCoordX() - central.getCoordX();
        int y = cliente.getCoordY() - central.getCoordY();
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    private int asignarCentral(int client_id, int central_id, Cliente client, int caso) {
        int tries = 0;
        boolean assignat = false;
        while (central_id < centrals.size() && !assignat) {
            Central central = centrals.get(central_id);
            double mwLliures = mwLliuresCentrals.get(central_id);
            System.out.println("Central:" + central_id); //+ " - mwLliures:" + mwLliures);
            System.out.println("Client:" + client_id + " - Consum: " + client.getConsumo());
            mwLliures -= client.getConsumo() + client.getConsumo() * VEnergia.getPerdida(getDistancia(client, central));
            if (mwLliures >= 0) {
                assignats++;
                assignacionsConsumidors.set(client_id, central_id);
                mwLliuresCentrals.set(central_id, mwLliures);
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
            if (caso == 0) assignacionsConsumidors.set(client_id, central_id - 1);
            else if (caso == 1) assignacionsConsumidors.set(client_id, myRandom.nextInt(centrals.size()));
            consumidorsZero.add(client_id);
        }
        return central_id;
    }

    private Boolean generarEstatInicialLineal() {
        int j = 0;
        ArrayList<Integer> clientsNoGarantitzats = new ArrayList<>();

        for (int client_id = 0; client_id < clients.size(); ++client_id) {
            Cliente client = clients.get(client_id);
            if (client.getContrato() == Cliente.GARANTIZADO) {
                j = asignarCentral(client_id, j, client, 0);
                if (j == -1) {
                    return false;
                }
            } else {
                clientsNoGarantitzats.add(client_id);
            }
        }
        for (int client_id = 0; client_id < clientsNoGarantitzats.size(); ++client_id) {
            Cliente client = clients.get(clientsNoGarantitzats.get(client_id));
            j = asignarCentral(client_id, j, client, 0);
        }
        return true;
    }

    private Boolean generarEstatInicialAleatori() {
        int ncentrals = centrals.size();
        ArrayList<Integer> clientsNoGarantitzatsRandom = new ArrayList<>();
        for (int client_id = 0; client_id < clients.size(); ++client_id) {
            Cliente client = clients.get(client_id);
            if (client.getContrato() == Cliente.GARANTIZADO) {
                int random = myRandom.nextInt(centrals.size());
                if (asignarCentral(client_id, random, client, 1) == -1) {
                    //System.out.println(-1);
                    return false;
                }
            } else {
                clientsNoGarantitzatsRandom.add(client_id);
            }
        }
        for (int client_id = 0; client_id < clientsNoGarantitzatsRandom.size(); ++client_id) {
            Cliente client = clients.get(clientsNoGarantitzatsRandom.get(client_id));
            int random = new Random().nextInt(ncentrals);
            asignarCentral(client_id, random, client, 1);
        }
        return true;
    }

    public Boolean generarEstatInicial(int tipus) {
        return switch (tipus) {
            case 0 -> generarEstatInicialLineal();
            case 1 -> generarEstatInicialAleatori();
            default -> true;
        };
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

    public int getClientsNoAssignats(){
        return consumidorsZero.size();
    }

    public int getClientsGarantitzatsNoAssignats() {
        int count=0;
        for (int i=0; i<assignacionsConsumidors.size(); ++i) {
            if (assignacionsConsumidors.get(i) == null && clients.get(i).getContrato() == Cliente.GARANTIZADO) {
                count++;
            }
        }
        return count;
    }

    public static ArrayList<Central> getCentrals() {
        return centrals;
    }

    public static ArrayList<Cliente> getClients() {
        return clients;
    }

    public static Set<Integer> getConsumidorsZero() {
        return consumidorsZero;
    }
}
