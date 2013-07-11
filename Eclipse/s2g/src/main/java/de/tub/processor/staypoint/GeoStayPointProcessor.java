package de.tub.processor.staypoint;

import java.sql.Timestamp;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.data.geo.GeoPointWrapper;
import de.tub.data.model.GeoPoint;
import de.tub.data.model.StayPoint;
import de.tub.data.model.User;
import de.tub.observer.Interests;
import de.tub.observer.Observer;
import de.tub.observer.Subject;
import de.tub.processor.IProcessor;
import de.tub.processor.ISimulator;
import de.tub.util.DateTimeUtil;
import de.tub.util.GeoUtil;

/**
 * This processor takes user entities and assesses, based on the
 * geographic points of the each user, their stay points.
 * <p />
 * As this processor implements the <code>ISimulator</code> interface
 * it can be used in simulation mode in which the stay points of each 
 * user are summed up and can be accessed after the processing of
 * every user. The computed stay points are <u>not</u> persisted 
 * while in simulation mode. 
 * 
 * @author Sebastian Oelke
 *
 */
public class GeoStayPointProcessor implements IProcessor<User>, ISimulator<Integer>, Observer {

	private static final Logger LOG = LoggerFactory.getLogger(GeoStayPointProcessor.class);

	private int distanceThreshold, timeThreshold, stayPointsOfUser;
	private boolean simulation = false;
	
	/**
	 * With this constructor the distance threshold as well as the
	 * time threshold has to be defined. The simulation mode is 
	 * turned off by default.
	 * 
	 * @param distanceThreshold the maximum distance two locations/points of a user
	 * can be apart and still be detected as a stay point.
	 * @param timeThreshold the time a user has to stay within a certain area to be identified as
	 * a stay point. 
	 */
	public GeoStayPointProcessor(int distanceThreshold, int timeThreshold) {
		this.distanceThreshold = distanceThreshold;
		this.timeThreshold = timeThreshold;
	}
	
	public GeoStayPointProcessor(int distanceThreshold, int timeThreshold, boolean simulation) {
		this(distanceThreshold, timeThreshold);
		
		this.simulation = simulation;
	}
	
	@Override
	public void newData(User data) {
		// Return if the reference of the data is null
		if (data == null) return;
		
		// Process the given data
		processData(data);
	}

	@Override
	public void finish() {	}
	
	private void processData(User data) {
		LOG.debug("Process user: {}", data);
		if (simulation) stayPointsOfUser = 0;
		
		// Get all geo points of a user
		List<GeoPoint> points = GeoPoint.where("user_id = ?", data.getString("id")).orderBy("recorded_at asc");
		int pointsSize = points.size();
		
		LOG.debug("Found {} points.", pointsSize);
		
		for (int i = 0; i < pointsSize - 1; i++) {
			// First point to check
			GeoPoint point1 = points.get(i);
			double lat1 = point1.getDouble("latitude");
			double long1 = point1.getDouble("longitude");
			
			for (int j = i + 1; j < pointsSize; j++) {
				// Second point to check
				GeoPoint point2 = points.get(j);
				
				// Calculate distance between two points				
				double lat2 = point2.getDouble("latitude");
				double long2 = point2.getDouble("longitude");
				
				// Compute distance in meters between geographic values of point1 and point2
				double distance = GeoUtil.distanceInMeter(lat1, long1, lat2, long2);
				LOG.debug("Distance in meters: {}", distance);
				
				// The user has to stay within the distance threshold
				if (distance > distanceThreshold) {
					// Compute time the user stayed within the distance threshold
					Timestamp p1TimeStamp = point1.getTimestamp("recorded_at");
					Timestamp p2TimeStamp = point2.getTimestamp("recorded_at");
					long differenceInMinutes = DateTimeUtil.differenceInMinutes(p1TimeStamp, p2TimeStamp);
					
					LOG.debug("Difference in minutes: {}", differenceInMinutes);
					// The user has to stay within the distance threshold at least for the time threshold
					if (differenceInMinutes > timeThreshold) {
						// Compute the mean of latitude and longitude for a stay point
						GeoPointWrapper meanGeoPoint = GeoUtil.computeMeanPosition(points.subList(i, j+1));
						
						// Test if in simulation mode
						if (simulation) {
							stayPointsOfUser++;
						}
						else {
							// Create stay point and add it to current user
							StayPoint s = new StayPoint();
							s.set("latitude", meanGeoPoint.getMeanLatitude()).
								set("longitude", meanGeoPoint.getMeanLongitude()).
								set("arr_time", p1TimeStamp).
								set("leav_time", p2TimeStamp);
							data.add(s);
							
							LOG.debug("Created: {}.", s);
						}
					}
					i = j - 1;
					break;
				}
			}
		}
	}
	
	@Override
	public void update(Subject theSubject, Interests interest, Object arg) {
		LOG.warn("This processor does not support state changes of a subject, yet.");
	}
	
	//###################################################################
	// Setters & Getters
	//###################################################################

	@Override
	public boolean isSimulation() {
		return simulation;
	}

	@Override
	public void setSimulation(boolean simulate) {
		this.simulation = simulate;
	}

	/**
	 * Returns the number of stay points detected for the given user
	 * after processing it in simulation mode.  
	 * 
	 * @return the stay points of the given user.
	 */
	@Override
	public Integer getSimulationData() {
		return stayPointsOfUser;
	}
	
}
