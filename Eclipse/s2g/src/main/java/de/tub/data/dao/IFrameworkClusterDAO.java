package de.tub.data.dao;




/**
 * This interface provides methods to access Framework
 * cluster. The <code>IFrameworkClusterDAO</code> is part
 * of the Data Access Object design pattern.
 * 
 * @author Sebastian Oelke
 *
 */
public interface IFrameworkClusterDAO<E> extends IDao {

	/**
	 * Creates a cluster as well as an entry in the cluster index and saves it.
	 *  
	 * @param id the identifier of this cluster.
	 * @return the created node or <code>null</code> if the node could not be created.
	 */
	E createFrameworkCluster(Object id);
	
	/**
	 * Deletes the cluster with the given id.
	 *  
	 * @param id the identifier of the cluster to delete.
	 */
	void deleteFrameworkCluster(Object id);
	
	/**
	 * Updates the given cluster's property with the given value.
	 * 
	 * @param cluster the cluster to update.
	 * @param property the property of the cluster to be updated, if it is not present it is created.
	 * @param value the value to set to the property of the cluster.
	 * @return the updated node or <code>null</code> if the node could not be updated.
	 */
	E updateFrameworkCluster(E cluster, String property, Object value);
	
	/**
	 * Finds a cluster by means of its identifier.
	 * 
	 * @param id the identifier of the cluster to find.
	 * @return the cluster with the given identifier or <code>null</code> if no cluster was found.
	 */
	E findFrameworkClusterById(Object id);
	
	/**
	 * Connects the given Stay Point with the given cluster.
	 * 
	 * @param parent the parent to which to connect the Stay Point to.
	 * @param stayPoint the Stay Point to connect with.
	 */
	void addStayPoint(E parent, E stayPoint);
	
	/**
	 * Connects the given child cluster with the parent cluster.
	 * 
	 * @param parent the parent to which to connect the child to.
	 * @param child the child cluster to connect with.
	 */
	void addChildFrameworkCluster(E parent, E child);

	/**
	 * Connects the given root cluster with the reference node of the graph.
	 * 
	 * @param referenceNode the reference node of the graph to which to connect the root cluster to.
	 * @param root the root cluster.
	 */
	void addRootFrameworkCluster(E referenceNode, E root);

	/**
	 * Returns the root cluster of the shared framework that is 
	 * attached to the graphs reference node.
	 * 
	 * @return the framework root cluster or <code>null</code> if there is no
	 * relationship from the graph's reference node to the framework root cluster or the
	 * framework root cluster itself is <code>null</code>.
	 */
	E getFrameworkRootCluster();
	
	/**
	 * Returns the framework cluster id of the given framework cluster node.
	 * 
	 * @param cluster the framework cluster from which to extract the framework cluster id.
	 * @return the framework cluster id of the given cluster node or <code>null</code> if the node 
	 * does not have a framework cluster id.
	 */
	Object getFrameworkClusterId(E cluster);
}
