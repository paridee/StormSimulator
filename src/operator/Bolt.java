package operator;

public abstract class Bolt {
	public int	level		=	1;
	String label;
	public abstract void addInQueue(Tuple value);
	public abstract int  getQueueSize();
	public abstract void flushQueues();
	
}
