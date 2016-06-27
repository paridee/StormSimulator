package storm;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import SimulationElements.SimulationMain;

public class SimulationBolt extends BaseBasicBolt{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1164834917457337503L;

	@Override
	public void execute(Tuple arg0, BasicOutputCollector arg1) {
		// TODO Auto-generated method stub
		 //SimulationMain main	=	new SimulationMain(32,3,0,450,epsilon,yota,alpha,beginning);
		 Thread executor	=	new Thread();
		 executor.start();
		 try {
			executor.join();
			double 	epsilon		=	arg0.getDouble(0);
			double 	alpha		=	arg0.getDouble(2);;
			double 	yota		=	arg0.getDouble(1);
			int 	step		=	arg0.getInteger(3);
			long 	beginning	=	arg0.getLong(4);
			SimulationMain main	=	new SimulationMain(32,3,0,450,epsilon,yota,alpha,beginning);
			Thread simTh		=	new Thread(main);
			simTh.start();
			simTh.join();
			double  value		=	main.totalReward;
			arg1.emit(new Values(epsilon,yota,alpha,step,value));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		arg0.declare(new Fields("epsilon", "yota", "alpha","step","value"));
	}

}
