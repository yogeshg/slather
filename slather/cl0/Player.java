package slather.cl0;

import slather.sim.Cell;
import slather.sim.Point;
import slather.sim.Move;
import slather.sim.Pherome;
import java.util.*;
import java.lang.*;

// Either there's a giant bug.
// Or I'm complete rekt by the random bot


public class Player implements slather.sim.Player {

        private class Event implements Comparable<Event> {
            public double value;
            public int index;

            public Event(double value, int index) {
                this.value = value;
                this.index = index;
            }

            public int compareTo(Event a) {
                return Double.compare(value, a.value);
            }
        };

    private Random gen;

    public void init(double d, int t) {
        gen = new Random();
    }

    public Move play(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
        if (player_cell.getDiameter() >= 2) // reproduce whenever possible
            return new Move(true, (byte)-1, (byte)-1);



        //Below I treat my cell as a point, and every other has radius increase by a certain number
        Point position = player_cell.getPosition();
        double radius = player_cell.getDiameter() * 0.5;
        int index = 0;
        List<Event> dirs = new ArrayList<Event>();
        for (Cell cell : nearby_cells) {
            ++ index;
            Point p = cell.getPosition();
            double r = cell.getDiameter() * 0.5 + radius;
            double delta = Math.asin(Math.min(1.0, r / position.distance(p)));
            double angle = Math.atan2(p.y - position.y, p.x - position.x);
            double left = angle - delta, right = angle + delta;
            if (left < - Math.PI) left += 2 * Math.PI;
            if (right > Math.PI) right -= 2 * Math.PI;
            dirs.add(new Event(left, index)); dirs.add(new Event(right, -index));
        }
        for (Pherome pheromes : nearby_pheromes) {
            ++ index;
            Point p = pheromes.getPosition();
            double delta = Math.asin(Math.min(1.0, radius / position.distance(p)));
            double angle = Math.atan2(p.y - position.y, p.x - position.x);
            double left = angle - delta, right = angle + delta;
            if (left < - Math.PI) left += 2 * Math.PI;
            if (right > Math.PI) right -= 2 * Math.PI;
            dirs.add(new Event(left, index)); dirs.add(new Event(right, -index));
        }

        Collections.sort(dirs);

        int l = dirs.size();
        double width = -1, direction = -Math.PI + gen.nextDouble() * 2 * Math.PI;
        Set<Integer> inStack = new HashSet<Integer>();

        for (int i = 0; i < l; ++ i) {
            if (inStack.isEmpty() && i > 0) {
                double w = dirs.get(i).value - dirs.get(i - 1).value;
                if (w > width) {
                    width = w;
                    direction = 0.5 * (dirs.get(i).value + dirs.get(i - 1).value);
                }
            }

            Event e = dirs.get(i);
            if (e.index > 0) inStack.add(e.index);
            else inStack.remove(-e.index);
        }

        if (inStack.isEmpty() && dirs.size() > 0) {
            double L = dirs.get(l - 1).value;
            double R = dirs.get(0).value + 2 * Math.PI;
            if (R - L > width) {
                width = R - L;
                direction = 0.5 * (R + L);
                if (direction > Math.PI) direction -= 2 * Math.PI;
                if (direction < - Math.PI) direction += 2 * Math.PI;
            }
        }

        //if (width > 0) 
        return new Move(new Point(Math.cos(direction), Math.sin(direction)), (byte)0);
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
