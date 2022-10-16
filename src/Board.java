import IA.Energia.*;

import java.text.NumberFormat;
import java.util.*;

public class Board {

    private ArrayList<ArrayList<Integer>> assignacionsConsumidors;
    private ArrayList<Integer> assignacionsCentrals;
    private static ArrayList<Central> centrals;
    private static ArrayList<Cliente> clients;
    private static int numClients;
    private static int numCentrals;
    private static ArrayList<ArrayList<Double>> distancies; // distancies[clients][centrals]
    private static ArrayList<ArrayList<Double>> consums; // consums[clients][centrals]
    private static Random myRandom;
    static final int MAX_TRIES = 10000;

    /********************** CONSTRUCTORS **********************/
    public Board() {
        myRandom = new Random();

    }

    public Board(Board board_to_copy) {
        assignacionsConsumidors = new ArrayList<>();
        int i = 0;
        for (ArrayList<Integer> oldSet : board_to_copy.getAssignacionsConsumidors()) {
            assignacionsConsumidors.add((ArrayList<Integer>) oldSet.clone());
            /*for (Integer j : oldSet) {
                assignacionsConsumidors.get(i).add(j);
            } */
            i++;
        }
        assignacionsCentrals = (ArrayList<Integer>) board_to_copy.getAssignacionsCentrals().clone();
    }

    /********************** GENERADORS **********************/
    public void generarCentrals(int[] tipos_centrales, int seed) throws Exception {
        centrals = new Centrales(tipos_centrales, seed);
        numCentrals = centrals.size();
        // La mida d'assignacionsConsumidors es centrals.size() + 1 perquè a l'última posició s'assignaran tots els clients que no siguin subministrats.
        assignacionsConsumidors = new ArrayList<>(numCentrals + 1);
        for (int i = 0; i < numCentrals + 1; i++) {
            assignacionsConsumidors.add(new ArrayList<>());
        }
    }

    public void generarClients(int ncl, double[] propc, double propg, int seed) throws Exception {
        clients = new Clientes(ncl, propc, propg, seed);
        numClients = clients.size();
        distancies = new ArrayList<>(numClients);
        consums = new ArrayList<>(numClients);
        for (int i = 0; i < numClients; i++) {
            distancies.add(new ArrayList<>(numCentrals));
            consums.add(new ArrayList<>(numCentrals));
            for (int j = 0; j < numCentrals; j++) {
                distancies.get(i).add(calcularDistancia(clients.get(i), centrals.get(j)));
                consums.get(i).add(calcularConsumMwClientACentral(i, j));
            }
        }
        assignacionsCentrals = new ArrayList<>(Collections.nCopies(clients.size(), numCentrals));
    }


    private int asignarCentralLineal(int client_id, int central_id) {
        Cliente client = clients.get(client_id);
        while (central_id < numCentrals) {
            if (setAssignacioConsumidor(central_id, client_id)) return central_id;
            central_id += 1;
        }
        //No queden centrals
        if (client.getContrato() == Cliente.GARANTIZADO) return -1;
        setClientExclos(client_id);
        return central_id;
    }

    private Boolean generarEstatInicialLineal() {
        int j = 0;
        ArrayList<Integer> clientsNoGarantitzats = new ArrayList<>();
        for (int client_id = 0; client_id < numClients; ++client_id) {
            Cliente client = clients.get(client_id);
            if (client.getContrato() == Cliente.GARANTIZADO) {
                if ((j = asignarCentralLineal(client_id, j)) == -1) return false;
            } else clientsNoGarantitzats.add(client_id);
        }
        for (int client_id : clientsNoGarantitzats) {
            j = asignarCentralLineal(client_id, j);
        }
        return true;
    }

    private Boolean assignarCentralAleatori(int client_id, int central_id, Cliente client) {
        int tries = 0;
        while (!setAssignacioConsumidor(central_id, client_id)) {
            if (tries < MAX_TRIES) {
                central_id = new Random().nextInt(numCentrals);
                ++tries;
            } else {
                if (client.getContrato() == Cliente.GARANTIZADO) return false;
                setClientExclos(client_id);
                return true;
            }
        }
        return true;
    }

    private Boolean generarEstatInicialAleatori() {
        ArrayList<Integer> clientsNoGarantitzatsRandom = new ArrayList<>();
        for (int client_id = 0; client_id < numClients; ++client_id) {
            Cliente client = clients.get(client_id);
            if (client.getContrato() == Cliente.GARANTIZADO) {
                int central_random = myRandom.nextInt(numCentrals);
                if (!assignarCentralAleatori(client_id, central_random, client)) return false;
            } else {
                clientsNoGarantitzatsRandom.add(client_id);
            }
        }
        for (int client_id : clientsNoGarantitzatsRandom) {
            Cliente client = clients.get(client_id);
            int central_random = new Random().nextInt(numCentrals);
            assignarCentralAleatori(client_id, central_random, client);
        }
        return true;
    }

    private Boolean assignarCentralGreedy(int client_id) {
        HashSet<Integer> centrals_intentades = new HashSet<>();
        for (int it = 0; it < numCentrals; ++it) { // Fem l'intent d'intertarlo en totes les centrals
            int central_id = 0;
            double min_distancia = Double.MAX_VALUE;
            for (int i = 0; i < numCentrals; ++i) { // Busquem la central més propera
                if (!centrals_intentades.contains(i)) {
                    if (distancies.get(client_id).get(i) < min_distancia) {
                        min_distancia = distancies.get(client_id).get(i);
                        central_id = i;
                    }
                }
            }
            if (setAssignacioConsumidor(central_id, client_id)) return true;
            centrals_intentades.add(central_id);

        }
        return false;
    }

    private Boolean generarEstatInicialGreedy() {
        ArrayList<Integer> clientsNoGarantitzats = new ArrayList<>();
        for (int client_id = 0; client_id < numClients; ++client_id) {
            Cliente client = clients.get(client_id);
            if (client.getContrato() == Cliente.GARANTIZADO) {
                if (!assignarCentralGreedy(client_id)) return false;
            } else clientsNoGarantitzats.add(client_id);

        }
        for (int client_id : clientsNoGarantitzats) {
            if (!assignarCentralGreedy(client_id)) setClientExclos(client_id);
        }
        return true;
    }

    public Boolean generarEstatInicial(int tipus) {
        if (tipus == 1) {
            return generarEstatInicialLineal();
        } else if (tipus == 2) {
            return generarEstatInicialAleatori();
        } else if (tipus == 3) {
            return generarEstatInicialGreedy();
        }
        System.out.println("Error: tipus d'estat inicial incorrecte");
        return false;
    }

    /********************** HEURISTIQUES **********************/
    public double getCostCentrals() {
        double cost = 0;
        for (int i = 0; i < numCentrals; ++i) {
            // Suma dels costos de les centrals que estan enceses
            Central central = centrals.get(i);
            // Cost central en marxa
            if (!assignacionsConsumidors.get(i).isEmpty()) {
                // Sumar costos
                try {
                    if (getMwLliuresCentral(i) < 0) return Integer.MAX_VALUE;
                    cost += VEnergia.getCosteMarcha(central.getTipo());
                    cost += VEnergia.getCosteProduccionMW(central.getTipo()) * central.getProduccion();
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
        for (int i = 0; i < numCentrals; ++i) {
            ArrayList<Integer> clientsCentral = assignacionsConsumidors.get(i);
            for (int client_id : clientsCentral) {
                Cliente client = clients.get(client_id);
                if (client.getContrato() == Cliente.GARANTIZADO) {
                    try {
                        beneficio += VEnergia.getTarifaClienteGarantizada(client.getTipo()) * client.getConsumo();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        beneficio += VEnergia.getTarifaClienteNoGarantizada(client.getTipo()) * client.getConsumo();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        ArrayList<Integer> clientsExclosos = assignacionsConsumidors.get(numCentrals);
        for (int client_id : clientsExclosos) {
            Cliente client = clients.get(client_id);
            try {
                if (client.getContrato() == Cliente.GARANTIZADO) return Integer.MAX_VALUE;
                beneficio -= VEnergia.getTarifaClientePenalizacion(client.getTipo()) * client.getConsumo();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return beneficio;
    }

    public double getBenefici() {
        double beneficio = 0;
        for (int i = 0; i < numCentrals; ++i) {
            ArrayList<Integer> clientsCentral = assignacionsConsumidors.get(i);
            for (int client_id : clientsCentral) {
                Cliente client = clients.get(client_id);
                if (client.getContrato() == Cliente.GARANTIZADO) {
                    try {
                        beneficio += VEnergia.getTarifaClienteGarantizada(client.getTipo()) * client.getConsumo();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        beneficio += VEnergia.getTarifaClienteNoGarantizada(client.getTipo()) * client.getConsumo();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            Central central = centrals.get(i);
            // Cost central en marxa
            if (!assignacionsConsumidors.get(i).isEmpty()) {
                // Sumar costos
                try {
                    if (getMwLliuresCentral(i) < 0) return Integer.MAX_VALUE;
                    beneficio -= VEnergia.getCosteMarcha(central.getTipo());
                    beneficio -= VEnergia.getCosteProduccionMW(central.getTipo()) * central.getProduccion();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            // Cost central parada
            else {
                try {
                    beneficio -= VEnergia.getCosteParada(central.getTipo());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        ArrayList<Integer> clientsExclosos = assignacionsConsumidors.get(numCentrals);
        for (int client_id : clientsExclosos) {
            Cliente client = clients.get(client_id);
            try {
                if (client.getContrato() == Cliente.GARANTIZADO) return Integer.MAX_VALUE;
                beneficio -= VEnergia.getTarifaClientePenalizacion(client.getTipo()) * client.getConsumo();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return beneficio;
    }

    public double getTotalMWLliures() {
        double mwLliures = 0;
        for (int i = 0; i < centrals.size(); ++i) {
            if (!assignacionsConsumidors.get(i).isEmpty()) {
                mwLliures += getMwLliuresCentral(i);
            }
        }
        return mwLliures;
    }

    public double getMWEntropia() {
        double entropia = 0;
        for (int i = 0; i < centrals.size(); ++i) {
            if (!assignacionsConsumidors.get(i).isEmpty()) {
                double prob = (centrals.get(i).getProduccion() - getMwLliuresCentral(i)) / centrals.get(i).getProduccion();
                if (prob != 0) {
                    entropia += prob * Math.log(prob);
                }
            }
        }
        return -entropia;
    }

    public double getMWOcupatsAmbPes() {
        double ocupats = 0;
        for (int i = 0; i < numCentrals; ++i) {
            if (!assignacionsConsumidors.get(i).isEmpty())
                ocupats += Math.log(centrals.get(i).getProduccion() - getMwLliuresCentral(i)) / Math.log(2);
        }
        return ocupats;
    }

    public double getEnergiaPerdudaPerDistancia() {
        double perduda = 0;
        for (int central_id = 0; central_id < numCentrals; central_id++) {
            int nConsumidorsAssignats = assignacionsConsumidors.get(central_id).size();
            for (int client_id = 0; client_id < nConsumidorsAssignats; client_id++) {
                Cliente client = clients.get(client_id);
                perduda += client.getConsumo() * VEnergia.getPerdida(getDistancia(client_id, central_id));
            }
        }
        return perduda;
    }

    /********************** OPERADORS **********************/


    public boolean canSwap(int client1_id, int client2_id, int central1_id, int central2_id) {

        if (central1_id == central2_id) return false;
        if (isCentralExcluida(central1_id) && clients.get(client2_id).getContrato() == Cliente.GARANTIZADO)
            return false;
        if (isCentralExcluida(central2_id) && clients.get(client1_id).getContrato() == Cliente.GARANTIZADO)
            return false;
        if (!isCentralExcluida(central1_id) && getMwLliuresCentralAmbNouConsumidor(central1_id, client2_id) + getConsumMwClientACentral(client1_id, central1_id) < 0)
            return false;
        if (!isCentralExcluida(central2_id) && getMwLliuresCentralAmbNouConsumidor(central2_id, client1_id) + getConsumMwClientACentral(client2_id, central2_id) < 0)
            return false;
        return true;
    }

    public void swap(int client1_id, int client2_id, int central1_id, int central2_id) {
        assignacionsConsumidors.get(central1_id).remove(Integer.valueOf(client1_id));
        assignacionsConsumidors.get(central2_id).remove(Integer.valueOf(client2_id));
        setAssignacioConsumidor(central1_id, client2_id);
        setAssignacioConsumidor(central2_id, client1_id);

    }

    public boolean canMove(int id_client, int id_central, int old_central) {
        if (old_central == id_central) return false;
        if (isCentralExcluida(id_central) && clients.get(id_client).getContrato() == Cliente.GARANTIZADO) return false;
        if (!isCentralExcluida(id_central) && getMwLliuresCentralAmbNouConsumidor(id_central, id_client) < 0)
            return false;
        return true;
    }

    public void move(int id_client, int id_central, int old_central) {
        assignacionsConsumidors.get(old_central).remove(Integer.valueOf(id_client));
        setAssignacioConsumidor(id_central, id_client);
    }

    /********************** GETTERS I SETTERS **********************/
    private static double calcularDistancia(Cliente cliente, Central central) {
        int x = cliente.getCoordX() - central.getCoordX();
        int y = cliente.getCoordY() - central.getCoordY();
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    public static double getDistancia(Integer client_id, Integer central_id) {
        return distancies.get(client_id).get(central_id);
    }

    public int getCentralsApagades() {
        int centralsApagades = 0;
        for (int i = 0; i < numCentrals; ++i) {
            if (assignacionsConsumidors.get(i).isEmpty()) ++centralsApagades;
        }
        return centralsApagades;
    }

    public int getAssignacioCentral(int client_id) {
        // Retorna la central a la que es troba el client, numCentrals en cas que no estigui assignat el client
        return assignacionsCentrals.get(client_id);
    }

    public double getMwLliuresCentral(int central_id) {
        // Retorna els megawatts lliures de la central
        Central central = centrals.get(central_id);
        double mw_lliures = central.getProduccion();
        ArrayList<Integer> clientsCentralX = assignacionsConsumidors.get(central_id);
        for (int client_id : clientsCentralX) {
            mw_lliures -= getConsumMwClientACentral(client_id, central_id);
        }
        return mw_lliures;
    }

    public double getMwLliuresCentralAmbNouConsumidor(int central_id, int client_id) {
        // Retorna els megawatts lliures de la central
        double mwLliures = getMwLliuresCentral(central_id);
        mwLliures -= getConsumMwClientACentral(client_id, central_id);
        return mwLliures;
    }

    public double calcularConsumMwClientACentral(int client_id, int central_id) {
        Cliente client = clients.get(client_id);
        return client.getConsumo() + client.getConsumo() * VEnergia.getPerdida(getDistancia(client_id, central_id));
    }

    public double getConsumMwClientACentral(int client_id, int central_id) {
        return consums.get(client_id).get(central_id);
    }


    public Boolean setAssignacioConsumidor(int central_id, int client_id) {
        // Retorna true si s'ha pogut assignar tots els clients, false en cas contrari
        if (!isCentralExcluida(central_id)) {
            double mw_lliures = getMwLliuresCentral(central_id);
            if (mw_lliures - getConsumMwClientACentral(client_id, central_id) >= 0) {
                assignacionsConsumidors.get(central_id).add(client_id);
                assignacionsCentrals.set(client_id, central_id);
                return true;
            }
            return false;
        }
        assignacionsConsumidors.get(central_id).add(client_id);
        assignacionsCentrals.set(client_id, numCentrals);
        return true;
    }

    public void setClientExclos(int client_id) {
        // Assigna el client a la central exclosa
        assignacionsConsumidors.get(numCentrals).add(client_id);
    }

    public static ArrayList<Central> getCentrals() {
        return centrals;
    }

    public static ArrayList<Cliente> getClients() {
        return clients;
    }

    public ArrayList<ArrayList<Integer>> getAssignacionsConsumidors() {
        return assignacionsConsumidors;
    }

    public ArrayList<Integer> getAssignacionsCentrals() {
        return assignacionsCentrals;
    }

    public boolean isCentralExcluida(int central_id) {
        return central_id == numCentrals;
    }

    private int getClientesNoAsignados() {
        return assignacionsConsumidors.get(numCentrals).size();
    }

    /********************** PRINTS PER CONSOLA **********************/
    public void printResultat() {
        double costCentrals = getCostCentrals();
        double beneficiConsumidors = getBeneficiConsumidors();
        int clientesNoAsignados = getClientesNoAsignados();
        System.out.println("---------------------");
        System.out.println("Coste de las centrales: " + NumberFormat.getCurrencyInstance(new Locale("es", "ES"))
                .format(costCentrals));
        System.out.println("Benefici de los consumidores: " + NumberFormat.getCurrencyInstance(new Locale("es", "ES"))
                .format(beneficiConsumidors));
        System.out.println("Benefici total: " + NumberFormat.getCurrencyInstance(new Locale("es", "ES"))
                .format((beneficiConsumidors - costCentrals)));
        System.out.println("Clientes asignados: " + (numClients - clientesNoAsignados) + "/" + numClients);
        System.out.println("Centrals apagades: " + getCentralsApagades() + "/" + numCentrals);
        System.out.println("---------------------");
    }
}
