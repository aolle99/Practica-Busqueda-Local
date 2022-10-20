import aima.search.framework.HeuristicFunction;
import aima.search.framework.SuccessorFunction;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;


public class Main {
    //Indica el número de iteraciones que se realizarán per a generar l'estat inicial.
    static final int MAX_TRIES = 999999;
    //Paràmetre que permet executar el codi varies vegades, per tal de
    static final int REPLIQUES = 30;
    //Serveix per a configurar el nombre de centrals de cada tipus que es volen generar (A, B, C)
    static final int[] TIPUS_CENTRALS = {5, 10, 50};
    // Serveix per a indicar el nombre de clients que es volen generar
    static final int NUM_CLIENTS = 1000;
    // Serveix per indicar la proporcio de clients de cada tipus que es volen generar (XG,MG,G)
    static final double[] PROPC = {0.25, 0.30, 0.45};
    //Serveix per indicar el percentatge de clients que són garantitzats.
    static final double PROPG = 0.75;
    //Serveix per a seleccionar l'heurística que es vol utilitzar.
    // (1: Calcul del benefici, 2: Calcul dels MW lliures, 3: Calcul dels MW ocupats utilitzant la fòrmula de l'antropia, 4: Calcul dels MW ocupats amb pes, 5. Energia perduda per distància)
    static final int HEURISTICA = 1;
    // Serveix per seleccionar el tiùs de generació de l'estat inicial (1. Ordenat, 2. Aleatori, 3. Greedy)
    private static final int ESTAT_INICIAL = 3;
    // Serveix per indicar la seed que s'utilitzarà per a generar l'estat inicial.
    static int seed = 1234;
    static Random myRandom;
    //Conté l'estat
    static Board board;
    static int iteracio = 0;

    static ArrayList<Integer> seeds = new ArrayList<>();

    /**
     * Funció principal del programa. S'encarrega de fer les crides per a l'execució del programa.
     * Permet fer varies execucions del programa, per tal de poder fer mitjanes.
     */
    public static void main(String[] args) {
        inicializeSeedArray();
        if (REPLIQUES > 1) {
            try {
                System.setOut(new PrintStream(new FileOutputStream("resultats6.txt")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < REPLIQUES; i++) {
            System.out.println("|=======================| REPLICA " + (i + 1) + " |=======================|");
            if (initBoard()) {
                //board.printResultat();
                //hillClimbing();
                simulatedAnnealing();
            }
            System.out.println("|============================================================|");
            System.out.println();
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
            seed = myRandom.nextInt();
            //seed = seeds.get(iteracio);
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

        int it = 1000000;
        int pit = 10000;
        int k = 5;
        double lbd = 0.00001;
        try {
            searcher = new Searcher(board, operators, heuristic, it, pit, k, lbd);
            searcher.executeSearch();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void inicializeSeedArray() {
        seeds.add(1234);
        seeds.add(12345);
        seeds.add(123456);
        seeds.add(1234567);
        seeds.add(12345678);
        seeds.add(123456789);
        seeds.add(1234567890);
        seeds.add(987654321);
        seeds.add(98765432);
        seeds.add(9876543);
        seeds.add(987654);
        seeds.add(98765);
        seeds.add(9876);
        seeds.add(987);
        seeds.add(98);
        seeds.add(9);
        seeds.add(87654321);
        seeds.add(8765432);
        seeds.add(876543);
        seeds.add(87654);
        seeds.add(8765);
        seeds.add(876);
        seeds.add(87);
        seeds.add(8);
        seeds.add(7654321);
        seeds.add(765432);
        seeds.add(76543);
        seeds.add(7654);
        seeds.add(765);
        seeds.add(76);
        seeds.add(7);

    }
}
