package SimulationElements;

public class SimulatedQueueLengthStateReader implements SimulatedStateReader {

	@Override
	public int getCurrentState() {
		// TODO Auto-generated method stub
		return 0;
	}
/*OBSOLETE CLASS
	
	int lowThresh	=	0;
	int highThresh	=	0;
	
	public SimulatedQueueLengthStateReader(int lowThresh, int highThresh) {
		super();
		this.lowThresh = lowThresh;
		this.highThresh = highThresh;
	}


	@Override
	public int getCurrentState() {
		if(SimulationMain.queue.size()<lowThresh){
			return 0;
		}
		else if(SimulationMain.queue.size()>highThresh){
			return 2;
		}
		return 1;
	}
*/
}
