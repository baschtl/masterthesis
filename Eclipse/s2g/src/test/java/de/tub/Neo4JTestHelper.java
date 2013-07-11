package de.tub;

import java.io.File;
import java.util.Properties;

import org.neo4j.graphdb.Node;

import de.tub.data.dao.DAOFactory;
import de.tub.data.dao.Neo4JHGClusterDAO;
import de.tub.data.dao.Neo4JStaypointDAO;
import de.tub.data.dao.Neo4JUserDAO;
import de.tub.util.DBUtil;
import de.tub.util.PropertiesUtil;

/**
 * This helper class provides convenience methods
 * when testing the Neo4j graph. 
 * 
 * @author Sebastian Oelke
 *
 */
public class Neo4JTestHelper {
	
	public final static String DB_TEST_PROP_FILE = "/db.properties";
	
	public static final String USER_ID_1 = "1";
	public static final String USER_ID_2 = "2";
	public static final String USER_ID_3 = "3";
	
	public static final String HG_CLUSTER_LEVEL_1_0 = "1_0";
	
	public static final String HG_CLUSTER_LEVEL_2_0 = "2_0";
	public static final String HG_CLUSTER_LEVEL_2_1 = "2_1";
	
	public static final String HG_CLUSTER_LEVEL_3_0 = "3_0";
	public static final String HG_CLUSTER_LEVEL_3_1 = "3_1";
	
	public static Node userNode1, userNode2, userNode3;
	
	public static void generateHg(boolean generateStayPoints) {
		Neo4JUserDAO uDao = (Neo4JUserDAO) DAOFactory.instance().getUserDAO();
		Neo4JHGClusterDAO hgDao = (Neo4JHGClusterDAO) DAOFactory.instance().getHGClusterDAO();
		
		// Create user nodes and connect them to the graph's reference node
		userNode1 = uDao.createUser(USER_ID_1);
		userNode2 = uDao.createUser(USER_ID_2);
		userNode3 = uDao.createUser(USER_ID_3);
		
		uDao.addRootUser(userNode1.getGraphDatabase().getReferenceNode(), userNode1);
		uDao.addRootUser(userNode2.getGraphDatabase().getReferenceNode(), userNode2);
		uDao.addRootUser(userNode3.getGraphDatabase().getReferenceNode(), userNode3);
		
		// Create hg root clusters
		Node hgRootUser1 = hgDao.createHGCluster(HG_CLUSTER_LEVEL_1_0, USER_ID_1);
		Node hgRootUser2 = hgDao.createHGCluster(HG_CLUSTER_LEVEL_1_0, USER_ID_2);
		uDao.addRootHGCluster(userNode1, hgRootUser1);
		uDao.addRootHGCluster(userNode2, hgRootUser2);
		
		// Build hg of user 1
		Node hgLevel20User1 = hgDao.createHGCluster(HG_CLUSTER_LEVEL_2_0, USER_ID_1);
		Node hgLevel21User1 = hgDao.createHGCluster(HG_CLUSTER_LEVEL_2_1, USER_ID_1);
		Node hgLevel30User1 = hgDao.createHGCluster(HG_CLUSTER_LEVEL_3_0, USER_ID_1);
		hgDao.addChildHGCluster(hgRootUser1, hgLevel20User1);
		hgDao.addChildHGCluster(hgRootUser1, hgLevel21User1);
		hgDao.addChildHGCluster(hgLevel20User1, hgLevel30User1);
		
		// Build hg of user 2
		Node hgLevel20User2 = hgDao.createHGCluster(HG_CLUSTER_LEVEL_2_0, USER_ID_2);
		Node hgLevel21User2 = hgDao.createHGCluster(HG_CLUSTER_LEVEL_2_1, USER_ID_2);
		Node hgLevel30User2 = hgDao.createHGCluster(HG_CLUSTER_LEVEL_3_0, USER_ID_2);
		Node hgLevel31User2 = hgDao.createHGCluster(HG_CLUSTER_LEVEL_3_1, USER_ID_2);
		hgDao.addChildHGCluster(hgRootUser2, hgLevel20User2);
		hgDao.addChildHGCluster(hgRootUser2, hgLevel21User2);
		hgDao.addChildHGCluster(hgLevel20User2, hgLevel30User2);
		hgDao.addChildHGCluster(hgLevel21User2, hgLevel31User2);
		
		if (generateStayPoints) {
			Neo4JStaypointDAO sDao = (Neo4JStaypointDAO) DAOFactory.instance().getStaypointDAO();
			
			// ### User 1 stay points
			Node s1U1 = sDao.createStayPoint(1, 1.0, 1.0, 1L, 2L);
			Node s2U1 = sDao.createStayPoint(2, 1.0, 1.0, 2L, 3L);
			Node s3U1 = sDao.createStayPoint(3, 1.0, 1.0, 3L, 4L);
			Node s4U1 = sDao.createStayPoint(4, 1.0, 1.0, 4L, 5L);
			Node s5U1 = sDao.createStayPoint(5, 1.0, 1.0, 5L, 6L);
			Node s6U1 = sDao.createStayPoint(6, 1.0, 1.0, 6L, 8L);
			
			// Level 1
			hgDao.addStayPoint(hgRootUser1, s1U1, null);
			hgDao.addStayPoint(hgRootUser1, s2U1, null);
			hgDao.addStayPoint(hgRootUser1, s3U1, null);
			hgDao.addStayPoint(hgRootUser1, s4U1, null);
			hgDao.addStayPoint(hgRootUser1, s5U1, null);
			hgDao.addStayPoint(hgRootUser1, s6U1, null);
			
			// Level 2
			hgDao.addStayPoint(hgLevel20User1, s1U1, null);
			hgDao.addStayPoint(hgLevel20User1, s2U1, null);
			hgDao.addStayPoint(hgLevel21User1, s3U1, null);
			hgDao.addStayPoint(hgLevel21User1, s4U1, null);
			hgDao.addStayPoint(hgLevel20User1, s5U1, null);
			hgDao.addStayPoint(hgLevel20User1, s6U1, null);
			
			// Level 3
			hgDao.addStayPoint(hgLevel30User1, s1U1, null);
			hgDao.addStayPoint(hgLevel30User1, s2U1, null);
			hgDao.addStayPoint(hgLevel30User1, s5U1, null);
			hgDao.addStayPoint(hgLevel30User1, s6U1, null);
			
			// ### User 2 stay points
			Node s1U2 = sDao.createStayPoint(7, 1.0, 1.0, 1L, 2L);
			Node s2U2 = sDao.createStayPoint(8, 1.0, 1.0, 2L, 3L);
			Node s3U2 = sDao.createStayPoint(9, 1.0, 1.0, 3L, 4L);
			Node s4U2 = sDao.createStayPoint(10, 1.0, 1.0, 4L, 5L);
			Node s5U2 = sDao.createStayPoint(11, 1.0, 1.0, 5L, 6L);
			Node s6U2 = sDao.createStayPoint(12, 1.0, 1.0, 6L, 8L);
			Node s7U2 = sDao.createStayPoint(13, 1.0, 1.0, 8L, 12L);
			
			// Level 1
			hgDao.addStayPoint(hgRootUser2, s1U2, null);
			hgDao.addStayPoint(hgRootUser2, s2U2, null);
			hgDao.addStayPoint(hgRootUser2, s3U2, null);
			hgDao.addStayPoint(hgRootUser2, s4U2, null);
			hgDao.addStayPoint(hgRootUser2, s5U2, null);
			hgDao.addStayPoint(hgRootUser2, s6U2, null);
			hgDao.addStayPoint(hgRootUser2, s7U2, null);
			
			// Level 2
			hgDao.addStayPoint(hgLevel20User2, s1U2, null);
			hgDao.addStayPoint(hgLevel21User2, s2U2, null);
			hgDao.addStayPoint(hgLevel21User2, s3U2, null);
			hgDao.addStayPoint(hgLevel21User2, s4U2, null);
			hgDao.addStayPoint(hgLevel20User2, s5U2, null);
			hgDao.addStayPoint(hgLevel20User2, s6U2, null);
			hgDao.addStayPoint(hgLevel20User2, s7U2, null);
			
			// Level 3
			hgDao.addStayPoint(hgLevel31User2, s2U2, null);
			hgDao.addStayPoint(hgLevel31User2, s3U2, null);
			hgDao.addStayPoint(hgLevel30User2, s5U2, null);
			hgDao.addStayPoint(hgLevel30User2, s6U2, null);
			hgDao.addStayPoint(hgLevel30User2, s7U2, null);
		}
	}
	
	public static void resetGraph() {
		// Close the graph
		DBUtil.closeGraph();
		// Delete the graph created during a test run
		resetDefaultGraph();
	}
	
	private static void resetDefaultGraph() {
		// Load test properties file and request db path
		Properties prop = PropertiesUtil.load(DB_TEST_PROP_FILE);
		String path = prop.getProperty("neo4j.path");
		
		// Open file with path
		File pathFile = new File(path);
		TestHelper.deleteFileOrDirectory(pathFile);
	}

}
