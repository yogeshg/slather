package slather.g7.strategies;

import java.util.Set;

import slather.g7.DummyMemory;
import slather.g7.ExplorerMemory;
import slather.g7.Memory;
import slather.g7.Strategy;
import slather.sim.Cell;
import slather.sim.Move;
import slather.sim.Pherome;
import slather.sim.Point;

public class ReproductionStrategy implements Strategy{

	@Override
	public Memory generateNextMoveMemory(Memory currentMemory) {
		// Not needed
		return null;
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
		Memory childMemory = generateFirstChildMemory(memory);
		Memory secondChildMemory = generateSecondChildMemory(memory, childMemory);
		return new Move(true, childMemory.getByte(), secondChildMemory.getByte());
	}

	@Override
	public Point generateNextDirection(Cell player_cell, Memory memory, Set<Cell> nearby_cells,
			Set<Pherome> nearby_pheromes) {
		// Not needed
		return null;
	}

}
