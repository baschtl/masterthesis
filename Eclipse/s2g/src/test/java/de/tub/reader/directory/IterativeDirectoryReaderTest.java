package de.tub.reader.directory;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.tub.TestPropertiesLoader;
import de.tub.reader.ReaderFactory;
import de.tub.reader.SimpleFileReader;
import de.tub.util.StringUtil;

public class IterativeDirectoryReaderTest {

	public static final String ROOT_CHILD_ONE = "child1";
	public static final String ROOT_CHILD_TWO = "child2";
	public static final String ROOT_SUBDIR = "subdir";
	
	@Test
	public void testReadDirectories() {
		// Create IterativeDirectoryReader with proper paths to test directories
		IterativeDirectoryReader dr = (IterativeDirectoryReader) ReaderFactory.instance().getIterativeDirectoryReader();
		dr.setDirectoryName(TestPropertiesLoader.getReaderRootDir());
		dr.setPathInChildDirectory(ROOT_SUBDIR);
		
		// Set a simplified reader which safes the directories found by the IterativeDirectoryReader
		SimpleFileReader sfr = new SimpleFileReader();
		dr.setReader(sfr);
		
		// Execute IterativeDirectoryReader
		dr.read();
		
		// Test SimpleReader for found directories
		assertEquals(StringUtil.concatWithFileSeparator(
				new String[] {TestPropertiesLoader.getReaderRootDir(), ROOT_CHILD_ONE, ROOT_SUBDIR}), 
				sfr.getFiles().get(0).getPath());
		assertEquals(StringUtil.concatWithFileSeparator(
				new String[] {TestPropertiesLoader.getReaderRootDir(), ROOT_CHILD_TWO, ROOT_SUBDIR}), 
				sfr.getFiles().get(1).getPath());
	}
}
