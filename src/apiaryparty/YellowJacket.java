package apiaryparty;

import java.util.Random;

/**
 * Example attacker agent.
 * 
 * Make use of the three protected variables inherited from Attacker. These variables include:
 * protected ArrayList&lt;Node&gt; capturedNodes - a list of the already captured nodes
 * protected ArrayList&lt;Node&gt; availableNodes - a list of the available nodes for attacking and probing.
 * protected int budget - the current budget of the Attacker. Be careful that your next move will not cost more than your budget.
 * 
 * @author Marcus - updated by Oscar
 */
public class YellowJacket extends Attacker {

    private final static String attackerName = "YellowJacket";
    
    public Random r;

    /**
     * Constructor
     * @param defenderName defender's name
     * @param graphFile graph to read
     */
	public YellowJacket(String defenderName, String graphFile) {
		super(attackerName, defenderName, graphFile);
	}
	
	/**
	 * default constructor
	 */
	public YellowJacket(){
		super(attackerName);
	}
	
	/**
	 * If you need to initialize anything, do it  here
	 */
	protected void initialize(){
		r = new Random();
	}


	/**
	 * Overide this
	 * @return the action you agent wants to do
	 */
	public AttackerAction makeAction() {
		Random r = new Random();
        if(availableNodes.size()==0)
            return new AttackerAction(AttackerActionType.INVALID,0);
		int nodeID = availableNodes.get(r.nextInt(availableNodes.size())).getNodeID();
		int move = r.nextInt(4);
		AttackerActionType type;
		if(move == 0)
			type = AttackerActionType.ATTACK;
		else if(move == 1)
			type = AttackerActionType.SUPERATTACK;
		else if(move == 2)
			type = AttackerActionType.PROBE_POINTS;
		else if(move == 3)
			type = AttackerActionType.PROBE_HONEYPOT;
		else
			type = AttackerActionType.INVALID;
		return new AttackerAction(type, nodeID);
	}
	/**
	 * The result of your action is updated here if you want to do anything with it. The network will be updated anyway.
	 * @param lastNode the node if successfully attacked
	 */
	protected void result(Node lastNode) {
		// TODO Auto-generated method stub
		
	}
}
