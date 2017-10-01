package main;

import entities.Product;
import services.ProductService;

public class Client {
	public static void main(String args[]){
		ProductService productService = new ProductService(5, 5000, 2);
		new Thread(productService).start(); 
		
		for(int i = 0; i < 10000000; i++){
			Product product = new Product();
			product.setName("Milk "+i);
			productService.addProduct(product);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
