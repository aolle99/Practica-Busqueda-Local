import IA.Energia.*;

import java.util.*;

public class CentralsEnergiaBoard {

    private ArrayList<Integer> assignacionsConsumidors;
    private ArrayList<Double> mwLliuresCentrals;
    private Set<Integer> consumidorsZero;
    private static ArrayList<Central> centrals;
    private static ArrayList<Cliente> clients;

    private double benefici = 0;

    private Random myRandom;

    public CentralsEnergiaBoard() {
        consumidorsZero = new HashSet<>();
        myRandom = new Random();
    }

    public CentralsEnergiaBoard(CentralsEnergiaBoard board_to_copy) {
        this.assignacionsConsumidors = new ArrayList<>(board_to_copy.getAssignacionsConsumidors());
        this.mwLliuresCentrals = new ArrayList<>(board_to_copy.getMwLliuresCentrals());
        this.consumidorsZero = new HashSet<>(board_to_copy.getConsumidorsZero());
        this.myRandom = new Random();
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
        assignacionsConsumidors = new ArrayList<>(Collections.nCopies(clients.size(), -1));
    }

    public static double getDistancia(Cliente cliente, Central central) {
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
            mwLliures -= client.getConsumo() + client.getConsumo() * VEnergia.getPerdida(getDistancia(client, central));
            if (mwLliures >= 0) {
                //System.out.println("client: " + client_id + ", central: " + central_id + ", tipus: " + client.getContrato());
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
            if (caso == 0) {
                if (client.getContrato() == Cliente.GARANTIZADO) return -1;
                assignacionsConsumidors.set(client_id, -1);
            }
            else if (caso == 1) assignacionsConsumidors.set(client_id, -1);
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
        for (int client_id : clientsNoGarantitzats) {
            Cliente client = clients.get(client_id);
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
        boolean generat = false;
        if (tipus == 0) {
            generat = generarEstatInicialLineal();
            if (generat){

                System.out.println("L'estat inicial lineal s'ha generat correctament");
                System.out.println("S'han assignat " + (assignacionsConsumidors.size() - consumidorsZero.size())+ " clients, i són els següents:");
                StringBuilder assignacions = new StringBuilder();
                for (Integer assignacionsConsumidor : assignacionsConsumidors) {
                    if (assignacionsConsumidor != -1) assignacions.append(assignacionsConsumidor).append(", ");
                }
                System.out.println(assignacions);
                System.out.println("Els clients que no s'han pogut assignar en són " + (consumidorsZero.size()));
                printResultat();
            }
        } else {
            generat = generarEstatInicialAleatori();
        }
        return generat;
    }

    public boolean isGoal() {
        //Una central no té més demanda de la que pot produir (la suma dels consumidors assignats <= producció màxima central);
        for (int i = 0; i < mwLliuresCentrals.size(); i++){
            if (mwLliuresCentrals.get(i) < 0) return false;
            if (mwLliuresCentrals.get(i) > centrals.get(i).getProduccion()) return false;
        }

        // Si una central està encesa (té algun client assignat), llavors genera tota la producció (tot el cost).
        for (int i = 0; i < mwLliuresCentrals.size(); i++){
            if (mwLliuresCentrals.get(i) == centrals.get(i).getProduccion()){
                for (Integer assignacionsConsumidor : assignacionsConsumidors) {
                    if (assignacionsConsumidor == i) return false;
                }
            }
        }

        // Els clients de servei garantit, han de ser servits sempre
        for (int i = 0; i < consumidorsZero.size(); i++){
            if (clients.get(i).getContrato() == Cliente.GARANTIZADO) return false;
        }
        return true;
    }

    public double getCostCentrals() {
        double cost = 0;
        for (int i = 0; i < centrals.size(); ++i) {
            // Suma dels costos de les centrals que estan enceses
            Central central = centrals.get(i);
            // Cost central en marxa
            if (mwLliuresCentrals.get(i) != null &&central.getProduccion() != mwLliuresCentrals.get(i)) {
                // Sumar costos
                try {
                    if (mwLliuresCentrals.get(i) < 0) return Integer.MAX_VALUE;
                    cost += VEnergia.getCosteMarcha(central.getTipo());
                    cost += VEnergia.getCosteProduccionMW(central.getTipo());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
            // Cost central parada
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


    public double getBeneficiConsumidors() {
        double beneficio = 0;
        for (int i = 0; i < clients.size(); ++i) {
            Cliente client = clients.get(i);
            if (!consumidorsZero.contains(i) && assignacionsConsumidors.get(i) != -1) {
                if (client.getContrato() == Cliente.GARANTIZADO) {
                    try {
                        beneficio += VEnergia.getTarifaClienteGarantizada(client.getTipo());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    try {
                        beneficio += VEnergia.getTarifaClienteNoGarantizada(client.getTipo());
                    }catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            else {
                try {
                    if (client.getContrato()== Cliente.GARANTIZADO) return Integer.MAX_VALUE;
                    beneficio -= VEnergia.getTarifaClientePenalizacion(client.getTipo());
                }catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        }
        return beneficio;
    }

    //GETTERS I SETTERS
    public double getTotalMWLliures() {

        return mwLliuresCentrals.stream().reduce(0.0, Double::sum);
    }

    public int getClientsNoAssignats(){
        return consumidorsZero.size();
    }

    public int getClientsGarantitzatsNoAssignats() {
        int count = 0;
        for (int i = 0; i < assignacionsConsumidors.size(); ++i) {
            if (assignacionsConsumidors.get(i) == null && clients.get(i).getContrato() == Cliente.GARANTIZADO) {
                count++;
            }
        }
        return count;
    }

    public double getMWEntropia() {
        double entropia = 0;
        for (int i = 0; i < centrals.size(); ++i) {
            double prob = (centrals.get(i).getProduccion() - mwLliuresCentrals.get(i)) / centrals.get(i).getProduccion();
            if (prob != 0) {
                entropia += prob * Math.log(prob);
            }
        }
        return -entropia;
    }

    public double getMWOcupatsAmbPes() {
        double ocupats = 0;
        for (int i = 0; i < centrals.size(); ++i) {
            ocupats += Math.log(centrals.get(i).getProduccion() - mwLliuresCentrals.get(i)) / Math.log(2);
        }
        return ocupats;
    }

    public double getEnergiaPerdudaPerDistancia(){
        double perduda = 0;
        for (int i = 0; i < assignacionsConsumidors.size(); i++){
            if (assignacionsConsumidors.get(i) != -1) {
                Cliente client = clients.get(i);
                Central central = centrals.get(assignacionsConsumidors.get(i));
                perduda += VEnergia.getPerdida(getDistancia(client, central));
            }
        }
        return perduda;
    }

    public static ArrayList<Central> getCentrals() {
        return centrals;
    }

    public static ArrayList<Cliente> getClients() {
        return clients;
    }

    public Set<Integer> getConsumidorsZero() {
        return consumidorsZero;
    }

    public ArrayList<Integer> getAssignacionsConsumidors() {
        return assignacionsConsumidors;
    }

    public ArrayList<Double> getMwLliuresCentrals() {
        return mwLliuresCentrals;
    }

    public Double getMwLliuresCentral(int idCentral) {
        return mwLliuresCentrals.get(idCentral);
    }

    public int getCentralAssignada(int idConsumidor) {
        return assignacionsConsumidors.get(idConsumidor);
    }

    public boolean assignedToZero(int idConsumidor) {
        return consumidorsZero.contains(idConsumidor);
    }

    public void setMwLliuresCentral(int idCentral, double mwLliures) {
        mwLliuresCentrals.set(idCentral, mwLliures);
    }

    public void setAssignacioConsumidor(int idConsumidor, int idCentral) {
        assignacionsConsumidors.set(idConsumidor, idCentral);
    }

    public void setNoAssignat(int idConsumidor) {
        consumidorsZero.add(idConsumidor);
    }


    public void printResultat() {
        double costCentrals = getCostCentrals();
        double beneficiConsumidors = getBeneficiConsumidors();
        System.out.println("---------------------");
        System.out.println("Coste de las centrales: " + costCentrals + "€");
        System.out.println("Benefici de los consumidores: " + beneficiConsumidors + "€");
        System.out.println("Benefici total: " + (beneficiConsumidors - costCentrals) + "€");
        System.out.println("---------------------");
    }


}
