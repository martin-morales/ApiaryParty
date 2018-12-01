package apiaryparty;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

/**
 * Auxiliary class for creating and parsing .attack files.
 * Attacker will use the three parameter constructor and combination of the two attack methods and the four probe methods
 * to generate an .attack file. Have Attacker remember to call close() when finished for safety.
 * Game Master will use the two parameter constructor for parsing original network and attacker file to generate the attacker's visible network and history.
 *
 * Actions deemed invalid will be charged the Parameters.INVALID_RATE value.
 *
 * @author      Marcus Gutierrez
 * @version		2014/11/14
 */

public class AttackerMonitor
{
	private Network net;
	private Network visibleNet;

	private String attackerName;
	private String defenderName;
	private String graphName;
	private PrintWriter history;

	private int budget;
	private int points;
	private Random r;
	private ArrayList<Node> availableNodes;

	/**
	 * Constructor
	 * @param attackerName attacker name
	 * @param defenderName defender name
	 * @param graphName graph name
	 */
	public AttackerMonitor(String attackerName, String defenderName, String graphName){
		budget = Parameters.ATTACKER_BUDGET;
		if(Parameters.VERBOSITY)
			System.out.println("Initial Budget: " + budget);
		r = new Random();
		this.attackerName = attackerName;
		this.defenderName = defenderName;
		this.graphName = graphName;
		try {
			net = Parser.parseGraph(defenderName + "-" + graphName + ".graph");
			visibleNet = Parser.parseGraph(defenderName + "-" + graphName + "-hidden.graph");
			availableNodes = visibleNet.getAvailableNodes();

			//clears history and adds the public nodes to the history
			history = new PrintWriter(new FileWriter(attackerName + "-" + defenderName + "-" + graphName + ".history", false));
			ArrayList<Node> publicNodes = visibleNet.getCapturedNodes();
			for(int i = 0; i < publicNodes.size(); i++){
				String publicString = "6," + publicNodes.get(i).getNodeID()+",";
				ArrayList<Node> neighbors = publicNodes.get(i).getNeighborList();
				for(int j = 0; j < neighbors.size(); j++)
					publicString += neighbors.get(j).getNodeID() + ",";
				publicString = publicString.substring(0, publicString.length() - 1);
				history.println(publicString);
			}
			history.close();
		} catch (IOException e) {e.printStackTrace();}
		catch(Exception e){System.out.println("Unknonwn exception could not run game");}
		history.close();
	}

	/**
	 * Parses attacker's move and writes the result to history
	 * @param a The attacker action
	 * @return the network
	 */
	public Network readMove(AttackerAction a){
		try{
			visibleNet = Parser.parseAttackerHistory(attackerName, defenderName, graphName);
			history = new PrintWriter(new FileWriter(attackerName + "-" + defenderName + "-" + graphName + ".history", true));
			availableNodes = visibleNet.getAvailableNodes();

			int id;
			String neighbors = ",";
			switch (a.move){
			case ATTACK://attack node
				id = a.nodeID;
				if(isAvailableNode(id) && isValidAttack(id))
				{
					budget -= Parameters.ATTACK_RATE;
					Node n = net.getNode(id);
					int attackRoll = r.nextInt(Parameters.ATTACK_ROLL) + 1; 
					if(attackRoll >= n.getSv()){
						visibleNet.getNode(id).setPv(n.getPv());
						visibleNet.getNode(id).setSv(n.getSv());
						visibleNet.getNode(id).setHoneyPot(n.getHoneyPot());
						visibleNet.getNode(id).setCaptured(true);
						if(visibleNet.getNode(id).isHoneyPot()){
							//points += Parameters.HONEY_PENALTY;
							//points -= visibleNet.getNode(id).getPv();
							budget = 0;
							if(Parameters.VERBOSITY)
								System.out.println("attack on node " + id + " triggered a honeypot with a roll of " + attackRoll + "! Ending round.");
							history.println("0," + id + ",true," + attackRoll + "," + n.getPv() + "," + n.getSv() + "," + n.isDatabase() + "," + n.getHoneyPot());
							break;
						}else{
							points += visibleNet.getNode(id).getPv();
							/*for(int j = 0; j < n.neighbor.size(); j++){
								visibleNet.getNode(id).addNeighbor(visibleNet.getNode(n.neighbor.get(j).getNodeID()));
								neighbors += n.neighbor.get(j).getNodeID() + ",";
							}*/
						}
						neighbors = neighbors.substring(0, neighbors.length()-1);
						if(Parameters.VERBOSITY)
							System.out.println("attack on node " + id + " was successful with a roll of " + attackRoll + "!");
						//history.println("0," + id + ",true," + attackRoll + "," + n.getPv() + "," + n.getSv() + "," + n.isDatabase() + "," + n.getHoneyPot() + neighbors);
						history.println("0," + id + ",true," + attackRoll + "," + n.getPv() + "," + n.getSv() + "," + n.isDatabase() + "," + n.getHoneyPot());
					}else{
						if(Parameters.VERBOSITY)
							System.out.println("attack on node " + id + " was unsuccessful with a roll of " + attackRoll);
						history.println("0," + id + ",false," + attackRoll);
					}
				}
				else{
					if(Parameters.VERBOSITY)
						System.out.println("Invalid attack on node "+ id + "! " + isAvailableNode(id));
					history.println("-1");
					budget -= Parameters.INVALID_RATE;
				}
				break;
			case SUPERATTACK://super attack node
				id = a.nodeID;
				if(isAvailableNode(id) && isValidSuperAttack(id))
				{
					budget -= Parameters.SUPERATTACK_RATE;
					Node n = net.getNode(id);

					int attackRoll = r.nextInt(Parameters.SUPERATTACK_ROLL) + 1; 
					if(attackRoll >= n.getSv()){
						visibleNet.getNode(id).setPv(n.getPv());
						visibleNet.getNode(id).setSv(n.getSv());
						visibleNet.getNode(id).setHoneyPot(n.getHoneyPot());
						visibleNet.getNode(id).setCaptured(true);
						if(visibleNet.getNode(id).isHoneyPot()){
							//points += Parameters.HONEY_PENALTY;
							//points -= visibleNet.getNode(id).getPv();
							budget = 0;
							if(Parameters.VERBOSITY)
								System.out.println("super attack on node " + id + " triggered a honeypot with a roll of " + attackRoll + "! Ending round.");
							history.println("1," + id + ",true," + attackRoll + "," + n.getPv() + "," + n.getSv() + "," + n.isDatabase() + "," + n.getHoneyPot());
							break;
						}else{
							points += visibleNet.getNode(id).getPv();
							/*for(int j = 0; j < n.neighbor.size(); j++){
								visibleNet.getNode(id).addNeighbor(visibleNet.getNode(n.neighbor.get(j).getNodeID()));
								neighbors += n.neighbor.get(j).getNodeID() + ",";
							}*/
						}
						neighbors = neighbors.substring(0, neighbors.length()-1);
						if(Parameters.VERBOSITY)
							System.out.println("super attack on node " + id + " was successful with a roll of " + attackRoll + "!");
						//history.println("1," + id + ",true," + attackRoll + "," + n.getPv() + "," + n.getSv() + "," + n.isDatabase() + "," + n.getHoneyPot() + neighbors);
						history.println("1," + id + ",true," + attackRoll + "," + n.getPv() + "," + n.getSv() + "," + n.isDatabase() + "," + n.getHoneyPot());
					}else{
						if(Parameters.VERBOSITY)
							System.out.println("super attack on node " + id + " was unsuccessful with a roll of " + attackRoll);
						history.println("1," + id + ",false," + attackRoll);
					}
				}
				else{
					if(Parameters.VERBOSITY)
						System.out.println("Invalid superattack on node "+ id + "! " + isAvailableNode(id));
					history.println("-1");
					budget -= Parameters.INVALID_RATE;
				}
				break;
			case PROBE_POINTS://probe point value
				id = a.nodeID;
				if(isAvailableNode(id) && isValidProbeV(id))
				{
					budget -= Parameters.PROBE_POINTS_RATE;
					Node n = net.getNode(id);

					int pv = n.getPv();
					int sv = n.getSv();
					visibleNet.getNode(id).setPv(pv);
					//visibleNet.getNode(id).setSv(sv);
					if(Parameters.VERBOSITY)
						System.out.println("probed node " + id + "'s point value: " + pv);
					history.println("2," + id + "," + pv + "," + sv);
				}
				else{
					if(Parameters.VERBOSITY)
						System.out.println("Invalid probing of point value on node "+ id + "! " + isAvailableNode(id));
					history.println("-1");
					budget -= Parameters.INVALID_RATE;
				}
				break;
			case PROBE_HONEYPOT://probe honey pot
				id = a.nodeID;
				if(isAvailableNode(id) && isValidProbeHP(id))
				{
					budget -= Parameters.PROBE_HONEY_RATE;
					Node n = net.getNode(id);

					visibleNet.getNode(id).setHoneyPot(n.isHoneyPot());
					if(Parameters.VERBOSITY)
						System.out.println("probed node " + id + "'s honey pot: " + n.isHoneyPot());
					history.println("3," + id + "," + n.getHoneyPot());
				}
				else{
					if(Parameters.VERBOSITY)
						System.out.println("Invalid probing of honey pot on node "+ id + "! " + isAvailableNode(id));
					history.println("-1");
					budget -= Parameters.INVALID_RATE;
				}
				break;
			case END_TURN:
				budget = 0;
				break;
			default://some other case not defined
				if(Parameters.VERBOSITY)
					System.out.println("Invalid Move!");
				history.println("-1");
				budget -= Parameters.INVALID_RATE;
				break;
			}
			
			history.close();
			availableNodes = visibleNet.getAvailableNodes();
			
		}catch (Exception e){
			e.printStackTrace();
		}
		return visibleNet;
	}

	/**
	 * Attacker should call this method when done adding actions.
	 */
	public void close()
	{
		history.close();
	}

	/**
	 * Returns current budget.
	 * @return current budget
	 */
	public int getBudget()
	{
		return budget;
	}

	/**
	 * Actually doesn't check if move is valid but rather if there is enough budget for this move
	 * Ping Marcus to fix this
	 * @param id node id
	 * @return if there is enough budget for this move
	 */
	public boolean isValidAttack(int id){
		if(budget < Parameters.ATTACK_RATE)
			return false;
		return true;
	}

	/**
	 * Actually doesn't check if move is valid but rather if there is enough budget for this move
	 * Ping Marcus to fix this
	 * @param id node id
	 * @return if there is enough budget for this move
	 */
	public boolean isValidSuperAttack(int id){
		if(budget < Parameters.SUPERATTACK_RATE)
			return false;
		return true;
	}

	/**
	 * Actually doesn't check if move is valid but rather if there is enough budget for this move
	 * Ping Marcus to fix this
	 * @param id node id
	 * @return if there is enough budget for this move
	 */
	public boolean isValidProbeV(int id){
		if(budget < Parameters.PROBE_POINTS_RATE)
			return false;
		return true;
	}

	/**
	 * Actually doesn't check if move is valid but rather if there is enough budget for this move
	 * Ping Marcus to fix this
	 * @param id node id
	 * @return if there is enough budget for this move
	 */
	public boolean isValidProbeHP(int id){
		if(budget < Parameters.PROBE_HONEY_RATE)
			return false;
		return true;
	}

	/**
	 * Checks if node is available based on id
	 * @param id ID of node you want to check
	 * @return if node is available
	 */
	private boolean isAvailableNode(int id){
		Node n = new Node(id);
		return availableNodes.contains(n);
	}

	/**
	 * Gets total points
	 * @return total points
	 */
	public int getPoints()
	{
		return points;
	}
	
	public int numAvailNodes()
	{
		return availableNodes.size();
	}
}
