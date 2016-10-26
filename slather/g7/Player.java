package slather.g7;

import java.util.*;
import slather.g7.strategies.*;
import slather.sim.Cell;
import slather.sim.Move;
import slather.sim.Pherome;
import slather.sim.Point;

public class Player implements slather.sim.Player {

	public static Random gen = new Random();
	private Strategy strategy;

	public static int T, T1 = 2, T2 = 8;
	public static double D, D1 = 2.0, D2 = 6.0;
	public static int num_def_sides;
	public static double vision = 5.0;
	public static double autismStartAngle = Math.PI;

	// This map will contain all the scenario -> strategy info
	public static HashMap<Scenario, StrategyType> strategyPerScenario;

	@Override
	public void init(double d, int t, int side_length) {
		
		T = t;
		D = d;
		strategy = new AutismStrategy();
		num_def_sides = Integer.max(4, T);
		
		if(strategyPerScenario==null){
//            System.out.println("Need to initialize strategy map");
            initScenarioStrategyMapping(d, t);
		}
		
	}

	@Override
	public Move play(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
		
//		long startTime = System.nanoTime();

		// Check the type of cell
		String memStr = DefenderMemory.byteToString(memory);
		char defOrExp = memStr.charAt(0);
		
		/*
		 * 0: Explorer 1: Defender
		 */
		Memory memObj;
		//dummy memory
		if(memStr.equals("00000000")){
			memObj = new DummyMemory();
		}else if (defOrExp == '0') {
			ExplorerMemory thisMem = ExplorerMemory.getNewObject();
			thisMem.initialize(memory);
			memObj = thisMem;
		} else if (defOrExp == '1') {
			DefenderMemory thisMem = DefenderMemory.getNewObject();
			thisMem.initialize(memory);
			memObj = thisMem;
		} else {
			memObj = DefenderMemory.getNewObject();
		}

		Scenario currentScenario = getScenario(player_cell, nearby_cells, nearby_pheromes);
//		System.out.println("Scenario is "+currentScenario.name());
		StrategyType st = strategyPerScenario.get(currentScenario);
//		System.out.println("Current strategy is "+st.name());
		
		Move toTake;
		if (st == StrategyType.REPRODUCE) {
			Strategy mindSet = StrategyFactory.getStrategyByType(st);
			toTake = mindSet.generateMove(player_cell, memObj, nearby_cells, nearby_pheromes);
			return toTake;
		} else if (st == StrategyType.CIRCLE){
			Strategy mindSet = StrategyFactory.getStrategyByType(st);
			DefenderMemory thisMem = DefenderMemory.getNewObject();
			thisMem.initialize(memory);
			memObj = thisMem;
			toTake = mindSet.generateMove(player_cell, memObj, nearby_cells, nearby_pheromes);
			
//			System.out.println("Not keep distance, need to check.");
			toTake=adjustDistance(toTake,player_cell,memObj,nearby_cells,nearby_pheromes);
			
			return toTake;
		} else if (st == StrategyType.EXPLORER){
			Strategy mindSet = StrategyFactory.getStrategyByType(st);
			ExplorerMemory thisMem = ExplorerMemory.getNewObject();
			thisMem.initialize(memory);
			memObj = thisMem;
			toTake = mindSet.generateMove(player_cell, memObj, nearby_cells, nearby_pheromes);
			
			//System.out.println("Not keep distance, need to check.");
			toTake=adjustDistance(toTake,player_cell,memObj,nearby_cells,nearby_pheromes);
			
			return toTake;
		} else {
//			long time1=System.nanoTime();
//			long time1mil=(time1-startTime)/1000000;
//			System.out.println("Time point 1 in play "+time1mil);
			
			Strategy mindSet = StrategyFactory.getStrategyByType(st);
			toTake = mindSet.generateMove(player_cell, memObj, nearby_cells, nearby_pheromes);
			
//			long time2=System.nanoTime();
//			long time2mil=(time2-time1)/1000000;
//			System.out.println("Time point 2 in play "+time2mil);
			
			//adjust distance moving
			if(st!=StrategyType.KEEP_DISTANCE){
				//System.out.println("Not keep distance, need to check.");
				toTake=adjustDistance(toTake,player_cell,memObj,nearby_cells,nearby_pheromes);
			}
			
//			long time3=System.nanoTime();
//			long time3mil=(time3-time2)/1000000;
//			System.out.println("Time point 3 in play "+time3mil);
		}

		/*
		 * Todo: Add distance check to ensure that we can grow after move.
		 */
		/*Point dir = toTake.vector;
		System.out.println("Checking the distance for growth.");
		dir = ToolBox.checkSpaceForGrowth(player_cell, dir, nearby_cells, nearby_pheromes);
		toTake = new Move(dir, toTake.memory);
		System.out.println("Checked direction with regard to growth is x:" + dir.x + " y:" + dir.y);
		
		 If we cannot move in the chosen direction, keep a distance with things around 
		if(Math.abs(dir.x) < 0.01 && Math.abs(dir.y) < 0.01){
			System.out.println("The cell cannot move in this direction. Fall back to keeping a distance with everything.");
			toTake = new AutismStrategy().generateMove(player_cell, m, nearby_cells, nearby_pheromes);
			dir = ToolBox.checkSpaceForGrowth(player_cell, dir, nearby_cells, nearby_pheromes);
			toTake = new Move(dir, toTake.memory);
		}else{
			System.out.println("Moving in this turn");
		}*/
//		long endTime = System.nanoTime();
//
//		long duration = (endTime - startTime)/1000000;  //divide by 1000000 to get milliseconds.
//		System.out.println("Play function takes "+duration+" milliseconds.");
		
		return toTake;
	}
	private Move adjustDistance(Move toTake, Cell player_cell, Memory m,Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes){
		Point dir = toTake.vector;
		//System.out.println("Checking the distance for growth.");
		dir = ToolBox.checkSpaceForGrowth(player_cell, dir, nearby_cells, nearby_pheromes);
		toTake = new Move(dir, toTake.memory);
		//System.out.println("Checked direction with regard to growth is x:" + dir.x + " y:" + dir.y);
		
		 //If we cannot move in the chosen direction, keep a distance with things around 
		Move newMove;
		if(Math.abs(dir.x) < 0.01 && Math.abs(dir.y) < 0.01){
			//System.out.println("The cell cannot move in this direction. Fall back to keeping a distance with everything.");
			toTake = new AutismStrategy().generateMove(player_cell, m, nearby_cells, nearby_pheromes);
			dir = ToolBox.checkSpaceForGrowth(player_cell, dir, nearby_cells, nearby_pheromes);
			newMove = new Move(dir, toTake.memory);
		}else{
			//System.out.println("Moving in this turn");
			newMove=toTake;
		}
		return newMove;
	}
	
	private Scenario getScenario(Cell myCell, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
		Set<Cell> friends = new HashSet<>();
		Set<Cell> enemies = new HashSet<>();
		for (Cell c : nearby_cells) {
			if (c.player == myCell.player)
				friends.add(c);
			else
				enemies.add(c);
		}
		if (myCell.getDiameter() >= 2.0) {
			return Scenario.MAX_SIZE;
		}
		if (ScenarioClass.isAboutToReproduce(myCell, nearby_cells, nearby_pheromes)) {
			return Scenario.ALMOST_REPRODUCTION;
		}
		if (ScenarioClass.isOnlyMyPherome(myCell, nearby_cells, nearby_pheromes)) {
			return Scenario.ONE_PHEROMONE;
		}
		if (ScenarioClass.isEmpty(myCell, nearby_cells, nearby_pheromes)) {
			return Scenario.EMPTYNESS;
		}
		if (ScenarioClass.isClusterBorder(myCell, nearby_cells, nearby_pheromes)) {
			return Scenario.CLUSTER_BORDER;
		}
		if (ScenarioClass.isOnlyFriends(myCell, nearby_cells, nearby_pheromes)) {
			return Scenario.FRIENDS;
		}
		if (ScenarioClass.isOnlyEnemies(myCell, nearby_cells, nearby_pheromes)) {
			return Scenario.ENEMIES;
		}
		if (ScenarioClass.isNearbyFriends(myCell, nearby_cells, nearby_pheromes)) {
			return Scenario.NEARBY_FRIENDS;
		}
		if (ScenarioClass.isNearbyEnemies(myCell, nearby_cells, nearby_pheromes)) {
			return Scenario.NEARBY_ENEMIES;
		}

		return Scenario.DISPERSED;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public void initScenarioStrategyMapping(double d, int t) {
		if (d >= D2) {
			if (t <= T1) {
				strategyPerScenario = StrategyMaps.bigDsmallT();
			} else if (t <= T2) {
				strategyPerScenario = StrategyMaps.bigDmediumT();
			} else {
				strategyPerScenario = StrategyMaps.bigDbigT();
			}
		}else if (d >= D1){
			if (t <= T1) {
				strategyPerScenario = StrategyMaps.smallDsmallT();
			} else if (t <= T2) {
				strategyPerScenario = StrategyMaps.smallDmediumT();
			} else {
				strategyPerScenario = StrategyMaps.smallDbigT();
			}
			
		}else {
			if (t <= T1) {
				strategyPerScenario = StrategyMaps.mediumDsmallT();
			} else if (t <= T2) {
				strategyPerScenario = StrategyMaps.mediumDmediumT();
			} else {
				strategyPerScenario = StrategyMaps.mediumDbigT();
			}
		}
		
		//strategyPerScenario = StrategyMaps.defaultMapInit();
	}
	

}
