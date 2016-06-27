package SimulationElements;

public class ConstantIntervalManager implements IntervalManager {

	private int interval;
	
	ConstantIntervalManager(int interval){
		super();
		this.interval	=	interval;
	}
	
	@Override
	public int getInterval() {
		// TODO Auto-generated method stub
		return interval;
	}

}
