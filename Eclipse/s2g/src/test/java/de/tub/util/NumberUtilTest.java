package de.tub.util;

import static org.junit.Assert.*;

import java.text.DecimalFormat;

import org.junit.Test;

public class NumberUtilTest {

	public static final double NUMBER = 0.123456E-2;
	public static final String DEFAULT_RESULT = "0.00123456";
	public static final String CUSTOM_RESULT = "0.001";
	
	public static final String FORMAT = "0.###";
	
	@Test
	public void testDecimalFormat() {
		DecimalFormat defaultDf = NumberUtil.decimalFormat();
		assertTrue("The decimal number was not formatted correctly with the default formatter.", defaultDf.format(NUMBER).equals(DEFAULT_RESULT));
		
		DecimalFormat customDf = NumberUtil.decimalFormat(FORMAT);
		assertTrue("The decimal number was not formatted correctly with the custom formatter.", customDf.format(NUMBER).equals(CUSTOM_RESULT));
	}

}
