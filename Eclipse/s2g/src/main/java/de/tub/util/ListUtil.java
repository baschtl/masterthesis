package de.tub.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides utility methods for the work with
 * lists.
 * 
 * @author Sebastian Oelke
 *
 */
public class ListUtil {

	private static final Logger LOG = LoggerFactory.getLogger(ListUtil.class);
	
	/**
	 * Finds duplicates in the given list and returns them
	 * in a new set.
	 * 
	 * @param list the list to find duplicates in.
	 * @return a set with the found duplicates.
	 */
	public static <T> Set<T> findDuplicates(List<T> list) {
		if (list == null) {
			LOG.warn("You provided a null value as a list. " +
					"This value is expected to be non-null. So, null is returned.");
			return null;
		}
		
		final Set<T> resultingSet = new HashSet<T>();
		final Set<T> tempSet = new HashSet<T>();

		// Check the given list for duplicates
		for (int i = 0; i < list.size(); i++) {
			T item = list.get(i);
			// Add returns false if the temporary set already contained the item, so its a duplicate
			if (!tempSet.add(item))
				resultingSet.add(item);
		}
		
		return resultingSet;
	}
	
	/**
	 * Joins the elements of the array with an optional separator.
	 * 
	 * @param array the array that holds elements to join.
	 * @param with the value with which the elements of the array get separated during the join operation. 
	 * If this is <code>null</code> the elements are joined without any separator.
	 * @return a new string that holds the joined array elements or <code>null</code> if the given array
	 * is <code>null</code>.
	 */
	public static <T> String join(T[] array, String with, String encloseWith) {
		if (array == null) {
			LOG.warn("You provided a null value as an array. " +
					"This value is expected to be non-null. So, null is returned.");
			return null;
		}
		
		// Enclose the data string if encloseWith is not null or empty
		boolean enclose = encloseWith != null && !encloseWith.isEmpty();
		
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			// Prepend string with enclosing string
			if (enclose) result.append(encloseWith);
			
			// Write data string
			result.append(array[i].toString());
			
			// Append enclosing string
			if (enclose) result.append(encloseWith);
			
			// Append the separator
			if (i < array.length - 1 && with != null) 
				result.append(with);
		}
		
		return result.toString();
	}
	
}
