package simulation.state;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import operator.FinalBoltWorker;
import operator.SimulatedLatencyMonitor;

public class LatencyStateReader implements SimulatedStateReader {
	public final static Logger logger	=	LoggerFactory.getLogger(LatencyStateReader.class);
	SimulatedLatencyMonitor monitor;
	int lowerBoundNormal;
	int upperBoundNormal;
	public LatencyStateReader(SimulatedLatencyMonitor monitor,int lowerBoundNormal,int upperBoundNormal){
		super();
		this.monitor			=	monitor;
		this.lowerBoundNormal	=	lowerBoundNormal;
		this.upperBoundNormal	=	upperBoundNormal;
	}
	
	@Override
	public int getCurrentState() {
		double currentValue	=	monitor.getLatestStableValue();
		if(currentValue<this.lowerBoundNormal){
			//logger.debug("system underloaded");
			return 0;
		}
		if(currentValue>this.upperBoundNormal){
			//logger.debug("system overloaded");
			return 2;
		}
		//logger.debug("system load OK");
		return 1;
	}

}
