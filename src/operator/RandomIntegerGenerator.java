package operator;

import SimulationElements.ValueGenerator;

public class RandomIntegerGenerator implements ValueGenerator{
	int low;
	int high;
	
	public RandomIntegerGenerator(int lowerBound, int upperBound){
		this.low	=	lowerBound;
		this.high	=	upperBound;
	}
	
	public int generate(){
		int delta	=	high	-	low;
		int res		=	(int)(Math.random()*delta);
		return res+low;
	}
	

}
