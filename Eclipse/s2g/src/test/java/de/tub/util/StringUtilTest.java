package de.tub.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

public class StringUtilTest {

	private boolean isWin; 
	
	public final static String PATH_WITH_BACKSLASHES = "\\path\\with\\backslashes\\";
	public final static String PATH_WITH_SOME_BACKSLASHES_1 = "\\path\\with/backslashes\\";
	public final static String PATH_WITH_SOME_BACKSLASHES_2 = "\\path\\with\\backslashes/";
	public final static String PATH_WITH_SOME_BACKSLASHES_3 = "/path\\with\\backslashes/";
	public final static String PATH_WITH_BACKSLASHES_RESULT_OSX_LINUX = "/path/with/backslashes/";
	public final static String PATH_WITH_BACKSLASHES_RESULT_WIN = "\\path\\with\\backslashes\\";
	public final static String PATH_WITH_BACKSLASHES_ALT_RESULT = "path\\with\\backslashes";
	
	public final static String PATH_WITH_LEADING_BACKSLASH = "\\path";
	public final static String PATH_WITH_LEADING_BACKSLASH_RESULT_OSX_LINUX = "/path";
	public final static String PATH_WITH_LEADING_BACKSLASH_RESULT_WIN = "\\path";
	
	public final static String PATH_WITH_TAILING_BACKSLASH = "path\\";
	public final static String PATH_WITH_TAILING_BACKSLASH_RESULT_OSX_LINUX = "path/";
	public final static String PATH_WITH_TAILING_BACKSLASH_RESULT_WIN = "path\\";
	
	public final static String PATH_PART_1 = "path";
	public final static String PATH_PART_2 = "to";
	public final static String PATH_PART_3 = "some";
	public final static String PATH_PART_4 = "where";
	public final static String PATH_PART_RESULT_OSX_LINUX = "path/to/some/where";
	public final static String PATH_PART_RESULT_WIN = "path\\to\\some\\where";
	
	public final static String PATH_WITH_SINGLE_QUOTES_1 = "'single quotes please'";
	public final static String PATH_WITH_SINGLE_QUOTES_2 = "'1234-12-34 12:34:56'";
	public final static String PATH_WITH_SINGLE_QUOTES_RESULT_1 = "single quotes please";
	public final static String PATH_WITH_SINGLE_QUOTES_RESULT_2 = "1234-12-34 12:34:56";
	
	@Before
	public void detectOS() {
		if (System.getProperty("os.name").startsWith("Windows")) isWin = true;
		else isWin = false;
	}
	
	@Test
	public void testValidateFileSeparator() {
		// Test null and empty path
		assertNull(StringUtil.validateFileSeparator(null));
		assertEquals("", StringUtil.validateFileSeparator(""));
		
		// Set result depending on OS
		String result;
		if (isWin) result = PATH_WITH_BACKSLASHES_RESULT_WIN;
		else result = PATH_WITH_BACKSLASHES_RESULT_OSX_LINUX;
		
		// Test various paths
		assertEquals(result, StringUtil.validateFileSeparator(PATH_WITH_BACKSLASHES));
		assertEquals(result, StringUtil.validateFileSeparator(PATH_WITH_SOME_BACKSLASHES_1));
		assertEquals(result, StringUtil.validateFileSeparator(PATH_WITH_SOME_BACKSLASHES_2));
		assertEquals(result, StringUtil.validateFileSeparator(PATH_WITH_SOME_BACKSLASHES_3));
		
		if (isWin) result = PATH_WITH_LEADING_BACKSLASH_RESULT_WIN;
		else result = PATH_WITH_LEADING_BACKSLASH_RESULT_OSX_LINUX;
		
		assertEquals(result, StringUtil.validateFileSeparator(PATH_WITH_LEADING_BACKSLASH));
		
		if (isWin) result = PATH_WITH_TAILING_BACKSLASH_RESULT_WIN;
		else result = PATH_WITH_TAILING_BACKSLASH_RESULT_OSX_LINUX;
		
		assertEquals(result, StringUtil.validateFileSeparator(PATH_WITH_TAILING_BACKSLASH));
	}
	
	@Test
	public void testConcatWithFileSeparator() {
		// Test null and empty path
		assertNull(StringUtil.concatWithFileSeparator(null));
		assertNull(StringUtil.concatWithFileSeparator(new String[] {}));
		
		String result;
		if (isWin) result = PATH_PART_RESULT_WIN;
		else result = PATH_PART_RESULT_OSX_LINUX;
		
		assertEquals(result, StringUtil.concatWithFileSeparator(
				new String[] {PATH_PART_1, PATH_PART_2, PATH_PART_3, PATH_PART_4}));
	}
	
	@Test
	public void testTrimFileSeparator() {
		// Test null and empty path
		assertNull(StringUtil.trimPathSeparators(null));
		assertEquals("", StringUtil.trimPathSeparators(""));
		
		// Test a path
		assertEquals(PATH_WITH_BACKSLASHES_ALT_RESULT, StringUtil.trimPathSeparators(PATH_WITH_SOME_BACKSLASHES_2));
	}
	
	@Test
	public void testTrimSingleQuotes() {
		// Test null and empty string
		assertNull(StringUtil.trimSingleQuotes(null));
		assertEquals("", StringUtil.trimSingleQuotes(""));
		
		// Test single quote strings
		assertEquals(PATH_WITH_SINGLE_QUOTES_RESULT_1, StringUtil.trimSingleQuotes(PATH_WITH_SINGLE_QUOTES_1));
		assertEquals(PATH_WITH_SINGLE_QUOTES_RESULT_2, StringUtil.trimSingleQuotes(PATH_WITH_SINGLE_QUOTES_2));
	}

}
