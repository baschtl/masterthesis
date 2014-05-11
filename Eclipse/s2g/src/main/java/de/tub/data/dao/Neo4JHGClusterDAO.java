package de.tub.data.dao;

import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

import de.tub.graph.NodeProperties;
import de.tub.graph.RelTypes;
import de.tub.util.DBUtil;

/**
 * This class gives the possibility to access clusters.
 * It hides the technology of access so 
 * that the programmer does not have to worry about during usage. 
 * The <code>Neo4JHGClusterDAO</code> class is part of the 
 * Data Access Object design pattern. That is why it implements
 * the <code>IHGClusterDAO</code> interface with all its methods.
 * <br />
 * Furthermore it implements the interface <code>Clonable</code>.
 * This makes it possible to clone an instance of <code>Neo4JHGClusterDAO</code>
 * and makes this class a part of the Prototype design pattern.
 * 
 * @author Sebastian Oelke
 *
 */
public class Neo4JHGClusterDAO implements IHGClusterDAO<Node>, Cloneable {
	
	/**
	 * Standard empty constructor.
	 */
	public Neo4JHGClusterDAO() {}
	
	/**
	 * Copy constructor for use with the Prototype design pattern.
	 * If you use <code>clone()</code> this constructor has to be used
	 * and all instance variables have to be copied.<br />
	 * Because the <code>Neo4JHGClusterDAO</code> class has no instance variables
	 * to copy this constructor is equivalent with the standard constructor.
	 *  
	 * @param hgClusterDao The <code>Neo4JHGClusterDAO</code> which has to be copied.
	 * @see Neo4JHGClusterDAO#clone()
	 */
	public Neo4JHGClusterDAO(final Neo4JHGClusterDAO hgClusterDao) {}
	
	/**
	 * @throws NullPointerException if the given id is <code>null</code>.
	 * @throws RuntimeException if a node already exists with the given cluster id.
	 */
	@Override
	public Node createHGCluster(Object id, Object userId) throws NullPointerException, RuntimeException {
		if (id == null || userId == null)
			throw new NullPointerException(
				"You provided a null value for either the hg cluster id or the user id. " +
				"Both parameters are expected to be non-null. A " +
				"hg cluster cannot be created without an id or a specified user.");
		
		// Get graph instance and initialize hg cluster index
		GraphDatabaseService graph = DBUtil.graph();
		Index<Node> cIndex = graph.index().forNodes(DBUtil.HG_CLUSTER_INDEX);
		
		// Create transaction
		Transaction tx = graph.beginTx();
		Node c = null;
		
		try {
			// Create new hg cluster
			c = graph.createNode();
			
			// Check if cluster with given id already exists
			Node cExists = findHGClusterById(id, userId);
			if (cExists != null) {
				// Rollback transaction because node already exists
				tx.failure();
				throw new RuntimeException("A hierarchical graph cluster with the identifier [" + id + "] already exists. Creation is stopped.");
			}
			
			// Set properties
			c.setProperty(NodeProperties.HG_CLUSTER_ID, id);
			
			// Add cluster to index
			cIndex.add(c, NodeProperties.HG_CLUSTER_ID, id);
			cIndex.add(c, NodeProperties.USER_ID, userId);
			
			tx.success();
		} finally {
			// Commit transaction
			tx.finish();
		}
		
		return c;
	}

	/**
	 * @throws NullPointerException if the given id is <code>null</code>.
	 */
	@Override
	public void deleteHGCluster(Object id, Object userId) throws NullPointerException {
		if (id == null || userId == null)
			throw new NullPointerException(
				"You provided a null value for either the hg cluster id or the user id. " +
				"Both parameters are expected to be non-null. A " +
				"hg cluster cannot be deleted without an id or a specified user.");
		
		// Get graph instance and initialize hierarchical graph cluster index
		GraphDatabaseService graph = DBUtil.graph();
		Index<Node> cIndex = graph.index().forNodes(DBUtil.HG_CLUSTER_INDEX);
		
		// Create transaction
		Transaction tx = graph.beginTx();
		
		try {
			// Get node to delete from index
			Node c = cIndex.query(buildFindUserIndexQuery(id, userId)).getSingle();
			// Remove node from index
			cIndex.remove(c, NodeProperties.HG_CLUSTER_ID, id);
			cIndex.remove(c, NodeProperties.USER_ID, userId);
			
			// Delete all relationships attached to this cluster
			Iterable<Relationship> relationships = c.getRelationships();
			for (Relationship r : relationships)
				r.delete();
		
			// Delete cluster
			c.delete();
			
			tx.success();
		} finally {
			// Commit transaction
			tx.finish();
		}
	}
	
	/**
	 * @throws NullPointerException if at least one of the given arguments is <code>null</code>.
	 */
	@Override
	public Node findHGClusterById(Object id, Object userId) throws NullPointerException {
		if (id == null || userId == null)
			throw new NullPointerException(
				"You provided a null value for either the hg cluster id or the user id. " +
				"Both parameters are expected to be non-null.");

		// Initialize cluster index
		Index<Node> cIndex = DBUtil.graph().index().forNodes(DBUtil.HG_CLUSTER_INDEX);
		
		return cIndex.query(buildFindUserIndexQuery(id, userId)).getSingle();
	}

	/**
	 * @throws NullPointerException if one of the given arguments is <code>null</code>.
	 */
	public void addStayPoint(Node parent, Node stayPoint) throws NullPointerException {
		if (parent == null || stayPoint == null)
			throw new NullPointerException(
				"You provided a null value for either the parent or the stay point. " +
				"Both parameters are expected to be non-null.");
		
		SharedNeo4JDAO.addNodeHelper(parent, stayPoint, RelTypes.HasHGStayPoint, null);
	}
	
	/**
	 * @throws NullPointerException if one of the given arguments is <code>null</code>, 
	 * except for the <code>params</code> map.
	 */
	@Override
	public void addStayPoint(Node parent, Node stayPoint, Map<String, Object> params) throws NullPointerException {
		if (parent == null || stayPoint == null)
			throw new NullPointerException(
				"You provided a null value for either the parent or the stay point. " +
				"Both parameters are expected to be non-null.");
		
		SharedNeo4JDAO.addNodeHelper(parent, stayPoint, RelTypes.HasHGStayPoint, params);
	}

	/**
	 * @throws NullPointerException if one of the given arguments is <code>null</code>.
	 */
	@Override
	public void addChildHGCluster(Node parent, Node child) throws NullPointerException {
		if (parent == null || child == null)
			throw new NullPointerException(
				"You provided a null value for either the parent or the child. " +
				"Both parameters are expected to be non-null.");
		
		SharedNeo4JDAO.addNodeHelper(parent, child, RelTypes.HasHGChildCluster, null);
	}
	
	/**
	 * This method clones an instance of the <code>Neo4JHGClusterDAO</code>
	 * class. Therefore it copies all of its properties to a new
	 * instance. 
	 *  
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected IHGClusterDAO<Node> clone() throws CloneNotSupportedException {
		return new Neo4JHGClusterDAO(this);
	}
	
	//###################################################################
	// Helper
	//###################################################################

	/**
	 * Builds a Lucene query of the form:
	 * <code>hg_cluster_id:{id} AND user_id:{userId}</code>.
	 * 
	 * @param id the hg cluster id.
	 * @param userId the user id.
	 * @return a Lucene index query to retrieve a hg cluster node from the index.
	 */
	private String buildFindUserIndexQuery(Object id, Object userId) {
		StringBuilder builder = new StringBuilder();
		builder.append(NodeProperties.HG_CLUSTER_ID)
			.append(":")
			.append(id)
			.append(" AND ")
			.append(NodeProperties.USER_ID)
			.append(":")
			.append(userId);
		
		return builder.toString();
	}
}
