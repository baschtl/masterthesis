package de.tub;

import java.io.File;

/**
 * This class provides methods used by various test
 * classes.
 * 
 * @author Sebastian Oelke
 *
 */
public class TestHelper {

	/**
	 * Deletes the directory or file with the given path 
	 * and all its children.
	 * 
	 * @param path the name of the file to delete.
	 */
	public static void deleteFileOrDirectory(String path) {
		File file = new File(path);
		deleteFileOrDirectory(file);
	}
	
	/**
	 * Deletes the given directory or file and all its children.
	 * 
	 * @param file the file to delete.
	 */
	public static void deleteFileOrDirectory(File file) {
		if (file == null || !file.exists()) return;

        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteFileOrDirectory(child);
            }
        }
        file.delete();
	}
	
}
