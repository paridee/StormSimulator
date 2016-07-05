package storm;

import java.util.Map;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationSpout extends BaseRichSpout{
	public final static Logger logger	=	LoggerFactory.getLogger(ConfigurationSpout.class);
	int genInterval	=	10000;
	int alreadyGenerated	=	0;
	int changeGenerationRate=	32;
	boolean generatedAll	=	false;
	private SpoutOutputCollector collector;
	private int maxTh;
	private long msgId = 0;
	long beginning	=	System.currentTimeMillis();
	
	private static final long serialVersionUID = 1L;

	public ConfigurationSpout(int maxTh){
		super();
		this.maxTh	=	maxTh;
	}
	
	@Override
	public void nextTuple() {
		if(generatedAll==false){
			System.out.println("GENERATING TUPLE!!!");
		  int[] steps		=	new int[3];
		  steps[0]			=	2;
		  steps[1]			=	1;
		  steps[2]			=	4;
		  DecisionSteps	dSteps	=	new DecisionSteps();
		  dSteps.steps		=	steps;
			double 	epsilon;
			double 	alpha;
			double 	yota;
			for(int i=1;i<10;i++){
				yota	=	i*0.1;
				for(int j=10;j>0;j--){
					alpha	=	j*0.1;
					for(int k=0;k<6;k++){
						epsilon	=	k*0.05;
						collector.emit(new Values(epsilon, yota, alpha,beginning,dSteps), msgId++);
						//this.logger.debug("generated tuple");
						System.out.println("tuple generated eps "+epsilon+" yota "+yota+" alpha "+alpha);
						Utils.sleep(300);
						//Utils.sleep(30000);
					}
				}
			}
		}
		this.generatedAll	=	true;
	}

	@Override
	public void open(Map arg0, TopologyContext arg1, SpoutOutputCollector arg2) {
		this.collector	=	arg2;
		
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		arg0.declare(new Fields("epsilon", "yota", "alpha","beginning","steps"));
	}

}
