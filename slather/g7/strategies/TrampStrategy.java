package slather.g7.strategies;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Random;

import slather.g7.DummyMemory;
import slather.g7.Memory;
import slather.g7.Strategy;
import slather.g7.ToolBox;
import slather.sim.Cell;
import slather.sim.GridObject;
import slather.sim.Move;
import slather.sim.Pherome;
import slather.sim.Point;

public class TrampStrategy implements Strategy {
	// static boolean considerRadiusInFreeSpaceFinding=false;
	static Random gen = new Random();
	static double vision = 3.0;

	static int visionBufferSize = 10;

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
		Point nextStep = headToFreeSpace(player_cell, nearby_cells, nearby_pheromes);

		return nextStep;
	}

	/*
	 * Head to free space Look around to find the largest angle between things
	 * and go there
	 */
	public static Point headToFreeSpace(Cell myCell, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
		// Point runFromCells = runFromAll(myCell, nearby_cells, 1.0);
		// Point runFromPheromes = runFromAll(myCell, nearby_pheromes, 0.5);
		
		long startTime = System.nanoTime();

		Point myPoint = myCell.getPosition();
		nearby_cells = ToolBox.limitVisionOnCells(myCell, nearby_cells, vision);
		nearby_pheromes = ToolBox.limitVisionOnPheromes(myCell, nearby_pheromes, vision);

		/* Store a sorted angle from every grid object around my cell */
		TreeMap<Double, GridObject> angleMap = new TreeMap<>();

		//Limit vision to the closest several things
		Set<GridObject> thingsISee = ToolBox.limitVisionToSize(myCell, nearby_cells, nearby_pheromes, true, visionBufferSize);
		
		for (GridObject g : thingsISee) {
			double angle = ToolBox.getCosine(myCell.getPosition(), g.getPosition());
			angleMap.put(angle, g);
		}
		
//		long time1=System.nanoTime();
//		long time1mil=(time1-startTime)/1000000;
//		System.out.println("Time point 1 in play "+time1mil);

		/* Find the largest gap between things */
		if (angleMap.size() == 0) {
			Point dir = new Point(gen.nextDouble() - 0.5, gen.nextDouble() - 0.5);
			return ToolBox.normalizeDistance(dir);
		} else if (angleMap.size() == 1) {
			double toGo = angleMap.firstKey() + Math.PI;
			return ToolBox.newDirection(toGo);
		}

		Iterator<Entry<Double, GridObject>> it = angleMap.entrySet().iterator();
		GridObject lastPoint = angleMap.lastEntry().getValue();
		double lastAngle = angleMap.lastKey();
		double largestGap = 0.0;
		double largestGapStart = 0.0;
		double largestGapEnd = 0.0;
		while (it.hasNext()) {
			Entry<Double, GridObject> e = it.next();
			/* thisPoint is always on the right of lastPoint */
			GridObject thisPoint = e.getValue();

			double thisAngle = e.getKey();// this is the angle of the center
			double angleDiff = ToolBox.angleDiff(lastAngle, thisAngle);
			// If the object is cell, need to consider the radius
			Point mp = myCell.getPosition();
			
			double startAngleTan=0.0;
			double endAngleTan=0.0;
			if (lastPoint instanceof Cell) {
				Cell c = (Cell) lastPoint;
				Point cp = c.getPosition();
				// calculate the distance between two points
				Point diffPoint = ToolBox.pointDistance(cp, mp);
				double mc = diffPoint.x * diffPoint.x + diffPoint.y * diffPoint.y;
				if (mc <= 0.00001) {// might overflow
				} else {
					double cr = c.getDiameter();
					// If the object is cell, the distance cannot be 0
					double diffTheta = Math.asin(cr / mc);
					angleDiff -= diffTheta;
					endAngleTan=diffTheta;
				}
			}
			if (thisPoint instanceof Cell) {
				Cell c = (Cell) thisPoint;
				Point cp = c.getPosition();
				// calculate the distance between two points
				Point diffPoint = ToolBox.pointDistance(cp, mp);
				double mc = diffPoint.x * diffPoint.x + diffPoint.y * diffPoint.y;
				if (mc <= 0.00001) {// might overflow
				} else {
					double cr = c.getDiameter();
					// If the object is cell, the distance cannot be 0
					double diffTheta = Math.asin(cr / mc);
					angleDiff -= diffTheta;
					startAngleTan=diffTheta;
				}
			}

			if (angleDiff > largestGap) {
				largestGap = angleDiff;
				largestGapStart = thisAngle-startAngleTan;
				largestGapEnd = lastAngle+endAngleTan;
			}

			/* Slide the window */
			lastAngle = thisAngle;
			lastPoint = thisPoint;
		}
		
//		long time2=System.nanoTime();
//		long time2mil=(time2-time1)/1000000;
//		System.out.println("Time point 2 in play "+time2mil);

		/* Decide the angle to go */
		double toGo = largestGapStart - 0.5 * largestGap;

		/* Generate the point to go */
		return ToolBox.newDirection(toGo);
	}

}
