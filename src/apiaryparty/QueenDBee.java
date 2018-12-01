package apiaryparty;

import java.util.Random;

/**
 * Benchmark Defender that only adds database honeypots
 * @author Oscar Veliz
 */


public class QueenDBee extends Defender{
		Random r;
	
	   public QueenDBee(String graphFile)
	   {
	        super("QueenDBee",graphFile);
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
			Random r = new Random();
			int honeyNode = r.nextInt(net.getAvailableNodes().size());
			int honeypotCost = honeypotCost(honeyNode);
			if(getBudget() < honeypotCost){
				return new DefenderAction(DefenderActionType.INVALID);
			}
			return new DefenderAction(DefenderActionType.HONEYPOT, honeyNode);
			
		}
}
