package operator;

import java.util.ArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import SimulationElements.IntervalManager;
import SimulationElements.SimulatedEvalIntervalManager;
import SimulationElements.Simulation;
import SimulationElements.SimulationMain;
import SimulationElements.SimulationScheduler;
import SimulationElements.ValueGenerator;

public class IntermediateBoltWorker implements Simulation {
	public final static Logger logger	=	LogManager.getLogger(IntermediateBoltWorker.class);
	long nextFree	=	0;
	long exTimes[]	=	new long[10];	//take execution times in a bucket of 10
	int id				=	0;	
	SimulationScheduler	sched;
	IntervalManager manager;
	IntermediateBolt	owner;
	boolean generate	=	false;
	Bolt nextBolt;
	public ArrayList<IntegerTuple>			queue		=	new ArrayList<IntegerTuple>();
	ValueGenerator generator;
	long currentTupleTimestamp;
		
	public IntermediateBoltWorker(SimulationScheduler sched,IntervalManager manager,IntermediateBolt bolt,Bolt nextBolt,int id,ValueGenerator generator) {
		super();
		this.sched = sched;
		exTimes[0]	=	96;
		exTimes[1]	=	153;
		exTimes[2]	=	248;
		exTimes[3]	=	397;
		exTimes[4]	=	649;
		exTimes[5]	=	1045;
		exTimes[6]	=	1694;
		exTimes[7]	=	2764;
		exTimes[8]	=	4858;
		exTimes[9]	=	7619;
		this.id		=	id;
		this.manager=	manager;
		this.owner	=	bolt;
		this.nextBolt	=	nextBolt;
		this.generator	=	generator;
	}

	@Override
	public void advance(long t) throws Exception {
		//System.out.println("Intermediate worker id "+id+" advancing to "+t+" free at "+this.nextFree+" concurrency level "+owner.level);
		//flushes previous work
		if(t>=this.nextFree){
			if(this.generate==true){
				//TODO add in next bolt queue
				//logger.debug(owner.label+" RELEASE elapsed time "+(this.sched.simulatedTime-this.currentTupleTimestamp)+" ms");
				this.generate	=	false;
				IntegerTuple tuple	=	new IntegerTuple(this.currentTupleTimestamp,this.generator.generate());
				nextBolt.addInQueue(tuple);
				sched.releaseSlot();
				
			}
		}
		if(this.id<owner.level){
			if(t>=this.nextFree){
				if(sched.areSlotAvailable()==true){				
					//TODO remove test
					boolean res	=	this.id<owner.level;
					if(this.queue.size()>0){
						boolean check	=	sched.reserveSlot();
						if(check==false){
							throw new Exception("Fatal error 1");
						}
						IntegerTuple tuple	=	this.queue.remove(0);
						//logger.debug(owner.label+" Worker "+this.id+" free, getting element from queue elapsed "+(this.sched.simulatedTime-tuple.timestamp)+" from tuple generation, time elapsed "+sched.getElapsedTime());
						int index	=	tuple.value-35;
						long simTime=exTimes[index];
						this.nextFree	=	simTime+sched.simulatedTime;
						//this.manager.evaluateRespTime(simTime);
						//logger.debug(owner.label+" Worker "+this.id+" busy till "+nextFree);
						this.sched.insert(this.nextFree);
						this.currentTupleTimestamp	=	tuple.timestamp;
						generate	=	true;
					}
					else{
						//logger.debug(owner.label+" Worker "+this.id+" empty queue");
						this.sched.insert(this.sched.nextEvent());
					}
				}
				else{
					//logger.debug(owner.label+" NO THREADS AVAILABLE");
					this.sched.insert(sched.nextEvent());
				}
			}
			else{
				//System.out.println("Worker "+this.id+" busy");
			}
		}
		else{
			//System.out.println("Worker "+this.id+" disabled from actual concurrency level "+SimulationMain.concurrencyLevel);
		}
	}

}
