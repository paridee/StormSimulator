package simulation.algorithms;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import operator.IntermediateBolt;
import simulation.IntervalManager;
import simulation.RewardEvaluation;
import simulation.SimulatedAlphaCalculator;
import simulation.Simulation;
import simulation.SimulationMain;
import simulation.SimulationScheduler;
import simulation.executor.SimulatedActionExecutor;
import simulation.policy.SimulatedPolicyChooser;
import simulation.reward.RewardCalculator;
import simulation.state.SimulatedStateReader;

public class SimulatedSarsa implements Simulation,RewardEvaluation{
	public final static Logger logger	=	LoggerFactory.getLogger(SimulatedSarsa.class);
	int simulatedStep	=	0;
	long nextStepTime	=	0;
	RewardCalculator	rewardCalculator;
	int state;
	int action;
	IntervalManager intManager;
	public double	totalReward		=	0;
	
	int states				=	1;
	int actions				=	1;
	//int	evalInterval		=	1000;
	int currentState		=	0;
	double yotaParameter	=	0.1;
	double[] V;
	public double[][] Q;
	PrintWriter[][] QWriter;
	SimulatedPolicyChooser 		policy;
	SimulatedActionExecutor 	executor;
	SimulatedStateReader		stateReader;
	SimulatedAlphaCalculator	alphaCalculator;
	SimulationScheduler			sched;
	SimulationMain				currentMain;

	public SimulatedSarsa(int states, int actions,int initialState,SimulatedPolicyChooser chooser,SimulatedActionExecutor actionExecutor,SimulatedStateReader stateReader,SimulatedAlphaCalculator alphaCalculator,SimulationScheduler schedu,RewardCalculator rewCalculator,IntervalManager intManager,SimulationMain currentMain,double yotaParameter){
		super();
		this.states				=	states;
		this.actions			=	actions;
		this.state				=	initialState;
		this.policy				=	chooser;
		this.executor			=	actionExecutor;
		this.stateReader		=	stateReader;
		this.alphaCalculator	=	alphaCalculator;
		this.rewardCalculator	=	rewCalculator;
		this.sched				=	schedu;
		this.intManager			=	intManager;
		this.currentMain		=	currentMain;
		this.yotaParameter		=	yotaParameter;
		V		=	new double[states];
		Q		=	new double[states][actions];
		QWriter	=	new PrintWriter[states][actions];
		for(int i=0;i<states;i++){
			V[i]	=	0;
			for(int j=0;j<actions;j++){
				Q[i][j]	=	0;
				try {
					QWriter[i][j]	=	new PrintWriter("Q_"+i+"_"+j+".txt");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public void advance(long t) {
		if(t>=nextStepTime){
			if(this.simulatedStep==0){
				//System.out.println("Sarsa Step one");
				sarsaStepOne();
			}
			else if(this.simulatedStep==1){
				//System.out.println("Sarsa Step two");
				sarsaStepTwo();
			}
		}
	}
	
	private void sarsaStepTwo() {
		// TODO Auto-generated method stub	//begin with reward
		double	reward	=	this.rewardCalculator.giveReward();
		this.totalReward=	totalReward+reward;	
		//logger.debug("reward obtained "+reward+" total "+this.totalReward);
		int oldState	=	state;
		int newState	=	this.stateReader.getCurrentState();
		double[] policy		=	this.policy.policyForState(newState,Q);
		double temp			=	0;
		for(int i=0;i<actions;i++){
			temp			=	temp	+	(policy[i]*Q[newState][i]);
		}
		V[newState]			=	temp;
		Q[oldState][action]	=	Q[oldState][action]+
								this.alphaCalculator.getAlpha()*(
										reward+(this.yotaParameter*V[newState])-Q[oldState][action]);
		/*
		System.out.println("Updated Q["+oldState+"]["+action+"]");
		System.out.println("Q matrix:"+states+" "+actions);
		for(int i=0;i<states;i++){
			for(int j=0;j<actions;j++){
				double qij					=	Q[i][j];
				DecimalFormat numberFormat = new DecimalFormat("0.00");
				System.out.print(numberFormat.format(qij)+"\t");
			}
			System.out.print("\n");
		}*/
		for(int i=0;i<states;i++){
			for(int j=0;j<actions;j++){
				double qij					=	Q[i][j];
				DecimalFormat numberFormat = new DecimalFormat("0.00");
				QWriter[i][j].println(numberFormat.format(qij)+"\t");
				QWriter[i][j].flush();
				this.currentMain.qMatrix[i][j].set(qij);
			}
		}
		
		this.simulatedStep	=	0;
		this.sched.insert(sched.simulatedTime+1);
	}

	void sarsaStepOne(){
		state			=	this.stateReader.getCurrentState();	//reading state
		action			=	this.policy.actionForState(state,Q);
		try {
			this.executor.execute(action,state);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.nextStepTime	=	sched.simulatedTime+this.intManager.getInterval();
		this.sched.insert(nextStepTime);
		this.simulatedStep++;
	}

	@Override
	public double getTotalReward() {
		return this.totalReward;
	}


}
