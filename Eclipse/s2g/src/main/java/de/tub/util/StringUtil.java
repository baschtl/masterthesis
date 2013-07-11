package de.tub.util;

import java.io.File;

/**
 * This class provides utility methods for the work with
 * Strings.
 * 
 * @author Sebastian Oelke
 *
 */
public class StringUtil {

	/**
	 * Replaces all occurrences of '/' and '\' by the system-dependent
	 * file-separator and returns a new string. If the given path string
	 * is <code>null</code> or empty it is returned as it was given.
	 * 
	 * @param path the path to do replacement on.
	 * @return a path with system-dependent file-separators.
	 */
	public static String validateFileSeparator(String path) {
		if (path == null || path.isEmpty()) return path;
		
		return path.replaceAll("/|\\\\", File.separator); 
	}
	
	/**
	 * Concatenates the strings in the given array with the 
	 * system-dependent file-separator and returns a new string. Empty
	 * or <code>null</code> strings are ignored.
	 * 
	 * @param names the strings to concatenate.
	 * @return a new string with the given strings concatenated with the
	 * system-dependent file-separator or <code>null</code> if the given
	 * array is <code>null</code> or empty.
	 */
	public static String concatWithFileSeparator(String[] names) {
		if (names == null || names.length == 0) return null;
		
		StringBuilder sb = new StringBuilder();
		String name = null;
		
		for (int i = 0; i < names.length; i++) {
			name = names[i];
			if (name != null && !name.isEmpty()) {
				sb.append(name);
				if (i < names.length - 1) sb.append(File.separator);
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Removes all leading and trailing slashes and backslashes from the given string.
	 * If the given path string is <code>null</code> or empty it is 
	 * returned as it was given.
	 * 
	 * @param path the string from which to remove trailing slashes and backslashes from.
	 * @return a new string with trailing slashes and backslashes removed.
	 */
	public static String trimPathSeparators(String path) {
		if (path == null || path.isEmpty()) return path;
		
		return path.replaceAll("^(/|\\\\)+|(/|\\\\)+$", "");
	}
	
	/**
	 * Removes all leading and trailing single quotes from the given string.
	 * If the given path string is <code>null</code> or empty it is 
	 * returned as it was given.
	 * 
	 * @param in the string from which to remove leading and trailing single quotes from. 
	 * @return a new string with leading and trailing single quotes removed.
	 */
	public static String trimSingleQuotes(String in) {
		if (in == null || in.isEmpty()) return in;
		
		return in.replaceAll("^'*([\\w\\s\\p{Punct}&&[^']]*)'*$", "$1");
	}
	
}
