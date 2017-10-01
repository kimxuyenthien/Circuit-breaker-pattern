package breaker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import breaker.exceptions.BreakerExpiredException;
import breaker.exceptions.BreakerRejectException;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

public abstract class BreakerExecute implements Runnable {

	private static Logger LOGGER = Logger.getLogger(BreakerExecute.class);

	private static Timer TIMER = new HashedWheelTimer();
	private BlockingQueue<BreakerCommand> commandQueues = new LinkedBlockingQueue<BreakerCommand>();
	private ConcurrentMap<Long, Integer> mapRetryCommand = new ConcurrentHashMap<Long, Integer>();
	private BreakerState state = BreakerState.CLOSE;
	private AtomicInteger numberFailure = new AtomicInteger(0);
	private int numberAttempts;
	private int timeout;
	private int limitFailure;
	private boolean isRunning = false;

	public BreakerExecute(int numberAttempts, int timeout, int limitFailure) {
		super();
		this.numberAttempts = numberAttempts;
		this.timeout = timeout;
		this.limitFailure = limitFailure;
	}

	public void run() {

		if (isRunning) {
			return;
		}
		isRunning = true;

		while (isRunning) {
			
			BreakerCommand currentCommand = null;

			try {
				currentCommand = commandQueues.take();

				if (state == BreakerState.OPEN) {
					try {
						error(new BreakerRejectException(), null);
						commandQueues.add(currentCommand);
						continue;
					} catch (Exception errHandle) {
						LOGGER.error(errHandle, errHandle);
					}
				}
				
				currentCommand.run();

				if (state == BreakerState.HALF_OPEN) {

					state = BreakerState.CLOSE;
					numberFailure.set(0);
				}

			} catch (Exception err) {
				
				LOGGER.error(err, err);
				
				// re-put to queue attempts to execute command
				Integer counter = mapRetryCommand.get(currentCommand.getId());
				if(counter == null){
					counter = 1;
					mapRetryCommand.put(currentCommand.getId(), counter);
				}
				
				if (counter < numberAttempts) {
					commandQueues.add(currentCommand);
					counter++;
					mapRetryCommand.put(currentCommand.getId(), counter);
					
				} else {
				
					mapRetryCommand.remove(currentCommand.getId());
					try {
						error(new BreakerExpiredException(), currentCommand);
					} catch (Exception errHandle) {
						LOGGER.error(errHandle, errHandle);
					}
				}
				if (state == BreakerState.CLOSE) {

					if (numberFailure.incrementAndGet() > limitFailure) {
					
						state = BreakerState.OPEN;
						TIMER.newTimeout(new TimerTask() {
							@Override
							public void run(Timeout timeout) throws Exception {

								state = BreakerState.HALF_OPEN;
							}
						}, timeout, TimeUnit.MILLISECONDS);
					}
				}

				if (state == BreakerState.HALF_OPEN) {

					state = BreakerState.OPEN;
					TIMER.newTimeout(new TimerTask() {
						@Override
						public void run(Timeout timeout) throws Exception {

							state = BreakerState.HALF_OPEN;
						}
					}, timeout, TimeUnit.MILLISECONDS);
				}
			}
		}
	}

	public void putCommand(BreakerCommand command) {

		try {
			commandQueues.put(command);
		} catch (InterruptedException e) {
			LOGGER.error(e, e);
		}
	}

	public abstract void error(Exception err, BreakerCommand command) throws Exception;
}
