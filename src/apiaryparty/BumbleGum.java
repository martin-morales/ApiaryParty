package apiaryparty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

/**
 * Strategy: Maxmin, ignores chance/security values and just attacks nodes with highest values
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
		// No nodes left
		if(availableNodes.size()==0)
            return new AttackerAction(AttackerActionType.INVALID,0);
		
		// Probe all nodes' point values
		for(Node node : availableNodes) {
			if(node.getPv() == -1) {		// Have not checked this node yet
				//System.out.println("Probing points of " + node.getNodeID());
				return new AttackerAction(AttackerActionType.PROBE_POINTS, node.getNodeID());
			}
		}
		
		// Attack node with highest point value
		int highestPoints = -1;
		int bestNodeID = -1;
		for(Node node : availableNodes) {
			if(node.getPv() > highestPoints) {
				highestPoints = node.getPv();
				bestNodeID = node.getNodeID();
			}
		}
		
		return new AttackerAction(AttackerActionType.ATTACK, bestNodeID);
	}

	/**
	 * The game master is giving you the result of the action.
	 * @param  lastNode the node successfully attacked
	 */
	protected void result(Node lastNode) {
		
	}
}