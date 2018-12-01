package apiaryparty;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * Used for reading Network graphs from files
 */
public class Parser
{
	
	/**
	 * Given a .graph file, a network is generated following a predetermined format
	 * 
	 * @param filename the file name
	 * @return a Network object based on the .graph file given
	 */
	public static Network parseGraph(String filename)
	{
		boolean deleteJunkFile = false;
		try 
		{
			File gFile = new File(filename);
			if(!gFile.exists()){
				gFile.createNewFile();
				deleteJunkFile = true;
			}
			CSVParser parser = CSVParser.parse(gFile, StandardCharsets.US_ASCII, CSVFormat.DEFAULT);
			CSVParser parseRecords= CSVParser.parse(gFile, StandardCharsets.US_ASCII, CSVFormat.DEFAULT);
			int neighborsCounter = 0;
			int numNodes = parseRecords.getRecords().size()/2;
            Network network = new Network(0,numNodes);
            network.setName(filename.substring(0,filename.indexOf(".")));
			boolean flag = false;
			for (CSVRecord csvRecord : parser) 
			{
				Iterator<String> itr = csvRecord.iterator();
				if((neighborsCounter<numNodes) && flag==false)
				{
					Node node = network.getNode(neighborsCounter);
					while(itr.hasNext())
					{
						int x = Integer.parseInt(itr.next());
						if(x >= 0){
							Node neighbor = network.getNode(x);
							node.addNeighbor(neighbor);
						}
					}
					if(neighborsCounter==numNodes-1)
					{
						flag = true;
						neighborsCounter=0;
					}
					else
						neighborsCounter++;
				}
				else if(flag && (neighborsCounter<numNodes))
				{
					Node node = network.getNode(neighborsCounter);
					while(itr.hasNext())
					{
						int x  = Integer.parseInt(itr.next());
						node.setPv(x);
						int y = Integer.parseInt(itr.next());
						node.setSv(y);
						boolean db = Boolean.parseBoolean(itr.next());
						node.setDB(db);
						int z = Integer.parseInt(itr.next());
						node.setHoneyPot(z);
					}
					neighborsCounter++;
				}
			}
			if(deleteJunkFile)
				gFile.delete();
			parser.close();
			parseRecords.close();
			return network;
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
    /**
	 * Parses attacker's .history file and calculates and returns the attacker's network
	 * @param attackerName the attacker name
	 * @param defenderName the defender name
	 * @param graphName the graph name
	 * @return attacker's visible graph
	 */
	public static Network parseAttackerHistory(String attackerName, String defenderName, String graphName)
	{
		Network hidden = parseGraph(defenderName + "-" + graphName + "-hidden.graph");
		String historyFile = attackerName + "-" + defenderName + "-" + graphName + ".history";
		File csvTrainData = new File(historyFile);
		try
		{
			boolean deleteJunkFile = false;
			File hFile = new File(historyFile);
			if(!hFile.exists()){
				hFile.createNewFile();
				deleteJunkFile = true;
			}
			CSVParser parser = CSVParser.parse(csvTrainData, StandardCharsets.US_ASCII, CSVFormat.DEFAULT);
			for (CSVRecord csvRecord : parser)
			{
				Iterator<String> itr = csvRecord.iterator();
				int move = Integer.parseInt(itr.next());
				int id;
				Node node;
				boolean attackSuccess;
				int roll;
				int sv;
				int pv;
				boolean isDB;
				int hp;
				switch(move){
				case 0: //attack
					//budget -= Parameters.ATTACK_RATE;
					id = Integer.parseInt(itr.next());
					node = hidden.getNode(id);
					attackSuccess = Boolean.parseBoolean(itr.next());
					roll = Integer.parseInt(itr.next());
					if(roll > node.getBestRoll())
						node.setBestRoll(roll);
					if(attackSuccess){
						pv = Integer.parseInt(itr.next());
						node.setPv(pv);
						sv = Integer.parseInt(itr.next());
						node.setSv(sv);
						isDB = Boolean.parseBoolean(itr.next());
						node.setDB(isDB);
						hp = Integer.parseInt(itr.next());
						node.setHoneyPot(hp);
						node.setNeighborAmount(0);
						node.setCaptured(true);
						if(hp != 1){
							while(itr.hasNext())
								node.addNeighbor(hidden.getNode(Integer.parseInt(itr.next())));
						}
					}
					break;
				case 1: //superattack
					//budget -= Parameters.SUPERATTACK_RATE;
					id = Integer.parseInt(itr.next());
					node = hidden.getNode(id);
					attackSuccess = Boolean.parseBoolean(itr.next());
					roll = Integer.parseInt(itr.next());
					if(roll > node.getBestRoll())
						node.setBestRoll(roll);
					if(attackSuccess){
						pv = Integer.parseInt(itr.next());
						node.setPv(pv);
						sv = Integer.parseInt(itr.next());
						node.setSv(sv);
						isDB = Boolean.parseBoolean(itr.next());
						node.setDB(isDB);
						hp = Integer.parseInt(itr.next());
						//System.out.println("(" + pv + "," + sv + "," + isDB + "," + hp + ")");
						node.setHoneyPot(hp);
						node.setNeighborAmount(0);
						node.setCaptured(true);
						if(hp != 1){
							while(itr.hasNext())
								node.addNeighbor(hidden.getNode(Integer.parseInt(itr.next())));
						}
					}
					break;
				case 2: //probe values
					//budget -= Parameters.PROBE_SECURITY_RATE;
					id = Integer.parseInt(itr.next());
					node = hidden.getNode(id);
					pv = Integer.parseInt(itr.next());
					node.setPv(pv);
					sv = Integer.parseInt(itr.next());
					node.setSv(sv);
					break;
				case 3: //probe honeypot
					//budget -= Parameters.PROBE_HONEY_RATE;
					id = Integer.parseInt(itr.next());
					node = hidden.getNode(id);
					hp = Integer.parseInt(itr.next());
					node.setHoneyPot(hp);
					break;
				case 6: //public node
					id = Integer.parseInt(itr.next());
					node = hidden.getNode(id);
					node.setCaptured(true);
					node.setNeighborAmount(0);
					//while(itr.hasNext())
					//	node.addNeighbor(hidden.getNode(Integer.parseInt(itr.next())));
					break;
				case -1: default:
					//budget -= Parameters.INVALID_RATE;
					break;
				}
			}
			if(deleteJunkFile)
				hFile.delete();
			parser.close();
			return hidden;
		}
		catch(NumberFormatException nfe){ nfe.printStackTrace(); }
		catch (IOException e) { e.printStackTrace();}
		catch (Exception e) { e.printStackTrace(); }
		return hidden;
	}
	
	/**
	 * Parses attacker's .history file and calculates and returns the attacker's budget
	 * 
	 * @param attackerName the attacker name
	 * @param defenderName the defender name
	 * @param graphName the graph name
	 * @return budget i.e. integer value
	 */
	public static int parseAttackerBudget(String attackerName, String defenderName, String graphName){;
		String historyFile = attackerName + "-" + defenderName + "-" + graphName + ".history";
		int budget = Parameters.ATTACKER_BUDGET;
		try
		{
			File csvTrainData = new File(historyFile);
			CSVParser parser = CSVParser.parse(csvTrainData, StandardCharsets.US_ASCII, CSVFormat.DEFAULT);
			for (CSVRecord csvRecord : parser)
			{
				Iterator<String> itr = csvRecord.iterator();
				int move = Integer.parseInt(itr.next());
				
				switch(move){
				case 0: //attack
					budget -= Parameters.ATTACK_RATE;
					break;
				case 1: //superattack
					budget -= Parameters.SUPERATTACK_RATE;
					break;
				case 2: //probe security value
					budget -= Parameters.PROBE_POINTS_RATE;
					break;
				case 3: //probe honeypot
					budget -= Parameters.PROBE_HONEY_RATE;
					break;
				case -1: //public node
					budget -= Parameters.INVALID_RATE;
					break;
				case 6: default:
					break;
				}
			}
			parser.close();
			return budget;
		}
		catch(NumberFormatException nfe){ nfe.printStackTrace(); }
		catch (IOException e) { e.printStackTrace();}
		return budget;
	}
	
	
	public static void clean_files(String[] defNames, String[] atkNames, int numGames) {
		for(int game = 0; game < numGames; game++) {
			File baseFile = new File(game + ".graph");
			baseFile.delete();
			
			for(String defender : defNames) {
				boolean errorOnDelete = false;
				
				File graphFile = new File(defender + "-" + game + ".graph");
				if(graphFile.delete())
					errorOnDelete = true;
				
				File defenseFile = new File(defender + "-" + game + ".defense");
				if(defenseFile.delete())
					errorOnDelete = true;
				
				File hiddenFile = new File(defender + "-" + game + "-hidden.graph");
				if(hiddenFile.delete())
					errorOnDelete = true;
				
				for(String attacker : atkNames) {
					File historyFile = new File(attacker + "-" + defender + "-" + game + ".history");
					if(historyFile.delete())
						errorOnDelete = true;
				}
			}
		}
	}
}