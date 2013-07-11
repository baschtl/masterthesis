package de.tub.data.dao;

import java.util.List;


/**
 * This interface provides methods to access users.
 * The <code>IUserDAO</code> is part
 * of the Data Access Object design pattern.
 * 
 * @author Sebastian Oelke
 *
 */
public interface IUserDAO<E> extends IDao {

	/**
	 * Creates and saves a user.
	 *  
	 * @param id the identifier of this user.
	 * @return the created node or <code>null</code> if the node could not be created.
	 */
	E createUser(Object id);
	
	/**
	 * Deletes the user with the given id.
	 *  
	 * @param id the identifier of the user to delete.
	 */
	void deleteUser(Object id);
	
	/**
	 * Finds a user by means of its identifier.
	 * 
	 * @param id the identifier of the user to find.
	 * @return the user with the given identifier or <code>null</code> if no user was found.
	 */
	E findUserById(Object id);
	
	/**
	 * Finds all users.
	 * 
	 * @return all persisted users.
	 */
	List<E> findAll();
	
	/**
	 * Connects the given user node with the given hg root node.
	 * 
	 * @param user the user to which to connect the hg root node to.
	 * @param root the hg root cluster to connect with.
	 */
	void addRootHGCluster(E user, E root);
	
	/**
	 * Connects the given root user node with the reference node of the graph.
	 * 
	 * @param referenceNode the reference node of the graph to which to connect the root user node to.
	 * @param root the root user node.
	 */
	void addRootUser(E referenceNode, E root);
	
	/**
	 * Connects the first user node with the second user node and applies the given
	 * weight to the connection between them.
	 * 
	 * @param userNodeOne the first user node.
	 * @param userNodeTwo the second user node.
	 * @param weight the weight to apply to the connection between user node one and user node two.
	 */
	void connectSimilarUsers(E userNodeOne, E userNodeTwo, Object weight);
}
