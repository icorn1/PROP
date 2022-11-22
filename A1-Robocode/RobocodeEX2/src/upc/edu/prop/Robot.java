package upc.edu.prop;

import java.awt.Color;
import java.io.IOException;
import java.util.*;
import robocode.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;

/**
 * @author icorn & almogawer
 */
public class Robot extends TeamRobot{
    private int dist = 750;
    private boolean firstScan = true;
    private boolean eliminant = false;
    private Map<String, Double> distancies = new HashMap<String, Double>();
    private Map<String, Double> energies = new HashMap<String, Double>();
    private Map<String, Double> anglesEquip = new HashMap<String, Double>();

    /*
     * Cada Robot comença a escanejar d'immediat. Han de fer un escaneig complet i el que tinguin més aprop, disparar-li.
     * Quan li disparen:
     *      Es mou en direcció oposada al dispar.
     * Quan escaneja un aliat: 
     *      Guarda el seu angle i evita disparar-li indirectament.
     * while true:
     *      Mou-te endevant i endarrere i continua escanejant.
     */
    public void run() {
        inici();
        while (true){
            setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
            back(dist);
            ahead(dist);
            execute(); 
        }
    }
    
    public void onScannedRobot(ScannedRobotEvent e) {
        if(!isTeammate(e.getName())){   
            if(!firstScan){             // If de seguretat
                if(e.getName() == getTarget()){
                    // Hem observat que si parem el robot per disparar, es millora l'eficiencia per un 2%.
                    stop();
                    if(energies.get(e.getName())-e.getEnergy()>0){ //mirem si ha disparat el enemic escanejat mes proper amb la diferencia de energia
                        setAhead(36);               //avancem la mida del robot cap endavant
                    }
                    energies.put(e.getName(), e.getEnergy());

                    // Obtenim l'angle al que es preveu que anira el robot, per disparar-lo.
                    double theta = getAngleMoviment(e.getDistance(), e.getBearing());
                    double firePower = Math.min(900 / e.getDistance(), 3);
                    if(!friendlyFire(theta)){
                        turnGunRight(normalRelativeAngleDegrees(theta - getGunHeading()));
                        setFire(firePower);
                    }
                    resume();
                    scan(); //Tornem a cridar el mètode.
                }
            }
            else{
                firstScan = false;
            }

            if(!eliminant){
                // If de seguretat per errors de paralelisme.
                energies.put(e.getName(), e.getEnergy());
                distancies.put(e.getName(), e.getDistance());
            }
            else eliminant = false;
        }
        else{
            // Obtenim l'angle al que es preveu que anira el robot, per emmagatzemar-ho.
            if(!eliminant){
                double angle = getAngleMoviment(e.getDistance(), e.getBearing());
                anglesEquip.put(e.getName(), angle);
            }
            else eliminant = false;
        }
    }

    public void onRobotDeath(RobotDeathEvent e){
        // Eliminem del Hashmap l'informacio relacionada.
        if(!isTeammate(e.getName())){
            distancies.remove(e.getName());
            energies.remove(e.getName());
            eliminant = true;
        }
        else{
            anglesEquip.remove(e.getName());
            eliminant = true;
        }
    }
    
    public void onHitByBullet(HitByBulletEvent e){
        //Ens movem i evitem tornar ser disparats.
        setTurnRight(normalRelativeAngleDegrees(90 - (getHeading() - e.getHeading())));
		setAhead(dist);
		dist *= -1;
		scan();
    }

    public void inici() {
        // Inicialitzem colors.
        setBodyColor(Color.black);
        setGunColor(Color.red);
        setRadarColor(Color.red);
        setBulletColor(Color.red);
        setScanColor(Color.red);
    }

    public String getTarget(){
        // Es basa en la distancia per escollir el target.
        double min = 10000;
        String robotMesProper = "";
        for (Map.Entry<String, Double> set : distancies.entrySet()) {
            if(set.getValue() < min) {
                min = set.getValue();
                robotMesProper = set.getKey();
            } 
        }
        return robotMesProper;
    }

    public boolean friendlyFire(double angle){
        // Funció per, donat un angle "a" i sabent els angles de nosaltres als companys, detectar si "a" es similar a algun angle i evitar friendly fire
        for (Map.Entry<String, Double> set : anglesEquip.entrySet()) {
            int margeAngles = 7; 
            int angleCompanyArrodonit = (int) Math.round(set.getValue());
            if(angleCompanyArrodonit - margeAngles < angle && angleCompanyArrodonit + margeAngles > angle) {
                return true; 
            } 
        }
        return false;
    }

    public double calcDist(double xOrg, double yOrg, double xDest, double yDest){
        //Funció usada per calcular la distància entre dos punts de coordenades
        //return Math.sqrt(Math.pow(Math.abs(xOrg-xDest),2) + Math.pow(Math.abs(yOrg-yDest),2));
        return Math.hypot((xDest-xOrg), (yDest- yOrg));
    }
    
    public double getAngleTo(double xOrg, double yOrg, double xDest, double yDest){
        // Retorna l'angle de Punt(a) a punt(b).
        return Math.toDegrees(Math.atan2(xDest-xOrg, yDest-yOrg));
    }
    
    public double getAngleMoviment(double distancia, double bearing){
        // Calcular el bearing del enemic
        double enemyBearing = this.getHeading() + bearing;
        // Calcular posicio del enemic
        double enemyX = getX() + distancia * Math.sin(Math.toRadians(enemyBearing));
        double enemyY = getY() + distancia * Math.cos(Math.toRadians(enemyBearing));
        // Calcular x i y futures del target
        double dx = enemyX - this.getX();
        double dy = enemyY - this.getY();
        // Calcular angle
        //return Math.toDegrees(Math.atan2(dx, dy));
        return Math.toDegrees(Math.atan2(dx, dy));
    }
}