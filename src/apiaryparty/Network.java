package apiaryparty;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
/**
 * Network class is use for generating a network.
 * Game master will use this class to generate a network. 
 *
 * @author      Porag Chowdhury, Anjon Basak
 * @version     2014/11/01
 */

public class Network {
	private int name;
	private String fullGraphName;//for when the graph is modified by an agent i.e. Miners-1
	private Node[] nodes = new Node[Parameters.NUMBER_OF_NODES];

	public Network(){}

	/**
	 * Constructor used by Game master to initialize network.
	 * @param networkName An integer indicates network name
	 */
	public Network(int networkName)
	{
		name = networkName;
		fullGraphName = ""+name;//for now
		for(int i=0; i<Parameters.NUMBER_OF_NODES; i++)
		{
			nodes[i] = new Node();
			nodes[i].setNodeID(i);
		}
		generateNetwork();
	}

	/**
	 * Constructor used by Game master to initialize network.
	 * @param networkName An integer indicates network name
	 * @param numNodes An integer indicates number of nodes in the network
	 */
	public Network(int networkName, int numNodes){
		name = networkName;
		fullGraphName = "" + name;
		nodes = new Node[numNodes];
		for(int i=0; i<numNodes; i++){
			nodes[i] = new Node();
			nodes[i].setNodeID(i);
		}
	}

	public Network(Network n){
		this.name = n.name;
		this.nodes = new Node[n.nodes.length];
		for(int i = 0; i < nodes.length; i++){
			nodes[i] = n.nodes[i].clone();
		}
		for(int i = 0; i < nodes.length; i++){
			for(int j = 0; j < n.getNode(i).neighbor.size(); j++){
				int neighborID = n.getNode(i).neighbor.get(j).getNodeID();
				nodes[i].addNeighbor(nodes[neighborID]);
			}
		}
	}

	/**
	 * Returns network name.
	 * @return network name
	 */
	public int getName() {
		return name;
	}

	/**
	 * Sets network full name.
	 * @param name network name
	 */
	public void setName(String name) {
		fullGraphName = name;
	}

	/**
	 * Sets network name.
	 * @param name network name
	 */
	public void setName(int name) {
		this.name = name;
	}

	/**
	 * Returns node
	 * @param nodeId An integer indicates nodeId
	 * @return returns node.
	 */
	public Node getNode(int nodeId){
		if(nodeId >= nodes.length || nodeId < 0)
			return null;
		for (Node node : nodes)
		{
			if (node.getNodeID() == nodeId)
				return node;
		}
		return null;
	}


	/**
	 * Returns boolean validating a node to be eligible for Neighbor or not
	 * @param currentIndex An integer indicates current node id
	 * @param neighborIndex An integer indicates neighbor node id
	 * @param adjacencyMatrix A two dimensional array for adjacency
	 * @return boolean True/False validating a node to be eligible for Neighbor or not
	 */
	public boolean isAllowedToBeNeighbor(int currentIndex, int neighborIndex, int [][] adjacencyMatrix)
	{
		if (currentIndex == neighborIndex)
			return false;
		int neighborCount = 0;
		for(int i=0; i < adjacencyMatrix[neighborIndex].length; ++i)
		{
			if (adjacencyMatrix[neighborIndex][i] == 1)
				neighborCount++;
		}
		return neighborCount < Parameters.MAX_NEIGHBORS;

	}

	/**
	 * Returns size of the network
	 * @return size of the network i.e. number of total nodes
	 */
	public int getSize()
	{
		return nodes.length;
	}

	/**
	 * Adds Honeypot in the network
	 * @param sv An integer indicates security value
	 * @param pv An integer indicates point value
	 * @param isDatabase if it is a database honeypot
	 * @param neighbors An integer array indicates all the neighbors
	 */
	public void addHoneypot(int sv, int pv, boolean isDatabase, int[] neighbors){
		Node honeypot = new Node(nodes.length, sv, pv, isDatabase, 1);
		Node[] n = new Node[nodes.length+1];
		for(int i = 0; i < nodes.length; i++)
			n[i] = nodes[i];
		n[nodes.length] = honeypot;

		for(int i = 0; i < neighbors.length; i++){
			honeypot.neighbor.add(nodes[neighbors[i]]);
			nodes[neighbors[i]].neighbor.add(honeypot);
			
			//nodes[neighbors[i]].neighbor.add(n[nodes.length]);
		}
		nodes = n;
	}

	/**
	 * Print hidden network in a file.  private or unexplored nodes' rows will have -1.
	 * Edited by Marcus Gutierrez (11/12/2014 - 7:24 AM)
	 */
	public void printHiddenNetwork()
	{
		//ArrayList<Node> captured = getCapturedNodes();
		//System.out.println("Captured: " + captured.size());
		PrintWriter writer;
		try {
			writer = new PrintWriter(fullGraphName + "-hidden.graph", "UTF-8");
			for (int i = 0; i < nodes.length; i++)
			{
				Node node = getNode(i);
				int neighborSize = node.neighbor.size();

				int neighborCounter = 0;
				for(Node neighbor: node.neighbor)
				{
					if(neighbor.getNodeID()!=node.getNodeID())
					{
						if(neighborCounter==neighborSize-1)
							writer.print(neighbor.getNodeID());
						else
							writer.print(neighbor.getNodeID()+",");
					}
					neighborCounter++;
				}
				writer.println();

			}
			for (int i = 0; i < nodes.length; i++){
				Node node = getNode(i);
				if(node.isCaptured() == true)
					writer.println(node.getPv()+","+node.getSv()+","+node.isDatabase()+","+node.getHoneyPot());
				else
					writer.println("-1," + node.getSv() + "," + node.isDatabase()+",-1");
//					writer.println("-1,-1,"+node.isDatabase()+",-1");
			}
			writer.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch ( Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Print network in a file
	 */
	public void printNetwork()
	{
		PrintWriter writer;
		try {
			writer = new PrintWriter(fullGraphName + ".graph", "UTF-8");
			for (int i = 0; i < nodes.length; i++)
			{
				Node node = getNode(i);
				int neighborSize = node.neighbor.size();
				int neighborCounter = 0;

				if (node.neighbor.get(0) == null)
				{
					writer.print("-1");
				}
				else
				{
					for(Node neighbor: node.neighbor)
					{
						if(neighbor.getNodeID()!=node.getNodeID())
						{
							if(neighborCounter==neighborSize-1)
								writer.print(neighbor.getNodeID());
							else 
								writer.print(neighbor.getNodeID()+",");
						}
						neighborCounter++;
					}
				}
				writer.println();
			}
			for (int i = 0; i < nodes.length; i++)
			{
				Node node = getNode(i);
				//writer.println(node.getPv()+","+node.getSv()+","+node.getHoneyPot());
				writer.println(node.getPv()+","+node.getSv()+","+node.isDatabase()+","+node.getHoneyPot());
			}
			writer.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch ( Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns all the nodes in the network.
	 * @return nodes in the network
	 */
	public Node[] getNodes(){
		return nodes;
	}

	/**
	 * Shuffles all the nodes in the network
	 */
	public void shuffleNetwork()
	{
		ArrayList<Integer> assigned = new ArrayList<Integer>();
		Random rand = new Random();
		for(int i = 0; i< this.nodes.length; i++)
		{

			while(true)
			{
				int id = rand.nextInt(nodes.length);
				if((assigned.size()==0) || (!assigned.contains(id)))
				{
					this.nodes[i].setNodeID(id);
					assigned.add(id);
					break;
				}

			}
		}
	}

	/**
	 * Generates a random network based on the parameter class and prints it in a file
	 */
	public void generateNetwork()
	{
		//Network network = new Network(networkName, numNodes);
		Random r = new Random(name);
		int [][] adjacencyMatrix = new int[Parameters.NUMBER_OF_NODES][Parameters.NUMBER_OF_NODES];
		for(int i =0; i<nodes.length; i++)
			Arrays.fill(adjacencyMatrix[i], 0);
		ArrayList<Integer> completedNodes = new ArrayList<Integer>();
		ArrayList<Integer> tmpNodeStack = new ArrayList<Integer>();
		int currentIndex = 0;
		for (int i = 0; i < nodes.length; i++)
		{
			int localMax = r.nextInt(Parameters.MAX_NEIGHBORS - Parameters.MIN_NEIGHBORS) + Parameters.MIN_NEIGHBORS;
			int neighborCounter = 0;
			ArrayList<Integer> tmpNeighbors = new ArrayList<Integer>();
			ArrayList<Integer> rejectedNeighbors = new ArrayList<Integer>();
			while(true)
			{
				int nodeIndex= r.nextInt(nodes.length);
				int totalNeighbors = 0;
				for(int k=0; k<nodes.length; k++)
					if(adjacencyMatrix[currentIndex][k]==1)
						totalNeighbors++;
				if(totalNeighbors==Parameters.MAX_NEIGHBORS)
					break;
				if(rejectedNeighbors.size()>0)
					if(rejectedNeighbors.size()==(nodes.length-tmpNeighbors.size()-1))
						break;

				if (isAllowedToBeNeighbor(currentIndex, nodeIndex, adjacencyMatrix))
				{
					if((tmpNeighbors.size()>0 && !tmpNeighbors.contains(nodeIndex) && tmpNeighbors.size() < Parameters.MAX_NEIGHBORS) || tmpNeighbors.size()==0)
					{
						//System.out.println("Current Index  " + currentIndex + "neighbor counter "+ neighborCounter + " Num Neighbors " + localMax);
						//System.out.println("Got Neighbor " + nodeIndex);
						adjacencyMatrix[currentIndex][nodeIndex] = 1;
						adjacencyMatrix[nodeIndex][currentIndex] = 1;
						tmpNeighbors.add(nodeIndex);
						if (!tmpNodeStack.contains(nodeIndex)){
							tmpNodeStack.add(nodeIndex);
							//System.out.println("Adding to stack: " + nodeIndex);
						}
						neighborCounter++;
						if(neighborCounter==localMax)
						{	
							//System.out.println("Neighbour Count for " + currentIndex + " : " +neighborCounter);
							break;
						}
					}
					else
						if(rejectedNeighbors.size()>=0 && !rejectedNeighbors.contains(nodeIndex))
							rejectedNeighbors.add(nodeIndex);
				}
				else
					if(rejectedNeighbors.size()>=0 && !rejectedNeighbors.contains(nodeIndex))
						rejectedNeighbors.add(nodeIndex);
			}
			completedNodes.add(currentIndex);
			while(true)
			{
				if (tmpNodeStack.size() == 0)
					break;
				// pick a node from the stack
				currentIndex = tmpNodeStack.get(0);
				//System.out.println("Current index: " + currentIndex);
				tmpNodeStack.remove(0);
				if (!completedNodes.contains(currentIndex))
					break;
			}
		}
		ArrayList<Integer> tmpPublicNodes = new ArrayList<Integer>();
		int publicNodeCounter = 0;
		while(true)
		{
			int nodeIndex= r.nextInt(nodes.length);
			if((tmpPublicNodes.size()>0 && !tmpPublicNodes.contains(nodeIndex)) || tmpPublicNodes.size()==0)
			{
				tmpPublicNodes.add(nodeIndex);
				publicNodeCounter++;
				if(publicNodeCounter==Parameters.NUMBER_OF_PUBLIC_NODES)
					break;
			}
		}

		ArrayList<Integer> tmpDBNodes = new ArrayList<Integer>();
		int databaseNodeCounter = 0;
		while(true)
		{
			int routerNodeIndex= r.nextInt(nodes.length);
			if (!tmpPublicNodes.contains(routerNodeIndex))
			{
				if((tmpDBNodes.size()>0 && !tmpDBNodes.contains(routerNodeIndex)) || tmpDBNodes.size()==0)
				{
					tmpDBNodes.add(routerNodeIndex);
					databaseNodeCounter++;
					if(databaseNodeCounter==Parameters.NUMBER_OF_DATABASE_NODES)
						break;
				}
			}
		}

		for(int i = 0; i < nodes.length; ++i)
		{
			Node tempNode = getNode(i);
			tempNode.setNodeID(i);
			tempNode.setHoneyPot(0);

			if(tmpPublicNodes.contains(i))
			{
				tempNode.setPv(0);
				tempNode.setSv(0);
				tempNode.setHoneyPot(0);
				tempNode.setCaptured(true);
			}
			else if(tmpDBNodes.contains(i))
			{
				//Sets some nodes as the database nodes which guarantee high point values
				int sv = r.nextInt(8) + 12;
				int pv = r.nextInt(10) + 20;
				tempNode.setSv(sv);
				tempNode.setPv(pv);
				tempNode.setDB(true);
				tempNode.setHoneyPot(0);
			}
			else 
			{
				int nodePointValue= r.nextInt(Parameters.MAX_POINT_VALUE - 1) + 1;
				tempNode.setPv(nodePointValue);
				tempNode.setHoneyPot(0);
				int randSecurity= r.nextInt(5 - 1) + 1;
				int maxSecurityValue = nodePointValue + randSecurity;
				if (maxSecurityValue > Parameters.MAX_POINT_VALUE)
					maxSecurityValue = Parameters.MAX_POINT_VALUE;
				int minSecurityValue = nodePointValue - randSecurity;
				if (minSecurityValue < 0)
					minSecurityValue = 0;
				int securityValue= r.nextInt(maxSecurityValue - minSecurityValue) + minSecurityValue;
				if(securityValue == 0)
					securityValue = 1;
				tempNode.setSv(securityValue);
			}
		}
		for(int i = 0; i < nodes.length; ++i)
			adjacencyMatrix[i][i] = 0;

		for (int i = 0; i < nodes.length; ++i)
		{
			Node tempNode = getNode(i);
			for(int j = 0; j < nodes.length; ++j)
			{
				if (adjacencyMatrix[i][j] == 1)
				{
					Node tempNeighbor = getNode(j);
					tempNode.addNeighbor(tempNeighbor);
				}
			}
		}
		for(int i=0; i<nodes.length; i++)
		{
			if(nodes[i].neighbor.size()==0)
			{
				//add some random neighbor
				int neighborcounter = 0;
				//System.out.println("Node "+ i +" has no neighbor");
				Random rand = new Random();
				while(true)
				{
					int nodeid = rand.nextInt(nodes.length-1);
					if(i!=nodeid)
					{

						if(neighborcounter==2)
						{
							break;
						}
						if(nodes[i].neighbor.size()==0)
						{
							nodes[i].neighbor.add(nodes[nodeid]);
							nodes[nodeid].neighbor.add(nodes[i]);
							neighborcounter++;
						}
						else if((nodes[i].neighbor.size()>0) && !(nodes[i].neighbor.contains(nodes[nodeid])))
						{
							nodes[i].neighbor.add(nodes[nodeid]);
							nodes[nodeid].neighbor.add(nodes[i]);
							neighborcounter++;
						}

					}
				}

			}
		}
	}

	/**
	 * Returns an array list of all the captured nodes
	 * @return all captured nodes in the network
	 */
	public ArrayList<Node> getCapturedNodes(){
		ArrayList<Node> capturedNodes = new ArrayList<Node>();
		for(int i = 0; i < nodes.length; i++)
			if(nodes[i].isCaptured())
				capturedNodes.add(nodes[i]);
		return capturedNodes;
	}
	

	/**
	 * Returns an array list of all the available nodes for attacking
	 * @return all non-captured available nodes in the network
	 */
	public ArrayList<Node> getAvailableNodes(){
		ArrayList<Node> availableNodes = new ArrayList<Node>();
		for(int i = 0; i < nodes.length; i++){
			for(int j = 0; nodes[i].isCaptured() && j < nodes[i].neighbor.size(); j++){
				Node neighbor = nodes[i].getNeighbor(j);
				if(neighbor != null && !neighbor.isCaptured() && !availableNodes.contains(neighbor))
					availableNodes.add(neighbor);
			}
		}
		return availableNodes;
	}
	
	
	/**
	 * @return String of contents of network
	 */
	public String toString() {
		String contents = "";
		for(int i = 0; i < nodes.length; i++) {
			Node nd = this.nodes[i];
			contents += "Node " + i + ": (PV: " + 
					nd.getPv() + ", SV: " + 
					nd.getSv() + ", DB: " + 
					nd.isDatabase() + ", HP: " + 
					nd.getHoneyPot() + ")\r\n";
			contents += "    Neighbors: ";
			int neighborCounter = 0;
			int neighborSize = nd.neighbor.size();
			for(Node neighbor: nd.neighbor){
				if(neighbor.getNodeID()!=nd.getNodeID()){
					if(neighborCounter==neighborSize-1)
						contents += neighbor.getNodeID();
					else 
						contents += neighbor.getNodeID() + ",";
				}
				neighborCounter++;
			}
			if(i < nodes.length - 1)
				contents += "\r\n";
		}
		return contents;
	}
}
