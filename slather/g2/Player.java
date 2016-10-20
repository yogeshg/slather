package slather.g2;

import slather.sim.Cell;
import slather.sim.Point;
import slather.sim.Move;
import slather.sim.Pherome;
import java.util.*;


import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


import slather.g2.util.Vector;

public class Player implements slather.sim.Player {

    protected Random RANDOM_GENERATOR;
    protected double VISION;
    protected int TAIL_LENGTH;
    protected double BOARD_SIZE;

    protected String CONFIG_FILE_NAME = "player.cfg";

    protected double PROB_PLAYER1_TO_PLAYER2;
    protected double PROB_PLAYER2_TO_PLAYER1;
    protected double RADIUS_TO_TAIL_RATIO;
    protected double GRID_DIST_TO_TAIL_RATIO;
    protected String INITIAL_CLASS;
    protected String PROB_CLASS;
    protected String ADJUST_PROB;

    private Player player1 = null;
    private Player player2 = null;

    // Initialize here, initialize sub strategies here.
    // Take care to initialize VISION, TAIL_LENGTH, BOARD_SIZE, RANDOM_GENERATOR in your subclass
    // Do not make recursive calls to this init.

    private Player getPlayerInstance(String className) {
        System.out.println(className);
        if(className.equals("Scout")) {
            return new Scout();
        } else if(className.equals("GridCircler")) {
            return new GridCircler();
        } else if(className.equals("Grider")) {
            return new Grider();
        } else if(className.equals("Circler")) {
            return new Circler();
        } else if(className.equals("AwayFromBig")) {
            return new AwayFromBig();
        }  else if(className.equals("Landmark")) {
            return new Landmark();
        } else {
            return new Chiller();
        }
    }

    public void init(double d, int t, int side_length) {
        System.out.println("Player init");

        this.RANDOM_GENERATOR = new Random(System.currentTimeMillis());
        this.VISION = d;
        this.TAIL_LENGTH = t;
        this.BOARD_SIZE = side_length;

        getPropertiesSafe();

        if( this.ADJUST_PROB.equals("0WhenTaillessThan5") ){
            if (t <= 4) {
                this.PROB_PLAYER1_TO_PLAYER2 = 0;
            }
        }


        player1 = getPlayerInstance(INITIAL_CLASS);
        player2 = getPlayerInstance(PROB_CLASS);

        player1.init(d, t, side_length);
        player2.init(d, t, side_length);
    }

    public String move2String(Move m) {
        return
        "Move(" +m.reproduce
            +","+m.vector.x
            +","+m.vector.y
            +","+String.format("%8s", Integer.toBinaryString(m.memory & 0xFF)).replace(' ', '0')
            +","+String.format("%8s", Integer.toBinaryString(m.daughter_memory & 0xFF)).replace(' ', '0')
            +")";
    }

    public Move play(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
        // reproduce whenever possible
        Move m;
        if (player_cell.getDiameter() >= 2){
            final boolean prob12 = (RANDOM_GENERATOR.nextDouble() < PROB_PLAYER1_TO_PLAYER2);
            final boolean prob21 = (RANDOM_GENERATOR.nextDouble() < PROB_PLAYER2_TO_PLAYER1);
            if(!byteIsCircle(memory)) {
                if( prob12 ) {
                    m = player2.reproduce(player_cell, memory, nearby_cells, nearby_pheromes);
                    m = new Move( true, (byte)(m.memory|ROLE_CIRCLE), (byte)(m.daughter_memory&ROLE_SCOUT) );
                } else {
                    m = player1.reproduce(player_cell, memory, nearby_cells, nearby_pheromes);
                    m = new Move( true, (byte)(m.memory&ROLE_SCOUT), (byte)(m.daughter_memory&ROLE_SCOUT) );
                }
            } else {
                if( prob21 ) {
                    m = player1.reproduce(player_cell, memory, nearby_cells, nearby_pheromes);
                    m = new Move( true, (byte)(m.memory|ROLE_CIRCLE), (byte)(m.daughter_memory&ROLE_SCOUT) );
                } else {
                    m = player2.reproduce(player_cell, memory, nearby_cells, nearby_pheromes);
                    m = new Move( true, (byte)(m.memory|ROLE_CIRCLE), (byte)(m.daughter_memory|ROLE_CIRCLE) );
                }
                
            }
        } else {
            if(!byteIsCircle(memory)) {
                m = player1.play(player_cell, memory, nearby_cells, nearby_pheromes);
            } else {
                m = player2.play(player_cell, memory, nearby_cells, nearby_pheromes);
            }
        }

        // System.out.println(move2String(m));

        return m;
    }


    public Move reproduce(Cell player_cell, byte memory, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
        return new Move(true, memory, memory);
    }

    // check if moving player_cell by vector collides with any nearby cell or hostile pherome
    protected boolean collides(Cell player_cell, Vector vector, Set<Cell> nearby_cells, Set<Pherome> nearby_pheromes) {
        Iterator<Cell> cell_it = nearby_cells.iterator();
        Vector destination = new Vector( player_cell.getPosition().move(vector.getPoint()) );
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

    protected static final int ANGLE_BITS = 5;
    protected static final int ANGLE_MIN = 0;
    protected static final int ANGLE_MAX = 1 << ANGLE_BITS;
    protected static final int ANGLE_MASK = ANGLE_MAX - 1;

    protected static final int ROLE_BITS = 1;
    protected static final int ROLE_MIN = ANGLE_MAX;
    protected static final int ROLE_MAX = ROLE_MIN + ( 1<<ROLE_BITS );
    protected static final int ROLE_MASK = (ROLE_MAX - 1) & ~ANGLE_MASK;
    protected static final int ROLE_SCOUT = 0 << ANGLE_BITS;
    protected static final int ROLE_CIRCLE = 1 << ANGLE_BITS;

    private boolean byteIsCircle(byte b){
        //System.out.printf("%d %d %d\n",b,ROLE_MASK,ROLE_CIRCLE);
        return (b&ROLE_MASK)==ROLE_CIRCLE;
    }


    private boolean getProperties() {
        Properties prop = new Properties();
        InputStream input = null;
        boolean returnVal;
        try {
            input = getClass().getResourceAsStream(CONFIG_FILE_NAME);
            // input = new FileInputStream(CONFIG_FILE_NAME);
            prop.load(input);
            this.PROB_PLAYER1_TO_PLAYER2          = Float.parseFloat(prop.getProperty("PROB_PLAYER1_TO_PLAYER2"));
            this.PROB_PLAYER2_TO_PLAYER1          = Float.parseFloat(prop.getProperty("PROB_PLAYER2_TO_PLAYER1"));
            this.RADIUS_TO_TAIL_RATIO = Float.parseFloat(prop.getProperty("RADIUS_TO_TAIL_RATIO"));
            this.GRID_DIST_TO_TAIL_RATIO = Float.parseFloat(prop.getProperty("GRID_DIST_TO_TAIL_RATIO"));
            this.INITIAL_CLASS        = prop.getProperty("INITIAL_CLASS");
            this.PROB_CLASS           = prop.getProperty("PROB_CLASS");
            this.ADJUST_PROB          = prop.getProperty("ADJUST_PROB");
            returnVal = true;
        } catch (Exception e) {
            e.printStackTrace();
            returnVal = false;
        } finally {
            if(input!=null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return returnVal;
    }

    private void setProperties() {
        Properties prop = new Properties();
        OutputStream output = null;
        try {
            File file = new File( getClass().getResource(CONFIG_FILE_NAME).toURI() );
            output = new FileOutputStream(file);

            prop.setProperty("PROB_PLAYER1_TO_PLAYER2", ""+this.PROB_PLAYER1_TO_PLAYER2);
            prop.setProperty("PROB_PLAYER2_TO_PLAYER1", ""+this.PROB_PLAYER2_TO_PLAYER1);
            prop.setProperty("RADIUS_TO_TAIL_RATIO", ""+this.RADIUS_TO_TAIL_RATIO);
            prop.setProperty("GRID_DIST_TO_TAIL_RATIO", ""+this.GRID_DIST_TO_TAIL_RATIO);
            prop.setProperty("INITIAL_CLASS", this.INITIAL_CLASS);
            prop.setProperty("PROB_CLASS", this.PROB_CLASS);
            prop.setProperty("ADJUST_PROB", this.ADJUST_PROB);
            prop.store(output, null);

        } catch (
            Exception io) {
            io.printStackTrace();
        } finally {
            if(output!=null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void getPropertiesSafe() {
        if(!getProperties()){
            this.PROB_PLAYER1_TO_PLAYER2 = 0.2;
            this.PROB_PLAYER2_TO_PLAYER1 = 0.1;
            this.RADIUS_TO_TAIL_RATIO = 1.0;
            this.GRID_DIST_TO_TAIL_RATIO = 1.0;
            this.INITIAL_CLASS = "Scout";
            this.PROB_CLASS = "Circler";
            this.ADJUST_PROB = "0WhenTaillessThan5";
            setProperties();
        }
    }

}
