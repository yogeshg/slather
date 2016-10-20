package slather.g2;

import slather.sim.Cell;
import slather.sim.Point;
import slather.sim.Move;
import slather.sim.Pherome;
import java.util.*;

import slather.g2.util.Vector;


public class Landmark extends Player {

    Vector LANDMARK;
    double RADIUS;

    private static final double TWOPI = 2*Math.PI;

    public void init(double d, int t, int side_length) {
        System.out.println("Landmark init");

        this.RANDOM_GENERATOR = new Random(System.currentTimeMillis());
        this.VISION = d;
        this.TAIL_LENGTH = t;
        this.BOARD_SIZE = side_length;

        getPropertiesSafe();

        LANDMARK = new Vector(50,50);
        RADIUS = this.RADIUS_TO_TAIL_RATIO * t / (TWOPI);
    }

    public Move play(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
        Set<Cell> friendly_inside = new HashSet<Cell>();
        Set<Cell> friendly_outside = new HashSet<Cell>();
        Set<Cell> foes = new HashSet<Cell>();

        Vector position = new Vector( player_cell.getPosition() );
        final Vector fromLandmark = position.getTaurusDistance(LANDMARK, BOARD_SIZE);
        final Vector tangentToLandmark = new Vector(fromLandmark.y, -fromLandmark.x);
        final double landmarkSide = fromLandmark.multiply(-1).crossProduct(tangentToLandmark);

        final int player = player_cell.player;
        for( Cell c : nearby_cells) {
            final Vector cPosition = new Vector(c.getPosition());
            final Vector cDifference = cPosition.getTaurusDistance(position,BOARD_SIZE);
            final double cDistance = cDifference.norm();
            if( player == c.player && (RADIUS > cDistance) ) {
                final double cSide = cDifference.crossProduct(tangentToLandmark);
                if ( 0 < landmarkSide * cSide ) {
                    friendly_outside.add(c);
                } else {
                    friendly_inside.add(c);
                }
            } else {
                foes.add(c);
            }
        }

        // System.out.print( fromLandmark.toString() + foes.size()+"," + friendly_outside.size()+"," + friendly_inside.size()+"," );

        final int extraOutside = friendly_outside.size() - friendly_inside.size();

        if( (0 < foes.size()) ) {
            // System.out.println("Move in");
            final Vector dir = tangentToLandmark.unitVector().add( fromLandmark.multiply(-0.1) ).unitVector();
            return new Move(dir, memory);
        } else if( 0>extraOutside ) {
            // System.out.println("Move out");
            final Vector dir = tangentToLandmark.unitVector().add( fromLandmark.multiply(100) ).unitVector();
            return new Move( dir , memory);
        } else {
            // System.out.println("Move circular");
            final double randomFactor = (RANDOM_GENERATOR.nextDouble() > 0.999) ? 0.1 : 0.0;
            final Vector dir = tangentToLandmark.unitVector().add( fromLandmark.multiply( randomFactor ) ).unitVector();
            return new Move(dir, memory);
        }
        // return new Move(new Point(0,0), memory);
    }

    public Move reproduce(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
        byte b = vector2byte(new Vector(player_cell.getPosition()), memory);
        return new Move(true, b, b);
    }

    public byte vector2byte(Vector v, byte b) {
        double actualAngle = (v.x/BOARD_SIZE) * ANGLE_MAX;
        System.out.println(v.x);
        final int anglePart = (int) (((int)actualAngle) & this.ANGLE_MASK);
        final byte memoryPart = (byte) ( b & ~this.ANGLE_MASK);
        return (byte) ((anglePart | memoryPart));
    }

    public Vector byte2vector(byte b) {
        double x = BOARD_SIZE * ((double)(b & this.ANGLE_MASK))/this.ANGLE_MAX;
        System.out.println(x);
        return new Vector(x, 25);
    }

}
