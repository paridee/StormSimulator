package simulation.reward;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Jama.Matrix;
import operator.Bolt;
import operator.SimulatedLatencyMonitor;

public class ParabolicProcessTimeRewardCalculator implements RewardCalculator {
	/**
	 * This class gives a "parabolic" reward set by:
	 * -focus
	 * -left (X axis) intercept
	 * -right (X axis) intercept
	 * -maximum reward
	 */
	public final static Logger LOG	=	LoggerFactory.getLogger(ParabolicProcessTimeRewardCalculator.class);
	SimulatedLatencyMonitor simMon;
	double a			=	0;
	double b			=	0;
	double c			=	0;
	double maxReward	=	100;
	double focus;
	double xLIntercept	=	0;	//intercetta asse X sx
	double xRIntercept	=	0;	//intercetta alle X dx
	boolean zeroIfOutside	=	false;
	int nInstances;//(threads)
	ArrayList<Bolt> bolts;	
	
	public ParabolicProcessTimeRewardCalculator(double targetProcessTime, double maxReward,
			double xRIntercept,int nInstances,SimulatedLatencyMonitor simMon,ArrayList<Bolt> bolts,boolean zeroIfOutside) { //XL intercept is fixed on order to have maxv there!!!
			super();
			this.focus = targetProcessTime;
			this.maxReward = maxReward;
			this.xLIntercept 	= 	targetProcessTime-(xRIntercept-targetProcessTime);
			this.xRIntercept 	= 	xRIntercept;
			this.nInstances		=	nInstances;
			this.bolts			=	bolts;
			this.simMon			=	simMon;
			this.zeroIfOutside	=	zeroIfOutside;
		}

		@Override
		public double giveReward() {
			if(a==0){
				if(a==b){
					if(a==c){
						LOG.info("Parameters not initialized, calculation");
						this.calculateParameters();
					}
				}
			}
			double lat			=	this.simMon.getLatestStableValue();
			//LOG.debug("known latency "+simMon.getLatestStableValue()+" parameters a: "+a+" b: "+b+" c: "+c);
			double latSquared	=	lat*lat;
			double reward		=	(a*latSquared)+(b*lat)+c;
			reward				=	reward	-	((maxReward/(nInstances))*(double)this.getAllBoltsThread());
			/*if(singletons.SystemStatus.workerNumber!=this.prevInstanceNumber){
				LOG.info("switch number detected");
				reward			=	reward-(maxReward/4);
				this.prevInstanceNumber	=	singletons.SystemStatus.workerNumber;
			}*/
			//LOG.debug("reward returned "+reward);
			if(this.zeroIfOutside==true){
				if(reward<0){
					return 0;
				}
			}
			return reward;
		}

		/**
		 * This methods calculates parabolic cohefficients
		 */
		public void calculateParameters(){
			double xLSquared	=	Math.pow(xLIntercept,2);
			double xRSquared	=	Math.pow(xRIntercept,2);
			double focusSquared	=	Math.pow(focus,2);
			//Ax=B
		    double[][] vals 	= 	{{xLSquared,xLIntercept,1.},{focusSquared,focus,1.},{xRSquared,xRIntercept,1.}};
		    Matrix 		A 		= 	new Matrix(vals);
		    double[][] valsB	=	{{0.},{maxReward},{0.}};	
		    Matrix 		b 		= 	new Matrix(valsB);
		    Matrix 		x 		= 	A.solve(b);
		    System.out.println(x.getRowDimension()+"X"+x.getColumnDimension());
		    for(int i=0;i<x.getRowDimension();i++){
		    	for(int j=0;j<x.getColumnDimension();j++){
		    		LOG.info(x.get(i, j)+"");
		    	  }
		    }
		    this.a	=	x.get(0, 0);
		    this.b	=	x.get(1, 0);
		    this.c	=	x.get(2, 0);
		}
		
		private int getAllBoltsThread(){
			int res	=	0;
			for(int i=0;i<this.bolts.size();i++){
				res	=	res+this.bolts.get(i).level;
			}
			return res;
		}

}