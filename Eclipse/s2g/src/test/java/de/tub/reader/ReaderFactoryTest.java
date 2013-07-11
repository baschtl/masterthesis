package de.tub.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import de.tub.reader.file.TextFileLineReader;

/**
 * Tests the <code>ReaderFactory</code>. 
 * 
 * @author Sebastian Oelke
 * 
 * @see de.tub.reader.ReaderFactory
 *
 */
public class ReaderFactoryTest {

	@Test
	public void testGetInstance() {
		ReaderFactory rf = ReaderFactory.instance();
		
		assertNotNull("The Reader Factory instance is null.", rf);
		assertEquals("The Reader Factory instance is not of type ReaderFactory.", ReaderFactory.class, rf.getClass());
	}
	
	@Test
	public void testGetAvailableReaders() {
		Set<Class<?>> availReader = ReaderFactory.instance().getAvailableReaders();
		
		assertFalse("There are no available Reader.", availReader.isEmpty());
		assertTrue("The Reader Factory does not contain " + TextFileLineReader.class.getName(), availReader.contains(TextFileLineReader.class));
	}
	
	@Test
	public void testGetReader() {
		IReader textFileLineReader = ReaderFactory.instance().getReader(TextFileLineReader.class);
		
		assertEquals("The requested Reader is not of the expected type.", TextFileLineReader.class, textFileLineReader.getClass());
	}
	
	@Test
	public void testRemoveReader() {
		assertEquals("The size of available Readers is not as expected.", 4, ReaderFactory.instance().getAvailableReaders().size());
		assertTrue("The Reader Factory does not contain " + TextFileLineReader.class.getName(), ReaderFactory.instance().getAvailableReaders().contains(TextFileLineReader.class));
		
		ReaderFactory.instance().removeReader(TextFileLineReader.class);
		
		assertEquals("The size of available Reader is not as expected.", 3, ReaderFactory.instance().getAvailableReaders().size());
		assertFalse("The Reader Factory does still contain " + TextFileLineReader.class.getName(), ReaderFactory.instance().getAvailableReaders().contains(TextFileLineReader.class));
	}
	
	@Test
	public void testAddReader() {
		ReaderFactory.instance().removeAll();
		
		assertEquals("The size of available Reader is not as expected.", 0, ReaderFactory.instance().getAvailableReaders().size());
		assertFalse("The Reader Factory does still contain " + TextFileLineReader.class.getName(), ReaderFactory.instance().getAvailableReaders().contains(TextFileLineReader.class));
		
		ReaderFactory.instance().addReader(TextFileLineReader.class, new TextFileLineReader());
		
		assertEquals("The size of available Reader is not as expected.", 1, ReaderFactory.instance().getAvailableReaders().size());
		assertTrue("The Reader Factory does not contain " + TextFileLineReader.class.getName(), ReaderFactory.instance().getAvailableReaders().contains(TextFileLineReader.class));
	}

	@Test
	public void testGetTextFileLineReader() {
		IReader textFileLineReader = ReaderFactory.instance().getTextFileLineReader();
		
		assertNotNull("The requested TextFileLineReader is null.", textFileLineReader);
		assertEquals("The requested TextFileLineReader is not of the expected type.", TextFileLineReader.class, textFileLineReader.getClass());
	}
}
