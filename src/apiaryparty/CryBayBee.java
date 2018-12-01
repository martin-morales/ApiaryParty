package apiaryparty;

import java.util.Random;

/**
 * @author Martin Morales, Cynthia Valencia
 */
public class CryBayBee extends Defender{
		Random r;
	
	   public CryBayBee(String graphFile)
	    {
	        super("CryBayBee",graphFile);
	    }

	   @Override
		public void initialize() {
			r = new Random();
		}

		@Override
		public void actionResult(boolean actionSuccess) {
			
		}

		@Override
		public DefenderAction makeAction() {
			System.out.println("Budget: " + getBudget());

			while(getBudget() > Parameters.STRENGTHEN_RATE) {
				// Randomly call a Defender Action
				int randNum = r.nextInt(3);

				switch (randNum) {
					case 0: if(getBudget() > Parameters.STRENGTHEN_RATE){
								System.out.println("Making Strengthen move");
								return new DefenderAction(DefenderActionType.STRENGTHEN);
							}
							break;
					case 1: if(getBudget() > Parameters.FIREWALL_RATE) {
								System.out.println("Making Firewall move");
								return new DefenderAction(DefenderActionType.FIREWALL);
							}
							break;
					case 2: int honeyNode = r.nextInt(net.getAvailableNodes().size());
							int honeypotCost = honeypotCost(honeyNode);
							if(getBudget() > honeypotCost) {
								System.out.println("Making Honeypot move");
								return new DefenderAction(DefenderActionType.HONEYPOT);
							}
							break;
					default: break;
				}
			}

			return new DefenderAction(false);
		}
}