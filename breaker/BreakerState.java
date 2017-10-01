package breaker;

public enum BreakerState {

	CLOSE(0), OPEN(1), HALF_OPEN(2);
	private int value;
	private BreakerState(int mValue){
		value = mValue;
	}
	public int getValue(){
		return value;
	}
}
