package com.acertainbookstore.client.tests;

import java.util.List;
import java.util.Set;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.client.tests.ConcurrencyTest.Result;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;

public class GetBooksThread implements Runnable{

	private StockManager stockmanager;
	private Result result;
	private Set<Integer> isbns;
	private int iterations;
	private int beforeStock;
	private int afterStock;
	
	public GetBooksThread(StockManager stockmanager, 
						  Result result, Set<Integer> isbns, 
						  int iterations, 
						  int beforeStock, 
						  int afterStock){
		this.stockmanager = stockmanager;
		this.result = result;
		this.isbns = isbns;
		this.iterations = iterations;
		this.beforeStock = beforeStock;
		this.afterStock = afterStock;
	}
	
	@Override
	public void run() {
		try {
			int counter = 0;
			while(counter < iterations){
				List<StockBook> books = stockmanager.getBooksByISBN(isbns);
				if(books.get(0).getNumCopies() == beforeStock){
					for(StockBook b : books){
						if(b.getNumCopies() != beforeStock){
							result.addError();
						}
					}
				}
				else {
					for(StockBook b : books){
						if(b.getNumCopies() != afterStock){
							result.addError();
						}
					}
				}
				counter++;
			}
		} catch (BookStoreException e) {
			e.printStackTrace();
		}
	}
}