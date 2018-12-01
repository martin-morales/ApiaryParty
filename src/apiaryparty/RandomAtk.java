package apiaryparty;

import java.util.Random;

/**
 * Strategy: Make completely random attacks
 * @author Martin Morales, Cynthia Valencia
 */
public class RandomAtk extends Attacker {

    private final static String attackerName = "RandomAtk";
    
    public Random r;

    /**
     * Constructor
     * @param defenderName defender's name
     * @param graphFile graph to read
     */
	public RandomAtk(String defenderName, String graphFile) {
		super(attackerName, defenderName, graphFile);
	}
	/**
	 * Default constructor do not change
	 */
	public RandomAtk(){
		super(attackerName);
	}
	
	/**
	 * If you need to initialize anything, do it  here
	 */
	protected void initialize(){
		r = new Random();
	}

	/**
	 * This is called by the game master while your agent still has funds. You return an action.
	 * @return your action
	 */
	public AttackerAction makeAction() {
		System.out.println("Budget: " + budget);

		while(budget > Parameters.PROBE_HONEY_RATE) {
			// Randomly call a Defender Action
			int randNum = r.nextInt(4);
			int nodeID = r.nextInt(availableNodes.size());

			switch (randNum) {
				case 0: if(budget > Parameters.ATTACK_RATE){
							System.out.println("Making attack move");
							return new AttackerAction(AttackerActionType.ATTACK, nodeID);
						}
						break;
				case 1: if(budget > Parameters.SUPERATTACK_RATE) {
							System.out.println("Making super attack move");
							return new AttackerAction(AttackerActionType.SUPERATTACK, nodeID);
						}
						break;
				case 2: if(budget > Parameters.PROBE_POINTS_RATE) {
							System.out.println("Making Probe points move");
							return new AttackerAction(AttackerActionType.PROBE_POINTS, nodeID);
						}
						break;
				case 3: if(budget > Parameters.PROBE_HONEY_RATE) {
							System.out.println("Making Probe Honey move");
							return new AttackerAction(AttackerActionType.PROBE_HONEYPOT, nodeID);
						}
						break;
				default: break;
			}
		}
		return new AttackerAction(AttackerActionType.END_TURN, -1);
	}

	/**
	 * The game master is giving you the result of the action.
	 * @param  lastNode the node successfully attacked
	 */
	protected void result(Node lastNode) {
		
	}
}