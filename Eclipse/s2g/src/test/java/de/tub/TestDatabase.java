package de.tub;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Statement;

import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.util.DBUtil;

/**
 * This is a base test class which can be inherited
 * by test classes in the data domain. It provides 
 * means to connect to the test database before a
 * test is run and to close the connection 
 * after the test was run.
 * 
 * @author Sebastian Oelke
 *
 */
public class TestDatabase {
	
	private static final Logger LOG = LoggerFactory.getLogger(TestDatabase.class);
	
	private static final String COMMENT_LINE_START = "#";
	
	public static void setup() {
		// Setup connection to database with default values
		DBUtil.open();
		// Begin transaction for a test
		Base.openTransaction();
	}
	
	public static void teardown() {
		// Rollback transaction for a test to clean the database
		Base.rollbackTransaction();
		// Close the connection to the database
		DBUtil.close();
	}
	
	/**
	 * Executes all SQL statements in the given file. Empty
	 * lines are ignored. Each statement in the file has to 
	 * fill a single line.
	 * 
	 * @param fileName the file which consists of SQL queries
	 */
	public static void executeStatementsFromFile(String fileName) {
		if (fileName == null || fileName.isEmpty())
			throw new IllegalArgumentException("A valid file name has to be provided.");
		
		// Read out file and test each line for an expected output 
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
		
			String line = "";
			
			// Read lines one by one
			while ((line = br.readLine()) != null) {
				// Only process non-empty lines that are not comments (that start with a hash)
				if (!line.isEmpty() && !line.startsWith(COMMENT_LINE_START)) {
					// Populate database with statements from file
					try {
			            Statement st;
			            st = Base.connection().createStatement();
			            try{
			            	st.executeUpdate(line);
			            }
			            catch(Exception e){
			                throw e;
			            }
			            st.close();
					}
			        catch (Exception e) {
			        	throw new RuntimeException(line, e);
			        }
				} else
					continue;
			}			
		} catch (FileNotFoundException e) {
			LOG.error("The file {} could not be found:\n{}", fileName, e);
		} catch (IOException e) {
			LOG.error("An error occurred while reading the file {}:\n{}", fileName, e);
		}
	}
}
