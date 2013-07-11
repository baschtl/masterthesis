package de.tub.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import de.tub.TestHelper;

public class FileUtilTest {

	public static final String DIRECTORY_TO_CREATE = "fileUtilTestDir";
	
	@Test
	public void testIfNotExistCreateDir() {
		// Test null and empty file strings
		assertFalse("A test directory should not have been created because of a null file string.", FileUtil.ifNotExistCreateDir(null));
		assertFalse("A test directory should not have been created because of an empty file string.", FileUtil.ifNotExistCreateDir(""));
		
		// Create test directory
		assertTrue("A test directory should have been created.", FileUtil.ifNotExistCreateDir(DIRECTORY_TO_CREATE));
	
		// Test if directory was created
		File createdDir = new File(DIRECTORY_TO_CREATE);
		assertTrue("The test directory should be really existent.", createdDir.exists());
		assertTrue("The test directory should be a directory.", createdDir.isDirectory());
		
		assertTrue("A test directory should already have been created before.", FileUtil.ifNotExistCreateDir(DIRECTORY_TO_CREATE));
	
		// Clean up
		TestHelper.deleteFileOrDirectory(DIRECTORY_TO_CREATE);
	}

}
