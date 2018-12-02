package apiaryparty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

/**
 * Strategy: 
 * @author Martin Morales, Cynthia Valencia
 */
public class BumbleGum extends Attacker {

    private final static String attackerName = "BumbleGum";
    
    public Random r;

    /**
     * Constructor
     * @param defenderName defender's name
     * @param graphFile graph to read
     */
	public BumbleGum(String defenderName, String graphFile) {
		super(attackerName, defenderName, graphFile);
	}
	/**
	 * Default constructor do not change
	 */
	public BumbleGum(){
		super(attackerName);
	}
	
	/**
	 * If you need to initialize anything, do it  here
	 */
	protected void initialize(){
		r = new Random();
	}

	/**
	 * This is called by the game master while your agent still has funds. You return an action.
	 * @return your action
	 */
	public AttackerAction makeAction() {
		return new AttackerAction(AttackerActionType.END_TURN, -1);
	}

	/**
	 * The game master is giving you the result of the action.
	 * @param  lastNode the node successfully attacked
	 */
	protected void result(Node lastNode) {
		
	}
}