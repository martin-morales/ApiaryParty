package apiaryparty;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Random;

/**
 * Pits Attacker and Defender agents against one another in the name of Science!
 * 
 * STUDENTS: add your defenders and attackers to the sections in main that say
 * "add defenders here" and "add attackers here" Also add your defender to the
 * method getDefenderByName() and your attacker to getAttackerByName() You may
 * also edit the rates in the Parameters class. Trust that these rates will be
 * changed when the full tournament is run.
 * 
 * @author Marcus Gutierrez, Oscar Veliz, Porag Chowdhury, Anjon Basak
 * @version 2017/11/14
 */
public class GameMaster {
	
	/**
	 * Runs the tournament
	 * 
	 * @param args not using any command line arguments
	 */
	public static void main(String[] args) {
		int numGames = 1;
		generateParameterValues();
		generateGraphs(numGames);

		// add Defenders here
		ArrayList<Defender> defenders = new ArrayList<Defender>();
		defenders.add(new WorkerBee("0"));
		defenders.add(new Honeycomb("0"));
		defenders.add(new QueenDBee("0"));
		// ADD YOUR STUDENT DEFENDER AGENTS HERE
		

		// get names of defenders
		String[] defenderNames = new String[defenders.size()];
		for (int i = 0; i < defenders.size(); i++)
			defenderNames[i] = defenders.get(i).getName();
		int numDefenders = defenderNames.length;
		// execute defenders
		for (int d = 0; d < numDefenders; d++) {
			for (int g = 0; g < numGames; g++) {
				Defender defender = getDefender(defenderNames[d], g + "");
				tryDefender(new DefenderDriver(PlayerState.INIT, defender));
				boolean execute = true;
				DefenderMonitor dm = new DefenderMonitor(defender);
				
				String defense = "";
				do{ //While defender has money and has not ended turn
					tryDefender(new DefenderDriver(PlayerState.MAKE_ACTION, defender));
					DefenderAction action = defender.getLastAction();
					
					if(action != null){
						dm.applyAction(action);
						if(action.getType() != DefenderActionType.END_TURN){
							tryDefender(new DefenderDriver(PlayerState.RESULT, defender, true));
							defense += action.toString();
						}
						else{//end turn
							execute = false;
						}
					}else{
						tryDefender(new DefenderDriver(PlayerState.RESULT, defender, false));
					}
					//System.out.println(defender.getBudget());
				}while(execute && defender.getBudget() > 0 );
				defender.endGame();
				
				new DefenderMonitor(defender.getName(), g+"",defense);
				//new DefenderMonitor(defender.getName(), defender.getGraph());
				dm.getNetwork().setName(defenderNames[d]+"-"+g);
				dm.getNetwork().printNetwork();
				dm.getNetwork().shuffleNetwork();//comment this out for testing
				dm.getNetwork().printHiddenNetwork();
			}
		}
		
		// Attacker List
		ArrayList<Attacker> attackers = new ArrayList<Attacker>();
		attackers.add(new GreenHornet());
		attackers.add(new BumbleBeeMan());
		attackers.add(new Beedrill());
		attackers.add(new YellowJacket());
		// ADD YOUR STUDENT ATTACKER AGENTS HERE
		attackers.add(new BeeGees());
		attackers.add(new BumbleGum());
		attackers.add(new Beeverly());
		

		// get names of attackers
		String[] attackerNames = new String[attackers.size()];
		for (int i = 0; i < attackers.size(); i++)
			attackerNames[i] = attackers.get(i).getName();
		int numAttackers = attackerNames.length;
		// initialize point matrix
		int[][] points = new int[numDefenders][numAttackers];
		
		int gameNum = 1;

		// execute attackers
		for (int d = 0; d < numDefenders; d++) {
			String defenderName = defenderNames[d];
			for (int a = 0; a < numAttackers; a++) {
				String attackerName = attackerNames[a];
				for (int g = 0; g < numGames; g++) {
					if(Parameters.VERBOSITY) {
						System.out.println("Game Number " + gameNum++);
						System.out.println(defenderName + " vs " + attackerName);
						System.out.println();
					}
					
					String graphName = g + "";
					AttackerMonitor am = new AttackerMonitor(attackerName,defenderName, graphName);
					Attacker attacker = getAttacker(defenderName,attackerName, graphName);
					tryAttacker(new AttackerDriver(PlayerState.INIT, attacker));
					while (am.getBudget() > 0) {
						tryAttacker(new AttackerDriver(PlayerState.MAKE_ACTION, attacker));
						if(attacker.getLastAction() == null)
							continue;
						Network visible = am.readMove(attacker.getLastAction());
						if(visible == null)
							continue;
						tryAttacker(new AttackerDriver(PlayerState.RESULT, attacker, visible));
						if(Parameters.VERBOSITY) {
							System.out.println("Budget after move: "+ am.getBudget());	// Out of sync
							System.out.println();
						}
					}
					am.close();
					points[d][a] += am.getPoints();
				}
			}
		}
		//Clean up created files
		if(Parameters.GRAPH_CLEANUP)
			Parser.clean_files(defenderNames, attackerNames, numGames);
		
		// perform analysis
		new Analyzer(points, attackerNames, defenderNames);
		
	}

	/**
	 * Generates graphs
	 * 
	 * @param numGraphs
	 *            the number of graphs to generate
	 */
	public static void generateGraphs(int numGraphs) {
		for (int i = 0; i < numGraphs; i++) {
			Network n = new Network(i);
			n.printNetwork();
			System.out.println(n.toString());
		}
	}

	/**
	 * You should edit this method to include your defender
	 * 
	 * @param name
	 *            name of defender
	 * @param file
	 *            graph defender will read
	 * @return your defender
	 */
	public static Defender getDefender(String defName, String file) {
		try {
			Class<?> defenderClass = Class.forName("apiaryparty." + defName);
			Constructor<?> defenderConstr = defenderClass.getConstructor(String.class);
			return (Defender) defenderConstr.newInstance(file);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// invalid defender if name could not be found
		return new Defender("", "") {
			public void initialize() {}
			public void actionResult(boolean actionSuccess) {}
			public DefenderAction makeAction() {
				return null;
			}
		};
	}

	/**
	 * You should edit this method to include your attacker
	 * 
	 * @param defName
	 *            name of defender attacker will be pit against
	 * @param atkName
	 *            name of attacker
	 * @param file
	 *            graph attacker will attack
	 * @return your attacker
	 */
	public static Attacker getAttacker(String defName, String atkName, String file) {
		try {
			Class<?> attackerClass = Class.forName("apiaryparty." + atkName);
			Constructor<?> attackerConstr = attackerClass.getConstructor(String.class, String.class);
			return (Attacker) attackerConstr.newInstance(defName, file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// in case your name was not added
		return new Attacker("", "", "") {
			protected void initialize() {}
			public AttackerAction makeAction() {
				return null;
			}
			protected void result(Node lastNode) {}
		};
		
	}
	
	/**
	 * Tries to execute a Defender's class' method by using threads a layer of protection in case
	 * the Defender subclasses crash or time out.
	 * 
	 * @param dDriver The thread that will ask the player to execute some code
	 */
	private static void tryDefender(DefenderDriver dDriver){
		int timeLimit;
		if(dDriver.state == PlayerState.INIT)
			timeLimit = Parameters.INIT_TIME;
		else if(dDriver.state == PlayerState.RESULT)
			timeLimit = Parameters.RESULT_TIME;
		else
			timeLimit = Parameters.ACTION_TIME;

		Thread playerThread = new Thread(dDriver);
		playerThread.start();
		for(int sleep = 0; sleep < timeLimit; sleep+=10){
			if(playerThread.isAlive())
				try {Thread.sleep(10);} catch (Exception e) {e.printStackTrace();}
			else
				return;
		}
	}
	
	/**
	 * Tries to execute an Attacker's class' method by using threads a layer of protection in case
	 * the Defender subclasses crash or time out.
	 * 
	 * @param aDriver The thread that will ask the player to execute some code
	 */
	private static void tryAttacker(AttackerDriver aDriver){
		int timeLimit;
		if(aDriver.state == PlayerState.INIT)
			timeLimit = Parameters.INIT_TIME;
		else if(aDriver.state == PlayerState.RESULT)
			timeLimit = Parameters.RESULT_TIME;
		else
			timeLimit = Parameters.ACTION_TIME;

		Thread playerThread = new Thread(aDriver);
		playerThread.start();
		for(int sleep = 0; sleep < timeLimit; sleep+=10){
			if(playerThread.isAlive())
				try {Thread.sleep(10);} catch (Exception e) {e.printStackTrace();}
			else
				return;
		}
	}
	
	private static void generateParameterValues() {
		Random r = new Random();

		System.out.println("---Parameters used---");
		
		do {
			Parameters.NUMBER_OF_NODES = r.nextInt(15) + 1;
			Parameters.NUMBER_OF_PUBLIC_NODES = r.nextInt(3) + 1;
			Parameters.NUMBER_OF_DATABASE_NODES = r.nextInt(3) + 1;
		} while(Parameters.NUMBER_OF_PUBLIC_NODES >= Parameters.NUMBER_OF_NODES &&
				Parameters.NUMBER_OF_DATABASE_NODES >= Parameters.NUMBER_OF_NODES - Parameters.NUMBER_OF_PUBLIC_NODES);
		System.out.println("number of nodes: "+ Parameters.NUMBER_OF_NODES);
		System.out.println("public nodes: "+ Parameters.NUMBER_OF_PUBLIC_NODES);
		System.out.println("DB nodes: "+ Parameters.NUMBER_OF_DATABASE_NODES);
		
		do {
			Parameters.MAX_NEIGHBORS = r.nextInt(5) + 2;
			Parameters.MIN_NEIGHBORS = r.nextInt(5) + 1;
		} while(Parameters.MAX_NEIGHBORS > Parameters.NUMBER_OF_NODES ||
				Parameters.MIN_NEIGHBORS >= Parameters.MAX_NEIGHBORS);
		System.out.println("max neighbors: "+ Parameters.MAX_NEIGHBORS);
		System.out.println("min neighbors: "+ Parameters.MIN_NEIGHBORS);
		
		Parameters.MAX_POINT_VALUE = r.nextInt(10) + 15;
		System.out.println("max point value: "+ Parameters.MAX_POINT_VALUE);
		
		Parameters.MAX_ROUTER_EDGES = r.nextInt(3) + 1;
		System.out.println("max router edges: "+ Parameters.MAX_ROUTER_EDGES);
		
		Parameters.DEFENDER_RATE = r.nextInt(8) + 8;
		System.out.println("defender rate: "+ Parameters.DEFENDER_RATE);
		
		Parameters.INVALID_RATE = r.nextInt(8) + 8;
		System.out.println("invalid rate: "+ Parameters.INVALID_RATE);
		
		
		
		do {
			Parameters.STRENGTHEN_RATE = r.nextInt(5) + 5;
			Parameters.FIREWALL_RATE = r.nextInt(5) + 6;
			Parameters.HONEYPOT_RATE = r.nextInt(10) + 10;
		} while(Parameters.FIREWALL_RATE < Parameters.STRENGTHEN_RATE &&
				Parameters.HONEYPOT_RATE < Parameters.FIREWALL_RATE);
		System.out.println("strengthen rate: "+ Parameters.STRENGTHEN_RATE);
		System.out.println("firewall rate: "+ Parameters.FIREWALL_RATE);
		System.out.println("honeypot rate: "+ Parameters.HONEYPOT_RATE);
		
		
		Parameters.ATTACKER_RATE = r.nextInt(8) + 8;
		System.out.println("attacker rate: "+ Parameters.ATTACKER_RATE);
		
		Parameters.ATTACK_ROLL = r.nextInt(10) + 15;
		System.out.println("attack roll: "+ Parameters.ATTACK_ROLL);
		
		Parameters.SUPERATTACK_ROLL = r.nextInt(30) + 35;
		System.out.println("super attack roll: "+ Parameters.SUPERATTACK_ROLL);
		
		do {
			Parameters.ATTACK_RATE = r.nextInt(10) + 5;
			Parameters.PROBE_POINTS_RATE = r.nextInt(5) + 1;
			Parameters.PROBE_HONEY_RATE = r.nextInt(5) + 1;
			Parameters.SUPERATTACK_RATE = r.nextInt(10) + 15;
		} while(Parameters.PROBE_HONEY_RATE > Parameters.PROBE_POINTS_RATE &&
				Parameters.SUPERATTACK_RATE <= Parameters.ATTACK_RATE &&
				Parameters.SUPERATTACK_RATE <= Parameters.PROBE_POINTS_RATE);
		
		System.out.println("attack rate: "+ Parameters.ATTACK_RATE);
		System.out.println("probe points rate: "+ Parameters.PROBE_POINTS_RATE);
		System.out.println("probe honey rate: "+ Parameters.PROBE_HONEY_RATE);
		System.out.println("super attack rate: "+ Parameters.SUPERATTACK_RATE);
		
		Parameters.HONEY_PENALTY = r.nextInt(8) + 8;
		System.out.println("honey penalty: "+ Parameters.HONEY_PENALTY);
	}
}
