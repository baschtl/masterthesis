package de.tub.data.dao;

import java.util.Map;


/**
 * This interface provides methods to access Framework
 * cluster. The <code>IFrameworkClusterDAO</code> is part
 * of the Data Access Object design pattern.
 * 
 * @author Sebastian Oelke
 *
 */
public interface IHGClusterDAO<E> extends IDao {

	/**
	 * Creates a cluster as well as an entry in the cluster index for the 
	 * user with the given id and saves it.
	 *  
	 * @param id the identifier of this cluster.
	 * @param userId the identifier of the user.
	 * @return the created node or <code>null</code> if the node could not be created.
	 */
	E createHGCluster(Object id, Object userId);
	
	/**
	 * Deletes the cluster with the given id.
	 *  
	 * @param id the identifier of the cluster to delete.
	 * @param userId the identifier of the user to delete the cluster from.
	 */
	void deleteHGCluster(Object id, Object userId);
	
	/**
	 * Finds a cluster of a user by means of its identifier.
	 * 
	 * @param id the identifier of the cluster to find.
	 * @param userId the id of the user to find the cluster for.
	 * @return the cluster with the given identifier or <code>null</code> if no cluster was found.
	 */
	E findHGClusterById(Object id, Object userId);
	
	/**
	 * Connects the given Stay Point with the given cluster.
	 * 
	 * @param parent the parent to which to connect the Stay Point to.
	 * @param stayPoint the Stay Point to connect with.
	 * @param params the parameters/properties to add to the connection. The implementation
	 * defines if <code>null</code> values are allowed. A <code>null Map</code> should indicate
	 * that no properties should be added to the connection. 
	 */
	void addStayPoint(E parent, E stayPoint, Map<String, Object> params);
	
	/**
	 * Connects the given child cluster with the parent cluster.
	 * 
	 * @param parent the parent to which to connect the child to.
	 * @param child the child cluster to connect with.
	 */
	void addChildHGCluster(E parent, E child);

}
