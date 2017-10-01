package breaker.exceptions;

public class BreakerExpiredException extends Exception{

	private static final long serialVersionUID = 1L;

	public BreakerExpiredException() {
		super("The number of execution attempts failed");
	}
}
