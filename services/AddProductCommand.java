package services;

import java.util.Random;

import org.apache.log4j.Logger;

import breaker.BreakerCommand;
import entities.Product;

public class AddProductCommand extends BreakerCommand{
	
	private static Logger LOGGER = Logger.getLogger(AddProductCommand.class);
	private Product product;
	
	@Override
	public void run() {
		LOGGER.info("Process product "+product.getName());
	/*	int random = new Random().nextInt(20);
		if(random > 15){
			int a = 1/0;
		}*/
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}
}
