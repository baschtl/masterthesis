package de.tub.processor.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.tooling.GlobalGraphOperations;

import de.tub.Neo4JTestHelper;
import de.tub.TestPropertiesLoader;
import de.tub.graph.NodeProperties;
import de.tub.graph.RelTypes;
import de.tub.observer.Interests;
import de.tub.reader.ReaderFactory;
import de.tub.reader.file.IterativeFileReader;
import de.tub.reader.file.TextFileLineReader;
import de.tub.util.DBUtil;
import de.tub.util.FileUtil;

/**
 * @author Sebastian Oelke
 *
 */
public class SharedFrameworkProcessorTest {

	public static final String ROOT_CLUSTER_ID = "1_0";
	public static final String CHILD_CLUSTER_ID = "2_0";
	
	public static List<Integer> stayPointIds = new ArrayList<Integer>();
	
	@Before
	public void setup() {
		// Initialize list with expected stay points
		stayPointIds.add(30661);
		stayPointIds.add(30662);
		stayPointIds.add(10656);
		stayPointIds.add(13675);
	}
	
	@Test
	public void testClusterAndStaypointCreation() {
		// ### Initialize all readers and the processor
		IterativeFileReader iterativeFileReader = (IterativeFileReader) ReaderFactory.instance().getIterativeFileReader();
    	TextFileLineReader textFileLineReader = (TextFileLineReader) ReaderFactory.instance().getTextFileLineReader();
    	iterativeFileReader.setReader(textFileLineReader);
    	
    	// Setup IterativeFileReader
    	File dir = new File(TestPropertiesLoader.getSharedFrameworkInDir());
    	iterativeFileReader.setFile(dir);
    	iterativeFileReader.setFileFilter(FileUtil.acceptOnlyClusterFilesFilter());
    	
    	// Setup processor
    	SharedFrameworkProcessor clusterPostProcessor = new SharedFrameworkProcessor();
    	textFileLineReader.setProcessor(clusterPostProcessor);
    	// Get informed about the finishing of each cluster (file) to reset the processor's status
    	textFileLineReader.attach(clusterPostProcessor, Interests.HasFinished);
    	// Get informed about the finishing of all clusters to give each cluster in the generated graph a pretty id
    	iterativeFileReader.attach(clusterPostProcessor, Interests.HasFinished);
    	
    	iterativeFileReader.read();
    	
    	// ### Test created clusters and stay points
    	// There should be one root cluster with one child connected to the reference node
    	Node ref = DBUtil.graph().getReferenceNode();
    	
    	Node root = ref.getSingleRelationship(RelTypes.RootFrameworkCluster, Direction.OUTGOING).getEndNode();
    	Node child = root.getSingleRelationship(RelTypes.HasChildCluster, Direction.OUTGOING).getEndNode();
    	
    	assertNotNull("The root framework cluster should not be null.", root);
    	assertEquals("The framework cluster id of the root cluster is not as expected.", 
    			ROOT_CLUSTER_ID, root.getProperty(NodeProperties.FRAMEWORK_CLUSTER_ID));
    	assertNotNull("The child framework cluster should not be null.", child);
    	assertEquals("The framework cluster id of the child cluster is not as expected.", 
    			CHILD_CLUSTER_ID, child.getProperty(NodeProperties.FRAMEWORK_CLUSTER_ID));
    	
    	// Count all stay points
    	Iterable<Node> nodes = GlobalGraphOperations.at(DBUtil.graph()).getAllNodes();
    	int spCount = 0;
    	List<Node> stayPoints = new ArrayList<Node>();
    	for (Node n : nodes) {
    		if (n.hasRelationship(RelTypes.HasStayPoint, Direction.INCOMING)) {
    			spCount++;
    			stayPoints.add(n);
    		}
    	}
    	assertEquals("There should be exactly four stay points.", 4, spCount);
    	
    	// Test existence of stay points by id
    	for (int i = 0; i < stayPoints.size(); i++) {
    		int thisStayPointId = (Integer) stayPoints.get(i).getProperty(NodeProperties.STAYPOINT_ID);
    		
    		for (int j = 0; j < stayPointIds.size(); j++) {
    			if (stayPointIds.contains(thisStayPointId))
    				stayPointIds.remove(stayPointIds.indexOf(thisStayPointId));
    		}
    	}
    	
    	assertEquals("All expected stay points should have been found.", 0, stayPointIds.size());
    	
	}
	
	@After
	public void resetGraph() {
		Neo4JTestHelper.resetGraph();
	}

}
