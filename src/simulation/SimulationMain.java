package simulation;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.MetricsServlet;
import operator.Bolt;
import operator.FinalBolt;
import operator.IntermediateBolt;
import operator.IntermediateBoltWorker;
import operator.RandomIntegerGenerator;
import operator.SimulatedLatencyMonitor;
import operator.SimulatedSeeder;
import simulation.algorithms.SimulatedSarsa;
import simulation.executor.DynamicExecutorIncreaserDecreaser;
import simulation.executor.ExecutorIncreaserDecreaser;
import simulation.executor.SimulatedActionExecutor;
import simulation.policy.SimulatedEpsilonGreedyChooser;
import simulation.reward.DeltaRewarderSimplified;
import simulation.reward.ParabolicProcessTimeRewardCalculator;
import simulation.reward.RewardCalculator;
import simulation.state.LatencyStateReader;

public class SimulationMain implements Runnable{
	public final static Logger logger	=	LoggerFactory.getLogger(SimulationMain.class);
	static ReentrantLock 	thNumberLock	=	new ReentrantLock();
	static int 				thNumberSim		=	0;
	static int 				thMax			=	4;
	static HashMap<String, Object> configuration	=	new HashMap<String,Object>();
	
	
	
	public int 						maxConcurrentThreads	=	32;
	public SimulationScheduler 		testScheduler			=	new SimulationScheduler(maxConcurrentThreads);
	public int						statesN					=	3;
	public int 						actionsN				=	0;
	public int						seedingInterval			=	450;
	public double					epsilonLevel			=	0.1;
	public double					yotaParameter			=	0.1;
	public double					alpha					=	0.2;
	public double					totalReward;
	public int[]					steps;

	public Gauge			thNumber	=	Gauge.build().name("bench_thLevel").help("Number of working threads").register();
	public Gauge			queueDim	=	Gauge.build().name("bench_queueDim").help("Number of queued elements").register();
	public Gauge			rewardVal	=	Gauge.build().name("bench_rewardVal").help("Reward received value").register();
	public Gauge			evalInterval=	Gauge.build().name("bench_evalInterval").help("Evaluation interval").register();
	public Gauge.Child[][]	qMatrix;
	private long beginning;
	
	public SimulationMain(int maxConcurrentThreads,int statesN,int actionsN,int seedingInterval,double epsilonLevel,double yota,double alpha,long beginning,int[] steps){
		super();
		this.maxConcurrentThreads	=	maxConcurrentThreads;
		this.statesN				=	statesN;
		this.actionsN				=	actionsN;
		this.seedingInterval		=	seedingInterval;
		this.epsilonLevel			=	epsilonLevel;
		this.yotaParameter			=	yota;
		this.alpha					=	alpha;
		this.beginning				=	beginning;
		this.steps					=	steps;
	}
	
	public void run(){
	  	ArrayList<Bolt> bolts	=	new ArrayList<Bolt>();
	  	IntervalManager staticManager				=	new ConstantIntervalManager(180000);
		//SimulatedEvalIntervalManager	manager		=	new SimulatedEvalIntervalManager(10000,this);	
		//SimulatedQueueMonitor			monitor		=	new SimulatedQueueMonitor(10,0.4,manager);
		ArrayList<Simulation>			simElements	=	new ArrayList<Simulation>();
		SimulatedLatencyMonitor			latMon		=	new SimulatedLatencyMonitor(0.2,180000);
		//RandomIntegerGenerator			generator	=	new RandomIntegerGenerator(31,36);
		
   	  	int[] deltas1	=	new int[4];
   	  	deltas1[0]			=	2;
   	  	deltas1[1]			=	2;
   	  	deltas1[2]			=	2;
   	  	deltas1[3]			=	2;
   	  	
   	  	int[] basev1		=	new int[4];
   	  	basev1[0]			=	33;
   	  	basev1[1]			=	34;
   	  	basev1[2]			=	35;
   	  	basev1[3]			=	31;
   	  	
   	  	int[] deltas2	=	new int[4];
   	  	deltas2[0]			=	2;
   	  	deltas2[1]			=	2;
   	  	deltas2[2]			=	2;
   	  	deltas2[3]			=	2;
   	  	
   	  	int[] basev2		=	new int[4];
   	  	basev2[0]			=	31;
   	  	basev2[1]			=	31;
   	  	basev2[2]			=	31;
   	  	basev2[3]			=	35;
   	  	
   	  	int[] deltas3	=	new int[4];
   	  	deltas3[0]			=	2;
   	  	deltas3[1]			=	2;
   	  	deltas3[2]			=	2;
   	  	deltas3[3]			=	2;
   	  	
   	  	int[] basev3		=	new int[4];
   	  	basev3[0]			=	34;
   	  	basev3[1]			=	32;
   	  	basev3[2]			=	32;
   	  	basev3[3]			=	31;
   	  	
		DynamicIntegerGenerator			gen			=	new DynamicIntegerGenerator(testScheduler,basev1,deltas1);
		DynamicIntegerGenerator			gen2		=	new DynamicIntegerGenerator(testScheduler,basev2,deltas2);
		DynamicIntegerGenerator			gen3		=	new DynamicIntegerGenerator(testScheduler,basev3,deltas3);
		FinalBolt 		 				thirdLevel	=	new FinalBolt(maxConcurrentThreads,10,testScheduler,staticManager,simElements,"secondlevel",latMon);	
		IntermediateBolt 				secondLevel	=	new IntermediateBolt(maxConcurrentThreads,10,thirdLevel,testScheduler,staticManager,simElements,"firstlevel",gen3);
		IntermediateBolt 				firstLevel	=	new IntermediateBolt(maxConcurrentThreads,10,secondLevel,testScheduler,staticManager,simElements,"firstlevel",gen2);
		
		bolts.add(firstLevel);
		bolts.add(secondLevel);
		RewardCalculator		rewarder	=	new DeltaRewarderSimplified(300,3000,4500,bolts,latMon);
		actionsN	=	2*bolts.size()+1;
		
		SimulatedActionExecutor		executor	=	new DynamicExecutorIncreaserDecreaser(bolts,maxConcurrentThreads,steps);
		
	   	  	int[] intervals	=	new int[24];
  	   	  	intervals[0]	=	60;
  	   	  	intervals[1]	=	65;
  	   	  	intervals[2]	=	70;
  	   	  	intervals[3]	=	75;
  	   	  	intervals[4]	=	80;
  	   	  	intervals[5]	=	75;
  	   	  	intervals[6]	=	70;
  	   	  	intervals[7]	=	50;
  	   	  	intervals[8]	=	45;
  	   	  	intervals[9]	=	42;
  	   	  	intervals[10]	=	39;
  	   	  	intervals[11]	=	35;
  	   	  	intervals[12]	=	30;
  	   	  	intervals[13]	=	20;
  	   	  	intervals[14]	=	10;
  	   	  	intervals[15]	=	20;
  	   	  	intervals[16]	=	17;
  	   	  	intervals[17]	=	30;
  	   	  	intervals[18]	=	43;
  	   	  	intervals[19]	=	45;
  	   	  	intervals[20]	=	35;
  	   	  	intervals[21]	=	31;
  	   	  	intervals[22]	=	10;
  	   	  	intervals[23]	=	30;
	   	  	
		SimulatedSeeder	s1	=	new SimulatedSeeder(firstLevel,testScheduler,gen,intervals);
		simElements.add(s1);
		simElements.add(latMon);
		
		String[] labels	=	new String[2];
		labels[0]		=	"row";
		labels[1]		=	"column";
		Gauge q			=	Gauge.build().name("QValue").help("Value of Q matrix in position").labelNames(labels).register();
		qMatrix			=	new Gauge.Child[statesN][actionsN];
		for(int i=0;i<statesN;i++){
			for(int j=0;j<actionsN;j++){
				String[] labelst	=	new String[2];
				labelst[0]		=	i+"";
				labelst[1]		=	j+"";
				qMatrix[i][j]	=	new Gauge.Child();
				q.setChild(qMatrix[i][j], labelst);
				qMatrix[i][j].set(0);
			}
		}
		SimulatedSarsa	sarsa	=	new SimulatedSarsa(statesN,actionsN,1,new SimulatedEpsilonGreedyChooser(epsilonLevel),executor, new LatencyStateReader(latMon,1500,4500),new SimulatedStaticAlphaCalculator(alpha),testScheduler,rewarder,staticManager,this,yotaParameter);
		simElements.add(sarsa);
		//SimulatedDynaQ	dynaQ	=	new SimulatedDynaQ(statesN,concurrentThreads,0,new SimulatedEpsilonGreedyChooser(0.1),new SimulatedFibonacciActionExecutor(),new SimulatedQueueLengthStateReader(15,45),new SimulatedStaticAlphaCalculator(),5,0.95,testScheduler,manager,new SimulatedQueueLengthRewarder());	
		//simElements.add(dynaQ);
		///SimulatedLinearGradientDescendSarsaLambda sarsal	=	new SimulatedLinearGradientDescendSarsaLambda(12,0.1,0.9,0.5,new SimulatedQueueLengthStateReader(),new SimulatedFeaturesEvaluator(),new SimulatedFibonacciActionExecutor(testScheduler),new SimulatedStaticAlphaCalculator(),new SimulatedQueueLengthRewarder(),4,0,testScheduler,manager);
		//simElements.add(sarsal);
		
		
		//simElements.add(monitor);
		//while(true){
		long simEndTime	=	this.testScheduler.simulatedTime+259200000;//12h simulated
		long start		=	beginning;
		long simSteps	=	0;
		int  simHour	=	(new Date(testScheduler.simulatedTime)).getHours();
		while(this.testScheduler.simulatedTime<simEndTime){	
			simSteps++;/*
			if((simSteps%10000)==0){
				//logger.debug("simulated time "+testScheduler.simulatedTime);
				int currentSimHour	=	(new Date(testScheduler.simulatedTime)).getHours();
				if(currentSimHour>simHour){
					simHour	=	currentSimHour;
					long temp	=	System.currentTimeMillis();
					temp	=	temp-start;
					temp=temp/1000;
					if(temp==0){
						temp=1;
					}
					long simulatedSeconds	=	(testScheduler.simulatedTime-testScheduler.beginning)/1000;
					System.out.println("Loading "+((double)(testScheduler.simulatedTime-testScheduler.beginning)/(simEndTime-testScheduler.beginning))*100+"%, simulated "+simulatedSeconds+" seconds in "+temp+" seconds, scaling factor "+simulatedSeconds/temp);
					for(int i=0;i<statesN;i++){
						for(int j=0;j<actionsN;j++){
							DecimalFormat numberFormat = new DecimalFormat("0.00");
							System.out.print(numberFormat.format(sarsa.Q[i][j])+"\t");
							//System.out.print(numberFormat.format(dynaQ.Q[i][j])+"\t");
						}
						System.out.print("\n");
					}
				}		
			}	*/
			for(int i=0;i<simElements.size();i++){
				try {
					simElements.get(i).advance(testScheduler.simulatedTime);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
			}
			try {
				//System.out.println("Scheduled events:");
				//testScheduler.print();
				testScheduler.advance();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		long end	=	System.currentTimeMillis();
		System.out.println("Simulated "+(testScheduler.simulatedTime-testScheduler.beginning)/1000+" seconds (about "+((testScheduler.simulatedTime-testScheduler.beginning)/1000)/3600+" hours)");
		end	=	end	-	start;
		System.out.println("Tempo esecuzione "+(end/1000)/60+" minuti");
		// TODO Auto-generated method stub
		thNumberLock.lock();
		thNumberSim--;
		boolean write	=	false;
		if(this.configuration.isEmpty()){
			write	=	true;
		}
		else{
			double totRew	=	(double)this.configuration.get("totalReward");
			if(totRew<sarsa.totalReward){
				write	=	true;
			}
		}
		if(write==true){
			this.configuration.clear();
			this.configuration.put("totalReward", sarsa.totalReward);
			this.configuration.put("epsilon", this.epsilonLevel);
			this.configuration.put("yota", this.yotaParameter);
			this.configuration.put("alpha", this.alpha);
		}
		for(int i=0;i<statesN;i++){
			for(int j=0;j<actionsN;j++){
				DecimalFormat numberFormat = new DecimalFormat("0.00");
				System.out.print(numberFormat.format(sarsa.Q[i][j])+"\t");
				//System.out.print(numberFormat.format(dynaQ.Q[i][j])+"\t");
			}
			System.out.print("\n");
		}
		this.totalReward	=	sarsa.totalReward;
		logger.debug("Finished evaluation of the following configuration epsilon "+this.epsilonLevel+" alpha "+this.alpha+" yota "+this.yotaParameter);
		logger.debug("Current optimal configuration: total reward "+this.configuration.get("totalReward")+" epsilon "+this.configuration.get("epsilon")+" yota "+this.configuration.get("yota")+" alpha "+this.configuration.get("alpha"));
		thNumberLock.unlock();
		
	}
	
	public static void main(String[] args) {
	 // BasicConfigurator.configure();	log4j
	  /*Server server = new Server(1234);
	  ServletContextHandler context = new ServletContextHandler();
	  context.setContextPath("/");
	  server.setHandler(context);
	  context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
	  try {
		  server.start();
	  } catch (Exception e1) {
		  e1.printStackTrace();
	  }*/

	  for(int i=1;i<10;i++){
		  double alpha	=	(double)i*0.1;
		  for(int j=1;j<10;j++){
			  double yota	=	(double)j*0.1;
			  for(int k=0;k<10;k++){
				  thNumberLock.lock();
				  thNumberSim++;
				  thNumberLock.unlock();
				  double epsilon	=	(double)k*0.1;
				  long beginning	=	System.currentTimeMillis();
				  int[] steps		=	new int[3];
				  steps[0]			=	2;
				  steps[1]			=	1;
				  steps[2]			=	4;
				  SimulationMain main	=	new SimulationMain(32,3,0,450,epsilon,yota,alpha,beginning,steps);
				  Thread simTh	=	new Thread(main);
				  while(thNumberSim>thMax){
					  try {
						logger.debug("all slots busy, delaying thread generation");
						Thread.sleep(10000);
						
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				  }
				  simTh.start();
			  }
		  }
	  }
	}

}
