package de.tub.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides methods to deal with numbers.
 * 
 * @author Sebastian Oelke
 *
 */
public class NumberUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(NumberUtil.class);
	
	public static final String DEFAULT_DECIMAL_FORMAT = "0.##########";
	
	private static DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
	
	static {
		// Change the default decimal and grouping separator 
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
	}
	
	/**
	 * A convenience method which uses <code>decimalFormat(String)</code> with 
	 * the default decimal format.
	 * 
	 * @return a new instance of the <code>DecimalFormat</code> class.
	 * 
	 * @see de.tub.util.NumberUtil#decimalFormat(String)
	 */
	public static DecimalFormat decimalFormat() {
		return decimalFormat(DEFAULT_DECIMAL_FORMAT);
	}
	
	/**
	 * Returns a new instance of the <code>DecimalFormat</code> class
	 * that can be used to format decimal numbers. If the given format 
	 * is not valid (i.e., <code>null</code> or empty) the default format 
	 * will be used.
	 * <p />
	 * All decimal numbers will be formatted using the '.' character as
	 * a decimal separator and the ',' character as a grouping separator.
	 * 
	 * @param format the format to use to create the instance. 
	 * @return a new instance of the <code>DecimalFormat</code> class.
	 * 
	 * @see de.tub.util.NumberUtil#DEFAULT_DECIMAL_FORMAT The default decimal format as defined in DEFAULT_DECIMAL_FORMAT
	 */
	public static DecimalFormat decimalFormat(String format) {
		// Use default format if given one is not valid
		if (format == null || format.isEmpty()) {
			LOG.debug("The given decimal format is not valid (i.e., null or empty). The default format '{}' is used.", DEFAULT_DECIMAL_FORMAT);
			format = DEFAULT_DECIMAL_FORMAT;
		}
		
		return new DecimalFormat(format, symbols);
	}
	
}
