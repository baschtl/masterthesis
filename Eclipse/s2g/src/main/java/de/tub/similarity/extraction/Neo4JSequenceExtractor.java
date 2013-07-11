package de.tub.similarity.extraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.IteratorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.data.dao.DAOFactory;
import de.tub.data.dao.Neo4JUserDAO;
import de.tub.graph.CypherQueries;
import de.tub.graph.NodeProperties;
import de.tub.similarity.Sequence;
import de.tub.similarity.SequenceCluster;
import de.tub.similarity.SequenceWrapper;
import de.tub.util.GraphUtil;
import de.tub.util.ListUtil;

/**
 * The <code>Neo4JSequenceExtractor</code> extracts cluster sequences
 * of two given users based on their hierarchical graphs that are persisted
 * in a Neo4j graph database.
 * 
 * @author Sebastian Oelke
 *
 */
public class Neo4JSequenceExtractor implements IExtractor<Map<Integer, SequenceWrapper>> {

	private static final Logger LOG = LoggerFactory.getLogger(Neo4JSequenceExtractor.class);
	
	/**
	 * Holds the cluster sequences for a particular level
	 * on the users hierarchical graphs.
	 */
	private HashMap<Integer, SequenceWrapper> sequencesOnLevel;
	
	private Neo4JUserDAO uDao = (Neo4JUserDAO) DAOFactory.instance().getUserDAO();
	
	private int fromLevel = -1;
	private int toLevel = -1;
	private Node userNodeOne, userNodeTwo;
	
	public Neo4JSequenceExtractor() {}
	
	public Neo4JSequenceExtractor(Node userNodeOne, Node userNodeTwo) {
		this.userNodeOne = userNodeOne;
		this.userNodeTwo = userNodeTwo;
	}
	
	public Neo4JSequenceExtractor(Node userNodeOne, Node userNodeTwo, int fromLevel, int toLevel) {
		this(userNodeOne, userNodeTwo);
		
		this.fromLevel = fromLevel;
		this.toLevel = toLevel;
	}
	
	/**
	 * @return the cluster sequences of two users for each level of their hierarchical
	 * graphs where common clusters were identified. If no common cluster could be found
	 * an empty map is returned.
	 * @throws NullPointerException if one or both of the given users is <code>null</code>.
	 */
	@Override
	public Map<Integer, SequenceWrapper> extract() throws NullPointerException {
		if (userNodeOne == null || userNodeTwo == null)
			throw new NullPointerException(
				"You provided a null value for one or both required user nodes. " +
				"Both parameters are expected to be non-null.");
		
		// Initialize the sequences on level map
		sequencesOnLevel = new HashMap<Integer, SequenceWrapper>();
		
		//###################################################################
		// Step 1: 	Find the common clusters for both users on each level of 
		//			their hierarchical graph.
		//###################################################################
		
		// Get all hg cluster ids of both users based on their node ids
		ExecutionResult result = CypherQueries.allHgClusterIdsForUsersFromToLevel(
				new Long[] {userNodeOne.getId(), 
							userNodeTwo.getId() 
							},			// starting nodes of the query
				fromLevel, toLevel);	// the start and end levels for the query
		
		// Extract the resulting hg cluster ids into a list
		List<String> resultList = new ArrayList<String>();
		Iterator<String> resultIt = result.columnAs(NodeProperties.HG_CLUSTER_ID);
		for (String hgClusterId : IteratorUtil.asIterable(resultIt)) {
			resultList.add(hgClusterId);
		}
		LOG.debug("Hg clusters of both users: {}", resultList);
		
		// Find the common clusters of both users
		Set<String> commonClusterIds = ListUtil.findDuplicates(resultList);
		LOG.debug("Common hg clusters: {}", commonClusterIds);
		
		// If there are no common cluster ids the users do not share clusters. This means they cannot be spatially similar in any way.
		if (commonClusterIds.isEmpty()) {
			LOG.warn("No common clusters were found for the users [{}] and [{}].",
					uDao.getUserId(userNodeOne), uDao.getUserId(userNodeTwo));
			
			// return an empty map
			return sequencesOnLevel;
		}
		
		// Put common clusters into a map that holds the common clusters
		// with the mapping <level, common_clusters_of_level>
		Map<String, List<String>> commonClustersForLevel = mapCommonClustersToLevel(commonClusterIds);
		LOG.debug("Common clusters map: {}", commonClustersForLevel);
		
		//###################################################################
		// Step 2: 	Build the cluster sequences for both users for each
		//			level of common clusters.
		//###################################################################
		
		// Iterate through the identified common cluster levels
		Set<String> levels = commonClustersForLevel.keySet();
		for (String level : levels) {
			// Get common cluster list for level
			List<String> clusterLevelList = commonClustersForLevel.get(level);
			
			String[] clusterLevelArray = clusterLevelList.toArray(new String[0]);
			
			// Get hg clusters for user 1 ordered by leaving time of stay points
			result = CypherQueries.hgClustersInStaypointOrderForUser(userNodeOne.getId(), clusterLevelArray);
			
			// Build up sequence list for the current level for user 1
			Sequence<SequenceCluster> levelSequenceUser1 = createSequenceClusterLevelListFromResult(result);
			
			// Get hg clusters for user 2 ordered by leaving time of stay points
			result = CypherQueries.hgClustersInStaypointOrderForUser(userNodeTwo.getId(), clusterLevelArray);
			
			// Build up sequence list for the current level for user 2
			Sequence<SequenceCluster> levelSequenceUser2 = createSequenceClusterLevelListFromResult(result);
			
			LOG.debug("User 1 level {} sequence: {}", level, levelSequenceUser1);
			LOG.debug("User 2 level {} sequence: {}", level, levelSequenceUser2);
			
			// Wrap both sequences
			SequenceWrapper seqWrapper = new SequenceWrapper(levelSequenceUser1, levelSequenceUser2);
			// Add the current wrapper to the sequence level list for the current level
			sequencesOnLevel.put(Integer.parseInt(level), seqWrapper);
		}
		
		return sequencesOnLevel;
	}
	
	//###################################################################
	// Helper
	//###################################################################

	/**
	 * Converts the results of a Cypher query into a sequence of sequence clusters.
	 * 
	 * @param result the result to convert.
	 * @return a sequence of sequence clusters.
	 * @see de.tub.similarity.Sequence
	 * @see de.tub.similarity.SequenceCluster
	 */
	private Sequence<SequenceCluster> createSequenceClusterLevelListFromResult(ExecutionResult result) {
		// The list holding the sequence clusters
		Sequence<SequenceCluster> levelSequence = new Sequence<SequenceCluster>();
		SequenceCluster currentSequenceCluster = new SequenceCluster();
		
		boolean newCluster = false;
		
		// Read each row of the result
		for (Map<String, Object> row : result) {
			// Read each column of the row separately
			for (Entry<String, Object> column : row.entrySet()) {
				// The column name: column.getKey();
				// The column value: column.getValue();
				
				// Column: hg_cluster_id
				if (column.getKey().equals(NodeProperties.HG_CLUSTER_ID)) {
					String currentHgClusterId = (String) column.getValue();
					
					// Fresh start with new sequence cluster
					if (currentSequenceCluster.getId() == null) {
						// Set current hg cluster id and add sequence cluster to sequence
						currentSequenceCluster.setId(currentHgClusterId);
						levelSequence.addCluster(currentSequenceCluster);
						newCluster = true;
					}
					// A row with the same hg cluster id
					else if (currentSequenceCluster.getId().equals(currentHgClusterId)) {
						// Increase the number of times the user stayed in the cluster
						currentSequenceCluster.incSuccessivelyInCluster();
						newCluster = false;
					}
					// A row with a new hg cluster id
					else if (!currentSequenceCluster.getId().equals(currentHgClusterId)) {
						// Create new current sequence cluster, set hg cluster id, add to sequence
						currentSequenceCluster = new SequenceCluster(currentHgClusterId);
						levelSequence.addCluster(currentSequenceCluster);
						newCluster = true;
					}
				}
				// Column: staypoint_arr
				else if (column.getKey().equals(NodeProperties.STAYPOINT_ARRIVAL)) {
					long currentArrivalTime = (Long) column.getValue();
					
					// The arrival time should only be set if a new cluster was read
					if (newCluster)	currentSequenceCluster.setArrivalTime(currentArrivalTime);
				}
				// Column: staypoint_leav
				else if (column.getKey().equals(NodeProperties.STAYPOINT_LEAVING)) {
					long currentLeavingTime = (Long) column.getValue();
					
					// The leaving time is always set to the latest value
					currentSequenceCluster.setLeavingTime(currentLeavingTime);
				}
			}
		}
		
		return levelSequence;
	}
	
	/**
	 * Returns a map that contains the given common cluster ids mapped to
	 * their corresponding level.
	 * 
	 * @param commonClusterIds the common cluster ids to iterate.
	 * @return a map of the form <code>[level, common_clusters_of_level]</code>.
	 */
	private Map<String, List<String>> mapCommonClustersToLevel(Collection<String> commonClusterIds) {
		Map<String, List<String>> commonClustersForLevel = new HashMap<String, List<String>>();
		
		for (String commonClusterId : commonClusterIds) {
			// Extract the depth in the graph of the current cluster with the help of its id
			String depth = GraphUtil.extractFrameworkClusterDepth(commonClusterId);
			
			// Get cluster id list for the current level
			List<String> levelList = commonClustersForLevel.get(depth);
			// If there is no list for the current level create one
			if (levelList == null) {
				levelList = new ArrayList<String>();
				commonClustersForLevel.put(depth, levelList);
			}
			
			// Add the current cluster id to the level list
			levelList.add(commonClusterId);
		}
		
		return commonClustersForLevel;
	}
	
	//###################################################################
	// Setter & Getter
	//###################################################################

	/**
	 * @return the level of the hierarchical graph of both users to start
	 * the extraction from.
	 */
	public int getFromLevel() {
		return fromLevel;
	}

	/**
	 * @param fromLevel the level of the hierarchical graph of both users to start
	 * the extraction from. Giving a value of <code>-1</code> indicates that the 
	 * extraction is started at the first level.
	 */
	public void setFromLevel(int fromLevel) {
		this.fromLevel = fromLevel;
	}

	/**
	 * @return the level of the hierarchical graph of both users to end
	 * the extraction at.
	 */
	public int getToLevel() {
		return toLevel;
	}

	/**
	 * @param toLevel the level of the hierarchical graph of both users to end
	 * the extraction at. Giving a value of <code>-1</code> indicates that the 
	 * extraction ends at the last level.
	 */
	public void setToLevel(int toLevel) {
		this.toLevel = toLevel;
	}

	/**
	 * @return the first user node.
	 */
	public Node getUserNodeOne() {
		return userNodeOne;
	}

	/**
	 * @param userNodeOne the first user node to set.
	 */
	public void setUserNodeOne(Node userNodeOne) {
		this.userNodeOne = userNodeOne;
	}

	/**
	 * @return the the second user node.
	 */
	public Node getUserNodeTwo() {
		return userNodeTwo;
	}

	/**
	 * @param userNodeTwo the second user node to set.
	 */
	public void setUserNodeTwo(Node userNodeTwo) {
		this.userNodeTwo = userNodeTwo;
	}
	
	
}
