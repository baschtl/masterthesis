package de.tub.graph;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.cypher.MissingIndexException;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.util.DBUtil;
import de.tub.util.ListUtil;

/**
 * This class provides methods to execute Cypher queries on
 * the Neo4j graph database.
 * 
 * @author Sebastian Oelke
 *
 */
public class CypherQueries {
	
	private static final Logger LOG = LoggerFactory.getLogger(CypherQueries.class);

	/**
	 * This method queries the graph database with a cypher query
	 * to receive all hg cluster ids of the given user nodes. 
	 * The depth of the resulting hg clusters
	 * can be tweaked by the parameters <code>fromLevel</code> and
	 * <code>toLevel</code>. Negative values for the parameters result
	 * in querying all depths. A <code>fromLevel</code> of zero is also ignored
	 * to avoid the inclusion of the starting node (i.e., the user node).
	 * <p />
	 * The resulting query looks like the following:
	 * 
	 * <pre>
	 * START n=node({id})
	 * MATCH n-[:HasHG|HasHGChildCluster*]->hgc
	 * RETURN hgc.hg_cluster_id AS hg_cluster_id
	 * ORDER BY hgc.hg_cluster_id
	 * </pre>
	 * 
	 * The result column of the returned <code>ExecutionResult</code>
	 * is named 'hg_cluster_id' (cf. the generated Cypher query). To access it use this name.
	 * <p />
	 * @param userNodeIds the ids of the starting user nodes. At least one user node id has to be provided.
	 * @param fromLevel the starting depth of the query. This has to be greater than zero. Otherwise it is ignored.
	 * @param toLevel the end depth of the query. This has to be greater or equal zero. Otherwise it is ignored.
	 * @return the result of the cypher query in form of an <code>ExecutionResult</code> which includes all found hg cluster ids.
	 * @throws NullPointerException if the array of user node ids is <code>null</code>.
	 * @throws IllegalArgumentException if the array of starting user nodes is empty.
	 * 
	 * @see org.neo4j.cypher.javacompat.ExecutionResult ExecutionResult
	 */
	public static ExecutionResult allHgClusterIdsForUsersFromToLevel(
			Long[] userNodeIds, int fromLevel, int toLevel) throws NullPointerException, IllegalArgumentException {
		if (userNodeIds == null)
			throw new NullPointerException(
				"You provided a null value for the list of user node ids. This parameter is expected to be non-null. " +
				"The query cannot be performed without any starting node.");
		
		if (userNodeIds.length == 0)
			throw new IllegalArgumentException(
					"You provided an empty list of user node ids. The list " +
					"has to include at least one valid user node id. The query cannot be " +
					"performed without any starting node.");
		
		// Build parameter map for cypher query
		Map<String, Object> params = new HashMap<String, Object>();
		// Ids of the two starting nodes
    	params.put("id", Arrays.asList(userNodeIds));
		
    	// Level string, default is to search all levels and ignore the starting node
    	String interval = "*";
    	String fromInterval = "";
    	String toInterval = "";
    	
    	// Check the from and to level parameters
    	if (fromLevel > 0) fromInterval = String.valueOf(fromLevel);
    	if (toLevel >= 0) toInterval = String.valueOf(toLevel);
    	
    	// Build final level string based on fromLevel and toLevel
    	if (!fromInterval.isEmpty() || !toInterval.isEmpty())
    		interval += fromInterval + ".." + toInterval;
    	
    	// Build the query
		StringBuilder builder = new StringBuilder();
		builder.append("START n=node({id}) ")
			.append("MATCH n-[:")
			.append(RelTypes.HasHG)
			.append("|")
			.append(RelTypes.HasHGChildCluster)
			.append(interval)
			.append("]->hgc ")
			.append("RETURN hgc.")
			.append(NodeProperties.HG_CLUSTER_ID)
			.append(" AS ")
			.append(NodeProperties.HG_CLUSTER_ID)
			.append(" ORDER BY hgc.")
			.append(NodeProperties.HG_CLUSTER_ID);
		
		String stringQuery = builder.toString();
		
		logCypherQuery(stringQuery);
		
		// Execute the query and return the results
		return DBUtil.cypherEngine().execute(stringQuery, params);
	}
	
	/**
	 * This method queries the graph database with a cypher query
	 * to receive the hg clusters of the given user node ordered by
	 * the leaving time of the connected stay points. The ids of the
	 * hg clusters to receive are restricted by the parameter 
	 * <code>hgClusterIds</code> which has to be non-null and non-empty.
	 * <p />
	 * The resulting query looks like the following:
	 * 
	 * <pre>
	 * START u=node({userNodeId})
	 * MATCH u-[:HasHG|HasHGChildCluster*]->hgc-[:HasHGStayPoint]->sp
	 * WHERE hgc.hg_cluster_id IN [{hgClusterIds}]
	 * RETURN hgc.hg_cluster_id AS hg_cluster_id, 
	 * 	sp.staypoint_arr AS staypoint_arr,
	 * 	sp.staypoint_leav AS staypoint_leav
	 * ORDER BY sp.staypoint_leav
	 * </pre>
	 *
	 * The result columns of the returned <code>ExecutionResult</code>
	 * are named 'hg_cluster_id', 'staypoint_arr' and 'staypoint_leav' 
	 * (cf. the generated Cypher query). To access them use those names.
	 * <p />
	 * @param userNodeId the id of the user node to access its hierarchical graph.
	 * @param hgClusterIds the ids of the hg clusters to restrict the result to. This 
	 * parameter has to be non-null and non-empty.
	 * @return the result of the cypher query in form of an <code>ExecutionResult</code> which includes all 
	 * found hg cluster ids as well as the arrival and leaving time of the corresponding stay points.
	 * 
	 * @throws NullPointerException if the array of hg cluster ids is <code>null</code>.
	 * @throws IllegalArgumentException if the array of hg cluster ids is empty.
	 */
	public static ExecutionResult hgClustersInStaypointOrderForUser(Long userNodeId, String[] hgClusterIds)
			 throws IllegalArgumentException {
		if (hgClusterIds == null)
			throw new NullPointerException(
				"You provided a null value for the list of hg cluster ids. " +
				"This parameter is expected to be non-null. The query cannot be " +
				"performed without any starting node.");
		
		if (hgClusterIds.length == 0)
			throw new IllegalArgumentException(
					"You provided an empty list of hg cluster ids. This list " +
					"has to include at least one valid hg cluster id. The query cannot be " +
					"performed without any starting node.");
		
		// Build parameter map for cypher query
		Map<String, Object> params = new HashMap<String, Object>();
		// User id
		params.put("userNodeId", userNodeId);
		
    	// Build the query
    	StringBuilder builder = new StringBuilder();
    	builder.append("START u=node({userNodeId}) ")
			.append("MATCH u-[:")
			.append(RelTypes.HasHG)
			.append("|")
			.append(RelTypes.HasHGChildCluster)
			.append("*]->hgc-[:")
			.append(RelTypes.HasHGStayPoint)
			.append("]->sp ")
			.append("WHERE hgc.")
			.append(NodeProperties.HG_CLUSTER_ID)
			.append(" IN [")
			.append(ListUtil.join(hgClusterIds, ",", "\""))
			.append("] ")
			.append("RETURN hgc.")
			.append(NodeProperties.HG_CLUSTER_ID)
			.append(" AS ")
			.append(NodeProperties.HG_CLUSTER_ID)
			.append(", sp.")
			.append(NodeProperties.STAYPOINT_ARRIVAL)
			.append(" AS ")
			.append(NodeProperties.STAYPOINT_ARRIVAL)
			.append(", sp.")
			.append(NodeProperties.STAYPOINT_LEAVING)
			.append(" AS ")
			.append(NodeProperties.STAYPOINT_LEAVING)
			.append(" ORDER BY sp.")
			.append(NodeProperties.STAYPOINT_LEAVING);
		
		String stringQuery = builder.toString();
		
		logCypherQuery(stringQuery);
		
		// Execute the query and return the results
		return DBUtil.cypherEngine().execute(stringQuery, params);
	}
	
	// TODO: Refactor this to be used in the users dao.
	/**
	 * This method queries the graph database with a cypher query
	 * to receive the overall number of users.
	 * The resulting query looks like the following:
	 * 
	 * <pre>
	 * START r=node(0) 
	 * MATCH r-[:RootUser]->u
	 * RETURN count(u) AS usersCount
	 * LIMIT 1
	 * </pre>
	 *
	 * The result column of the returned <code>ExecutionResult</code>
	 * is named 'usersCount' (cf. the generated Cypher query).
	 * To access it use this name.
	 */
	public static ExecutionResult countUsers() {
    	// Build the query
    	StringBuilder builder = new StringBuilder();
    	builder.append("START r=node(0) ")
			.append("MATCH r-[:")
			.append(RelTypes.RootUser)
			.append("]->u ")
			.append("RETURN count(u) AS usersCount ")
			.append("LIMIT 1");
		
		String stringQuery = builder.toString();
		
		logCypherQuery(stringQuery);
		
		// Execute the query and return the results
		return DBUtil.cypherEngine().execute(stringQuery);
	}
	
	/**
	 * This method queries the graph database with a cypher query
	 * to receive all user nodes.
	 * The resulting query looks like the following:
	 * 
	 * <pre>
	 * START u=node:users("user_id: *")
	 * RETURN u AS users
	 * ORDER BY u.user_id
	 * </pre>
	 *
	 * The result column of the returned <code>ExecutionResult</code>
	 * is named 'users' (cf. the generated Cypher query).
	 * To access it use this name.
	 * <p />
	 * If the index 'users' does not exist a fallback Cypher query is used 
	 * that traverses the graph beginning from the reference node:
	 * 
	 * <pre>
	 * START r=node(0) 
	 * MATCH r-[:RootUser]->u
	 * RETURN u AS users
	 * ORDER BY u.user_id
	 * </pre>
	 */
	public static ExecutionResult allUsers() {
    	// Build the query
    	StringBuilder builder = new StringBuilder();
    	builder.append("START u=node:")
    		.append(DBUtil.USER_INDEX)
    		.append("('")
    		.append(NodeProperties.USER_ID)
    		.append(": *') ")
			.append("RETURN u AS users ")
			.append("ORDER BY u.user_id");
		
		String stringQuery = builder.toString();
		
		logCypherQuery(stringQuery);
		
		// Execute the query and return the results
		ExecutionResult result = null;
		try {
			result = DBUtil.cypherEngine().execute(stringQuery);
		} catch (MissingIndexException e) {
			// If the user node index is not found an exception is thrown
			// Use fallback Cypher query without index
			LOG.warn("The '{}' index does not exist. Try to find users without the index.", DBUtil.USER_INDEX);
			
			// Reset string builder
			builder.setLength(0);
			builder.append("START r=node(0) ")
				.append("MATCH r-[:")
				.append(RelTypes.RootUser)
				.append("]->u ")
				.append("RETURN u AS users ")
				.append("ORDER BY u.user_id");
			
			stringQuery = builder.toString();
			logCypherQuery(stringQuery);
			
			result = DBUtil.cypherEngine().execute(stringQuery);
		}
		
		return result;
	}
	
	/**
	 * This method queries the graph database with a cypher query
	 * to receive the number of users that visited a specific hg 
	 * cluster.
	 * <p />
	 * The resulting query looks like the following:
	 * 
	 * <pre>
	 * START r=node(0)
	 * MATCH r-[:RootUser]->u-[:HasHG|HasHGChildCluster*]->hgc
	 * WHERE hgc.hg_cluster_id = "{hgClusterId}"
	 * RETURN count(distinct u)	AS usersInHgCluster
	 * LIMIT 1
	 * </pre>
	 *
	 * The result column of the returned <code>ExecutionResult</code>
	 * is named 'usersInHgCluster' (cf. the generated Cypher query). 
	 * To access it use this name.
	 * <p />
	 * @param hgClusterId the id of the hg cluster to find the number of visiting
	 * users for. This argument has to be non-null and not empty.
	 * @return the result of the cypher query in form of an <code>ExecutionResult</code> which includes 
	 * the number of distinct users that visited the hg cluster with the given id.
	 * 
	 * @throws NullPointerException if the hg cluster id is <code>null</code>.
	 * @throws IllegalArgumentException if the hg cluster id is empty.
	 */
	public static ExecutionResult countUsersInHgCluster(String hgClusterId)
			 throws IllegalArgumentException {
		if (hgClusterId == null)
			throw new NullPointerException(
				"You provided a null value for the hg cluster id. " +
				"This parameter is expected to be non-null.");
		
		if (hgClusterId.isEmpty())
			throw new IllegalArgumentException(
					"You provided an empty string for the hg cluster id. " +
					"The hg cluster id should not be empty.");
		
    	// Build the query
    	StringBuilder builder = new StringBuilder();
    	builder.append("START r=node(0) ")
			.append("MATCH r-[:")
			.append(RelTypes.RootUser)
			.append("]->u-[:")
			.append(RelTypes.HasHG)
			.append("|")
			.append(RelTypes.HasHGChildCluster)
			.append("*]->hgc ")
			.append("WHERE hgc.")
			.append(NodeProperties.HG_CLUSTER_ID)
			.append(" = '")
			.append(hgClusterId)
			.append("' ")
			.append("RETURN count(distinct u) AS usersInHgCluster ")
			.append("LIMIT 1");
		
		String stringQuery = builder.toString();
		
		logCypherQuery(stringQuery);
		
		// Execute the query and return the results
		return DBUtil.cypherEngine().execute(stringQuery);
	}
	
	// TODO: Refactor this to be used in the users dao.
	/**
	 * This method queries the graph database with a cypher query
	 * to receive the number of stay points for a given user node.
	 * <p />
	 * The resulting query looks like the following:
	 * 
	 * <pre>
	 * START u=node({id})
	 * MATCH u-[:HasHG|HasHGChildCluster*]->hgc-[:HasHGStayPoint]->sp
	 * RETURN count(sp) AS userStayPointsCount
	 * LIMIT 1
	 * </pre>
	 * 
	 * The result column of the returned <code>ExecutionResult</code>
	 * is named 'userStayPointsCount' (cf. the generated Cypher query). To access it use this name.
	 * <p />
	 * @param userNodeId the id of the starting user node.
	 * @return the result of the cypher query in form of an <code>ExecutionResult</code> which includes 
	 * the number of stay points for the given user node.
	 * 
	 * @see org.neo4j.cypher.javacompat.ExecutionResult ExecutionResult
	 */
	public static ExecutionResult countUserStaypoints(long userNodeId) {
		// Build parameter map for cypher query
		Map<String, Object> params = new HashMap<String, Object>();
		// Id of the starting node
    	params.put("id", userNodeId);
    	
    	// Build the query
		StringBuilder builder = new StringBuilder();
		builder.append("START u=node({id}) ")
			.append("MATCH u-[:")
			.append(RelTypes.HasHG)
			.append("|")
			.append(RelTypes.HasHGChildCluster)
			.append("*]->hgc-[:")
			.append(RelTypes.HasHGStayPoint)
			.append("]->sp ")
			.append("RETURN count(sp) AS userStayPointsCount")
			.append(" LIMIT 1");
		
		String stringQuery = builder.toString();
		
		logCypherQuery(stringQuery);
		
		// Execute the query and return the results
		return DBUtil.cypherEngine().execute(stringQuery, params);
	}
	
	/**
	 * Logs the given query to the debug log.
	 * 
	 * @param query the query to log.
	 */
	private static void logCypherQuery(String query) {
		if (query != null)
			LOG.debug("Executing: {}", query);
	}
	
}
