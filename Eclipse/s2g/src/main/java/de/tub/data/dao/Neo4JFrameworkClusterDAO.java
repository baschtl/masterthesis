package de.tub.data.dao;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.graph.NodeProperties;
import de.tub.graph.RelTypes;
import de.tub.util.DBUtil;

/**
 * This class gives the possibility to access a framework cluster.
 * It hides the technology of access so 
 * that the programmer does not have to worry about during usage. 
 * The <code>Neo4JFrameworkClusterDAO</code> class is part of the 
 * Data Access Object design pattern. That is why it implements
 * the <code>IFrameworkClusterDAO</code> interface with all its methods.
 * <br />
 * Furthermore it implements the interface <code>Clonable</code>.
 * This makes it possible to clone an instance of <code>Neo4JFrameworkClusterDAO</code>
 * and makes this class a part of the Prototype design pattern.
 * 
 * @author Sebastian Oelke
 *
 */
public class Neo4JFrameworkClusterDAO implements IFrameworkClusterDAO<Node>, Cloneable {

	private static final Logger LOG = LoggerFactory.getLogger(Neo4JFrameworkClusterDAO.class);
	
	/**
	 * Standard empty constructor.
	 */
	public Neo4JFrameworkClusterDAO() {}
	
	/**
	 * Copy constructor for use with the Prototype design pattern.
	 * If you use <code>clone()</code> this constructor has to be used
	 * and all instance variables have to be copied.<br />
	 * Because the <code>Neo4JFrameworkClusterDAO</code> class has no instance variables
	 * to copy this constructor is equivalent with the standard constructor.
	 *  
	 * @param fClusterDao The <code>Neo4JFrameworkClusterDAO</code> which has to be copied.
	 * @see Neo4JFrameworkClusterDAO#clone()
	 */
	public Neo4JFrameworkClusterDAO(final Neo4JFrameworkClusterDAO fClusterDao) {}
	
	/**
	 * @throws NullPointerException if the given id is <code>null</code>.
	 * @throws RuntimeException if a node already exists with the given cluster id.
	 */
	@Override
	public Node createFrameworkCluster(Object id) throws NullPointerException, RuntimeException {
		if (id == null)
			throw new NullPointerException(
				"You provided a null value for the framework cluster id. " +
				"This parameter is expected to be non-null. A " +
				"framework cluster cannot be created without an id.");
		
		// Get graph instance and initialize framework cluster index
		GraphDatabaseService graph = DBUtil.graph();
		Index<Node> cIndex = graph.index().forNodes(DBUtil.FRAMEWORK_CLUSTER_INDEX);
		
		// Create transaction
		Transaction tx = graph.beginTx();
		Node c = null;
		
		try {
			// Create new framework cluster
			c = graph.createNode();
			
			// Check if cluster with given id already exists
			Node cExists = cIndex.get(NodeProperties.FRAMEWORK_CLUSTER_ID, id).getSingle();
			if (cExists != null) {
				// Rollback transaction because node already exists
				tx.failure();
				throw new RuntimeException("A framework cluster with the identifier [" + id + "] already exists. Creation is stopped.");
			}
			
			// Set properties
			c.setProperty(NodeProperties.FRAMEWORK_CLUSTER_ID, id);
			
			// Add cluster to index
			cIndex.add(c, NodeProperties.FRAMEWORK_CLUSTER_ID, id);
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
	public void deleteFrameworkCluster(Object id) throws NullPointerException {
		if (id == null)
			throw new NullPointerException(
				"You provided a null value for the framework cluster id. " +
				"This parameter is expected to be non-null. A " +
				"framework cluster cannot be deleted without an id.");
		
		// Get graph instance and initialize framework cluster index
		GraphDatabaseService graph = DBUtil.graph();
		Index<Node> cIndex = graph.index().forNodes(DBUtil.FRAMEWORK_CLUSTER_INDEX);
		
		// Create transaction
		Transaction tx = graph.beginTx();
		
		try {
			// Get node to delete from index
			Node c = cIndex.get(NodeProperties.FRAMEWORK_CLUSTER_ID, id).getSingle();
			// Remove node from index
			cIndex.remove(c, NodeProperties.FRAMEWORK_CLUSTER_ID, id);
			
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
	public Node updateFrameworkCluster(Node cluster, String property, Object value) throws NullPointerException {
		if (cluster == null || property == null || value == null)
			throw new NullPointerException(
				"You provided a null value for either the cluster, the property to update or the value to set. " +
				"All three parameters are expected to be non-null.");
		
		Transaction tx = DBUtil.graph().beginTx();
		
		try {
			// Set the property
			cluster.setProperty(property, value);
			
			tx.success();
		} finally {
			tx.finish();
		}
		
		return cluster;
	}
	
	/**
	 * @throws NullPointerException if the given id is <code>null</code>.
	 */
	@Override
	public Node findFrameworkClusterById(Object id) throws NullPointerException {
		if (id == null)
			throw new IllegalArgumentException(
				"You provided a null value for the framework cluster id. " +
				"This parameter is expected to be non-null.");
		
		// Initialize cluster index
		Index<Node> cIndex = DBUtil.graph().index().forNodes(DBUtil.FRAMEWORK_CLUSTER_INDEX);
		return cIndex.get(NodeProperties.FRAMEWORK_CLUSTER_ID, id).getSingle();
	}

	/**
	 * @throws NullPointerException if one of the given arguments is <code>null</code>.
	 */
	@Override
	public void addStayPoint(Node parent, Node stayPoint) throws NullPointerException {
		if (parent == null || stayPoint == null)
			throw new NullPointerException(
				"You provided a null value for either the parent or the stay point. " +
				"Both parameters are expected to be non-null.");
		
		SharedNeo4JDAO.addNodeHelper(parent, stayPoint, RelTypes.HasStayPoint, null);
	}

	/**
	 * @throws NullPointerException if one of the given arguments is <code>null</code>.
	 */
	@Override
	public void addChildFrameworkCluster(Node parent, Node child) throws NullPointerException {
		if (parent == null || child == null)
			throw new NullPointerException(
				"You provided a null value for either the parent or the child. " +
				"Both parameters are expected to be non-null.");
		
		SharedNeo4JDAO.addNodeHelper(parent, child, RelTypes.HasChildCluster, null);
	}

	/**
	 * @throws NullPointerException if one of the given arguments is <code>null</code>.
	 */
	@Override
	public void addRootFrameworkCluster(Node referenceNode, Node root) throws NullPointerException {
		if (referenceNode == null || root == null)
			throw new NullPointerException(
				"You provided a null value for either the reference or the root node. " +
				"Both parameters are expected to be non-null.");
		
		SharedNeo4JDAO.addNodeHelper(referenceNode, root, RelTypes.RootFrameworkCluster, null);
	}
	
	@Override
	public Node getFrameworkRootCluster() {
		// Get the graphs reference node
		Node refNode = DBUtil.graph().getReferenceNode();
		// Get relationship to the framework root cluster
		Relationship relToRoot = refNode.getSingleRelationship(
									RelTypes.RootFrameworkCluster, 
									Direction.OUTGOING);
		// Get framework root cluster
		Node frameworkRootCluster = null;
		if (relToRoot != null) {
			frameworkRootCluster = relToRoot.getEndNode();
		} else
			LOG.warn("The relationship from the graph's reference node to the root framework cluster " +
						"could not be found. Null is returned instead of a reference to the framework root cluster.");
		
		// This should not happen as of the Neo4J documentation for class Relationship.
		if (frameworkRootCluster == null)
			LOG.warn("The node found at the end of the relationship between the graph's reference node " +
						"and the root framework cluster is null. Null is returned instead of a reference " +
						"to the framework root cluster.");
		
		return frameworkRootCluster;
	}
	
	/**
	 * @throws NullPointerException if the given argument is <code>null</code>.
	 */
	public Object getFrameworkClusterId(Node cluster) throws NullPointerException {
		if (cluster == null)
			throw new NullPointerException(
				"You provided a null value for the cluster node. " +
				"The parameter is expected to be non-null.");
		
		return cluster.getProperty(NodeProperties.FRAMEWORK_CLUSTER_ID, null);
	}
	
	/**
	 * This method clones an instance of the <code>Neo4JFrameworkClusterDAO</code>
	 * class. Therefore it copies all of its properties to a new
	 * instance. 
	 *  
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected IFrameworkClusterDAO<Node> clone() throws CloneNotSupportedException {
		return new Neo4JFrameworkClusterDAO(this);
	}

}
