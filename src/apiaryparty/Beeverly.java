package apiaryparty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

/**
 * Strategy: Maxmin, assumes PV ~= SV to avoid probing and attacks the highest SV node with super attacks
 * @author Martin Morales, Cynthia Valencia
 */
public class Beeverly extends Attacker {

    private final static String attackerName = "Beeverly";
    
    public Random r;

    /**
     * Constructor
     * @param defenderName defender's name
     * @param graphFile graph to read
     */
	public Beeverly(String defenderName, String graphFile) {
		super(attackerName, defenderName, graphFile);
	}
	/**
	 * Default constructor do not change
	 */
	public Beeverly(){
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
		// No nodes left
		if(availableNodes.size()==0)
            return new AttackerAction(AttackerActionType.INVALID,0);
		
		// Attack node with highest security value
		int highestSecurity = -1;
		int bestNodeID = -1;
		for(Node node : availableNodes) {
			if(node.getSv() > highestSecurity) {
				highestSecurity = node.getSv();
				bestNodeID = node.getNodeID();
			}
		}
		
		return new AttackerAction(AttackerActionType.SUPERATTACK, bestNodeID);
	}

	/**
	 * The game master is giving you the result of the action.
	 * @param  lastNode the node successfully attacked
	 */
	protected void result(Node lastNode) {
		
	}
}