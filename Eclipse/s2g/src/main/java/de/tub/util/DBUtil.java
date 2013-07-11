package de.tub.util;

import java.util.Properties;

import org.javalite.activejdbc.DB;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class provides methods for initializing and
 * closing a connection to a database. 
 * 
 * @author Sebastian Oelke
 *
 */
public class DBUtil {
	private static final Logger LOG = LoggerFactory.getLogger(DBUtil.class);
	private static final String DB_PROPERTIES = "/db.properties";
	
	// The logical name of the database connection
	private static final String DB_LOGICAL_NAME = "default";
	// The reference to the database connection
	private static DB db;
	// Properties for relational database (e.g., MySQL)
	private static String sqlDriver, sqlUrl, sqlUser, sqlPassword;
	// Properties for graph database (e.g., Neo4j)
	private static String neo4jPath;
	
	// Neo4j graph instance
	private static GraphDatabaseService graph;
	// Neo4j indices names
	public static final String FRAMEWORK_CLUSTER_INDEX = "framework_cluster";
	public static final String HG_CLUSTER_INDEX = "hg_cluster";
	public static final String STAYPOINT_INDEX = "staypoints";
	public static final String USER_INDEX = "users";
	
	private DBUtil() {};
	
	/**
	 * Load properties file that holds the information needed to connect to
	 * the database.
	 * 
	 * @throws NullPointerException if the database properties file could not be found.
	 */
	static {
		Properties dbProps = PropertiesUtil.load(DB_PROPERTIES);
		if (dbProps == null)
			throw new NullPointerException(
					"The properties file " + DB_PROPERTIES + " could not be found. " +
					"Please provide this file with the appropriate information (i.e., " +
					"db.driver, db.url, db.user, db.password) to be able to connect to the database.");
		
		// Get database properties
    	sqlDriver = dbProps.getProperty("db.driver");
    	sqlUrl = dbProps.getProperty("db.url");
    	sqlUser = dbProps.getProperty("db.user");
    	sqlPassword = dbProps.getProperty("db.pw");
    	
    	// Get graph database propteries
    	neo4jPath = dbProps.getProperty("neo4j.path");
	}
	
	//###################################################################
	// Relational database
	//###################################################################
	
	/**
	 * Sets up a connection to the database with the default 
	 * values defined in the properties file jdbc.properties.
	 * <p />
	 * This is a convenience method for calling 
	 * <code>open(driver, url, user, password)</code> with 
	 * the default values.
	 * 
	 * @see DBUtil#open(String, String, String, String)
	 */
	public static void open() {
		open(sqlDriver, sqlUrl, sqlUser, sqlPassword);
	}
	
	/**
	 * Sets up a connection to the database with the given
	 * parameters. Checks for non-null values.
	 * 
	 * @param driver the driver to use for the connection
	 * @param url the url to use for the connection
	 * @param user the user to use for the connection
	 * @param password the password to use for the connection
	 * 
	 * @throws NullPointerException if at least on of the given arguments is <code>null</code>.
	 */
	public static void open(String driver, String url, String user, String password) throws NullPointerException {
		if (driver == null 	||
			url == null		||
			user == null	||
			password == null) {
			
			throw new NullPointerException(
					"You have to provide non-null values for a database driver, url, user and password. " +
					"You entered:" +
					"\n\tDriver: " + driver + 
					"\n\tURL: " + url + 
					"\n\tUser: " + user + 
					"\n\tPassword: " + password);
		}
		
		LOG.debug(	"Setup connection to database:\n" +
					"\tDriver: {}\n\tURL: {}\n\tUser: {}\n\tPassword: {}",
					new Object[] {driver, url, user, password});
		
		// Create database instance
		if (db == null) {
			db = new DB(DB_LOGICAL_NAME);
		} else
			LOG.warn("There is already a database instance. A new one is not created.");
		
		// Setup connection to database if not already done
		if (db != null && !db.hasConnection())
			db.open(driver, url, user, password);
		else
			LOG.warn("A connection to the database called {} was already opened.", DB_LOGICAL_NAME);
		
		// Let the connection to the relational database be closed correctly
		registerDbShutdownHook();
	}
	
	/**
	 * Closes the connection to the database.
	 */
	public static void close() {
		if (db != null && db.hasConnection()) {
			LOG.debug("Closing connection to database.");
			db.close();
		}
	}
	
	//###################################################################
	// Graph database (Neo4j)
	//###################################################################
	
	/**
	 * Creates a Neo4j <code>EmbeddedGraphDatabase</code> if not already done
	 * and returns it. This is a convenience method for <code>graph(storagePath)</code>
	 * using the default storage path for the creation of a Neo4j graph database.
	 * 
	 * @return an instance of <code>EmbeddedGraphDatabase</code> with the storage
	 * directory specified in the properties file 'db.properties' with the key
	 * 'neo4j.path'.
	 * @see DBUtil#graph(String)
	 */
	public synchronized static GraphDatabaseService graph() {
		return graph(neo4jPath);
	}

	/**
	 * Creates a Neo4j <code>EmbeddedGraphDatabase</code> if not already done
	 * and returns it.
	 * 
	 * @param storagePath the path to store the graph database in
	 * @return an instance of <code>EmbeddedGraphDatabase</code> with the storage
	 * directory specified in <code>storagePath</code>.
	 * @throws NullPointerException if the given <code>storagePath</code> is <code>null</code>
	 */
	public synchronized static GraphDatabaseService graph(String storagePath) {
		if (graph == null && storagePath != null) {
			LOG.debug(	"Setup connection to Neo4j graph database:\n" +
						"\tStorage path: {}", storagePath);
			graph = new EmbeddedGraphDatabase(storagePath);
			
			// Let the graph database shutdown correctly
			registerGraphShutdownHook();
		}
		else if (storagePath == null) {
			throw new NullPointerException(
					"You have to specify a non-null storage path for the proper creation of " +
					"a Neo4j graph database.");
		}
		
		return graph;
	}
	
	/**
	 * Closes the Neo4j <code>EmbeddedGraphDatabase</code> if it was 
	 * instantiated beforehand.
	 * 
	 * @see GraphDatabaseService#shutdown()
	 */
	public static void closeGraph() {
		if (graph != null) {
			LOG.debug("Closing connection to graph database.");
			graph.shutdown();
			graph = null;
		}
	}
	
	/**
	 * Creates a Neo4j Cypher <code>ExecutionEngine</code> to run
	 * Cypher queries on the graph database.
	 * 
	 * @return an instance of <code>ExecutionEngine</code>.
	 * @see org.neo4j.cypher.javacompat.ExecutionEngine ExecutionEngine
	 */
	public static ExecutionEngine cypherEngine() {
		LOG.debug("Creation of Cypher execution engine.");
		
		return new ExecutionEngine(graph());
	}
	
	//###################################################################
	// Helper
	//###################################################################
	
	/**
	 * This hook is registered when a connection to the relational database 
	 * is opened. If the JVM is shutdown all shutdown hooks are invoked. This 
	 * ensures that the connection to the relational database is closed 
	 * successfully in any case.
	 * 
	 */
	private static void registerDbShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }
        }
        );
    }
	
	/**
	 * This hook is registered when the graph database is invoked. If the 
	 * JVM is shutdown all shutdown hooks are invoked. This ensures that the 
	 * graph database is shutdown successfully in any case.
	 * 
	 */
	private static void registerGraphShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                closeGraph();
            }
        }
        );
    }

	//###################################################################
	// Setter & Getter
	//###################################################################
	
	/**
	 * @return the the path to the Neo4j graph database.
	 */
	public static String getNeo4jPath() {
		return neo4jPath;
	}
}
