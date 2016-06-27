package SimulationElements;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class SimulatedEvalIntervalManager implements IntervalManager {
	private double 	worstCase	=	10;
	private int 	expiration	=	100;
	private int 	counter			=	0;
	public final static Logger logger	=	LogManager.getLogger(SimulatedEvalIntervalManager.class);
	PrintWriter		writer;
	SimulationMain simMain;

	public SimulatedEvalIntervalManager(int expiration,SimulationMain simMain){
		super();
		this.expiration	=	expiration;
		try {
			writer			=	new PrintWriter("evalInterval.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.simMain	=	simMain;
	}
	
	public void evaluateRespTime(double time){
		counter++;
		if(time>this.worstCase||counter>expiration){
			logger.debug("EvalIntervalManager set worst case to "+time);
			this.worstCase	=	time;
			counter			=	0;
			writer.println(time*3+"");
			this.simMain.evalInterval.set(time*3);
			writer.flush();
		}
	}
	public double getWorstCase(){
		return this.worstCase;
	}

	public int getInterval() {
		return (int)this.worstCase*3;
	}
}
