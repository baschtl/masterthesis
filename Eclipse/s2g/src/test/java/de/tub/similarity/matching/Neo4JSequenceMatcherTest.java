package de.tub.similarity.matching;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.tub.similarity.Sequence;
import de.tub.similarity.SequenceCluster;
import de.tub.similarity.SequenceWrapper;
import de.tub.similarity.SimilarSequenceCluster;

public class Neo4JSequenceMatcherTest {
	
	public static final double TEMPORAL_CONSTRAINT = 0.41;
	public static final int SPLIT_THRESHOLD = 48;
	public static final int MIN_SEQUENCE_LENGTH = 1;

	public static final String CLUSTER_1 = "1_0";
	public static final String CLUSTER_20 = "2_0";
	public static final String CLUSTER_21 = "2_1";
	public static final String CLUSTER_22 = "2_2";
	public static final String CLUSTER_30 = "3_0";
	public static final String CLUSTER_31 = "3_1";
	
	@Test
	public void testMatch() {
		// Build the matcher
		Neo4JSequenceMatcher matcher = new Neo4JSequenceMatcher(
				buildSequencesForTwoUsers(), 
				SPLIT_THRESHOLD, MIN_SEQUENCE_LENGTH, TEMPORAL_CONSTRAINT);
		// Invoke the matcher
		Map<Integer, List<Sequence<SimilarSequenceCluster>>> matchingResults = matcher.match();
		
		assertEquals("There should be three levels of matched sequences.", 3, matchingResults.keySet().size());
		
		// Test level 1 sequences
		List<Sequence<SimilarSequenceCluster>> level1 = matchingResults.get(1);
		assertEquals("There should be one sequence included for level 1.", 1, level1.size());
		
		Sequence<SimilarSequenceCluster> sequenceLevel1 = level1.get(0);
		assertEquals("The sequence of level 1 should have one cluster.", 1, sequenceLevel1.size());
		assertEquals("The sequence of level 1 should have a cluster with id 1_0.", "1_0", sequenceLevel1.getCluster(0).getId());
		
		// Test level 1 sequences
		List<Sequence<SimilarSequenceCluster>> level2 = matchingResults.get(2);
		assertEquals("There should be one sequence included for level 2.", 1, level2.size());
		
		Sequence<SimilarSequenceCluster> sequenceLevel2 = level2.get(0);
		assertEquals("The sequence of level 2 should have one cluster.", 3, sequenceLevel2.size());
		assertEquals("The sequence of level 2 should have a cluster with id 2_0.", "2_0", sequenceLevel2.getCluster(0).getId());
		assertEquals("The sequence of level 2 should have a cluster with id 2_1.", "2_1", sequenceLevel2.getCluster(1).getId());
		assertEquals("The sequence of level 2 should have a cluster with id 2_2.", "2_2", sequenceLevel2.getCluster(2).getId());
		
		// Test level 1 sequences
		List<Sequence<SimilarSequenceCluster>> level3 = matchingResults.get(3);
		
		assertEquals("There should be one sequence included for level 3.", 1, level3.size());
		
		Sequence<SimilarSequenceCluster> sequenceLevel3 = level3.get(0);
		assertEquals("The sequence of level 3 should have one cluster.", 3, sequenceLevel3.size());
		assertEquals("The sequence of level 3 should have a cluster with id 3_0.", "3_0", sequenceLevel3.getCluster(0).getId());
		assertEquals("The sequence of level 3 should have a cluster with id 3_1.", "3_1", sequenceLevel3.getCluster(1).getId());
		assertEquals("The sequence of level 3 should have a cluster with id 3_0.", "3_0", sequenceLevel3.getCluster(2).getId());
		
		// Test null sequences list
		matcher.setSequencesOnLevel(null);
		boolean exceptionThrown = false;
		
		// Invoke the matcher
		try {
			matchingResults = matcher.match();
		} catch (Exception e) {
			exceptionThrown = true;
		}
		
		assertTrue("An exception should have been thrown for a given null sequences list.", exceptionThrown);
		
		// Test empty sequences list
		matcher.setSequencesOnLevel(new HashMap<Integer, SequenceWrapper>());
		matchingResults = matcher.match();
		
		assertEquals("An empty list of similar sequences should have been returned for a given empty map of sequences on level.",
				0, matchingResults.size());
	}

	private Map<Integer, SequenceWrapper> buildSequencesForTwoUsers() {
		Map<Integer, SequenceWrapper> sequencesOnLevel = new HashMap<Integer, SequenceWrapper>();
		
		// ### Level one
		SequenceWrapper wrapperLevel1 = new SequenceWrapper();
		
		Sequence<SequenceCluster> seq1 = new Sequence<SequenceCluster>();
		SequenceCluster seq1Level1Cluster = new SequenceCluster();
		seq1Level1Cluster.setId(CLUSTER_1);
		seq1Level1Cluster.setSuccessivelyInCluster(13);
		seq1Level1Cluster.setArrivalTime(100L);
		seq1Level1Cluster.setLeavingTime(5000L);
		
		seq1.addCluster(seq1Level1Cluster);
		wrapperLevel1.setFirstSequence(seq1);
		
		Sequence<SequenceCluster> seq2 = new Sequence<SequenceCluster>();
		SequenceCluster seq2Level1Cluster = new SequenceCluster();
		seq2Level1Cluster.setId(CLUSTER_1);
		seq2Level1Cluster.setSuccessivelyInCluster(15);
		seq2Level1Cluster.setArrivalTime(250L);
		seq2Level1Cluster.setLeavingTime(6500L);
		
		seq2.addCluster(seq2Level1Cluster);
		wrapperLevel1.setSecondSequence(seq2);
		
		// ### Level two
		SequenceWrapper wrapperLevel2 = new SequenceWrapper();
		
		Sequence<SequenceCluster> seq3 = new Sequence<SequenceCluster>();
		SequenceCluster seq3Level2Cluster1 = new SequenceCluster();
		seq3Level2Cluster1.setId(CLUSTER_20);
		seq3Level2Cluster1.setSuccessivelyInCluster(5);
		seq3Level2Cluster1.setArrivalTime(100L);
		seq3Level2Cluster1.setLeavingTime(1200L);
		
		SequenceCluster seq3Level2Cluster2 = new SequenceCluster();
		seq3Level2Cluster2.setId(CLUSTER_21);
		seq3Level2Cluster2.setSuccessivelyInCluster(2);
		seq3Level2Cluster2.setArrivalTime(1250L);
		seq3Level2Cluster2.setLeavingTime(2200L);
		
		SequenceCluster seq3Level2Cluster3 = new SequenceCluster();
		seq3Level2Cluster3.setId(CLUSTER_22);
		seq3Level2Cluster3.setSuccessivelyInCluster(6);
		seq3Level2Cluster3.setArrivalTime(2250L);
		seq3Level2Cluster3.setLeavingTime(4000L);
		
		seq3.addCluster(seq3Level2Cluster1);
		seq3.addCluster(seq3Level2Cluster2);
		seq3.addCluster(seq3Level2Cluster3);
		
		wrapperLevel2.setFirstSequence(seq3);
		
		Sequence<SequenceCluster> seq4 = new Sequence<SequenceCluster>();
		SequenceCluster seq4Level2Cluster1 = new SequenceCluster();
		seq4Level2Cluster1.setId(CLUSTER_20);
		seq4Level2Cluster1.setSuccessivelyInCluster(8);
		seq4Level2Cluster1.setArrivalTime(250L);
		seq4Level2Cluster1.setLeavingTime(1300L);
		
		SequenceCluster seq4Level2Cluster2 = new SequenceCluster();
		seq4Level2Cluster2.setId(CLUSTER_21);
		seq4Level2Cluster2.setSuccessivelyInCluster(3);
		seq4Level2Cluster2.setArrivalTime(1350L);
		seq4Level2Cluster2.setLeavingTime(2300L);
		
		SequenceCluster seq4Level2Cluster3 = new SequenceCluster();
		seq4Level2Cluster3.setId(CLUSTER_22);
		seq4Level2Cluster3.setSuccessivelyInCluster(9);
		seq4Level2Cluster3.setArrivalTime(2350L);
		seq4Level2Cluster3.setLeavingTime(4300L);
		
		SequenceCluster seq4Level2Cluster4 = new SequenceCluster();
		seq4Level2Cluster4.setId(CLUSTER_20);
		seq4Level2Cluster4.setSuccessivelyInCluster(13);
		seq4Level2Cluster4.setArrivalTime(4500L);
		seq4Level2Cluster4.setLeavingTime(5900L);
		
		seq4.addCluster(seq4Level2Cluster1);
		seq4.addCluster(seq4Level2Cluster2);
		seq4.addCluster(seq4Level2Cluster3);
		seq4.addCluster(seq4Level2Cluster4);
		
		wrapperLevel2.setSecondSequence(seq4);
		
		// # Level three
		SequenceWrapper wrapperLevel3 = new SequenceWrapper();
		
		Sequence<SequenceCluster> seq5 = new Sequence<SequenceCluster>();
		SequenceCluster seq5Level3Cluster1 = new SequenceCluster();
		seq5Level3Cluster1.setId(CLUSTER_30);
		seq5Level3Cluster1.setSuccessivelyInCluster(7);
		seq5Level3Cluster1.setArrivalTime(4100L);
		seq5Level3Cluster1.setLeavingTime(4330L);
		
		SequenceCluster seq5Level3Cluster2 = new SequenceCluster();
		seq5Level3Cluster2.setId(CLUSTER_31);
		seq5Level3Cluster2.setSuccessivelyInCluster(2);
		seq5Level3Cluster2.setArrivalTime(4400L);
		seq5Level3Cluster2.setLeavingTime(4600L);
		
		SequenceCluster seq5Level3Cluster3 = new SequenceCluster();
		seq5Level3Cluster3.setId(CLUSTER_30);
		seq5Level3Cluster3.setSuccessivelyInCluster(6);
		seq5Level3Cluster3.setArrivalTime(4670L);
		seq5Level3Cluster3.setLeavingTime(5000L);
		
		seq5.addCluster(seq5Level3Cluster1);
		seq5.addCluster(seq5Level3Cluster2);
		seq5.addCluster(seq5Level3Cluster3);
		
		wrapperLevel3.setFirstSequence(seq5);
		
		Sequence<SequenceCluster> seq6 = new Sequence<SequenceCluster>();
		SequenceCluster seq6Level3Cluster1 = new SequenceCluster();
		seq6Level3Cluster1.setId(CLUSTER_31);
		seq6Level3Cluster1.setSuccessivelyInCluster(3);
		seq6Level3Cluster1.setArrivalTime(5900L);
		seq6Level3Cluster1.setLeavingTime(6200L);
		
		SequenceCluster seq6Level3Cluster2 = new SequenceCluster();
		seq6Level3Cluster2.setId(CLUSTER_30);
		seq6Level3Cluster2.setSuccessivelyInCluster(5);
		seq6Level3Cluster2.setArrivalTime(6400L);
		seq6Level3Cluster2.setLeavingTime(6500L);
		
		SequenceCluster seq6Level3Cluster3 = new SequenceCluster();
		seq6Level3Cluster3.setId(CLUSTER_31);
		seq6Level3Cluster3.setSuccessivelyInCluster(4);
		seq6Level3Cluster3.setArrivalTime(6550L);
		seq6Level3Cluster3.setLeavingTime(6600L);
		
		SequenceCluster seq6Level3Cluster4 = new SequenceCluster();
		seq6Level3Cluster4.setId(CLUSTER_30);
		seq6Level3Cluster4.setSuccessivelyInCluster(2);
		seq6Level3Cluster4.setArrivalTime(6650L);
		seq6Level3Cluster4.setLeavingTime(6800L);
		
		seq6.addCluster(seq6Level3Cluster1);
		seq6.addCluster(seq6Level3Cluster2);
		seq6.addCluster(seq6Level3Cluster3);
		seq6.addCluster(seq6Level3Cluster4);
		
		wrapperLevel3.setSecondSequence(seq6);
		
		sequencesOnLevel.put(1, wrapperLevel1);
		sequencesOnLevel.put(2, wrapperLevel2);
		sequencesOnLevel.put(3, wrapperLevel3);
		
		return sequencesOnLevel;
	}
	
}
