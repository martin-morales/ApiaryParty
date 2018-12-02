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
			return new DefenderAction(false);
		}
}