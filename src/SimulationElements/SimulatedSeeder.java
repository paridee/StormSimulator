package SimulationElements;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import operator.Bolt;
import operator.IntegerTuple;
import operator.IntermediateBolt;
import operator.RandomIntegerGenerator;

public class SimulatedSeeder implements Simulation {
	public final static Logger logger	=	LogManager.getLogger(SimulatedSeeder.class);
	int seedInterval	=	0;
	long nextSeed		=	0;
	Bolt firstBolt;
	PrintWriter out;
	private SimulationScheduler sched;
	ValueGenerator	gen;
	private int[] intervals;
	
	public SimulatedSeeder(int seedInterval, Bolt firstBolt,SimulationScheduler sched,ValueGenerator generator,int[] intervals) {
		super();
		this.seedInterval = seedInterval;
		this.firstBolt		=	firstBolt;
		this.sched	=	sched;
		try {
			out	=	new PrintWriter("queue.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.nextSeed	=	sched.simulatedTime+1;
		gen	=	generator;
		this.intervals	=	intervals;
	}

	public int generate(){	//generator to be substituted
		return gen.generate();
	}
	
	public SimulatedSeeder(int seedInterval){
		this.seedInterval	=	seedInterval;
	}

	@Override
	public void advance(long t) {
		if(t>=this.nextSeed){
			Date	date	=	new Date(t);
			//logger.debug("Generated date "+date.toString());
			Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
			calendar.setTime(date);   // assigns calendar to given date 
			int hourNow		=	calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
			int minutesNow	=	calendar.get(Calendar.MINUTE);
			double begin	=	intervals[hourNow];
			double end		=	intervals[(hourNow+1)%24];
			double sleepVal	=	begin+((((double)(minutesNow))/60)*((double)(end-begin)));
			this.nextSeed	=	t+(int)sleepVal;
			this.firstBolt.addInQueue(new IntegerTuple(sched.simulatedTime,this.generate()));
			//logger.debug("Generating seed at "+sched.simulatedTime+" seed interval "+this.seedInterval+" next seed "+nextSeed+" t "+t);
			//System.out.println("Added element in queue, size "+queue.size());
//			System.out.println("ADDING ELEMENT IN QUEUE (SIZE): "+this.firstBolt.getQueueSize());
			out.println("ADDING ELEMENT IN QUEUE (SIZE): "+this.firstBolt.getQueueSize());
			out.flush();
			this.sched.insert((long) (t+this.seedInterval));
			this.sched.insert(this.sched.simulatedTime+1);
		}
	}
	
}
