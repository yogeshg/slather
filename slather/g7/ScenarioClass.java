package slather.g7;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import slather.sim.Cell;
import slather.sim.GridObject;
import slather.sim.Pherome;
import slather.sim.Point;

public class ScenarioClass {
	
	private static double RATIO_THRESHOLD = 0.75;

	public static boolean isEmpty(Cell myCell, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
		return (nearby_cells.isEmpty() && nearby_pheromes.isEmpty());
	}

	public static boolean isAboutToReproduce(Cell myCell, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
		return (myCell.getDiameter() > ToolBox.diameterBeforeReproduction);
	}

	public static boolean isOnlyMyPherome(Cell myCell, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {

		if (!nearby_cells.isEmpty()) return false;
		for (Pherome p : nearby_pheromes) {
			if (p.player != myCell.player) {
				return false;
			}
		}
		
		return true;
	}

	public static boolean isOnlyFriends(Cell myCell, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
		for (Cell c : nearby_cells) {
			if (c.player != myCell.player)
				return false;
		}

		return !nearby_cells.isEmpty();
	}

	public static boolean isOnlyEnemies(Cell myCell, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
		for (Cell c : nearby_cells) {
			if (c.player == myCell.player)
				return false;
		}

		return !nearby_cells.isEmpty();
	}

	public static boolean isNearbyFriends(Cell myCell, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
		int friends = 0, enemies = 0;
		for (Cell c : nearby_cells) {
			if (c.player == myCell.player)
				friends++;
			else
				enemies++;
		}

		if (friends == 0 || enemies == 0) return false;

		if (((double)friends) / enemies >= RATIO_THRESHOLD) return true;
		else return false;
	}

	public static boolean isNearbyEnemies(Cell myCell, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
		int friends = 0, enemies = 0;
		for (Cell c : nearby_cells) {
			if (c.player == myCell.player)
				friends++;
			else
				enemies++;
		}
		
		if (friends == 0 || enemies == 0) return false;
		
		if (((double)enemies) / friends >= RATIO_THRESHOLD) return true;
		else return false;
	}

	public static boolean isClusterBorder(Cell myCell, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
		TreeMap<Double, Cell> angleMap = new TreeMap<>();
		Cell currentCell;
		double angle;
		int friends = 0,enemies = 0;
		for (Cell c : nearby_cells) {
			if (c.player == myCell.player)
				friends++;
			else
				enemies++;
		}
		if (friends < ToolBox.minCells || enemies < ToolBox.minCells) return false;
		

		for (Cell g : nearby_cells) {
			 angle = ToolBox.getCosine(myCell.getPosition(), g.getPosition());
			angleMap.put(angle, g);
		}
		Iterator<Entry<Double, Cell>> it = angleMap.entrySet().iterator();
		Cell firstCell = angleMap.firstEntry().getValue();
		boolean isLastFriend;
		Point lastPoint = new Point(firstCell.getPosition().x,firstCell.getPosition().y);
		
		if (firstCell.player == myCell.player) {isLastFriend = true;}
		else {isLastFriend = false;}
		
		while (it.hasNext()) {
			Entry<Double, Cell> e = it.next();
			currentCell = e.getValue();
			
			if (isLastFriend && currentCell.player != myCell.player){
				angle = ToolBox.getCosine(lastPoint, currentCell.getPosition());
				if (angle < ToolBox.friendsAngleThreshold){
					return false;
				}
				lastPoint = currentCell.getPosition();
			}
			if (!isLastFriend && currentCell.player == myCell.player){
					angle = ToolBox.getCosine(lastPoint, currentCell.getPosition());
					if (angle < ToolBox.friendsAngleThreshold){
						return false;
					}
					lastPoint = currentCell.getPosition();
			
			}
			if (currentCell.player == myCell.player){
				isLastFriend = true;
			}
			else{
				isLastFriend = false;
			}
			
		}
		
	
		if ((angleMap.firstEntry().getValue().player != angleMap.lastEntry().getValue().player) && (ToolBox.getCosine(angleMap.lastEntry().getValue().getPosition(),angleMap.firstEntry().getValue().getPosition()) < ToolBox.friendsAngleThreshold)){
			return false;
		}
		return true;

	}
		
	/*
	public static boolean isClusterBorder(Cell myCell, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
		Set<Cell> enemies = new HashSet<>();
		Set<Cell> friends = new HashSet<>();
		for (Cell c : nearby_cells) {
			if (c.player == myCell.player)
				friends.add(c);
			else
				enemies.add(c);

		}
		double max_angle_friends = 0;
		double max_angle_enemies = 0;
		if (!friends.isEmpty() && !enemies.isEmpty()) {

			for (Cell p : friends) {
				for (Cell c : friends) {
					if (ToolBox.getCosine(p.getPosition(), c.getPosition()) > max_angle_friends
							&& (ToolBox.getCosine(p.getPosition(), c.getPosition()) < Math.PI)) {
						max_angle_friends = ToolBox.getCosine(p.getPosition(), c.getPosition());
					}
				}
			}
			for (Cell p : enemies) {
				for (Cell c : enemies) {
					if (ToolBox.getCosine(p.getPosition(), c.getPosition()) > max_angle_enemies
							&& (ToolBox.getCosine(p.getPosition(), c.getPosition()) < Math.PI)) {
						max_angle_enemies = ToolBox.getCosine(p.getPosition(), c.getPosition());
					}
				}
			}

			return ((max_angle_friends > ToolBox.friendsAngleThreshold)
					&& (max_angle_enemies > ToolBox.enemiesAngleThreshold));
		}
		return false;

	}
	*/
}
