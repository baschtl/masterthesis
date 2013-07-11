package de.tub.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * @author Sebastian Oelke
 *
 */
public class ListUtilTest {

	private static final String DUP_1 = "one";
	private static final String DUP_2 = "four";
	
	private static final String[] ARRAY_TO_JOIN = {"one", "two", "three"};
	private static final String ARRAY_TO_JOIN_RESULT = "one two three";
	private static final String ARRAY_TO_JOIN_WITH_ENCLOSING_RESULT = "'one' 'two' 'three'";
	
	@Test
	public void testFindDuplicates() {
		// Build a list with duplicates
		List<String> duplicatesList = new ArrayList<String>();
		duplicatesList.add(DUP_1);
		duplicatesList.add("two");
		duplicatesList.add(DUP_1);
		duplicatesList.add("three");
		duplicatesList.add(DUP_2);
		duplicatesList.add("five");
		duplicatesList.add(DUP_2);
		
		// Find duplicates
		Set<String> duplicates = ListUtil.findDuplicates(duplicatesList);
		assertNotNull("The duplicates set should not be null.", duplicates);
		assertEquals("There are more or less duplicates as expected.", 2, duplicates.size());
		
		// Test found duplicates
		List<String> expectedDuplicates = new ArrayList<String>();
		expectedDuplicates.add(DUP_1);
		expectedDuplicates.add(DUP_2);
		
		List<String> expectedDuplicatesInResult = new ArrayList<String>();
		List<String> notExpectedDuplicatesInResult = new ArrayList<String>();
		
		for (String duplicate : duplicates) {
			if (expectedDuplicates.contains(duplicate))
				expectedDuplicatesInResult.add(duplicate);
			else
				notExpectedDuplicatesInResult.add(duplicate);
				
		}
		assertEquals("The expected number of duplicates is not correct.", 2, expectedDuplicatesInResult.size());
		assertEquals("Not expected duplicates were found.", 0, notExpectedDuplicatesInResult.size());
		
		for (String expectedDuplicate : expectedDuplicates) {
			assertTrue("A not expected duplicate was found.", expectedDuplicatesInResult.contains(expectedDuplicate));
		}
		
		// Test null parameter
		assertNull("Given a null list null should be returned.", ListUtil.findDuplicates(null));
		
		// Test no duplicate
		duplicatesList.clear();
		duplicatesList.add("one");
		duplicatesList.add("two");
		duplicatesList.add("three");
		
		duplicates = ListUtil.findDuplicates(duplicatesList);
		assertEquals("No duplicates should have been found.", 0, duplicates.size());
	}
	
	@Test
	public void testJoin() {
		// Without enclosing
		String joinResult = ListUtil.join(ARRAY_TO_JOIN, " ", null);
		assertNotNull("The join result should not be null.", joinResult);
		assertEquals("The result of the join is not as expected.", ARRAY_TO_JOIN_RESULT, joinResult);
		
		// With enclosing
		joinResult = ListUtil.join(ARRAY_TO_JOIN, " ", "'");
		assertNotNull("The join result should not be null.", joinResult);
		assertEquals("The result of the join is not as expected.", ARRAY_TO_JOIN_WITH_ENCLOSING_RESULT, joinResult);
		
		// Test null value
		joinResult = ListUtil.join(null, " ", null);
		assertNull("The join result should be null if a null array is given.", joinResult);
	}

}
