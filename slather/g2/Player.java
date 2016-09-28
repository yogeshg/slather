package slather.g2;

import slather.sim.Cell;
import slather.sim.Point;
import slather.sim.Move;
import slather.sim.Pherome;
import java.util.*;

import slather.util.Vector;

public class Player implements slather.sim.Player {

    private Random gen;
    private double vision;
    private int tailLength;
    private double radius;
    private double dTheta;
    private double p_circle = 0;
    private double size;

    private Chiller chiller = null;
    // Doing new Chiller(); here throws stack over flow error. unclear why.

    public void init(double d, int t, int side_length) {
        System.out.println("Player init");

        gen = new Random(System.currentTimeMillis());
        vision = d;
        tailLength = t;
        radius = 2 * tailLength / (2*Math.PI);
        dTheta = 1 / radius;
        size = side_length;

        chiller = new Chiller();
        chiller.init(d, t, side_length);

    }

    public Move playCircle(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
        System.out.println("play circle");
        double theta = byte2angle( memory );
        double nextTheta = normalizeAngle(theta + dTheta,0);
        byte nextMemory = 0;
        Vector vector = null;
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
        return chiller.play(player_cell, memory, nearby_cells, nearby_pheromes);
    }

    //return true if there are more than one nearby friendly cell
    private boolean crowded(Cell player_cell, Set<Cell> nearby_cells){
        for(Cell c: nearby_cells){
            if(c.player == player_cell.player && c.getPosition().distance(player_cell.getPosition()) < 2*radius) return true;
        }
        return false;
    }

    //by default goes scout;
    //everytime we split there's a p_circle chance we go circle; 
    public Move play(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
        double theta = byte2angle( memory );
        // reproduce whenever possible
        if (player_cell.getDiameter() >= 2){
            byte memory2 = angle2byte( normalizeAngle(theta + Math.PI,0), memory );
            if(!byteIsCircle(memory) && (gen.nextDouble() < p_circle)) {
                //System.out.println("Generating a circle");
                memory2 = (byte) (memory2|ROLE_CIRCLE);
            } else {
                //System.out.println("Generating a scout");
                memory2 = (byte) (memory2& (~ROLE_CIRCLE));
            }
            return new Move(true, memory, memory2);
        } 

        //if surrounding is too crowded, play circle
        boolean nextCircle = byteIsCircle(memory);
        if(crowded(player_cell,nearby_cells)) nextCircle = true;
        if(!nextCircle) 
            return playScout(player_cell, memory, nearby_cells, nearby_pheromes);
        return playG1(player_cell, memory, nearby_cells, nearby_pheromes);
    }

    // check if moving player_cell by vector collides with any nearby cell or hostile pherome
    private boolean collides(Cell player_cell, Vector vector, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
        Iterator<Cell> cell_it = nearby_cells.iterator();
        Vector destination = new Vector( player_cell.getPosition().move(vector.getPoint()) );
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
    private static final int ROLE_SCOUT = 0 << ANGLE_BITS;
    private static final int ROLE_CIRCLE = 1 << ANGLE_BITS;

    private boolean byteIsCircle(byte b){
        //System.out.printf("%d %d %d\n",b,ROLE_MASK,ROLE_CIRCLE);
        return (b&ROLE_MASK)==ROLE_CIRCLE;
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

    private Vector angle2vector(double a) {
        double dx = Cell.move_dist * Math.cos(a);
        double dy = Cell.move_dist * Math.sin(a);
        return new Vector(dx, dy);
    }

    // convert an angle (in 2-deg increments) to a vector with magnitude Cell.move_dist (max allowed movement distance)
    private Vector extractVectorFromAngle(int arg) {
        double theta = Math.toRadians( 2* (double)arg );
        double dx = Cell.move_dist * Math.cos(theta + dTheta);
        double dy = Cell.move_dist * Math.sin(theta + dTheta);
        return new Vector(dx, dy);
    }


     public Move playScout(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
        System.out.println("play scout");
        if (player_cell.getDiameter() >= 2) // reproduce whenever possible
            return new Move(true, (byte)-1, (byte)-1);
        Vector position = new Vector(player_cell.getPosition());
        double radius = player_cell.getDiameter() * 0.5;
        Vector direction = new Vector(0, 0);
        for (Cell cell : nearby_cells) {
            Vector p = new Vector(cell.getPosition());
            double r = cell.getDiameter() * 0.5 + radius;
            double d = position.distance(p);

            Vector dir = correctedSubtract(p, position);

            dir = normalize(dir);
            if (Math.abs(d) > 1e-7)
                dir = multiply(dir, weight(d, r));

            direction = add(direction, multiply(dir, -1));
        }
        for (Pherome pherome : nearby_pheromes) {
            Vector p = new Vector(pherome.getPosition());
            double r = radius;
            double d = position.distance(p);
            
            Vector dir = correctedSubtract(p, position);

            dir = normalize(dir);
            if (Math.abs(d) > 1e-7)
                dir = multiply(dir, weight(d, r));

            direction = add(direction, multiply(dir, -1));
        }
        if (direction.norm() < 1e-8) {
            double angle = gen.nextDouble() * 2 * Math.PI - Math.PI;
            //double angle = 0;
            direction = new Vector(Math.cos(angle), Math.sin(angle));
        }
        direction = normalize(direction);
        return new Move(direction, (byte)0);
    }

    public Move playG1(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {

        Vector self_pos = new Vector(player_cell.getPosition());
        Vector other_pos = null;
        Vector difference = null;
        final float MIN_DISTANCE = tailLength;
        List<Vector> differences = new ArrayList<Vector>();

        for(Cell other : nearby_cells) {
            other_pos = new Vector(other.getPosition());
            // difference = getDistance(other_pos, self_pos);
            difference = correctedSubtract(other_pos, self_pos);
            if( Math.hypot( difference.x, difference.y ) <= MIN_DISTANCE ) {
                differences.add( difference );
            }
        }

        difference = new Vector(0,0);
        for(Vector p : differences) {
            difference = vectorAdd(difference, p);
        }

        difference = scalarMult(difference, -1);

        if( vectorLength(difference) < 0.001 ) {
            return playCircle(player_cell, memory, nearby_cells, nearby_pheromes);
        } else {
            difference = unitVector( difference );
            return new Move(difference, memory);
        }

    }


    private static double logGamma(double x) {
      double tmp = (x - 0.5) * Math.log(x + 4.5) - (x + 4.5);
      double ser = 1.0 + 76.18009173    / (x + 0)   - 86.50532033    / (x + 1)
                       + 24.01409822    / (x + 2)   -  1.231739516   / (x + 3)
                       +  0.00120858003 / (x + 4)   -  0.00000536382 / (x + 5);
      return tmp + Math.log(ser * Math.sqrt(2 * Math.PI));
   }
   private static double gamma(double x) { return Math.exp(logGamma(x)); }

    private double weight(double dist, double r) {
        final double lambda = 4;

        return Math.pow(lambda, dist) * Math.exp(-lambda) / gamma(dist);
    }

    @Deprecated
    private Vector add(Vector a, Vector b) {
        return new Vector(a.x + b.x, a.y + b.y);
    }

    @Deprecated
    private Vector subtract(Vector a, Vector b) {
        return new Vector(a.x - b.x, a.y - b.y);
    }

    @Deprecated
    private Vector correctedSubtract(Vector a, Vector b) {
        return a.getTaurusDistance(b, size  );
    }

    @Deprecated
    private Vector multiply(Vector a, double d) {
        return new Vector(a.x * d, a.y * d);
    }

    @Deprecated
    private Vector normalize(Vector a) {
        if (a.norm() < 1e-7) return a;
        else return multiply(a, 1.0/a.norm());
    }

    @Deprecated
    private double vectorLength(Vector p) {
        return Math.hypot(p.x, p.y);
    }

    @Deprecated
    private Vector scalarMult(Vector a, double t) {
        return new Vector(a.x*t, a.y*t);
    }

    @Deprecated
    private Vector vectorAdd(Vector a, Vector b) {
        return new Vector(a.x+b.x, a.y+b.y);
    }

    @Deprecated
    private Vector unitVector(Vector a) {
        double t = vectorLength(a);
        return scalarMult(a, 1/t);
    }

}
