package operator;

import java.util.ArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import SimulationElements.IntervalManager;
import SimulationElements.SimulatedEvalIntervalManager;
import SimulationElements.Simulation;
import SimulationElements.SimulationScheduler;

public class FinalBolt extends Bolt {
	public final static Logger logger	=	LogManager.getLogger(FinalBolt.class);
	public ArrayList<ArrayList<IntegerTuple>>			queues		=	new ArrayList<ArrayList<IntegerTuple>>();
	public ArrayList<FinalBoltWorker>	workerList	=	new ArrayList<FinalBoltWorker>();
	public int								maxLevel	=	1;
	public String 							label;
	public int shuffleCounter				=	0;	
	
	public FinalBolt(int maxLevel,int level,SimulationScheduler sched,IntervalManager manager,ArrayList<Simulation> simList,String label,SimulatedLatencyMonitor latMon){
		super();
		this.maxLevel	=	maxLevel;
		this.level		=	level;
		for(int i=0;i<maxLevel;i++){
			FinalBoltWorker aWorker	=	new FinalBoltWorker(sched,manager,this,i,latMon);
			workerList.add(aWorker);
			simList.add(aWorker);
			queues.add(aWorker.queue);
		}
		this.label	=	label;
	}
	@Override
	public void addInQueue(Tuple value) {
		// TODO Auto-generated method stub
		if(value instanceof IntegerTuple){
			this.queues.get(shuffleCounter%level).add((IntegerTuple)value);
			shuffleCounter++;
			if(shuffleCounter%level==0){
				shuffleCounter	=	0;	//avoid int overflow
			}
		}
	}
	@Override
	public int getQueueSize() {
		int size	=	0;
		for(int i=0;i<this.queues.size();i++){
			size	=	size	+	queues.get(i).size();
		}
		return size;
	}
	@Override
	public void flushQueues() {
		for(int i=0;i<this.queues.size();i++){
			queues.get(i).clear();
		}
	}	
}
