package apiaryparty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Strategy: Bayesian game where the cost of parameters determines the strategy we play
 * Cheap Probe Points/HP: Probe points/HP of all available nodes, calculate expected value for each and make maximizing decision
 * Cheap Probe HP: Probe HP of all available nodes, assume SV = PV, calculate expected value for each and make maximizing decision
 * Cheap Probe Points: Probe points of all available nodes, calculate expected value and make maximizing decision
 * Expensive Probe Points/HP: Assume SV = PV, calculate expected value for each and make maximizing decision
 * @author Martin Morales, Cynthia Valencia
 */
public class BeeGees extends Attacker {

    private final static String attackerName = "BeeGees";
    
    // Bayesian Game where the cost of parameters determines the strategy we play
    // 0: Prob/HP 
    private int strategy = -1;

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
		
		if(Parameters.PROBE_POINTS_RATE + Parameters.PROBE_HONEY_RATE + Parameters.ATTACK_RATE <= Parameters.ATTACKER_RATE) {
			strategy = 1;
			//System.out.println("Choosing strategy 1");
		} else if(Parameters.PROBE_HONEY_RATE + Parameters.ATTACK_RATE <= Parameters.ATTACKER_RATE) {
			strategy = 2;
			//System.out.println("Choosing strategy 2");
		} else if(Parameters.PROBE_POINTS_RATE + Parameters.ATTACK_RATE <= Parameters.ATTACKER_RATE) {
			strategy = 3;
			//System.out.println("Choosing strategy 3");
		} else {
			strategy = 4;
			//System.out.println("Choosing strategy 4");
		}
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
		if(strategy == 1 || strategy == 3) {		// Strategies where probing for points is cheap
			for(Node node : availableNodes) {
				//System.out.println(node.getNodeID() + " sv: " + node.getSv() + " pv: " + node.getPv());
				if(node.getPv() == -1) {		// Have not checked this node yet
					//System.out.println("Probing points of " + node.getNodeID());
					return new AttackerAction(AttackerActionType.PROBE_POINTS, node.getNodeID());
				}
			}
		}

		// Probe for honeypots, if there are some
		if(strategy== 1 || strategy == 2) {			// Strategies where probing for HPs is cheap
			if(net.getNodes().length > Parameters.NUMBER_OF_NODES) {
				for(Node node : availableNodes) {
					if(node.getHoneyPot() == -1) {		// Have not checked this node yet
						//System.out.println("Probing if " + node.getNodeID() + " is a honeypot");
						return new AttackerAction(AttackerActionType.PROBE_HONEYPOT, node.getNodeID());
					}
				}
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
		
		//System.out.println(expValues);

		// Stop if only honeypots are left
		if(strategy == 1 || strategy == 2) {		// Strategies where probing for HPs is cheap
			if(checkForOnlyHoneypots()) {
				//System.out.println("Only honeypots left. Ending turn.");
				return new AttackerAction(AttackerActionType.END_TURN, -1);
			}
		}

		// Find best move to make
		int bestNodeID = -1;
		double bestValue = Integer.MIN_VALUE;
		int typeOfAtk = -1;
		// Loop through expected values to find best node to attack
		for(int i = 0; i < expValues.size(); i++) {
			for(Entry<Integer, Double> entry : expValues.get(i).entrySet()) {
			    Integer nodeID = entry.getKey();
			    Double expValue = entry.getValue();

			    if(expValue > bestValue) {
			    	bestNodeID = nodeID;
			    	bestValue = expValue;
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
		expValues.add(calculateExpValues(true));
		expValues.add(calculateExpValues(false));
		return expValues;
	}
	
	private HashMap<Integer, Double> calculateExpValues(boolean forAtk) {
		HashMap<Integer, Double> expValues = new HashMap<>();
		for(Node node : availableNodes) {
			double estPointsValue = -1.0;
			if(node.getPv() != -1) {
				estPointsValue = (double) node.getPv();
			} else {
				estPointsValue = (double) node.getSv();
			}
			
			if(node.isHoneyPot()) {
				expValues.put(node.getNodeID(), (double) Parameters.HONEY_PENALTY);
			} else {
				if(forAtk) {
					double chance = (double) (Parameters.ATTACK_ROLL - node.getSv() - 1) / Parameters.ATTACK_ROLL;	// ex. SV=5 -> 14/20, since defender wins ties
					double val = estPointsValue * chance;
					val -= Parameters.ATTACK_RATE;		// Here I'm just subtracting the atk cost, assuming 1 budget is worth the same as 1 point
					expValues.put(node.getNodeID(), val);
				} else {
					double chance = (double) (Parameters.SUPERATTACK_ROLL - node.getSv() - 1) / Parameters.SUPERATTACK_ROLL;	// ex. SV=5 -> 14/20, since defender wins ties
					double val = estPointsValue * chance;
					val -= Parameters.SUPERATTACK_RATE;		// Here I'm just subtracting the atk cost, assuming 1 budget is worth the same as 1 point
					expValues.put(node.getNodeID(), val);
				}
			}
		}
		return expValues;
	}
	
	private boolean checkForOnlyHoneypots() {
		boolean onlyHoneypots = true;
		for(Node node : availableNodes) {
			if(!node.isHoneyPot()) {
				onlyHoneypots = false;
				break;
			}
		}
		return onlyHoneypots;
	}
}