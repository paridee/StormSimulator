package operator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import SimulationElements.Simulation;
import SimulationElements.SimulationScheduler;

public class SimulatedLatencyMonitor implements Simulation {
	public final static Logger logger	=	LogManager.getLogger(SimulatedLatencyMonitor.class);
	
	double 	smoothing	=	0.3;
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

	public double getLatestStableValue(){
		return atLatestInterval;
	}
}