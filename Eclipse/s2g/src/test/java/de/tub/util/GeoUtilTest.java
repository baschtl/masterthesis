package de.tub.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class GeoUtilTest {

	private static final double lat1 = 38.929854;
	private static final double lat2 = 38.921779;
	private static final double lat3 = 48.865251;
	private static final double long1 = -77.027976;
	private static final double long2 = -77.042057;
	private static final double long3 = 2.330861;
	
	@Test
	public void testDistanceInMeter() {
		// Note: the exact value of the output of the method to test
		// is not important here
		double result = GeoUtil.distanceInMeter(lat1, long1, lat2, long2);
		assertEquals(1513.0, Math.floor(result), (1513.0 / 10e6));
		
		result = GeoUtil.distanceInMeter(lat1, long1, lat3, long3);
		assertEquals(6160838.0, Math.floor(result), (6160838.0 / 10e6));
	}

}
