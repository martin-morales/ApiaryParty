package apiaryparty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Martin Morales, Cynthia Valencia
 */
public class CryBayBee extends Defender{

	private Node[] nodes = net.getNodes();
	private final static String attackerName = "CryBayBee";
	Random r;
	private int strategy;
	private static int s3numStrength;
	private static int s2numHoneyPots;
	private double hpNodes;


	    public CryBayBee(String graphFile)
	    {
	        super("CryBayBee", graphFile);
	    }

	    @Override
		public void initialize() {
			r = new Random();
			//s2numStrength = 0;	// the number of nodes strengthened if playing strategy 2
			s3numStrength = 0;	// the number of nodes strengthened if playing strategy 3
			s2numHoneyPots = 0;

			if(Parameters.STRENGTHEN_RATE == Parameters.PROBE_POINTS_RATE &&
					Parameters.STRENGTHEN_RATE == Parameters.HONEYPOT_RATE){
				strategy = 1;		// Make all honeypots
			}
			else if((Parameters.STRENGTHEN_RATE * 0.01) <= 0.07 && (Parameters.HONEYPOT_RATE * 0.01) <= 1.5){
				strategy = 2;		// cheap strength/hp
			}
			else if((Parameters.STRENGTHEN_RATE * 0.01) <= 0.07 && (Parameters.FIREWALL_RATE * 0.01) <= 0.08){
				strategy = 3;		// cheap strength/fw
			}
			else if((Parameters.HONEYPOT_RATE * 0.01) <= 1.5){
				strategy = 4;		// cheap hp
			}
			else if((Parameters.FIREWALL_RATE * 0.01) <= 0.08){
				strategy = 5;		// cheap fw
			}
			else{
				strategy = 6; 		// default strategy/cheap strength
			}
		}

		@Override
		public void actionResult(boolean actionSuccess) {
		}

		@Override
		public DefenderAction makeAction() {
			if (strategy == 1) {            // all strategies cost the same so make all honeypots
				Random r = new Random();
				int honeyNode = r.nextInt(net.getAvailableNodes().size());
				int honeypotCost = honeypotCost(honeyNode);

				if (getBudget() < honeypotCost) {
					return new DefenderAction(DefenderActionType.INVALID);
				}
				return new DefenderAction(DefenderActionType.HONEYPOT, honeyNode);
			}
			else if (strategy == 2) { // strengthening and honeypots are cheap (use ratio of how many honeypots to make)
				// 70% Honey Pots
				hpNodes =  Parameters.NUMBER_OF_NODES * 0.70;
				int numHPNodes = (int) hpNodes;

				if(s2numHoneyPots < numHPNodes){ // if honeypot nodes you have is less than 70% honeypots
					Random r = new Random();
					int honeyNode = r.nextInt(net.getAvailableNodes().size());
					int honeypotCost = honeypotCost(honeyNode);

					if (getBudget() < honeypotCost) {
						return new DefenderAction(DefenderActionType.INVALID);
					}
					s2numHoneyPots++;
					return new DefenderAction(DefenderActionType.HONEYPOT, honeyNode);
				}
				else{
					return makeMaxExpValDecision();

				}
			} else if (strategy == 3) { // strengthening and firewall is cheap (use ratio of how many firewalls to add
				// 50% strengthening
				double n =  Parameters.NUMBER_OF_NODES * 0.50;
				int numStrengtheningNodes = (int) n;

				if(s3numStrength < numStrengtheningNodes){ // if strengthened nodes is less than 50%
					s3numStrength ++;
					return makeMaxExpValDecision();
				}
				// else make firewalls
				else{
					return firewall();
				}
			} else if (strategy == 4) { 	// only honeypots
				Random r = new Random();
				int honeyNode = r.nextInt(net.getAvailableNodes().size());
				int honeypotCost = honeypotCost(honeyNode);

				if (getBudget() < honeypotCost) {
					return new DefenderAction(DefenderActionType.INVALID);
				}
				return new DefenderAction(DefenderActionType.HONEYPOT, honeyNode);
			}
			else if (strategy == 5) { 		//only firewalls
				return firewall();
			}
			else {		// only strengthen
				return makeMaxExpValDecision();
			}
		}

	/**
	 * Loops through all available nodes and calculates the expected value for the attacker; which is equal to
	 * defender's expected regret
	 * NodeExpValue = PV * (AtckRollChance - SV / AtckRollChance) - AtckRollCost
	 */

	private ArrayList<HashMap<Integer, Double>> calculateExpRegret() {
		// First hash map is to store expected node values with atk rolls, second is for super atk rolls
		ArrayList<HashMap<Integer, Double>> expValues = new ArrayList<HashMap<Integer, Double>>();
		expValues.add(calculateExpRegret(true));
		expValues.add(calculateExpRegret(false));
		return expValues;
	}

	private HashMap<Integer, Double> calculateExpRegret(boolean forAtk) {
		HashMap<Integer, Double> expValues = new HashMap<>();
		for(Node node : nodes) {
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

	private DefenderAction makeMaxExpValDecision() {
		ArrayList<HashMap<Integer, Double>> expValues = calculateExpRegret();

		// Find best move to make
		int bestNodeID = -1;
		double bestCost = Integer.MIN_VALUE;

		for(int i = 0; i < expValues.size(); i++) {
			for(Map.Entry<Integer, Double> entry : expValues.get(i).entrySet()) {
				Integer key = entry.getKey();
				Double value = entry.getValue();

				if(value > bestCost) {
					bestNodeID = key;
					bestCost = value;
				}
			}
		}

		if(Parameters.DEFENDER_BUDGET > Parameters.STRENGTHEN_RATE)
			return new DefenderAction(DefenderActionType.STRENGTHEN, bestNodeID);
		else
			return new DefenderAction(DefenderActionType.END_TURN, -1);
	}

	private Node[] findPublicNodes(){
		Node publicNodes [] = new Node[Parameters.NUMBER_OF_PUBLIC_NODES];
		int foundPublicNodes = 0;

		int i = 0;
		for(Node node : nodes) {
			if(foundPublicNodes == Parameters.NUMBER_OF_PUBLIC_NODES){
				break;
			}
			if(node.getPv() == 0){ // 0 point value means a public node
				publicNodes[i] = node; // add node
				foundPublicNodes ++;
				i++;
			}

		}
		return publicNodes;
	}

	private HashMap<Integer, Double> calculateExpRegretFirewall(boolean forAtk) {
		HashMap<Integer, Double> expValues = new HashMap<>();
		Node publicNodes [] = findPublicNodes();

		for(Node node : publicNodes) {
			ArrayList<Node> neighbors = node.getNeighborList();
			for (Node neighbor : neighbors){
				double estPointsValue = -1.0;
				if(neighbor.getPv() != -1) {
					estPointsValue = (double) neighbor.getPv();
				} else {
					estPointsValue = (double) neighbor.getSv();
				}

				if(neighbor.isHoneyPot()) {
					expValues.put(neighbor.getNodeID(), (double) Parameters.HONEY_PENALTY);
				} else {
					if(forAtk) {
						double chance = (double) (Parameters.ATTACK_ROLL - neighbor.getSv() - 1) / Parameters.ATTACK_ROLL;	// ex. SV=5 -> 14/20, since defender wins ties
						double val = estPointsValue * chance;
						val -= Parameters.ATTACK_RATE;		// Here I'm just subtracting the atk cost, assuming 1 budget is worth the same as 1 point
						expValues.put(neighbor.getNodeID(), val);
					} else {
						double chance = (double) (Parameters.SUPERATTACK_ROLL - neighbor.getSv() - 1) / Parameters.SUPERATTACK_ROLL;	// ex. SV=5 -> 14/20, since defender wins ties
						double val = estPointsValue * chance;
						val -= Parameters.SUPERATTACK_RATE;		// Here I'm just subtracting the atk cost, assuming 1 budget is worth the same as 1 point
						expValues.put(neighbor.getNodeID(), val);
					}
				}
			}

		}
		return expValues;
	}

	private DefenderAction firewall(){
		Node publicNodes [] = findPublicNodes();
		ArrayList<Node> neighborNodes = new ArrayList<Node>();
		HashMap<Integer, Double> expNeighborRegrets = new HashMap<>();

		// Calculate exp regret for each node that's connected to a public node
		for(Node node : publicNodes) {
			ArrayList<Node> neighbors = node.getNeighborList();
			for (Node neighbor : neighbors) {
				double nodeRegret = calculateExpRegret(neighbor);
				expNeighborRegrets.put(neighbor.getNodeID(), nodeRegret);
				neighborNodes.add(neighbor);
			}
		}

		// Create firewall between neighbor with highest regret and public node
		// Get highest regret node
		int highestRegretNodeID = -1;
		double highestRegret = Double.MIN_VALUE;
		for (Node node : neighborNodes) {
			if(expNeighborRegrets.get(node.getNodeID()) > highestRegret) {
				highestRegretNodeID = node.getNodeID();
				highestRegret = expNeighborRegrets.get(node.getNodeID());
			}
		}

		// Get public node id attached to the neighbor node with heighest exp regret
		Node highestRegretNode = net.getNode(highestRegretNodeID);
		int publicNodeID = -1;
		for(Node neighbor : highestRegretNode.getNeighborList()) {
			if(neighbor.isCaptured()) {
				publicNodeID = neighbor.getNodeID();
				break;
			}
		}

		// Make firewall if we have the budget
		if(Parameters.DEFENDER_BUDGET > Parameters.FIREWALL_RATE)
			return new DefenderAction(publicNodeID, highestRegretNodeID);
		else
			return new DefenderAction(DefenderActionType.END_TURN, -1);
	}

	private double calculateExpRegret(Node node) {
		double chance = (double) (Parameters.ATTACK_ROLL - node.getSv() - 1) / Parameters.ATTACK_ROLL;
		double expRegret = node.getPv() * chance - Parameters.ATTACK_RATE;
		return expRegret;
	}
}