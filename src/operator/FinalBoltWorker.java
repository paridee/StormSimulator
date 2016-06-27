package operator;

import java.util.ArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import SimulationElements.IntervalManager;
import SimulationElements.SimulatedEvalIntervalManager;
import SimulationElements.Simulation;
import SimulationElements.SimulationScheduler;

public class FinalBoltWorker implements Simulation{
	public final static Logger logger	=	LogManager.getLogger(FinalBoltWorker.class);
	long nextFree	=	0;
	long exTimes[]	=	new long[10];	//take execution times in a bucket of 10
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
						int index	=	tuple.value-35;
						long simTime=exTimes[index];
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
