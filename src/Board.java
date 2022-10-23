import IA.Energia.*;

import java.text.NumberFormat;
import java.util.*;

public class Board {
    private ArrayList<Integer> assignacionsCentrals; // Assignació de centrals a clients, on cada posició es un client i el valor representa la central assignada
    private ArrayList<Double> mwLliuresCentrals; // MW lliures de cada central, on cada posicio es una central i el valor representa els MW lliures que li queden.
    private static ArrayList<Central> centrals; // Llista de centrals generades
    private static ArrayList<Cliente> clients; // Llista de clients generats
    private static int numClients; // Nombre de clients
    private static int numCentrals; // Nombre de centrals
    private static ArrayList<ArrayList<Double>> distancies; // Matriu amb les distàncies entre centrals i clients. distancies[clients][centrals]
    private static ArrayList<ArrayList<Double>> consums; // Matriu amb les distancies entre centrals i clients. consums[clients][centrals]
    private static Random myRandom; // Generador de nombres aleatoris
    private static final double factor_multiplicatiu = 150; // Factor multiplicatiu per a les penalitzacions.
    static final int MAX_TRIES = 10000; // Nombre màxim de intents per a la generació d'una solució inicial.

    /********************** CONSTRUCTORS **********************/
    /**
     * Constructora que inicialitza el generador de nombres aleatoris.
     */
    public Board() {
        myRandom = new Random();
    }

    /**
     * Constructora que inicialitza la board amb l'estat anterior. S'utilitza per a generar successors.
     *
     * @param board_to_copy és l'estat que es vol copiar i alterar.
     */
    public Board(Board board_to_copy) {
        assignacionsCentrals = (ArrayList<Integer>) board_to_copy.getAssignacionsCentrals().clone();
        mwLliuresCentrals = (ArrayList<Double>) board_to_copy.getMwLliuresCentrals().clone();
    }

    /********************** GENERADORS **********************/
    /**
     * Genera les centrals que s'utilitzaran pel problema
     *
     * @param tipos_centrales és una llista amb el nombre de centrals de cada tipus que es vol generar
     * @param seed            és la seed que es vol utilitzar per a generar els clients.
     * @throws Exception en cas de que no s'hagin pogut generar.
     */
    public void generarCentrals(int[] tipos_centrales, int seed) throws Exception {
        centrals = new Centrales(tipos_centrales, seed);
        numCentrals = centrals.size();
        // La mida d'assignacionsConsumidors es centrals.size() + 1 perquè a l'última posició s'assignaran tots els clients que no siguin subministrats.
        mwLliuresCentrals = new ArrayList<>(numCentrals);
        for (int i = 0; i < numCentrals; i++) {
            mwLliuresCentrals.add(centrals.get(i).getProduccion());
        }
    }

    /**
     * Genera els clients que s'utilitzaran pel problema
     *
     * @param ncl   és el nombre de clients que es volen generar.
     * @param propc és una llista amb la proporcio que es vol generar de cada tipus de client.
     * @param propg indica la proporció de clients que es volen generar que siguin garantitzats.
     * @param seed  és la seed que es vol utilitzar per a generar els clients.
     * @throws Exception en cas de que no s'hagin pogut generar.
     */
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

    /**
     * S'encarrega de trobar una assignació de central per a un client concret de manera lineal.
     *
     * @param client_id  és l'identificador del client que es vol assignar.
     * @param central_id és l'identificador de la central per la que es vol començar a intentar d'assignar.
     * @return retorna l'identificador de la central assignada, o -1 si no s'ha pogut assignar.
     */
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

    /**
     * S'encarrega d'intentar generar l'estat inicial de manera ordenada.
     *
     * @return retorna true si s'ha pogut generar, false en cas contrari.
     */
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

    /**
     * S'encarrega de trobar una assignació de central per a un client concret de manera aleatoria.
     *
     * @param client_id és l'identificador del client que es vol assignar.
     * @return retorna true si s'ha pogut assignar, false en cas contrari.
     */
    private Boolean assignarCentralAleatori(int client_id) {
        int tries = 0;
        int central_id = myRandom.nextInt(numCentrals);
        while (!setAssignacioConsumidor(central_id, client_id)) {
            if (tries < MAX_TRIES) {
                central_id = myRandom.nextInt(numCentrals);
                ++tries;
            } else {
                Cliente client = clients.get(client_id);
                if (client.getContrato() == Cliente.GARANTIZADO) return false;
                setClientExclos(client_id);
                return true;
            }
        }
        return true;
    }

    /**
     * S'encarrega d'intentar generar l'estat inicial de manera aleatoria.
     *
     * @return retorna true si s'ha pogut generar, false en cas contrari.
     */
    private Boolean generarEstatInicialAleatori() {
        ArrayList<Integer> clientsNoGarantitzatsRandom = new ArrayList<>();
        for (int client_id = 0; client_id < numClients; ++client_id) {
            Cliente client = clients.get(client_id);
            if (client.getContrato() == Cliente.GARANTIZADO) {
                if (!assignarCentralAleatori(client_id)) return false;
            } else {
                clientsNoGarantitzatsRandom.add(client_id);
            }
        }
        for (int client_id : clientsNoGarantitzatsRandom) {
            assignarCentralAleatori(client_id);
        }
        return true;
    }

    /**
     * S'encarrega de trobar una assignació de central per a un client concret tenint en compte la distància i intentant assignar-la a la més propera possible.
     *
     * @param client_id és l'identificador del client que es vol assignar.
     * @return retorna true si s'ha pogut assignar, false en cas contrari.
     */
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

    /**
     * S'encarrega d'intentar generar l'estat inicial de manera greedy.
     *
     * @return retorna true si s'ha pogut generar, false en cas contrari.
     */
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

    /**
     * S'encarrega de posar tots els clients com a no assignats.
     *
     * @return retorna sempre true, ja que no hi ha cap cas en que no es pugui generar l'estat inicial.
     */
    private Boolean generarEstatInicialBuit() {
        for (int client_id = 0; client_id < numClients; ++client_id) {
            setClientExclos(client_id);
        }
        return true;
    }

    /**
     * S'encarrega de seleccionar quin tipus d'estat inicial es vol generar.
     *
     * @param tipus és el tipus d'estat inicial que es vol generar.
     * @return retorna true si s'ha pogut generar, false en cas contrari.
     */
    public Boolean generarEstatInicial(int tipus) {
        if (tipus == 1) {
            return generarEstatInicialLineal();
        } else if (tipus == 2) {
            return generarEstatInicialAleatori();
        } else if (tipus == 3) {
            return generarEstatInicialGreedy();
        } else if (tipus == 4) {
            return generarEstatInicialBuit();
        }
        System.out.println("Error: tipus d'estat inicial incorrecte");
        return false;
    }

    /********************** HEURISTIQUES **********************/
    /**
     * S'encarrega de calcular el cost de la solució actual causat per les centrals.
     *
     * @return retorna el cost de la solució actual causat per les centrals.
     */
    public double getCostCentrals() {
        double cost = 0;
        for (int central_id = 0; central_id < numCentrals; ++central_id) {
            // El client està sent subministrat per una central
            Central central = centrals.get(central_id);
            // Cost central en marxa
            double mwLliures = getMwLliuresCentral(central_id);
            if (mwLliures != central.getProduccion()) {
                try {
                    if (mwLliures < 0) cost += 57500 * factor_multiplicatiu; //57500 * 1,5
                    cost += VEnergia.getCosteMarcha(central.getTipo());
                    cost += VEnergia.getCosteProduccionMW(central.getTipo()) * central.getProduccion();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    cost += VEnergia.getCosteParada(central.getTipo());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return cost;
    }

    /**
     * S'encarrega de calcular els ingressos de la solució actual causat per els clients.
     *
     * @return retorna els ingressos de la solució actual causat per els clients.
     */
    public double getBeneficiConsumidors() {
        double beneficio = 0;
        for (int i = 0; i < numClients; ++i) {
            Cliente client = clients.get(i);
            // El client està sent subministrat per una central
            int central_id = getAssignacioCentral(i);
            if (central_id != numCentrals) {
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
            } else { // El client no està subministrat per cap central
                try {
                    if (client.getContrato() == Cliente.GARANTIZADO)
                        beneficio -= 1000 * factor_multiplicatiu; // 50 indemnitzacio * 20 Mw *1,5 per a afegir pes
                    beneficio -= VEnergia.getTarifaClientePenalizacion(client.getTipo()) * client.getConsumo();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return beneficio;
    }

    /**
     * S'encarrega de calcular el benefici a través dels ingressos i el cost de la solució actual.
     *
     * @return retorna el benefici de la solució actual.
     */
    public double getBenefici() {
        return getBeneficiConsumidors() - getCostCentrals();
    }

    /**
     * S'encarrega de sumar els MW lliures de les centrals
     *
     * @return total de MW lliures de les centrals
     */
    public double getTotalMWLliures() {
        double mwLliures = 0;
        for (int i = 0; i < centrals.size(); ++i) {
            if (getMwLliuresCentral(i) != centrals.get(i).getProduccion()) {
                mwLliures += getMwLliuresCentral(i);
            }
        }
        return mwLliures;
    }

    /**
     * S'encarrega de calcular l'entropia dels MW ocupats de les centrals
     *
     * @return entropia dels MW ocupats de les centrals
     */
    public double getMWEntropia() {
        double entropia = 0;
        for (int i = 0; i < centrals.size(); ++i) {
            if (getMwLliuresCentral(i) != centrals.get(i).getProduccion()) {
                double prob = (centrals.get(i).getProduccion() - getMwLliuresCentral(i)) / centrals.get(i).getProduccion();
                if (prob != 0) {
                    entropia += prob * Math.log(prob);
                }
            }
        }
        return -entropia;
    }

    /**
     * S'encarrega de fer el sumatori dels MW ocupats aplicant un pes per a que estigui més guiat.
     *
     * @return retorna el sumatori dels MW ocupats aplicant un pes per a que estigui més guiat.
     */
    public double getMWOcupatsAmbPes() {
        double ocupats = 0;
        for (int i = 0; i < numCentrals; ++i) {
            if (getMwLliuresCentral(i) != centrals.get(i).getProduccion())
                ocupats += Math.log(centrals.get(i).getProduccion() - getMwLliuresCentral(i)) / Math.log(2);
        }
        return ocupats;
    }

    /**
     * S'encarrega de calcular l'energia perduda per distància aplicant-hi un pes.
     *
     * @return retorna l'energia perduda per distància aplicant-hi un pes.
     */
    public double getEnergiaPerdudaPerDistancia() {
        double perduda = 0;
        for (int i = 0; i < numClients; i++) {
            Cliente client = clients.get(i);
            int central_id = getAssignacioCentral(i);
            if (central_id != numCentrals)
                perduda += VEnergia.getPerdida(getDistancia(i, central_id)) * client.getConsumo();
            else perduda += client.getConsumo() * factor_multiplicatiu;
        }
        return Math.pow(Math.round(perduda), 50);
    }

    /********************** OPERADORS **********************/

    /**
     * S'encarrega de comprovar si és possible fer el swap de dos clients.
     *
     * @param client1_id  id del primer client.
     * @param client2_id  id del segon client.
     * @param central1_id id de la central del primer client.
     * @param central2_id id de la central del segon client.
     * @return retorna true si és possible fer el swap, false en cas contrari.
     */
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

    /**
     * S'encarrega de fer el swap de dos clients.
     *
     * @param client1_id  id del primer client.
     * @param client2_id  id del segon client.
     * @param central1_id id de la central del primer client.
     * @param central2_id id de la central del segon client.
     */
    public void swap(int client1_id, int client2_id, int central1_id, int central2_id) {
        removeClientDeCentral(client1_id, central1_id);
        removeClientDeCentral(client2_id, central2_id);
        setAssignacioConsumidor(central1_id, client2_id);
        setAssignacioConsumidor(central2_id, client1_id);

    }

    /**
     * S'encarrega de comprovar si és possible fer el move d'un client entre una central i una altra.
     *
     * @param id_client   id del client.
     * @param id_central  id de la central a la que es vol canviar.
     * @param old_central id de la central actual que té assignada el client.
     * @return
     */
    public boolean canMove(int id_client, int id_central, int old_central) {
        if (old_central == id_central) return false;
        if (getMwLliuresCentralAmbNouConsumidor(id_central, id_client) < 0)
            return false;
        return true;
    }

    /**
     * S'encarrega de comprovar si es pot canviar el client i fer que no tingui cap central assignada.
     *
     * @param id_client   id del client.
     * @param old_central id de la central actual que té assignada el client.
     * @return retorna true si es pot fer el move, false en cas contrari.
     */
    public boolean canMoveExclosa(int id_client, int old_central) {
        if (isCentralExcluida(old_central)) return false;
        if (clients.get(id_client).getContrato() == Cliente.GARANTIZADO) return false;
        return true;
    }

    /**
     * S'encarrega de fer el move d'un client entre una central i una altra.
     *
     * @param id_client   id del client.
     * @param id_central  id de la central a la que es vol canviar.
     * @param old_central id de la central actual que té assignada el client.
     */
    public void move(int id_client, int id_central, int old_central) {
        removeClientDeCentral(id_client, old_central);
        setAssignacioConsumidor(id_central, id_client);
    }

    /********************** GETTERS I SETTERS **********************/
    /**
     * Calcula la distància entre un client i una central aplicant el teorema de pitàgores.
     *
     * @param cliente id del client.
     * @param central id de la central.
     * @return retorna la distància entre un client i una central.
     */
    private static double calcularDistancia(Cliente cliente, Central central) {
        int x = cliente.getCoordX() - central.getCoordX();
        int y = cliente.getCoordY() - central.getCoordY();
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    /**
     * Retorna la distància entre un client i una central.
     *
     * @param client_id  id del client.
     * @param central_id id de la central.
     * @return retorna la distància entre un client i una central.
     */
    public static double getDistancia(Integer client_id, Integer central_id) {
        return distancies.get(client_id).get(central_id);
    }

    /**
     * Calcula el nombre de centrals apagades en la solució actual.
     *
     * @return retorna el nombre de centrals apagades en la solució actual.
     */
    public int getCentralsApagades() {
        int centralsApagades = 0;
        for (int i = 0; i < numCentrals; ++i) {
            if (getMwLliuresCentral(i) == centrals.get(i).getProduccion()) {
                ++centralsApagades;
            }
        }
        return centralsApagades;
    }

    /**
     * Obté la central a la que està assignat un client.
     *
     * @param client_id id del client.
     * @return retorna la central a la que està assignat un client.
     */
    public int getAssignacioCentral(int client_id) {
        // Retorna la central a la que es troba el client, numCentrals en cas que no estigui assignat el client
        return assignacionsCentrals.get(client_id);
    }

    /**
     * Obté els MW lliures que li queden a una central
     *
     * @param central_id id de la central.
     * @return retorna els MW lliures que li queden a una central.
     */
    public double getMwLliuresCentral(int central_id) {
        // Retorna els megawatts lliures de la central
        return mwLliuresCentrals.get(central_id);
    }

    /**
     * Obté els MW lliures que li queden a una central tenint en compte que es vol afegir un nou consumidor.
     *
     * @param central_id id de la central.
     * @param client_id  id del client que es vol afegir a aquella central.
     * @return retorna els MW lliures que li queden a una central havent afegit el nou client.
     */
    public double getMwLliuresCentralAmbNouConsumidor(int central_id, int client_id) {
        // Retorna els megawatts lliures de la central
        double mwLliures = getMwLliuresCentral(central_id);
        mwLliures -= getConsumMwClientACentral(client_id, central_id);
        return mwLliures;
    }

    /**
     * Calcula els MW que consumeix un client a una central aplicant-hi el sobrecost per distància.
     *
     * @param client_id  id del client.
     * @param central_id id de la central.
     * @return retorna els MW que consumeix un client a una central aplicant-hi el sobrecost per distància.
     */
    public double calcularConsumMwClientACentral(int client_id, int central_id) {
        Cliente client = clients.get(client_id);
        return client.getConsumo() + client.getConsumo() * VEnergia.getPerdida(getDistancia(client_id, central_id));
    }

    /**
     * Obté els MW que consumeix un client a una central aplicant-hi el sobrecost per distància.
     *
     * @param client_id  id del client.
     * @param central_id id de la central.
     * @return retorna els MW que consumeix un client a una central.
     */
    public double getConsumMwClientACentral(int client_id, int central_id) {
        return consums.get(client_id).get(central_id);
    }

    /**
     * Intenta assignar un client a una central tenint en compte que els MW que consumeix a aquella central no superin els MW lliures que té aquella central.
     *
     * @param central_id id de la central.
     * @param client_id  id del client que es vol assignar.
     * @return retorna true si s'ha pogut assignar el client a la central, false en cas contrari.
     */
    public Boolean setAssignacioConsumidor(int central_id, int client_id) {
        // Retorna true si s'ha pogut assignar tots els clients, false en cas contrari
        if (!isCentralExcluida(central_id)) {
            double mw_lliures = getMwLliuresCentral(central_id);
            if (mw_lliures - getConsumMwClientACentral(client_id, central_id) >= 0) {
                assignacionsCentrals.set(client_id, central_id);
                mwLliuresCentrals.set(central_id, mw_lliures - getConsumMwClientACentral(client_id, central_id));
                return true;
            }
            return false;
        }
        assignacionsCentrals.set(client_id, numCentrals);
        return true;
    }

    /**
     * Desassigna un client i el posa com a client sense central assignada.
     *
     * @param client_id id del client que es vol desassignar.
     */
    public void setClientExclos(int client_id) {
        // Assigna el client a la central exclosa
        assignacionsCentrals.set(client_id, numCentrals);
    }

    /**
     * Elimina un client de l'assignació a una central, fent que aquella central tingui els MW lliures que tenia abans de tenir aquest client assignat.
     *
     * @param client_id  id del client que es vol desassignar.
     * @param central_id id de la central de la qual es vol desassignar el client.
     */
    private void removeClientDeCentral(int client_id, int central_id) {
        if (!isCentralExcluida(central_id)) {
            double mw_lliures = getMwLliuresCentral(central_id);
            mwLliuresCentrals.set(central_id, mw_lliures + getConsumMwClientACentral(client_id, central_id));
        }
    }

    /**
     * Retorna totes les centrals del problema
     *
     * @return retorna totes les centrals del problema
     */
    public static ArrayList<Central> getCentrals() {
        return centrals;
    }

    /**
     * Retorna tots els clients del problema
     *
     * @return retorna tots els clients del problema
     */
    public static ArrayList<Cliente> getClients() {
        return clients;
    }

    /**
     * Retorna les assignacions de centrals a clients
     *
     * @return retorna les assignacions de centrals a clients
     */
    public ArrayList<Integer> getAssignacionsCentrals() {
        return assignacionsCentrals;
    }

    /**
     * Retorna els MW lliures de les centrals
     *
     * @return retorna els MW lliures de les centrals
     */
    public ArrayList<Double> getMwLliuresCentrals() {
        return mwLliuresCentrals;
    }

    /**
     * Comprova si el valor que té un client és el referent a que no té central assignada.
     *
     * @param central_id id de la central.
     * @return retorna true en cas de que el client no tingui central assignada, false en cas contrari.
     */
    public boolean isCentralExcluida(int central_id) {
        return central_id == numCentrals;
    }

    /**
     * Obté els clients que no tenen cap central assignada.
     *
     * @return retorna el nombre de clients que no tenen central assignada.
     */
    private int getClientesNoAsignados() {
        int clientesNoAsignados = 0;
        for (int i = 0; i < numClients; ++i) {
            if (getAssignacioCentral(i) == numCentrals) ++clientesNoAsignados;
        }
        return clientesNoAsignados;
    }

    /**
     * Obté el percentatge de clients que estan assignats a una central de tipus C
     *
     * @return retorna el percentatge de clients que estan assignats a una central de tipus C
     */
    private double getPorcentajeUtilizacionCentralsTipoC() {
        int clientesConCentralTipoC = 0;
        int totalAssignats = 0;
        for (int i = 0; i < numClients; ++i) {
            if (getAssignacioCentral(i) != numCentrals) {
                if (centrals.get(getAssignacioCentral(i)).getTipo() == Central.CENTRALC) ++clientesConCentralTipoC;
                ++totalAssignats;
            }
        }
        return clientesConCentralTipoC / (totalAssignats * 1.0);
    }

    /**
     * Obté el percentatge de clients garantitzats que no tenen central assignada.
     *
     * @return retorna el percentatge de clients garantitzats que no tenen central assignada.
     */
    private double getClientesGarantizadosNoAsignados() {
        int clientesAsignados = 0;
        int totalGarantizados = 0;
        for (int i = 0; i < numClients; ++i) {
            if (getAssignacioCentral(i) != numCentrals && clients.get(i).getContrato() == Cliente.GARANTIZADO)
                ++clientesAsignados;
            if (clients.get(i).getContrato() == Cliente.GARANTIZADO) ++totalGarantizados;
        }
        return clientesAsignados / (totalGarantizados * 1.0);
    }

    /********************** PRINTS PER CONSOLA **********************/
    /**
     * S'encarrega d'imprimir els diferents valors de la solució.
     *
     * @param debug indica el que es vol imprimir.
     */
    public void printResultat(int debug) {
        if (debug == 2 || debug == 1) {
            if (debug == 1) System.out.print("Benefici: ");
            System.out.println(NumberFormat.getCurrencyInstance(new Locale("es", "ES")).format(getBenefici()).replace("\u00A0", " "));
        }
        if (debug == 1) System.out.println("Assignats: " + (numClients - getClientesNoAsignados()) + "/" + numClients);
        if (debug == 5) System.out.println(String.valueOf(getPorcentajeUtilizacionCentralsTipoC()).replace(".", ","));
        if (debug == 1)
            System.out.println("% Garantitzats no assignats (de 0 a 1): " + String.valueOf(getClientesGarantizadosNoAsignados()).replace(".", ","));
    }
}
