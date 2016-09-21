package slather.g21;

import slather.sim.Cell;
import slather.sim.Point;
import slather.sim.Move;
import slather.sim.Pherome;
import java.util.*;

public class Player implements slather.sim.Player {

    private Random gen;
    private int tailLength;
    private double radius;
    private double dTheta;
    private double p_scout = 0.5;


    public void init(double d, int t) {
        gen = new Random(System.currentTimeMillis());
        tailLength = t;
        radius = 2 * tailLength / (2*Math.PI);
        dTheta = 1 / radius;
        System.out.println("vision: " + d);
    }

    public Move playCircle(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
        double theta = byte2angle( memory );
        double nextTheta = normalizeAngle(theta + dTheta,0);
        byte nextMemory = 0;
        Point vector = null;
        // Try to go any of four normal directions.
        for(int i=0; i<4; ++i) {
            nextMemory = angle2byte( nextTheta, memory );
            // System.out.println("memory,angles " +memory+","+nextMemory+"," +theta+","+nextTheta);
            vector = angle2vector( nextTheta);
            if (!collides( player_cell, vector, nearby_cells, nearby_pheromes))
                return new Move(vector, nextMemory);
            nextTheta += Math.PI/2;
        }

        // if all tries fail, just chill in place
        return new Move(new Point(0,0), (byte)0);
    }

    public Move playScout(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
        double theta = byte2angle( memory );
        double nextTheta = theta;
        byte nextMemory = 0;
        Point vector = null;
        // Try to go any of four normal directions.
        for(int i=0; i<4; ++i) {
            nextMemory = angle2byte( nextTheta, memory );
            vector = angle2vector( nextTheta);
            if (!collides( player_cell, vector, nearby_cells, nearby_pheromes))
                return new Move(vector, nextMemory);
            nextTheta += Math.PI/2;
        }

        // if all tries fail, just chill in place
        return new Move(new Point(0,0), (byte)0);
    }

    public Move play(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
        double theta = byte2angle( memory );
        if (player_cell.getDiameter() >= 2){
            byte memory2 = angle2byte( normalizeAngle(theta + Math.PI,0), memory );
            if( !byteIsScout(memory) && (gen.nextDouble() < p_scout)) {
                //System.out.println("Generating a scout");
                memory2 = (byte) (memory2|ROLE_SCOUT);
            } else {
                memory2 = (byte) (memory2|ROLE_CIRCLE);
            }
            return new Move(true, memory, memory2);
        } // reproduce whenever possible
            
        // if (memory > 0) { // follow previous direction unless it would cause a collision
        // Try to go in circle

        if(byteIsScout(memory)) {
            return playScout(player_cell, memory, nearby_cells, nearby_pheromes);
        } else {
            return playCircle(player_cell, memory, nearby_cells, nearby_pheromes);
        }

        // if all tries fail, just chill in place
        // return new Move(new Point(0,0), (byte)0);
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

    private static final int ANGLE_BITS = 5;
    private static final int ANGLE_MIN = 0;
    private static final int ANGLE_MAX = 1 << ANGLE_BITS;
    private static final int ANGLE_MASK = ANGLE_MAX - 1;

    private static final int ROLE_BITS = 1;
    private static final int ROLE_MIN = ANGLE_MAX;
    private static final int ROLE_MAX = ROLE_MIN + ( 1<<ROLE_BITS );
    private static final int ROLE_MASK = (ROLE_MAX - 1) & ~ANGLE_MASK;
    private static final int ROLE_CIRCLE = 0 << ANGLE_BITS;
    private static final int ROLE_SCOUT = 1 << ANGLE_BITS;

    private boolean byteIsScout(byte b){
        // System.out.printf("%d %d %d\n",b,ROLE_MASK,ROLE_SCOUT);
        return (b&ROLE_MASK)==ROLE_SCOUT;
    }

    private static final double TWOPI = 2*Math.PI;

    private double byte2angle(byte b) {
        // -128 <= b < 128
        // -1 <= b/128 < 1
        // -pi <= a < pi
        return normalizeAngle(TWOPI * (((double) ((b) & ANGLE_MASK) ) / ANGLE_MAX), 0);
    }

    private byte angle2byte(double a, byte b) {
        final double actualAngle = ((normalizeAngle(a,0) / TWOPI)*ANGLE_MAX);
        final int anglePart = (int) (((int)actualAngle) & ANGLE_MASK);
        final byte memoryPart = (byte) ( b & ~ANGLE_MASK);
        // System.out.println("angle2byte "+ memoryPart +","+ anglePart +","+ (normalizeAngle(a,0)/TWOPI) +","+ ANGLE_MAX +","+ a);
        return (byte) ((anglePart | memoryPart));
    }

    private double normalizeAngle(double a, double start) {
        if( a < start ) {
            return normalizeAngle( a+TWOPI, start);
        } else if (a >= (start+TWOPI)) {
            return normalizeAngle( a-TWOPI, start);
        } else {
            return a;
        }
    }

    private Point angle2vector(double a) {
        double dx = Cell.move_dist * Math.cos(a);
        double dy = Cell.move_dist * Math.sin(a);
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
