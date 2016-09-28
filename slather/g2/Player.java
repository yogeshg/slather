package slather.g2;

import slather.sim.Cell;
import slather.sim.Point;
import slather.sim.Move;
import slather.sim.Pherome;
import java.util.*;

public class Player implements slather.sim.Player {

    private Random gen;
    private int tailLength;
    private double side_length;
    private double d;
    private double t;

    public void init(double d, int t, int side_length) {
        gen = new Random(System.currentTimeMillis());
        tailLength = t;
        radius = (1.1) * tailLength / (2*Math.PI);
        dTheta = 1 / radius;
        this.size = side_length;
    }

    //return true if there are more than one nearby friendly cell
    private boolean crowded(Cell player_cell, Set<Cell> nearby_cells){
        Iterator<Cell> itr = nearby_cells.iterator();
        int count = 0;
        while(itr.hasNext()){
            Cell c = itr.next();
            if(c.player == player_cell.player) count++;
        }
        return count >= 1;
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
        return playCircle(player_cell, memory, nearby_cells, nearby_pheromes);
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
}
