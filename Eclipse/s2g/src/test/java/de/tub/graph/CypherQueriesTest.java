package de.tub.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.helpers.collection.IteratorUtil;

import de.tub.Neo4JTestHelper;
import de.tub.data.dao.DAOFactory;
import de.tub.data.dao.Neo4JHGClusterDAO;
import de.tub.data.dao.Neo4JStaypointDAO;
import de.tub.data.dao.Neo4JUserDAO;

public class CypherQueriesTest {

	private static final String USER_ID_1 = "1";
	private static final String USER_ID_2 = "2";
	
	private static final String HG_CLUSTER_LEVEL_1_0 = "1_0";
	
	private static final String HG_CLUSTER_LEVEL_2_0 = "2_0";
	private static final String HG_CLUSTER_LEVEL_2_1 = "2_1";
	
	private static final String HG_CLUSTER_LEVEL_3_0 = "3_0";
	private static final String HG_CLUSTER_LEVEL_3_1 = "3_1";
	
	private static final int STAYPOINT_1_USER_1 = 1;
	private static final int STAYPOINT_2_USER_1 = 2;
	private static final int STAYPOINT_3_USER_1 = 3;
	private static final int STAYPOINT_4_USER_1 = 4;
	
	private static final int STAYPOINT_1_USER_2 = 5;
	private static final int STAYPOINT_2_USER_2 = 6;
	private static final int STAYPOINT_3_USER_2 = 7;
	
	private static long user1NodeId, user2NodeId;
	
	@BeforeClass
	public static void initGraph() {
		// Generate hg for two users with stay points
		generateHg(true);
	}

	@AfterClass
	public static void resetGraph() {
		Neo4JTestHelper.resetGraph();
	}
	
	@Test
	public void testAllHgClusterIdsForRootesFromToLevel() {
		// Fill list with expected hg cluster ids to receive
		List<String> expectedClusterIds = new ArrayList<String>();
		expectedClusterIds.add(HG_CLUSTER_LEVEL_1_0);
		expectedClusterIds.add(HG_CLUSTER_LEVEL_1_0);
		expectedClusterIds.add(HG_CLUSTER_LEVEL_2_0);
		expectedClusterIds.add(HG_CLUSTER_LEVEL_2_0);
		expectedClusterIds.add(HG_CLUSTER_LEVEL_2_1);
		expectedClusterIds.add(HG_CLUSTER_LEVEL_2_1);
		expectedClusterIds.add(HG_CLUSTER_LEVEL_3_0);
		expectedClusterIds.add(HG_CLUSTER_LEVEL_3_0);
		expectedClusterIds.add(HG_CLUSTER_LEVEL_3_1);
		int initialExpectedCusterIdsSize = expectedClusterIds.size();
		
		// Execute Cypher query
		ExecutionResult result = CypherQueries.allHgClusterIdsForUsersFromToLevel(
									new Long[] {user1NodeId, user2NodeId}, -1, -1);
		
		assertNotNull("The Cypher query result should not be null.", result);
		assertEquals("There should be only one column returned.", 1, result.columns().size());
		
		// List for not expected results
		List<String> notExpectedClusterIds = new ArrayList<String>();
		List<String> expectedClusterIdsInResult = new ArrayList<String>();
		
		// Test results
		Iterator<String> id_column = result.columnAs(NodeProperties.HG_CLUSTER_ID);
		while (id_column.hasNext()) {
			String id = id_column.next();
			if (expectedClusterIds.contains(id)) {
				expectedClusterIdsInResult.add(id);
				expectedClusterIds.remove(id);
			}
			else
				notExpectedClusterIds.add(id);
		}
		
		assertEquals("The Cypher query result has not the expected number of expected results.", 
				initialExpectedCusterIdsSize, expectedClusterIdsInResult.size());
		assertEquals("There were unexpected results from the Cypher query.", 
				0, notExpectedClusterIds.size());
		
		// Execute Cypher query with null and empty array
		boolean hasThrownException = false;
		try {
			result = CypherQueries.allHgClusterIdsForUsersFromToLevel(
								null, 0, -1);
		} catch (Exception e) {
			hasThrownException = true;
		}
		assertTrue("An exception should have been thrown when executing the Cypher query with a null array.", hasThrownException);
		
		hasThrownException = false;
		try {
			result = CypherQueries.allHgClusterIdsForUsersFromToLevel(
								new Long[] {}, 0, -1);
		} catch (Exception e) {
			hasThrownException = true;
		}
		assertTrue("An exception should have been thrown when executing the Cypher query with an empty array.", hasThrownException);
	}
	
	@Test
	public void testHgClustersInStaypointOrderForUser() {
		// Execute Cypher query for user 1
		ExecutionResult resultUser1 = CypherQueries.hgClustersInStaypointOrderForUser(
										user1NodeId, new String[] {
												HG_CLUSTER_LEVEL_1_0, 
												HG_CLUSTER_LEVEL_2_0, 
												HG_CLUSTER_LEVEL_2_1,
												HG_CLUSTER_LEVEL_3_0});
		
		assertNotNull("The Cypher query result should not be null.", resultUser1);
		assertEquals("There should be only one column returned.", 3, resultUser1.columns().size());
		
		String[] expectedResultOrder = new String[] {HG_CLUSTER_LEVEL_2_0, 
				HG_CLUSTER_LEVEL_2_1, HG_CLUSTER_LEVEL_3_0, HG_CLUSTER_LEVEL_2_0};
		int i = 0;
		
		Iterator<String> resultColumn = resultUser1.columnAs(NodeProperties.HG_CLUSTER_ID);
		for (String hgClusterId : IteratorUtil.asIterable(resultColumn)) {
			assertTrue("There were too many results returned.",
					(i < expectedResultOrder.length));
			assertEquals("The resulting hg cluster id is not as expected.",
					expectedResultOrder[i], hgClusterId);
			i++;
		}
		
		// Execute Cypher query for user 2
		ExecutionResult resultUser2 = CypherQueries.hgClustersInStaypointOrderForUser(
				user2NodeId, new String[] {
						HG_CLUSTER_LEVEL_1_0, 
						HG_CLUSTER_LEVEL_2_0, 
						HG_CLUSTER_LEVEL_2_1,
						HG_CLUSTER_LEVEL_3_0,
						HG_CLUSTER_LEVEL_3_1});

		assertNotNull("The Cypher query result should not be null.", resultUser2);
		assertEquals("There should be only one column returned.", 3, resultUser2.columns().size());

		expectedResultOrder = new String[] { HG_CLUSTER_LEVEL_2_0,
				HG_CLUSTER_LEVEL_3_1, HG_CLUSTER_LEVEL_2_1 };
		i = 0;

		resultColumn = resultUser2.columnAs(NodeProperties.HG_CLUSTER_ID);
		for (String hgClusterId : IteratorUtil.asIterable(resultColumn)) {
			assertTrue("There were too many results returned.",
					(i < expectedResultOrder.length));
			assertEquals("The resulting hg cluster id is not as expected.",
					expectedResultOrder[i], hgClusterId);
			i++;
		}
	}
	
	@Test
	public void testCountUsers() {
		// Query graph database
		ExecutionResult result = CypherQueries.countUsers();
		
		assertNotNull("The Cypher query result should not be null.", result);
		assertEquals("There should be only one column returned.", 1, result.columns().size());
		
		long usersCount = 0L;
		Iterator<Long> resultIt = result.columnAs("usersCount");
		
		// Extract single returned column
		for (Long count : IteratorUtil.asIterable(resultIt)) {
			usersCount = count;
			break;
		}
		
		assertEquals("There should be two users found.", 2, usersCount);
	}
	
	@Test
	public void testAllUsers() {
		// Query graph database
		ExecutionResult result = CypherQueries.allUsers();
		
		assertNotNull("The Cypher query result should not be null.", result);
		assertEquals("There should be only one column returned.", 1, result.columns().size());
		
		List<Node> users = new ArrayList<Node>();
		Iterator<Node> resultIt = result.columnAs("users");
		
		// Extract single returned column
		for (Node user : IteratorUtil.asIterable(resultIt)) {
			users.add(user);
		}
		
		assertEquals("There should be two users found.", 2, users.size());
	}
	
	@Test
	public void testUsersInHgCluster() {
		// Query graph database
		ExecutionResult result = CypherQueries.countUsersInHgCluster(HG_CLUSTER_LEVEL_1_0);
		
		assertNotNull("The Cypher query result should not be null.", result);
		assertEquals("There should be only one column returned.", 1, result.columns().size());
		
		long usersCount = 0L;
		Iterator<Long> resultIt = result.columnAs("usersInHgCluster");
		
		// Extract single returned column
		for (Long count : IteratorUtil.asIterable(resultIt)) {
			usersCount = count;
			break;
		}
		
		assertEquals("There should be two users found.", 2, usersCount);
		
		// Query graph database
		result = CypherQueries.countUsersInHgCluster(HG_CLUSTER_LEVEL_3_1);
		
		assertNotNull("The Cypher query result should not be null.", result);
		assertEquals("There should be only one column returned.", 1, result.columns().size());
		
		usersCount = 0L;
		resultIt = result.columnAs("usersInHgCluster");
		
		// Extract single returned column
		for (Long count : IteratorUtil.asIterable(resultIt)) {
			usersCount = count;
			break;
		}
		
		assertEquals("There should be one user found.", 1, usersCount);
		
		// Query graph database for not existing hg cluster
		result = CypherQueries.countUsersInHgCluster("21_0");
		
		assertNotNull("The Cypher query result should not be null.", result);
		assertEquals("There should be only one column returned.", 1, result.columns().size());
		
		usersCount = 0L;
		resultIt = result.columnAs("usersInHgCluster");
		
		// Extract single returned column
		for (Long count : IteratorUtil.asIterable(resultIt)) {
			usersCount = count;
			break;
		}
		
		assertEquals("There should be no user found.", 0, usersCount);
	}
	
	@Test
	public void testCountUserStaypoints() {
		// Query graph database for user 1
		ExecutionResult result = CypherQueries.countUserStaypoints(user1NodeId);
		
		assertNotNull("The Cypher query result should not be null.", result);
		assertEquals("There should be only one column returned.", 1, result.columns().size());
		
		long userStayPointsCount = 0L;
		Iterator<Long> resultIt = result.columnAs("userStayPointsCount");
		
		// Extract single returned column
		for (Long count : IteratorUtil.asIterable(resultIt)) {
			userStayPointsCount = count;
			break;
		}
		
		assertEquals("There should be four stay points for user 1 found.", 4, userStayPointsCount);
		
		// Query graph database for user 2
		result = CypherQueries.countUserStaypoints(user2NodeId);
		
		assertNotNull("The Cypher query result should not be null.", result);
		assertEquals("There should be only one column returned.", 1, result.columns().size());
		
		userStayPointsCount = 0L;
		resultIt = result.columnAs("userStayPointsCount");
		
		// Extract single returned column
		for (Long count : IteratorUtil.asIterable(resultIt)) {
			userStayPointsCount = count;
			break;
		}
		
		assertEquals("There should be three stay points for user 2 found.", 3, userStayPointsCount);
		
		// Query graph database for not existing user
		result = CypherQueries.countUserStaypoints(3000L);
		
		assertNotNull("The Cypher query result should not be null.", result);
		assertEquals("There should be only one column returned.", 1, result.columns().size());
		
		userStayPointsCount = 0L;
		resultIt = result.columnAs("userStayPointsCount");
		
		boolean exceptionThrown = false;
		// Extract single returned column
		try {
			for (Long count : IteratorUtil.asIterable(resultIt)) {
				userStayPointsCount = count;
				break;
			}
		} catch (NotFoundException e) {
			exceptionThrown = true;
		}
		
		assertTrue("There should be an exception thrown when counting the stay points for a not existing user.", exceptionThrown);
	}
	
	//###################################################################
	// Helper
	//###################################################################
	
	private static void generateHg(boolean generateStayPoints) {
		Neo4JUserDAO uDao = (Neo4JUserDAO) DAOFactory.instance().getUserDAO();
		Neo4JHGClusterDAO hgDao = (Neo4JHGClusterDAO) DAOFactory.instance().getHGClusterDAO();
		
		// Create user nodes and connect them to the graph's reference node
		Node user1 = uDao.createUser(USER_ID_1);
		Node user2 = uDao.createUser(USER_ID_2);
		
		user1NodeId = user1.getId();
		user2NodeId = user2.getId();
		
		uDao.addRootUser(user1.getGraphDatabase().getReferenceNode(), user1);
		uDao.addRootUser(user2.getGraphDatabase().getReferenceNode(), user2);
		
		// Create hg root clusters
		Node hgRootUser1 = hgDao.createHGCluster(HG_CLUSTER_LEVEL_1_0, USER_ID_1);
		Node hgRootUser2 = hgDao.createHGCluster(HG_CLUSTER_LEVEL_1_0, USER_ID_2);
		uDao.addRootHGCluster(user1, hgRootUser1);
		uDao.addRootHGCluster(user2, hgRootUser2);
		
		// Build hg of user 1
		Node hgLevel20User1 = hgDao.createHGCluster(HG_CLUSTER_LEVEL_2_0, USER_ID_1);
		Node hgLevel21User1 = hgDao.createHGCluster(HG_CLUSTER_LEVEL_2_1, USER_ID_1);
		Node hgLevel30User1 = hgDao.createHGCluster(HG_CLUSTER_LEVEL_3_0, USER_ID_1);
		hgDao.addChildHGCluster(hgRootUser1, hgLevel20User1);
		hgDao.addChildHGCluster(hgRootUser1, hgLevel21User1);
		hgDao.addChildHGCluster(hgLevel20User1, hgLevel30User1);
		
		// Build hg of user 2
		Node hgLevel20User2 = hgDao.createHGCluster(HG_CLUSTER_LEVEL_2_0, USER_ID_2);
		Node hgLevel21User2 = hgDao.createHGCluster(HG_CLUSTER_LEVEL_2_1, USER_ID_2);
		Node hgLevel30User2 = hgDao.createHGCluster(HG_CLUSTER_LEVEL_3_0, USER_ID_2);
		Node hgLevel31User2 = hgDao.createHGCluster(HG_CLUSTER_LEVEL_3_1, USER_ID_2);
		hgDao.addChildHGCluster(hgRootUser2, hgLevel20User2);
		hgDao.addChildHGCluster(hgRootUser2, hgLevel21User2);
		hgDao.addChildHGCluster(hgLevel20User2, hgLevel30User2);
		hgDao.addChildHGCluster(hgLevel21User2, hgLevel31User2);
		
		if (generateStayPoints) {
			Neo4JStaypointDAO sDao = (Neo4JStaypointDAO) DAOFactory.instance().getStaypointDAO();
			
			// User 1 stay points
			hgDao.addStayPoint(hgLevel20User1, sDao.createStayPoint(STAYPOINT_1_USER_1, 1.0, 1.0, 1L, 1L), null);
			hgDao.addStayPoint(hgLevel21User1, sDao.createStayPoint(STAYPOINT_2_USER_1, 1.0, 1.0, 1L, 2L), null);
			hgDao.addStayPoint(hgLevel30User1, sDao.createStayPoint(STAYPOINT_3_USER_1, 1.0, 1.0, 1L, 3L), null);
			hgDao.addStayPoint(hgLevel20User1, sDao.createStayPoint(STAYPOINT_4_USER_1, 1.0, 1.0, 1L, 4L), null);
			
			// User 2 stay points
			hgDao.addStayPoint(hgLevel20User2, sDao.createStayPoint(STAYPOINT_1_USER_2, 1.0, 1.0, 1L, 1L), null);
			hgDao.addStayPoint(hgLevel31User2, sDao.createStayPoint(STAYPOINT_2_USER_2, 1.0, 1.0, 1L, 2L), null);
			hgDao.addStayPoint(hgLevel21User2, sDao.createStayPoint(STAYPOINT_3_USER_2, 1.0, 1.0, 1L, 3L), null);
		}
	}
}
