package de.tub.data.dao;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import de.tub.util.DBUtil;

/**
 * This class provides methods that are shared between different
 * Neo4J DAO implementations.
 * 
 * @author Sebastian Oelke
 *
 */
public class SharedNeo4JDAO {
	
	/**
	 * Creates a relationship between two nodes.
	 * 
	 * @param fromNode the node from which the relationship starts.
	 * @param toNode the node at which the relationship ends.
	 * @param relType the relationship to use.
	 * @param params the parameters/properties to add to the relationship. Properties are only added to the relationship
	 * if the given <code>Map</code> is non-null as well as all keys and values within it.
	 * @throws NullPointerException if one of the given arguments is <code>null</code>, except the parameter map.
	 */
	public static void addNodeHelper(Node fromNode, Node toNode, RelationshipType relType, Map<String, Object> params) 
						throws NullPointerException {
		
		if (fromNode == null || toNode == null || relType == null)
			throw new NullPointerException(
				"You provided a null value for either the from node, the to node or the relationship type. " +
				"All parameters are expected to be non-null.");
		
		// Create transaction
		Transaction tx = DBUtil.graph().beginTx();
		
		try {
			// Create connection from fromNode to toNode
			Relationship r = fromNode.createRelationshipTo(toNode, relType);
			
			// Add properties to relationship if needed
			if (params != null && !params.isEmpty()) {
				// Get property names
				Set<String> keys = params.keySet();
				Iterator<String> i = keys.iterator();
				// Add corresponding property value 
				while (i.hasNext()) {
					String key = i.next();
					if (key != null) {
						Object value = params.get(key);
						if (value != null) 
							r.setProperty(key, value);
					}
				}
			}
			
			tx.success();
		} finally {
			tx.finish();
		}
	}

}
