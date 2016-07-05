package simulation.executor;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import operator.Bolt;
import operator.FinalBolt;

public class DynamicExecutorIncreaserDecreaser implements SimulatedActionExecutor {
	public final static Logger logger	=	LoggerFactory.getLogger(ExecutorIncreaserDecreaser.class);
	ArrayList<Bolt> bolts;
	int maxExecutorNumber;
	int[]	steps;
	
	public DynamicExecutorIncreaserDecreaser(ArrayList<Bolt> bolts,int maxN,int[]	steps){
		super();
		this.bolts				=	bolts;
		this.maxExecutorNumber	=	maxN;
		this.steps				=	steps;
	}
	
	/*
	 * actions decrease increase (each per bolt) plus another one (don't do anything)
	 */
	@Override
	public void execute(int action) throws Exception {
		throw new Exception("This executor requires state knowledge");
	}
	
	public int getActiveInstances(){
		int res	=	0;
		for(int i=0;i<bolts.size();i++){
			res	=	res	+	bolts.get(i).level;
		}
		return res;
	}

	public void flushQueues(){
		for(int i=0;i<bolts.size();i++){
			bolts.get(i).flushQueues();
		}
	}

	@Override
	public void execute(int action, int state) {
		int boltN	=	action/2;
		int actionF	=	action-(2*boltN);
		int increaseValue	=	steps[state];
		if(boltN<bolts.size()){
			if(actionF==0){
				if(bolts.get(boltN).level>1){
					int tempValue	=	bolts.get(boltN).level-increaseValue;
					if(tempValue<1){
						bolts.get(boltN).level	=	1;
					}
					else{
						bolts.get(boltN).level	=	tempValue;
					}
				}
				//logger.debug("action choosen: "+action+" decrease level for bolt "+boltN+" to "+bolts.get(boltN).level);
				this.flushQueues();
			}
			else if(actionF==1){
				if(bolts.get(boltN).level<this.maxExecutorNumber){
					int tempValue	=	bolts.get(boltN).level+increaseValue;
					if(tempValue>this.maxExecutorNumber){
						bolts.get(boltN).level	=	this.maxExecutorNumber;
					}
					else{
						bolts.get(boltN).level	=	tempValue;
					}
				}
				//logger.debug("action choosen: "+action+" increase level for bolt "+boltN+" to "+bolts.get(boltN).level);
				this.flushQueues();
			}
		}
		else{
			//logger.debug("action choosen: "+action+" leave unchanged");
		}
	}
}
