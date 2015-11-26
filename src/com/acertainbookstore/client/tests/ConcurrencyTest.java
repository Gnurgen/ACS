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
import com.acertainbookstore.business.ConcurrentCertainBookStore;
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
				ConcurrentCertainBookStore store = new ConcurrentCertainBookStore();
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
		int initialStock = 20000;
		int copies = 5;
		int iterations = 2000;
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
		books.add(new BookCopy(TEST_ISBN+1, copies));
		books.add(new BookCopy(TEST_ISBN+2, copies));
		books.add(new BookCopy(TEST_ISBN+3, copies));
		books.add(new BookCopy(TEST_ISBN+4, copies));
		books.add(new BookCopy(TEST_ISBN+5, copies));
		books.add(new BookCopy(TEST_ISBN+6, copies));
		books.add(new BookCopy(TEST_ISBN+7, copies));
		books.add(new BookCopy(TEST_ISBN+8, copies));
		books.add(new BookCopy(TEST_ISBN+9, copies));
		books.add(new BookCopy(TEST_ISBN+10, copies));

		Thread c1 = new Thread(new BuyBooksThread(client, books, iterations));
		Thread c2 = new Thread(new AddCopiesThread(storeManager, books, iterations));
		
		c1.start();
		c2.start();
		
		c1.join();
		c2.join();
		
		List<StockBook> bookResult = storeManager.getBooks();
		
		if(bookResult.size() !=10){
			fail();
		}
		
		boolean result = true;
		searching:
		for (StockBook b : storeManager.getBooks()) {
			if(b.getNumCopies() != initialStock) {
				result = false;
				System.out.println(b.getTitle() + " was wrong");
				break searching;
			}
		}
		assertTrue(result);
	}
	
	@Test
	public void test2() throws BookStoreException, InterruptedException{
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		int initialStock = 50000;
		int copies = 10;
		int iterations = 2000;
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
		books.add(new BookCopy(TEST_ISBN+1, copies));
		books.add(new BookCopy(TEST_ISBN+2, copies));
		books.add(new BookCopy(TEST_ISBN+3, copies));
		books.add(new BookCopy(TEST_ISBN+4, copies));
		books.add(new BookCopy(TEST_ISBN+5, copies));
		books.add(new BookCopy(TEST_ISBN+6, copies));
		books.add(new BookCopy(TEST_ISBN+7, copies));
		books.add(new BookCopy(TEST_ISBN+8, copies));
		books.add(new BookCopy(TEST_ISBN+9, copies));
		books.add(new BookCopy(TEST_ISBN+10, copies));

		Set<Integer> isbns = new HashSet<Integer>();
		isbns.add(TEST_ISBN+1);
		isbns.add(TEST_ISBN+2);
		isbns.add(TEST_ISBN+3);
		isbns.add(TEST_ISBN+4);
		isbns.add(TEST_ISBN+5);
		isbns.add(TEST_ISBN+6);
		isbns.add(TEST_ISBN+7);
		isbns.add(TEST_ISBN+8);
		isbns.add(TEST_ISBN+9);
		isbns.add(TEST_ISBN+10);
		
		Result result = new Result();
		Thread c1 = new Thread(new BuyBooksAddCopiesThread(storeManager,client, books, iterations));
		Thread c2 = new Thread(new GetBooksThread(storeManager, result, isbns, iterations, initialStock, initialStock - copies));
		
		c1.start();
		c2.start();
		
		c1.join();
		c2.join();
		
		assertTrue(result.getErrors() == 0);
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws BookStoreException {
		storeManager.removeAllBooks();
		if (!localTest) {
			((BookStoreHTTPProxy) client).stop();
			((StockManagerHTTPProxy) storeManager).stop();
		}
	}
	
public class Result {
		
		private int errors;
		
		public Result(){
			errors = 0;
		}
		
		public void addError(){
			errors++;
		}
		
		public int getErrors(){
			return errors;
		}
	}
}
