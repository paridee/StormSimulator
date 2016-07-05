package simulation;

public class SimulatedFeaturesEvaluator {
	public static int[] getFeatures(int state,int action){
		int[]	features	=	new int[12];
		/*if(state==0){
			features[0]	=	1;
		}
		else if(state==1){
			features[1]	=	1;
		}
		else if(state==2){
			features[2]	=	1;
		}
		features[2+(action+1)]	=	1;/*
		if(action+1<currentWorker){
			features[3]	=	1;
		}
		else if(action+1==currentWorker){
			features[4]	=	1;
		}
		else if(action+1>currentWorker){
			features[5]	=	1;
		}*/
		
		features[(state*4)+action]	=	1;
		return features;
	}
}
