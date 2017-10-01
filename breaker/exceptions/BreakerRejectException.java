package breaker.exceptions;

public class BreakerRejectException extends Exception {

	private static final long serialVersionUID = 1L;


	public BreakerRejectException() {
		super("Service is unavailable, stop put command");
	}
}
