package de.tub.similarity.extraction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tub.Neo4JTestHelper;
import de.tub.similarity.Sequence;
import de.tub.similarity.SequenceCluster;
import de.tub.similarity.SequenceWrapper;

/**
 * @author Sebastian Oelke
 *
 */
public class Neo4JSequenceExtractorTest {
	
	@AfterClass
	public static void resetGraph() {
		Neo4JTestHelper.resetGraph();
	}
	
	@BeforeClass
	public static void buildGraph() {
		// Generate hg for two users
		Neo4JTestHelper.generateHg(true);
	}
	
	@Test
	public void testExtract() {
		// Extract sequences
		Neo4JSequenceExtractor ex = new Neo4JSequenceExtractor(Neo4JTestHelper.userNode1, Neo4JTestHelper.userNode2);
		Map<Integer, SequenceWrapper> extractedSequences = ex.extract();
		
		// ### Test level 1 sequences
		SequenceWrapper seqWrapLev1 = extractedSequences.get(1);
		
		// User 1
		Sequence<SequenceCluster> seqLev1U1 = seqWrapLev1.getFirstSequence();
		assertNotNull("The sequence should not be null.", seqLev1U1);
		assertEquals("The size of the sequence is not right.", 1, seqLev1U1.size());
		
		SequenceCluster c = seqLev1U1.getCluster(0);
		assertEquals("The id of the cluster is not right.", Neo4JTestHelper.HG_CLUSTER_LEVEL_1_0, c.getId());
		assertEquals("The successively in cluster count of the cluster is not right.", 6, c.getSuccessivelyInCluster());
		assertEquals("The arrival time of the cluster is not right.", 1L, c.getArrivalTime());
		assertEquals("The leaving time of the cluster is not right.", 8L, c.getLeavingTime());
		
		// User 2
		Sequence<SequenceCluster> seqLev1U2 = seqWrapLev1.getSecondSequence();
		assertNotNull("The sequence should not be null.", seqLev1U2);
		assertEquals("The size of the sequence is not right.", 1, seqLev1U2.size());
		
		c = seqLev1U2.getCluster(0);
		assertEquals("The id of the cluster is not right.", Neo4JTestHelper.HG_CLUSTER_LEVEL_1_0, c.getId());
		assertEquals("The successively in cluster count of the cluster is not right.", 7, c.getSuccessivelyInCluster());
		assertEquals("The arrival time of the cluster is not right.", 1L, c.getArrivalTime());
		assertEquals("The leaving time of the cluster is not right.", 12L, c.getLeavingTime());
		
		// ### Test level 2 sequences
		SequenceWrapper seqWrapLev2 = extractedSequences.get(2);
		
		// User 1
		Sequence<SequenceCluster> seqLev2U1 = seqWrapLev2.getFirstSequence();
		assertNotNull("The sequence should not be null.", seqLev2U1);
		assertEquals("The size of the sequence is not right.", 3, seqLev2U1.size());
		
		c = seqLev2U1.getCluster(0);
		assertEquals("The id of the cluster is not right.", Neo4JTestHelper.HG_CLUSTER_LEVEL_2_0, c.getId());
		assertEquals("The successively in cluster count of the cluster is not right.", 2, c.getSuccessivelyInCluster());
		assertEquals("The arrival time of the cluster is not right.", 1L, c.getArrivalTime());
		assertEquals("The leaving time of the cluster is not right.", 3L, c.getLeavingTime());
		
		c = seqLev2U1.getCluster(1);
		assertEquals("The id of the cluster is not right.", Neo4JTestHelper.HG_CLUSTER_LEVEL_2_1, c.getId());
		assertEquals("The successively in cluster count of the cluster is not right.", 2, c.getSuccessivelyInCluster());
		assertEquals("The arrival time of the cluster is not right.", 3L, c.getArrivalTime());
		assertEquals("The leaving time of the cluster is not right.", 5L, c.getLeavingTime());
		
		c = seqLev2U1.getCluster(2);
		assertEquals("The id of the cluster is not right.", Neo4JTestHelper.HG_CLUSTER_LEVEL_2_0, c.getId());
		assertEquals("The successively in cluster count of the cluster is not right.", 2, c.getSuccessivelyInCluster());
		assertEquals("The arrival time of the cluster is not right.", 5L, c.getArrivalTime());
		assertEquals("The leaving time of the cluster is not right.", 8L, c.getLeavingTime());
		
		// User 2
		Sequence<SequenceCluster> seqLev2U2 = seqWrapLev2.getSecondSequence();
		assertNotNull("The sequence should not be null.", seqLev2U2);
		assertEquals("The size of the sequence is not right.", 3, seqLev2U2.size());
		
		c = seqLev2U2.getCluster(0);
		assertEquals("The id of the cluster is not right.", Neo4JTestHelper.HG_CLUSTER_LEVEL_2_0, c.getId());
		assertEquals("The successively in cluster count of the cluster is not right.", 1, c.getSuccessivelyInCluster());
		assertEquals("The arrival time of the cluster is not right.", 1L, c.getArrivalTime());
		assertEquals("The leaving time of the cluster is not right.", 2L, c.getLeavingTime());
		
		c = seqLev2U2.getCluster(1);
		assertEquals("The id of the cluster is not right.", Neo4JTestHelper.HG_CLUSTER_LEVEL_2_1, c.getId());
		assertEquals("The successively in cluster count of the cluster is not right.", 3, c.getSuccessivelyInCluster());
		assertEquals("The arrival time of the cluster is not right.", 2L, c.getArrivalTime());
		assertEquals("The leaving time of the cluster is not right.", 5L, c.getLeavingTime());
		
		c = seqLev2U2.getCluster(2);
		assertEquals("The id of the cluster is not right.", Neo4JTestHelper.HG_CLUSTER_LEVEL_2_0, c.getId());
		assertEquals("The successively in cluster count of the cluster is not right.", 3, c.getSuccessivelyInCluster());
		assertEquals("The arrival time of the cluster is not right.", 5L, c.getArrivalTime());
		assertEquals("The leaving time of the cluster is not right.", 12L, c.getLeavingTime());
		
		// ### Test level 3 sequences
		SequenceWrapper seqWrapLev3 = extractedSequences.get(3);
		
		// User 1
		Sequence<SequenceCluster> seqLev3U1 = seqWrapLev3.getFirstSequence();
		assertNotNull("The sequence should not be null.", seqLev3U1);
		assertEquals("The size of the sequence is not right.", 1, seqLev3U1.size());
		
		c = seqLev3U1.getCluster(0);
		assertEquals("The id of the cluster is not right.", Neo4JTestHelper.HG_CLUSTER_LEVEL_3_0, c.getId());
		assertEquals("The successively in cluster count of the cluster is not right.", 4, c.getSuccessivelyInCluster());
		assertEquals("The arrival time of the cluster is not right.", 1L, c.getArrivalTime());
		assertEquals("The leaving time of the cluster is not right.", 8L, c.getLeavingTime());
		
		// User 2
		Sequence<SequenceCluster> seqLev3U2 = seqWrapLev3.getSecondSequence();
		assertNotNull("The sequence should not be null.", seqLev3U2);
		assertEquals("The size of the sequence is not right.", 1, seqLev3U2.size());
		
		c = seqLev3U2.getCluster(0);
		assertEquals("The id of the cluster is not right.", Neo4JTestHelper.HG_CLUSTER_LEVEL_3_0, c.getId());
		assertEquals("The successively in cluster count of the cluster is not right.", 3, c.getSuccessivelyInCluster());
		assertEquals("The arrival time of the cluster is not right.", 5L, c.getArrivalTime());
		assertEquals("The leaving time of the cluster is not right.", 12L, c.getLeavingTime());
	}
}
