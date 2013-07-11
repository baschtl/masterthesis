package de.tub.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class GraphUtilTest {

	private static Object CLUSTER_ID = "1_0";
	private static Object USER_ID = "3";
	private static Object EXPECTED_RESULT_1 = "3#1_0";
	
	private static String EXPECTED_DEPTH = "1";
	
	@Test
	public void testGenerateHGClusterId() {
		// Test generation of hierarchical graph cluster id
		String generatedId = GraphUtil.generateHGClusterId(USER_ID, CLUSTER_ID);
		assertNotNull("The generated id should not be null.", generatedId);
		assertEquals("The generated hierarchical graph cluster id is not as expected.", EXPECTED_RESULT_1, generatedId);
		
		// Test null values
		generatedId = GraphUtil.generateHGClusterId(null, null);
		assertNull("The generated id should be null if null values are provided.", generatedId);
	}
	
	@Test
	public void testExtractFrameworkClusterDepth() {
		// Test extraction of node depth
		String nodeDepth = GraphUtil.extractFrameworkClusterDepth(CLUSTER_ID.toString());
		assertNotNull("The extracted node depth should not be null.", nodeDepth);
		assertEquals("The extracted node depth is not as expected.", EXPECTED_DEPTH, nodeDepth);
		
		// Test null value
		String nullId = null;
		nodeDepth = GraphUtil.extractFrameworkClusterDepth(nullId);
		assertNull("The extracted node depth should be null.", nodeDepth);
	}
}
