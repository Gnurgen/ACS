/**
 * 
 */
package com.acertainbookstore.business;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreUtility;
import com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory;

/**
 * CertainBookStore implements the bookstore and its functionality which is
 * defined in the BookStore
 */
public class ConcurrentCertainBookStore implements BookStore, StockManager {
	private Map<Integer, BookStoreBook> bookMap = null;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

	public ConcurrentCertainBookStore() {
		// Constructors are not synchronized
		bookMap = new HashMap<Integer, BookStoreBook>();
	}
	
	// Synchronize!
	public void addBooks(Set<StockBook> bookSet)
			throws BookStoreException {

		if (bookSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}
		w.lock();
		// Check if all are there
		for (StockBook book : bookSet) {
			int ISBN = book.getISBN();
			String bookTitle = book.getTitle();
			String bookAuthor = book.getAuthor();
			int noCopies = book.getNumCopies();
			float bookPrice = book.getPrice();
			if (BookStoreUtility.isInvalidISBN(ISBN)
					|| BookStoreUtility.isEmpty(bookTitle)
					|| BookStoreUtility.isEmpty(bookAuthor)
					|| BookStoreUtility.isInvalidNoCopies(noCopies)
					|| bookPrice < 0.0) {
				w.unlock();
				throw new BookStoreException(BookStoreConstants.BOOK
						+ book.toString() + BookStoreConstants.INVALID);
			} else if (bookMap.containsKey(ISBN)) {
				w.unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN
						+ BookStoreConstants.DUPLICATED);
			}
		}

		for (StockBook book : bookSet) {
			int ISBN = book.getISBN();
			bookMap.put(ISBN, new BookStoreBook(book));
		}
		w.unlock();
	}

	// Synchronize!
	public void addCopies(Set<BookCopy> bookCopiesSet)
			throws BookStoreException {
		int ISBN, numCopies;

		if (bookCopiesSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}
        w.lock();
		for (BookCopy bookCopy : bookCopiesSet) {
			ISBN = bookCopy.getISBN();
			numCopies = bookCopy.getNumCopies();
			if (BookStoreUtility.isInvalidISBN(ISBN)){
				w.unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN
						+ BookStoreConstants.INVALID);
			}
			if (!bookMap.containsKey(ISBN)){
				w.unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN
						+ BookStoreConstants.NOT_AVAILABLE);
			}
			if (BookStoreUtility.isInvalidNoCopies(numCopies)){
				w.unlock();
				throw new BookStoreException(BookStoreConstants.NUM_COPIES
						+ numCopies + BookStoreConstants.INVALID);
			}

		}

		BookStoreBook book;
		// Update the number of copies
		for (BookCopy bookCopy : bookCopiesSet) {
			ISBN = bookCopy.getISBN();
			numCopies = bookCopy.getNumCopies();
			book = bookMap.get(ISBN);
			book.addCopies(numCopies);
		}
		w.unlock();
	}

	// Synchronize!
	public List<StockBook> getBooks() {
		List<StockBook> listBooks = new ArrayList<StockBook>();
		r.lock();
		Collection<BookStoreBook> bookMapValues = bookMap.values();
		r.unlock();
		for (BookStoreBook book : bookMapValues) {
			listBooks.add(book.immutableStockBook());
		}
		return listBooks;
	}

	// Synchronize!
	public void updateEditorPicks(Set<BookEditorPick> editorPicks)
			throws BookStoreException {
		// Check that all ISBNs that we add/remove are there first.
		if (editorPicks == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		int ISBNVal;
        
		w.lock();
		for (BookEditorPick editorPickArg : editorPicks) {
			ISBNVal = editorPickArg.getISBN();
			if (BookStoreUtility.isInvalidISBN(ISBNVal)){
				w.unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + ISBNVal
						+ BookStoreConstants.INVALID);
			}
			if (!bookMap.containsKey(ISBNVal)){
				w.unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + ISBNVal
						+ BookStoreConstants.NOT_AVAILABLE);
			}
		}

		for (BookEditorPick editorPickArg : editorPicks) {
			bookMap.get(editorPickArg.getISBN()).setEditorPick(
					editorPickArg.isEditorPick());
		}
		w.unlock();
	}

	// Synchronize!
	public void buyBooks(Set<BookCopy> bookCopiesToBuy)
			throws BookStoreException {
		if (bookCopiesToBuy == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		// Check that all ISBNs that we buy are there first.
		int ISBN;
		BookStoreBook book;
		Boolean saleMiss = false;
		
		w.lock();
		for (BookCopy bookCopyToBuy : bookCopiesToBuy) {
			ISBN = bookCopyToBuy.getISBN();
			if (bookCopyToBuy.getNumCopies() < 0){
				w.unlock();
				throw new BookStoreException(BookStoreConstants.NUM_COPIES
						+ bookCopyToBuy.getNumCopies()
						+ BookStoreConstants.INVALID);
			}
			if (BookStoreUtility.isInvalidISBN(ISBN)){
				w.unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN
						+ BookStoreConstants.INVALID);
			}
			if (!bookMap.containsKey(ISBN)){
				w.unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN
						+ BookStoreConstants.NOT_AVAILABLE);
			}
			book = bookMap.get(ISBN);
			if (!book.areCopiesInStore(bookCopyToBuy.getNumCopies())) {
				book.addSaleMiss(); // If we cannot sell the copies of the book
									// its a miss
				saleMiss = true;
			}
		}

		// We throw exception now since we want to see how many books in the
		// order incurred misses which is used by books in demand
		if (saleMiss){
			w.unlock();
			throw new BookStoreException(BookStoreConstants.BOOK
					+ BookStoreConstants.NOT_AVAILABLE);
		}

		// Then make purchase
		for (BookCopy bookCopyToBuy : bookCopiesToBuy) {
			book = bookMap.get(bookCopyToBuy.getISBN());
			book.buyCopies(bookCopyToBuy.getNumCopies());
		}
		w.unlock();
	}

	// Synchronize!
	public List<StockBook> getBooksByISBN(Set<Integer> isbnSet)
			throws BookStoreException {
		if (isbnSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}
		r.lock();
		for (Integer ISBN : isbnSet) {
			if (BookStoreUtility.isInvalidISBN(ISBN)){
				r.unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN
						+ BookStoreConstants.INVALID);
			}
			if (!bookMap.containsKey(ISBN)){
				r.unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN
						+ BookStoreConstants.NOT_AVAILABLE);
			}
		}

		List<StockBook> listBooks = new ArrayList<StockBook>();

		for (Integer ISBN : isbnSet) {
			listBooks.add(bookMap.get(ISBN).immutableStockBook());
		}
		r.unlock();

		return listBooks;
	}

	// Synchronize!
	public List<Book> getBooks(Set<Integer> isbnSet)
			throws BookStoreException {
		if (isbnSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}
		// Check that all ISBNs that we rate are there first.
		r.lock();
		for (Integer ISBN : isbnSet) {
			if (BookStoreUtility.isInvalidISBN(ISBN)){
				r.unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN
						+ BookStoreConstants.INVALID);
			}
			if (!bookMap.containsKey(ISBN)){
				r.unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN
						+ BookStoreConstants.NOT_AVAILABLE);
			}
		}

		List<Book> listBooks = new ArrayList<Book>();

		// Get the books
		for (Integer ISBN : isbnSet) {
			listBooks.add(bookMap.get(ISBN).immutableBook());
		}
		r.unlock();
		return listBooks;
	}

	// Synchronize!
	public List<Book> getEditorPicks(int numBooks)
			throws BookStoreException {
		if (numBooks < 0) {
			throw new BookStoreException("numBooks = " + numBooks
					+ ", but it must be positive");
		}

		List<BookStoreBook> listAllEditorPicks = new ArrayList<BookStoreBook>();
		List<Book> listEditorPicks = new ArrayList<Book>();
		
		r.lock();
		Iterator<Entry<Integer, BookStoreBook>> it = bookMap.entrySet()
				.iterator();
		BookStoreBook book;
		r.unlock();
		
		// Get all books that are editor picks
		while (it.hasNext()) {
			Entry<Integer, BookStoreBook> pair = (Entry<Integer, BookStoreBook>) it
					.next();
			book = (BookStoreBook) pair.getValue();
			if (book.isEditorPick()) {
				listAllEditorPicks.add(book);
			}
		}

		// Find numBooks random indices of books that will be picked
		Random rand = new Random();
		Set<Integer> tobePicked = new HashSet<Integer>();
		int rangePicks = listAllEditorPicks.size();
		if (rangePicks <= numBooks) {
			// We need to add all the books
			for (int i = 0; i < listAllEditorPicks.size(); i++) {
				tobePicked.add(i);
			}
		} else {
			// We need to pick randomly the books that need to be returned
			int randNum;
			while (tobePicked.size() < numBooks) {
				randNum = rand.nextInt(rangePicks);
				tobePicked.add(randNum);
			}
		}

		// Get the numBooks random books
		for (Integer index : tobePicked) {
			book = listAllEditorPicks.get(index);
			listEditorPicks.add(book.immutableBook());
		}
		return listEditorPicks;

	}

	// Synchronize!
	@Override
	public List<Book> getTopRatedBooks(int numBooks)
			throws BookStoreException {
		if(numBooks < 1) {
			throw new BookStoreException(BookStoreConstants.INVALID_NUM);
		}
		r.lock();
		if(bookMap.size() < numBooks){
			r.unlock();
			throw new BookStoreException(BookStoreConstants.INSUFFICIENT_BOOKS);
		}
		
		List<BookStoreBook> temp = new ArrayList<BookStoreBook>();
		for(BookStoreBook b : bookMap.values()){
			if(temp.size() < numBooks){
				temp.add(b);
			}
			else {
				BookStoreBook lowest = lowestRated(temp);
				if(b.getAverageRating() > lowest.getAverageRating()){
					temp.remove(lowest);
					temp.add(b);
				}
			}
		}
		r.unlock();
		ArrayList<Book> result = new ArrayList<Book>();
		for(BookStoreBook b : temp){
			result.add(b.immutableBook());
		}
		return result;
	}
	
	private BookStoreBook lowestRated(List<BookStoreBook> books){
		BookStoreBook result = books.get(0);
		for(BookStoreBook b : books){
			if(b.getAverageRating() < result.getAverageRating()){
				result = b;
			}
		}
		return result;
	}

	// Synchronize!
	/*
	 * Does not throw any errors, as an empty bookstore is the same
	 * as no books in demand.
	 */
	@Override
	public List<StockBook> getBooksInDemand()
			throws BookStoreException {
		ArrayList<BookStoreBook> temp = new ArrayList<BookStoreBook>();
		
		r.lock();
		for(BookStoreBook b : bookMap.values()){
			if(b.getSaleMisses() > 0){
				temp.add(b);
			}
		}
		r.unlock();
		
		ArrayList<StockBook> result = new ArrayList<StockBook>();
		for(BookStoreBook b : temp){
			result.add(b.immutableStockBook());
		}
		return result;
	}

	// Synchronize!
	@Override
	public void rateBooks(Set<BookRating> bookRating)
			throws BookStoreException {
		
		w.lock();
		for(BookRating br : bookRating){
			if(!bookMap.containsKey(br.getISBN())){
				w.unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + br.getISBN() + BookStoreConstants.INVALID);
			}
			if(br.getRating() > 5 || br.getRating() < 0){
				w.unlock();
				throw new BookStoreException(BookStoreConstants.INVALID_RATING);
			}
		}
		for(BookRating br : bookRating){
			bookMap.get(br.getISBN()).addRating(br.getRating());
		}
		w.unlock();
	}

	// Synchronize!
	public void removeAllBooks() throws BookStoreException {
		w.lock();
		bookMap.clear();
		w.unlock();
	}

	// Synchronize!
	public void removeBooks(Set<Integer> isbnSet)
			throws BookStoreException {

		if (isbnSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}
		w.lock();
		for (Integer ISBN : isbnSet) {
			if (BookStoreUtility.isInvalidISBN(ISBN)){
				w.unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN
						+ BookStoreConstants.INVALID);
			}
			if (!bookMap.containsKey(ISBN)){
				w.unlock();
				throw new BookStoreException(BookStoreConstants.ISBN + ISBN
						+ BookStoreConstants.NOT_AVAILABLE);
			}
		}

		for (int isbn : isbnSet) {
			bookMap.remove(isbn);
		}
		w.unlock();
	}
}