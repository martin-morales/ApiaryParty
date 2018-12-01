package apiaryparty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

/**
 * Strategy: Probe all available nodes, calculate expected value for each and make maximizing decision
 * @author Martin Morales, Cynthia Valencia
 */
public class BeeGees extends Attacker {

    private final static String attackerName = "BeeGees";
    
    public Random r;

    /**
     * Constructor
     * @param defenderName defender's name
     * @param graphFile graph to read
     */
	public BeeGees(String defenderName, String graphFile) {
		super(attackerName, defenderName, graphFile);
	}
	/**
	 * Default constructor do not change
	 */
	public BeeGees(){
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
		
		// Probe all nodes' point values
		for(Node node : availableNodes) {
			//System.out.println(node.getNodeID() + " sv: " + node.getSv() + " pv: " + node.getPv());
			if(node.getPv() == -1) {
				//System.out.println("Making a probe");
				return new AttackerAction(AttackerActionType.PROBE_POINTS, node.getNodeID());
			}
		}
		return makeMaxExpValDecision();
	}

	/**
	 * The game master is giving you the result of the action.
	 * @param  lastNode the node successfully attacked
	 */
	protected void result(Node lastNode) {
		
	}
	
	/**
	 * Calculates the expected values for making atks/super atks on all available nodes
	 * and then executes the best attack
	 */
	private AttackerAction makeMaxExpValDecision() {
		ArrayList<HashMap<Integer, Double>> expValues = calculateExpValues();
		int bestNodeID = -1;
		double bestCost = Integer.MIN_VALUE;
		int typeOfAtk = -1;
		
		//System.out.println(expValues);
		
		// Find best move to make
		for(int i = 0; i < expValues.size(); i++) {
			for(Entry<Integer, Double> entry : expValues.get(i).entrySet()) {
			    Integer key = entry.getKey();
			    Double value = entry.getValue();

			    if(value > bestCost) {
			    	bestNodeID = key;
			    	bestCost = value;
			    	typeOfAtk = i;
			    }
			}
		}
		
		//System.out.println("Best Node ID: " + bestNodeID + " bestCost: " + bestCost + " typeOfAtk: " + typeOfAtk);
		
		if(typeOfAtk == 0) {
			return new AttackerAction(AttackerActionType.ATTACK, bestNodeID);
		} else if(typeOfAtk == 1) {
			return new AttackerAction(AttackerActionType.SUPERATTACK, bestNodeID);
		} else {
			System.out.println("FAILURE: Unexpected typeOfAtk");
			return new AttackerAction(AttackerActionType.END_TURN, -1);
		}
	}
	
	/**
	 * Loops through all available nodes and calculates their expected value
	 * NodeExpValue = PV * (RollChance - SV / RollChance) - RollCost
	 * Assumes all available nodes have point values. Probe all available nodes before calling this
	 */
	private ArrayList<HashMap<Integer, Double>> calculateExpValues() {
		// First hash map is to store expected node values with atk rolls, second is for super atk rolls
		ArrayList<HashMap<Integer, Double>> expValues = new ArrayList<HashMap<Integer, Double>>();
		
		// Get Atk Exp Values
		HashMap<Integer, Double> atkExpValues = new HashMap<>();
		for(Node node : availableNodes) {
			double chance = (double) (Parameters.ATTACK_ROLL - node.getSv()) / Parameters.ATTACK_ROLL;	// ex. SV=5 -> 15/20
			double val = node.getPv() * chance;
			val -= Parameters.ATTACK_RATE;		// Here I'm just subtracting the atk cost, assuming 1 budget is worth the same as 1 point
			atkExpValues.put(node.getNodeID(), val);
		}
		
		// Get Super Atk Exp Values
		HashMap<Integer, Double> superAtkExpValues = new HashMap<>();
		for(Node node : availableNodes) {
			double chance = (double) (Parameters.SUPERATTACK_ROLL - node.getSv()) / Parameters.SUPERATTACK_ROLL;	// ex. SV=5 -> 15/20
			double val = node.getPv() * chance;
			val -= Parameters.SUPERATTACK_RATE;		// Here I'm just subtracting the atk cost, assuming 1 budget is worth the same as 1 point
			superAtkExpValues.put(node.getNodeID(), val);
		}
		
		expValues.add(atkExpValues);
		expValues.add(superAtkExpValues);
		return expValues;
	}
}