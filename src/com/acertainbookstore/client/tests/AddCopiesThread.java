package com.acertainbookstore.client.tests;

import java.util.Set;

import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.StockBook;

public class AddCopiesThread implements Runnable{

	private StockManager storemanager;
	private Set<BookCopy> books;
	private int iterations;
	
	public AddCopiesThread(StockManager storemanager, Set<BookCopy> books, int iterations){
		this.storemanager = storemanager;
		this.books = books;
		this.iterations = iterations;
	}
	
	@Override
	public void run() {
		int counter = 0;
		while(counter < iterations){
			try {
				storemanager.addCopies(books);
			} catch (BookStoreException e) {
				e.printStackTrace();
			}
			counter++;
		}
	}
	
	
	
}
