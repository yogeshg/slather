package slather.g2;

import slather.sim.Cell;
import slather.sim.Point;
import slather.sim.Move;
import slather.sim.Pherome;
import java.util.*;


public class Player implements slather.sim.Player {
    
    private Random gen;
    private List<Point> directions;

    public void init(double d, int t) {
		gen = new Random();
		directions = new ArrayList<Point>();
		directions.add(extractVectorFromAngle(0));
		directions.add(extractVectorFromAngle(90));
		directions.add(extractVectorFromAngle(180));
		directions.add(extractVectorFromAngle(270));
    }

    private byte convertToByte(int[] b){
    	int res = 0;
    	for(int i = 0; i < 8; i++){
    		res += b[i] * (1<<i);
    	}
    	return (byte)res;
    }


    private int[] convertToArray(byte b){
    	int[] res = new int[8];
    	for(int i = 0; i < 8; i++){
    		if( ((int)b&(1<<i))!=0){
				res[i] = 1;
			}
    	}
    	return res;
    }


    //use first two bits to denote previous direction
    //use third bit to indicate if previously splitted
    public Move play(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
    	//always reproduce when possible
		int[] m = convertToArray(memory);
		if(player_cell.getDiameter() >= 2){
			int[] copy = copy(m);
			copy[2] = 1;
			return new Move(true,memory,convertToByte(copy));
		}
		
		int prevDir = (m[0]*1 + m[1]*2);
		int nextDir = (prevDir+m[2])%4;
		//stay on previous course as much as we can
		//turn counter-clockwise 90 degrees from previous direction when reproduced
		//if all four directions fail, move at random
		for(int i = 0; i < 4; i++){
			int dir = (prevDir+i)%4;
			Point vector = directions.get(dir);
			if(!collides(player_cell, vector, nearby_cells, nearby_pheromes)){
				m[0] = dir%2 == 0? 0: 1;
				m[1] = dir/2 == 0? 0: 1;
				m[2] = 0;
				return new Move(vector, convertToByte(m));
			}
		}
		
		m[0] = 0;
		m[1] = 0;
		setNum(m,0,3);
		//if all other methods fail, move at random
		
		for (int i=0; i<10; i++) {
		    int arg = gen.nextInt(180)+1;
		    Point vector = extractVectorFromAngle(arg);
		    if (!collides(player_cell, vector, nearby_cells, nearby_pheromes)){
		    	//System.out.println("random");
		    	return new Move(vector, convertToByte(m));
		    }
		}

		// if all tries fail, just chill in place
		// System.out.println("stay");
		return new Move(new Point(0,0), (byte)0);
    }

    private int getNum(int[] m,int bits){
    	int res = 0;
    	for(int i = bits; i < 8; i++){
    		res += (1 << (i-bits)) * m[i];
    	}
    	return res;
    }

    private void setNum(int[] m,int c,int bits){
    	if(c > Math.pow(2,8-bits) - 1){
    		System.out.println("num too big");
    		System.exit(1);
    	}
    	for(int i = bits; i < 8; i++){
    		m[i] = c%2;
    		c = c >> 1;
    	}
    }


    private int[] copy(int[] org){
    	int[] res = new int[org.length];
    	for(int i = 0; i < org.length; i++){
    		res[i] = org[i];
    	}
    	return res;
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
	double theta = Math.toRadians( (double)arg );
	double dx = Cell.move_dist * Math.cos(theta);
	double dy = Cell.move_dist * Math.sin(theta);
	return new Point(dx, dy);
    }
}
