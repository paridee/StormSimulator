package operator;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import SimulationElements.IntervalManager;
import SimulationElements.SimulatedEvalIntervalManager;
import SimulationElements.Simulation;
import SimulationElements.SimulationScheduler;
import SimulationElements.ValueGenerator;

public class IntermediateBolt extends Bolt{
	public final static Logger logger	=	LoggerFactory.getLogger(IntermediateBolt.class);
	public ArrayList<ArrayList<IntegerTuple>>			queues		=	new ArrayList<ArrayList<IntegerTuple>>();
	public ArrayList<IntermediateBoltWorker>	workerList	=	new ArrayList<IntermediateBoltWorker>();
	public int								maxLevel	=	1;
	public String 							label;
	public int shuffleCounter				=	0;	
	
	public IntermediateBolt(int maxLevel,int level,Bolt nextBolt,SimulationScheduler sched,IntervalManager manager,ArrayList<Simulation> simList,String label,ValueGenerator generator){
		super();
		this.maxLevel	=	maxLevel;
		this.level		=	level;
		for(int i=0;i<maxLevel;i++){
			IntermediateBoltWorker aWorker	=	new IntermediateBoltWorker(sched,manager,this,nextBolt,i,generator);
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
