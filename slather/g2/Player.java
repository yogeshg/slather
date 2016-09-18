package slather.g2;

import slather.sim.Cell;
import slather.sim.Point;
import slather.sim.Move;
import slather.sim.Pherome;
import java.util.*;


public class Player implements slather.sim.Player {

    private Random gen;
    private int tailLength;
    double radius;
    double dTheta;

    public void init(double d, int t) {
        gen = new Random();
        tailLength = t;
        radius = tailLength / (2*Math.PI);
        dTheta = 1 / radius;
    }

    public Move play(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
        if (player_cell.getDiameter() >= 2) // reproduce whenever possible
            return new Move(true, (byte)-1, (byte)-1);
        // if (memory > 0) { // follow previous direction unless it would cause a collision
        // Try to go in circle
        double theta = byte2angle( memory );
        theta = normalizeAngle(theta + dTheta);
        memory = angle2byte( theta );
        Point vector = angle2vector(theta);
        // check for collisions
        if (!collides( player_cell, vector, nearby_cells, nearby_pheromes))
            return new Move(vector, memory);
        // }

        // if no previous direction specified or if there was a collision, try random directions to go in until one doesn't collide
        for (int i=0; i<4; i++) {
            // Try to make a recursive call here
            byte arg = (byte) (gen.nextInt(256)-128);
            vector = angle2vector( byte2angle(arg) );
            if (!collides(player_cell, vector, nearby_cells, nearby_pheromes)) 
                return new Move(vector, arg);
        }

        // if all tries fail, just chill in place
        return new Move(new Point(0,0), (byte)0);
    }

    // check if moving player_cell by vector collides with any nearby cell or hostile pherome
    private boolean collides(Cell player_cell, Point vector, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
        Iterator<Cell> cell_it = nearby_cells.iterator();
        Point destination = player_cell.getPosition().move(vector);
        while (cell_it.hasNext()) {
            Cell other = cell_it.next();
            if ( destination.distance(other.getPosition()) < 0.5*player_cell.getDiameter() + 0.5*other.getDiameter() + 0.00011) 
                return true;
        }
        Iterator<Pherome> pherome_it = nearby_pheromes.iterator();
        while (pherome_it.hasNext()) {
            Pherome other = pherome_it.next();
            if (other.player != player_cell.player && destination.distance(other.getPosition()) < 0.5*player_cell.getDiameter() + 0.0001) 
                return true;
        }
        return false;
    }

    private double byte2angle(byte b) {
        // -128 <= b < 127
        // -1 <= b/128 < 1
        // -pi <= a < pi
        return Math.PI * ((double) b)/128;
    }

    private byte angle2byte(double a) {
        return (byte)(int)(128*(a/Math.PI));
    }

    private double normalizeAngle(double a) {
        double ap = a-2*Math.PI;
        if( ap >= -Math.PI ) {  // a > Math.PI
            return ap;
        }                       // Math.PI < a
        ap = a+2*Math.PI;
        if( ap <= Math.PI ) {   // a <= -Math.PI
            return ap;
        }
        return a;
    }

    private Point angle2vector(double a) {
        double dx = Cell.move_dist * Math.cos( a );
        double dy = Cell.move_dist * Math.sin( a );
        return new Point(dx, dy);
    }

    // convert an angle (in 2-deg increments) to a vector with magnitude Cell.move_dist (max allowed movement distance)
    private Point extractVectorFromAngle(int arg) {
        double theta = Math.toRadians( 2* (double)arg );

        double dx = Cell.move_dist * Math.cos(theta + dTheta);
        double dy = Cell.move_dist * Math.sin(theta + dTheta);
        return new Point(dx, dy);
    }

}
