package de.tub.data.dao;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

import de.tub.graph.NodeProperties;
import de.tub.util.DBUtil;

/**
 * This class gives the possibility to access a framework cluster.
 * It hides the technology of access so 
 * that the programmer does not have to worry about during usage. 
 * The <code>Neo4JStaypointDAO</code> class is part of the 
 * Data Access Object design pattern. That is why it implements
 * the <code>IStaypointDAO</code> interface with all its methods.
 * <br />
 * Furthermore it implements the interface <code>Clonable</code>.
 * This makes it possible to clone an instance of <code>Neo4JStaypointDAO</code>
 * and makes this class a part of the Prototype design pattern.
 * 
 * @author Sebastian Oelke
 *
 */
public class Neo4JStaypointDAO implements IStaypointDAO<Node>, Cloneable {
	
	/**
	 * Standard empty constructor.
	 */
	public Neo4JStaypointDAO() {}
	
	/**
	 * Copy constructor for use with the Prototype design pattern.
	 * If you use <code>clone()</code> this constructor has to be used
	 * and all instance variables have to be copied.<br />
	 * Because the <code>Neo4JStaypointDAO</code> class has no instance variables
	 * to copy this constructor is equivalent with the standard constructor.
	 *  
	 * @param stayPointDao The <code>Neo4JStaypointDAO</code> which has to be copied.
	 * @see Neo4JStaypointDAO#clone()
	 */
	public Neo4JStaypointDAO(final Neo4JStaypointDAO stayPointDao) {}
	
	/**
	 * @throws RuntimeException if a node already exists with the given stay point id.
	 */
	@Override
	public Node createStayPoint(int id, double latitude, double longitude,
			long arrivalTime, long leavingTime) throws RuntimeException {
		// Get graph instance and initialize stay point index
		GraphDatabaseService graph = DBUtil.graph();
		Index<Node> spIndex = graph.index().forNodes(DBUtil.STAYPOINT_INDEX);
		
		// Create transaction
		Transaction tx = graph.beginTx();
		Node sp = null;
		
		try {
			// Create new stay point and add relationship to parent
			sp = graph.createNode();
			
			// Check if stay point with given id already exists
			Node spExists = spIndex.get(NodeProperties.STAYPOINT_ID, id).getSingle();
			if (spExists != null) {
				// Rollback transaction because node already exists
				tx.failure();
				throw new RuntimeException("A stay point with the identifier [" + id + "] already exists. Creation is stopped.");
			}
			
			// Set properties
			sp.setProperty(NodeProperties.STAYPOINT_ID, id);
			sp.setProperty(NodeProperties.STAYPOINT_LAT, latitude);
			sp.setProperty(NodeProperties.STAYPOINT_LONG, longitude);
			sp.setProperty(NodeProperties.STAYPOINT_ARRIVAL, arrivalTime);
			sp.setProperty(NodeProperties.STAYPOINT_LEAVING, leavingTime);
			
			// Add stay point to index
			spIndex.add(sp, NodeProperties.STAYPOINT_ID, id);
			tx.success();
		} finally {
			// Commit transaction
			tx.finish();
		}
		
		return sp;
	}

	@Override
	public void deleteStayPoint(int id) {
		// Get graph instance and initialize stay point index
		GraphDatabaseService graph = DBUtil.graph();
		Index<Node> spIndex = graph.index().forNodes(DBUtil.STAYPOINT_INDEX);
		
		// Create transaction
		Transaction tx = graph.beginTx();
		
		try {
			// Get node to delete from index
			Node sp = spIndex.get(NodeProperties.STAYPOINT_ID, id).getSingle();
			// Remove node from index
			spIndex.remove(sp, NodeProperties.STAYPOINT_ID, id);
			
			// Remove relationships of stay point
			Iterable<Relationship> relationships = sp.getRelationships();
			for (Relationship r : relationships)
				r.delete();
			
			// Delete stay point
			sp.delete();
			
			tx.success();
		} finally {
			// Commit transaction
			tx.finish();
		}
	}

	@Override
	public Node findStayPointById(int id) {
		// Initialize stay point index
		Index<Node> spIndex = DBUtil.graph().index().forNodes(DBUtil.STAYPOINT_INDEX);
		return spIndex.get(NodeProperties.STAYPOINT_ID, id).getSingle();
	}

	/**
	 * This method clones an instance of the <code>Neo4JStaypointDAO</code>
	 * class. Therefore it copies all of its properties to a new
	 * instance. 
	 *  
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected IStaypointDAO<Node> clone() throws CloneNotSupportedException {
		return new Neo4JStaypointDAO(this);
	}
}
