package de.tub.data.dao;


/**
 * This interface provides methods to access Stay Points.
 * The <code>IStaypointDAO</code> is part
 * of the Data Access Object design pattern.
 * 
 * @author Sebastian Oelke
 *
 */
public interface IStaypointDAO<E> extends IDao {

	/**
	 * Creates and saves a Stay Point.
	 *  
	 * @param id the identifier of this Stay Point.
	 * @param latitude the latitude of this Stay Point.
	 * @param longitude the longitude of this Stay Point.
	 * @param arrivalTime the arrival time of this Stay Point in milliseconds (e.g., Date.getTime()).
	 * @param leavingTime the leaving time of this Stay Point in milliseconds (e.g., Date.getTime()).
	 * @return the created node or <code>null</code> if the node could not be created.
	 */
	E createStayPoint(int id, double latitude, double longitude, 
							long arrivalTime, long leavingTime);
	
	/**
	 * Deletes the Stay Point with the given id.
	 *  
	 * @param id the identifier of the Stay Point to delete.
	 */
	void deleteStayPoint(int id);
	
	/**
	 * Finds a Stay Point by means of its identifier.
	 * 
	 * @param id the identifier of the Stay Point to find.
	 * @return the Stay Point with the given identifier or <code>null</code> if no Stay Point was found.
	 */
	E findStayPointById(int id);
}
