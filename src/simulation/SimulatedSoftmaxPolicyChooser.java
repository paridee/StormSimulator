package simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulation.policy.SimulatedPolicyChooser;

public class SimulatedSoftmaxPolicyChooser implements SimulatedPolicyChooser {
	
	double e			=	2.71828;
	double temperature	=	1;
	public final static Logger logger	=	LoggerFactory.getLogger(SimulatedSoftmaxPolicyChooser.class);
	
	public SimulatedSoftmaxPolicyChooser(double temp){
		super();
		this.temperature	=	temp;
	}
	
	@Override
	public double[] policyForState(int stateId, double[][] q) {
		double den		=	0;
		double[] result	=	new double[q[stateId].length];
		for(int i=0;i<q[stateId].length;i++){
			den	=	den+(Math.pow(e, (q[stateId][i]/temperature)));
		}
		for(int i=0;i<q[stateId].length;i++){
			result[i]	=	Math.pow(e, (q[stateId][i]/temperature))/den;
			//logger.debug("p("+i+")="+result[i]);
		}
		return result;
	}

	@Override
	public int actionForState(int currentState, double[][] q) {
		double[] policy				=	this.policyForState(currentState, q);
		double[] cumulative			=	new double[policy.length];
		cumulative[0]				=	policy[0];
		cumulative[policy.length-1]	=	1;
		for(int i=1;i<policy.length-1;i++){
			cumulative[i]	=	cumulative[i-1]+policy[i];
		}
		
		for(int i=0;i<policy.length;i++){
			logger.debug("X("+i+")="+cumulative[i]);
		}
		
		
		double rand	=	Math.random();
		logger.debug("Random value "+rand);
		for(int i=0;i<policy.length;i++){
			if(rand<=cumulative[i]){
				return i;
			}
		}
		return policy.length-1;
	}

}
