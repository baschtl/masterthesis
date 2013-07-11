package de.tub.data.dao;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.helpers.collection.IteratorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.graph.CypherQueries;
import de.tub.graph.NodeProperties;
import de.tub.graph.RelProperties;
import de.tub.graph.RelTypes;
import de.tub.util.DBUtil;

/**
 * This class gives the possibility to access a user.
 * It hides the technology of access so 
 * that the programmer does not have to worry about during usage. 
 * The <code>Neo4JUserDAO</code> class is part of the 
 * Data Access Object design pattern. That is why it implements
 * the <code>IUserDAO</code> interface with all its methods.
 * <br />
 * Furthermore it implements the interface <code>Clonable</code>.
 * This makes it possible to clone an instance of <code>Neo4JUserDAO</code>
 * and makes this class a part of the Prototype design pattern.
 * 
 * @author Sebastian Oelke
 *
 */
public class Neo4JUserDAO implements IUserDAO<Node>, Cloneable {

	private static final Logger LOG = LoggerFactory.getLogger(Neo4JUserDAO.class);
	
	/**
	 * Standard empty constructor.
	 */
	public Neo4JUserDAO() {}
	
	/**
	 * Copy constructor for use with the Prototype design pattern.
	 * If you use <code>clone()</code> this constructor has to be used
	 * and all instance variables have to be copied.<br />
	 * Because the <code>Neo4JUserDAO</code> class has no instance variables
	 * to copy this constructor is equivalent with the standard constructor.
	 *  
	 * @param userDao The <code>Neo4JUserDAO</code> which has to be copied.
	 * @see Neo4JUserDAO#clone()
	 */
	public Neo4JUserDAO(final Neo4JUserDAO userDao) {}
	
	/**
	 * @throws NullPointerException if the given id is <code>null</code>.
	 * @throws RuntimeException if a node already exists with the given user id.
	 */
	@Override
	public Node createUser(Object id) throws NullPointerException, RuntimeException {
		if (id == null)
			throw new NullPointerException(
				"You provided a null value for the user id. " +
				"This parameter is expected to be non-null. A " +
				"user cannot be created without an id");
		
		// Get graph instance and initialize user index
		GraphDatabaseService graph = DBUtil.graph();
		Index<Node> uIndex = graph.index().forNodes(DBUtil.USER_INDEX);
		
		// Create transaction
		Transaction tx = graph.beginTx();
		Node u = null;
		
		try {
			// Create new user
			u = graph.createNode();
			
			// Check if user with given id already exists
			Node uExists = uIndex.get(NodeProperties.USER_ID, id).getSingle();
			if (uExists != null) {
				// Rollback transaction because node already exists
				tx.failure();
				throw new RuntimeException("A user with the identifier [" + id + "] already exists. Creation is stopped.");
			}
			
			// Set properties
			u.setProperty(NodeProperties.USER_ID, id);
			
			// Add user to index
			uIndex.add(u, NodeProperties.USER_ID, id);
			tx.success();
		} finally {
			// Commit transaction
			tx.finish();
		}
		
		return u;
	}

	/**
	 * @throws NullPointerException if the given is id <code>null</code>.
	 */
	@Override
	public void deleteUser(Object id) throws NullPointerException {
		if (id == null)
			throw new NullPointerException(
				"You provided a null value for the user id. " +
				"This parameter is expected to be non-null. A " +
				"user cannot be deleted without an id.");
		
		// Get graph instance and initialize user index
		GraphDatabaseService graph = DBUtil.graph();
		Index<Node> uIndex = graph.index().forNodes(DBUtil.USER_INDEX);
		
		// Create transaction
		Transaction tx = graph.beginTx();
		
		try {
			// Get node to delete from index
			Node u = uIndex.get(NodeProperties.USER_ID, id).getSingle();
			// Remove node from index
			uIndex.remove(u, NodeProperties.USER_ID, id);
			
			// Delete all relationships attached to this user
			Iterable<Relationship> relationships = u.getRelationships();
			for (Relationship r : relationships)
				r.delete();
			
			// Delete user
			u.delete();
			
			tx.success();
		} finally {
			// Commit transaction
			tx.finish();
		}
	}
	
	/**
	 * @throws NullPointerException if the given id is <code>null</code>.
	 */
	@Override
	public Node findUserById(Object id) throws NullPointerException {
		if (id == null)
			throw new NullPointerException(
				"You provided a null value for the user id. " +
				"This parameter is expected to be non-null.");
		
		// Initialize cluster index
		Index<Node> uIndex = DBUtil.graph().index().forNodes(DBUtil.USER_INDEX);
		return uIndex.get(NodeProperties.USER_ID, id).getSingle();
	}
	
	@Override
	public List<Node> findAll() {
		// Query graph database for all users
		ExecutionResult result = CypherQueries.allUsers();
		
		List<Node> results = new LinkedList<Node>();
		Iterator<Node> resultIt = result.columnAs("users");
		
		// Extract returned column
		for (Node user : IteratorUtil.asIterable(resultIt))
			results.add(user);
		
		return results;
	}

	/**
	 * @throws NullPointerException if one of the given arguments is <code>null</code>.
	 */
	@Override
	public void addRootHGCluster(Node user, Node root) throws NullPointerException {
		if (user == null || root == null)
			throw new NullPointerException(
				"You provided a null value for either the user or the hg root node. " +
				"Both parameters are expected to be non-null.");
		
		SharedNeo4JDAO.addNodeHelper(user, root, RelTypes.HasHG, null);
	}
	
	/**
	 * @throws NullPointerException if one of the given arguments is <code>null</code>.
	 */
	@Override
	public void addRootUser(Node referenceNode, Node root) throws NullPointerException {
		if (referenceNode == null || root == null)
			throw new NullPointerException(
				"You provided a null value for either the graph's reference node or the root user node. " +
				"Both parameters are expected to be non-null.");
		
		SharedNeo4JDAO.addNodeHelper(referenceNode, root, RelTypes.RootUser, null);
	}
	
	/**
	 * @throws NullPointerException if one of the given arguments is <code>null</code>.
	 */
	@Override
	public void connectSimilarUsers(Node userNodeOne, Node userNodeTwo, Object weight) throws NullPointerException {
		if (userNodeOne == null || userNodeTwo == null)
			throw new NullPointerException(
				"You provided a null value for at least one of the two user nodes. " +
				"Both parameters are expected to be non-null.");
		
		// Build parameter map
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(RelProperties.SIMILARITY_WEIGHT, weight);
		
		SharedNeo4JDAO.addNodeHelper(userNodeOne, userNodeTwo, RelTypes.SpatiallySimilar, params);
	}
	
	/**
	 * Returns the user id of the given user node.
	 * 
	 * @param user the user from which to extract the user id.
	 * @return the user id of the given node or <code>null</code> if the node 
	 * does not have a user id.
	 * @throws NullPointerException if the given argument is <code>null</code>.
	 */
	public Object getUserId(Node user) throws NullPointerException {
		if (user == null)
			throw new NullPointerException(
				"You provided a null value for the user node. " +
				"The parameter is expected to be non-null.");
		
		return user.getProperty(NodeProperties.USER_ID, null);
	}
	
	/**
	 * This method clones an instance of the <code>Neo4JUserDAO</code>
	 * class. Therefore it copies all of its properties to a new
	 * instance. 
	 *  
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected IUserDAO<Node> clone() throws CloneNotSupportedException {
		return new Neo4JUserDAO(this);
	}

}
