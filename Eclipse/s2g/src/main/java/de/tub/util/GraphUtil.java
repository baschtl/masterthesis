package de.tub.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.data.dao.DAOFactory;
import de.tub.data.dao.Neo4JFrameworkClusterDAO;

/**
 * This class provides utility methods for the work with
 * a graph database.
 * 
 * @author Sebastian Oelke
 *
 */
public class GraphUtil {

	private static final Logger LOG = LoggerFactory.getLogger(GraphUtil.class);
	
	/**
	 * Accepts one or more numbers at the beginning of the input string.
	 */
	public static final String EXTRACT_FRAMEWORK_CLUSTER_DEPTH_PATTERN = "^\\d+";
	
	/**
	 * The separator used to generate a hg cluster id based on a user id and
	 * a framework cluster id.
	 */
	public static final String HG_CLUSTER_ID_SEPARATOR = "#";
	
	/**
	 * Returns a hierarchical graph cluster id in the form of
	 * <pre>[user_id]#[cluster_id]</pre>.
	 * 
	 * @param userId the user id to use for generation.
	 * @param clusterId the cluster id to use for generation.
	 * @return a hierarchical graph cluster id or <code>null</code> if one or both of the parameters are <code>null</code>.
	 */
	public static String generateHGClusterId(Object userId, Object clusterId) {
		if (userId == null || clusterId == null) {
			LOG.warn("You provided a null value for either the user id or the cluster id. " +
						"Both values are expected to be non-null. So, null is returned.");
			return null;
		}
		
		return userId.toString() + HG_CLUSTER_ID_SEPARATOR + clusterId.toString();
	}
	
	/**
	 * Extracts the depth of the framework cluster out of the cluster id 
	 * which has the format <pre>[depth_in_tree]_[id]</pre>. 
	 * 
	 * @param frameworkCluster the cluster node within the shared framework from which to extract the depth from.
	 * @return the depth of the given cluster or <code>null</code> if the given node is <code>null</code>, 
	 * the node has not the desired cluster id property or the depth of the given cluster could not be extracted.
	 */
	public static String extractFrameworkClusterDepth(Node frameworkCluster) {
		if (frameworkCluster == null) {
			LOG.warn("You provided a null value for the framework cluster node." +
					"This value is expected to be non-null. So, null is returned.");
			return null;
		}
		
		Neo4JFrameworkClusterDAO fDao = (Neo4JFrameworkClusterDAO) DAOFactory.instance().getFrameworkClusterDAO();
		
		Object clusterId;
		if ((clusterId = fDao.getFrameworkClusterId(frameworkCluster)) != null)
			return extractFrameworkClusterDepthHelper(clusterId.toString());
		// This node has not the desired cluster id property
		else
			return null;
	}
	
	/**
	 * Extracts the depth of the framework cluster out of the cluster id 
	 * which has the format <pre>[depth_in_tree]_[id]</pre>. 
	 * 
	 * @param frameworkClusterId the framework cluster id from which to extract the depth from.
	 * @return the depth of the given framework cluster id or <code>null</code> if the given 
	 * framework cluster id is <code>null</code>, is empty or the depth of the given cluster 
	 * could not be extracted.
	 */
	public static String extractFrameworkClusterDepth(String frameworkClusterId) {
		if (frameworkClusterId == null || frameworkClusterId.isEmpty()) {
			LOG.warn("You provided a null value or an empty string for the framework cluster id. " +
						"This value is expected to be non-null. So, null is returned.");
			return null;
		}
		
		return extractFrameworkClusterDepthHelper(frameworkClusterId);
	}
	
	//###################################################################
	// Helper
	//###################################################################
	
	private static String extractFrameworkClusterDepthHelper(String id) {
		Matcher m = Pattern.compile(EXTRACT_FRAMEWORK_CLUSTER_DEPTH_PATTERN).matcher(id);
		// If everything worked fine the first match is the desired depth of the given cluster id
		if (m.find())
			return m.group();
		// Otherwise the cluster id has a wrong format
		else
			return null;
	}
	
}
