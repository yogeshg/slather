package slather.g2;

import slather.sim.Cell;
import slather.sim.Point;
import slather.sim.Move;
import slather.sim.Pherome;
import java.util.*;

import slather.g2.util.Vector;


public class GridCircler extends Chiller {

    private Grider grider = null;
    private Landmark circler = null;
    private Scout scout = null;
    float EPSILON;


    public void init(double d, int t, int side_length) {
        System.out.println("GridCircler init");

        grider = new Grider();
        grider.init(d, t, side_length);

        circler = new Landmark();
        circler.init(d, t, side_length);
        // circler.RADIUS = 2.0 * t / (2*Math.PI);

        scout = new Scout();
        scout.init(d, t, side_length);

        this.EPSILON = (float)0.001;

    }

    public Move play(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
        Move m = null;

        m = grider.play(player_cell, memory, nearby_cells, nearby_pheromes);

        if(m.vector.norm() < this.EPSILON ){
            m = circler.play(player_cell, memory, nearby_cells, nearby_pheromes);
        }

        Move m2 = null;
        if((m.vector.norm() < this.EPSILON ) || (this.collides( player_cell, new Vector (m.vector), nearby_cells, nearby_pheromes))){
            m2 = scout.play(player_cell, memory, nearby_cells, nearby_pheromes);
            if((m2.vector.norm() < this.EPSILON ) || (this.collides( player_cell, new Vector (m2.vector), nearby_cells, nearby_pheromes))){
                Vector dir;
                
                while( (this.collides( player_cell, new Vector (m2.vector), nearby_cells, nearby_pheromes)) && (m.vector.norm() > this.EPSILON) ) {
                    dir = new Vector( m.vector );
                    dir = dir.multiply(0.9);
                    m = new Move( dir, memory );
                }
                
            } else {
                m = m2;
            }
        }

        // if all tries fail, just chill in place
        return m;
    }

    public Move reproduce(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
        return circler.reproduce(player_cell, memory, nearby_cells, nearby_pheromes);
    }

}