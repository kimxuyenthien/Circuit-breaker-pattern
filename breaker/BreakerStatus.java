package breaker;

public enum BreakerStatus {

	CLOSE(0), OPEN(1), HALF_OPEN(2);
	private int value;
	private BreakerStatus(int mValue){
		value = mValue;
	}
	public int getValue(){
		return value;
	}
}
