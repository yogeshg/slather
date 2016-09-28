package slather.g2;

import slather.sim.Cell;
import slather.sim.Point;
import slather.sim.Move;
import slather.sim.Pherome;
import java.util.*;

import slather.util.Vector;


public class Chiller extends Player {

    public void init(double d, int t, int side_length) {
        System.out.println("Chiller init");
    }

    public Move play(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
        return new Move(new Point(0,0), memory);
    }

}