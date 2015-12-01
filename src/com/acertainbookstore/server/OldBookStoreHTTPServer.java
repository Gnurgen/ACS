/**
 * 
 */
package com.acertainbookstore.server;

import com.acertainbookstore.business.CertainBookStore;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.server.OldBookStoreHTTPMessageHandler;

/**
 * Starts the bookstore HTTP server that the clients will communicate with.
 */
public class OldBookStoreHTTPServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CertainBookStore bookStore = new CertainBookStore();
		int listen_on_port = 8082;
		OldBookStoreHTTPMessageHandler handler = new OldBookStoreHTTPMessageHandler(
				bookStore);
		String server_port_string = System.getProperty(BookStoreConstants.PROPERTY_KEY_SERVER_PORT);
		if(server_port_string != null) {
			try {
				listen_on_port = Integer.parseInt(server_port_string);
			} catch(NumberFormatException ex) {
				System.err.println(ex);
			}
		}
		if (BookStoreHTTPServerUtility.createServer(listen_on_port, handler)) {
			;
		}
	}

}
