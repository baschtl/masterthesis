package de.tub.similarity.matching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.similarity.Sequence;
import de.tub.similarity.SequenceCluster;
import de.tub.similarity.SequenceWrapper;
import de.tub.similarity.SimilarSequenceCluster;
import de.tub.similarity.matching.comparator.SequenceDescendingLengthComparator;
import de.tub.util.DateTimeUtil;

/**
 * The <code>Neo4JSequenceMatcher</code> tries to match cluster sequences
 * of two users for different levels of their hierarchical graph to
 * find a maximal length similar sequence that both users share. 
 * 
 * @author Sebastian Oelke
 *
 */
public class Neo4JSequenceMatcher implements IMatcher<Map<Integer, List<Sequence<SimilarSequenceCluster>>>> {

	private static final Logger LOG = LoggerFactory.getLogger(Neo4JSequenceMatcher.class);
	
	private Map<Integer, List<Sequence<SimilarSequenceCluster>>> maximalLengthSimilarSequencesOnLevel = new HashMap<Integer, List<Sequence<SimilarSequenceCluster>>>();
	private Map<Integer, SequenceWrapper> sequencesOnLevel;
	private double temporalConstraintThreshold;
	private int splitThreshold, minSequenceLength = 1;
	
	/**
	 * @param sequencesOnLevel the sequences of two users on different levels of their hierarchical graphs.
	 * @param splitThreshold the threshold in hours to use when splitting sequences in sub-sequences. If the transition time
	 * between two consecutive clusters of a sequence exceeds this value the sequence is splitted.
	 * @param minSequenceLength the minimum length a similar sequence has to have to be recognized. Defaults to a 
	 * length of one which is the minimal value possible.
	 * @param temporalConstraintThreshold the temporal constraint threshold used to ensure that the sequences 
	 * of two users have similar transition times between consecutive clusters.
	 */
	public Neo4JSequenceMatcher(Map<Integer, SequenceWrapper> sequencesOnLevel, 
			int splitThreshold, int minSequenceLength, double temporalConstraintThreshold) {
		this.sequencesOnLevel = sequencesOnLevel;
		this.splitThreshold = splitThreshold;
		this.minSequenceLength = (minSequenceLength >= 1) ? minSequenceLength : 1;
		this.temporalConstraintThreshold = temporalConstraintThreshold;
	}
	
	public Neo4JSequenceMatcher(int splitThreshold, int minSequenceLength, double temporalConstraintThreshold) {
		this(null, splitThreshold, minSequenceLength, temporalConstraintThreshold);
	}
	
	/**
	 * @throws NullPointerException if the list of sequences on each level is <code>null</code>.
	 */
	public Map<Integer, List<Sequence<SimilarSequenceCluster>>> match() throws NullPointerException {
		if (sequencesOnLevel == null)
			throw new NullPointerException(
				"You provided a null value for the required sequences on level. " +
				"This parameter is expected to be non-null.");
		
		// Reset the found maximal length similar sequences on level for the current run
		maximalLengthSimilarSequencesOnLevel = new HashMap<Integer, List<Sequence<SimilarSequenceCluster>>>();
		
		// Go through each level of the given sequences
		Set<Integer> levels = sequencesOnLevel.keySet();
		for (Integer level : levels) {
			// Get the sequences for both users for this level
			SequenceWrapper wrapper = sequencesOnLevel.get(level);
			if (wrapper != null) {
				//###################################################################
				// Step 1: 	Split the sequences of both users in sub-sequences.
				//###################################################################
				
				// Get both users sequences
				Sequence<SequenceCluster> levelSequenceUser1 = wrapper.getFirstSequence();
				Sequence<SequenceCluster> levelSequenceUser2 = wrapper.getSecondSequence();
				
				// Split the sequences of both users based on the split threshold
				List<Sequence<SequenceCluster>> splittedLevelSequenceListUser1 = splitSequence(levelSequenceUser1);
				List<Sequence<SequenceCluster>> splittedLevelSequenceListUser2 = splitSequence(levelSequenceUser2);
				
				LOG.debug("Level {}, User 1 splitted sequences: {}", level, splittedLevelSequenceListUser1);
				LOG.debug("Level {}, User 2 splitted sequences: {}", level, splittedLevelSequenceListUser2);
				
				//###################################################################
				// Step 2: 	Match all found sub-sequences against each other
				//			and apply the temporal constraint. Finally, add
				//			all found maximal length similar sequences to the
				//			list of sequences for the current level.
				//###################################################################
				
				for (int i = 0; i < splittedLevelSequenceListUser1.size(); i++) {
					Sequence<SequenceCluster> currentSeqUser1 = splittedLevelSequenceListUser1.get(i);
					for (int j = 0; j < splittedLevelSequenceListUser2.size(); j++) {
						Sequence<SequenceCluster> currentSeqUser2 = splittedLevelSequenceListUser2.get(j);
						
						// Search for similar sequences in the current sequences
						List<Sequence<SimilarSequenceCluster>> maxLengthSimilarSequences = searchMaximalLengthSimilarSequences(currentSeqUser1, currentSeqUser2);
						if (!maxLengthSimilarSequences.isEmpty()) {
							// Check if there is already a list of sequences for the current level
							List<Sequence<SimilarSequenceCluster>> levelList = maximalLengthSimilarSequencesOnLevel.get(level);
							if (levelList == null) {
								// There exists no list for maximal length similar sequences for the current level, create it
								levelList = new ArrayList<Sequence<SimilarSequenceCluster>>();
								maximalLengthSimilarSequencesOnLevel.put(level, levelList);
							}
							// Add the retrieved maximal length similar sequences to the current level
							levelList.addAll(maxLengthSimilarSequences);
						}
					}
				}
			} else 
				LOG.warn("A null sequence wrapper was found for level {}. Going to next level.", level);
		}
		
		if (maximalLengthSimilarSequencesOnLevel.isEmpty())
			LOG.warn("There were no maximal length similar sequences found.");
		else
			LOG.debug("Maximal length similar sequences: {}", maximalLengthSimilarSequencesOnLevel);
		
		return maximalLengthSimilarSequencesOnLevel;
	}
	
	//###################################################################
	// Helper
	//###################################################################
	
	/**
	 * Goes through the given sequence and splits it if the transition time between
	 * two consecutive clusters is greater than the split threshold.
	 * 
	 * @param sequence the sequence to split.
	 * @return a list of sub sequences if there are any.
	 */
	private List<Sequence<SequenceCluster>> splitSequence(Sequence<SequenceCluster> sequence) {
		// Case 1: Sequence is null or empty
		if (sequence == null || sequence.isEmpty()) return new ArrayList<Sequence<SequenceCluster>>();
		
		List<Sequence<SequenceCluster>> listOfSequences = new ArrayList<Sequence<SequenceCluster>>();
		
		int startIndex = 0;
		
		// Case 2: Sequence has only one cluster
		// Add the sequence to the final list and return the list
		if (sequence.size() == 1) {
			listOfSequences.add(sequence);
			return listOfSequences;
		}
		
		// Case 3: Sequence has more than one cluster
		// Get first cluster of sequence
		SequenceCluster firstCluster = sequence.getCluster(startIndex);
		SequenceCluster secondCluster;
		
		// Go through the rest of the sequence
		for (int i = 1; i < sequence.size(); i++) {
			secondCluster = sequence.getCluster(i);
			
			// Split the sequence in two parts if the split threshold is exceeded
			if (DateTimeUtil.differenceInHours(firstCluster.getLeavingTime(), secondCluster.getArrivalTime()) > splitThreshold) {
				listOfSequences.add(sequence.subList(startIndex, i));
				startIndex = i;
			}
			// At the end of the sequence add the remaining part to the final list of sequences
			if (i == sequence.size() - 1)
				listOfSequences.add(sequence.subList(startIndex, i + 1));
			
			firstCluster = secondCluster;
		}
		
		return listOfSequences;
	}
	
	/**
	 * Tries to find similar sequences within the given two sequences based on the 
	 * dynamic programming approach used to solve the <i>longest common subsequence problem</i>.
	 * If multiple similar sequences were found those with the maximal length are returned.
	 * <p />
	 * For an introduction to the longest common subsequence problem have a look at:<br />
	 * {@link http://en.wikipedia.org/wiki/Longest_common_subsequence_problem}
	 * 
	 * @param seq1 the first sequence of sequence clusters.
	 * @param seq2 the second sequence of sequence clusters.
	 * @return the maximal length similar sequences that could be found within the given two sequences. 
	 * The returned list may be empty if no similar sequences could be found.
	 */
	private List<Sequence<SimilarSequenceCluster>> searchMaximalLengthSimilarSequences(Sequence<SequenceCluster> seq1, Sequence<SequenceCluster> seq2) {
		// --> Start: Longest common subsequence problem algorithm
		
		// Compute length of common subsequences
		int[][] c = computeLengthOfCommonSubsequences(seq1, seq2);
		
		// Extract all common subsequences with the help of the matrix
		List<Sequence<SimilarSequenceCluster>> result = new ArrayList<Sequence<SimilarSequenceCluster>>(backtrackAll(c, seq1, seq2, seq1.size(), seq2.size()));
		
		// <-- End: Longest common subsequence problem algorithm
		// --> Start: Post-processing of common subsequences
		
		// This list holds the final maximal length similar sequences for which the temporal constraint was checked successfully
		List<Sequence<SimilarSequenceCluster>> maximalLengthSimilarSequencesCheckedTempConstraint = new ArrayList<Sequence<SimilarSequenceCluster>>();
		
		// Do not proceed if there were no common subsequences found
		if (!result.isEmpty()) {
			// Extract the longest found common sequences
			List<Sequence<SimilarSequenceCluster>> maximalLengthSimilarSequences = extractMaximalLengthSimilarSequences(result);
			
			// Do not proceed if there was no sequence found that holds the criteria for a maximal
			// length similar sequence (i.e., sequence.size >= minSequenceLength)
			if (!maximalLengthSimilarSequences.isEmpty()) {
				// Apply temporal constraint threshold to each found maximal length similar sequence
				for (int i = 0; i < maximalLengthSimilarSequences.size(); i++) {
					Sequence<SimilarSequenceCluster> currentSeq = maximalLengthSimilarSequences.get(i);
					Sequence<SimilarSequenceCluster> checkedSequence = checkTemporalConstraintForSimilarSequence(currentSeq, false);
					// If the sequence is empty or null after applying the temporal constraint ignore it
					if (checkedSequence != null && !checkedSequence.isEmpty())
						maximalLengthSimilarSequencesCheckedTempConstraint.add(checkedSequence);
				}
			}
		}
		
		// <-- End: Post-processing of common subsequences
		
		return maximalLengthSimilarSequencesCheckedTempConstraint;
	}
	
	/**
	 * Returns a matrix of integer values that holds the length of the found similar
	 * subsequences.
	 * 
	 * @param seq1 the first sequence to compare.
	 * @param seq2 the second sequence to compare.
	 * @return a matrix of integer values that holds the length of the found similar
	 * subsequences.
	 */
	private int[][] computeLengthOfCommonSubsequences(Sequence<SequenceCluster> seq1, Sequence<SequenceCluster> seq2) {
		int[][] c = new int[seq1.size() + 1][seq2.size() + 1];
		
		// Compute length of longest common subsequences into a matrix
		for (int i = 1; i <= seq1.size(); i++) {
			String seq1ClusterId = seq1.getCluster(i - 1).getId();
			for (int j = 1; j <= seq2.size(); j++) {
				if (seq1ClusterId.equals(seq2.getCluster(j - 1).getId()))
					c[i][j] = c[i-1][j-1] + 1;
				else
					c[i][j] = Math.max(c[i][j-1], c[i-1][j]);
			}
		}
		
		return c;
	}
	
	/**
	 * Based on the length of longest common subsequences computed in <code>computeLengthOfCommonSubsequences()</code> 
	 * this method extracts the actual common subsequences.
	 * 
	 * @param c the matrix that holds the length of longest common subsequences.
	 * @param seq1 the first sequence that was analyzed.
	 * @param seq2 the second sequence that was analyzed.
	 * @param i a control variable used to access elements of <code>seq1</code>.
	 * @param j a control variable used to access elements of <code>seq2</code>.
	 * @return a set of all found similar sequences. Duplicates are not returned.
	 */
	private Set<Sequence<SimilarSequenceCluster>> backtrackAll(
			int[][] c, Sequence<SequenceCluster> seq1, Sequence<SequenceCluster> seq2, int i, int j) {
		
		// Beginning of one sequence reached, stop here
		if (i == 0 || j == 0) {
	        return new HashSet<Sequence<SimilarSequenceCluster>>();
		}
		
		// An element of both sequences is equal
		else if (seq1.getCluster(i - 1).getId().equals(seq2.getCluster(j - 1).getId())) {
			Set<Sequence<SimilarSequenceCluster>> backTrackSet = backtrackAll(c, seq1, seq2, i - 1, j - 1);
			Set<Sequence<SimilarSequenceCluster>> returnSet = new HashSet<Sequence<SimilarSequenceCluster>>();
	        
	        SequenceCluster currentSeq1Cluster = seq1.getCluster(i - 1);
	        SequenceCluster currentSeq2Cluster = seq2.getCluster(j - 1);
	        
	        // Create a similar sequence cluster and set the successively in cluster count to
	        // the minimum of both current clusters
	        SimilarSequenceCluster clusterToAdd = new SimilarSequenceCluster(
	        										currentSeq1Cluster.getId(), 
	        										Math.min(currentSeq1Cluster.getSuccessivelyInCluster(),
	        						        				currentSeq2Cluster.getSuccessivelyInCluster()),
	        						        		currentSeq1Cluster.getArrivalTime(), 
	        						        		currentSeq1Cluster.getLeavingTime(), 
	        						        		currentSeq2Cluster.getArrivalTime(), 
	        						        		currentSeq2Cluster.getLeavingTime());
	        
	        // Add the similar sequence cluster to the end of the collected sequences
	        for (Sequence<SimilarSequenceCluster> seq : backTrackSet)
	        	returnSet.add(seq.addCluster(clusterToAdd));
	        
	        Sequence<SimilarSequenceCluster> tempSeq = new Sequence<SimilarSequenceCluster>();
	        tempSeq.addCluster(clusterToAdd);
	        returnSet.add(tempSeq);
	        
	        return returnSet;
	    }
		// The current elements of both sequences are not equal, go to a previous element,
		// the element with the higher number is taken
		else {
			Set<Sequence<SimilarSequenceCluster>> returnSet = new HashSet<Sequence<SimilarSequenceCluster>>();
	        if (c[i][j - 1] >= c[i - 1][j]) {
	            returnSet = backtrackAll(c, seq1, seq2, i, j - 1);
	        }
	        if (c[i - 1][j] >= c[i][j - 1]) {
	            returnSet.addAll(backtrackAll(c, seq1, seq2, i - 1, j));
	        }
	        return returnSet;
	    }
	}
	
	/**
	 * Extracts those sequences of the given sequences list that have a maximum length and 
	 * redeem the <code>minSequenceLength</code> parameter.
	 * 
	 * @param sequences the list of sequences that should be analyzed.
	 * @return sequences with a maximal length that have at least <code>minSequenceLength</code> elements.
	 */
	private <T> List<Sequence<T>> extractMaximalLengthSimilarSequences(List<Sequence<T>> sequences) {
		int sequencesSize = sequences.size();
		// The given list of sequences holds more than one sequence
		if (sequencesSize > 1) {
			// Sort the sequences in descending order of length
			Collections.sort(sequences, new SequenceDescendingLengthComparator<T>());
		
			int maxLength = Integer.MIN_VALUE;
			List<Sequence<T>> maximalLengthSimilarSequences = new ArrayList<Sequence<T>>();
			
			// Find the included sequences with maximum length ...
			for (int i = 0; i < sequencesSize; i++) {
				Sequence<T> currentSeq = sequences.get(i);
				
				// ... that have at least minSequenceLength elements
				if (currentSeq.size() >= maxLength && currentSeq.size() >= minSequenceLength) {
					maximalLengthSimilarSequences.add(sequences.get(i));
					maxLength = currentSeq.size();
				} else
					break;
			}
		
			return maximalLengthSimilarSequences;
		}
		// The given list of sequences holds only one sequence
		else if (sequencesSize == 1) {
			// This one sequence has to have at least minSequenceLength elements
			if (sequences.get(0).size() >= minSequenceLength)
				return sequences;
			else {
				sequences.clear();
				return sequences;
			}
		} else {
			return sequences;
		}
	}
	
	/**
	 * Checks if the temporal constraint was met for the given similar sequence.
	 *  
	 * @param seq the similar sequence to check the temporal constraint for.
	 * @param checkedTemporalConstraint <code>true</code> if the given similar sequence was already 
	 * successfully checked for the temporal constraint, <code>false</code> otherwise. If you call 
	 * this method you should give <code>false</code> for this argument since you want to let the 
	 * similar sequence be checked, right?
	 * @return If the given similar sequence mets the temporal constraint between each pair of 
	 * clusters the sequence is returned as given. If the temporal constraint was not met between 
	 * a pair of clusters the first of those clusters is rejected. The similar sequence is checked 
	 * as long as the sequence consists of more than one cluster and the temporal constraint was 
	 * not met in the last run.<br />
	 * If the similar sequence consists of only one cluster (e.g., it was given to this method like
	 * that or after several passes of not redeeming the temporal constraint) the overlapping of
	 * the intervals both users the similar sequences originates from spent in the cluster is inspected. 
	 * If there is an overlap between both intervals the similar sequence is returned with this one
	 * cluster, otherwise an empty sequence is returned.
	 */
	private Sequence<SimilarSequenceCluster> checkTemporalConstraintForSimilarSequence(
			Sequence<SimilarSequenceCluster> seq, boolean checkedTemporalConstraint) {
		// If the temporal constraint was met or the given sequence has no clusters return it
		if (checkedTemporalConstraint || seq.isEmpty()) {
			return seq;
		}
		
		// The sequence only has one element
		if (seq.size() == 1) {
			SimilarSequenceCluster cluster = seq.getCluster(0);
			
			// Check for overlap of visiting intervals of the only element
			if (DateTimeUtil.doTimeIntervalsOverlap(
					cluster.getArrivalTime(), cluster.getLeavingTime(), 
					cluster.getSecondArrivalTime(), cluster.getSecondLeavingTime()))
				// The intervals overlap, so this sequence can be processed further
				return seq;
			else
				return new Sequence<SimilarSequenceCluster>();
		}
		
		// The sequence has at least two elements
		if (!checkedTemporalConstraint) {
			boolean checkedTemporalConstraintInLoop = true;
			
			Sequence<SimilarSequenceCluster> tempSequence = new Sequence<SimilarSequenceCluster>();
			
			for (int j = 0; j < seq.size() - 1; j++) {
				SimilarSequenceCluster cluster1 = seq.getCluster(j);
				SimilarSequenceCluster cluster2 = seq.getCluster(j + 1);
				
				boolean tempConstraintRedeemed = DateTimeUtil.isTemporalConstraintRedeemed(
						cluster2.getArrivalTime(), 
						cluster1.getLeavingTime(), 
						cluster2.getSecondArrivalTime(), 
						cluster1.getSecondLeavingTime(), 
						temporalConstraintThreshold);
				
				if (tempConstraintRedeemed)
					tempSequence.addCluster(cluster1);
				else
					checkedTemporalConstraintInLoop = false;
				
				if (j+1 == seq.size()-1) {
					tempSequence.addCluster(cluster2);
					if (checkedTemporalConstraintInLoop) checkedTemporalConstraint = true;
				}
			}
			
			seq = checkTemporalConstraintForSimilarSequence(tempSequence, checkedTemporalConstraint);
		}
		
		return seq;
	}
	
	//###################################################################
	// Setter & Getter
	//###################################################################

	/**
	 * @return the sequences on the levels of the hierarchical graphs of two users.
	 */
	public Map<Integer, SequenceWrapper> getSequencesOnLevel() {
		return sequencesOnLevel;
	}

	/**
	 * @param sequencesOnLevel the sequences on the levels of the hierarchical graphs of two users to set.
	 */
	public void setSequencesOnLevel(Map<Integer, SequenceWrapper> sequencesOnLevel) {
		this.sequencesOnLevel = sequencesOnLevel;
	}

	/**
	 * @return the temporal constraint threshold used to ensure that the sequences 
	 * of two users have similar transition times between consecutive clusters.
	 */
	public double getTemporalConstraintThreshold() {
		return temporalConstraintThreshold;
	}

	/**
	 * @param temporalConstraintThreshold the temporal constraint threshold used to 
	 * ensure that the sequences of two users have similar transition times between 
	 * consecutive clusters to set.
	 */
	public void setTemporalConstraintThreshold(double temporalConstraintThreshold) {
		this.temporalConstraintThreshold = temporalConstraintThreshold;
	}

	/**
	 * @return the threshold in hours to use when splitting sequences in sub-sequences.
	 */
	public int getSplitThreshold() {
		return splitThreshold;
	}

	/**
	 * @param splitThreshold the threshold in hours to use when splitting sequences 
	 * in sub-sequences to set.
	 */
	public void setSplitThreshold(int splitThreshold) {
		this.splitThreshold = splitThreshold;
	}

	/**
	 * @return the minimal length of similar sequences to include.
	 */
	public int getMinSequenceLength() {
		return minSequenceLength;
	}

	/**
	 * @param minSequenceLength the minimal length of included similar sequences to set.
	 */
	public void setMinSequenceLength(int minSequenceLength) {
		this.minSequenceLength = minSequenceLength;
	}
}
