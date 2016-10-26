package slather.g7.strategies;

import java.util.Set;

import slather.g7.DummyMemory;
import slather.g7.Memory;
import slather.g7.Player;
import slather.g7.Strategy;
import slather.g7.ToolBox;
import slather.sim.Cell;
import slather.sim.Move;
import slather.sim.Pherome;
import slather.sim.Point;

/*
 * This strategy will keep a distance to everyone around it.
 * Basically it combines vectors with regard to the square of distance
 * */
public class AutismStrategy implements Strategy{

	double DISTANCE_STEP = 0.1;

	double ANGLE_STEP = Math.PI / 22;
	
	@Override
	public Memory generateNextMoveMemory(Memory currentMemory) {
		return new DummyMemory();
	}

	@Override
	public Memory generateFirstChildMemory(Memory currentMemory) {
		return new DummyMemory();
	}

	@Override
	public Memory generateSecondChildMemory(Memory currentMemory, Memory firstChildMemory) {
		return new DummyMemory();
	}

	@Override
	public Move generateMove(Cell player_cell, Memory memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
		Point nextStep = generateNextDirection(player_cell, memory, nearby_cells, nearby_pheromes);
		Memory nextMem = generateNextMoveMemory(memory);
		return new Move(nextStep, nextMem.getByte());
	}

	@Override
	public Point generateNextDirection(Cell player_cell, Memory memory, Set<Cell> nearby_cells,
			Set<Pherome> nearby_pheromes) {
		nearby_cells = ToolBox.limitVisionOnCells(player_cell, nearby_cells, 
													(2 + player_cell.getDiameter()/2));
		nearby_pheromes = ToolBox.limitVisionOnEnemyPheromes(player_cell, nearby_pheromes, 
														  (2 + player_cell.getDiameter()/2));
		
		Point pocketMove = findBestPocketMove(player_cell, nearby_cells, nearby_pheromes);
		
		return pocketMove;
	}

	private Point findBestPocketMove(Cell player_cell, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
		long startTime = System.nanoTime();
		
		
		Point player_pos = player_cell.getPosition();
		
		// Initialize best moves with 0 distance move
		double max_closest = ToolBox.distanceToClosestObject(player_cell, nearby_cells, nearby_pheromes);
		double start = max_closest;
		Point best_move = new Point(0, 0);
		
		// Check distances and angles and select the one where the closest object is farthest
		for (double angle = Player.autismStartAngle; angle < 2 * Math.PI + Player.autismStartAngle; angle += ANGLE_STEP) {
			for (double dist = DISTANCE_STEP; dist <= 1; dist += DISTANCE_STEP) {
				// Moving this much
				Point move_vec = new Point(dist * Math.cos(angle), dist * Math.sin(angle));
				// If I can move, see if this move is the best I've seen till now
				Point new_pos = new Point((player_pos.x + move_vec.x + 100) % 100, 
										  (player_pos.y + move_vec.y + 100) % 100);
				Cell temp_cell = new Cell(new_pos, player_cell.player);
				double closest = ToolBox.distanceToClosestObject(temp_cell, nearby_cells, nearby_pheromes);
				if (closest >= max_closest) {
					max_closest = closest;
					best_move = move_vec;
				} else if (closest < 0) {
					break;
				}
			}
			
			long time = System.nanoTime();
			if (time - startTime > 28000000) return best_move;
		}
		
		
//
//		long duration = (endTime - startTime)/1000000;  //divide by 1000000 to get milliseconds.
//		System.out.println("Look around and this takes "+duration+" milliseconds.");
		
		return best_move;
	}

}
