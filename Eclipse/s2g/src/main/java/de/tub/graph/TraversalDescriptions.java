package de.tub.graph;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.Traversal;

/**
 * This class provides definitions for traversals.
 * 
 * @author Sebastian Oelke
 *
 */
public class TraversalDescriptions {
	
	/**
	 * Follows the relationships <code>RootFrameworkCluster</code> as
	 * well as <code>HasChildCluster</code>, excludes the starting node (e.g., 
	 * the reference node of the graph) and does a breadth first search.
	 */
	public static TraversalDescription FRAMEWORK_CLUSTER_TRAVERSAL = 
			Traversal.description()
					.breadthFirst()
					.relationships(RelTypes.RootFrameworkCluster, Direction.OUTGOING)
					.relationships(RelTypes.HasChildCluster, Direction.OUTGOING)
					.evaluator(Evaluators.excludeStartPosition());
	
	/**
	 * Follows the incoming relationship <code>HasChildCluster</code>, excludes
	 * the starting node and does a depth first search.
	 */
	public static TraversalDescription FRAMEWORK_CLUSTER_UP_TRAVERSAL = 
			Traversal.description()
					.depthFirst()
					.relationships(RelTypes.HasChildCluster, Direction.INCOMING)
					.evaluator(Evaluators.excludeStartPosition());
}
