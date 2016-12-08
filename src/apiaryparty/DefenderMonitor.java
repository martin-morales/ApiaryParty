package apiaryparty;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.Stack;

/**
 * Auxiliary class for creating and parsing defense files.
 * Defender will use the three parameter constructor and combination of strengthen(), firewall, and honeypot()
 * to generate defense file. Have Defender remember to call close() when finished for safety.
 * Game Master will use the two parameter constructor for parsing original network and defense file to generate new network.
 *
 * Actions deemed invalid will be charged the Parameters.INVALID_RATE value.
 *
 * @author      Oscar Veliz
 * @version     2014/11/01
 */

public class DefenderMonitor
{
    private Network net;
    private String name;
    private PrintWriter pw;
    private int budget;

    /**
     * Constructor used by Defender to initialize defense file and keep track of network changes.
     * @param network Graph being secured given a budget
     * @param graphFile Contains original name of graph i.e. "1" for 1.graph
     * @param defenderName Name of defender will be prepended to defense file i.e. "tower" for tower-1.defense
     */
    public DefenderMonitor(Network network, String graphFile, String defenderName)
    {
        budget = Parameters.DEFENDER_BUDGET;
        net = network;
        name = defenderName;
        try
        {
            pw = new PrintWriter(name+"-"+graphFile + ".defense", "UTF-8");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    /**
     * Constructor used by Defender to initialize defense file and keep track of network changes.
     * @param defenderName defender name
     * @param graphFile Contains original name of graph i.e. "1" for 1.graph
     * @param actions the actions made
     */
    public DefenderMonitor(String defenderName, String graphFile, String actions){
    	 try
         {
             pw = new PrintWriter(defenderName+"-"+graphFile + ".defense", "UTF-8");
             pw.write(actions);
             pw.close();
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
    }
    /**
     * Intializes the defender monitor with a defender
     * @param d the defender
     */
    public DefenderMonitor(Defender d){
    	name = d.getName();
        budget = Parameters.DEFENDER_BUDGET;
    	net = new Network(d.net);
    }

    /**
     * Constructor used by GameMaster to create new secured graph based on Defender's defense actions.
     * @param defenderName Name of defender prepended to defense file i.e. "tower" for tower-1.defense
     * @param graphFile Contains original name of graph i.e. "1" for 1.graph
     */
    public DefenderMonitor(String defenderName, String graphFile){
        name = defenderName;
        budget = Parameters.DEFENDER_BUDGET;
        net = Parser.parseGraph(graphFile+".graph");
        File csv = new File(defenderName+"-"+graphFile+".defense");
		try
		{
			CSVParser parser = CSVParser.parse(csv, StandardCharsets.US_ASCII, CSVFormat.DEFAULT);
			for (CSVRecord csvRecord : parser)
			{
				Iterator<String> itr = csvRecord.iterator();
                int mode = Integer.parseInt(itr.next());
                switch (mode){
                    case 0://strengthen
                        int id = Integer.parseInt(itr.next());
                        if(isValidStrengthen(id))
                        {
                            budget -= Parameters.STRENGTHEN_RATE;
                            Node n = net.getNode(id);
                            n.setSv(n.getSv()+1);
                        }
                        else
                            budget -= Parameters.INVALID_RATE;
                        break;
                    case 1://firewall
                        int id1 = Integer.parseInt(itr.next());
                        int id2 = Integer.parseInt(itr.next());
                        System.out.println("Attempting to remove Edge [" + id1 + "," + id2 + "]");
                        if(isValidFirewall(id1,id2)){
                            Node n1 = net.getNode(id1);
                            Node n2 = net.getNode(id2);
                            n1.neighbor.remove(n2);
                            n2.neighbor.remove(n1);
                            budget -= Parameters.FIREWALL_RATE;
                        }
                        else
                            budget -= Parameters.INVALID_RATE;

                        break;
                    case 2://honeypot
                        int sv = Integer.parseInt(itr.next());
                        int pv = Integer.parseInt(itr.next());
                        boolean isDB = Boolean.parseBoolean(itr.next());
                        ArrayList<Integer> newNeighbors = new ArrayList<Integer>();
                        while (itr.hasNext())
                            newNeighbors.add(Integer.parseInt(itr.next()));
                        int[] n = new int[newNeighbors.size()];
                        for(int i = 0; i < n.length; i++)
                            n[i] = newNeighbors.get(i);
                   
                        net.addHoneypot(sv, pv, isDB, n);
                        budget -= Parameters.HONEYPOT_RATE;
                       
                     break;
                    case 3://end turn
                    	budget = 0;
                    	break;
                     default://some other case not defined
                        budget -= Parameters.INVALID_RATE;
                     break;
                }
            }
            parser.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        net.setName(name+"-"+graphFile);
        net.shuffleNetwork();//avoid predictable location of honeypot (last node in list)
        net.printNetwork();
        net.printHiddenNetwork();
    }
    /**
     * Applies the defender action
     * @param action the defender action
     */
    public void applyAction(DefenderAction action){
    	//int mode = Integer.parseInt(itr.next());
    	DefenderActionType mode = action.getType();
        switch (mode){
            case STRENGTHEN://strengthen
                int id = action.getSNode();
                if(isValidStrengthen(id))
                {
                    budget -= Parameters.STRENGTHEN_RATE;
                    Node n = net.getNode(id);
                    n.setSv(n.getSv()+1);
                }
                else
                    budget -= Parameters.INVALID_RATE;
                break;
            case FIREWALL://firewall
                int id1 = action.getFwall1();
                int id2 = action.getFwall2();
                System.out.println("Attempting to remove Edge [" + id1 + "," + id2 + "]");
                if(isValidFirewall(id1,id2)){
                    Node n1 = net.getNode(id1);
                    Node n2 = net.getNode(id2);
                    n1.neighbor.remove(n2);
                    n2.neighbor.remove(n1);
                    budget -= Parameters.FIREWALL_RATE;
                }
                else
                    budget -= Parameters.INVALID_RATE;

                break;
            case HONEYPOT://honeypot
            	Random r = new Random();
            	int honeyNodeID = action.getHoneyNode();
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

            	int cost = honeypotCost(honeyNodeID);
            	if (cost > budget){
            		budget -= Parameters.INVALID_RATE;
            		break;
            	}
            	boolean isDatabase = honeyNode.isDatabase();
            	
            	int[] honeyNeighbors = new int[honeyNode.neighbor.size()+1];
            	honeyNeighbors[0] = honeyNode.getNodeID();
            	
            	for(int i = 1; i < honeyNode.neighbor.size(); i++){
            		honeyNeighbors[i] = honeyNode.neighbor.get(i-1).getNodeID();
            	}
            	
            	action.setHPValues(pv, sv);
            	action.setNeighbors(honeyNeighbors);
            	budget -= cost;
	            net.addHoneypot(sv, pv, isDatabase, honeyNeighbors);
            	break;
            case END_TURN://end turn
            	budget = 0;
            	break;
             default://some other case not defined
                budget -= Parameters.INVALID_RATE;
             break;
        }

    	
    }

    /**
     * Defender should call this method when done adding actions.
     */
    public void close()
    {
        pw.close();
    }

    /**
     * Action has been deemed invalid
     */
    public void invalid()
    {
        budget -= Parameters.INVALID_RATE;
        pw.write("-1");
        pw.println();
    }

    /**
     * Adds 1 to a node security value if the node is not public or not already at maximum security.
     * @param id The id of the node being strengthened
     */
    public void strengthen(int id)
    {
        if(isValidStrengthen(id))
        {
            Node n = net.getNode(id);
            budget -= Parameters.STRENGTHEN_RATE;
            n.setSv(n.getSv()+1);
            pw.write("0,"+id);
            pw.println();
        }
        else
            invalid();
    }

    /**
     * Removes the edge between two nodes. Will not remove if doing so will isolate a node. Will not remove if there is no
     * edge to remove.
     *
     * @param id1 First node's id
     * @param id2 Second node's id
     */
    public void firewall(int id1, int id2)
    {
        if(isValidFirewall(id1, id2))
        {
            Node n1 = net.getNode(id1);
            Node n2 = net.getNode(id2);
            n1.neighbor.remove(n2);
            n2.neighbor.remove(n1);
            budget -= Parameters.FIREWALL_RATE;
            pw.write("1,"+id1+","+id2);
            pw.println();
        }
        else{
        	System.out.println("Cannot firewall [" + id1 + "," + id2 + "]");
            invalid();
        }
    }

    /**
     * Adds a honeypot node to the graph if possible. Otherwise charges an invalid.
     * @param sv Security Value for the honeypot
     * @param pv Point Value for the honeypot
     * @param isDB if it is a database
     * @param honeyNodeID the node to attach the honeypot to
     */
    public void honeypot(int sv, int pv, boolean isDB, int honeyNodeID)
    {
    	Node honeyNode = net.getNode(honeyNodeID);
    	
    	int[] newNeighbors = new int[honeyNode.neighbor.size()+1];
    	newNeighbors[0] = honeyNodeID;
    	for(int i = 1; i <= honeyNode.neighbor.size(); i++){
    		newNeighbors[i] = honeyNode.neighbor.get(i-1).getNodeID();
    	}
    	
        if(isValidHoneypot(honeyNodeID, newNeighbors))
        {
            net.addHoneypot(sv, pv, isDB, newNeighbors);
            
            //PRINT HERE
            
            budget -= honeypotCost(honeyNodeID);
            String s = "2,"+sv+","+pv+",";
            for(int i =0; i < newNeighbors.length-1;i++)
                s = s + newNeighbors[i]+",";
            s = s + newNeighbors[newNeighbors.length-1];
            pw.write(s);
            pw.println();
        }else{
            invalid();
        }
    }

    /**
     * Returns current budget.
     * @return current budget
     */
    public int getBudget()
    {
        return budget;
    }

    public boolean isValidStrengthen(int id)
    {
        if(budget < Parameters.STRENGTHEN_RATE){
            return false;
        }else
        {
            Node n = net.getNode(id);
            return (n != null && n.getSv() != 20 && n.getSv() != 0);//can't strengthen public node or maxed out node
        }
    }

 
    public boolean isValidFirewall(int id1, int id2)
    {
        if(budget < Parameters.FIREWALL_RATE)
            return false;
        else
        {
            Node n1 = net.getNode(id1);
            Node n2 = net.getNode(id2);
            if(n1 == null || n2 == null)
                return false;
            else if(n1.neighbor.size()==1 || n2.neighbor.size()==1)
                return false;
            else if(n1.neighbor.contains(n2)){
            	return disconnectsGraph(n1, n2);
            }else{
            	return false;
            }
        }
    }
    
    /**
     * checking if the firewalls with disconnect the graph
     * @n1 the first node
     * @n2 the second node
     * @return if the graph is disconnected
     */
    private boolean disconnectsGraph(Node n1, Node n2){
    	//int n1loc = n2.neighbor.indexOf(n1);
    	//int n2loc = n1.neighbor.indexOf(n2);
    	n1.neighbor.remove(n2);
    	n2.neighbor.remove(n1);
    	boolean n1Dis = canReachPublicNode(n1);
    	boolean n2Dis = canReachPublicNode(n2);
    	n1.neighbor.add(n2);
    	n2.neighbor.add(n1);
    	return n1Dis && n2Dis;
    }
    
    /**
     * checks if the node is reachable from the start
     * @n the node to check
     * @return if the node is reachable
     */
    private boolean canReachPublicNode(Node n){
    	Stack<Node> fringe = new Stack<Node>();
    	boolean[] visited = new boolean[net.getSize()];
    	fringe.push(n);
    	Node current;
    	while(!fringe.isEmpty()){
    		current = fringe.pop();
    		visited[current.getNodeID()] = true;
    		if(current.getSv() == 0)
    			return true;
    		
    		for(int i = 0; i < current.neighbor.size(); i++){
    			Node neighbor = current.neighbor.get(i);
    			if(neighbor.getSv() == 0) //if neighbor is a public node
    				return true;
    			else if(!visited[neighbor.getNodeID()] && !neighbor.isHoneyPot())
    				fringe.push(neighbor);
    		}
    	}
    	return false;
    }

    public boolean isValidHoneypot(int honeyNodeID){
    	Node honeyNode = net.getNode(honeyNodeID);
    	int[] newNeighbors = new int[honeyNode.neighbor.size()+1];
    	newNeighbors[0] = honeyNodeID;
    	for(int i = 1; i <= honeyNode.neighbor.size(); i++){
    		newNeighbors[i] = honeyNode.neighbor.get(i-1).getNodeID();
    	}
        if(budget < honeypotCost(honeyNodeID))
            return false;
        else{
            //check if there are two of the same neighbor (indicator that something is wrong
            //and that all of the nodes being connected to exist
            Arrays.sort(newNeighbors);
            for(int i = 0; i < newNeighbors.length-1;i++)
                if(newNeighbors[i]==newNeighbors[i+1] || net.getNode(newNeighbors[i])==null)
                    return false;
            return true;
        }
    }
    
    public boolean isValidHoneypot(int honeyNodeID, int[] newNeighbors){
        if(budget < honeypotCost(honeyNodeID))
            return false;
        else{
            //check if there are two of the same neighbor (indicator that something is wrong
            //and that all of the nodes being connected to exist
            Arrays.sort(newNeighbors);
            for(int i = 0; i < newNeighbors.length-1;i++)
                if(newNeighbors[i]==newNeighbors[i+1] || net.getNode(newNeighbors[i])==null)
                    return false;
            return true;
        }
    }
    
    public void endGame(){
    	budget = -1;
    }
    
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
    
    public Network getNetwork(){
    	return net;
    }
}
