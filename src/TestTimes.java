import SimulationElements.SimulationScheduler;

public class TestTimes {
	
    public static long fibonacci(long i) {
	/* F(i) non e` definito per interi i negativi! */
	if (i == 0) return 0;
	else if (i == 1) return 1;
	else return fibonacci(i-1) + fibonacci(i-2);
    }
	
	public static void main(String[] args) {
		/*double [] values	=	new double [10];
		for(int i=36;i<46;i++){
			long total	=	0;
			for(int j=0;j<20;j++){
				long now	=	System.nanoTime();//test
				long fibResult	=	fibonacci(i);
				long after	=	System.nanoTime();//test
				total		=	total	+	((after-now)/1000000);
			}
			values[i-36]	=	(total/20);
			System.out.println("average response time "+i+" "+values[i-36]);
		}*/
		
		
		SimulationScheduler testScheduler	=	new SimulationScheduler(32);
		testScheduler.insert(100L);
		testScheduler.insert(400L);
		testScheduler.insert(200L);
		testScheduler.insert(500L);
		testScheduler.insert(50L);
		testScheduler.print();
		
		
	}
}
