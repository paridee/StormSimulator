package simulation.reward;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class SimulatedQueueLengthRewarder implements RewardCalculator {

	@Override
	public double giveReward() {
		// TODO Auto-generated method stub
		return 0;
	}
/*OBSOLETE CLASS
	//keeps 5 elements in queue, parabolic
	PrintWriter out	 ;
	double a	=	-((double)4/9);
	double b	=	((double)80)/3;
	double c	=	-((double)300)/1;//-((double)525);
	
	public SimulatedQueueLengthRewarder(){
		super();
		try {
			out	=	new PrintWriter("reward.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public double giveReward() {
		int x			=	SimulationMain.queue.size();
		double firstpart	=	a*x*x;
		double secondpart	=	b*x;
		double reward	=	firstpart+secondpart+c;
		//System.out.println("Reward calculated for queue size "+x+": "+reward+"("+firstpart+"+"+secondpart+",a="+a+" b="+b+")");
		out.println(reward);
		out.flush();
		SimulationMain.rewardVal.set(reward);
		return reward;
	}*/

}
