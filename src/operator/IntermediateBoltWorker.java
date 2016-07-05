package operator;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulation.IntervalManager;
import simulation.SimulatedEvalIntervalManager;
import simulation.Simulation;
import simulation.SimulationMain;
import simulation.SimulationScheduler;
import simulation.ValueGenerator;

public class IntermediateBoltWorker implements Simulation {
	public final static Logger logger	=	LoggerFactory.getLogger(IntermediateBoltWorker.class);
	long nextFree	=	0;
	HashMap<Integer,Long> exTimes	=	new HashMap<Integer,Long>();
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
		exTimes.put(25, (long) 1);
		exTimes.put(26, (long) 1);
		exTimes.put(27, (long) 2);
		exTimes.put(28, (long) 3);
		exTimes.put(29, (long) 5);
		exTimes.put(30, (long) 8);
		exTimes.put(31, (long) 13);
		exTimes.put(32, (long) 21);
		exTimes.put(33, (long) 35);
		exTimes.put(34, (long) 57);
		exTimes.put(35, (long) 95);
		exTimes.put(36, (long) 156);
		exTimes.put(37, (long) 250);
		exTimes.put(38, (long) 391);
		exTimes.put(39, (long) 659);
		exTimes.put(40, (long) 1068);
		exTimes.put(41, (long) 1713);
		exTimes.put(42, (long) 2772);
		exTimes.put(43, (long) 4492);
		exTimes.put(44, (long) 7256);
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
						//System.out.println("tuple value "+tuple.value);
						long simTime=exTimes.get(tuple.value);
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
