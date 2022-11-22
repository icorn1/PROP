package edu.epsevg.prop.lab.c4;

/**
 * Jugador QMatic
 * "Si hoc algorithmus te verberat, debes mihi 5 arietes"
 * @author icorn1, Robacoure
 */
public class Qmatic
  implements Jugador, IAuto
{
  final private  int MAX = 10000000;  // Maxim d'heuristica (10M)
  private int color;
  private String nom;
  private int profunditat;
  private int jugades;  //Jugades explorades
  private int nJugades; //# jugades reals
  private double sumTime = 0; //Suma del temps que tarda cada jugada
  private boolean printStats = true;

  //METODES DE CREACIO I NOM
    /**
    * Crea un nou Qmatic donada una profunditat i una flag (printstats)
    *
    * @param profunditat indica la profunditat a arribar del arbre de jugades possibles
    * @param printStats per defecte true, imprimeix per consola estadistiques del joc. 
    */
  public Qmatic(int profunditat, boolean printStats)
  {
    this.profunditat = profunditat;
    this.printStats = printStats;
    nom = "QMatic("+profunditat+")";
  }
  /**
    * Crea un nou Qmatic donada una profunditat
    *
    * @param profunditat indica la profunditat a arribar del arbre de jugades possibles 
    */
  public Qmatic(int profunditat)
  {
    this.profunditat = profunditat;
    nom = "QMatic("+profunditat+")";
  }
  /**
    * Retorna el nom del "robot"
    */
  public String nom()
  {
    return nom;
  }

  //MOVIMENT, MINMAX I HEURISTICA
  /**
    * Realitza un moviment sobre el tauler t segons l'algorisme dissenyat
    *
    * @param t tauler sobre el qual fer el moviment
    * @param color color del nostre jugador QMatic 
    */
  public int moviment(Tauler t, int color)
  {
    this.color = color;
    if(printStats){
        //Al fer printStats podrem saber el temps que ha tardat en fer calcular la columna a tirar,
        //i també les jugades explorades (quantes fulles). Al final del programa dirà el temps mitjà.
        jugades = 0;
        nJugades++;
        //Obtenim columna on tirar i quant tarda
        double startTime = System.currentTimeMillis();
        int jugada = minMax(t,profunditat);
        double endTime = System.currentTimeMillis();
        System.out.println("S'ha decit tirar a la columna "+jugada+" havent explorat "+jugades+" jugades.");
        //Calculem el temps que ha tardat i fem mitja
        double time = (endTime - startTime)/1000.0;
        sumTime+=time;
        System.out.printf("He tardat %.3f segons en decidir.%n", time);
        System.out.printf("Mitja de temps de jugades: %.4f%n", sumTime/(double)nJugades);

        return jugada;
    } 
    else 
        return minMax(t,profunditat); 
  }
  
  /**
    * Calcular l'heuristica de la columna col del tauler t segons l'algorisme dissenyat (veure documentacio)
    *
    * @param t tauler sobre el qual fer el moviment
    * @param col columnar a checkejar.
    */
  public int hCol(Tauler t, int col){
    //Pre: 
    //Post: Retorna l'heuristica vertical per a columna col (veure documentacio per mes detalls)
        Integer first = 0, cont = 0, cont_buides = 0;
        for(int i = t.getMida()-1; i >= 0; --i){
            // Comprovem si no hi ha fitxa i distinguim dos casos
            if(first==0){
                // Si no hi ha fitxa, "first" seguira sent 0 (fins que trobi una fitxa).
                first = t.getColor(i, col);
                if(first != 0)
                    cont += first;
                else
                    cont_buides += 1;
            }else{
                int fitxa = t.getColor(i, col);
                if(first == fitxa) 
                    cont += fitxa;
                else 
                    break;
                if(cont > 3 || cont < -3){
                    //Aixo vol dir que hem 4 en ratlla vertical!
                    return color*first*MAX;
                }
            }
        }
        if(cont == 0 || cont_buides+first*cont<4)
            return 0;
        
        return (int)(color*first*(Math.pow(10.0,first*(cont-first))));
    }
    /**
    * Calcular l'heuristica de la fila fil del tauler t segons l'algorisme dissenyat (veure documentacio)
    *
    * @param t tauler sobre el qual fer el moviment
    * @param fil fila a checkejar.
    */
    public int hFil(Tauler t, int fil){
    //Pre: 
    //Post: Retorna l'heuristica horitzonatl per a fila fil (veure documentacio per mes detalls)
        int cont_buides = 0,cont = 0,color_actual = 0,res = 0,color_aux = 0;
        for(int i = t.getMida()-1; i >= 0; --i){
            int fitxa = t.getColor(fil, i);
            // Comprovem si no hi ha fitxa i distinguim dos casos
            if(fitxa == 0){
                if(color_actual != 0){
                    if(cont+cont_buides>3){
                        res+=(int)(color*color_actual*(Math.pow(10.0,cont-1)));
                    }
                        color_aux = color_actual;
                        color_actual = 0;
                        cont_buides = 1;
                }else{
                    ++cont_buides;
                    if(cont+cont_buides>3){
                        res+=(int)(color*color_aux*(Math.pow(10.0,cont-1)));
                        color_aux = 0;
                    }
                }
            }else{
                // Ja hem trobat una fitxa a la fila. Sumem.
                if(fitxa == color_actual){
                    ++cont;
                    if(cont>3)
                        return color*color_actual*MAX;
                }
                else if(color_actual == 0){
                    color_actual = fitxa;
                    cont = 1;
                }else{
                    if(cont+cont_buides>3){
                        res+=(int)(color*color_actual*(Math.pow(10.0,cont-1)));
                    }
                        color_actual *=-1;
                        cont = 1;
                        cont_buides = 0;
                }
            }
            if(i == 0 && cont+cont_buides>3){
                res+=(int)(color*color_actual*(Math.pow(10.0,cont-1)));
            }
        }
        return res;
    }
    
    /**
    * Calcular l'heuristica de les diagonals del tauler t segons l'algorisme dissenyat (veure documentacio)
    *
    * @param t tauler sobre el qual fer el moviment
    */
    public int hDiagonals(Tauler t){
    //Pre: 
    //Post: Retorna l'heuristica diagonal pel tauler sencer (veure documentacio per mes detalls)
        int col = t.getMida()-4, fil = 0, res = 0;
        while(fil < t.getMida()-2){
            int cont_buides = 0,cont = 0,color_actual = 0,color_aux = 0;
            for(int i = 0; i+col < t.getMida() && i+fil < t.getMida(); ++i){
                int fitxa = t.getColor(fil+i, col+i);
                if(fitxa == 0){
                    if(color_actual != 0){
                        if(cont+cont_buides>3){
                            res+=(int)(color*color_actual*(Math.pow(10.0,cont-1)));
                        }
                            color_aux = color_actual;
                            color_actual = 0;
                            cont_buides = 1;
                    }else{
                        ++cont_buides;
                        if(cont+cont_buides>3){
                            res+=(int)(color*color_aux*(Math.pow(10.0,cont-1)));
                            color_aux = 0;
                        }
                    }
                }else{
                    if(fitxa == color_actual){
                        ++cont;
                        if(cont>3)
                            return color*color_actual*MAX;
                    }
                    else if(color_actual == 0){
                        color_actual = fitxa;
                        cont = 1;
                    }else{
                        if(cont+cont_buides>3){
                            res+=(int)(color*color_actual*(Math.pow(10.0,cont-1)));
                        }
                            color_actual *=-1;
                            cont = 1;
                            cont_buides = 0;
                    }
                }
            }
            if(cont+cont_buides>3){
                    res+=(int)(color*color_actual*(Math.pow(10.0,cont-1)));
            }
            if(col>0)
                --col;
            else
                ++fil;
        }
        col = 3;
        fil = 0;
        while(fil<t.getMida()-2){
            int cont_buides = 0,cont = 0,color_actual = 0,color_aux = 0;
            for(int i = 0; col-i > 0 && i+fil < t.getMida(); ++i){
                int fitxa = t.getColor(fil+i, col-i);
                if(fitxa == 0){
                    if(color_actual != 0){
                        if(cont+cont_buides>3){
                            res+=(int)(color*color_actual*(Math.pow(10.0,cont-1)));
                        }
                            color_aux = color_actual;
                            color_actual = 0;
                            cont_buides = 1;
                    }else{
                        ++cont_buides;
                        if(cont+cont_buides>3){
                            res+=(int)(color*color_aux*(Math.pow(10.0,cont-1)));
                            color_aux = 0;
                        }
                    }
                }else{
                    if(fitxa == color_actual){
                        ++cont;
                        if(cont>3)
                            return color*color_actual*MAX;
                    }
                    else if(color_actual == 0){
                        color_actual = fitxa;
                        cont = 1;
                    }else{
                        if(cont+cont_buides>3){
                            res+=(int)(color*color_actual*(Math.pow(10.0,cont-1)));
                        }
                            color_actual *=-1;
                            cont = 1;
                            cont_buides = 0;
                    }
                }
            }
            if(cont+cont_buides>3){
                    res+=(int)(color*color_actual*(Math.pow(10.0,cont-1)));
            }
            ++col;
            if(col==t.getMida()){
                --col;
                ++fil;
            }
        }
        return res;
    }

    /**
    * Calcular l'heuristica del tauler t a partir d'altres metodes ja creats.
    *
    * @param t tauler sobre el qual fer el moviment
    */
    public int getHeuristica(Tauler t) {
        // Pre: 
        // Post: retorna l'heuristica del tauler t segons la definició de la documentació.
        ++jugades;
        int res = 0;
        for (int i = t.getMida()-1; i >= 0; --i){
            res+=hCol(t,i);
            res+=(10*hFil(t,i)/(i+1));  // Ponderem segons l'alçada del 4 en ratlla horitzontal
            // Casos en que es guanya/perd:
            //MAX és suficientment gran com per a garantir que la seva meitat sempre serà major que l'heurística trobada
            // en un tauler de "mida mitjanament normal" així doncs evitem el no entrar en aquest if per motius de que 
            // si hem trobat primer una heurística de -100 i després una de MAX (obtenint com a resultat MAX-100)
            // llavors en aquest cas, si possesim res > MAX, llavors no funcionaria. Amb MAX/2, sí.
            if(res >= MAX/2) 
                return MAX;
            if(res <= -MAX/2) 
                return -MAX;
        }
        return res+hDiagonals(t);
    }

    /**
    * Algorisme dissenyat de minmax amb poda alfa-beta. Retorna la columna on es millor tirar.
    *
    * @param t tauler sobre el qual fer el moviment
    * @param profunditat profunditat del arbre de jugades.
    */
    public int minMax(Tauler t, int profunditat){
        int col = 0;
        Integer valor = -MAX-1;
        int alfa = -MAX;
        int beta = MAX;
        for (int i = 0; i < t.getMida(); ++i){
            if(t.movpossible(i)){
                Tauler aux = new Tauler(t);
                aux.afegeix(i,color); //Cal fer un tauler auxiliar cada cop
                int min = minValor(aux, i, alfa, beta, profunditat-1);
                if (valor < min){
                    col = i;
                    valor = min;
                }
                if (beta < valor){
                    return col;
                }
                alfa = Math.max(valor,alfa);
            }
        }
        return col;
    }
    
    /**
    * Funcio de suport per l'algoritme minmax creat.
    *
    * @param t tauler sobre el qual fer el moviment
    * @param col columna sobre la qual s'ha fet l'ultima jugada.
    * @param alfa valor de alfa per a la poda
    * @param beta valor de beta per a la poda.
    * @param profunditat profunditat del arbre de jugades.
    */
    public int maxValor(Tauler t, int col, int alfa, int beta, int profunditat){
        if(t.solucio(col, -color))
            return -MAX;
        if(profunditat > 0){
            Integer valor = -MAX-1;
            for (int i = 0; i < t.getMida(); ++i){
                if(t.movpossible(i)){
                    Tauler aux = new Tauler(t);
                    aux.afegeix(i,color);
                    valor = Math.max(valor, minValor(aux,i, alfa, beta, profunditat-1));
                    if (beta < valor){
                        return valor;
                    }
                    alfa = Math.max(valor,alfa);
                }
            }
            return valor;
        }else{
            return getHeuristica(t);
        }
        
    }
    /**
    * Funcio de suport per l'algoritme minmax creat.
    *
    * @param t tauler sobre el qual fer el moviment
    * @param col columna sobre la qual s'ha fet l'ultima jugada.
    * @param alfa valor de alfa per a la poda
    * @param beta valor de beta per a la poda.
    * @param profunditat profunditat del arbre de jugades.
    */
    public int minValor(Tauler t, int col, int alfa, int beta, int profunditat){
        if(t.solucio(col, color))   
            return MAX;
        if(profunditat > 0){
            Integer valor = MAX-1;
            for (int i = 0; i < t.getMida(); ++i){
                if(t.movpossible(i)){
                    Tauler aux = new Tauler(t);
                    aux.afegeix(i,-color);
                    valor = Math.min(valor, maxValor(aux,i, alfa, beta, profunditat-1));
                    if (valor < alfa){
                        return valor; 
                    }
                    beta = Math.min(valor,beta);
                }
            }
            return valor;
        }
        else{
            return getHeuristica(t);
        }
    }
}