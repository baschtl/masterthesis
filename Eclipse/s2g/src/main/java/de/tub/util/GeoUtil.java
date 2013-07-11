package de.tub.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.data.geo.GeoPointWrapper;
import de.tub.data.model.GeoPoint;

/**
 * This class provides convenience methods for geographic
 * calculations.
 * 
 * @author Sebastian Oelke
 *
 */
public class GeoUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(GeoUtil.class);
	
	// Approximate earth radius in meters
	public static final double EARTH_RADIUS = 6371000;

	/**
	 * Computes the great-circle distance of two geographic points
	 * using the haversine formula. The returned value is in meters.
	 * <p />
	 * References:<br />
	 * R. W. Sinnott, "Virtues of the Haversine", Sky and Telescope 68 (2), 159 (1984)<br />
	 * http://www.movable-type.co.uk/scripts/gis-faq-5.1.html<br />
	 * http://en.wikipedia.org/wiki/Great-circle_distance
	 * 
	 * @param lat1 latitude of the first point
	 * @param long1 longitude of the first point
	 * @param lat2 latitude of the first point
	 * @param long2 longitude of the first point
	 * @return the great-circle distance of two geographic points in meters
	 */
	public static double distanceInMeter(double lat1, double long1, double lat2, double long2) {
		// Compute difference of latitude, longitude and convert to radians
		double diffLat = Math.toRadians(lat2 - lat1);
		double diffLong = Math.toRadians(long2 - long1);
		
		// Convert single latitude values to radiant
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		
		double diffLatSin = Math.sin(diffLat/2);
		double diffLongSin = Math.sin(diffLong/2);
		
		double a = 	diffLatSin * diffLatSin +
					diffLongSin * diffLongSin *
					Math.cos(lat1) * Math.cos(lat2);
		
		double greatCircleDistanceRad = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		
		return EARTH_RADIUS * greatCircleDistanceRad;
	}
	
	/**
	 * Computes the mean longitude and latitude values for the given
	 * points.
	 * 
	 * @param items the points to compute the mean latitude and longitude for
	 * @return the mean latitude and longitude wrapped in an instance of <code>GeoPointWrapper</code>
	 */
	public static GeoPointWrapper computeMeanPosition(List<GeoPoint> items) {
		double meanLatitude = 0.0, meanLongitude = 0.0;
		int itemsSize = items.size();
		
		for (int i = 0; i < itemsSize; i++) {
			GeoPoint p = items.get(i);
			meanLatitude += p.getDouble("latitude");
			meanLongitude += p.getDouble("longitude");
		}
		
		// Compute mean
		meanLatitude = meanLatitude / itemsSize;
		meanLongitude = meanLongitude / itemsSize;
		
		return new GeoPointWrapper(meanLatitude, meanLongitude);
	}
	
}
