package de.tub.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import de.tub.Neo4JTestHelper;
import de.tub.graph.NodeProperties;

public class GraphUtilDatabaseTest {
	
	private static Object CLUSTER_ID = "1_0";
	private static String EXPECTED_RESULT_1 = "1";
	
	@After
	public void teardownDatabase() {
		// Reset graph database
		Neo4JTestHelper.resetGraph();
	}
	
	@Test
	public void testExtractFrameworkClusterDepth() {
		// Create node
		Node n = null;
		
		Transaction tx = DBUtil.graph().beginTx();
		try {
			n = DBUtil.graph().createNode();
			n.setProperty(NodeProperties.FRAMEWORK_CLUSTER_ID, CLUSTER_ID);
			tx.success();
		} finally {
			tx.finish();
		}
		
		// Test created node
		assertNotNull(n);
		assertEquals(CLUSTER_ID, n.getProperty(NodeProperties.FRAMEWORK_CLUSTER_ID));
		
		// Test extraction of node depth
		String nodeDepth = GraphUtil.extractFrameworkClusterDepth(n);
		assertNotNull("The extracted node depth should not be null.", nodeDepth);
		assertEquals("The extracted node depth is not as expected.", EXPECTED_RESULT_1, nodeDepth);
		
		// Set malformed cluster id
		tx = DBUtil.graph().beginTx();
		try {
			n.setProperty(NodeProperties.FRAMEWORK_CLUSTER_ID, "_1");
			tx.success();
		} finally {
			tx.finish();
		}
		
		// Test extraction of node depth
		nodeDepth = GraphUtil.extractFrameworkClusterDepth(n);
		assertNull("The extracted node depth should be null.", nodeDepth);
	}

}
