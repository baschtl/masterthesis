package de.tub.data.geo;

/**
 * This class wraps the latitude and longitude values of a 
 * geographic point.
 * 
 * @author Sebastian Oelke
 *
 */
public class GeoPointWrapper {
	private double meanLatitude, meanLongitude;
	
	public GeoPointWrapper(double meanLatitude, double meanLongitude) {
		this.meanLatitude = meanLatitude;
		this.meanLongitude = meanLongitude;
	}
	
	//###################################################################
	// Setters & Getters
	//###################################################################

	public double getMeanLatitude() {
		return meanLatitude;
	}

	public void setMeanLatitude(double meanLatitude) {
		this.meanLatitude = meanLatitude;
	}

	public double getMeanLongitude() {
		return meanLongitude;
	}

	public void setMeanLongitude(double meanLongitude) {
		this.meanLongitude = meanLongitude;
	}
}
