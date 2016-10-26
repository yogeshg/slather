package slather.g6;

import slather.sim.Cell;
import slather.sim.Point;
import slather.sim.Move;
import slather.sim.Pherome;
import slather.sim.GridObject;
import java.util.*;

public class Player implements slather.sim.Player {

	private Random gen;
	private int t;
	private double d;
	private static int cell_vision = 2;

	@Override
	public void init(double d, int t, int side_length) {
		gen = new Random();
		this.t = t;
		this.d = d;
	}

	@Override
	public Move play(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
		// reproduce whenever possible
		if (player_cell.getDiameter() >= 2) {
			return new Move(true, (byte) 0, (byte) 0);
		}
		Set<GridObject> neighbors = findNeighbors(player_cell, nearby_cells, nearby_pheromes);
		if (neighbors.size()==0) {
			// follow previous path
			Point vector = extractVectorFromAngle((int) memory);
			if (Double.isNaN(vector.x) || Double.isNaN(vector.y)) {
			}
			if (!collides(player_cell, vector, nearby_cells, nearby_pheromes))
				return new Move(vector, memory);
		}
		else if(neighbors.size()==1){
			Point vector = avoidCell(player_cell, neighbors.iterator().next());
			if (!collides(player_cell, vector, nearby_cells, nearby_pheromes))
				return new Move(vector, (byte)extractAngleFromVector(vector, player_cell));
		}
		else {
			Point largestAnglePath = findBestPath(player_cell, neighbors);
			if (!collides(player_cell, largestAnglePath, nearby_cells, nearby_pheromes)) {
				int angle = extractAngleFromVector(largestAnglePath, player_cell) / 2;
				if (Double.isNaN(largestAnglePath.x) || Double.isNaN(largestAnglePath.y)) {
				}
				return new Move(largestAnglePath, (byte) angle);
			}
		}
		// Generate a random new direction to travel
		for (int i = 0; i < 4; i++) {
			gen = new Random();
			int arg = gen.nextInt(180) + 1;
			Point vector = extractVectorFromAngle(arg);
			if (!collides(player_cell, vector, nearby_cells, nearby_pheromes))
				return new Move(vector, (byte) arg);
		}
		// if all tries fail, just chill in place
		return new Move(new Point(0, 0), (byte) 0);
	}
	private Point avoidCell(Cell pl_cell, GridObject gridObject) {
		int enemy_dir = extractAngleFromVector(gridObject.getPosition(), pl_cell);
		enemy_dir *= 2; // back to 360 degrees for easier mental arithmetic
		int my_cell_dir = (enemy_dir + 180) % 360; // opposite direction of
													// enemy
		my_cell_dir /= 2;
		return this.extractVectorFromAngle(my_cell_dir);
	}

	private Set<GridObject> findNeighbors(Cell player_cell, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
		Set<GridObject> neighbors = new HashSet<GridObject>();
		Set<GridObject> neighbors_samespecies = new HashSet<GridObject>();
		Set<GridObject> neighbors_enemies = new HashSet<GridObject>();

		for (Cell cell : nearby_cells) {
			if (player_cell.distance(cell) <= cell_vision) {
				if (cell.player == player_cell.player) {
					neighbors_samespecies.add(cell);
				} else {
					neighbors_enemies.add(cell);
				}
				neighbors.add(cell);
			}
		}
		for (Pherome pherome : nearby_pheromes) {
			if (player_cell.distance(pherome) <= cell_vision && pherome.player != player_cell.player) {
				neighbors.add(pherome);
			}
		}
		if (neighbors_samespecies.size() > neighbors_enemies.size()) {
			return neighbors_samespecies;
		}
		return neighbors;
	}

	private Set<Cell> findCellWithinVision(Set<Cell> nearby_cells, Cell player_cell) {
		Set<Cell> cells = new HashSet<>();
		for (Cell cell : nearby_cells) {
			if (player_cell.distance(cell) <= cell_vision) {
				cells.add(cell);
			}
		}
		return cells;
	}

	private Set<Pherome> findPheromeWithinVision(Set<Pherome> nearby_pheromes, Cell player_cell) {
		Set<Pherome> pheormes = new HashSet<>();
		for (Pherome p : nearby_pheromes) {
			if (player_cell.distance(p) <= cell_vision) {
				pheormes.add(p);
			}
		}
		return pheormes;
	}

	private Point findBestPath(Cell player_cell, Set<GridObject> nearbyObj) {
		List<Point> neighbors = new ArrayList<Point>();
		double largest = 0;
		int index = -1;
		for (GridObject cell : nearbyObj) {
			Point position = getNearbyPosition(player_cell.getPosition(), cell.getPosition());
			neighbors.add(position);
		}
		neighbors.sort(new PointsComparator());
		if (neighbors.size() > 1) {
			for (int i = 1; i < neighbors.size(); ++i) {
				double angle1 = Math.atan2(neighbors.get(i).y, neighbors.get(i).x);
				double angle2 = Math.atan2(neighbors.get(i - 1).y, neighbors.get(i - 1).x);
				if (angle1 - angle2 > largest) {
					largest = angle1 - angle2;
					index = i;
				}
			}
			double angle1 = Math.atan2(neighbors.get(0).y, neighbors.get(0).x);
			double angle2 = Math.atan2(neighbors.get(neighbors.size() - 1).y, neighbors.get(neighbors.size() - 1).x);
			if (largest < angle1 + 2 * Math.PI - angle2) {
				largest = angle1 + 2 * Math.PI - angle2;
				index = 0;
			}
			if (index < 1)
				index = neighbors.size() - 1;
			else
				index = index - 1;
			Point p = neighbors.get(index);
			double x, y;
			x = p.x * Math.cos(largest / 2) - p.y * Math.sin(largest / 2);
			y = p.y * Math.cos(largest / 2) + p.x * Math.sin(largest / 2);
			return new Point(x, y);
		} else if (neighbors.size() == 1) {
			return new Point(-neighbors.get(0).x, -neighbors.get(0).y);
		}
		return new Point(0, 0);
	}

	private Point getNearbyPosition(Point one, Point two) {
		double x = two.x;
		double y = two.y;
		double dis = 100;
		Point p = null;
		for (int x_coordinate = -1; x_coordinate <= 1; x_coordinate++) {
			for (int y_coordinate = -1; y_coordinate <= 1; y_coordinate++) {
				x = two.x + x_coordinate * 100;
				y = two.y + y_coordinate * 100;
				double d = getDistanceOfTwoPoints(one, new Point(x, y));

				if (dis > d) {
					dis = d;
					p = new Point(x - one.x, y - one.y);
				}
			}
		}

		double X = p.x, Y = p.y;
		double L = Math.hypot(X, Y);
		if (L == 0.0) {
			return new Point(0, 0);
		}
		X /= L;
		Y /= L;
		return new Point(X, Y);
	}

	private double getDistanceOfTwoPoints(Point one, Point second) {
		double dist_square = (one.x - second.x) * (one.x - second.x) + (one.y - second.y) * (one.y - second.y);
		double dist = Math.sqrt(dist_square);
		return dist;
	}

	// check if moving player_cell by vector collides with any nearby cell or
	// hostile pherome
	private boolean collides(Cell player_cell, Point vector, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
		Iterator<Cell> cell_it = nearby_cells.iterator();
		Point destination = player_cell.getPosition().move(vector);
		while (cell_it.hasNext()) {
			Cell other = cell_it.next();
			if (destination.distance(other.getPosition()) < 0.5 * player_cell.getDiameter() + 0.5 * other.getDiameter()
					+ 0.00011)
				return true;
		}
		Iterator<Pherome> pherome_it = nearby_pheromes.iterator();
		while (pherome_it.hasNext()) {
			Pherome other = pherome_it.next();
			if (other.player != player_cell.player
					&& destination.distance(other.getPosition()) < 0.5 * player_cell.getDiameter() + 0.0001)
				return true;
		}
		return false;
	}

	private int extractAngleFromVector(Point arg, Cell player_cell) {
		double x = player_cell.getPosition().x;
		double y = player_cell.getPosition().y;

		if (x == arg.x) { // cell is either directly above or below ours
			if (y > arg.y) { // go up
				return 45; // 90/2
			} else { // otherwise go down
				return 135; // 270/2
			}
		}

		double dx = arg.x - x;
		double dy = arg.y - y;
		double angle = Math.atan(dy / dx);
		if (arg.x < x)
			angle += Math.PI;
		if (angle < 0)
			angle += 2 * Math.PI;
		return (int) (Math.toDegrees(angle) / 2);
	}

	// convert an angle (in 2-deg increments) to a vector with magnitude
	private Point extractVectorFromAngle(int arg) {
		double theta = Math.toRadians(2 * (double) arg);
		double dx = Cell.move_dist * Math.cos(theta);
		double dy = Cell.move_dist * Math.sin(theta);
		return new Point(dx, dy);
	}
}
