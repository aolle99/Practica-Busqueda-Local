import aima.search.framework.HeuristicFunction;
import aima.search.framework.SuccessorFunction;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Random;


public class Main {
    /**
     * Paràmetres a modificar per tal de fer proves amb diferents valors
     */
    static final int REPLIQUES = 5; //Paràmetre que permet executar el codi varies vegades, per tal de fer mitjanes
    static final int[] TIPUS_CENTRALS = {5, 10, 25}; //Serveix per a configurar el nombre de centrals de cada tipus que es volen generar (A, B, C)
    static final int NUM_CLIENTS = 1000; // Serveix per a indicar el nombre de clients que es volen generar
    static final double[] PROPC = {0.25, 0.30, 0.45}; // Serveix per indicar la proporcio de clients de cada tipus que es volen generar (XG,MG,G)
    static final double PROPG = 0.75; //Serveix per indicar el percentatge de clients que són garantitzats.
    static final int HEURISTICA = 1; //Serveix per a seleccionar l'heurística que es vol utilitzar. (1: Calcul del benefici, 2: Calcul dels MW lliures, 3: Calcul dels MW ocupats utilitzant la fòrmula de l'antropia, 4: Calcul dels MW ocupats amb pes, 5. Energia perduda per distància)
    private static final int ESTAT_INICIAL = 3; // Serveix per seleccionar el tiùs de generació de l'estat inicial (1. Ordenat, 2. Aleatori, 3. Greedy)
    private static final int SEARCH_ALGORITHM = 2; // 1. Hill Climbing, 2. Simulated Annealing
    private static final int it = 1000000; // Nombre d'iteracions per a l'algorisme de simulated annealing
    private static final int passos = 10000; // Nombre de passos per a l'algorisme de simulated annealing
    private static final int k = 5; // Constant per a l'algorisme de simulated annealing
    private static double lambda = 0.00001; // Constant per a l'algorisme de simulated annealing
    private static final double[] repeticionsToTest = {}; // Serveix per a llençar execucions amb diferents valors de una variable concreta
    private static final int SEED_TYPE = 3; // Serveix per indicar la seed que s'utilitzarà per a generar l'estat inicial. 1. Aleatori, 2. 1234, 3. Fixes


    /**
     * Variables per a mantenir estats de la simulació. No tocar ni modificar.
     */
    static Random myRandom; //Variable per a generar nombres aleatoris
    static Board board; //Conté l'estat
    static int iteracio = 0; //Conté el nombre d'iteracions que s'han fet
    static final int MAX_TRIES = 999999; //Indica el número de iteraciones que se realizarán per a generar l'estat inicial.

    /**
     * Funció principal del programa. S'encarrega de fer les crides per a l'execució del programa.
     * Permet fer varies execucions del programa, per tal de poder fer mitjanes.
     */
    public static void main(String[] args) {

        if (REPLIQUES > 1) {
            try {
                System.setOut(new PrintStream(new FileOutputStream("resultats6.txt")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (repeticionsToTest.length > 0) {
            for (double v : repeticionsToTest) {
                //posar variable a la qual se li volen anar assignant els valors de repeticionsToTest. Exemple: lambda = v;
                lambda = v;
                System.out.println("-----Repeticions: " + v + "-----");
                executarRepliques();
            }
        } else {
            executarRepliques();
        }

    }

    private static void executarRepliques() {
        for (int replica = 0; replica < REPLIQUES; replica++) {
            //System.out.println("|=======================| REPLICA " + (i + 1) + " |=======================|");
            if (initBoard()) {
                board.printResultat();
                if (SEARCH_ALGORITHM == 1) hillClimbing();
                else if (SEARCH_ALGORITHM == 2) simulatedAnnealing();
                else System.out.println("Error: Algoritme de cerca no vàlid");
            }
            //System.out.println("|============================================================|");
            //System.out.println();
            iteracio++;
        }
    }

    /**
     * Funció que s'encarrega de generar els clients i centrals que s'utilitzaran per al problema i d'intentar generar l'estat inicial.
     *
     * @return Retorna cert si s'ha pogut generar l'estat inicial, fals en cas contrari.
     */
    private static boolean initBoard() {
        try {
            myRandom = new Random();
            int seed = selectSeed();
            board = new Board();
            board.generarCentrals(TIPUS_CENTRALS, seed);
            board.generarClients(NUM_CLIENTS, PROPC, PROPG, seed);
            boolean generat = false;
            int tries = 0;
            while (!generat && tries < MAX_TRIES) {
                generat = board.generarEstatInicial(ESTAT_INICIAL);
                tries++;
            }
            if (tries == MAX_TRIES) {
                System.out.println("No s'ha pogut generar l'estat inicial");
                return false;
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Funció que s'encarrega de configurar els paràmetres per fer la crida per a executar l'algorisme hill climbing.
     */
    private static void hillClimbing() {
        //Declarem variables
        Searcher searcher;
        HeuristicFunction heuristic = null;
        SuccessorFunction operators = new HCSuccessorFunction();

        switch (HEURISTICA) {
            case 1 -> heuristic = new HeuristicFunction1();
            case 2 -> heuristic = new HeuristicFunction2();
            case 3 -> heuristic = new HeuristicFunction3();
            case 4 -> heuristic = new HeuristicFunction4();
            case 5 -> heuristic = new HeuristicFunction5();
        }
        try {
            searcher = new Searcher(board, operators, heuristic);
            searcher.executeSearch();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void simulatedAnnealing() {
        //Declarem variables
        Searcher searcher;
        HeuristicFunction heuristic = null;
        SuccessorFunction operators = new SASuccessorFunction();

        switch (HEURISTICA) {
            case 1 -> heuristic = new HeuristicFunction1();
            case 2 -> heuristic = new HeuristicFunction2();
            case 3 -> heuristic = new HeuristicFunction3();
            case 4 -> heuristic = new HeuristicFunction4();
            case 5 -> heuristic = new HeuristicFunction5();
        }


        try {
            searcher = new Searcher(board, operators, heuristic, it, passos, k, lambda);
            searcher.executeSearch();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static int selectSeed() {
        if (SEED_TYPE == 1) return myRandom.nextInt();
        else if (SEED_TYPE == 2) return 1234;
        else if (SEED_TYPE == 3) return inicializeSeedArray();
        System.out.println("Error: Seed type no vàlid");
        return 0;
    }

    private static int inicializeSeedArray() {
        int[] seeds = {1234, 12345, 54321, 4431, 723549, 97001, 37133, 95248, 26901, 83683, 94083, 102994, 46821, 99119, 39301, 11182, 192482,
                33399, 1927009, 3344201, 3344992, 1998238612, 1926628822, 33399909, 199823099, 14604552, 11454829, 13276248, 15621458,
                1};
        return seeds[iteracio];

    }
}
