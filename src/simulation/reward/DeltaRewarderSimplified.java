package simulation.reward;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import operator.Bolt;
import operator.SimulatedLatencyMonitor;


public class DeltaRewarderSimplified implements RewardCalculator {
	private static final Logger logger = LoggerFactory.getLogger(DeltaRewarderSimplified.class);
	int distThreshold;
	int oldInstanceNumber;
	int upperBound;
	int lowerBound;
	ArrayList<Bolt> bolts;
	double oldDistance;
	int obj;
	SimulatedLatencyMonitor monitor;
	
	
	public int getInstances(ArrayList<Bolt> bolts){
		int total	=	0;
		for(int i=0;i<bolts.size();i++){
			total	=	bolts.get(i).level;
		}
		return total;
	}
	
	public DeltaRewarderSimplified(int distThreshold, int obj,int upperBound,ArrayList<Bolt> bolts,SimulatedLatencyMonitor mon) {
		super();
		this.distThreshold = distThreshold;
		this.obj = obj;
		this.bolts	=	bolts;
		oldInstanceNumber	=	this.getInstances(bolts);
		this.monitor		=	mon;
		this.upperBound		=	upperBound;
		lowerBound			=	obj-(upperBound-obj);
	}



	@Override
	public double giveReward() {
		double reward		=	0;
		double currentDist	=	this.monitor.getLevel()-obj;
		if(currentDist<0){
			currentDist	=	-currentDist;
		}
		if(currentDist<this.distThreshold){
			reward	=	reward+2;
		}
		logger.debug("distance delta "+(oldDistance-currentDist)+" positive means decreased");
		if(oldDistance-currentDist>this.distThreshold){
			
			reward	=	reward+1;
			logger.debug("Distance shortened, reward +1");
		}
		else if(oldDistance-currentDist<-this.distThreshold){
			reward	=	reward-1;
			logger.debug("Distance increased, reward -1");
		}
		this.oldDistance		=	currentDist;
		int machineDelta		=	this.oldInstanceNumber-this.getInstances(this.bolts);
		this.oldInstanceNumber	= 	this.getInstances(this.bolts);
		if(machineDelta>0){
			logger.debug("Instances number decreased, reward +0.5");
			reward	=	reward+0.5;
		}
		else if(machineDelta<0){
			logger.debug("Instances number increased, reward -0.5");
			reward	=	reward-0.5;
		}
		double currentLatency	=	this.monitor.getLevel();
		if(/*(currentLatency<this.lowerBound)||*/(currentLatency>this.upperBound)){
			logger.debug("Destination state overloaded, reward -0.5 (latency "+currentLatency+")");
			reward	=	reward - 0.5;
		}
		return reward;
	}

}
