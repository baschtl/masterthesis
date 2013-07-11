package de.tub.processor.graph;

import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.Traverser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.data.dao.DAOFactory;
import de.tub.data.dao.Neo4JFrameworkClusterDAO;
import de.tub.data.dao.Neo4JHGClusterDAO;
import de.tub.data.dao.Neo4JStaypointDAO;
import de.tub.data.dao.Neo4JUserDAO;
import de.tub.data.model.StayPoint;
import de.tub.data.model.User;
import de.tub.graph.RelTypes;
import de.tub.graph.TraversalDescriptions;
import de.tub.observer.Interests;
import de.tub.observer.Observer;
import de.tub.observer.Subject;
import de.tub.processor.IProcessor;
import de.tub.reader.model.UserReader;

/**
 * This processor creates a hierarchical graph for a given
 * user based on the user's stay points that are persisted 
 * in a relational database and the shared
 * framework that is persisted in a Neo4J graph database.
 * <p />
 * This processor handles the <code>UserFinished</code> interest
 * of a <code>UserReader</code>. Thereupon, it finishes its processing
 * by creating a user node and connects it to the graph's reference node.
 * 
 * @author Sebastian Oelke
 *
 */
public class HierarchicalGraphProcessor implements IProcessor<User>, Observer {

	private static final Logger LOG = LoggerFactory.getLogger(HierarchicalGraphProcessor.class);
	
	private User currentUser;
	private boolean noHgForUser = false;
	
	private Neo4JFrameworkClusterDAO fDao = (Neo4JFrameworkClusterDAO) DAOFactory.instance().getFrameworkClusterDAO();
	private Neo4JHGClusterDAO hgDao = (Neo4JHGClusterDAO) DAOFactory.instance().getHGClusterDAO();
	private Neo4JStaypointDAO sDao = (Neo4JStaypointDAO) DAOFactory.instance().getStaypointDAO();
	private Neo4JUserDAO uDao = (Neo4JUserDAO) DAOFactory.instance().getUserDAO();
	
	private Object rootFrameworkClusterId = fDao.getFrameworkClusterId(fDao.getFrameworkRootCluster());
	
	@Override
	public void newData(User data) {
		// Return if the data's reference is null
		if (data == null) return;
		
		// Process the given data
		processData(data);
	}
	
	@Override
	public void finish() {
		if (currentUser != null) {
			// Create a user node
			Node userNode = uDao.createUser(currentUser.getId());
			// Connect user node to graph reference node
			uDao.addRootUser(userNode.getGraphDatabase().getReferenceNode(), userNode);
			
			// A hg was created for this user
			if (!noHgForUser) {
				// Connect the hg root cluster to the root of the users hierarchical graph (i.e., the user node) 
				Node hgRootCluster = hgDao.findHGClusterById(rootFrameworkClusterId, currentUser.getId());
				uDao.addRootHGCluster(userNode, hgRootCluster);
			}
			// A hg was not created for this user because of the lack of stay points.
			// Only the user node is created and attached to the graph's reference node.
			else {
				LOG.warn("For the user with id [{}] no stay points were detected. " +
							"Therefore, a hierarchical graph is not created for this user.", currentUser.getId());
				
				// Reset for the following user
				noHgForUser = false;
			}
		} else
			LOG.error("The current user instance is null. Cannot properly finish this processor, i.e., " +
						"cannot create a user node and connect it to the graph's reference node.");
	}
	
	private void processData(User user) {
		// Set the current user
		currentUser = user;
		
		// Get stay points of user
		List<StayPoint> stayPoints = currentUser.getAll(StayPoint.class);
		
		for (int i = 0; i < stayPoints.size(); i++) {
			// Get stay point node from graph database
			StayPoint sp = stayPoints.get(i);
			Node spNode = sDao.findStayPointById((Integer) sp.getId());
			
			if (spNode != null) {
				// Every stay point node should have an incoming connection to one framework cluster
				// Get this cluster
				Relationship r = spNode.getSingleRelationship(RelTypes.HasStayPoint, Direction.INCOMING);
				if (r != null) {
					Node frameworkCluster = r.getStartNode();
					// Create hierarchical graph cluster
					Object hgClusterId = fDao.getFrameworkClusterId(frameworkCluster);
					
					Node hgCluster = null;
					try {
						hgCluster = hgDao.createHGCluster(hgClusterId, currentUser.getId());
					} catch (RuntimeException e) {
						// If an exception is thrown a cluster with this id already exists, so get it
						hgCluster = hgDao.findHGClusterById(hgClusterId, currentUser.getId());
					}
					
					// Create relationship between new hg cluster and stay point
					hgDao.addStayPoint(hgCluster, spNode);
					
					// Traverse upwards in the shared framework and add hg clusters as needed and attach the stay point to it
					Node currentHGChild = hgCluster;
					Traverser traverser = TraversalDescriptions.FRAMEWORK_CLUSTER_UP_TRAVERSAL.traverse(frameworkCluster);
					for (Path p : traverser) {
						// Get end node / parent of each found path which should be a framework cluster
						Node endNode = p.endNode();
						
						// Create a hg cluster that resembles the parent framework cluster
						Object parentHgClusterId = fDao.getFrameworkClusterId(endNode);
						
						Node parentHgCluster = null;
						try {
							// A hg cluster with this id does not exist if no exception is thrown
							parentHgCluster = hgDao.createHGCluster(parentHgClusterId, currentUser.getId());
							// If the cluster was just created no connection to the child hg cluster is available
							// Create the connection
							hgDao.addChildHGCluster(parentHgCluster, currentHGChild);
							
						} catch (RuntimeException e) {
							// If an exception is thrown a parent framework cluster with this id already exists, so get it
							parentHgCluster = hgDao.findHGClusterById(parentHgClusterId, currentUser.getId());
							
							// Check if a connection is already there to the child hg cluster and create it if it does not exist
							// Get all outgoing connections from the parent hg cluster
							Iterable<Relationship> hgChildClusterRels = parentHgCluster.getRelationships(RelTypes.HasHGChildCluster, Direction.OUTGOING);
							boolean desiredConnectionExists = false;
							
							// Check if the connection from the parent hg cluster to the child hg cluster exists
							for (Relationship rel : hgChildClusterRels) {
								if (rel.getEndNode().equals(currentHGChild)) {
									// The desired connection exists, so stop here
									desiredConnectionExists = true;
									break;
								}
							}
							
							// The connection does not yet exist, create it
							if (!desiredConnectionExists) {
								hgDao.addChildHGCluster(parentHgCluster, currentHGChild);
							}
						}
						
						// Connect the created parent hg cluster with the stay point
						hgDao.addStayPoint(parentHgCluster, spNode);
						
						// The current parent hg cluster is the child hg cluster for the next iteration
						currentHGChild = parentHgCluster;
					}
					
				} else {
					// No relationship from the given stay point to a framework cluster found
					LOG.error("There was no relationship to a framework cluster found for the stay point with id [{}].", spNode.getId());
				}
			} else {
				// A stay point with the given id could not be found in the graph
				LOG.error("A stay point with id [{}] could not be found in the graph database.", sp.getId());
			}
		}
		
		// This user has no stay points so no hg can be created
		if (stayPoints.isEmpty())
			noHgForUser = true;
	}

	@Override
	public void update(Subject theSubject, Interests interest, Object arg) {
		if (theSubject instanceof UserReader) {
			// A UserReader notifies us about its finishing
			if (interest == Interests.UserFinished) {
				// The reading of a user ended, finish this processor
				finish();
			}
		} 
	}
}
