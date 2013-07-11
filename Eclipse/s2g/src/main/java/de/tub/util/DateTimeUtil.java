package de.tub.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides convenience methods
 * for handling dates. 
 * 
 * @author Sebastian Oelke
 *
 */
public class DateTimeUtil {

	private static final Logger LOG = LoggerFactory.getLogger(DateTimeUtil.class);
	
	public static final long MILLISECONDS_PER_MINUTE = 60 * 1000L;
	public static final long MILLISECONDS_PER_HOUR = 60 * 60 * 1000L;
	
	public static final String DEFAULT_DATE_FORMAT = "dd-MM-yyyy HH:mm:ss.SSS";
	
	/**
	 * Calculates the difference in time between two dates. If
	 * both dates are valid (i.e., not <code>null</code>) the difference in minutes
	 * is returned.
	 * 
	 * @param date1 the first date
	 * @param date2 the second date
	 * @return the difference in minutes between <code>date1</code> and <code>date2</code>
	 * @throws NullPointerException if one of the given dates or both are invalid, i.e., <code>null</code>
	 */
	public static long differenceInMinutes(Date date1, Date date2) {
		if (date1 == null || date2 == null) 
			throw new NullPointerException("Both dates given as parameters must not be null.");
		
		return (date2.getTime() - date1.getTime()) / (MILLISECONDS_PER_MINUTE);
	}
	
	/**
	 * Calculates the difference in hours between two dates given as
	 * <code>long</code> values.
	 * 
	 * @param date1 the first date
	 * @param date2 the second date
	 * @return the difference in hours between <code>date1</code> and <code>date2</code>
	 */
	public static int differenceInHours(long date1, long date2) {
		return (int)((date2 - date1) / (MILLISECONDS_PER_HOUR));
	}
	
	/**
	 * Checks if the transition time between two consecutive clusters
	 * of two users sequences are similar. 
	 * 
	 * @param c1Arrival the arrival time of the second cluster of the first sequence.
	 * @param c1Leaving the leaving time of the first cluster of the first sequence.
	 * @param c2Arrival the arrival time of the second cluster of the second sequence.
	 * @param c2Leaving the leaving time of the first cluster of the second sequence.
	 * @param temporalConstraintThreshold the upper temporal threshold to use.
	 * @return <code>true</code> if the temporal constraint was redeemed, otherwise <code>false</code>.
	 */
	public static boolean isTemporalConstraintRedeemed(
			long c1Arrival, long c1Leaving,
			long c2Arrival, long c2Leaving,
			double temporalConstraintThreshold) {
		
		// Calculate the difference of both cluster times
		long diffPairOne = c1Arrival - c1Leaving;
		long diffPairTwo = c2Arrival - c2Leaving;
		
		// Calculate the absolute value of the subtract of both differences
		// and the maximal value of the two differences
		double absSubtract = Math.abs(diffPairOne - diffPairTwo);
		double maxDiff = Math.max(diffPairOne, diffPairTwo);
		
		return (absSubtract / maxDiff) <= temporalConstraintThreshold;
	}
	
	/**
	 * Checks if two time intervals (in the form of two long values for each
	 * interval) overlap. This method could also be used for non-time 
	 * related tasks.
	 * 
	 * @param t1Start the start time of the first interval.
	 * @param t1End the end time of the first interval.
	 * @param t2Start the start time of the second interval.
	 * @param t2End the end time of the second interval.
	 * @return <code>true</code> if the given time intervals overlap, <code>false</code> otherwise.
	 */
	public static boolean doTimeIntervalsOverlap(
			long t1Start, long t1End,
			long t2Start, long t2End) {
		
		return ((t1Start <= t2End) && (t1End >= t2Start));
	}
	
	/**
	 * Returns the current date as a formatted string with the given
	 * format. If the given format is not valid (i.e., <code>null</code>
	 * or empty) the default format will be used.
	 * 
	 * @param format the format to use to format the current date.
	 * @return the current date formatted with the given format.
	 * 
	 * @see de.tub.util.DateTimeUtil#DEFAULT_DATE_FORMAT The default date format as defined in DEFAULT_DATE_FORMAT
	 */
	public static String currentDate(String format) {
		// Use default format if given one is not valid
		if (format == null || format.isEmpty()) {
			LOG.debug("The given date format is not valid (i.e., null or empty). The default format '{}' is used.", DEFAULT_DATE_FORMAT);
			format = DEFAULT_DATE_FORMAT;
		}
		
		// Build current date for the file name
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		
		return sdf.format(date);
	}
}
