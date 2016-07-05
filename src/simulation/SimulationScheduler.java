package simulation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import operator.IntermediateBolt;

public class SimulationScheduler {
	public final static Logger logger	=	LoggerFactory.getLogger(SimulationScheduler.class);
	public long simulatedTime	=	0;
	public long beginning		=	0;
	private List<Long>	events	=	new ArrayList<Long>();
	private int maxBusyThreads	=	1;
	private int busyThreads		=	0;
	
	
	public SimulationScheduler(int maxBusyThreads){
		super();
		simulatedTime		=	System.currentTimeMillis();
		beginning			=	simulatedTime;
		this.maxBusyThreads	=	maxBusyThreads;
	}
	
	public boolean areSlotAvailable(){
		if(this.busyThreads<maxBusyThreads){
			return true;
		}
		else{
			return false;
		}
	}
	
	public boolean setMaxBusyThreads(int lev){
		this.maxBusyThreads	=	lev;
		return true;
	}
	
	public boolean reserveSlot() throws Exception{
		if(busyThreads==maxBusyThreads){
			return false;
		}
		else if(busyThreads<maxBusyThreads){
			busyThreads++;
			return true;
		}
		else throw new Exception("wrong concurrency state exception");
	}
	
	public boolean releaseSlot() throws Exception{
		if(busyThreads<=0){
			throw new Exception("releasing an already empty slot bucket");
		}
		else{
			//logger.debug("releasing slot");
			busyThreads--;
			return true;
		}
	}
	
	public Long advance() throws Exception{
		//print();
		if(events.size()>0){
			Long temp	=	events.remove(0);
		//	System.out.println("Scheduler: advancing to "+temp);
			if(temp>=this.simulatedTime){
				this.simulatedTime	=	temp;
				//System.out.println("Scheduler: advanced to "+this.simulatedTime);
				return temp;
			}
			else throw new Exception("Wrong calendar");
		}
		else{
			this.simulatedTime	=	this.simulatedTime+1;
			//System.out.println("Scheduler: advanced to "+this.simulatedTime);
			return this.simulatedTime;
		}
	}
	
	public long nextEvent(){
		if(events.size()==0){
			return this.simulatedTime+1;
		}
		else return events.get(0);
	}
	
	public long getElapsedTime(){
		return	(this.simulatedTime-this.beginning);
	}

	public void insert(Long ev){
		if(!events.contains(ev)){
			events.add(ev);
			Collections.sort(events);
		}
	}
	
	public void print(){
		for(int i=0;i<events.size();i++){
			System.out.print(events.get(i)+" ");
		}
		System.out.print("\n#############################################\n");
	}
}
