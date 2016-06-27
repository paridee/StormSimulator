package SimulationElements;

public class SimulatedFibonacciActionExecutor implements SimulatedActionExecutor{
	public SimulationScheduler scheduler;
		
	public SimulatedFibonacciActionExecutor(SimulationScheduler scheduler) {
		super();
		this.scheduler = scheduler;
	}



	@Override
	public void execute(int action) {
		scheduler.setMaxBusyThreads(action+1);
		System.out.println("Set concurrency level to "+action+1);
	}

}
