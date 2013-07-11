package de.tub.reader.file;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import de.tub.TestPropertiesLoader;
import de.tub.reader.ReaderFactory;
import de.tub.reader.SimpleTextFileReader;
import de.tub.util.StringUtil;

public class IterativeFileReaderTest {

	public static final String FILE_ONE = "file1.txt";
	public static final String FILE_TWO = "file2.txt";
	
	@Test
	public void testReadFiles() {
		// Create IterativeFileReader with proper paths to test directories
		IterativeFileReader fr = (IterativeFileReader) ReaderFactory.instance().getIterativeFileReader();
		File file = new File(TestPropertiesLoader.getReaderFullDir());
		fr.setFile(file);
		
		// Set a simplified reader which safes the directories found by the IterativeDirectoryReader
		SimpleTextFileReader sfr = new SimpleTextFileReader();
		fr.setReader(sfr);
		
		// Execute IterativeDirectoryReader
		fr.read();
		
		// Test SimpleReader for found directories
		assertEquals(StringUtil.concatWithFileSeparator(
				new String[] {TestPropertiesLoader.getReaderFullDir(), FILE_ONE}), 
				sfr.getFiles().get(0));
		assertEquals(StringUtil.concatWithFileSeparator(
				new String[] {TestPropertiesLoader.getReaderFullDir(), FILE_TWO}), 
				sfr.getFiles().get(1));
	}

}
