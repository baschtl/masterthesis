package de.tub.reader;

import java.util.HashMap;
import java.util.Set;

/**
 * This class is an abstract implementation of
 * a Reader Factory. It is part of the Abstract 
 * Factory design pattern.
 * 
 * @author Sebastian Oelke
 *
 */
public abstract class AbstractReaderFactory {
	private HashMap<Class<?>, IReader> readers = new HashMap<Class<?>, IReader>();
	
	/**
	 * Adds a concrete reader to the available readers.
	 * 
	 * @param clas The key for this reader.
	 * @param reader The reader.
	 * @return the previous reader associated with the specified key or <code>null</code> if there was no 
	 * 			mapping for this key or the associated value was <code>null</code> before. 
	 */
	public IReader addReader(Class<?> clas, IReader reader) {
		return readers.put(clas, reader);
	}
	
	/**
	 * Finds a specific reader which is associated with the given key.
	 * 
	 * @param clas The key for the reader to get.
	 * @return the reader which is associated with the given key or <code>null</code> 
	 * 			if there is no mapping for this key or the associated value is <code>null</code>.
	 */
	public IReader getReader(Class<?> clas) {
		return readers.get(clas);
	}
	
	/**
	 * Removes the reader associated with the given key.
	 * 
	 * @param clas The key associated with the reader which is to be removed.
	 * @return the reader which was associated with the given key or <code>null</code> 
	 * 			if there was no mapping for this key or the associated value was 
	 * 			<code>null</code> before.
	 */
	public IReader removeReader(Class<?> clas) {
		return readers.remove(clas);
	}
	
	/**
	 * Removes the all readers of this Reader Factory.
	 * 
	 */
	public void removeAll() {
		readers.clear();
	}
	
	/**
	 * Gives an overview of available readers held by the reader registration
	 * of this Reader Factory.
	 * 
	 * @return all available readers as a Set.
	 */
	public Set<Class<?>> getAvailableReaders() {
		return readers.keySet();
	}
}
