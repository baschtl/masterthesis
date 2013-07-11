package de.tub.writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.junit.Test;

import de.tub.TestHelper;

public class ArrayToCsvWriterTest {

	public static final String OUT_DIR = "doubles";
	
	public static final double[][] TEST_DOUBLES = new double[][] {
		new double[] { 0.1, 0.2, 0.3 }, 
		new double[] { 0.4, 0.5, 0.6 },
		new double[] { 0.7, 0.8, 0.9 }
	};
	
	public static final String[] ADDITIONAL_INFORMATION = new String[] {
		"additional", "information"
	};
	
	@Test
	public void testWriteDoubles() {
		// Test null and empty out dir
		boolean exceptionThrown = false;
		try {
			ArrayToCsvWriter.writeDoubles(TEST_DOUBLES, null, ADDITIONAL_INFORMATION);
		} catch (NullPointerException e) {
			exceptionThrown = true;
		}
		assertTrue("An exception should have been thrown.", exceptionThrown);
		
		try {
			ArrayToCsvWriter.writeDoubles(TEST_DOUBLES, "", ADDITIONAL_INFORMATION);
		} catch (IllegalArgumentException e) {
			exceptionThrown = true;
		}
		assertTrue("An exception should have been thrown.", exceptionThrown);
		
		// Write doubles to file
		ArrayToCsvWriter.writeDoubles(TEST_DOUBLES, OUT_DIR, ADDITIONAL_INFORMATION);
		
		// Test file creation
		File outDir = new File(OUT_DIR);
		
		// One file should have been created
		assertEquals("One file should have been created.", 1, outDir.listFiles().length);
		
		File doubles = outDir.listFiles()[0];
		
		// Test created file
		assertNotNull("The created file instance should not be null.", doubles);
		assertTrue("The created file should exist.", doubles.exists());
		assertTrue("The created file should be a file.", doubles.isFile());
		
		// Read created file line-wise
		FileInputStream in = null;
		
		exceptionThrown = false;
		try {
			in = new FileInputStream(doubles);
		} catch (Exception e) {
			exceptionThrown = true;
		}
		assertFalse("No exception is expected when opening the file stream.", exceptionThrown);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		
		// We expect six lines in the file
		try {
			line = br.readLine();
			assertNotNull(line);
			assertTrue(line.startsWith("# Created: "));
			
			line = br.readLine();
			assertNotNull(line);
			assertEquals("# additional: information", line);
			
			line = br.readLine();
			assertNotNull(line);
			assertEquals("", line);
			
			line = br.readLine();
			assertNotNull(line);
			assertEquals("0.1,0.2,0.3", line);
			
			line = br.readLine();
			assertNotNull(line);
			assertEquals("0.4,0.5,0.6", line);
			
			line = br.readLine();
			assertNotNull(line);
			assertEquals("0.7,0.8,0.9", line);
		} catch (Exception e) {
			exceptionThrown = true;
		}
		assertFalse("No exception is expected when reading the file.", exceptionThrown);
	
		// Clean up
		TestHelper.deleteFileOrDirectory(OUT_DIR);
	}

}
