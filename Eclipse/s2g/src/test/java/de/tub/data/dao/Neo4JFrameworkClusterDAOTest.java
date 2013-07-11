package de.tub.data.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.tooling.GlobalGraphOperations;

import de.tub.Neo4JTestHelper;
import de.tub.graph.NodeProperties;
import de.tub.graph.RelTypes;
import de.tub.util.DBUtil;

public class Neo4JFrameworkClusterDAOTest {
	
	public static final int CLUSTER_1_ID = 1;
	public static final int CLUSTER_2_ID = 2;

	@Test
	public void testCreateFrameworkCluster() {
		Neo4JFrameworkClusterDAO cDao = (Neo4JFrameworkClusterDAO) DAOFactory.instance().getFrameworkClusterDAO();
		
		// ### Create cluster
		Node c1 = cDao.createFrameworkCluster(CLUSTER_1_ID);
		long cId = c1.getId();
		GraphDatabaseService g = c1.getGraphDatabase();
		
		assertNotNull("Cluster with id " + CLUSTER_1_ID + " was not created properly. It is null.", c1);
		c1 = null;
		
		// Retrieve cluster by node id
		c1 = g.getNodeById(cId);
		
		assertNotNull("Cluster with id " + CLUSTER_1_ID + " was not created properly. It is null.", c1);
		assertEquals("Cluster with id " + CLUSTER_1_ID + " was not created properly. It has a wrong id.",
				CLUSTER_1_ID, c1.getProperty(NodeProperties.FRAMEWORK_CLUSTER_ID));
		
		// ### Test double id cluster
		boolean hasThrownException = false;
		try {
			cDao.createFrameworkCluster(CLUSTER_1_ID);
		} catch(RuntimeException e) {
			hasThrownException = true;
		}
		
		assertTrue("An error should have been thrown during creation of a cluster with an id that exists.", hasThrownException);
		// Check if there is really only one cluster in the graph
		int size = 0;
		Iterable<Node> nodes = GlobalGraphOperations.at(DBUtil.graph()).getAllNodes();
		for (Node n : nodes) {
			if (n.hasProperty(NodeProperties.FRAMEWORK_CLUSTER_ID) && 
				n.getProperty(NodeProperties.FRAMEWORK_CLUSTER_ID).equals(CLUSTER_1_ID))
				size++;
		}
		
		assertEquals("There should only be one cluster.", 1, size);
	}
	
	@Test
	public void testDeleteFrameworkCluster() {		
		Neo4JFrameworkClusterDAO cDao = (Neo4JFrameworkClusterDAO) DAOFactory.instance().getFrameworkClusterDAO();
		
		// ### Create cluster
		Node c1 = cDao.createFrameworkCluster(CLUSTER_1_ID);
		long c1NodeId = c1.getId();
		GraphDatabaseService g = c1.getGraphDatabase();
		
		// Delete cluster
		cDao.deleteFrameworkCluster(CLUSTER_1_ID);
		c1 = null;

		boolean wasNotFound = false;
		try {
			g.getNodeById(c1NodeId);
		} catch (NotFoundException e){
			wasNotFound = true;
		}
		
		// Try to get the cluster by node id
		assertTrue("A node with id [" + c1NodeId + "] must not be in the graph.", wasNotFound);
	}
	
	@Test
	public void testFindFrameworkClusterById() {		
		Neo4JFrameworkClusterDAO cDao = (Neo4JFrameworkClusterDAO) DAOFactory.instance().getFrameworkClusterDAO();
		
		// ### Create cluster
		Node c1 = cDao.createFrameworkCluster(CLUSTER_1_ID);
		long c1NodeId = c1.getId();
		c1 = null;
		
		// Find cluster with DAO by framework cluster id
		c1 = cDao.findFrameworkClusterById(CLUSTER_1_ID);
		
		assertNotNull("A cluster with the framework cluster id [" + CLUSTER_1_ID + "] should have been found.", c1);
		assertEquals("The found cluster should have the node id [" + c1NodeId + "].", c1NodeId, c1.getId());
		assertEquals("Cluster with id " + CLUSTER_1_ID + " was not found properly. It has a wrong id.", 
				CLUSTER_1_ID, c1.getProperty(NodeProperties.FRAMEWORK_CLUSTER_ID));
	}
	
	@Test
	public void testUpdateFrameworkCluster() {		
		Neo4JFrameworkClusterDAO cDao = (Neo4JFrameworkClusterDAO) DAOFactory.instance().getFrameworkClusterDAO();
		
		// ### Create initial cluster
		Node c1 = cDao.createFrameworkCluster(CLUSTER_1_ID);
		
		// ### Update cluster id
		// Test for null parameters
		boolean hasThrownException = false;
		try {
			cDao.updateFrameworkCluster(null, NodeProperties.FRAMEWORK_CLUSTER_ID, CLUSTER_2_ID);
		} catch(RuntimeException e) {
			hasThrownException = true;
		}
		assertTrue("An error should have been thrown during updating of a cluster with a null cluster reference.", hasThrownException);
		
		hasThrownException = false;
		try {
			cDao.updateFrameworkCluster(c1, null, CLUSTER_2_ID);
		} catch(RuntimeException e) {
			hasThrownException = true;
		}
		assertTrue("An error should have been thrown during updating of a cluster with a null cluster property.", hasThrownException);
		
		hasThrownException = false;
		try {
			cDao.updateFrameworkCluster(c1, NodeProperties.FRAMEWORK_CLUSTER_ID, null);
		} catch(RuntimeException e) {
			hasThrownException = true;
		}
		assertTrue("An error should have been thrown during updating of a cluster with a null cluster property value.", hasThrownException);
		
		// Test with non-null parameters
		hasThrownException = false;
		try {
			cDao.updateFrameworkCluster(c1, NodeProperties.FRAMEWORK_CLUSTER_ID, CLUSTER_2_ID);
		} catch(RuntimeException e) {
			hasThrownException = true;
		}
		assertFalse("No error should have been thrown during updating of a cluster with non-null values.", hasThrownException);
		
		// Test new cluster id value
		assertEquals("The framework cluster id of this cluster is not as expected.", CLUSTER_2_ID, c1.getProperty(NodeProperties.FRAMEWORK_CLUSTER_ID));
	}
	
	@Test
	public void testAddStayPoint() {
		Neo4JFrameworkClusterDAO cDao = (Neo4JFrameworkClusterDAO) DAOFactory.instance().getFrameworkClusterDAO();
		Neo4JStaypointDAO sDao = (Neo4JStaypointDAO) DAOFactory.instance().getStaypointDAO();
		
		// ### Create initial cluster
		Node c1 = cDao.createFrameworkCluster(CLUSTER_1_ID);
		
		// Create stay point to add
		Node sp = sDao.createStayPoint(123, 10.0, 20.0, 12345, 23456);
		
		// Add stay point to cluster
		cDao.addStayPoint(c1, sp);
		
		// Check created relationships
		assertTrue("The cluster should have an outgoing relationship of type " + RelTypes.HasStayPoint + ".", 
				c1.hasRelationship(RelTypes.HasStayPoint, Direction.OUTGOING));
		assertTrue("The stay point should have an incoming relationship of type " + RelTypes.HasStayPoint + ".", 
				sp.hasRelationship(RelTypes.HasStayPoint, Direction.INCOMING));
		assertEquals("The cluster should be connected to the newly created stay point.", 
				sp, c1.getSingleRelationship(RelTypes.HasStayPoint, Direction.OUTGOING).getEndNode());
	}

	@Test
	public void testAddChildFrameworkCluster() {
		Neo4JFrameworkClusterDAO cDao = (Neo4JFrameworkClusterDAO) DAOFactory.instance().getFrameworkClusterDAO();
		
		// ### Create initial cluster and child
		Node c1 = cDao.createFrameworkCluster(CLUSTER_1_ID);
		Node child = cDao.createFrameworkCluster(CLUSTER_2_ID);
		
		// Add child to cluster
		cDao.addChildFrameworkCluster(c1, child);
		
		// Check created relationships
		assertTrue("The cluster should have an outgoing relationship of type " + RelTypes.HasChildCluster + ".", 
				c1.hasRelationship(RelTypes.HasChildCluster, Direction.OUTGOING));
		assertTrue("The child cluster should have an incoming relationship of type " + RelTypes.HasChildCluster + ".", 
				child.hasRelationship(RelTypes.HasChildCluster, Direction.INCOMING));
		assertEquals("The cluster should be connected to the newly created child cluster.", 
				child, c1.getSingleRelationship(RelTypes.HasChildCluster, Direction.OUTGOING).getEndNode());
	}
	
	@Test
	public void testAddRootFrameworkCluster() {
		Neo4JFrameworkClusterDAO cDao = (Neo4JFrameworkClusterDAO) DAOFactory.instance().getFrameworkClusterDAO();
		
		// ### Create initial cluster and get reference node
		Node c1 = cDao.createFrameworkCluster(CLUSTER_1_ID);
		Node ref = DBUtil.graph().getReferenceNode();
		
		// Add cluster to ref node
		cDao.addRootFrameworkCluster(ref, c1);
		
		// Check created relationships
		assertTrue("The reference node should have an outgoing relationship of type " + RelTypes.RootFrameworkCluster + ".", 
				ref.hasRelationship(RelTypes.RootFrameworkCluster, Direction.OUTGOING));
		assertTrue("The cluster should have an incoming relationship of type " + RelTypes.RootFrameworkCluster + ".", 
				c1.hasRelationship(RelTypes.RootFrameworkCluster, Direction.INCOMING));
		assertEquals("The reference node should be connected to the newly created cluster.", 
				c1, ref.getSingleRelationship(RelTypes.RootFrameworkCluster, Direction.OUTGOING).getEndNode());
	}
	
	@Test
	public void testGetFrameworkRootCluster() {
		Neo4JFrameworkClusterDAO cDao = (Neo4JFrameworkClusterDAO) DAOFactory.instance().getFrameworkClusterDAO();
		
		// ### Create initial cluster and get reference node
		Node c1 = cDao.createFrameworkCluster(CLUSTER_1_ID);
		Node ref = DBUtil.graph().getReferenceNode();
		
		// Add cluster to ref node
		cDao.addRootFrameworkCluster(ref, c1);
		
		assertEquals("The retrieved cluster seems not to be the framework root cluster.", c1, cDao.getFrameworkRootCluster());
		
		// Delete framework root cluster
		cDao.deleteFrameworkCluster(CLUSTER_1_ID);
		assertNull("There should have been no framework root cluster found.", cDao.getFrameworkRootCluster());
	}
	
	@Test
	public void testGetFrameworkClusterId() {
		Neo4JFrameworkClusterDAO cDao = (Neo4JFrameworkClusterDAO) DAOFactory.instance().getFrameworkClusterDAO();
		
		// ### Create initial cluster and get reference node
		Node c1 = cDao.createFrameworkCluster(CLUSTER_1_ID);
		
		assertEquals("The retrieved cluster id seems not to be correct.", CLUSTER_1_ID, cDao.getFrameworkClusterId(c1));
		
		// Test for null parameters
		boolean hasThrownException = false;
		try {
			cDao.getFrameworkClusterId(null);
		} catch(RuntimeException e) {
			hasThrownException = true;
		}
		assertTrue("An error should have been thrown during retrieving the id of a null cluster reference.", hasThrownException);
	}
	
	@After
	public void resetGraph() {
		Neo4JTestHelper.resetGraph();
	}
}
