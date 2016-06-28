package storm;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.topology.TopologyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulationTopology {
	public final static Logger logger	=	LoggerFactory.getLogger(SimulationTopology.class);
    public static void main(String[] args){
        TopologyBuilder builder = new TopologyBuilder();
        Config conf = new Config();
    	conf.setMessageTimeoutSecs(360000);
    	
    	
    	conf.setDebug(true);
    	builder.setSpout("spout", new ConfigurationSpout(32), 1);
    	builder.setBolt("firststage", new SimulationBolt(), 1).shuffleGrouping("spout");
    	builder.setBolt("finalstage", new ResultsBolt(), 1).shuffleGrouping("firststage");
    	if(args.length==0){
    		conf.setMaxTaskParallelism(3);
  	      LocalCluster cluster = new LocalCluster();
  	      logger.info("STARTING LOCAL CLUSTER");
  	      cluster.submitTopology("localSimulator", conf, builder.createTopology());
    	}
    	else{
	    	try {
	    		conf.setNumWorkers(4);
				StormSubmitter.submitTopology(args[0]+"", conf, builder.createTopology());
			} catch (AlreadyAliveException | InvalidTopologyException | AuthorizationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
 	//builder.setSpout("spout", new ConfigurationSpout(5000,intervals), 1);
	//    builder.setBolt("firststage", new IntermediateWorker(5000), 32).shuffleGrouping("spout");
	//    builder.setBolt("secondstage", new FinalWorker(), 32).shuffleGrouping("firststage");
 	//  	StormSubmitter.submitTopology(args[0]+"", conf, builder.createTopology());
}
