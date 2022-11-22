package upc.edu.prop;

import java.awt.Color;
import java.io.IOException;
import java.util.*;
import robocode.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;
/**
 *
 * @author Arnau Roca & icorn1
 */

public class CornerRobot  extends TeamRobot
{
    private boolean LIDER = false;              // Indica si el robot es el Lider / Kamikaze. En el nostre programa, utilitzem lider en comptes de kamikaze, però es el mateix.
    private boolean CORNER = false;             // Per saber si el robot és un CornerRobot.
    private Map<String, Double> corner0Pos = new HashMap<String, Double>();
    private Map<String, Double> corner1Pos = new HashMap<String, Double>();
    private Map<String, Double> corner2Pos = new HashMap<String, Double>();
    private Map<String, Double> corner3Pos = new HashMap<String, Double>();
    private Map<String, Boolean> robotTeCorner = new HashMap<String, Boolean>();
    private int myCorner = -1;                           // Indica el corner al que es dirigeix, inicialment -1
    private int direccioGir = 1;                         // Direccio de gir del kamikaze
    private boolean onCorner= false;                    // Indica si el robot ha arribat al corner.
     
    public void run() {
        try {
            inici();                    // Inicialitzar colors, trobar la disatncia de cada robot a cada corner.
        } catch (IOException ignored) {}

        turnLeft(0);                    // Ens hem trobat que si no fiquem una instruccio inutil, de vegades es queda trabat al run sense continuar
        for(int i = 0; i < 4; i++)  try {
            if(!CORNER)
                findCornerRobot(i);     // Per a cada corner, busquem el robot amb la distancia mínima (en l'ordre 0,1,2,3 [veure documentació]).
        } catch (IOException igonred) {}// Un cop trobat el que té la menor distancia al corner, se li envia el missatge per a que vagi al corner.

        turnLeft(0);
        if(!CORNER) {
            LIDER = true;               //Si finalment el robot no te corner, vol dir que es el Lider/Kamikaze. Li canviem el color per reconeixer-lo.
            setGunColor(Color.black);
            setRadarColor(Color.black);
        }
        
        while(true) {                   // Dos casos: Si el robot es el lider, o si no ho és.
            if(LIDER){                  // LIDER: Mou el radar molt rapid i continua fent girs fins escanejar un robot. 
                setTurnRadarRight(10000);
                setTurnRight(5 * direccioGir);
            }
            else{                       // CORNERROBOT: Prioritza anar al corner si encara no hi ha arribat (ex: s'ha xocat amb un robot o una paret). 
                if(!onCorner){          // Si hi ha arribat, llavors ja pot començar a detectar robots i fer moviment sentinella.
                    goToCorner(myCorner);
                }
                setTurnRadarRight(10000);
                back(150);
                ahead(150);   
            }
            execute();
        }
    }
    
    @Override 
    public void onMessageReceived(MessageEvent event){
        Missatge m = (Missatge) event.getMessage();   //Convertim el missatge en Missatge. 
        switch(m.getCodi()) {
            case 0:     // REBEM LA DISTANCIA D'UN ROBOT A X CORNER
                if (m.getX() == 0){ 
                    corner0Pos.put(event.getSender(), m.getY());
                }
                else if (m.getX() == 1){ 
                    corner1Pos.put(event.getSender(), m.getY());
                }
                else if (m.getX() == 2){ 
                    corner2Pos.put(event.getSender(), m.getY());
                }
                else if (m.getX() == 3){ 
                    corner3Pos.put(event.getSender(), m.getY());
                }
                break;
            case 1:     // GO TO CORNER
                int corner = (int) m.getX();
                goToCorner(corner);
                CORNER = true;
                myCorner = corner;
                robotTeCorner.put(getName(), true);
                break;
        }    
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        // Ignorem els casos on el robot escanejat es company d'equip.
        if(!isTeammate(e.getName())){
            if(LIDER){  // Si el lider escaneja un robot, ens fiquem en la seva trajectoria i anem a per ell!
                // Calcular el bearing del enemic
                double enemyBearing = this.getHeading() + e.getBearing();
                // Calcular posicio del enemic
                double enemyX = getX() + e.getDistance() * Math.sin(Math.toRadians(enemyBearing));
                double enemyY = getY() + e.getDistance() * Math.cos(Math.toRadians(enemyBearing));

                // Ara anem a per ell modifican el gir per a igualar trajectories.
                if (e.getBearing() >= 0) {
                    direccioGir = 1;
                } else {
                    direccioGir = -1;
                }
        
                turnRight(e.getBearing());
                ahead(e.getDistance() + 5);
                scan(); // Reinicia l'event
            }
            else{
                if(CORNER){ // DCalculem la posició del enemic i trajectoria i disparem.
                    // Calcular el bearing del enemic
                    double enemyBearing = this.getHeading() + e.getBearing();
                    // Calcular posicio del enemic
                    double enemyX = getX() + e.getDistance() * Math.sin(Math.toRadians(enemyBearing));
                    double enemyY = getY() + e.getDistance() * Math.cos(Math.toRadians(enemyBearing));
                    // Calcular x i y futures del target
                    double dx = enemyX - this.getX();
                    double dy = enemyY - this.getY();
                    // Calcular angle
                    double theta = Math.toDegrees(Math.atan2(dx, dy));
                    // Apuntar i disparar.
                    // Hem observat que si parem el robot per disparar, es millora l'eficiencia contra MyFirstTeam per un 2%.
                    stop();
                    turnGunRight(normalRelativeAngleDegrees(theta - getGunHeading()));
                    fire(3);
                    resume();
                    scan();
                }
                else{
                    fire(3);
                }
            }
        }
    }
   
    public void onHitRobot(HitRobotEvent e) {
        // Dos casos, que sigui lider o que no ho sigui.
        if(LIDER){                  // Si es lider, dispara i continua xocant al robot.
            if(!isTeammate(e.getName())){
                if (e.getBearing() >= 0) {
                    direccioGir = 1;
                } else {
                    direccioGir = -1;
                }
                turnRight(e.getBearing());
                fire(3);
                ahead(40); // A per ell!!
            }
        }
        else{       // Si no esta al corner, que hi vagi esquivant el robot.
            if(!onCorner)
                goToCorner(myCorner);
        }
    }
		
    public void inici() throws IOException {
        // Inicialitzem colors.
        setBodyColor(Color.black);
        setGunColor(Color.yellow);
        setRadarColor(Color.yellow);
        setBulletColor(Color.black);
        setScanColor(Color.yellow);

        // No tenen corner al principi, inicialitzem la variable "robotTeCorner" a false.
        for (String s: getTeammates()) {          
            robotTeCorner.put(s, false);
        }
        robotTeCorner.put(getName(), false);

       //TROBEM EL ROBOT MÉS APROP DE CADA CANTONADA
        for(int i = 0; i < 4; i++)
            distanceFromCorner(i);    
    }

    public void distanceFromCorner(int corner) throws IOException{
        double cornerX,cornerY, dist;
        // Per al corner "corner", canviem el HashMap on es guardarà la distancia de cada robot al corner corresponent.
        if (corner==0){              //cantó  abaix esquerra
            dist = calcDist(getX(), getY(), 0, 0);
            //Enviem missatge amb codi 0, incloent el corner al que fara referència i la distància corresponent.
            broadcastMessage(new Missatge(0, (int)corner, dist));
            // Afegim la propia distancia al corner.
            corner0Pos.put(getName(), dist);
        }                 
        //Fem el mateix per tots els robots.      
        else if (corner==1){ //Cantó abaix dreta
            dist = calcDist(getX(), getY(), getBattleFieldWidth(), 0);
            broadcastMessage(new Missatge(0, (int)corner, dist));
            corner1Pos.put(getName(), dist);
        }   
        else if (corner==2){ //cantó adalt esquerra 
            dist = calcDist(getX(), getY(), 0, getBattleFieldHeight());
            broadcastMessage(new Missatge(0, (int)corner, dist)); 
            corner2Pos.put(getName(), dist);
        }   
        else {              //canto adalt dreta
            dist = calcDist(getX(), getY(), getBattleFieldWidth(), getBattleFieldHeight());
            broadcastMessage(new Missatge(0, (int)corner, dist));
            corner3Pos.put(getName(), dist);
        } 
    }
    
    public void findCornerRobot(int corn) throws IOException{
        // Per al corner "corn" trobem el robot que hi està mes aprop gràcies als hashmaps que guarden les distancies de cada robot al corner (calcul previ obligatori).
        double min = 10000;
        String robotMax = " ";
        switch (corn){
            case 0:
                for (Map.Entry<String, Double> set : corner0Pos.entrySet()) {
                    if(set.getValue() < min && !robotTeCorner.get(set.getKey())) {
                        min = set.getValue();
                        robotMax = set.getKey();
                    } 
                }
                break;
            case 1:
                for (Map.Entry<String, Double> set : corner1Pos.entrySet()) {
                    if(set.getValue() < min && !robotTeCorner.get(set.getKey())) {
                        min = set.getValue();
                        robotMax = set.getKey();
                    } 
                }
                break;
            case 2:
                for (Map.Entry<String, Double> set : corner2Pos.entrySet()) {
                    if(set.getValue() < min && !robotTeCorner.get(set.getKey())) {
                        min = set.getValue();
                        robotMax = set.getKey();
                    } 
                }    
                break;
            case 3:
                for (Map.Entry<String, Double> set : corner3Pos.entrySet()) {
                    if(set.getValue() < min && !robotTeCorner.get(set.getKey())) {
                        min = set.getValue();
                        robotMax = set.getKey();
                    } 
                }    
                break;
        }
        // Un sol robot envia el missatge per a que el robot X vagi al corner.
        if(getName().contains("1")) //nomes cal que ho envii un robot.
            sendMessage(robotMax, new Missatge(1, corn, corn));
        robotTeCorner.put(robotMax, true);
    }

    public void goToCorner(int corn){
        out.println("Vaig al corner pa\n");
        double cornerPosX, cornerPosY;
        double angle;
        // PER AL CORNER "corn", EL ROBOT SEGUEIX EL SEGUENT PROCEDIMENT:
        /* 
         *  1. Obté l'angle al que s'ha de dirigir per arribar al corner corresponent i va cap allà.
         *  2. 50 unitats de camp abans d'arribar al corner es para i es dirigeix en direcció vertical / horitzontal, depenent del corner.
         *  3. Avança de forma horitzontal / vertical les unitats que li quedin per arribar al cantó - 20, d'aquesta manera no toca el corner i evitem mal.
         *  4. Gira i avança la distancia que queda per arribar al corner en l'altre sentit (de vertical a horitzontal o de horitzontal a vertical).
         *  5. Si al acabar la funcio està a menys de 30 unitats de camp del corner, considerem que ho està i fiquem la variable "onCorner" a true.
        */
        // Aquest moviment vertical / horitzontal quan ja s'ha anat en direcció al corner es fa per evitar que el robot xoqui amb les parets.
        switch(corn){
            case 0:
                cornerPosX=0; cornerPosY=0;
                angle = getAngleTo(getX(), getY(), cornerPosX, cornerPosY);
                turnRight(-getHeading() + angle + 180);
                ahead(calcDist(getX(), getY(), cornerPosX, cornerPosY)-80);
                turnRight(180-getHeading());
                ahead(getY()-20);
                turnRight(90);
                ahead(getX()-20);
                if(calcDist(getX(),getY(),0,0)<30){
                    onCorner=true;
                }
                break;
            case 1:
                cornerPosX=getBattleFieldWidth(); cornerPosY=0;
                angle = getAngleTo(getX(), getY(), cornerPosX, cornerPosY);
                turnRight(-getHeading() + angle + 180);
                ahead(calcDist(getX(), getY(), cornerPosX, cornerPosY)-80);
                turnRight(180-getHeading());
                ahead(getY()-20);
                turnLeft(90);
                ahead(getBattleFieldWidth()-getX()-20);
                if(calcDist(getX(),getY(),getBattleFieldWidth(),0)<30){
                    onCorner=true;
                }
                break;
            case 2:
                cornerPosX=0; cornerPosY=getBattleFieldHeight();
                angle = getAngleTo(getX(), getY(), cornerPosX, cornerPosY);
                turnRight(-getHeading() + angle);
                ahead(calcDist(getX(), getY(), cornerPosX, cornerPosY)-80);
                turnRight(360-getHeading());
                ahead(getBattleFieldHeight()-getY()-20);
                turnLeft(90);
                ahead(getX()-20);
                if(calcDist(getX(),getY(),0,getBattleFieldHeight())<30){
                    onCorner=true;
                }
                break;
            case 3:
                cornerPosX=getBattleFieldWidth(); cornerPosY=getBattleFieldHeight(); 
                angle = getAngleTo(getX(), getY(), cornerPosX, cornerPosY);
                turnRight(-getHeading() + angle);
                ahead(calcDist(getX(), getY(), cornerPosX, cornerPosY)-80);
                turnRight(-getHeading());
                ahead(getBattleFieldHeight()-getY()-20);
                turnRight(90);
                ahead(getBattleFieldWidth()-getX()-20);
                if(calcDist(getX(),getY(),getBattleFieldWidth(),getBattleFieldHeight())<30){
                    onCorner=true;
                }

                break;
        }
    }

    // Funcions trigonometriques per a calcular distancies / angles.
    public double calcDist(double xOrg, double yOrg, double xDest, double yDest){
        //Funció usada per calcular la distància entre dos punts de coordenades
        return Math.sqrt(Math.pow(Math.abs(xOrg-xDest),2) + Math.pow(Math.abs(yOrg-yDest),2));
    }

    public double getAngleTo(double xOrg, double yOrg, double xDest, double yDest){
        double Y = (yDest-yOrg);
        double X = (xDest-xOrg);
        double angle = (Math.atan((X/Y)));
        return (Math.toDegrees(angle));
    }
}