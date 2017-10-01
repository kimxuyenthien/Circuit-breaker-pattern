package services;

import org.apache.log4j.Logger;

import breaker.BreakerCommand;
import entities.Product;

public class AddProductCommand extends BreakerCommand{
	
	private static Logger LOGGER = Logger.getLogger(AddProductCommand.class);
	private Product product;
	
	@Override
	public void run() {
		LOGGER.info("Process product "+product.getName());
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}
}
