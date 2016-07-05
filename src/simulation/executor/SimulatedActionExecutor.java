package simulation.executor;

public interface SimulatedActionExecutor {
	void execute(int action) throws Exception;
	void execute(int action, int state);
}
