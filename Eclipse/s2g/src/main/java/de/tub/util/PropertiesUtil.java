package de.tub.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides utility methods for the work with
 * Properties.
 * 
 * @author Sebastian Oelke
 *
 */
public class PropertiesUtil {

	private static final Logger LOG = LoggerFactory.getLogger(PropertiesUtil.class);
	
    /**
     * Loads a properties resource with the given name. For rules of
     * how to name the properties resource see 
     * <code>Class.getResourceAsStream(String)</code>.
     * 
     * @param resource the properties resource to read.
     * @see java.lang.Class#getResourceAsStream(String)
     */
	public static Properties load(String resource) {
		if (resource == null || resource.isEmpty()) {
			LOG.warn("The given resource string is null or empty. Cannot load properties without a valid resource.");
			return null;
		}
		
		// Load properties file
		Properties prop = new Properties();
		InputStream in = PropertiesUtil.class.getResourceAsStream(resource);
		try {
			prop.load(in);
			in.close();
		} catch (IOException e) {
			LOG.error("An error occurred while loading the properties file {}:\n{}", resource, e);
		}
		
		return prop;
	}
}
