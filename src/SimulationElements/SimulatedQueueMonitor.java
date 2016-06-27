package SimulationElements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulatedQueueMonitor implements Simulation{
	public final static Logger logger	=	LoggerFactory.getLogger(SimulatedQueueMonitor.class);
	public int 			timesPerInterval	=	1;
	public double 		weight				=	0.3;
	private long 		nextEvent			=	-1;	
	public SimulationScheduler scheduler;
	private SimulatedEvalIntervalManager evalIntManager;
	
	public SimulatedQueueMonitor(int timesPerInterval,double weight, SimulatedEvalIntervalManager evalIntManager){
		super();
		this.timesPerInterval	=	timesPerInterval;
		this.weight				=	weight;
		this.evalIntManager		=	evalIntManager;
	}

	@Override
	public void advance(long t) {
		if(scheduler.simulatedTime>=t){
			/*SimulationMain.queueSize	=	((1-this.weight)*SimulationMain.queueSize)+(this.weight*SimulationMain.queue.size());
			int interval	=	(this.evalIntManager.getInterval())/this.timesPerInterval;
			nextEvent		=	t+interval;
			//logger.debug("Estimated queue size "+SimulationMain.queueSize+" instant value "+SimulationMain.queue.size());
			SimulationMain.testScheduler.insert(nextEvent);
			SimulationMain.queueDim.set(SimulationMain.queueSize);
			*/
		}
	}

}
