package slather.g2;

import slather.sim.Cell;
import slather.sim.Point;
import slather.sim.Move;
import slather.sim.Pherome;
import java.util.*;

import slather.util.Vector;

public class Player implements slather.sim.Player {

    protected Random RANDOM_GENERATOR;
    protected double VISION;
    protected int TAIL_LENGTH;
    protected double BOARD_SIZE;

    protected double PROB_CIRCLE = 0;

    private Chiller chiller = null;
    private Chiller occupier = null;
    private Chiller scout = null;

    // Initialize here, initialize sub strategies here.
    // Take care to initialize VISION, TAIL_LENGTH, BOARD_SIZE, RANDOM_GENERATOR in your subclass
    // Do not make recursive calls to this init.

    public void init(double d, int t, int side_length) {
        System.out.println("Player init");

        this.RANDOM_GENERATOR = new Random(System.currentTimeMillis());
        this.VISION = d;
        this.TAIL_LENGTH = t;
        this.BOARD_SIZE = side_length;

        chiller = new Chiller();
        chiller.init(d, t, side_length);

        occupier = new Chiller();
        occupier.init(d, t, side_length);

        scout = new Chiller();
        scout.init(d, t, side_length);
    }

    public Move play(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
        // reproduce whenever possible
        if (player_cell.getDiameter() >= 2){
            if(!byteIsCircle(memory) && (RANDOM_GENERATOR.nextDouble() < PROB_CIRCLE)) {
                return occupier.reproduce(player_cell, memory, nearby_cells, nearby_pheromes);
            } else {
                return scout.reproduce(player_cell, memory, nearby_cells, nearby_pheromes);
            }
        }
        boolean nextIsCircle = byteIsCircle(memory);
        // if(scout.crowded(player_cell,nearby_cells)){
        //     nextIsCircle = true;
        // }
        if(!nextIsCircle) {
            return scout.play(player_cell, memory, nearby_cells, nearby_pheromes);
        } else {
            return occupier.play(player_cell, memory, nearby_cells, nearby_pheromes);
        }
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

}
