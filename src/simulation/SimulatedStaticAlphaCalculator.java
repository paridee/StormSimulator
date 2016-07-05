package simulation;

public class SimulatedStaticAlphaCalculator implements SimulatedAlphaCalculator {
	double alpha;
	
	public SimulatedStaticAlphaCalculator(double alphaValue){
		super();
		this.alpha	=	alphaValue;
	}
	@Override
	public double getAlpha() {
		// TODO Auto-generated method stub
		return alpha;
	}

}
