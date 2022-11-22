package upc.edu.prop;

// Classe creada per poder comunicar-se entre robotos de forma més fàcil.
public class Missatge implements java.io.Serializable {
    
    //Usem la variable codi per a referir-nos a l'accio que volem que fagi el robot (veure documentacio).
    private int codi;
    //Dues variables per comunicar informacio.
    private double x;
    private double y;

    public Missatge(int m){
        this.codi=m;
    }

    public Missatge(int m,double X, double Y){
        this.codi=m;
        this.x=X;
        this.y=Y;
    }

    // Normalment enviarem coordenades, d'aqui ve el nom, pero ocasionalment s'enviaran altres variables de tipus double.
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getCodi() {
        return codi;
    }

    
}
