package storm;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;

import SimulationElements.SimulationMain;

public class ResultsBolt extends BaseBasicBolt{
	public final static Logger logger	=	LogManager.getLogger(ResultsBolt.class);
	static double 	bestEpsilon		=	0;
	static double 	bestAlpha		=	0;
	static double 	bestYota		=	0;
	static int 		bestStep		=	0;
	static double  bestReward		=	0;
	/**
	 * 
	 */
	private static final long serialVersionUID = -7783234200219060017L;

	@Override
	public void execute(Tuple arg0, BasicOutputCollector arg1) {
		// TODO Auto-generated method stub
	
		double 	epsilon		=	arg0.getDouble(0);
		double 	alpha		=	arg0.getDouble(2);;
		double 	yota		=	arg0.getDouble(1);
		int 	step		=	arg0.getInteger(3);
		double  reward		=	arg0.getDouble(4);
		if(reward>bestReward){
			bestEpsilon		=	epsilon;
			bestAlpha		=	alpha;
			bestYota		=	yota;
			bestStep		=	step;
			bestReward		=	reward;
			logger.debug("New best configuration found, value "+reward+" epsilon "+epsilon+" alpha "+alpha+" yota "+yota+" step "+step);
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		// TODO Auto-generated method stub
		
	}

}
