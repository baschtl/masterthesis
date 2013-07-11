package de.tub.similarity.analysis;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.helpers.collection.IteratorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.graph.CypherQueries;
import de.tub.similarity.Sequence;
import de.tub.similarity.SimilarSequenceCluster;
import de.tub.util.SimilarityUtil;

/**
 * The <code>Neo4JSimilarityAnalyzer</code> computes the spatial
 * similarity between two users based on their maximal length
 * similar sequences.
 * 
 * @author Sebastian Oelke
 *
 */
public class Neo4JSimilarityAnalyzer implements IAnalyzer<Double> {

	private static final Logger LOG = LoggerFactory.getLogger(Neo4JSimilarityAnalyzer.class);
	
	private Map<Integer, List<Sequence<SimilarSequenceCluster>>> maximalLengthSimilarSequencesOnLevel;
	private Node userNodeOne, userNodeTwo;
	
	public Neo4JSimilarityAnalyzer() {}
	
	/**
	 * @param maximalLengthSimilarSequencesOnLevel the maximal length similar sequences for two users
	 * for different levels of their hierarchical graphs.
	 */
	public Neo4JSimilarityAnalyzer(
			Map<Integer, List<Sequence<SimilarSequenceCluster>>> maximalLengthSimilarSequencesOnLevel,
			Node userOne, Node userTwo) {
		this.maximalLengthSimilarSequencesOnLevel = maximalLengthSimilarSequencesOnLevel;
		this.userNodeOne = userOne;
		this.userNodeTwo = userTwo;
	}
	
	/**
	 * @throws NullPointerException if the list of maximal length similar sequences on level or one of
	 * the user nodes is <code>null</code>.
	 */
	public Double analyze() throws NullPointerException {
		if (maximalLengthSimilarSequencesOnLevel == null)
			throw new NullPointerException(
				"You provided a null value for the required maximal length similar sequences on level. " +
				"This parameter is expected to be non-null.");
		
		if (userNodeOne == null || userNodeTwo == null)
			throw new NullPointerException(
				"You provided a null value for one of the required user nodes. " +
				"Both parameters are expected to be non-null.");
		
		double overallSimilarityScore = 0;
		if (!maximalLengthSimilarSequencesOnLevel.isEmpty()) {
			// Get number of users
			long usersCount = computeUsersCount();
			
			// Go through each level of the given maximal length similar sequences
			Set<Integer> levels = maximalLengthSimilarSequencesOnLevel.keySet();
			for (Integer level : levels) {
				List<Sequence<SimilarSequenceCluster>> currentSequences = maximalLengthSimilarSequencesOnLevel.get(level);
				
				double levelSimilarityScore = 0;
				// Compute the alpha value that is used to weight the overall similarity score
				double alpha = SimilarityUtil.alpha(level);
				
				if (currentSequences != null) {
					if (!currentSequences.isEmpty()) {
						double sequenceSimilarityScore = 0;
						double beta = 0;
						
						for (Sequence<SimilarSequenceCluster> seq : currentSequences) {
							if (seq != null) {
								if (!seq.isEmpty()) {
									// Compute the beta value that is used to weight the level similarity score
									beta = SimilarityUtil.beta(seq.size());
									
									for (int i = 0; i < seq.size(); i++) {
										SimilarSequenceCluster cluster = seq.getCluster(i);
										
										// Compute IDF for each cluster in a sequence, more visitors means less weight for the current cluster
										double idf = computeIdfForHgCluster(cluster.getId(), usersCount);
										
										// Compute sequence similarity score, depends on IDF of current cluster and the minimum number of 
										// times the two users visited the cluster successively 
										sequenceSimilarityScore += idf * cluster.getSuccessivelyInCluster();
										LOG.debug("Successively in cluster: {}, IDF: {}, sequence score: {}", new Object[] {cluster.getSuccessivelyInCluster(), idf, sequenceSimilarityScore});
									}
								} else
									LOG.warn("The similarity for a sequence on level {} could not be computed because it is empty. A sequence similarity score of zero is assumed.", level);
							} else
								LOG.warn("The similarity for a sequence on level {} could not be computed because it is null. A sequence similarity score of zero is assumed.", level);
							
							// Compute level similarity score, depends on sequence similarity score and beta
							levelSimilarityScore += beta * sequenceSimilarityScore;
							LOG.debug("Beta: {}, level score: {}", beta, levelSimilarityScore);
						}
					} else
						LOG.warn("The similarity for the level {} could not be computed because there are no maximal length similar sequences for it. A level similarity score of zero is assumed.", level);
				} else
					LOG.warn("The similarity for the level {} could not be computed because the list of maximal length similar sequences for it is null. A level similarity score of zero is assumed.", level);
				
				// Compute overall similarity score, depends on level similarity score and alpha
				overallSimilarityScore += alpha * levelSimilarityScore;
				LOG.debug("Alpha: {}, overall score: {}", alpha, overallSimilarityScore);
			}
		} else
			LOG.warn("The similarity could not be computed because the given map of maximal length similar sequences is empty. A similarity score of zero is assumed.");
		
		LOG.debug("Overall score before normalization: {}", overallSimilarityScore);
		
		// Normalize the overall similarity score with the number of stay points of both users
		if (overallSimilarityScore != 0) {
			LOG.debug("Normalize overall score with the number of stay points of both users.");
			
			// Get number of stay points of both users
			long userOneStayPointsCount = computeUserStaypoints(userNodeOne);
			long userTwoStayPointsCount = computeUserStaypoints(userNodeTwo);
			
			LOG.debug("Number of stay points of user one: {}, user two: {}", userOneStayPointsCount, userTwoStayPointsCount);
			
			overallSimilarityScore = (overallSimilarityScore / (userOneStayPointsCount * userTwoStayPointsCount));
			
			LOG.debug("Overall score after normalization: {}", overallSimilarityScore);
		}
		
		return overallSimilarityScore;
	}
	
	//###################################################################
	// Helper
	//###################################################################

	/**
	 * Queries the graph database to get the overall number of user nodes.
	 * 
	 * @return the overall number of users.
	 */
	private long computeUsersCount() {
		// Query graph database
		ExecutionResult result = CypherQueries.countUsers();
		
		long usersCount = 0L;
		Iterator<Long> resultIt = result.columnAs("usersCount");
		
		// Extract single returned column
		for (Long count : IteratorUtil.asIterable(resultIt)) {
			usersCount = count;
			break;
		}
		
		return usersCount;
	}
	
	/**
	 * Queries the graph database to get the number of users that visited the
	 * hg cluster with the given hg cluster id. Then, an IDF value is computed
	 * based on the number of users that visited the hg cluster with the given
	 * id and the overall number of users.
	 * 
	 * @param hgClusterId the hg cluster id of the hg cluster to compute the visitors for.
	 * @param overallUsers the overall number of users.
	 * @return the IDF value for the hg cluster.
	 */
	private double computeIdfForHgCluster(String hgClusterId, long overallUsers) {
		// Query graph database for users in given hg cluster
		ExecutionResult result = CypherQueries.countUsersInHgCluster(hgClusterId);
		
		long usersInHgClusterCount = 0L;
		Iterator<Long> resultIt = result.columnAs("usersInHgCluster");
		
		// Extract single returned column
		for (Long count : IteratorUtil.asIterable(resultIt)) {
			usersInHgClusterCount = count;
			break;
		}
		
		LOG.debug("Users in cluster: {}", usersInHgClusterCount);
		
		// Compute IDF
		return SimilarityUtil.idfOfHgCluster(overallUsers, usersInHgClusterCount);
	}
	
	/**
	 * Queries the graph database to compute the number of stay points for a given 
	 * user node.
	 * 
	 * @param user the user node.
	 * @return the number of stay points of the given user node. If the given user node
	 * does not exist in the graph database zero is returned.
	 */
	private long computeUserStaypoints(Node user) {
		// Query graph database for given user
		ExecutionResult result = CypherQueries.countUserStaypoints(user.getId());
		
		long userStayPointsCount = 0L;
		Iterator<Long> resultIt = result.columnAs("userStayPointsCount");
		
		// Extract single returned column
		try {
			for (Long count : IteratorUtil.asIterable(resultIt)) {
				userStayPointsCount = count;
				break;
			}
		} catch (NotFoundException e) {
			LOG.warn("A user node with the id {} could not be found. Hence, it has no stay points.", user.getId());
		}
		
		return userStayPointsCount;
	}

	//###################################################################
	// Setter & Getter
	//###################################################################
	
	/**
	 * @return the maximal length similar sequences of the two users to analyze.
	 */
	public Map<Integer, List<Sequence<SimilarSequenceCluster>>> getMaximalLengthSimilarSequencesOnLevel() {
		return maximalLengthSimilarSequencesOnLevel;
	}

	/**
	 * @param maximalLengthSimilarSequencesOnLevel the maximal length similar sequences of the two users to analyze.
	 */
	public void setMaximalLengthSimilarSequencesOnLevel(
			Map<Integer, List<Sequence<SimilarSequenceCluster>>> maximalLengthSimilarSequencesOnLevel) {
		this.maximalLengthSimilarSequencesOnLevel = maximalLengthSimilarSequencesOnLevel;
	}
	
	/**
	 * @return the node of the first user.
	 */
	public Node getUserNodeOne() {
		return userNodeOne;
	}

	/**
	 * @param userNodeOne the node of the first user to set.
	 */
	public void setUserNodeOne(Node userNodeOne) {
		this.userNodeOne = userNodeOne;
	}

	/**
	 * @return the node of the second user.
	 */
	public Node getUserNodeTwo() {
		return userNodeTwo;
	}

	/**
	 * @param userNodeTwo the node of the second user to set.
	 */
	public void setUserNodeTwo(Node userNodeTwo) {
		this.userNodeTwo = userNodeTwo;
	}
	
}
