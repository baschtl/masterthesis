package de.tub.clustering;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lmu.ifi.dbs.elki.application.KDDCLIApplication;

/**
 * This class provides methods to invoke the clustering 
 * of ELKI.
 * 
 * @author Sebastian Oelke
 *
 */
public class ELKIClusterer {
	
	private static final Logger LOG = LoggerFactory.getLogger(ELKIClusterer.class);
	
	public static final double MIN_OPTICS_XI = 0.0;
	public static final double MAX_OPTICS_XI = 1.0;
	public static final int MIN_OPTICS_POINTS = 1;
	
	/**
	 * Starts the clustering of stay points with ELKI using the OPTICS clustering algorithm.
	 * The final clusters are created using OPTICS-XI and the resulting cluster order of the
	 * OPTICS run.
	 * <p />
	 * OPTICS is invoked with the following parameters:
	 * <pre>
	 * -dbc.in {inputFile}
	 * -algorithm clustering.OPTICSXi
 	 * -opticsxi.xi {opticsXi}
 	 * -opticsxi.algorithm OPTICS
	 * -algorithm.distancefunction geo.LatLngDistanceFunction
	 * -optics.epsilon [unset]
	 * -optics.minpts {opticsMinPoints}
 	 * -evaluator AutomaticEvaluation
 	 * -resulthandler ResultWriter
 	 * -out {outputDir}
	 * </pre>
	 * For all other parameters the default values are implied.
	 * <p />
	 * 
	 * @param inputFile the input file holding the data that has to be clustered.
	 * @param outputDir the output directory that holds the resulting cluster files after the OPTICS-XI run.
	 * @param opticsXi the xi value used by OPTICS-XI. This has to be in the interval [0.0, 1.0).
	 * @param opticsMinPoints the minimum points a cluster must have to be created used by OPTICS. Has to be 
	 * greater than zero.
	 * 
	 * @throws NullPointerException if the given parameter for the input file or the output directory is <code>null</code>.
	 * @throws IllegalArgumentException if the given parameter for the given input file or the output directory is empty. 
	 * If the parameters opticsXi or opticsMinPoints are not valid.
	 */
	public static void cluster(String inputFile, String outputDir, double opticsXi, int opticsMinPoints) 
			throws NullPointerException, IllegalArgumentException {
		if (inputFile == null)
			throw new NullPointerException(
				"You provided a null value for the required input file. " +
				"The clustering cannot be performed without any input data.");
		else if (inputFile.isEmpty())
			throw new IllegalArgumentException(
				"You provided an empty string for the required input file. " +
				"The clustering cannot be performed without any input data.");
		
		if (outputDir == null)
			throw new NullPointerException(
				"You provided a null value for the output directory. " +
				"The clustering cannot be performed without a specified output directory.");
		else if (outputDir.isEmpty())
			throw new IllegalArgumentException(
				"You provided an empty string for the output directory. " +
				"The clustering cannot be performed without a specified output directory.");
		
		if (opticsXi < MIN_OPTICS_XI || opticsXi >= 1.0)
			throw new IllegalArgumentException(
					"The parameter 'opticsXi' has to be within the range [0.0, 1.0). " +
					"The clustering cannot be performed without a valid value for the 'opticsXi' parameter.");
		
		if (opticsMinPoints < MIN_OPTICS_POINTS)
			throw new IllegalArgumentException(
					"The parameter 'opticsMinPoints' has to be within the greater than zero. " +
					"The clustering cannot be performed without a valid value for the 'opticsMinPoints' parameter.");
		
		// Build up arguments for ELKI clustering
		List<String> args = new ArrayList<String>();
		// Input file
		args.add("-dbc.in");
		args.add(inputFile);
		// Output directory
		args.add("-out");
		args.add(outputDir);
		// Clustering algorithm
		args.add("-algorithm");
		args.add("clustering.OPTICSXi");
		// Clustering algorithm parameters
		args.add("-opticsxi.xi");
		args.add(String.valueOf(opticsXi));
		args.add("-algorithm.distancefunction");
		args.add("geo.LatLngDistanceFunction");
		args.add("-optics.minpts");
		args.add(String.valueOf(opticsMinPoints));
		// Result evaluator
		args.add("-evaluator");
		args.add("AutomaticEvaluation");
		// Result handler
		args.add("-resulthandler");
		args.add("ResultWriter");
		// Debug output
		if (LOG.isDebugEnabled()) {
			args.add("-verbose");
			args.add("-enableDebug");
			args.add("true");
		}
		
		LOG.debug("Invoke ELKI clustering with: {}.", args);
		
		// Invoke ELKI with the given arguments
		KDDCLIApplication.main(args.toArray(new String[] {}));
	}
}
