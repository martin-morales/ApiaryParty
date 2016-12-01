package apiaryparty;

import java.util.Random;

/**
 * Defender agent. The actions for the defender in this game include strengthening nodes, adding firewalls, and adding honeypots.
 * All logic/equations/formulas/etc for how your defender decides to select actions should be included in makeAction()
 */
public abstract class Defender
{
    protected Network net;
    private DefenderMonitor dm;
    protected String defenderName;
    protected String graph;
    private DefenderAction lastAction;
    /**
     * Constructor.
     * Parses Network stored in graphFile.
     * Performs Defender logic to select actions.
     * Outputs [agentName]-[graphFile].defense with selected actions
     * @param agentName Defender agent's name i.e. "Miners"
     * @param graphFile String containing number of network i.e. "1914"
     */
    public Defender(String agentName, String graphFile)
    {
        defenderName = agentName;
        graph = graphFile;
        net = Parser.parseGraph(graphFile+".graph");
        dm = new DefenderMonitor(net,graphFile, agentName);
    }
    
    /**
     * Used for any initializations
     */
    public abstract void initialize();
    
    /**
     * GameMaster uses this method to determine the last action of the player
     * @return last action the player has made
     */
    public DefenderAction getLastAction(){
    	return lastAction;
    }
    
    /**
     * Notifies the defender if their last action was successful and allows for computations
     * based off result of last action.
     * @param actionSuccess the action's success
     */
    public abstract void actionResult(boolean actionSuccess);
    
    /**
     * This method forces the subclass player to make a single action (pickup or move a card).
     * Returning a null/invalid action will result in a wasted turn.
     */
    public void handleAction(){
    	lastAction = null;
        DefenderAction a = makeAction();
        if(a != null){
            switch(a.getType()){
            case STRENGTHEN:
            	if(dm.isValidStrengthen(a.getSNode())){
            		dm.strengthen(a.getSNode());
            		lastAction = a;
            		return;
            	}else{
            		dm.invalid();
            		lastAction = new DefenderAction(); //INVALID MOVE
            		return;
            	}
            case FIREWALL:
            	if(dm.isValidFirewall(a.getFwall1(), a.getFwall2())){
            		dm.firewall(a.getFwall1(), a.getFwall2());
            		lastAction = a;
            		return;
            	}else{
            		dm.invalid();
            		lastAction = new DefenderAction(); //INVALID MOVE
            		return;
            	}
            case HONEYPOT:
            	Random r = new Random();
            	int honeyNodeID = a.getHoneyNode();
            	Node honeyNode = net.getNode(honeyNodeID);
            	
            	int sv = honeyNode.getSv(); //set honeypot's SV to [-1,+1] of source honey node
            	int randTmp = r.nextInt(3)-1;
            	sv += randTmp;
            	sv = Math.min(sv, 19);
            	sv = Math.max(sv, 1);
            	
            	int pv = honeyNode.getPv(); //set honeypot's PV to [-1,+1] of source honey node
            	randTmp = r.nextInt(3)-1;
            	pv += randTmp;
            	pv = Math.min(pv, 19);
            	pv = Math.max(pv, 1);
            	
            	boolean isDatabase = honeyNode.isDatabase();
            	
            	if(dm.isValidHoneypot(honeyNodeID)){
            		dm.honeypot(sv, pv, isDatabase, honeyNodeID);
            		lastAction = a;
            		return;
            	}else{
            		dm.invalid();
            		lastAction = new DefenderAction(DefenderActionType.INVALID); //INVALID MOVE
            		return;
            	}
            case END_TURN:
            	lastAction = new DefenderAction(false);//end turn
            	return;
            default:
            	dm.invalid();
        		lastAction = new DefenderAction(); //INVALID MOVE
        		return;
            }
        }
    }
    
    /**
     * given the node id will returns true if you can strengthen it. false otherwise.
     * @param node the node id
     * @return true if you can strengthen, false otherwise
     */
    protected boolean isValidStrengthen(int node){
    	return dm.isValidStrengthen(node);
    }
    /**
     * given two nodes id will returns true if you can firewall it. false otherwise.
     * @param node1 the first node
     * @param node2 the second node
     * @return true if you can firewall, false otherwise
     */
    protected boolean isValidFirewall(int node1, int node2){
    	return dm.isValidFirewall(node1, node2);
    }
    /**
     * given the node id will returns true if it is a valid honeypot
     * @param honeyNode the node id
     * @return true if it is a valid honeypot, false otherwise
     */
    protected boolean isValidHP(int honeyNode){
    	return dm.isValidHoneypot(honeyNode);
    }

    /**
     * Get Agent Name used by GameMaster.
     * @return Name of defender
     */
    public final String getName()
    {
        return defenderName;
    }

    /**
     * Get Game used by GameMaster
     * @return graph number
     */
    public final String getGraph()
    {
        return graph;
    }

    /**
     * Defender logic goes here
     * @return the action
     */
    public abstract DefenderAction makeAction();
    
    /**
     * Returns remaining defender budget
     * @return the budget
     */
    public int getBudget(){
    	return dm.getBudget();
    }
    
    /**
     * Ends game
     */
    public void endGame(){
    	dm.endGame();
    	dm.close();
    }
    
    /**
     * Computes the cost of a honeypot based on source node of honeypot
     * @param honeyNode the source node
     * @return the cost of the honeypot
     */
    public int honeypotCost(int honeyNode){
    	return net.getNode(honeyNode).getPv() + Parameters.HONEYPOT_RATE;
    	/*switch(ht){
    		case NETWORKED_CONVIENCE:
    			return Parameters.HONEYPOT_RATE;
    		case PERSONAL_DEVICE:
    			return Parameters.HONEYPOT_RATE*2;
    		case SECURED_DEVICE:
    			return Parameters.HONEYPOT_RATE*3;
    		case DATABASE:
    			return Parameters.HONEYPOT_RATE*5;
    		default:
    			return Parameters.INVALID_RATE;
    			
    	}*/
    }
    

}
