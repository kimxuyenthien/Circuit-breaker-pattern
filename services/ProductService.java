package services;

import org.apache.log4j.Logger;

import breaker.BreakerCommand;
import breaker.BreakerExecute;
import breaker.exceptions.BreakerRejectException;
import entities.Product;

public class ProductService extends BreakerExecute{
	
	private static Logger LOGGER = Logger.getLogger(ProductService.class);
	
	public ProductService(int numberAttempts, int timeout, int numberLimitFailure) {
		super(numberAttempts, timeout, numberLimitFailure);
	}
	
	public void addProduct(Product product){
		AddProductCommand command = new AddProductCommand();
		command.setProduct(product);
		putCommand(command);
	}

	@Override
	public void error(Exception err, BreakerCommand command) throws Exception {
		// TODO Auto-generated method stub
		LOGGER.error(err, err);
		
		if(err instanceof BreakerRejectException){
			System.out.println("Service is unavaible");
		}
	}
}
