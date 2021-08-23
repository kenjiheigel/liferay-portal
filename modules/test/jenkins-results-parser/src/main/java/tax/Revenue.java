/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package tax;

/**
 * @author Calum Ragan
 */
public class Revenue {

	public static void main(String[] args) {
		test("1 book at 12.49 ");
	}

	//product
	//quantity, exempt, imported, amount
	public static class Product {
		int quantity;
		boolean exempt;
		boolean imported;
		double amount;

		public Product(int newQuantity, boolean newExempt, boolean newImported, double newAmount){
			quantity = newQuantity;
			exempt = newExempt;
			imported = newImported;
			amount = newAmount;
		}
	};


	public static void test(String product){
		String[] productArray = product.split(" ");
		int productQuantity = Integer.parseInt(productArray[0]);
		boolean isExempt = false;
		boolean isImported = false;
		String productName = "";
		double productPrice = Double.parseDouble(productArray[productArray.length - 1]);
		for(String word : productArray){
			if(word.equals("book") || word.equals("chocolate") || word.equals("pills")){
				isExempt = true;
				productName = word;
			};
			if (word.equals("imported")){
				isImported = true;
			};
		};
		Product newProduct = new Product(productQuantity,isExempt,isImported,productPrice);
		double salesTax = 0;
		double totalAmount = 0;
		if(!newProduct.exempt && newProduct.imported){
			salesTax = newProduct.amount * 0.15;
		}
		if (newProduct.imported){
			salesTax = newProduct.amount * 0.05;
		}
		if (newProduct.exempt = false){
			salesTax = newProduct.amount * 0.10;
		}
		totalAmount = salesTax + newProduct.amount;
		System.out.println(newProduct.quantity + " " + productName + " at " + newProduct.amount);
		System.out.println("Sales Tax: " + salesTax);
		System.out.println("Total: " + totalAmount);
	}

	//use product string in system.out

}