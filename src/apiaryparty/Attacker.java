package apiaryparty;

import java.util.ArrayList;

/**
 * Attacker agent. The actions for the attacker in this game include attacking a node, super attacking a node, 
 * probing for security values of a node, probing for the point value of a node, probing number of connections, and probing for honey pots.
 * All logic/equations/formulas/etc for how your attacker decides to select actions should be included in makeAction()
 * @author Marcus Gutierrez
 * @version 2014/11/14
 */
public abstract class Attacker{
    private String attackerName = "defaultAttacker"; //Overwrite this variable in your attacker subclass
    private String graph;
    protected Network net; //attacker's visible network
    private String agentName;
    private String defenderName;
    private String graphName;
    private int lastNodeID;
    private AttackerAction lastAction;
    protected ArrayList<Node> capturedNodes;
    protected ArrayList<Node> availableNodes;
    protected int budget;

    /**
     * Constructor.
     * Parses Network stored in graphFile.
     * Performs Attacker logic to select actions.
     * Outputs [agentName]-[graphFile].attack with selected actions
     * @param agentName Attacker agent's name i.e. "Sharks"
     * @param defenderName Defender agent's name i.e. "Jets"
     * @param graphName String containing number of visibility network i.e. "1914"
     */
    public Attacker(String agentName, String defenderName, String graphName){
        attackerName = this.getClass().getSimpleName();
        graph = graphName;
        net = Parser.parseAttackerHistory(attackerName, defenderName, graphName);
        capturedNodes = net.getCapturedNodes();
        availableNodes = net.getAvailableNodes();
        budget = Parser.parseAttackerBudget(attackerName, defenderName, graphName);
    }
    
    public Attacker(String attackerName){
    	this.attackerName = attackerName;
    }
    
    protected abstract void initialize();

    protected final boolean isValidAttack(int id){
    	Node n = net.getNode(id);
        if(budget < Parameters.ATTACK_RATE || n == null)
            return false;
        return true;
    }

    protected boolean isValidSuperAttack(int id){
    	Node n = net.getNode(id);
        if(budget < Parameters.SUPERATTACK_RATE || n == null)
            return false;
        return true;
    }

    protected boolean isValidProbeV(int id){
    	Node n = net.getNode(id);
        if(budget < Parameters.PROBE_POINTS_RATE || n == null)
            return false;
        return true;
    }

    protected boolean isValidProbeHP(int id){
    	Node n = net.getNode(id);
        if(budget < Parameters.PROBE_HONEY_RATE || n == null)
            return false;
        return true;
    }

    /**
     * Executes one action for the attacker
     */
    public final void handleAction(){
    	if(Parameters.VERBOSITY) {
	    	int i;
			System.out.print("Available Nodes: ");
			if(availableNodes.size() > 1){
				for(i = 0; i < availableNodes.size() - 1; i++)
					System.out.print(availableNodes.get(i).getNodeID() + ",");
				System.out.println(availableNodes.get(i).getNodeID());
			} else if(availableNodes.size() == 1) {
				System.out.println(availableNodes.get(0).getNodeID());
			} else
				System.out.println(-1);
			
			int j;
			System.out.print("Captured Nodes: ");
			if(capturedNodes.size() > 1){
	            for(j = 0; j < capturedNodes.size() - 1; j++)
	                System.out.print(capturedNodes.get(j).getNodeID() + ",");
	            System.out.println(capturedNodes.get(j).getNodeID());
			} else if(capturedNodes.size() == 1) {
				System.out.println(capturedNodes.get(0).getNodeID());
			} else 
				System.out.println(-1);
			
			System.out.println("Budget before move: " + budget);
    	}
    	
		try {
	        AttackerAction a = makeAction();
	        if(a != null){
	        	lastNodeID = a.nodeID;
		        switch(a.move){
		        case ATTACK:
		        	if(isValidAttack(a.nodeID)){
		        		budget -= Parameters.ATTACK_RATE;;
		        		lastAction = a;
		        	}
		        	break;
		        case SUPERATTACK:
		        	if(isValidSuperAttack(a.nodeID)){
		        		budget -= Parameters.SUPERATTACK_RATE;;
		        		lastAction = a;
		        	}
		        	break;
		        case PROBE_POINTS:
		        	if(isValidProbeV(a.nodeID)){
		        		budget -= Parameters.PROBE_POINTS_RATE;;
		        		lastAction = a;
		        	}
		        	break;
		        case PROBE_HONEYPOT:
		        	if(isValidProbeHP(a.nodeID)){
		        		budget -= Parameters.PROBE_HONEY_RATE;;
		        		lastAction = a;
		        	}
		        	break;
		        case END_TURN:
					budget = 0;
					lastAction = a;
		        	break;
		        case INVALID:
		        	budget -= Parameters.INVALID_RATE;
		        	lastAction = a;
		        	break;
		        }
	        }else{
	        	lastNodeID = -1;
	        	budget -= Parameters.INVALID_RATE;
	        	lastAction = new AttackerAction(AttackerActionType.INVALID, -1);
	        }
		} catch(Exception e) {
			lastNodeID = -1;
        	budget -= Parameters.INVALID_RATE;
        	lastAction = new AttackerAction(AttackerActionType.INVALID, -1);
		}
        
    }
    
    public abstract AttackerAction makeAction();
    
    protected AttackerAction getLastAction(){
    	return lastAction;
    }
    
    public final void actionResult(){
    	net = Parser.parseAttackerHistory(agentName, defenderName, graphName);
        capturedNodes = net.getCapturedNodes();
        availableNodes = net.getAvailableNodes();
        result(net.getNode(lastNodeID));
    }
    
    public final void actionResult(Network net){
    	this.net = net;
    	capturedNodes = net.getCapturedNodes();
        availableNodes = net.getAvailableNodes();
        result(net.getNode(lastNodeID));
    }
    
    
    protected abstract void result(Node lastNode);

    /**
     * Get Agent Name used by GameMaster.
     * @return Name of defender
     */
    public String getName()
    {
        return this.getClass().getSimpleName();
    }

    /**
     * Get Game used by GameMaster
     * @return graph number
     */
    public final String getGraph()
    {
        return graph;
    }
    
    public void setVisible(Network visible){
    	net = visible;
    }
}
