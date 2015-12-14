/**
 * 
 */
package com.acertainbookstore.client.workloads;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;

/**
 * 
 * Worker represents the workload runner which runs the workloads with
 * parameters using WorkloadConfiguration and then reports the results
 * 
 */
public class Worker implements Callable<WorkerRunResult> {
	private WorkloadConfiguration configuration = null;
	private int numSuccessfulFrequentBookStoreInteraction = 0;
	private int numTotalFrequentBookStoreInteraction = 0;
	private StockManager stockManager = null;
	private BookStore bookStore = null;
	private BookSetGenerator generator = null;

	public Worker(WorkloadConfiguration config) {
		configuration = config;
		stockManager = config.getStockManager();
		bookStore = config.getBookStore();
		generator = new BookSetGenerator();
	}

	/**
	 * Run the appropriate interaction while trying to maintain the configured
	 * distributions
	 * 
	 * Updates the counts of total runs and successful runs for customer
	 * interaction
	 * 
	 * @param chooseInteraction
	 * @return
	 */
	private boolean runInteraction(float chooseInteraction) {
		try {
			if (chooseInteraction < configuration
					.getPercentRareStockManagerInteraction()) {
				runRareStockManagerInteraction();
			} else if (chooseInteraction < configuration
					.getPercentFrequentStockManagerInteraction()) {
				runFrequentStockManagerInteraction();
			} else {
				numTotalFrequentBookStoreInteraction++;
				runFrequentBookStoreInteraction();
				numSuccessfulFrequentBookStoreInteraction++;
			}
		} catch (BookStoreException ex) {
			return false;
		}
		return true;
	}

	/**
	 * Run the workloads trying to respect the distributions of the interactions
	 * and return result in the end
	 */
	public WorkerRunResult call() throws Exception {
		int count = 1;
		long startTimeInNanoSecs = 0;
		long endTimeInNanoSecs = 0;
		int successfulInteractions = 0;
		long timeForRunsInNanoSecs = 0;

		Random rand = new Random();
		float chooseInteraction;

		// Perform the warmup runs
		while (count++ <= configuration.getWarmUpRuns()) {
			chooseInteraction = rand.nextFloat() * 100f;
			runInteraction(chooseInteraction);
		}

		count = 1;
		numTotalFrequentBookStoreInteraction = 0;
		numSuccessfulFrequentBookStoreInteraction = 0;

		// Perform the actual runs
		startTimeInNanoSecs = System.nanoTime();
		while (count++ <= configuration.getNumActualRuns()) {
			chooseInteraction = rand.nextFloat() * 100f;
			if (runInteraction(chooseInteraction)) {
				successfulInteractions++;
			}
		}
		endTimeInNanoSecs = System.nanoTime();
		timeForRunsInNanoSecs += (endTimeInNanoSecs - startTimeInNanoSecs);
		return new WorkerRunResult(successfulInteractions,
				timeForRunsInNanoSecs, configuration.getNumActualRuns(),
				numSuccessfulFrequentBookStoreInteraction,
				numTotalFrequentBookStoreInteraction);
	}

	/**
	 * Runs the new stock acquisition interaction
	 * 
	 * @throws BookStoreException
	 */
	private void runRareStockManagerInteraction() throws BookStoreException {
		List<StockBook> books = stockManager.getBooks();
		List<Integer> isbns = null;
		for (StockBook b : books) {
			isbns.add(b.getISBN());
		}

		Set<StockBook> randbooks = generator.nextSetOfStockBooks(configuration.getNumBooksToAdd());
		
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		
		for (StockBook b : randbooks){
			if (!isbns.contains(b.getISBN())){
				booksToAdd.add(b);
			}
		}		
		stockManager.addBooks(booksToAdd);
	}

	/**
	 * Runs the stock replenishment interaction
	 * 
	 * @throws BookStoreException
	 */
	private void runFrequentStockManagerInteraction() throws BookStoreException {
		List<StockBook> books = stockManager.getBooks();
		
		List<StockBook> temp = new LinkedList<>();
		
        for(StockBook b : books){
            if(temp.size() < configuration.getNumBooksWithLeastCopies()){
                temp.add(b);
            }
            else {
                StockBook lowest = lowestCopies(temp);
                if(b.getAverageRating() > lowest.getAverageRating()){
                    temp.remove(lowest);
                    temp.add(b);
                }
            }
        }
        Set<BookCopy> booksToAdd = new HashSet<>();
        for(StockBook b : temp){
        	BookCopy book = new BookCopy(b.getISBN(), configuration.getNumAddCopies());
        	booksToAdd.add(book);
        }
        stockManager.addCopies(booksToAdd);
	}
	
	private StockBook lowestCopies(List<StockBook> books){
	    StockBook result = books.get(0);
	    for(StockBook b : books){
	        if(b.getNumCopies() < result.getNumCopies()){
	            result = b;
	        }
	    }
	    return result;	}

	/**
	 * Runs the customer interaction
	 * 
	 * @throws BookStoreException
	 */
	private void runFrequentBookStoreInteraction() throws BookStoreException {
		List<Book> books = bookStore.getEditorPicks(configuration.getNumEditorPicksToGet());
		Set<Integer> isbns = new HashSet<Integer>();
		for (Book b : books){
			isbns.add(b.getISBN());
		}
		
		Set<Integer> temp = generator.sampleFromSetOfISBNs(isbns, configuration.getNumBookCopiesToBuy());
		
		Set<BookCopy> booksToBuy = new HashSet<BookCopy>();
		for(Integer i : temp){
			BookCopy b = new BookCopy(i, configuration.getNumBookCopiesToBuy());
			booksToBuy.add(b);
		}
		bookStore.buyBooks(booksToBuy);
	}

}
