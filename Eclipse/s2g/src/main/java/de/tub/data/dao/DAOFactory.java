package de.tub.data.dao;

import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is an implementation of a DAO Factory. 
 * It is part of the Data Access Object and Abstract 
 * Factory design pattern.
 * <p />
 * Because there is only one 
 * <code>DAOFactory</code> per application this class 
 * is implemented using the Singleton design pattern.
 * 
 * @author Sebastian Oelke
 *
 */
public class DAOFactory extends AbstractDAOFactory {
	
	private static DAOFactory instance;
	private static Logger LOG = LoggerFactory.getLogger(DAOFactory.class);
	
	private DAOFactory() {
		// Initialize DAO registration with call to super constructor
		super();
		
		// Add initial prototypes to DAO registration
		addDAO(Neo4JFrameworkClusterDAO.class, new Neo4JFrameworkClusterDAO());
		addDAO(Neo4JHGClusterDAO.class, new Neo4JHGClusterDAO());
		addDAO(Neo4JStaypointDAO.class, new Neo4JStaypointDAO());
		addDAO(Neo4JUserDAO.class, new Neo4JUserDAO());
	}
	
	/**
	 * Create an instance of the <code>DAOFactory</code> if not already
	 * done and return it.
	 * 
	 * @return an instance of the <code>DAOFactory</code>.
	 */
	public synchronized static DAOFactory instance() {
		if (instance == null) 
			instance = new DAOFactory();
		
		return instance;
	}
	
	/**
	 * Returns an instance of the <code>Neo4JFrameworkClusterDAO</code> class.
	 * If it was not possible to create the instance 
	 * <code>null</code> is returned.
	 * 
	 * @return an instance of the <code>Neo4JFrameworkClusterDAO</code> class.
	 */
	public IFrameworkClusterDAO<Node> getFrameworkClusterDAO() {
		Neo4JFrameworkClusterDAO fClusterDao = (Neo4JFrameworkClusterDAO) getDAO(Neo4JFrameworkClusterDAO.class);
		try {
			return fClusterDao.clone();
		} catch (CloneNotSupportedException e) {
			LOG.error("Cloning {} did not work:\n{}", Neo4JFrameworkClusterDAO.class, e);
			return null;
		}
	}
	
	/**
	 * Returns an instance of the <code>Neo4JHierarchicalGraphClusterDAO</code> class.
	 * If it was not possible to create the instance 
	 * <code>null</code> is returned.
	 * 
	 * @return an instance of the <code>Neo4JHierarchicalGraphClusterDAO</code> class.
	 */
	public IHGClusterDAO<Node> getHGClusterDAO() {
		Neo4JHGClusterDAO hgClusterDao = (Neo4JHGClusterDAO) getDAO(Neo4JHGClusterDAO.class);
		try {
			return hgClusterDao.clone();
		} catch (CloneNotSupportedException e) {
			LOG.error("Cloning {} did not work:\n{}", Neo4JHGClusterDAO.class, e);
			return null;
		}
	}
	
	/**
	 * Returns an instance of the <code>Neo4JStaypointDAO</code> class.
	 * If it was not possible to create the instance 
	 * <code>null</code> is returned.
	 * 
	 * @return an instance of the <code>Neo4JStaypointDAO</code> class.
	 */
	public IStaypointDAO<Node> getStaypointDAO() {
		Neo4JStaypointDAO stayPointDao = (Neo4JStaypointDAO) getDAO(Neo4JStaypointDAO.class);
		try {
			return stayPointDao.clone();
		} catch (CloneNotSupportedException e) {
			LOG.error("Cloning {} did not work:\n{}", Neo4JStaypointDAO.class, e);
			return null;
		}
	}
	
	/**
	 * Returns an instance of the <code>Neo4JUserDAO</code> class.
	 * If it was not possible to create the instance 
	 * <code>null</code> is returned.
	 * 
	 * @return an instance of the <code>Neo4JUserDAO</code> class.
	 */
	public IUserDAO<Node> getUserDAO() {
		Neo4JUserDAO userDao = (Neo4JUserDAO) getDAO(Neo4JUserDAO.class);
		try {
			return userDao.clone();
		} catch (CloneNotSupportedException e) {
			LOG.error("Cloning {} did not work:\n{}", Neo4JUserDAO.class, e);
			return null;
		}
	}
	
}
