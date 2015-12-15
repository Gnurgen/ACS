package com.acertainbookstore.client.workloads;

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
	private String characters = "abcdefghijklmnopqrstuvwxyz ";
	
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
		Set<Integer> randset = new HashSet<>(list.subList(0, num-1));
		return randset;
	}

	/**
	 * Return num stock books. For now return an ImmutableStockBook
	 * 
	 * @param num
	 * @return
	 */
	public Set<StockBook> nextSetOfStockBooks(int num) {
		Random r = new Random();
		Set<StockBook> books = new HashSet<StockBook>();
		for (int i=1; i<num+1; i++){
			// get a random 10 digit number
			int ISBN = r.nextInt(1000000000);
			String title = randomString(10,r);
			int price = 100+r.nextInt(500);
			String author = randomString(10,r);
			int numCopies = 1000+r.nextInt(1000);
			int SaleMisses = 0;
			int timesRated = 0;
			int totalRating = 0;
			boolean editorPick = r.nextBoolean();
			StockBook book = new ImmutableStockBook(ISBN, title, author, price, numCopies, SaleMisses, timesRated, totalRating, editorPick);
			books.add(book);
		}
		return books;
	}
	
	private String randomString(int length, Random r){
		String result = "";
		for(int i = 0; i < length; i++){
			result += characters.toCharArray()[r.nextInt(characters.length())];
		}
		return result;
	}
}
