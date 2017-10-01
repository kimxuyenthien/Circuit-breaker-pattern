package breaker;

import java.util.concurrent.atomic.AtomicLong;

public abstract class BreakerCommand {
	
	private static final AtomicLong ID_GENERATOR = new AtomicLong(0);
	private long id;
	
	public BreakerCommand(){
		id = ID_GENERATOR.incrementAndGet();
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public abstract void run(); 
}
