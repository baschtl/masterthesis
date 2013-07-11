package de.tub.data.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.tooling.GlobalGraphOperations;

import de.tub.Neo4JTestHelper;
import de.tub.graph.NodeProperties;
import de.tub.util.DBUtil;

/**
 * 
 * @author Sebastian Oelke
 *
 */
public class Neo4JStaypointDAOTest {
	
	public static final int STAYPOINT_ID = 1234;
	public static final double STAYPOINT_LAT = 10.0;
	public static final double STAYPOINT_LONG = 120.0;
	public static final long STAYPOINT_ARR = 123456789L;
	public static final long STAYPOINT_LEAV = 987654321L;
		
	@Test
	public void testCreateStayPoint() {		
		Neo4JStaypointDAO sDao = (Neo4JStaypointDAO) DAOFactory.instance().getStaypointDAO();
		
		// ### Create stay point
		Node sp = sDao.createStayPoint(STAYPOINT_ID, STAYPOINT_LAT, STAYPOINT_LONG, STAYPOINT_ARR, STAYPOINT_LEAV);
		long sId = sp.getId();
		GraphDatabaseService g = sp.getGraphDatabase();
		
		assertNotNull("Staypoint with id " + STAYPOINT_ID + " was not created properly. It is null.", sp);
		sp = null;
		
		// Retrieve stay point by node id
		sp = g.getNodeById(sId);
		
		assertNotNull("Staypoint with id " + STAYPOINT_ID + " was not created properly. It is null.", sp);
		assertEquals("Staypoint with id " + STAYPOINT_ID + " was not created properly. It has a wrong id.", 
				STAYPOINT_ID, sp.getProperty(NodeProperties.STAYPOINT_ID));
		assertEquals("Staypoint with id " + STAYPOINT_ID + " was not created properly. It has a wrong latitude value.", 
				STAYPOINT_LAT, sp.getProperty(NodeProperties.STAYPOINT_LAT));
		assertEquals("Staypoint with id " + STAYPOINT_ID + " was not created properly. It has a wrong longitude value.", 
				STAYPOINT_LONG, sp.getProperty(NodeProperties.STAYPOINT_LONG));
		assertEquals("Staypoint with id " + STAYPOINT_ID + " was not created properly. It has a wrong arrival time.", 
				STAYPOINT_ARR, sp.getProperty(NodeProperties.STAYPOINT_ARRIVAL));
		assertEquals("Staypoint with id " + STAYPOINT_ID + " was not created properly. It has a wrong leaving time.", 
				STAYPOINT_LEAV, sp.getProperty(NodeProperties.STAYPOINT_LEAVING));
			
		// ### Test double id stay point
		boolean hasThrownException = false;
		try {
			sDao.createStayPoint(STAYPOINT_ID, STAYPOINT_LAT, STAYPOINT_LONG, STAYPOINT_ARR, STAYPOINT_LEAV);
		} catch(RuntimeException e) {
			hasThrownException = true;
		}
		
		assertTrue("An error should have been thrown during creation of a stay point with an id that exists.", hasThrownException);
		// Check if there is really only one stay point in the graph
		int size = 0;
		Iterable<Node> nodes = GlobalGraphOperations.at(DBUtil.graph()).getAllNodes();
		for (Node n : nodes) {
			if (n.hasProperty(NodeProperties.STAYPOINT_ID) && 
				n.getProperty(NodeProperties.STAYPOINT_ID).equals(STAYPOINT_ID))
				size++;
		}
		
		assertEquals("There should only be one stay point.", 1, size);
	}
	
	@Test
	public void testDeleteStayPoint() {		
		Neo4JStaypointDAO sDao = (Neo4JStaypointDAO) DAOFactory.instance().getStaypointDAO();
		
		// ### Create stay point
		Node sp = sDao.createStayPoint(STAYPOINT_ID, STAYPOINT_LAT, STAYPOINT_LONG, STAYPOINT_ARR, STAYPOINT_LEAV);
		long spNodeId = sp.getId();
		GraphDatabaseService g = sp.getGraphDatabase();
		
		// Delete stay point
		sDao.deleteStayPoint(STAYPOINT_ID);
		sp = null;

		boolean wasNotFound = false;
		try {
			g.getNodeById(spNodeId);
		} catch (NotFoundException e){
			wasNotFound = true;
		}
		
		// Try to get the stay point by node id
		assertTrue("A node with id [" + spNodeId + "] must not be in the graph.", wasNotFound);
	}
	
	@Test
	public void testFindStayPointById() {		
		Neo4JStaypointDAO sDao = (Neo4JStaypointDAO) DAOFactory.instance().getStaypointDAO();
		
		// ### Create stay point
		Node sp = sDao.createStayPoint(STAYPOINT_ID, STAYPOINT_LAT, STAYPOINT_LONG, STAYPOINT_ARR, STAYPOINT_LEAV);
		long spNodeId = sp.getId();
		sp = null;
		
		// Find stay point with DAO by stay point id
		sp = sDao.findStayPointById(STAYPOINT_ID);
		
		assertNotNull("A stay point with the stay point id [" + STAYPOINT_ID + "] should have been found.", sp);
		assertEquals("The found stay point should have the node id [" + spNodeId + "].", spNodeId, sp.getId());
		assertEquals("Staypoint with id " + STAYPOINT_ID + " was not found properly. It has a wrong id.", 
				STAYPOINT_ID, sp.getProperty(NodeProperties.STAYPOINT_ID));
		assertEquals("Staypoint with id " + STAYPOINT_ID + " was not found properly. It has a wrong latitude value.", 
				STAYPOINT_LAT, sp.getProperty(NodeProperties.STAYPOINT_LAT));
		assertEquals("Staypoint with id " + STAYPOINT_ID + " was not found properly. It has a wrong longitude value.", 
				STAYPOINT_LONG, sp.getProperty(NodeProperties.STAYPOINT_LONG));
		assertEquals("Staypoint with id " + STAYPOINT_ID + " was not found properly. It has a wrong arrival time.", 
				STAYPOINT_ARR, sp.getProperty(NodeProperties.STAYPOINT_ARRIVAL));
		assertEquals("Staypoint with id " + STAYPOINT_ID + " was not found properly. It has a wrong leaving time.", 
				STAYPOINT_LEAV, sp.getProperty(NodeProperties.STAYPOINT_LEAVING));
	}
	
	@After
	public void resetGraph() {
		Neo4JTestHelper.resetGraph();
	}
}
