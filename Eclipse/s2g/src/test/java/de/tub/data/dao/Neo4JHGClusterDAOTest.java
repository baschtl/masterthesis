package de.tub.data.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

public class Neo4JHGClusterDAOTest {
	
	public static final int USER_ID = 1;
	public static final int CLUSTER_1_ID = 1;
	public static final int CLUSTER_2_ID = 2;

	@Test
	public void testCreateHGCluster() {
		Neo4JHGClusterDAO hgDao = (Neo4JHGClusterDAO) DAOFactory.instance().getHGClusterDAO();
		
		// ### Create hg cluster
		Node hg1 = hgDao.createHGCluster(CLUSTER_1_ID, USER_ID);
		long hgId = hg1.getId();
		GraphDatabaseService g = hg1.getGraphDatabase();
		
		assertNotNull("HG cluster with id " + CLUSTER_1_ID + " was not created properly. It is null.", hg1);
		hg1 = null;
		
		// Retrieve hg cluster by node id
		hg1 = g.getNodeById(hgId);
		
		assertNotNull("HG cluster with id " + CLUSTER_1_ID + " was not created properly. It is null.", hg1);
		assertEquals("HG cluster with id " + CLUSTER_1_ID + " was not created properly. It has a wrong id.",
				CLUSTER_1_ID, hg1.getProperty(NodeProperties.HG_CLUSTER_ID));
		
		// ### Test double id hg cluster
		boolean hasThrownException = false;
		try {
			hgDao.createHGCluster(CLUSTER_1_ID, USER_ID);
		} catch(RuntimeException e) {
			hasThrownException = true;
		}
		
		assertTrue("An error should have been thrown during creation of a hg cluster with an id that exists.", hasThrownException);
		// Check if there is really only one hg cluster in the graph
		int size = 0;
		Iterable<Node> nodes = GlobalGraphOperations.at(DBUtil.graph()).getAllNodes();
		for (Node n : nodes) {
			if (n.hasProperty(NodeProperties.HG_CLUSTER_ID) && 
				n.getProperty(NodeProperties.HG_CLUSTER_ID).equals(CLUSTER_1_ID))
				size++;
		}
		
		assertEquals("There should only be one hg cluster.", 1, size);
	}
	
	@Test
	public void testDeleteHGCluster() {		
		Neo4JHGClusterDAO hgDao = (Neo4JHGClusterDAO) DAOFactory.instance().getHGClusterDAO();
		
		// ### Create hg cluster
		Node hg1 = hgDao.createHGCluster(CLUSTER_1_ID, USER_ID);
		long hg1NodeId = hg1.getId();
		GraphDatabaseService g = hg1.getGraphDatabase();
		
		// Delete hg cluster
		hgDao.deleteHGCluster(CLUSTER_1_ID, USER_ID);
		hg1 = null;

		boolean wasNotFound = false;
		try {
			g.getNodeById(hg1NodeId);
		} catch (NotFoundException e){
			wasNotFound = true;
		}
		
		// Try to get the hg cluster by node id
		assertTrue("A node with id [" + hg1NodeId + "] must not be in the graph.", wasNotFound);
	}
	
	@Test
	public void testFindHGClusterById() {		
		Neo4JHGClusterDAO hgDao = (Neo4JHGClusterDAO) DAOFactory.instance().getHGClusterDAO();
		
		// ### Create hg cluster
		Node hg1 = hgDao.createHGCluster(CLUSTER_1_ID, USER_ID);
		long hg1NodeId = hg1.getId();
		hg1 = null;
		
		// Find hg cluster with DAO by hg cluster id
		hg1 = hgDao.findHGClusterById(CLUSTER_1_ID, USER_ID);
		
		assertNotNull("A hg cluster with the hg cluster id [" + CLUSTER_1_ID + "] should have been found.", hg1);
		assertEquals("The found hg cluster should have the node id [" + hg1NodeId + "].", hg1NodeId, hg1.getId());
		assertEquals("HG cluster with id " + CLUSTER_1_ID + " was not found properly. It has a wrong id.", 
				CLUSTER_1_ID, hg1.getProperty(NodeProperties.HG_CLUSTER_ID));
	}
	
	@Test
	public void testAddStayPoint() {
		Neo4JHGClusterDAO hgDao = (Neo4JHGClusterDAO) DAOFactory.instance().getHGClusterDAO();
		Neo4JStaypointDAO sDao = (Neo4JStaypointDAO) DAOFactory.instance().getStaypointDAO();
		
		// ### Create initial hg cluster
		Node hg1 = hgDao.createHGCluster(CLUSTER_1_ID, USER_ID);
		
		// Create stay point to add
		Node sp = sDao.createStayPoint(123, 10.0, 20.0, 12345, 23456);
		
		// Add stay point to hg cluster
		hgDao.addStayPoint(hg1, sp, null);
		
		// Check created relationships
		assertTrue("The hg cluster should have an outgoing relationship of type " + RelTypes.HasHGStayPoint + ".", 
				hg1.hasRelationship(RelTypes.HasHGStayPoint, Direction.OUTGOING));
		assertTrue("The stay point should have an incoming relationship of type " + RelTypes.HasHGStayPoint + ".", 
				sp.hasRelationship(RelTypes.HasHGStayPoint, Direction.INCOMING));
		assertEquals("The hg cluster should be connected to the newly created stay point.", 
				sp, hg1.getSingleRelationship(RelTypes.HasHGStayPoint, Direction.OUTGOING).getEndNode());
	}

	@Test
	public void testAddChildHGCluster() {
		Neo4JHGClusterDAO hgDao = (Neo4JHGClusterDAO) DAOFactory.instance().getHGClusterDAO();
		
		// ### Create initial hg cluster and child
		Node hg1 = hgDao.createHGCluster(CLUSTER_1_ID, USER_ID);
		Node child = hgDao.createHGCluster(CLUSTER_2_ID, USER_ID);
		
		// Add child to hg cluster
		hgDao.addChildHGCluster(hg1, child);
		
		// Check created relationships
		assertTrue("The hg cluster should have an outgoing relationship of type " + RelTypes.HasHGChildCluster + ".", 
				hg1.hasRelationship(RelTypes.HasHGChildCluster, Direction.OUTGOING));
		assertTrue("The child hg cluster should have an incoming relationship of type " + RelTypes.HasHGChildCluster + ".", 
				child.hasRelationship(RelTypes.HasHGChildCluster, Direction.INCOMING));
		assertEquals("The hg cluster should be connected to the newly created child hg cluster.", 
				child, hg1.getSingleRelationship(RelTypes.HasHGChildCluster, Direction.OUTGOING).getEndNode());
	}
	
	@After
	public void resetGraph() {
		Neo4JTestHelper.resetGraph();
	}
}
