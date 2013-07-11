package de.tub.util;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

public class DateTimeUtilTest {

	public static final String strD1 = "2010-07-24 06:45:10";
	public static final String strD2 = "2010-07-25 04:25:50";
	public static final String strD3 = "2010-07-25 06:45:10";
	public static final String strD4 = "2010-07-25 07:46:10";
	
	public static final long INTERVAL_1_START = 1000L;
	public static final long INTERVAL_1_END = 2000L;
	public static final long INTERVAL_2_START = 1015L;
	public static final long INTERVAL_2_END = 2001L;
	public static final long INTERVAL_3_START = 2001L;
	public static final long INTERVAL_3_END = 2059L;
	
	public static final double TEMPORAL_CONSTRAINT = 0.41;
	public static final long CLUSTER_1_LEAVING = 10005000L;
	public static final long CLUSTER_1_ARRIVAL = 10010000L;
	public static final long CLUSTER_2_LEAVING = 10000000L;
	public static final long CLUSTER_2_ARRIVAL = 10015000L;
	public static final long CLUSTER_3_LEAVING = 10003000L;
	public static final long CLUSTER_3_ARRIVAL = 10008000L;
	
	@Test
	public void testDifferenceInMinutes() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        // Parse the date strings
        Date d1 = null, d2 = null;
        try {
        	d1 = formatter.parse(strD1);
        	d2 = formatter.parse(strD2);
        } catch (ParseException e) {
        	System.err.println("An error occurred parsing the date strings:\n" + e);
		}
        
        assertEquals(1300L, DateTimeUtil.differenceInMinutes(d1, d2));
	}
	
	@Test
	public void testDifferenceInHours() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        // Parse the date strings
        Date d1 = null, d2 = null, d3 = null, d4 = null;
        try {
        	d1 = formatter.parse(strD1);
        	d2 = formatter.parse(strD2);
        	d3 = formatter.parse(strD3);
        	d4 = formatter.parse(strD4);
        } catch (ParseException e) {
        	System.err.println("An error occurred parsing the date strings:\n" + e);
		}
        
        int diff1 = DateTimeUtil.differenceInHours(d1.getTime(), d2.getTime());
        assertEquals(21, diff1);
        
        int diff2 = DateTimeUtil.differenceInHours(d1.getTime(), d3.getTime());
        assertEquals(24, diff2);
        
        int diff3 = DateTimeUtil.differenceInHours(d1.getTime(), d4.getTime());
        assertEquals(25, diff3);
	}
	
	@Test
	public void testDoTimeInervalsOverlap() {
		assertTrue("The given intervals should overlap.", DateTimeUtil.doTimeIntervalsOverlap(INTERVAL_1_START, INTERVAL_1_END, INTERVAL_2_START, INTERVAL_2_END));
		assertFalse("The given intervals should not overlap.", DateTimeUtil.doTimeIntervalsOverlap(INTERVAL_1_START, INTERVAL_1_END, INTERVAL_3_START, INTERVAL_3_END));
	}
	
	@Test
	public void testIsTemporalConstraintRedeemed() {
		assertFalse(DateTimeUtil.isTemporalConstraintRedeemed(CLUSTER_1_ARRIVAL, CLUSTER_1_LEAVING, CLUSTER_2_ARRIVAL, CLUSTER_2_LEAVING, TEMPORAL_CONSTRAINT));
		assertTrue(DateTimeUtil.isTemporalConstraintRedeemed(CLUSTER_1_ARRIVAL, CLUSTER_1_LEAVING, CLUSTER_3_ARRIVAL, CLUSTER_3_LEAVING, TEMPORAL_CONSTRAINT));
	}
	
	@Test
	public void testCurrentDate() {
		// Because this method creates a current date instance it is not possible
		// to test the resulting string, only a test for a non-null and a non-empty
		// result is possible.
		
		// Test null format
		String currentDateString = DateTimeUtil.currentDate(null);
		assertNotNull("The current date string should not be null with a null formatter.", currentDateString);
		assertFalse("The current date string should not be empty with a null formatter.", currentDateString.isEmpty());
		
		// Test empty format
		currentDateString = DateTimeUtil.currentDate("");
		assertNotNull("The current date string should not be null with an empty formatter.", currentDateString);
		assertFalse("The current date string should not be empty with an empty formatter.", currentDateString.isEmpty());
	}

}
