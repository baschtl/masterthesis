package de.tub;

import java.util.Properties;

import de.tub.util.PropertiesUtil;

/**
 * This class provides means 
 * to access common test properties.
 * 
 * @author Sebastian Oelke
 *
 */
public class TestPropertiesLoader {
	public final static String TEST_PROP_FILE = "/app.properties";
	
	private static String outTestFile, readerRootDir, readerFullDir, sharedFrameworkInDir;
	private static int distanceThreshold, timeThreshold;
	
	static {
		// Load test properties file
		Properties prop = PropertiesUtil.load(TEST_PROP_FILE);
		
		// Set static fields to the test data files
		readerRootDir = prop.getProperty("app.reader_test_root_dir");
		readerFullDir = prop.getProperty("app.reader_test_full_dir");
		sharedFrameworkInDir = prop.getProperty("app.build.shared_framework.in_dir");
		
		// Set static field to test out file
		outTestFile = prop.getProperty("app.preprocess.out_file");
		
		// Set the distance and time threshold
		distanceThreshold = Integer.parseInt(prop.getProperty("app.staypoints.distance_threshold"));
		timeThreshold = Integer.parseInt(prop.getProperty("app.staypoints.time_threshold"));
	}
	
	//###################################################################
	// Getters
	//###################################################################

	/**
	 * @return the outTestFile
	 */
	public static String getOutTestFile() {
		return outTestFile;
	}

	/**
	 * @return the readerRootDir
	 */
	public static String getReaderRootDir() {
		return readerRootDir;
	}

	/**
	 * @return the distanceThreshold
	 */
	public static int getDistanceThreshold() {
		return distanceThreshold;
	}

	/**
	 * @return the timeThreshold
	 */
	public static int getTimeThreshold() {
		return timeThreshold;
	}

	/**
	 * @return the readerFullDir
	 */
	public static String getReaderFullDir() {
		return readerFullDir;
	}

	/**
	 * @return the sharedFrameworkInDir
	 */
	public static String getSharedFrameworkInDir() {
		return sharedFrameworkInDir;
	}
	
}
