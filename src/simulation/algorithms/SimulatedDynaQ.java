package simulation.algorithms;

import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulation.SimulatedAlphaCalculator;
import simulation.SimulatedEvalIntervalManager;
import simulation.Simulation;
import simulation.SimulationMain;
import simulation.SimulationScheduler;
import simulation.executor.SimulatedActionExecutor;
import simulation.policy.SimulatedPolicyChooser;
import simulation.reward.RewardCalculator;
import simulation.state.SimulatedStateReader;


public class SimulatedDynaQ implements Simulation {
	public final static Logger logger	=	LoggerFactory.getLogger(SimulatedDynaQ.class);
	
	private int states;
	private int actions;
	private SimulatedPolicyChooser policy;
	private SimulatedActionExecutor executor;
	private SimulatedStateReader stateReader;
	private SimulatedAlphaCalculator alphaCalculator;
	private RewardCalculator rewardCalculator;
	private SimulationMain simMain;
	public 	double[][] 	Q;
	private double[][] 	modelCumulateR;
	private boolean[][] visited;
	private long[][][]	visits;
	private int			simulationSteps;
	private int 		currentState;
	private double yotaparameter;
	private SimulationScheduler	sched;
	private SimulatedEvalIntervalManager intManager;
	private int simulatedStep	=	0;
	private long nextStepTime	=	0;
	private int action;
	
	public SimulatedDynaQ(int states, int actions,int initialState,SimulatedPolicyChooser chooser,SimulatedActionExecutor actionExecutor,SimulatedStateReader stateReader,SimulatedAlphaCalculator alphaCalculator,int simulationSteps,double yotaparameter,SimulationScheduler	sched,SimulatedEvalIntervalManager intManager,RewardCalculator rewardCalculator,SimulationMain simMain){
		Q = new double[states][actions];
		this.yotaparameter		=	yotaparameter;
		this.states				=	states;
		this.actions			=	actions;
		this.policy				=	chooser;
		this.executor			=	actionExecutor;
		this.stateReader		=	stateReader;
		this.alphaCalculator	=	alphaCalculator;
		this.simulationSteps	=	simulationSteps;
		this.currentState		=	initialState;
		this.sched				=	sched;
		this.intManager			=	intManager;
		this.rewardCalculator	=	rewardCalculator;
		this.simMain			=	simMain;
		
		for(int i=0;i<states;i++){
			for(int j=0;j<actions;j++){
				Q[i][j]	=	0;
			}
		}
		this.modelCumulateR	=	new double[states][actions];
		this.visits	=	new long[states][actions][states];
		this.visited=	new boolean[states][actions];
		for(int i=0;i<states;i++){
			for(int j=0;j<actions;j++){
				for(int k=0;k<states;k++){
					visits[i][j][k]=1;	//initialized to one in order to avoid 0/0
				}
				visited[i][j]	=	false;	//visit array initialization
			}
		}
	}
	
	public void dynaQStepOne() {
		currentState		=	this.stateReader.getCurrentState();
		action			=	this.policy.actionForState(currentState,Q);
		logger.debug("Action chosen "+action);	//test
		try {
			this.executor.execute(action,currentState);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.nextStepTime	=	sched.simulatedTime+this.intManager.getInterval();
		this.sched.insert(nextStepTime);
		this.simulatedStep++;
	}

	public void dynaQStepTwo(){
		double	reward	=	this.rewardCalculator.giveReward();	
		this.simMain.rewardVal.set(reward);
		int oldState		=	this.currentState;
		int newState		=	this.stateReader.getCurrentState();
		this.currentState	=	newState;
		//updating model data
		this.modelCumulateR[oldState][action]	=	this.modelCumulateR[oldState][action]+reward;
		this.visits[oldState][action][newState]	=	this.visits[oldState][action][newState]+1;
		this.visited[oldState][action]			=	true;
		//end update model data
		double alpha	=	this.alphaCalculator.getAlpha();
		double maxQSnew	=	this.getMaxQ(newState);
		Q[oldState][action]	=	Q[oldState][action]+(alpha*(reward+(yotaparameter*maxQSnew)-Q[oldState][action]));
		
		logger.info("Real experience: Updated Q["+oldState+"]["+action+"]");
		logger.info("Q matrix:"+states+" "+actions);
		for(int i=0;i<states;i++){
			for(int j=0;j<actions;j++){
				double qij					=	Q[i][j];
				DecimalFormat numberFormat = new DecimalFormat("0.00");
				System.out.print(numberFormat.format(qij)+"\t");
				this.simMain.qMatrix[i][j].set(qij);
			}
			System.out.print("\n");
		}
		
		System.out.println("\n\ncumulated R matrix:"+states+" "+actions);
		for(int i=0;i<states;i++){
			for(int j=0;j<actions;j++){
				double qij					=	this.modelCumulateR[i][j];
				DecimalFormat numberFormat = new DecimalFormat("0.00");
				System.out.print(numberFormat.format(qij)+"\t");
				this.simMain.qMatrix[i][j].set(qij);
			}
			System.out.print("\n");
		}
		
		
		
		for(int k=0;k<this.simulationSteps;k++){
			int simState;
			int simAction;
			do{
				simState	=	((int)(Math.random()*states))%states;
				simAction	=	((int)(Math.random()*actions))%actions;
			}while(this.visited[simState][simAction]==false);
			double simReward	=	this.getStateActionAverageReward(simState, simAction);
			int nextState		=	this.getNextSimulatedState(simState, simAction);
			maxQSnew			=	this.getMaxQ(nextState);
			Q[simState][simAction]	=	Q[simState][simAction]+(alpha*(simReward+(yotaparameter*maxQSnew)-Q[simState][simAction]));
		
			
			logger.info("SimulationStep Q["+oldState+"]["+action+"]");
			logger.info("Q matrix:"+states+" "+actions);
			for(int i=0;i<states;i++){
				for(int j=0;j<actions;j++){
					double qij					=	Q[i][j];
					DecimalFormat numberFormat = new DecimalFormat("0.00");
					System.out.print(numberFormat.format(qij)+"\t");
					this.simMain.qMatrix[i][j].set(qij);
				}
				System.out.print("\n");
			}	
		}
		this.simulatedStep	=	0;
		this.sched.insert(sched.simulatedTime+1);
	}
	
	private double getMaxQ(int newState){
		double maxQSnew	=	Q[newState][0];
		for(int i=1;i<actions;i++){
			if(Q[newState][i]>maxQSnew){
				maxQSnew	=	Q[newState][i];
			}
		}
		return maxQSnew;
	}
	
	private int getNextSimulatedState(int state,int action){
		long 	totalVisits	=	0;
		for(int i=0;i<states;i++){
			totalVisits		=	totalVisits+this.visits[state][action][i];
		}
		double[] p	=	new double[states];
		for(int i=0;i<states;i++){
			p[i]		=	((double)this.visits[state][action][i])/((double)totalVisits);
			System.out.println("P estimated ["+state+"]["+action+"]["+i+"]: "+p[i]);
		}
		double[] cumulative	=	new double[states];
		cumulative[0]		=	p[0];
		System.out.println("F estimated ["+state+"]["+action+"]["+0+"]: "+cumulative[0]);
		for(int i=1;i<states;i++){
			cumulative[i]	=	p[i]+cumulative[i-1];
			System.out.println("F estimated ["+state+"]["+action+"]["+i+"]: "+cumulative[i]);
		}
		double rand	=	Math.random();
		for(int i=0;i<states;i++){	
			if(rand<cumulative[i]){
				System.out.println("random value "+rand+" returning "+i);
				return i;
			}
		}
		return states-1;
	}
	
	private double getStateActionAverageReward(int state, int action){
		double numerator			=	this.modelCumulateR[state][action];
		long   stateActionVisits	=	0;
		for(int i=0;i<this.states;i++){
			stateActionVisits		=	stateActionVisits+this.visits[state][action][i];
		}
		double result	=	numerator/(double)stateActionVisits;
		return result;
	}

	@Override
	public void advance(long t) {
		if(t>=nextStepTime){
			if(this.simulatedStep==0){
				//System.out.println("Sarsa Step one");
				this.dynaQStepOne();
			}
			else if(this.simulatedStep==1){
				//System.out.println("Sarsa Step two");
				this.dynaQStepTwo();
			}
		}
	}
}
