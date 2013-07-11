package de.tub.processor.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

import de.tub.Neo4JTestHelper;
import de.tub.TestDatabase;
import de.tub.graph.NodeProperties;
import de.tub.graph.RelTypes;
import de.tub.observer.Interests;
import de.tub.reader.ReaderFactory;
import de.tub.reader.model.UserReader;
import de.tub.util.DBUtil;

public class HierarchicalGraphProcessorTest {
	public final static String POPULATE_FILE = "/sql/user_hg_staypoint_populate.sql";
	public final static String DELETE_FILE = "/sql/user_hg_staypoint_delete.sql";

	private static final String ROOT_CLUSTER_ID = "1_0";
	private static final long USER_ID = 0L;
	private static final long USER_WITHOUT_HG_ID = 1L;
	
	private static final String HG_CLUSTER_LEVEL_2_ID_1 = "2_0";
	private static final String HG_CLUSTER_LEVEL_2_ID_2 = "2_1";
	private static final String HG_CLUSTER_LEVEL_2_ID_3 = "2_2";
	private static final String HG_CLUSTER_LEVEL_3_ID_1 = "3_0";
	private static final String HG_CLUSTER_LEVEL_3_ID_2 = "3_1";
	private static final String HG_CLUSTER_LEVEL_3_ID_3 = "3_2";
	
	private static URL url;
	
	@Before
	public void populateDatabase() {
		// Retrieve file url to populate data file
		url = HierarchicalGraphProcessorTest.class.getResource(POPULATE_FILE);
		// Open database connection
		TestDatabase.setup();
		// Populate database with data
		TestDatabase.executeStatementsFromFile(url.getFile());
	}
	
	@After
	public void tearDownAfterClass() throws Exception {
		// Retrieve file url to delete data file
		url = HierarchicalGraphProcessorTest.class.getResource(DELETE_FILE);
		// Delete data from database
		TestDatabase.executeStatementsFromFile(url.getFile());
		// Close database connection
		TestDatabase.teardown();
	}
	
	@Test
	public void testHGCreation() {
		GraphDatabaseService graph = DBUtil.graph();
		Index<Node> cIndex = graph.index().forNodes(DBUtil.FRAMEWORK_CLUSTER_INDEX);
		Index<Node> hgIndex = graph.index().forNodes(DBUtil.HG_CLUSTER_INDEX);
		Index<Node> sIndex = graph.index().forNodes(DBUtil.STAYPOINT_INDEX);
		Index<Node> uIndex = graph.index().forNodes(DBUtil.USER_INDEX);
		
		// ### Build up fake shared framework
		Transaction tx = graph.beginTx();
		
		try {
			// Create root framework cluster
			Node root = graph.createNode();
			root.setProperty(NodeProperties.FRAMEWORK_CLUSTER_ID, ROOT_CLUSTER_ID);
			cIndex.add(root, NodeProperties.FRAMEWORK_CLUSTER_ID, ROOT_CLUSTER_ID);
			graph.getReferenceNode().createRelationshipTo(root, RelTypes.RootFrameworkCluster);
			
			Node current = root;
			
			int spId = 0;
			
			// Create three level with two cluster each
    		for (int i = 0; i < 3; i++) {
    			
    			for (int j = 2; j < 4; j++) {
    				String id = j + "_" + i;
    				// Create node cluster
    				Node cluster = graph.createNode();
					cluster.setProperty(NodeProperties.FRAMEWORK_CLUSTER_ID, id);
    	        	// Add node to index
    	        	cIndex.add(cluster, NodeProperties.FRAMEWORK_CLUSTER_ID, id);
    	        	// Create relationship to last node
    	        	current.createRelationshipTo(cluster, RelTypes.HasChildCluster);
    	        	
    	        	// Add stay points
    	        	for (int s = 0; s < 2; s++) {
    	        		// Create node stay point
    	        		Node sp = graph.createNode();
    	            	sp.setProperty(NodeProperties.STAYPOINT_ID, spId);
    	            	// Add to index
    	            	sIndex.add(sp, NodeProperties.STAYPOINT_ID, spId);
    	            	// Create relationship to cluster
    	            	cluster.createRelationshipTo(sp, RelTypes.HasStayPoint);
    	            	spId++;
    	        	}
    	        	current = cluster;
    			}
    			current = root;
    		}
			
			
			tx.success();
		} finally {
			tx.finish();
		}
		
		// ### Create reader and processor
		// Create a user reader
		UserReader uReader = (UserReader) ReaderFactory.instance().getUserReader();
		// Create the processor to test
		HierarchicalGraphProcessor hgProc = new HierarchicalGraphProcessor();
		
		// Setup the reader
		uReader.setProcessor(hgProc);
		uReader.attach(hgProc, Interests.UserFinished);
		
		// Read the user resources to process
		uReader.read();
		
		// ### Test creation of hierarchical graph
		// Get user node
		Node user = uIndex.get(NodeProperties.USER_ID, USER_ID).getSingle();
		assertNotNull("The user node should node be null.", user);
		assertEquals("The user id is not as expected.", USER_ID, user.getProperty(NodeProperties.USER_ID));
		assertNotNull("The user node should be connected to the graph's reference node.", user.getSingleRelationship(RelTypes.RootUser, Direction.INCOMING));
		
		// Get root hg node
		Relationship r = user.getSingleRelationship(RelTypes.HasHG, Direction.OUTGOING);
		assertNotNull("The user should have a connection to the hg root node.", r);
		Node rootHgNode = r.getEndNode();
		assertNotNull("There should be a hg root node.", rootHgNode);
		
		// Get nodes connected to root hg node
		Node rootChild1 = hgIndex.query(buildFindUserIndexQuery(HG_CLUSTER_LEVEL_2_ID_1, USER_ID)).getSingle();
		Node rootChild2 = hgIndex.query(buildFindUserIndexQuery(HG_CLUSTER_LEVEL_2_ID_2, USER_ID)).getSingle();
		Node rootChild3 = hgIndex.query(buildFindUserIndexQuery(HG_CLUSTER_LEVEL_2_ID_3, USER_ID)).getSingle();
		
		assertNotNull("There should be a hg root node child one.", rootChild1);
		assertEquals("The hg cluster id is not as expected.", HG_CLUSTER_LEVEL_2_ID_1, rootChild1.getProperty(NodeProperties.HG_CLUSTER_ID));
		assertEquals(rootHgNode, rootChild1.getSingleRelationship(RelTypes.HasHGChildCluster, Direction.INCOMING).getStartNode());
		assertNotNull("There should be a hg root node child two.", rootChild2);
		assertEquals("The hg cluster id is not as expected.", HG_CLUSTER_LEVEL_2_ID_2, rootChild2.getProperty(NodeProperties.HG_CLUSTER_ID));
		assertEquals(rootHgNode, rootChild2.getSingleRelationship(RelTypes.HasHGChildCluster, Direction.INCOMING).getStartNode());
		assertNotNull("There should be a hg root node child three.", rootChild3);
		assertEquals("The hg cluster id is not as expected.", HG_CLUSTER_LEVEL_2_ID_3, rootChild3.getProperty(NodeProperties.HG_CLUSTER_ID));
		assertEquals(rootHgNode, rootChild3.getSingleRelationship(RelTypes.HasHGChildCluster, Direction.INCOMING).getStartNode());
		
		// Get hg nodes connected to root hg children
		Node child1Child = hgIndex.query(buildFindUserIndexQuery(HG_CLUSTER_LEVEL_3_ID_1, USER_ID)).getSingle();
		Node child2Child = hgIndex.query(buildFindUserIndexQuery(HG_CLUSTER_LEVEL_3_ID_2, USER_ID)).getSingle();
		Node child3Child = hgIndex.query(buildFindUserIndexQuery(HG_CLUSTER_LEVEL_3_ID_3, USER_ID)).getSingle();
		
		assertNotNull("There should be a child of the hg root node child one.", child1Child);
		assertEquals("The hg cluster id is not as expected.", HG_CLUSTER_LEVEL_3_ID_1, child1Child.getProperty(NodeProperties.HG_CLUSTER_ID));
		assertEquals(rootChild1, child1Child.getSingleRelationship(RelTypes.HasHGChildCluster, Direction.INCOMING).getStartNode());
		assertNotNull("There should be a child of the hg root node child two.", child2Child);
		assertEquals("The hg cluster id is not as expected.", HG_CLUSTER_LEVEL_3_ID_2, child2Child.getProperty(NodeProperties.HG_CLUSTER_ID));
		assertEquals(rootChild2, child2Child.getSingleRelationship(RelTypes.HasHGChildCluster, Direction.INCOMING).getStartNode());
		assertNotNull("There should be a child of the hg root node child three.", child3Child);
		assertEquals("The hg cluster id is not as expected.", HG_CLUSTER_LEVEL_3_ID_3, child3Child.getProperty(NodeProperties.HG_CLUSTER_ID));
		assertEquals(rootChild3, child3Child.getSingleRelationship(RelTypes.HasHGChildCluster, Direction.INCOMING).getStartNode());
		
		// ### Test stay points connected to the hg clusters
		// # Test root hg node
		// Stay point ids that are expected to be referenced by the root hg node
		List<Integer> expectedStayPoints = new ArrayList<Integer>();
		expectedStayPoints.add(1);
		expectedStayPoints.add(2);
		expectedStayPoints.add(5);
		expectedStayPoints.add(7);
		expectedStayPoints.add(10);
		
		checkConnectedStayPoints(expectedStayPoints, rootHgNode);
		
		// # Test first child of root hg node
		// Stay point ids that are expected to be referenced by the child hg node
		expectedStayPoints.clear();
		expectedStayPoints.add(1);
		expectedStayPoints.add(2);
		
		checkConnectedStayPoints(expectedStayPoints, rootChild1);
		
		// Test child of first child of root hg node
		// Stay point ids that are expected to be referenced by the child hg node
		expectedStayPoints.clear();
		expectedStayPoints.add(2);
		
		checkConnectedStayPoints(expectedStayPoints, child1Child);
		
		// # Test second child of root hg node
		// Stay point ids that are expected to be referenced by the child hg node
		expectedStayPoints.clear();
		expectedStayPoints.add(5);
		expectedStayPoints.add(7);
		
		checkConnectedStayPoints(expectedStayPoints, rootChild2);
		
		// Test child of second child of root hg node
		// Stay point ids that are expected to be referenced by the child hg node
		expectedStayPoints.clear();
		expectedStayPoints.add(7);
		
		checkConnectedStayPoints(expectedStayPoints, child2Child);
		
		// # Test third child of root hg node
		// Stay point ids that are expected to be referenced by the child hg node
		expectedStayPoints.clear();
		expectedStayPoints.add(10);
		
		checkConnectedStayPoints(expectedStayPoints, rootChild3);
		
		// Test child of third child of root hg node
		// Stay point ids that are expected to be referenced by the child hg node
		expectedStayPoints.clear();
		expectedStayPoints.add(10);
		
		checkConnectedStayPoints(expectedStayPoints, child3Child);
		
		// ### Test user with no stay points, so no hg
		user = uIndex.get(NodeProperties.USER_ID, USER_WITHOUT_HG_ID).getSingle();
		assertNotNull("The user node should node be null.", user);
		assertEquals("The user id is not as expected.", USER_WITHOUT_HG_ID, user.getProperty(NodeProperties.USER_ID));
		assertNotNull("The user node should be connected to the graph's reference node.", user.getSingleRelationship(RelTypes.RootUser, Direction.INCOMING));
		
		// The user should not have any outgoing connections
		int relationshipNumber = 0;
		for (Relationship outRel : user.getRelationships(Direction.OUTGOING))
			relationshipNumber++;
		
		assertEquals("The user node should not have any other outgoing connections.", 0, relationshipNumber);
	}
	
	/**
	 * Checks if stay points with the given ids are connected to the given hg node.
	 * 
	 * @param expectedStayPoints the expected stay point ids to find.
	 * @param nodeToCheck the node to check.
	 */
	private static void checkConnectedStayPoints(List<Integer> expectedStayPoints, Node nodeToCheck) {
		// List for unexpected staypoint ids
		List<Object> unexpectedHgStayPoints = new ArrayList<Object>();
		
		// The node should be connected to the expected stay points
		Iterable<Relationship> rootHgNodeStayPoints = nodeToCheck.getRelationships(RelTypes.HasHGStayPoint, Direction.OUTGOING);
		for (Relationship rel : rootHgNodeStayPoints) {
			Node sp = rel.getEndNode();
			Object stayPointId = null;
			// If the end node of the connection is a stay point check if it contains an expected id and remove it from the list
			if ((stayPointId = sp.getProperty(NodeProperties.STAYPOINT_ID, null)) != null)
				if (!expectedStayPoints.remove(stayPointId))
					unexpectedHgStayPoints.add(stayPointId);
		}
		
		assertEquals("There should be no unexpected stay points connected to the given hg node.", 0, unexpectedHgStayPoints.size());
		assertEquals("All expected stay points should be connected to the given hg node.", 0, expectedStayPoints.size());
	}
	
	/**
	 * Builds a Lucene query of the form:
	 * <code>hg_cluster_id:{id} AND user_id:{userId}</code>.
	 * 
	 * @param id the hg cluster id.
	 * @param userId the user id.
	 * @return a Lucene index query to retrieve a hg cluster node from the index.
	 */
	private static String buildFindUserIndexQuery(Object id, Object userId) {
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
	
	@After
	public void resetGraph() {
		Neo4JTestHelper.resetGraph();
	}

}
