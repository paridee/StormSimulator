package simulation.policy;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulatedEpsilonGreedyChooser implements SimulatedPolicyChooser {
	public final static Logger logger	=	LoggerFactory.getLogger(SimulatedEpsilonGreedyChooser.class);
	
	double	epsilon	=	0.2;
	
	public SimulatedEpsilonGreedyChooser(double eps){
		epsilon	=	eps;
	}
	
	@Override
	public double[] policyForState(int stateId, double[][] q) {
		double[] 	policy		=	new double[q[stateId].length];
		int 		bestaction	=	this.getBestAction(stateId, q);
		policy[bestaction]		=	1-epsilon;
		for(int i=0;i<q[stateId].length;i++){
			if(i!=bestaction){
				policy[i]		=	epsilon/(q[stateId].length-1);
			}
		}
		return policy;
	}

	@Override
	public int actionForState(int currentState, double[][] q) {
		int bestAction	=	this.getBestAction(currentState, q);
		double rand		=	Math.random();		//TODO: change uniform random choosing in exploration
		if(rand<epsilon){
			//logger.debug("Exploration phase "+rand);
			int tempAction	=	bestAction;
			while(tempAction==bestAction){
				tempAction	=	(int) (Math.random()*q[currentState].length);
			}
			return tempAction;
		}
		else{
			//logger.debug("Exploitation phase "+rand);
			return bestAction;
		}
	}
	
	private int getBestAction(int currentState, double[][] q){
		int 	tempaction	=	0;
		double 	tempvalue	=	q[currentState][0];
		for(int i=1;i<q[currentState].length;i++){
			if(q[currentState][i]>tempvalue){
				tempaction	=	i;
				tempvalue	=	q[currentState][i];
			}
		}
		//check if is the only action with that value
		ArrayList<Integer> valueActions	=	new ArrayList<Integer>();
		for(int i=0;i<q[currentState].length;i++){
			if(q[currentState][i]==tempvalue){
				valueActions.add(i);
			}
		}
		if(valueActions.size()>1){
			logger.debug("not a single action with value "+tempvalue);
			int index	=	((int)(Math.random()*(valueActions.size()+10000)))%valueActions.size();
			return valueActions.get(index);
		}
		return tempaction;
	}

}
