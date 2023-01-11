package edu.upc.epsevg.prop.othello.players.omatic;


import edu.upc.epsevg.prop.othello.CellType;
import edu.upc.epsevg.prop.othello.GameStatus;
import edu.upc.epsevg.prop.othello.IAuto;
import edu.upc.epsevg.prop.othello.IPlayer;
import edu.upc.epsevg.prop.othello.Move;
import edu.upc.epsevg.prop.othello.SearchType;
import java.awt.Point;
import java.util.ArrayList;

/**
 * Jugador aleatori
 * @author bernat
 */
public class PlayerID implements IPlayer, IAuto {

    private String      name;
    private CellType    jugador;
    private final int   MAX = 1000000;
    private int         nodesExplorats = 0;
    private boolean     timeoutFlag = false;
    private Heuristica  h;
    private double      avgProf = 0;
    private int         turns = 0;
    private boolean     printStats = false;
    private boolean     probCut = false;
   
    private static final double PROB_THRESHOLD = 0.1;

   /**
    * Constructor de la classe PlayerID
   */
    public PlayerID() {
        this.name = "OMatic(IDS)";
    }
    
    public PlayerID(boolean printStats, boolean probCut) {
        this.name = "OMatic(IDS)";
        this.printStats = printStats;
        this.probCut = probCut;
    }

    /**
   * Ens avisa que hem de parar la cerca en curs perquè s'ha exhaurit el temps de joc
   */
    @Override
    public void timeout() {
        timeoutFlag = true;
    }

    /**
     * @return nom del jugador actual
    */
    @Override
    public String getName() {
        return this.name;
    }

    /**
    * Realitza el moviment del jugador
    *
    * @param s tauler sobre el qual fer el moviment
    * @return Retorna el resultat de la crida a iterative deeping search per a trobar el millor moviment
    */
    @Override
    public Move move(GameStatus s) {
        timeoutFlag = false;
        this.jugador = s.getCurrentPlayer();
        h = new Heuristica(jugador);

        return IDS(s);
    }
    
    public double probability(int nMoviments, int depth) {
        return Math.pow(nMoviments, -depth);  // You can adjust this formula as needed.
    }
    
    /**
    * Iterative Deeping Search realitza crides a la funció minmax, anant incrementant la profunditat tenint en compte el timeout.
    *
    * @param s tauler sobre el qual fer el moviment
    * @return Retorna el millor moviment de la profunditat més baixa explorada.
    */
    public Move IDS(GameStatus s){
        turns++;
        Move bestMove = null;
        int prof = 1;
        double startTime = System.currentTimeMillis();
        
        while(!timeoutFlag && prof < s.getEmptyCellsCount()+1){
            if(printStats)
                System.out.println("** PROFUNDITAT " + prof + " **");
            nodesExplorats = 0;
            Move move = minMax(s, prof);
            if(!timeoutFlag)
                bestMove = move;
            prof++;
        }
        avgProf += (prof-1);

        if(printStats){
            double endTime = System.currentTimeMillis();
            double time = (endTime - startTime)/1000.0;
            System.out.println(time);
            System.out.printf("Profunditat mitjana: %.3f%n", (avgProf)/turns);
        }

        return bestMove;
    }

    /**
    * Algorisme dissenyat de minmax amb poda alfa-beta. Retorna la posició on es millor tirar. Per aquesta heuristica tindrem en compte els seguents factors:  La mobilitat, l'estabilitat, les cantonades i la paritat de peces.
    *
    * @param s tauler sobre el qual fer el moviment
    * @param profunditat profunditat del arbre de jugades.
    *
    * @return Retorna null si s'ha arribat al timeout, i sino, retorna el moviment optim
    */
    public Move minMax(GameStatus s, int profunditat){
        if(timeoutFlag)     //No ens importa el valor retornat. Només sortir lo més rapid del bucle
            return null;
        nodesExplorats = 0;
        Integer valor = -MAX-1;                     // valor d'heuristica ha de començar el mes petit possible per a poder superarla facil
        int alfa = -MAX;
        int beta = MAX;
        ArrayList<Point> moves =  s.getMoves();     // Llista de moviments
        Point moviment = null;                      // Moviment que realitzarem;
        if(moves.isEmpty()){
            // no podem moure, el moviment (de tipus Point) es passa null.
            return new Move(null, 0L, 0,  SearchType.MINIMAX); 
        } else {
            for (int i = 0; i < moves.size(); ++i){
               
                GameStatus aux = new GameStatus(s);
                aux.movePiece(moves.get(i)); //Cal fer una tirada auxiliar cada cop
                int min = minValor(aux, alfa, beta, profunditat-1);
                if (valor <= min){
                    moviment = moves.get(i);
                    valor = min;
                }
                if (beta < valor){
                    break;
                }
                alfa = Math.max(valor,alfa);

            }     
        } 
        return new Move(moviment, nodesExplorats, profunditat, SearchType.MINIMAX_IDS);
    }
    
    /**
    * Funcio de suport per l'algoritme minmax creat.
    *
    * @param s tauler sobre el qual fer el moviment
    * @param alfa valor de alfa per a la poda
    * @param beta valor de beta per a la poda.
    * @param profunditat profunditat del arbre de jugades.
    *
    * @return Retorna el valor obtingut de la heuristica
    */
    public int maxValor(GameStatus s, int alfa, int beta, int profunditat){
        if(timeoutFlag)     //No ens importa el valor retornat. Només sortir lo més rapid del bucle
            return 0;
        nodesExplorats++;
        if(s.checkGameOver()){
            if(s.getScore(jugador) > s.getScore(CellType.opposite(jugador)))
                return MAX;
            else 
                //Si score(jugador) < score(oposat) o son iguals, considerem que hem perdut.
                return -MAX;
        }
            
        if(profunditat > 0){
            Integer valor = -MAX-1;
            if (probCut && probability(s.getMoves().size(), profunditat) > PROB_THRESHOLD) {
                return valor;
            }
            ArrayList<Point> moves =  s.getMoves();
            for (int i = 0; i < moves.size(); ++i){
                GameStatus aux = new GameStatus(s);
                aux.movePiece(moves.get(i)); //Cal fer una tirada auxiliar cada cop
                valor = Math.max(valor, minValor(aux, alfa, beta, profunditat-1));
                if (beta < valor){
                    return valor;
                }
                alfa = Math.max(valor,alfa);
            }
            return valor;
        }
        else{
            if(!timeoutFlag)
                return h.getHeuristica(s);
            else
                return 0;
        }
        
    }
    /**
    * Funcio de suport per l'algoritme minmax creat.
    *
    * @param s tauler sobre el qual fer el moviment
    * @param alfa valor de alfa per a la poda
    * @param beta valor de beta per a la poda.
    * @param profunditat profunditat del arbre de jugades.
    *
    * @return Retorna el valor obtingut de la heuristica
    */
    public int minValor(GameStatus s, int alfa, int beta, int profunditat){
        if(timeoutFlag)     //No ens importa el valor retornat. Només sortir lo més rapid del bucle
            return 0;
        nodesExplorats++;
        if(s.checkGameOver()){
            if(s.getScore(jugador) > s.getScore(CellType.opposite(jugador)))
                return MAX;
            else 
                //Si score(jugador) < score(oposat) o son iguals, considerem que hem perdut.
                return -MAX;
        }
        if(profunditat > 0){
            Integer valor = MAX-1;
            if (probCut && probability(s.getMoves().size(), profunditat) > PROB_THRESHOLD) {
                return valor;
            }
            ArrayList<Point> moves =  s.getMoves();
            for (int i = 0; i < moves.size(); ++i){
                GameStatus aux = new GameStatus(s);
                aux.movePiece(moves.get(i)); //Cal fer una tirada auxiliar cada cop
                valor = Math.min(valor, maxValor(aux, alfa, beta, profunditat-1));
                if (valor < alfa){
                    return valor; 
                }
                beta = Math.min(valor,beta);
            }
            return valor;
        }
        else{   //Si es fulla
            if(!timeoutFlag)
                return h.getHeuristica(s);
            else
                return 0;
        }
    }   
}