package SimulationElements;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.MetricsServlet;
import operator.Bolt;
import operator.FinalBolt;
import operator.IntermediateBolt;
import operator.IntermediateBoltWorker;
import operator.RandomIntegerGenerator;
import operator.SimulatedLatencyMonitor;

public class SimulationMain implements Runnable{
	public final static Logger logger	=	LogManager.getLogger(SimulationMain.class);
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

	public Gauge			thNumber	=	Gauge.build().name("bench_thLevel").help("Number of working threads").register();
	public Gauge			queueDim	=	Gauge.build().name("bench_queueDim").help("Number of queued elements").register();
	public Gauge			rewardVal	=	Gauge.build().name("bench_rewardVal").help("Reward received value").register();
	public Gauge			evalInterval=	Gauge.build().name("bench_evalInterval").help("Evaluation interval").register();
	public Gauge.Child[][]	qMatrix;
	private long beginning;
	
	public SimulationMain(int maxConcurrentThreads,int statesN,int actionsN,int seedingInterval,double epsilonLevel,double yota,double alpha,long beginning){
		super();
		this.maxConcurrentThreads	=	maxConcurrentThreads;
		this.statesN				=	statesN;
		this.actionsN				=	actionsN;
		this.seedingInterval		=	seedingInterval;
		this.epsilonLevel			=	epsilonLevel;
		this.yotaParameter			=	yota;
		this.alpha					=	alpha;
		this.beginning				=	beginning;
	}
	
	public void run(){
	  	ArrayList<Bolt> bolts	=	new ArrayList<Bolt>();
	  	IntervalManager staticManager				=	new ConstantIntervalManager(180000);
		//SimulatedEvalIntervalManager	manager		=	new SimulatedEvalIntervalManager(10000,this);	
		//SimulatedQueueMonitor			monitor		=	new SimulatedQueueMonitor(10,0.4,manager);
		ArrayList<Simulation>			simElements	=	new ArrayList<Simulation>();
		SimulatedLatencyMonitor			latMon		=	new SimulatedLatencyMonitor(0.3,180000);
		RandomIntegerGenerator			generator	=	new RandomIntegerGenerator(36,39);
		FinalBolt 		 				secondLevel	=	new FinalBolt(maxConcurrentThreads,10,testScheduler,staticManager,simElements,"secondlevel",latMon);	
		IntermediateBolt 				firstLevel	=	new IntermediateBolt(maxConcurrentThreads,10,secondLevel,testScheduler,staticManager,simElements,"firstlevel",generator);
		bolts.add(firstLevel);
		bolts.add(secondLevel);
		SimulatedRewardCalculator		rewarder	=	new ParabolicProcessTimeRewardCalculator(2000, 100,
				3000,maxConcurrentThreads,latMon,bolts,false);
		actionsN	=	2*bolts.size()+1;
		
		ExecutorIncreaserDecreaser		executor	=	new ExecutorIncreaserDecreaser(bolts,maxConcurrentThreads,1);
		
	   	  	int[] intervals	=	new int[24];
	   	  	intervals[0]	=	120;
	   	  	intervals[1]	=	130;
	   	  	intervals[2]	=	140;
	   	  	intervals[3]	=	150;
	   	  	intervals[4]	=	160;
	   	  	intervals[5]	=	150;
	   	  	intervals[6]	=	140;
	   	  	intervals[7]	=	100;
	   	  	intervals[8]	=	90;
	   	  	intervals[9]	=	85;
	   	  	intervals[10]	=	78;
	   	  	intervals[11]	=	70;
	   	  	intervals[12]	=	60;
	   	  	intervals[13]	=	40;
	   	  	intervals[14]	=	20;
	   	  	intervals[15]	=	40;
	   	  	intervals[16]	=	35;
	   	  	intervals[17]	=	60;
	   	  	intervals[18]	=	85;
	   	  	intervals[19]	=	90;
	   	  	intervals[20]	=	70;
	   	  	intervals[21]	=	63;
	   	  	intervals[22]	=	20;
	   	  	intervals[23]	=	60;
	   	  	
		SimulatedSeeder	s1	=	new SimulatedSeeder(seedingInterval,firstLevel,testScheduler,generator,intervals);
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
		SimulatedSarsa	sarsa	=	new SimulatedSarsa(statesN,actionsN,1,new SimulatedEpsilonGreedyChooser(epsilonLevel),executor, new LatencyStateReader(latMon,1000,3000),new SimulatedStaticAlphaCalculator(alpha),testScheduler,rewarder,staticManager,this,yotaParameter);
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
	  BasicConfigurator.configure();	//log4j
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
				  SimulationMain main	=	new SimulationMain(32,3,0,450,epsilon,yota,alpha,beginning);
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
