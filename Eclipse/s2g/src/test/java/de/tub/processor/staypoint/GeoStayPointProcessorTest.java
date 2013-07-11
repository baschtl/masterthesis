package de.tub.processor.staypoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.sql.Timestamp;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.tub.TestDatabase;
import de.tub.TestPropertiesLoader;
import de.tub.data.model.StayPoint;
import de.tub.reader.ReaderFactory;
import de.tub.reader.model.UserReader;

public class GeoStayPointProcessorTest {
	public final static String POPULATE_FILE = "/sql/user_staypoint_populate.sql";
	public final static String DELETE_FILE = "/sql/user_staypoint_delete.sql";
	
	private static URL url;
	
	private static final String STAYPOINT_ARR = "2012-09-12 14:00:00";
	private static final String STAYPOINT_LEAV = "2012-09-12 14:31:40";
	private static final double STAYPOINT_LONG = 13.40647;
	private static final double STAYPOINT_LAT = 52.57767;
	
	@Before
	public void populateDatabase() {
		// Retrieve file url to populate data file
		url = GeoStayPointProcessorTest.class.getResource(POPULATE_FILE);
		// Open connection to database
		TestDatabase.setup();
		// Populate database with data
		TestDatabase.executeStatementsFromFile(url.getFile());
	}

	@After
	public void tearDownAfterClass() throws Exception {
		// Retrieve file url to delete data file
		url = GeoStayPointProcessorTest.class.getResource(DELETE_FILE);
		// Delete data from database
		TestDatabase.executeStatementsFromFile(url.getFile());
		// Close connection to database
		TestDatabase.teardown();
	}

	@Test
	public void testDetectStaypoints() {
		// Initialize a reader (the standard reader reads all users from the database)
		UserReader reader = (UserReader) ReaderFactory.instance().getUserReader();
		// Add a processor for detecting stay points
		GeoStayPointProcessor processor = new GeoStayPointProcessor(TestPropertiesLoader.getDistanceThreshold(), TestPropertiesLoader.getTimeThreshold());
		reader.setProcessor(processor);
		
		// Detect stay points
		reader.read();
		
		List<StayPoint> stayPoints = StayPoint.findAll();
		
		// One stay point should have been created based on the test data
		assertEquals("There should be one stay point created.", 1, stayPoints.size());
		
		// Check stay point and its values
		StayPoint sp = stayPoints.get(0);
		
		assertNotNull("The generated stay point should not be null.", sp);
		assertEquals("The latitude value of the generated stay point is not as expected.", STAYPOINT_LAT, sp.getDouble("latitude"), 0.1);
		assertEquals("The longitude value of the generated stay point is not as expected.", STAYPOINT_LONG, sp.getDouble("longitude"), 0.1);
		assertEquals("The arrival time of the generated stay point is not as expected.", Timestamp.valueOf(STAYPOINT_ARR), sp.getTimestamp("arr_time"));
		assertEquals("The leaving time of the generated stay point is not as expected.", Timestamp.valueOf(STAYPOINT_LEAV), sp.getTimestamp("leav_time"));
	}

}
