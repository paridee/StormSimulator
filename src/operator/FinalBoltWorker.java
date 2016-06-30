package operator;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import SimulationElements.IntervalManager;
import SimulationElements.SimulatedEvalIntervalManager;
import SimulationElements.Simulation;
import SimulationElements.SimulationScheduler;

public class FinalBoltWorker implements Simulation{
	public final static Logger logger	=	LoggerFactory.getLogger(FinalBoltWorker.class);
	long nextFree	=	0;
	HashMap<Integer,Long> exTimes	=	new HashMap<Integer,Long>();
	int id				=	0;	
	SimulationScheduler	sched;
	IntervalManager manager;
	FinalBolt	owner;
	boolean generate	=	false;
	public ArrayList<IntegerTuple>			queue		=	new ArrayList<IntegerTuple>();
	public SimulatedLatencyMonitor			latMonitor;
		
	public FinalBoltWorker(SimulationScheduler sched,IntervalManager manager,FinalBolt bolt,int id,SimulatedLatencyMonitor latMonitor) {
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
		this.latMonitor	=	latMonitor;
	}
	
	public FinalBoltWorker(SimulationScheduler sched,IntervalManager manager,FinalBolt bolt,int id){
		this(sched,manager,bolt,id,null);
	}

	@Override
	public void advance(long t) throws Exception {
		//flushes previous work
		if(t>=this.nextFree){
			if(this.generate==true){
				//TODO add in next bolt queue
				//System.out.println("RELEASE");
				this.generate	=	false;
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
						long simTime=exTimes.get(tuple.value);
						//logger.debug(owner.label+" level "+owner.level+" Final Worker "+this.id+" free, getting element from queue size "+this.queue.size()+" simulated time elapsed in elaboration "+(sched.simulatedTime-tuple.timestamp)+" ms, service time in this step "+simTime+" ms");						
						this.nextFree	=	simTime+sched.simulatedTime;
						//this.manager.evaluateRespTime(simTime);
						//logger.debug(owner.label+" "+"Worker "+this.id+" busy till "+nextFree);
						this.sched.insert(this.nextFree);
						if(this.latMonitor!=null){	//send latency to monitor
							int delta = (int)(nextFree-tuple.timestamp);
							//logger.debug("sending to monitor "+delta);
							this.latMonitor.addValue(delta);
						}
						else{
							logger.debug("MONITOR null");
						}
						generate	=	true;
					}
					else{
						//logger.debug(owner.label+" Worker "+this.id+" empty queue");
						this.sched.insert(this.sched.nextEvent());
					}
				}
				else{
					//logger.debug(owner.label+" "+"NO THREADS AVAILABLE");
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
