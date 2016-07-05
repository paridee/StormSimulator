package simulation.algorithms;

import simulation.SimulatedAlphaCalculator;
import simulation.SimulatedEvalIntervalManager;
import simulation.SimulatedFeaturesEvaluator;
import simulation.Simulation;
import simulation.SimulationScheduler;
import simulation.executor.SimulatedActionExecutor;
import simulation.reward.RewardCalculator;
import simulation.state.SimulatedStateReader;

public class SimulatedLinearGradientDescendSarsaLambda implements Simulation {
	int simulatedStep	=	0;
	long nextStepTime	=	0;
	int 	featuresN		=	12;
	double 	epsilon			=	0.1;
	double  yota			=	0.9;
	double  lambda			=	0.5;
	SimulatedStateReader 		reader;
	SimulatedFeaturesEvaluator 	eval;
	SimulatedActionExecutor		executor;
	SimulatedAlphaCalculator		alphaCalculator;
	private RewardCalculator rewardCalculator;
	int actions				=	4;
	int initAction;
	double[] 	eVector			=	new double[featuresN];
	double[] 	omega			=	new double[featuresN];
	int 		currentState;
	int 		action;
	int[] features;
	SimulationScheduler			sched;
	SimulatedEvalIntervalManager intManager;
	
	public SimulatedLinearGradientDescendSarsaLambda(int featuresN, double epsilon, double yota, double lambda,
			SimulatedStateReader reader, SimulatedFeaturesEvaluator eval, SimulatedActionExecutor executor, SimulatedAlphaCalculator alphaCalculator,RewardCalculator rewardCalculator,
			int actions,int initAction,SimulationScheduler sched,SimulatedEvalIntervalManager intManager) {
		super();
		this.featuresN 			= 	featuresN;
		this.epsilon 			=	epsilon;
		this.yota 				= 	yota;
		this.lambda 			= 	lambda;
		this.reader 			= 	reader;
		this.eval 				= 	eval;
		this.executor 			= 	executor;
		this.alphaCalculator 	= 	alphaCalculator;
		this.actions 			= 	actions;
		this.initAction			=	initAction;
		this.rewardCalculator	=	rewardCalculator;
		this.sched				=	sched;
		this.intManager			=	intManager;
		action			=	this.initAction;
	}

	public SimulatedLinearGradientDescendSarsaLambda(SimulatedStateReader reader, SimulatedFeaturesEvaluator eval, SimulatedActionExecutor executor,
			SimulatedAlphaCalculator alphaCalculator,RewardCalculator rewardCalculator,int initAction,SimulationScheduler sched,SimulatedEvalIntervalManager intManager) {
		super();
		this.reader 			= 	reader;
		this.eval 				= 	eval;
		this.executor 			= 	executor;
		this.alphaCalculator 	= 	alphaCalculator;
		this.initAction			=	initAction;
		this.rewardCalculator	=	rewardCalculator;
		this.sched				=	sched;
		this.intManager			=	intManager;
		action			=	this.initAction;
	}

	public void partOne() {
		// TODO Auto-generated method stub
		currentState	=	reader.getCurrentState();	//read state
			features		=	eval.getFeatures(currentState, action);
			for(int i=0;i<features.length;i++){
				if(features[i]==1){
					eVector[i]	=	1;
				}
				System.out.print(features[i]+"\t");
			}
			System.out.println("\n");
			try {
				executor.execute(action,currentState);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.nextStepTime	=	sched.simulatedTime+this.intManager.getInterval();
			this.sched.insert(nextStepTime);
			this.simulatedStep++;
	}
	void partTwo(){
			double reward	=	this.rewardCalculator.giveReward();
			currentState	=	reader.getCurrentState();
			double delta	=	reward;
			for(int i=0;i<features.length;i++){
				if(features[i]==1){
					delta		=	delta	+	omega[i];	
				}
			}
			double randomV			=	Math.random();
			double qActionChoosen	=	0;
			if(randomV>epsilon){
				//exploitation
				double Q[]	=	new double[actions];
				for(int i=0;i<actions;i++){
					features	=	eval.getFeatures(currentState, i);
					for(int j=0;j<features.length;j++){
						Q[i]	=	Q[i]	+	(omega[j]*features[j]);
					}
				}
				
				//testing
				for(int i=0;i<actions;i++){
					System.out.println("Q["+currentState+"]["+i+"] "+Q[i]+"\t");
				}
				
				
				//find best action
				int newAction	=	0;
				
				System.out.println("number of actions "+actions);
				
				double qAction	=	Q[0];
				for(int j=1;j<actions;j++){
					if(Q[j]>qAction){
						newAction	=	j;
						qAction		=	Q[j];
					}
				}
				action			=	newAction;
				qActionChoosen	=	qAction;
				System.out.println("Exploiation:Current state: "+currentState+" action "+action);
				//best action found
			}
			else{
				//exploration
				action			=	(int)((Math.random()*actions)%actions);
				features		=	eval.getFeatures(currentState, action);
				double tempQ	=	0;
				for(int j=0;j<features.length;j++){
					tempQ	=	tempQ	+	(omega[j]*features[j]);
				}
				qActionChoosen	=	tempQ;
				
				//testing
				System.out.println("Exploration:Current state: "+currentState+" action "+action);
				System.out.println("Q["+currentState+"]["+action+"] "+qActionChoosen+"\t");
				
			}
			delta	=	delta	+	(yota*qActionChoosen);
			for(int i=0;i<featuresN;i++){
				omega[i]	=	omega[i]+(alphaCalculator.getAlpha()*delta*eVector[i]);
				eVector[i]	=	yota*lambda*eVector[i];
			}
			
			System.out.println("Omega vector:");
			for(int i=0;i<featuresN;i++){
				System.out.print(omega[i]+"\t");
			}
			System.out.println("\nTrace vector:");
			for(int i=0;i<featuresN;i++){
				System.out.print(eVector[i]+"\t");
			}		
			
			
			this.simulatedStep	=	0;
			this.sched.insert(sched.simulatedTime+1);
	}

	@Override
	public void advance(long t) {
		if(t>=nextStepTime){
			if(this.simulatedStep==0){
				//System.out.println("Sarsa Step one");
				this.partOne();
			}
			else if(this.simulatedStep==1){
				//System.out.println("Sarsa Step two");
				this.partTwo();
			}
		}
	}
}
