package simulation;

import java.util.Date;

public class DynamicIntegerGenerator implements ValueGenerator {
	SimulationScheduler sched;
	public int[] basevalue;
	public int[] delta;
	
	DynamicIntegerGenerator(SimulationScheduler sched,int basevalue[],int[] delta){
		super();
		this.sched		=	sched;
		this.basevalue	=	basevalue;
		this.delta		=	delta;
	}
	
	@Override
	public int generate() {
		if(basevalue.length>3){
			if(delta.length>3){
				Date aDate	=	new Date(sched.simulatedTime);
				int hnow	=	(aDate).getHours();
				//System.out.println("chosen delta "+delta[hnow/6]+" chosen base "+basevalue[hnow/6]);
				int aValue	=	(int)((Math.random()*delta[hnow/6])+basevalue[hnow/6]);
				//System.out.println("value generated "+aValue);
				return aValue;
			}
		}
		// TODO Auto-generated method stub
		return 0;
	}

}
