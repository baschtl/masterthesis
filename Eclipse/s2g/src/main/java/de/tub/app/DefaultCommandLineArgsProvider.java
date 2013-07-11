package de.tub.app;

import java.util.Properties;

import com.beust.jcommander.IDefaultProvider;

import de.tub.util.PropertiesUtil;

/**
 * This class provides default values for various 
 * command line arguments from a properties file.
 * 
 * @author Sebastian Oelke
 *
 */
public class DefaultCommandLineArgsProvider implements IDefaultProvider {

	public final static String MAIN_PROP_FILE = "/app.properties";
	private static Properties props;
	
	public DefaultCommandLineArgsProvider() {
		// Initialize properties from file
		props = PropertiesUtil.load(MAIN_PROP_FILE);
	}
	
	@Override
	public String getDefaultValueFor(String optionName) {
		// Get default values for command line arguments from properties file
		
		// ### Data preprocessing
		if (optionName.equals(CommandLineArgs.PREPROCESS_IN_DIR))
			return props.getProperty("app.preprocess.in_dir");
		else if (optionName.equals(CommandLineArgs.PREPROCESS_OUT_FILE))
			return props.getProperty("app.preprocess.out_file");
		else if (optionName.equals(CommandLineArgs.PREPROCESS_PATH_IN_CHILD))
			return props.getProperty("app.preprocess.path_in_child_dir");
		else if (optionName.equals(CommandLineArgs.PREPROCESS_RESOURCE_LINE_OFFSET))
			return props.getProperty("app.preprocess.line_offset");
		
		// ### Stay point detection
		else if (optionName.equals(CommandLineArgs.SPD_MIN_USER_POINTS))
			return props.getProperty("app.staypoints.min_user_points");
		else if (optionName.equals(CommandLineArgs.SPD_MAX_USER_POINTS))
			return props.getProperty("app.staypoints.max_user_points");
		else if (optionName.equals(CommandLineArgs.SPD_DISTANCE_THRESHOLD))
			return props.getProperty("app.staypoints.distance_threshold");
		else if (optionName.equals(CommandLineArgs.SPD_TIME_THRESHOLD))
			return props.getProperty("app.staypoints.time_threshold");
		
		// ### Clustering
		else if (optionName.equals(CommandLineArgs.CLUSTERING_IN))
			return props.getProperty("app.clustering.in_file");
		else if (optionName.equals(CommandLineArgs.CLUSTERING_OUT))
			return props.getProperty("app.clustering.out_dir");
		else if (optionName.equals(CommandLineArgs.CLUSTERING_OPTICS_XI))
			return props.getProperty("app.clustering.optics_xi");
		else if (optionName.equals(CommandLineArgs.CLUSTERING_OPTICS_XI_STEP_SIZE))
			return props.getProperty("app.clustering.optics_xi_steps");
		else if (optionName.equals(CommandLineArgs.CLUSTERING_OPTICS_XI_MAX_VALUE))
			return props.getProperty("app.clustering.optics_xi_max");
		else if (optionName.equals(CommandLineArgs.CLUSTERING_OPTICS_MIN_POINTS))
			return props.getProperty("app.clustering.optics_minpts");
		else if (optionName.equals(CommandLineArgs.CLUSTERING_OPTICS_MIN_POINTS_STEP_SIZE))
			return props.getProperty("app.clustering.optics_minpts_steps");
		else if (optionName.equals(CommandLineArgs.CLUSTERING_OPTICS_MIN_POINTS_MAX_VALUE))
			return props.getProperty("app.clustering.optics_minpts_max");
		
		// ### Building shared framework
		else if (optionName.equals(CommandLineArgs.BUILD_FRAMEWORK_IN))
			return props.getProperty("app.build.shared_framework.in_dir");
		
		// ### Similarity measurement
		else if (optionName.equals(CommandLineArgs.CALC_SIMILARITY_SPLIT_THRESHOLD))
			return props.getProperty("app.similarity.split_threshold");
		else if (optionName.equals(CommandLineArgs.CALC_SIMILARITY_TEMP_CONSTRAINT_THRESHOLD))
			return props.getProperty("app.similarity.temp_constraint_threshold");
		else if (optionName.equals(CommandLineArgs.CALC_SIMILARITY_MIN_SEQUENCE_LENGTH))
			return props.getProperty("app.similarity.min_sequence_length");
		else if (optionName.equals(CommandLineArgs.CALC_SIMILARITY_FROM_LEVEL))
			return props.getProperty("app.similarity.from_level");
		else if (optionName.equals(CommandLineArgs.CALC_SIMILARITY_TO_LEVEL))
			return props.getProperty("app.similarity.to_level");
		
		// ### Evaluation
		else if (optionName.equals(CommandLineArgs.EVALUATION_OUT_DIR))
			return props.getProperty("app.evaluation.out_dir");
		
		else return null;
	}

}
