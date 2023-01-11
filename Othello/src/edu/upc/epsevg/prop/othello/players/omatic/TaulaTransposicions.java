package edu.upc.epsevg.prop.othello.players.omatic;


import java.util.Map;
import java.util.HashMap;

/*
 * @author icorn, eric_gonsalez
 */
public class TaulaTransposicions {
    
    private final Map<Long, Integer> taula;

    public TaulaTransposicions() {
        taula = new HashMap<>();
    }

    //put heuristic
    public void afegir(long hash, int h) {
        taula.put(hash, h);
    }

    public boolean conte(long hash) {
        return taula.containsKey(hash);
    }

    public int get(long hash) {
        return taula.get(hash);
    }
}