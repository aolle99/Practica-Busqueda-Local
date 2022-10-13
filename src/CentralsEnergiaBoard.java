import IA.Energia.*;

import java.util.*;

public class CentralsEnergiaBoard {

    private ArrayList<Set<Integer>> assignacionsConsumidors;
    private static ArrayList<Central> centrals;
    private static ArrayList<Cliente> clients;
    private static Random myRandom;

    public CentralsEnergiaBoard() {
        myRandom = new Random();
    }

    public CentralsEnergiaBoard(CentralsEnergiaBoard board_to_copy) {
        this.assignacionsConsumidors = new ArrayList<>(board_to_copy.getAssignacionsConsumidors());
    }


    public void generarCentrals(int[] tipos_centrales, int seed) throws Exception {
        centrals = new Centrales(tipos_centrales, seed);
        // La mida d'assignacionsConsumidors es centrals.size() + 1 perquè a l'última posició s'assignaran tots els clients que no siguin subministrats.
        assignacionsConsumidors = new ArrayList<>(Collections.nCopies(centrals.size()+1, new HashSet<>()));
    }

    public void generarClients(int ncl, double[] propc, double propg,int seed) throws Exception {
        clients = new Clientes(ncl, propc, propg, seed);
    }

    public static double getDistancia(Cliente cliente, Central central) {
        int x = cliente.getCoordX() - central.getCoordX();
        int y = cliente.getCoordY() - central.getCoordY();
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    public int getAssignacioCentral(int client_id){
        // Retorna la central a la que es troba el client, -1 en cas que no estigui assignat el client
        for (int i = 0; i < centrals.size(); i++){
            if (assignacionsConsumidors.get(i).contains(client_id)) return i;
        }
        return -1;
    }

    public double getMwLliuresCentral(int central_id){
        // Retorna els megawatts lliures de la central
        Central central = centrals.get(central_id);
        double mw_lliures = central.getProduccion();
        for (int client_id : assignacionsConsumidors.get(central_id)){
            Cliente client = clients.get(client_id);
            mw_lliures -= client.getConsumo() + client.getConsumo() * VEnergia.getPerdida(getDistancia(client, central));
        }
        return mw_lliures;
    }

    public Boolean setAssignacionsConsumidors(int central_id, int client_id) {
        // Retorna true si s'ha pogut assignar tots els clients, false en cas contrari
        double mw_lliures = getMwLliuresCentral(central_id);
        Cliente client = clients.get(client_id);
        Central central = centrals.get(central_id);
        if (mw_lliures - (client.getConsumo() + client.getConsumo() * VEnergia.getPerdida(getDistancia(client, central))) >= 0) {
            assignacionsConsumidors.get(central_id).add(client_id);
            return true;
        }
        return false;
    }

    private int asignarCentralLineal(int client_id, int central_id, Cliente client, int caso) {
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

    private int asignarCentralAleatori(int client_id, int central_id, Cliente client, int caso) {
        return 1;
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

    /********************** HEURISTIQUES **********************/
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

    public ArrayList<Set<Integer>> getAssignacionsConsumidors() {
        return assignacionsConsumidors;
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
