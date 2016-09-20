package slather.cl2;

import slather.sim.Cell;
import slather.sim.Point;
import slather.sim.Move;
import slather.sim.Pherome;
import java.util.*;
import java.lang.*;

// Either there's a giant bug.
// Or I'm complete rekt by the random bot


public class Player implements slather.sim.Player {

	private double size = 100;

    private Random gen;
    private double vision;
    private int fade;

    public void init(double d, int t) {
        gen = new Random();
        vision = d;
        fade = t;
    }

    public Move play(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
        if (player_cell.getDiameter() >= 2) // reproduce whenever possible
            return new Move(true, (byte)-1, (byte)-1);



        //Below I treat my cell as a point, and every other has radius increase by a certain number
        double acc_x = 0, acc_y = 0;
        Point position = player_cell.getPosition();
        double radius = player_cell.getDiameter() * 0.5;
        for (Cell cell : nearby_cells) {
            Point p = cell.getPosition();
            double r = cell.getDiameter() * 0.5 + radius;
            double d = position.distance(p);
            double dx = p.x - position.x, dy = p.y - position.y;
            if (dx > size - vision) dx -= size;
            if (dx < vision - size) dx += size;
            if (dy > size - vision) dy -= size;
            if (dy < vision - size) dy += size;
            if (Math.abs(d) < 1e-7) continue;
            dx /= d; dy /= d; 
            double amp = Math.asin(Math.min(1, r / d));
            dx *= amp; dy *= amp;

            acc_x -= dx; acc_y -= dy;
        }
        for (Pherome pherome : nearby_pheromes) {
            Point p = pherome.getPosition();
            double r = radius;
            double d = position.distance(p);
            double dx = p.x - position.x, dy = p.y - position.y;
            if (dx > size - vision) dx -= size;
            if (dx < vision - size) dx += size;
            if (dy > size - vision) dy -= size;
            if (dy < vision - size) dy += size;
            if (Math.abs(d) < 1e-7) continue;
            dx /= d; dy /= d;

            double amp = Math.asin(Math.min(1, r / d));
            dx *= amp; dy *= amp;

            acc_x -= dx; acc_y -= dy;
        }

        //System.out.println(nearby_cells.size());
        //System.out.println(nearby_pheromes.size());

        //if (width > 0) 
        if (Math.abs(acc_x) < 1e-7 && Math.abs(acc_y) < 1e-7) {
        	double angle = gen.nextDouble() * 2 * Math.PI - Math.PI;
        	acc_x = Math.cos(angle);
        	acc_y = Math.sin(angle);
        }
        double t = Math.hypot(acc_x, acc_y);
        return new Move(new Point(acc_x / t, acc_y / t), (byte)0);
        // Choose an arbitrary direction
        //return new Move(new Point(1,0), (byte)0);
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

    // convert an angle (in 2-deg increments) to a vector with magnitude Cell.move_dist (max allowed movement distance)
    private Point extractVectorFromAngle(int arg) {
        double theta = Math.toRadians( 2* (double)arg );
        double dx = Cell.move_dist * Math.cos(theta);
        double dy = Cell.move_dist * Math.sin(theta);
        return new Point(dx, dy);
    }

}
