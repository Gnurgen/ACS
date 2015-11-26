package com.acertainbookstore.client.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.CertainBookStore;
import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.client.BookStoreHTTPProxy;
import com.acertainbookstore.client.StockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;

public class ConcurrencyTest {

	private static final int TEST_ISBN = 3044560;
	private static final int NUM_COPIES = 5;
	private static boolean localTest = false;
	private static StockManager storeManager;
	private static BookStore client;

	@BeforeClass
	public static void setUpBeforeClass() {
		try {
			String localTestProperty = System
					.getProperty(BookStoreConstants.PROPERTY_KEY_LOCAL_TEST);
			localTest = (localTestProperty != null) ? Boolean
					.parseBoolean(localTestProperty) : localTest;
			if (localTest) {
				CertainBookStore store = new CertainBookStore();
				storeManager = store;
				client = store;
			} else {
				storeManager = new StockManagerHTTPProxy(
						"http://localhost:8081/stock");
				client = new BookStoreHTTPProxy("http://localhost:8081");
			}
			storeManager.removeAllBooks();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Helper method to add some books
	 */
	public void addBooks(int isbn, int copies) throws BookStoreException {
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		StockBook book = new ImmutableStockBook(isbn, "Test of Thrones",
				"George RR Testin'", (float) 10, copies, 0, 0, 0, false);
		booksToAdd.add(book);
		storeManager.addBooks(booksToAdd);
	}

	/**
	 * Method to clean up the book store, execute after every test case is run
	 */
	@After
	public void cleanupBooks() throws BookStoreException {
		storeManager.removeAllBooks();
	}


	@Test
	public void test1() throws BookStoreException, InterruptedException{
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		int initialStock = 2000;
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 1,
				"This is the book!", "A. Writer", (float) 300,
				initialStock, 0, 0, 0, false));
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 2,
				"Is this a book?", "Writer, Some", (float) 2,
				initialStock, 0, 0, 0, false));
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 3,
				"Studies confirm this is indeed a book...", "B.N. Writer", (float) 900,
				initialStock, 0, 0, 0, false));
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 4,
				"Star Wars, collection", "G. Lucas", (float) 1000,
				initialStock, 0, 0, 0, false));
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 5,
				"A Game of Thrones", "G. R. R., Martin", (float) 100,
				initialStock, 0, 0, 0, false));
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 6,
				"The Wheel of Time, collection", "J. O. Rigney", (float) 100,
				initialStock, 0, 0, 0, false));
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 7,
				"The Name of the Wind", "P. Rothfuss", (float) 150,
				initialStock, 0, 0, 0, false));
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 8,
				"Harry Potter, collection", "J.K. Rowling", (float) 150,
				initialStock, 0, 0, 0, false));
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 9,
				"The Lord of the Rings, collection", "J.R.R. Tolkien", (float) 550,
				initialStock, 0, 0, 0, false));
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 10,
				"The C programming language", "B. Kernighan and D. Ritchie", (float) 50,
				initialStock, 0, 0, 0, false));
		storeManager.addBooks(booksToAdd);
		
		Set<BookCopy> books = new HashSet<BookCopy>();
		books.add(new BookCopy(TEST_ISBN+1, 5));
		books.add(new BookCopy(TEST_ISBN+2, 5));
		books.add(new BookCopy(TEST_ISBN+3, 5));
		books.add(new BookCopy(TEST_ISBN+4, 5));
		books.add(new BookCopy(TEST_ISBN+5, 5));
		books.add(new BookCopy(TEST_ISBN+6, 5));
		books.add(new BookCopy(TEST_ISBN+7, 5));
		books.add(new BookCopy(TEST_ISBN+8, 5));
		books.add(new BookCopy(TEST_ISBN+9, 5));
		books.add(new BookCopy(TEST_ISBN+10, 5));

		Thread c1 = new Thread(new BookStoreThread(client, books, 2000));
		Thread c2 = new Thread(new StockManagerThread(storeManager, books, 2000));
		
		c1.start();
		c2.start();
		
		c1.join();
		c2.join();
		
		List<StockBook> bookResult = storeManager.getBooks();
		System.out.println(bookResult.size());
		for(StockBook b : bookResult){
			System.out.println(b.getTitle());
		}
		
		if(bookResult.size() !=10){
			fail();
		}
		
		boolean result = true;
		searching:
		for (StockBook b : storeManager.getBooks()) {
			if(b.getNumCopies() != initialStock) {
				result = false;
				break searching;
			}
		}
		assertTrue(result);
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws BookStoreException {
		storeManager.removeAllBooks();
		if (!localTest) {
			((BookStoreHTTPProxy) client).stop();
			((StockManagerHTTPProxy) storeManager).stop();
		}
	}
}
