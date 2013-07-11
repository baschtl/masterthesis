package de.tub.app;

import java.io.File;

import com.beust.jcommander.Parameter;

/**
 * Defines command line arguments which are 
 * supported by this application.
 * 
 * @author Sebastian Oelke
 *
 */
public class CommandLineArgs {
	
	//###################################################################
	// Argument name definitions
	//###################################################################
	
	// ### Data preprocessing
	public static final String PREPROCESS_DATA = "-p";
	public static final String PREPROCESS_DATA_LONG = "--preprocess";
	// Arguments for data preprocessing
	public static final String PREPROCESS_IN_DIR = "--preprocess-in-dir";
	public static final String PREPROCESS_OUT_FILE = "--preprocess-out-file";
	public static final String PREPROCESS_PATH_IN_CHILD = "--preprocess-path-in-child-dir";
	public static final String PREPROCESS_RESOURCE_LINE_OFFSET = "--preprocess-line-offset";
	
	// ### Stay Point detection
	public static final String STAY_POINT_DETECTION = "-s";
	public static final String STAY_POINT_DETECTION_LONG = "--stay-point-detection";
	// Arguments for stay point detection
	public static final String SPD_MIN_USER_POINTS = "--min-user-points";
	public static final String SPD_MAX_USER_POINTS = "--max-user-points";
	public static final String SPD_DISTANCE_THRESHOLD = "--distance-threshold";
	public static final String SPD_TIME_THRESHOLD = "--time-threshold";
	
	// ### Clustering
	public static final String CLUSTERING = "-c";
	public static final String CLUSTERING_LONG = "--clustering";
	// Arguments for clustering
	public static final String CLUSTERING_IN = "--clustering-in-file";
	public static final String CLUSTERING_OUT = "--clustering-out-dir";
	public static final String CLUSTERING_OPTICS_XI = "--optics-xi";
	public static final String CLUSTERING_OPTICS_XI_STEP_SIZE = "--optics-xi-step-size";
	public static final String CLUSTERING_OPTICS_XI_MAX_VALUE = "--optics-xi-max";
	public static final String CLUSTERING_OPTICS_MIN_POINTS = "--optics-minpts";
	public static final String CLUSTERING_OPTICS_MIN_POINTS_STEP_SIZE = "--optics-minpts-step-size";
	public static final String CLUSTERING_OPTICS_MIN_POINTS_MAX_VALUE = "--optics-minpts-max";
	
	// ### Build shared framework
	public static final String BUILD_FRAMEWORK = "-bf";
	public static final String BUILD_FRAMEWORK_LONG = "--build-framework";
	// Arguments for building the framework
	public static final String BUILD_FRAMEWORK_IN = "--build-framework-in-dir";
	
	// ### Build hierarchical graphs of each user
	public static final String BUILD_USER_GRAPHS = "-bhg";
	public static final String BUILD_USER_GRAPHS_LONG = "--build-hgs";
	
	// ### Calculate the spatial similarity between users
	public static final String CALC_SIMILARITY = "-cs";
	public static final String CALC_SIMILARITY_LONG = "--calculate-similarity";
	// Arguments for similarity measurement
	public static final String CALC_SIMILARITY_SPLIT_THRESHOLD = "--split-threshold";
	public static final String CALC_SIMILARITY_TEMP_CONSTRAINT_THRESHOLD = "--temp-constraint";
	public static final String CALC_SIMILARITY_MIN_SEQUENCE_LENGTH = "--min-sequence-length";
	public static final String CALC_SIMILARITY_FROM_LEVEL = "--from-level";
	public static final String CALC_SIMILARITY_TO_LEVEL = "--to-level";
	
	// ### Evaluation
	public static final String EVALUATION = "-e";
	public static final String EVALUATION_LONG = "--evaluation";
	// Arguments for evaluation
	public static final String EVALUATION_OUT_DIR = "--eval-out-dir";
	
	// ### Normalization
	public static final String NORMALIZATION= "-n";
	public static final String NORMALIZATION_LONG= "--normalize";
	
	// ### Automation
	public static final String AUTOMATION = "-a";
	public static final String AUTOMATION_LONG = "--automation";
	
	// ### Help
	public static final String HELP = "-h";
	public static final String HELP_LONG = "--help";
	
	//###################################################################
	// Data preprocessing
	//###################################################################
	
	@Parameter(	names = { PREPROCESS_DATA, PREPROCESS_DATA_LONG }, 
				description = "Starts the preprocessing of data. All data sources are defined in the properties file 'app.properties'. Change them to fit your needs.")
	public boolean preprocess = false;
	
	@Parameter(	names = PREPROCESS_IN_DIR, 
				description = "The absolute path to the directory used as data source for preprocessing.")
	public String preprocessInDir;
	
	@Parameter(	names = PREPROCESS_OUT_FILE, 
				description = "The absolute path to the file which is created after preprocessing. This file holds preprocessed data.")
	public String preprocessOutFile = System.getProperty("user.home") + File.separator + "out.txt";
	
	@Parameter(	names = PREPROCESS_PATH_IN_CHILD, 
				description = "During the preprocessing step each child directory of the root directory defined by '" + PREPROCESS_IN_DIR + "' is read. With this parameter you can specify a directory within the child directory in which the data lies (e.g., <root>/<child>/<path_in_child>).")
	public String preprocessPathInChildDir;
	
	@Parameter( names = PREPROCESS_RESOURCE_LINE_OFFSET,
				description = "If the resources to read (i.e., text files) includes lines that are not relevant they can be skipped. The number of lines to skip at the beginning of each resource can be defined with this parameter.")
	public int preprocessLineOffset;
	
	//###################################################################
	// Stay point detection
	//###################################################################
	
	@Parameter(	names = { STAY_POINT_DETECTION, STAY_POINT_DETECTION_LONG }, 
				description = "Starts the detection of stay points. This operation needs a valid database connection as well as user and geo point resources.")
	public boolean spDetection = false;
	
	@Parameter(	names = SPD_MIN_USER_POINTS, 
				description = "Staypoint detection: Defines how many geo points a user must have to be considered during stay point detection.")
	public int spdMinUserPoints;
	
	@Parameter(	names = SPD_MAX_USER_POINTS, 
				description = "Staypoint detection: Defines how many geo points a user can have at most to be considered during stay point detection. A value of -1 disables this upper bound.")
	public int spdMaxUserPoints = -1;
	
	@Parameter(	names = SPD_DISTANCE_THRESHOLD, 
				description = "Staypoint detection: Defines the maximum distance (in meters) two points can be apart and still be detected as a stay point.")
	public int spdDistanceThreshold = 200;
	
	@Parameter(	names = SPD_TIME_THRESHOLD, 
				description = "Staypoint detection: Defines the amount of time (in minutes) a user has to stay within a certain area to be detected as a stay point.")
	public int spdTimeThreshold = 30;
	
	//###################################################################
	// Clustering
	//###################################################################
	
	@Parameter(	names = { CLUSTERING, CLUSTERING_LONG }, 
				description = "Starts the clustering of stay points.")
	public boolean clustering = false;
	
	@Parameter(	names = CLUSTERING_IN, 
				description = "Clustering: The absolute path to the file containing stay points used as data source for clustering.")
	public String clusteringInFile;
	
	@Parameter(	names = CLUSTERING_OUT, 
				description = "Clustering: The absolute path to the directory that holds the clustering results.")
	public String clusteringOutDir = System.getProperty("user.home") + File.separator + "clustering";
	
	@Parameter(	names = CLUSTERING_OPTICS_XI, 
			description = "Clustering: The xi value used for the extraction of clusters by OPTICS-XI.")
	public double clusteringOpticsXi = 0.2;
	
	@Parameter(	names = CLUSTERING_OPTICS_XI_STEP_SIZE, 
			description = "Clustering: Defines the step size in that the xi value should be increased in consecutive clustering passes. A value of -1 disables the increase.")
	public double clusteringOpticsXiStepSize = -1.0;
	
	@Parameter(	names = CLUSTERING_OPTICS_XI_MAX_VALUE, 
			description = "Clustering: Defines the maximal value that the xi value should be increased to in consecutive clustering passes. A value of 1.0 is the upper bound.")
	public double clusteringOpticsXiMaxValue = 1.0;
	
	@Parameter(	names = CLUSTERING_OPTICS_MIN_POINTS, 
			description = "Clustering: The minimial number of points a cluster has to have to be created.")
	public int clusteringOpticsMinPts = 20;
	
	@Parameter(	names = CLUSTERING_OPTICS_MIN_POINTS_STEP_SIZE, 
			description = "Clustering: Defines the step size in that the value for the minimal number of points for a cluster should be increased in consecutive clustering passes. A value of -1 disables the increase.")
	public int clusteringOpticsMinPtsStepSize = -1;
	
	@Parameter(	names = CLUSTERING_OPTICS_MIN_POINTS_MAX_VALUE, 
			description = "Clustering: Defines the maximal value that the value for the minimal number of points for a cluster should be increased to in consecutive clustering passes.")
	public int clusteringOpticsMinPtsMaxValue = 100;
	
	//###################################################################
	// Build shared framework
	//###################################################################
	
	@Parameter(	names = { BUILD_FRAMEWORK, BUILD_FRAMEWORK_LONG }, 
				description = "Starts the building of the shared framework based on stay points.")
	public boolean buildFramework = false;
	
	@Parameter(	names = BUILD_FRAMEWORK_IN, 
				description = "The absolute path to the directory that holds the clustering results that are used to build the shared framework.")
	public String buildFrameworkInDir;
	
	//###################################################################
	// Build hierarchical graphs
	//###################################################################
	
	@Parameter(	names = { BUILD_USER_GRAPHS, BUILD_USER_GRAPHS_LONG }, 
				description = "Starts the building of the hierarchical graph for each user based on the user's stay points and the shared framework.")
	public boolean buildUserGraphs = false;
	
	//###################################################################
	// Calculate similarity
	//###################################################################
	
	@Parameter(	names = { CALC_SIMILARITY, CALC_SIMILARITY_LONG }, 
			description = "Starts the calculation of spatial similarity between users. Without using the evaluate option the similarity values are written to the graph database.")
	public boolean calcSimilarity = false;
	
	@Parameter(	names = { CALC_SIMILARITY_SPLIT_THRESHOLD }, 
			description = "Similarity measurement: The split threshold used by the sequence matcher in hours. A sequence is split if the transition time between two consecutive clusters of the sequence exceeds this value.")
	public int calcSimilaritySplitThreshold;
	
	@Parameter(	names = { CALC_SIMILARITY_TEMP_CONSTRAINT_THRESHOLD }, 
			description = "Similarity measurement: The temporal constraint threshold used by the sequence matcher as a floating point value. This threshold is used to ensure that the sequences of two users have similar transition times between consecutive clusters.")
	public double calcSimilarityTempConstraintThreshold;
	
	@Parameter(	names = { CALC_SIMILARITY_MIN_SEQUENCE_LENGTH }, 
			description = "Similarity measurement: The minimum length a similar sequence has to have to be recognized. This defaults to a length of one.")
	public int calcSimilarityMinSequenceLength = 1;
	
	@Parameter(	names = { CALC_SIMILARITY_FROM_LEVEL }, 
			description = "Similarity measurement: The level of the hierarchical graph of each user from which the similarity measurement starts. In other words, all levels equal or greater than the from level are included in the similarity measurement. This is ignored if the given value is smaller one, i.e., the measurement starts from the beginning of each graph.")
	public int calcSimilarityFromLevel = -1;
	
	@Parameter(	names = { CALC_SIMILARITY_TO_LEVEL }, 
			description = "Similarity measurement: The level of the hierarchical graph of each user at which the similarity measurement stops. In other words, all levels equal or less than the to level are included in the similarity measurement. This is ignored if the given value is smaller than zero, i.e., the measurement includes all levels of each graph.")
	public int calcSimilarityToLevel = -1;
	
	//###################################################################
	// Evaluation
	//###################################################################
	
	@Parameter(	names = { EVALUATION, EVALUATION_LONG }, 
			description = "Enables the final evaluation. This produces a CSV file for each similarity measurement pass that holds the similarities for all users. As the similarity between two users is valid in both directions of the relationship only one similarity value is written, all others are zero. The evaluation can only be performed in connection with the similarity measurement.")
	public boolean evaluation = false;
	
	@Parameter(	names = { EVALUATION_OUT_DIR }, 
			description = "Evaluation: The absolute path to the output directory for the results of the evaluation.")
	public String evaluationOutDir = System.getProperty("user.home") + File.separator + "evaluation";

	//###################################################################
	// Normalization
	//###################################################################
	
	@Parameter(	names = { NORMALIZATION, NORMALIZATION_LONG }, 
			description = "Enables the normalization of the results of the similarity measurement. This normalizes all similarity scores to values from zero to one. This has only an effect during the similarity measurement and automation tasks.")
	public boolean normalization = false;
	
	//###################################################################
	// Automation
	//###################################################################
	
	@Parameter(	names = { AUTOMATION, AUTOMATION_LONG }, 
			description = "Enables automation of different tasks provided by this application. In detail, the following tasks are run in a row: clustering of stay points, building of the shared framework, building of a hierarchical graph for each user, similarity measurement. The automation task can only be run in connection with the evaluation. Hence, after a complete run an evaluation file is written. Then, all created resources (i.e., the clustering files, the complete graph with the shared framework and the hierarchical graphs) are removed. This ensures that each automation run starts with reseted resources. Note that the automation requires an empty graph database as it is building it from scratch in each pass.")
	public boolean automation = false;
	
	//###################################################################
	// Other
	//###################################################################
	
	@Parameter(	names = { HELP, HELP_LONG }, 
				description = "Prints this explanatory text.",
				help = true )
	public boolean help = true;
}
