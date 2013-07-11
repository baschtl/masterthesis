package de.tub.graph.generator;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;

import de.tub.data.dao.DAOFactory;
import de.tub.data.dao.Neo4JFrameworkClusterDAO;
import de.tub.graph.NodeProperties;


/**
 * This generator generates pretty IDs for clusters in the framework depending on their
 * depth in the tree. A pretty ID has the form <pre>[depth_in_tree]_[id]</pre>
 * where the id starts at zero on each tree level and is incremented for each 
 * cluster.
 * <br />
 * So, the ID of the second cluster on level three of the tree structure would be 
 * 3_2.
 * 
 * @author Sebastian Oelke
 *
 */
public class PrettyClusterIdGenerator implements IGenerator<Path> {
	
	private Neo4JFrameworkClusterDAO cDao = (Neo4JFrameworkClusterDAO) DAOFactory.instance().getFrameworkClusterDAO();
	
	/**
	 * Initial cluster ID.
	 */
	private int id = -1;
	/**
	 * Initial tree depth.
	 */
	private int depth = 1;
	
	/**
	 * @throws NullPointerException if a <code>null</code> value is provided as input.
	 */
	@Override
	public void generate(Path input) throws NullPointerException {
		if (input == null)
			throw new NullPointerException(
					"You provided a null value for the entity for which to generate pretty ids for. " +
					"This parameter is expected to be non-null.");
		
		// Get last node of the given path
		Node end = input.endNode();
		
		// ID has to be incremented when the current depth does not change
		if (input.length() == depth) {
			id++;
		}
		// If the depth increases we reached a new tree level and have to 
		// set the depth accordingly and reset the ID
		else if (input.length() > depth) {
			depth = input.length();
			id = 0;
		}
		// Set new ID for current cluster
		cDao.updateFrameworkCluster(end, NodeProperties.FRAMEWORK_CLUSTER_ID, this.depth + "_" + this.id);
	}
}
