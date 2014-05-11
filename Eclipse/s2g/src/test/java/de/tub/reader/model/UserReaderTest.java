package de.tub.reader.model;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.tub.TestDatabase;
import de.tub.data.model.User;
import de.tub.reader.ReaderFactory;
import de.tub.reader.SimpleUserProcessor;
import de.tub.reader.model.UserReader;

public class UserReaderTest {
	public final static String POPULATE_FILE = "/sql/user_populate.sql";
	public final static String DELETE_FILE = "/sql/user_delete.sql";
	
	private static URL url;
	private static List<Long> userIds = new ArrayList<Long>();
	
	@Before
	public void populateDatabase() {
		// Retrieve file url to populate data file
		url = UserReaderTest.class.getResource(POPULATE_FILE);
		// Open database connection
		TestDatabase.setup();
		// Populate database with data
		TestDatabase.executeStatementsFromFile(url.getFile());
		
		// Set ids of users which have to be read in
		userIds.add(1L);
		userIds.add(2L);
		userIds.add(3L);
	}
	
	@After
	public void tearDownAfterClass() throws Exception {
		// Retrieve file url to delete data file
		url = UserReaderTest.class.getResource(DELETE_FILE);
		// Delete data from database
		TestDatabase.executeStatementsFromFile(url.getFile());
		// Close database connection
		TestDatabase.teardown();
	}
	
	@Test
	public void testReadUsers() {
		// Create user reader
		UserReader ur = (UserReader) ReaderFactory.instance().getUserReader();
		// Create a simple processor
		SimpleUserProcessor up = new SimpleUserProcessor();
		ur.setProcessor(up);
		
		// Read users from database
		ur.read();
		
		// Test read users
		List<User> users = up.getUsers();
		
		assertEquals("The wrong number of users was read.", 3, users.size());
		
		// Test existence of users by id
    	for (int i = 0; i < users.size(); i++) {
    		long thisUserId = (Long) users.get(i).getId();
    		
    		for (int j = 0; j < userIds.size(); j++) {
    			if (userIds.contains(thisUserId))
    				userIds.remove(userIds.indexOf(thisUserId));
    		}
    	}
    	assertEquals("All expected users should have been found.", 0, userIds.size());
    	
	}

}
