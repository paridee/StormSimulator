package storm;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.topology.TopologyBuilder;

public class SimulationTopology {
    
    public static void main(String[] args){
        TopologyBuilder builder = new TopologyBuilder();
        Config conf = new Config();
    	conf.setMessageTimeoutSecs(360000);
    	conf.setNumWorkers(4);
    	conf.setDebug(true);
    	builder.setSpout("spout", new ConfigurationSpout(), 1);
    	builder.setBolt("firststage", new SimulationBolt(), 32).shuffleGrouping("spout");
    	builder.setBolt("finalstage", new SimulationBolt(), 1).shuffleGrouping("firststage");
    	if(args.length==0){
  	      LocalCluster cluster = new LocalCluster();
  	      cluster.submitTopology("localSimulator", conf, builder.createTopology());
    	}
    	else{
	    	try {
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
