package com.acertainbookstore.client.tests;

import java.util.Set;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.utils.BookStoreException;

public class BookStoreThread implements Runnable{

	private BookStore bookstore;
	private Set<BookCopy> books;
	private int iterations;
	
	public BookStoreThread(BookStore bookstore, Set<BookCopy> books, int iterations){
		this.bookstore = bookstore;
		this.books = books;
		this.iterations = iterations;
	}
	
	@Override
	public void run() {
		int counter = 0;
		while(counter < iterations){
			try {
				bookstore.buyBooks(books);
			} catch (BookStoreException e) {
				e.printStackTrace();
			}
			counter++;
		}
	}

}
