package de.tub.graph;

import org.neo4j.graphdb.RelationshipType;

/**
 * Relationship types used by Neo4j.
 * 
 * @author Sebastian Oelke
 *
 */
public enum RelTypes implements RelationshipType {
	/**
	 * Relationship type used to connect the root cluster of the framework 
	 * with the reference node of the graph.
	 */
	RootFrameworkCluster,
	/**
	 * Relationship type used to connect framework cluster.
	 */
	HasChildCluster,
	/**
	 * Relationship type used to connect stay points to framework cluster.
	 */
	HasStayPoint,
	/**
	 * Relationship type used to connect the root user node  
	 * with the reference node of the graph.
	 */
	RootUser,
	/**
	 * Relationship type used to connect a user node with its hierarchical graph.
	 */
	HasHG,
	/**
	 * Relationship type used to connect a hierarchical graph cluster with its child.
	 */
	HasHGChildCluster,
	/**
	 * Relationship type used to connect a stay point with a hierarchical graph cluster.
	 */
	HasHGStayPoint,
	/**
	 * Relationship type used to connect two user nodes if they are spatially similar.
	 */
	SpatiallySimilar
}
