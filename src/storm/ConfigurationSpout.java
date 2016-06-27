package storm;

import java.util.Map;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

public class ConfigurationSpout extends BaseRichSpout{
	
	int genInterval	=	10000;
	int alreadyGenerated	=	0;
	int changeGenerationRate=	32;
	private SpoutOutputCollector collector;
	private int maxTh;
	private long msgId = 0;
	long beginning	=	System.currentTimeMillis();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void nextTuple() {
		Utils.sleep(genInterval);
		double 	epsilon;
		double 	alpha;
		double 	yota;
		for(int i=1;i<10;i++){
			yota	=	i*0.1;
			for(int j=1;j<10;j++){
				alpha	=	j*0.1;
				for(int k=0;k<10;k++){
					epsilon	=	k*0.1;
					for(int l=1;l<=maxTh/2;l++){
						collector.emit(new Values(epsilon, yota, alpha,l,beginning), msgId++);
						if(msgId>32){
							Utils.sleep(genInterval);
						}
					}
				}
			}
		}
        
	}

	@Override
	public void open(Map arg0, TopologyContext arg1, SpoutOutputCollector arg2) {
		this.collector	=	arg2;
		
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		arg0.declare(new Fields("epsilon", "yota", "alpha","step","beginning"));
	}

}
