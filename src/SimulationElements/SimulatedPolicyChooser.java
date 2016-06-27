package SimulationElements;

public interface SimulatedPolicyChooser {
	double[]	policyForState(int stateId, double[][] q);
	int 		actionForState(int currentState, double[][] q);
}
