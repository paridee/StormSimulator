package simulation.executor;

import simulation.SimulationScheduler;

public class SimulatedFibonacciActionExecutor implements SimulatedActionExecutor{
	public SimulationScheduler scheduler;
		
	public SimulatedFibonacciActionExecutor(SimulationScheduler scheduler) {
		super();
	}



	@Override
	public void execute(int action) {
		scheduler.setMaxBusyThreads(action+1);
		System.out.println("Set concurrency level to "+action+1);
	}



	@Override
	public void execute(int action, int state) {
		this.execute(action);
	}

}
