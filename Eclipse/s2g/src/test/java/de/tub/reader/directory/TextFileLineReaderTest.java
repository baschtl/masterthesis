package de.tub.reader.directory;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.junit.Test;

import de.tub.reader.ReaderFactory;
import de.tub.reader.SimpleTextLineProcessor;
import de.tub.reader.file.TextFileLineReader;

public class TextFileLineReaderTest {

	public static final String FILE = "/line_data.txt";
	public static final String LINE_START = "line";
	
	@Test
	public void testReadDirectories() {
		URL url = TextFileLineReaderTest.class.getResource(FILE);
		
		// Create TextFileLineReader with proper paths to test directories
		TextFileLineReader lr = (TextFileLineReader) ReaderFactory.instance().getTextFileLineReader();
		lr.setResourceName(url.getFile());
		
		// Set simplified processor
		SimpleTextLineProcessor p = new SimpleTextLineProcessor();
		lr.setProcessor(p);
		
		// Execute IterativeDirectoryReader
		lr.read();
		
		// Test SimpleTextLineProcessor for found text lines and their content
		assertEquals("The number of lines read is not right.", 4, p.getLines().size());
		for (int i = 0; i < p.getLines().size(); i++) {
			assertEquals("The content of this line is not right.", LINE_START + i, p.getLines().get(i));
		}
	}
}
