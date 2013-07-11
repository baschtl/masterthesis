package de.tub.util;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

public class PropertiesUtilTest {
	public final static String TEST_PROP_FILE = "/app.properties";
	
	@Test
	public void testLoad() {
		// Load test properties file
		Properties prop = PropertiesUtil.load(TEST_PROP_FILE);
		
		assertNotNull(prop.getProperty("app.preprocess.out_file"));
	}

}
