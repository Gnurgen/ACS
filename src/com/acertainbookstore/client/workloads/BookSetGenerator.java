package com.acertainbookstore.client.workloads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;

/**
 * Helper class to generate stockbooks and isbns modelled similar to Random
 * class
 */
public class BookSetGenerator {

	public BookSetGenerator() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Returns num randomly selected isbns from the input set
	 * 
	 * @param num
	 * @return
	 */
	public Set<Integer> sampleFromSetOfISBNs(Set<Integer> isbns, int num) {
		List<Integer> list = new LinkedList<Integer>(isbns);
		Collections.shuffle(list);
		Set<Integer> randset = new HashSet<>(list.subList(0, num));
		return randset;
	}

	/**
	 * Return num stock books. For now return an ImmutableStockBook
	 * The new ISBN is randomly generated, wich should result in few clashes with
	 * existing ISBNs.
	 * @param num
	 * @return
	 */
	public Set<StockBook> nextSetOfStockBooks(int num) {
		Random r = new Random();
		Set<StockBook> books = new HashSet<StockBook>();
		for (int i=1; i<num+1; i++){
			// get a random 7 digit number
			int ISBN = (int)((Math.random() * 9000000)+1000000);
			String title = "";
			int price = 0;
			String author = "";
			int numCopies = 0;
			int SaleMisses = 0;
			int timesRated = 0;
			int totalRating = 0; 
			boolean editorPick = false;
			StockBook book = new ImmutableStockBook(ISBN, title, author, price, numCopies, SaleMisses, timesRated, totalRating, editorPick);
			books.add(book);
		}
		return books;
	}

}
