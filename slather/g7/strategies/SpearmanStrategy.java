package slather.g7.strategies;

import java.util.Set;

import slather.g7.DummyMemory;
import slather.g7.Grouping;
import slather.g7.Memory;
import slather.g7.Strategy;
import slather.g7.ToolBox;
import slather.sim.Cell;
import slather.sim.Move;
import slather.sim.Pherome;
import slather.sim.Point;

/*
 * This strategy resembles spearmen that go towards enemies.
 * This is done by merging attractive forces from enemies and expelling forces from friends.
 * 
 * */
public class SpearmanStrategy implements Strategy{
	static boolean considerRadius = false;
	static double weightOfCells = 1.0;
	static double weightOfPheromes = 0.5;

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
		Point nextStep=generateNextDirection(player_cell,memory,nearby_cells,nearby_pheromes);
		Memory nextMem = generateNextMoveMemory(memory);

		return new Move(nextStep,nextMem.getByte());
	}

	@Override
	public Point generateNextDirection(Cell player_cell, Memory memory, Set<Cell> nearby_cells,
			Set<Pherome> nearby_pheromes) {
		long startTime=System.nanoTime();
		
		Grouping groups = new Grouping(player_cell, nearby_cells, nearby_pheromes);
		
		/* Migrate from merging vectors to merging gravity */
//		Point fromFriendCells = ToolBox.joinGravityFromCells(player_cell, groups.friendCells, weightOfCells, 1);
//		Point fromEnemyCells = ToolBox.joinGravityFromCells(player_cell, groups.enemyCells, weightOfCells, -1);
//		Point fromEnemyPheromes = ToolBox.joinGravityFromPheromes(player_cell, groups.enemyPheromes, weightOfPheromes,
//				-1);
//		Point fromFriendPheromes = ToolBox.joinGravityFromPheromes(player_cell, groups.friendPheromes, weightOfPheromes,
//				1);
		
//		Point fromFriendCells = ToolBox.joinForcesFromCells(player_cell, groups.friendCells, weightOfCells, 1,true);
		Point fromEnemyCells = ToolBox.joinForcesFromCells(player_cell, groups.enemyCells, weightOfCells, -1, true);
		Point fromEnemyPheromes = ToolBox.joinForcesFromPheromes(player_cell, groups.enemyPheromes, weightOfPheromes, -1);
//		Point fromFriendPheromes = ToolBox.joinGravityFromPheromes(player_cell, groups.friendPheromes, weightOfPheromes,
//				1);

		Point mergedForce = ToolBox.normalizeDistance(fromEnemyCells, fromEnemyPheromes);
//		
		if(Math.abs(mergedForce.x)<0.00001&&Math.abs(mergedForce.y)<0.00001)
			return ToolBox.generateRandomDirection();

		return mergedForce;
	}

}
