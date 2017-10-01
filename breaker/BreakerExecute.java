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
	private BlockingQueue<BreakerCommand> commandQueues = new LinkedBlockingQueue<>();
	private ConcurrentMap<BreakerCommand, Integer> mapRetryCommand = new ConcurrentHashMap<>();
	private BreakerStatus status = BreakerStatus.CLOSE;
	private AtomicInteger numberFailure = new AtomicInteger(0);
	private int numberAttempts;
	private int timeout;
	private int numberLimitFailure;
	private boolean isRunning = false;

	public BreakerExecute(int numberAttempts, int timeout, int numberLimitFailure) {
		super();
		this.numberAttempts = numberAttempts;
		this.timeout = timeout;
		this.numberLimitFailure = numberLimitFailure;
	}

	public void run() {

		if (isRunning) {
			return;
		}
		isRunning = true;

		while (isRunning) {
			
			BreakerCommand currentCommand = null;
			// take a command from queue to execute
			try {
				currentCommand = commandQueues.take();
				
				if (status == BreakerStatus.OPEN) {
					try {
						error(new BreakerRejectException(), null);
						commandQueues.add(currentCommand);
						continue;
					} catch (Exception errHandle) {
						LOGGER.error(errHandle, errHandle);
					}
				}
				
				currentCommand.run();

				if (status == BreakerStatus.HALF_OPEN) {

					status = BreakerStatus.CLOSE;
					numberFailure.set(0);
				}

			} catch (Exception err) {
				
				LOGGER.error(err, err);
				
				// re-put to queue attempts to execute command
				Integer counter = mapRetryCommand.get(currentCommand);
				if(counter == null){
					counter = 1;
					mapRetryCommand.put(currentCommand, counter);
				}
				
				if (counter < numberAttempts) {
					commandQueues.add(currentCommand);
					counter++;
					mapRetryCommand.put(currentCommand, counter);
					
				} else {
				
					mapRetryCommand.remove(currentCommand);
					try {
						error(new BreakerExpiredException(), currentCommand);
					} catch (Exception errHandle) {
						LOGGER.error(errHandle, errHandle);
					}
				}
				if (status == BreakerStatus.CLOSE) {

					if (numberFailure.incrementAndGet() > numberLimitFailure) {
					
						status = BreakerStatus.OPEN;
						TIMER.newTimeout(new TimerTask() {
							@Override
							public void run(Timeout timeout) throws Exception {

								status = BreakerStatus.HALF_OPEN;
							}
						}, timeout, TimeUnit.MILLISECONDS);
					}
				}

				if (status == BreakerStatus.HALF_OPEN) {

					status = BreakerStatus.OPEN;
					TIMER.newTimeout(new TimerTask() {
						@Override
						public void run(Timeout timeout) throws Exception {

							status = BreakerStatus.HALF_OPEN;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public abstract void error(Exception err, BreakerCommand command) throws Exception;
}
