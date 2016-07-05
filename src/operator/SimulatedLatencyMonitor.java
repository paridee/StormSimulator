package operator;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulation.Simulation;
import simulation.SimulationScheduler;

public class SimulatedLatencyMonitor implements Simulation {
	public final static Logger logger	=	LoggerFactory.getLogger(SimulatedLatencyMonitor.class);
	
	double 	smoothing	=	0.1;
	double 	average		=	0;
	int 	interval;
	long	nextIntervalEnd;
	double	atLatestInterval=0;
	
	public SimulatedLatencyMonitor(double smoothing, int interval){
		super();
		this.smoothing	=	smoothing;
		this.interval	=	interval;
	}
	
	public void addValue(int latency){
		//logger.debug("inserting value "+(double)latency/1000+" sec.");
		average	=	(smoothing*(double)latency)+((1-smoothing)*average);
	}
	
	@Override
	public void advance(long t) throws Exception {
		if(t>=this.nextIntervalEnd){
			this.atLatestInterval	=	average;
			this.nextIntervalEnd	=	t+interval;
			//logger.debug("average latency: "+average/1000+" sec.");
		}
	}

	public double getLevel(){
		return this.average;
	}
	public double getLatestStableValue(){
		return atLatestInterval;
	}
}
