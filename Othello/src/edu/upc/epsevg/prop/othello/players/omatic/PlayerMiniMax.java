package edu.upc.epsevg.prop.othello.players.omatic;


import edu.upc.epsevg.prop.othello.CellType;
import edu.upc.epsevg.prop.othello.GameStatus;
import edu.upc.epsevg.prop.othello.IAuto;
import edu.upc.epsevg.prop.othello.IPlayer;
import edu.upc.epsevg.prop.othello.Move;
import edu.upc.epsevg.prop.othello.SearchType;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

/**
 * Jugador aleatori
 * @author icorn1, eric_gonzalez
 */
public class PlayerMiniMax implements IPlayer, IAuto {

    private String name;
    private int profunditat;
    private long nodesExplorats;
    private CellType jugador;
    final private int MAX = 1000000;
    private Heuristica h;

   /**
    * Constructor de la classe PlayerMiniMax
   */
    public PlayerMiniMax(int profunditat) {
        this.profunditat = profunditat;
        this.name = "OMatic(" + profunditat + ")";
    }

   /**
    * En aquesta classe no es fa servir la funcio timeout, pero es necessita igualment per a poguer compilar correctament el projecte
   */
    @Override
    public void timeout() {
        // Nothing to do! 
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
        this.jugador = s.getCurrentPlayer();
        h = new Heuristica(jugador);
        return minMax(s);
    }

    /**
    * Algorisme dissenyat de minmax amb poda alfa-beta. Retorna la posició on es millor tirar. Per aquesta heuristica tindrem en compte els seguents factors:  La mobilitat, l'estabilitat, les cantonades i la paritat de peces.
    *
    * @param s tauler sobre el qual fer el moviment
    *
    * @return Retorna el moviment optim
    */  
    public Move minMax(GameStatus s){
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
                //System.out.println(" " + (moves.size()-i) + " movements left...");
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
        //System.out.println("Valor retornat: " + valor);   
        return new Move(moviment, nodesExplorats, profunditat, SearchType.MINIMAX);
    }
    
    /**
    * Funcio de suport per l'algoritme minmax creat.
    *
    * @param s tauler sobre el qual fer el moviment
    * @param alfa valor de alfa per a la poda
    * @param beta valor de beta per a la poda.
    * @param profunditat profunditat del arbre de jugades.
    */
    public int maxValor(GameStatus s, int alfa, int beta, int profunditat){
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
            //nodesExplorats++;
            return h.getHeuristica(s);
        }
        
    }
    /**
    * Funcio de suport per l'algoritme minmax creat.
    *
    * @param s tauler sobre el qual fer el moviment
    * @param alfa valor de alfa per a la poda
    * @param beta valor de beta per a la poda.
    * @param profunditat profunditat del arbre de jugades.
    */
    public int minValor(GameStatus s, int alfa, int beta, int profunditat){
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
            ArrayList<Point> moves =  s.getMoves();
            for (int i = 0; i < moves.size(); ++i){
                GameStatus aux = new GameStatus(s);
                aux.movePiece(moves.get(i)); //Cal fer una tirada auxiliar cada cop
                //de vegades retorna MAX-1 perque la funcio maxValor retorna MAX, per tant MAX-1 < MAX.
                valor = Math.min(valor, maxValor(aux, alfa, beta, profunditat-1));
                if (valor < alfa){
                    return valor; 
                }
                beta = Math.min(valor,beta);
            }
            return valor;
        }
        else{   //Si es fulla
            return h.getHeuristica(s);
        }
    }
}
