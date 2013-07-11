package de.tub.data.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

/**
 * @author Sebastian Oelke
 *
 */
public class DAOFactoryTest {

	@Test
	public void testGetInstance() {
		DAOFactory daoF = DAOFactory.instance();
		
		assertNotNull("The DAO Factory instance is null.", daoF);
		assertEquals("The DAO Factory instance is not of type DAOFactory.", DAOFactory.class, daoF.getClass());
		
		assertEquals(4, daoF.getAvailableDAOs().size());
	}

	@Test
	public void testGetAvailableDAOs() {
		Set<Class<?>> availDaos = DAOFactory.instance().getAvailableDAOs();
		
		assertFalse("There are no available DAOs.", availDaos.isEmpty());
		assertTrue("The DAO Factory does not contain " + Neo4JFrameworkClusterDAO.class.getName(), availDaos.contains(Neo4JFrameworkClusterDAO.class));
		assertTrue("The DAO Factory does not contain " + Neo4JStaypointDAO.class.getName(), availDaos.contains(Neo4JStaypointDAO.class));		
	}
	
	@Test
	public void testGetDAO() {
		IDao callDao = DAOFactory.instance().getDAO(Neo4JStaypointDAO.class);
		
		assertEquals("The requested DAO is not of the expected type.", Neo4JStaypointDAO.class, callDao.getClass());
	}
	
	@Test
	public void testRemoveDAO() {
		assertEquals("The size of available DAOs is not as expected.", 4, DAOFactory.instance().getAvailableDAOs().size());
		assertTrue("The DAO Factory does not contain " + Neo4JStaypointDAO.class.getName(), 
				DAOFactory.instance().getAvailableDAOs().contains(Neo4JStaypointDAO.class));
		
		DAOFactory.instance().removeDAO(Neo4JStaypointDAO.class);
		
		assertEquals("The size of available DAOs is not as expected.", 3, DAOFactory.instance().getAvailableDAOs().size());
		assertFalse("The DAO Factory does still contain " + Neo4JStaypointDAO.class.getName(), 
				DAOFactory.instance().getAvailableDAOs().contains(Neo4JStaypointDAO.class));
	}
	
	@Test
	public void testAddDAO() {
		DAOFactory.instance().removeDAO(Neo4JStaypointDAO.class);
		
		assertEquals("The size of available DAOs is not as expected.", 3, DAOFactory.instance().getAvailableDAOs().size());
		assertFalse("The DAO Factory does still contain " + Neo4JStaypointDAO.class.getName(), 
				DAOFactory.instance().getAvailableDAOs().contains(Neo4JStaypointDAO.class));
		
		DAOFactory.instance().addDAO(Neo4JStaypointDAO.class, new Neo4JStaypointDAO());
		
		assertEquals("The size of available DAOs is not as expected.", 4, DAOFactory.instance().getAvailableDAOs().size());
		assertTrue("The DAO Factory does not contain " + Neo4JStaypointDAO.class.getName(), 
				DAOFactory.instance().getAvailableDAOs().contains(Neo4JStaypointDAO.class));
	}			
	
	@Test
	public void testGetNeo4JStaypointDAO() {
		Neo4JStaypointDAO spDao = (Neo4JStaypointDAO) DAOFactory.instance().getStaypointDAO();
		
		assertNotNull("The requested Neo4JStaypointDAO is null.", spDao);
		assertEquals("The requested Neo4JStaypointDAO is not of the expected type.", Neo4JStaypointDAO.class, spDao.getClass());
	}
	
	@Test
	public void testGetNeo4JFrameworkClusterDAO() {
		Neo4JFrameworkClusterDAO cDao = (Neo4JFrameworkClusterDAO) DAOFactory.instance().getFrameworkClusterDAO();
		
		assertNotNull("The requested Neo4JFrameworkClusterDAO is null.", cDao);
		assertEquals("The requested Neo4JFrameworkClusterDAO is not of the expected type.", Neo4JFrameworkClusterDAO.class, cDao.getClass());
	}
}
