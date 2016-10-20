package slather.g2;

import slather.sim.Cell;
import slather.sim.Point;
import slather.sim.Move;
import slather.sim.Pherome;
import java.util.*;

import slather.g2.util.Vector;


public class AwayFromBig extends Player {

    // Vector LANDMARK;
    double RADIUS;

    private static final double TWOPI = 2*Math.PI;

    public void init(double d, int t, int side_length) {
        System.out.println("AwayFromBig init");

        this.RANDOM_GENERATOR = new Random(System.currentTimeMillis());
        this.VISION = d;
        this.TAIL_LENGTH = t;
        this.BOARD_SIZE = side_length;

        getPropertiesSafe();

        RADIUS = 5;
        // RADIUS = this.RADIUS_TO_TAIL_RATIO * t / (TWOPI);
    }

    public Move play(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
        Set<Cell> friendly = new HashSet<Cell>();
        Set<Cell> foes = new HashSet<Cell>();

        double maxDia = 0;
        Cell maxDiaFriendly = null;

        Vector position = new Vector( player_cell.getPosition() );

        final int player = player_cell.player;
        for( Cell c : nearby_cells) {
            final Vector cPosition = new Vector(c.getPosition());
            final double cDistance = cPosition.getTaurusDistance(position,BOARD_SIZE).norm();
            if( player == c.player && (RADIUS > cDistance) ) {
                friendly.add(c);
                if( c.getDiameter() > maxDia ) {
                    maxDia = c.getDiameter();
                    maxDiaFriendly = c;
                }
                
            } else {
                foes.add(c);
            }
        }
        if( 0 == foes.size() ) {
            if( maxDiaFriendly!=null && (player_cell.getDiameter() <= maxDiaFriendly.getDiameter() ) ) {
                Vector p = new Vector( player_cell.getPosition() );
                p = p.getTaurusDistance( maxDiaFriendly.getPosition(), BOARD_SIZE );
                p = p.unitVector();
                // Move away from maxDiaFriendly
                // as far as you can move in that direction.
                return new Move(p, memory);
            }
        }
        return new Move(new Point(0,0), memory);
    }

    public Move reproduce(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
        return new Move(true, memory, memory);
    }

}
