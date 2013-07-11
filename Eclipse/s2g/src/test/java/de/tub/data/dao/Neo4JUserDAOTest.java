package de.tub.data.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

import de.tub.Neo4JTestHelper;
import de.tub.graph.NodeProperties;
import de.tub.graph.RelProperties;
import de.tub.graph.RelTypes;
import de.tub.util.DBUtil;

public class Neo4JUserDAOTest {
	
	public static final int USER_1_ID = 1;
	public static final int USER_2_ID = 2;
	public static final int USER_3_ID = 3;
	
	public static final double SIMILARITY_SCORE = 0.02;

	@Test
	public void testCreateUser() {
		Neo4JUserDAO uDao = (Neo4JUserDAO) DAOFactory.instance().getUserDAO();
		
		// ### Create user
		Node u1 = uDao.createUser(USER_1_ID);
		long uId = u1.getId();
		GraphDatabaseService g = u1.getGraphDatabase();
		
		assertNotNull("User with id " + USER_1_ID + " was not created properly. It is null.", u1);
		u1 = null;
		
		// Retrieve user by node id
		u1 = g.getNodeById(uId);
		
		assertNotNull("User with id " + USER_1_ID + " was not created properly. It is null.", u1);
		assertEquals("User with id " + USER_1_ID + " was not created properly. It has a wrong id.",
				USER_1_ID, u1.getProperty(NodeProperties.USER_ID));
		
		// ### Test double id user
		boolean hasThrownException = false;
		try {
			uDao.createUser(USER_1_ID);
		} catch(RuntimeException e) {
			hasThrownException = true;
		}
		
		assertTrue("An error should have been thrown during creation of a user with an id that exists.", hasThrownException);
		// Check if there is really only one user in the graph
		int size = 0;
		Iterable<Node> nodes = GlobalGraphOperations.at(DBUtil.graph()).getAllNodes();
		for (Node n : nodes) {
			if (n.hasProperty(NodeProperties.USER_ID) && 
				n.getProperty(NodeProperties.USER_ID).equals(USER_1_ID))
				size++;
		}
		
		assertEquals("There should only be one user.", 1, size);
	}
	
	@Test
	public void testDeleteUser() {		
		Neo4JUserDAO uDao = (Neo4JUserDAO) DAOFactory.instance().getUserDAO();
		
		// ### Create cluster
		Node u1 = uDao.createUser(USER_1_ID);
		long u1NodeId = u1.getId();
		GraphDatabaseService g = u1.getGraphDatabase();
		
		// Delete cluster
		uDao.deleteUser(USER_1_ID);
		u1 = null;

		boolean wasNotFound = false;
		try {
			g.getNodeById(u1NodeId);
		} catch (NotFoundException e){
			wasNotFound = true;
		}
		
		// Try to get the user by node id
		assertTrue("A node with id [" + u1NodeId + "] must not be in the graph.", wasNotFound);
	}
	
	@Test
	public void testFindUserById() {		
		Neo4JUserDAO uDao = (Neo4JUserDAO) DAOFactory.instance().getUserDAO();
		
		// ### Create user
		Node u1 = uDao.createUser(USER_1_ID);
		long u1NodeId = u1.getId();
		u1 = null;
		
		// Find user with DAO by user id
		u1 = uDao.findUserById(USER_1_ID);
		
		assertNotNull("A user with the user id [" + USER_1_ID + "] should have been found.", u1);
		assertEquals("The found user should have the node id [" + u1NodeId + "].", u1NodeId, u1.getId());
		assertEquals("User with id " + USER_1_ID + " was not found properly. It has a wrong id.", 
				USER_1_ID, u1.getProperty(NodeProperties.USER_ID));
	}
	
	@Test
	public void testFindAll() {		
		Neo4JUserDAO uDao = (Neo4JUserDAO) DAOFactory.instance().getUserDAO();
		
		// W/o users an empty list should be returned
		List<Node> users = uDao.findAll();
		
		// Test returned empty list
		assertNotNull("The users list should not be null.", users);
		assertTrue("The users list should be empty.", users.isEmpty());
		
		// ### Create three users
		Node u1 = uDao.createUser(USER_1_ID);
		long u1NodeId = u1.getId();
		u1 = null;
		Node u2 = uDao.createUser(USER_2_ID);
		long u2NodeId = u2.getId();
		u2 = null;
		Node u3 = uDao.createUser(USER_3_ID);
		long u3NodeId = u3.getId();
		u3 = null;
		
		// Find all users via dao
		users = uDao.findAll();
		
		// Test returned list
		assertNotNull("The users list should not be null.", users);
		assertEquals("The users list should include three users.", 3, users.size());
		
		// Test first user
		assertNotNull("A user with the user id [" + USER_1_ID + "] should have been found.", users.get(0));
		assertEquals("The found user should have the node id [" + u1NodeId + "].", u1NodeId, users.get(0).getId());
		assertEquals("User with id " + USER_1_ID + " was not found properly. It has a wrong id.", 
				USER_1_ID, users.get(0).getProperty(NodeProperties.USER_ID));
		
		// Test second user
		assertNotNull("A user with the user id [" + USER_2_ID + "] should have been found.", users.get(1));
		assertEquals("The found user should have the node id [" + u2NodeId + "].", u2NodeId, users.get(1).getId());
		assertEquals("User with id " + USER_2_ID + " was not found properly. It has a wrong id.", 
				USER_2_ID, users.get(1).getProperty(NodeProperties.USER_ID));
				
		// Test third user
		assertNotNull("A user with the user id [" + USER_2_ID + "] should have been found.", users.get(2));
		assertEquals("The found user should have the node id [" + u3NodeId + "].", u3NodeId, users.get(2).getId());
		assertEquals("User with id " + USER_3_ID + " was not found properly. It has a wrong id.", 
				USER_3_ID, users.get(2).getProperty(NodeProperties.USER_ID));
	}
	
	@Test
	public void testAddHGRootCluster() {
		Neo4JUserDAO uDao = (Neo4JUserDAO) DAOFactory.instance().getUserDAO();
		
		// ### Create initial user and create hg node
		Node u1 = uDao.createUser(USER_1_ID);
		Node hg = null;
		Transaction tx = DBUtil.graph().beginTx();
		try {
			hg = DBUtil.graph().createNode();
			tx.success();
		} finally {
			tx.finish();
		}
		
		// Add hg node to user as root node
		uDao.addRootHGCluster(u1, hg);
		
		// Check created relationships
		assertTrue("The user node should have an outgoing relationship of type " + RelTypes.HasHG + ".", 
				u1.hasRelationship(RelTypes.HasHG, Direction.OUTGOING));
		assertEquals("The user node should be connected to the newly created hg cluster.", 
				hg, u1.getSingleRelationship(RelTypes.HasHG, Direction.OUTGOING).getEndNode());
	}
	
	@Test
	public void testAddRootUser() {
		Neo4JUserDAO uDao = (Neo4JUserDAO) DAOFactory.instance().getUserDAO();
		
		// ### Create initial user and get reference node
		Node u1 = uDao.createUser(USER_1_ID);
		Node ref = DBUtil.graph().getReferenceNode();
		
		// Add user node to ref node as root node
		uDao.addRootUser(ref, u1);
		
		// Check created relationships
		assertTrue("The reference node should have an outgoing relationship of type " + RelTypes.RootUser + ".", 
				ref.hasRelationship(RelTypes.RootUser, Direction.OUTGOING));
		assertEquals("The user node should be connected to the reference node.", 
				u1, ref.getSingleRelationship(RelTypes.RootUser, Direction.OUTGOING).getEndNode());
	}
	
	@Test
	public void testConnectSimilarUsers() {
		Neo4JUserDAO uDao = (Neo4JUserDAO) DAOFactory.instance().getUserDAO();
		
		// ### Create initial users and get reference node
		Node u1 = uDao.createUser(USER_1_ID);
		Node u2 = uDao.createUser(USER_2_ID);
		Node ref = DBUtil.graph().getReferenceNode();
		
		// Add user nodes to ref node as root node
		uDao.addRootUser(ref, u1);
		uDao.addRootUser(ref, u2);
		
		// Create similarity connection between both users
		uDao.connectSimilarUsers(u1, u2, SIMILARITY_SCORE);
		
		// Check created relationships
		Iterable<Relationship> rels = u1.getRelationships(RelTypes.SpatiallySimilar);
		for (Relationship r : rels) {
			assertEquals("User node one is not connected with a spatially similar relationship to user node two.", u2, r.getEndNode());
			assertEquals("The similarity weight is not as expected.", SIMILARITY_SCORE, r.getProperty(RelProperties.SIMILARITY_WEIGHT));
		}
		
		rels = u2.getRelationships(RelTypes.SpatiallySimilar);
		for (Relationship r : rels) {
			assertEquals("User node two is not connected with a spatially similar relationship to user node one.", u1, r.getStartNode());
			assertEquals("The similarity weight is not as expected.", SIMILARITY_SCORE, r.getProperty(RelProperties.SIMILARITY_WEIGHT));
		}
	}
	
	@After
	public void resetGraph() {
		Neo4JTestHelper.resetGraph();
	}
}
