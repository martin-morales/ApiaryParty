package apiaryparty;

import java.util.ArrayList;

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
		generateGraphs(numGames);

		// add Defenders here
		ArrayList<Defender> defenders = new ArrayList<Defender>();
		defenders.add(new WorkerBee("0"));
		defenders.add(new Honeycomb("0"));
		defenders.add(new QueenDBee("0"));

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
				new DefenderMonitor(defender.getName(), g+"",defense);
				//new DefenderMonitor(defender.getName(), defender.getGraph());
				dm.getNetwork().setName(defenderNames[d]+"-"+g);
				dm.getNetwork().printNetwork();
				dm.getNetwork().shuffleNetwork();//comment this out for testing
				dm.getNetwork().printHiddenNetwork();
			}
		}
		
		// add Attackers here
		ArrayList<Attacker> attackers = new ArrayList<Attacker>();
		attackers.add(new GreenHornet());
		attackers.add(new BumbleBeeMan());
		attackers.add(new Beedrill());
		attackers.add(new YellowJacket());

		// get names of attackers
		String[] attackerNames = new String[attackers.size()];
		for (int i = 0; i < attackers.size(); i++)
			attackerNames[i] = attackers.get(i).getName();
		int numAttackers = attackerNames.length;
		// initialize point matrix
		int[][] points = new int[numDefenders][numAttackers];

		// execute attackers
		for (int d = 0; d < numDefenders; d++) {
			String defenderName = defenderNames[d];
			for (int a = 0; a < numAttackers; a++) {
				String attackerName = attackerNames[a];
				for (int g = 0; g < numGames; g++) {
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
						System.out.println("Budget after move: "+ am.getBudget());
						System.out.println();
					}
					am.close();
					points[d][a] += am.getPoints();
				}
			}
		}
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
	public static Defender getDefender(String name, String file) {
		if (name.equalsIgnoreCase("WorkerBee"))
			return new WorkerBee(file);
		if (name.equalsIgnoreCase("Honeycomb"))
			return new Honeycomb(file);
		if (name.equalsIgnoreCase("QeenDBee"))
			return new QueenDBee(file);
		// add your defender

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
	 * @param atName
	 *            name of defender
	 * @param file
	 *            graph defender will attack
	 * @return your attacker
	 */
	public static Attacker getAttacker(String defName, String atName,
			String file) {
		if (atName.equalsIgnoreCase("GreenHornet"))
			return new GreenHornet(defName, file);
		if (atName.equalsIgnoreCase("BumbeBeeMan"))
			return new BumbleBeeMan(defName, file);
		if (atName.equalsIgnoreCase("Beedrill"))
			return new Beedrill(defName, file);
		if (atName.equalsIgnoreCase("YellowJacket"))
			return new YellowJacket(defName, file);

		// add your attacker here

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
}
