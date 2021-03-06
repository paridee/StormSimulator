package storm;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulation.SimulationMain;

public class SimulationBolt extends BaseBasicBolt{
	public final static Logger logger	=	LoggerFactory.getLogger(SimulationBolt.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1164834917457337503L;

	@Override
	public void execute(Tuple arg0, BasicOutputCollector arg1) {
		System.out.println("incoming tuple");
		// TODO Auto-generated method stub
		 //SimulationMain main	=	new SimulationMain(32,3,0,450,epsilon,yota,alpha,beginning);
		 Thread executor	=	new Thread();
		 executor.start();
		 try {
			executor.join();
			double 	epsilon		=	arg0.getDouble(0);
			double 	alpha		=	arg0.getDouble(2);;
			double 	yota		=	arg0.getDouble(1);
			long 	beginning	=	arg0.getLong(3);
			DecisionSteps steps	=	(DecisionSteps)arg0.getValue(4);
			logger.debug("simulation steps "+steps);
			SimulationMain main	=	new SimulationMain(32,3,0,450,epsilon,yota,alpha,beginning,steps.steps);
			Thread simTh		=	new Thread(main);
			System.out.println("starting simulation");
			simTh.start();
			simTh.join();
			double  value		=	main.totalReward;
			arg1.emit(new Values(epsilon,yota,alpha,value));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		arg0.declare(new Fields("epsilon", "yota", "alpha","value"));
	}

}
