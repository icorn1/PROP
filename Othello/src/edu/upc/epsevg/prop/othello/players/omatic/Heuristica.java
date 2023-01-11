package edu.upc.epsevg.prop.othello.players.omatic;

import edu.upc.epsevg.prop.othello.CellType;
import edu.upc.epsevg.prop.othello.GameStatus;

public class Heuristica {
    private CellType jugador;
    private int turns;

    private static final int POND_CORNERS = 50;
    private static final int POND_EDGES = 15;
    private static final int POND_SEDGES = 30;
    private static final double POND_MOBILITAT = 2;
    private static final double POND_PARITAT = 2;
    private static final int TURN_TRESHOLD = 20;

    /**
   * Constructor de la classe Heuristica
   * @param jugador el jugador som nosaltres mateixos (OMatic)
   */
    public Heuristica(CellType jugador){
        this.jugador = jugador;
        turns = 0;
    }
    
     /**
    *  La puntuació es calcula tenint en compte les cantonades del tauler (les caselles en les posicions (0,0), (0,7), (7,0) i (7,7)). Si el jugador actual té alguna d'aquestes caselles, se li suma una puntuació de 1. Si l'oponent té alguna d'aquestes caselles, se li resta una puntuació de 1. Si cap dels dos jugadors té cap d'aquestes caselles, es retorna 0. Per ultim es multiplica el resultat final per el seu pes corresponent que en el cas dels corners seria el més elevat (ja que te mes importancia) i es 50.
    *
    * @param s tauler sobre el qual fer el moviment
    * @return Retorna un enter que representa la puntuació de l'estat actual del joc
    */
    public int hCorners(GameStatus s){
        int corners = 0;
        if(s.getPos(0, 0) == jugador) corners++;
        else if(s.getPos(0, 0) == CellType.opposite(jugador)) corners--;
        
        if(s.getPos(0, 7) == jugador) corners++;
        else if(s.getPos(0, 7) == CellType.opposite(jugador)) corners--;
        
        if(s.getPos(7, 0) == jugador) corners++;
        else if(s.getPos(7, 0) == CellType.opposite(jugador)) corners--;
        
        if(s.getPos(7, 7) == jugador) corners++;
        else if(s.getPos(7, 7) == CellType.opposite(jugador)) corners--;
        
        return (int)(POND_CORNERS*corners);
    }

     /**
    * La puntuació es calcula tenint en compte la paritat de les fitxes del jugador i de l'oponent en el tauler, i realitzant la resta entre el nombre de pces del jugador, menys el nombre de peces de l’oponent. Si el jugador actual té més fitxes que l'oponent, es retornarà un valor positiu, per altra banda, si l'oponent té més fitxes que el jugador, es retornarà un valor negatiu. Si tots dos jugadors tenen la mateixa quantitat de fitxes, es retorna 0. Cal afegir que el resultat obtingut es multiplicarà per un pes baix com ara pot ser 0.5, ja que la paritat no té tanta importància com poden ser els corners
    *
    * @param s tauler sobre el qual fer el moviment
    * @return Retorna un enter que representa la puntuació de l'estat actual del joc
    */
    public int hParitat(GameStatus s){
        // retornem la diferencia de peçes de l'oponent i les nostres
        int paritat = s.getScore(jugador) - s.getScore(CellType.opposite(jugador));
        //System.out.println("Paritat: "+ paritat);
        paritat = (turns > TURN_TRESHOLD) ?  paritat*2 : paritat;
        return (int)(POND_PARITAT*paritat);
    }

     /**
    *  La puntuació es calcula tenint en compte les cantonades del tauler (les caselles en les posicions (0,0), (0,7), (7,0) i (7,7)). Si el jugador actual té alguna d'aquestes caselles, se li suma una puntuació de 1. Si l'oponent té alguna d'aquestes caselles, se li resta una puntuació de 1. Si cap dels dos jugadors té cap d'aquestes caselles, es retorna 0. Per ultim es multiplica el resultat final per el seu pes corresponent que en el cas dels corners seria el més elevat (ja que te mes importancia) i es 50.
    *
    * @param s tauler sobre el qual fer el moviment
    * @return Retorna un enter que representa la puntuació de l'estat actual del joc
    */
    public int hMobilitat(GameStatus s){
        int mobilitat = (turns > TURN_TRESHOLD) ?  s.getMoves().size()*2 : s.getMoves().size();
        return (int)(POND_MOBILITAT*mobilitat);
    }

     /**
    * Rep com a paràmetres la fila i la columna d'una fitxa i l'estat actual del joc, i retorna true si la fitxa és estable (és a dir, si no pot ser voltada per l'oponent de cap de les maneres) o false en cas contrari.    *
    * @param fil tauler sobre el qual fer el moviment
    * @param col tauler sobre el qual fer el moviment
    * @param s tauler sobre el qual fer el moviment
    * @return Retorna true si la fitxa és estable (és a dir, si no pot ser voltada per l'oponent de cap de les maneres) o false en cas contrari.
    */
    public boolean checkEdgeEstable(int fil, int col, GameStatus s) {
        // Pre: fil col representa un edge del tauler
        // El Edge (fil, col) es estable (te una sequencia de fitxes del mateix color com a minim fins a un corner)
        CellType color = s.getPos(fil, col);
        int i;
        if(fil == 0 || fil == 7){
            //Comprova si hi ha una linea de fitxes en direccio horitzontal fins al corner.
            for(i = fil+1; i < s.getSize(); i++){
                if(s.getPos(fil, i) != color)
                    break;
                else if(i == s.getSize()-1 && s.getPos(fil, i) == color)
                    return true;
            }
            for(i = fil-1; i >= 0; i--){
                if(s.getPos(fil, i) != color)
                    break;
                else if(i == 0 && s.getPos(fil, i) == color)
                    return true;
            }      
        }
        if(col == 0 || col == 7){
            //Comprova si hi ha una linea de fitxes en direccio vertical fins al corner.
            for(i = col+1; i < s.getSize(); i++){
                if(s.getPos(i, col) != color)
                    break;
                else if(i == s.getSize()-1 && s.getPos(i, col) == color)
                    return true;
            }
            for(i = col-1; i >= 0; i--){
                if(s.getPos(i, col) != color)
                    break;
                else if(i == 0 && s.getPos(i, col) == color)
                    return true;
            }
        }
        return false;
}

     /**
    * La puntuació es calcula tenint en compte les fitxes del jugador i de l'oponent que es troben en les vores del tauler (les files i columnes 0 i 7). Si el jugador actual té alguna fitxa en algun de les vores, se li suma una puntuació de 1. Si l'oponent té alguna fitxa en algun de les vores, se li resta una puntuació de 1. Si cap dels dos jugadors té cap fitxa en les vores, es retorna 0. 
    * @param s tauler sobre el qual fer el moviment
    * @return Retorna un enter que representa la puntuació de l'estat actual del joc
    */
    public int hEdges(GameStatus s){
        // Check if the piece is stable in the horizontal direction
        int edges = 0;  // Numero de edges
        int sEdges = 0; // Numero de edges estables
        for(int i = 0; i < s.getSize(); i++){
            for(int j = 0; j < s.getSize(); j++){
                if (((i == 0 || i == 7) && (j > 0 && j < 7)) || ((j == 0 || j == 7) && (i > 0 && i < 7))) { 
                    if(s.getPos(i,j) == jugador){
                        if(checkEdgeEstable(i, j, s)){
                            sEdges++;
                        }
                        edges++; 
                    }
                    else if(s.getPos(i,j) == CellType.opposite(jugador)){
                        if(checkEdgeEstable(i, j, s)){
                            sEdges--;
                        }
                        edges--;
                    }
                        
                }
            }
        }
        //System.out.println("Edges Normals: "+edges);
        //System.out.println("Edges Estables: "+sEdges);
        return POND_EDGES*edges+POND_SEDGES*sEdges;
    }
    
     /**
    *  La puntuació es calcula tenint en compte les cantonades del tauler (les caselles en les posicions (0,0), (0,7), (7,0) i (7,7)). Si el jugador actual té alguna d'aquestes caselles, se li suma una puntuació de 1. Si l'oponent té alguna d'aquestes caselles, se li resta una puntuació de 1. Si cap dels dos jugadors té cap d'aquestes caselles, es retorna 0. Per ultim es multiplica el resultat final per el seu pes corresponent que en el cas dels corners seria el més elevat (ja que te mes importancia) i es 50.
    *
    * @param s tauler donat
    * @return Retorna un enter el qual es la suma dels diferents factors que es tenen en compte (estabilitat, paritat, mobilitat, i corners)
    */
    public int getHeuristica(GameStatus s){
        turns++;
        return hCorners(s)+hEdges(s)+hParitat(s)+hMobilitat(s);
    }
}
