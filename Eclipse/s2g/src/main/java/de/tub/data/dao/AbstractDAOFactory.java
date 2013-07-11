package de.tub.data.dao;

import java.util.HashMap;
import java.util.Set;


/**
 * This class is an abstract implementation of
 * a DAO Factory. It is part of the Data Access Object
 * and Abstract Factory design pattern.
 * 
 * @author Sebastian Oelke
 *
 */
public abstract class AbstractDAOFactory {

	private HashMap<Class<?>, IDao> daos = new HashMap<Class<?>, IDao>();
	
	/**
	 * Adds a concrete DAO to the available DAOs.
	 * 
	 * @param clas The key for this DAO.
	 * @param dao The DAO.
	 * @return the previous DAO associated with the specified key or <code>null</code> if there was no 
	 * 			mapping for this key or the associated value was <code>null</code> before. 
	 */
	public IDao addDAO(Class<?> clas, IDao dao) {
		return daos.put(clas, dao);
	}
	
	/**
	 * Finds a specific DAO which is associated with the given key.
	 * 
	 * @param clas The key for the DAO to get.
	 * @return the DAO which is associated with the given key or <code>null</code> 
	 * 			if there is no mapping for this key or the associated value is <code>null</code>.
	 */
	public IDao getDAO(Class<?> clas) {
		return daos.get(clas);
	}
	
	/**
	 * Removes the DAO associated with the given key.
	 * 
	 * @param clas The key associated with the DAO which is to be removed.
	 * @return the DAO which was associated with the given key or <code>null</code> 
	 * 			if there was no mapping for this key or the associated value was 
	 * 			<code>null</code> before.
	 */
	public IDao removeDAO(Class<?> clas) {
		return daos.remove(clas);
	}
	
	/**
	 * Gives an overview of available DAOs held by the DAO registration
	 * of this DAO Factory.
	 * 
	 * @return all available DAOs as a Set.
	 */
	public Set<Class<?>> getAvailableDAOs() {
		return daos.keySet();
	}
}
