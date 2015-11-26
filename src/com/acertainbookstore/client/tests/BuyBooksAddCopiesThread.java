package com.acertainbookstore.client.tests;

import java.util.Set;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;

public class BuyBooksAddCopiesThread implements Runnable{

	private StockManager storemanager;
	private BookStore bookstore;
	private Set<BookCopy> books;
	private int iterations;
	
	public BuyBooksAddCopiesThread(StockManager storemanager, BookStore bookstore, Set<BookCopy> books, int iterations){
		this.storemanager = storemanager;
		this.books = books;
		this.iterations = iterations;
		this.bookstore = bookstore;
	}
	
	@Override
	public void run() {
		int counter = 0;
		while(counter < iterations){
			try {
				bookstore.buyBooks(books);
				storemanager.addCopies(books);
			} catch (BookStoreException e) {
				e.printStackTrace();
			}
			counter++;
		}
	}

}
