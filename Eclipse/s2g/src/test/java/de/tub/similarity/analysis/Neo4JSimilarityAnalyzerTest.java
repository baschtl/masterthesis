package de.tub.similarity.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tub.Neo4JTestHelper;
import de.tub.similarity.Sequence;
import de.tub.similarity.SimilarSequenceCluster;

/**
 * @author Sebastian Oelke
 *
 */
public class Neo4JSimilarityAnalyzerTest {

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
	public void testAnalyze() {
		Map<Integer, List<Sequence<SimilarSequenceCluster>>> maximalLengthSimilarSequencesOnLevel = new HashMap<Integer, List<Sequence<SimilarSequenceCluster>>>();
		
		// Test null sequences
		Neo4JSimilarityAnalyzer analyzer = new Neo4JSimilarityAnalyzer(null, Neo4JTestHelper.userNode1, Neo4JTestHelper.userNode2);
		
		boolean exceptionThrown = false;
		try {
			analyzer.analyze();
		} catch (Exception e) {
			exceptionThrown = true;
		}
		
		assertTrue("An exception should have been thrown for null sequences.", exceptionThrown);
		
		// Test empty sequences
		analyzer.setMaximalLengthSimilarSequencesOnLevel(maximalLengthSimilarSequencesOnLevel);
		double similarityScore = analyzer.analyze();
		
		assertEquals("Given an empty sequence the similarity score should be zero.", 0.0, similarityScore, 0.0);
		
		// Test filled sequences
		// Build sequences: Level 1
		List<Sequence<SimilarSequenceCluster>> sequencesLevel1 = new ArrayList<Sequence<SimilarSequenceCluster>>();
		Sequence<SimilarSequenceCluster> seqLevel1 = new Sequence<SimilarSequenceCluster>();
		
		SimilarSequenceCluster c1Level1 = new SimilarSequenceCluster(Neo4JTestHelper.HG_CLUSTER_LEVEL_1_0, 16, 0L, 0L, 0L, 0L);
		seqLevel1.addCluster(c1Level1);
		sequencesLevel1.add(seqLevel1);
		
		maximalLengthSimilarSequencesOnLevel.put(1, sequencesLevel1);
		
		// Build sequences: Level 2
		List<Sequence<SimilarSequenceCluster>> sequencesLevel2 = new ArrayList<Sequence<SimilarSequenceCluster>>();
		Sequence<SimilarSequenceCluster> seqLevel2 = new Sequence<SimilarSequenceCluster>();
		
		SimilarSequenceCluster c1Level2 = new SimilarSequenceCluster(Neo4JTestHelper.HG_CLUSTER_LEVEL_2_1, 2, 0L, 0L, 0L, 0L);
		SimilarSequenceCluster c2Level2 = new SimilarSequenceCluster(Neo4JTestHelper.HG_CLUSTER_LEVEL_2_1, 5, 0L, 0L, 0L, 0L);
		SimilarSequenceCluster c3Level2 = new SimilarSequenceCluster(Neo4JTestHelper.HG_CLUSTER_LEVEL_2_0, 7, 0L, 0L, 0L, 0L);
		SimilarSequenceCluster c4Level2 = new SimilarSequenceCluster(Neo4JTestHelper.HG_CLUSTER_LEVEL_2_0, 2, 0L, 0L, 0L, 0L);
		
		seqLevel2.addCluster(c1Level2);
		seqLevel2.addCluster(c2Level2);
		seqLevel2.addCluster(c3Level2);
		seqLevel2.addCluster(c4Level2);
		sequencesLevel2.add(seqLevel2);
		
		maximalLengthSimilarSequencesOnLevel.put(2, sequencesLevel2);
		
		// Build sequences: Level 2
		List<Sequence<SimilarSequenceCluster>> sequencesLevel3 = new ArrayList<Sequence<SimilarSequenceCluster>>();
		Sequence<SimilarSequenceCluster> seqLevel3 = new Sequence<SimilarSequenceCluster>();
		
		SimilarSequenceCluster c1Level3 = new SimilarSequenceCluster(Neo4JTestHelper.HG_CLUSTER_LEVEL_3_0, 8, 0L, 0L, 0L, 0L);
		SimilarSequenceCluster c2Level3 = new SimilarSequenceCluster(Neo4JTestHelper.HG_CLUSTER_LEVEL_3_0, 2, 0L, 0L, 0L, 0L);
		SimilarSequenceCluster c3Level3 = new SimilarSequenceCluster(Neo4JTestHelper.HG_CLUSTER_LEVEL_3_0, 6, 0L, 0L, 0L, 0L);
		
		seqLevel3.addCluster(c1Level3);
		seqLevel3.addCluster(c2Level3);
		seqLevel3.addCluster(c3Level3);
		sequencesLevel3.add(seqLevel3);
		
		maximalLengthSimilarSequencesOnLevel.put(3, sequencesLevel3);
		
		// Run similarity measurement
		analyzer.setMaximalLengthSimilarSequencesOnLevel(maximalLengthSimilarSequencesOnLevel);
		similarityScore = analyzer.analyze();
		
		assertEquals("A similarity score of around 0.7 is assumed.", 0.7, similarityScore, 0.005);
	}

}
