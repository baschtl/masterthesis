package de.tub.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.IDefaultProvider;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import de.tub.clustering.ELKIClusterer;
import de.tub.data.dao.DAOFactory;
import de.tub.data.dao.Neo4JUserDAO;
import de.tub.evaluation.SimilarityEvaluation;
import de.tub.evaluation.SimilarityEvaluator;
import de.tub.normalization.SimilarityNormalizer;
import de.tub.observer.Interests;
import de.tub.processor.graph.HierarchicalGraphProcessor;
import de.tub.processor.graph.SharedFrameworkProcessor;
import de.tub.processor.preprocessing.GpsLogLineProcessor;
import de.tub.processor.staypoint.GeoStayPointProcessor;
import de.tub.reader.ReaderFactory;
import de.tub.reader.directory.IterativeDirectoryReader;
import de.tub.reader.file.IterativeFileReader;
import de.tub.reader.file.TextFileLineReader;
import de.tub.reader.model.UserReader;
import de.tub.similarity.Sequence;
import de.tub.similarity.SequenceWrapper;
import de.tub.similarity.SimilarSequenceCluster;
import de.tub.similarity.analysis.Neo4JSimilarityAnalyzer;
import de.tub.similarity.extraction.Neo4JSequenceExtractor;
import de.tub.similarity.matching.Neo4JSequenceMatcher;
import de.tub.util.DBUtil;
import de.tub.util.FileUtil;
import de.tub.util.NumberUtil;
import de.tub.writer.ArrayToCsvWriter;

/**
 * This class is the entry point for different tasks
 * this program can process.
 * 
 * @author Sebastian Oelke
 *
 */
public class Application {
	public final static String MAIN_PROP_FILE = "/app.properties";
	
	private static final Logger LOG = LoggerFactory.getLogger(Application.class);
	
	private static CommandLineArgs clArgs;
	
	// Current values for the clustering task
	private static int currentOpticsMinPoints;
	private static double currentOpticsXi;
	
	// Decimal number formatter
	private static DecimalFormat df = NumberUtil.decimalFormat();
	
    public static void main( String[] args ) {
    	// Prepare processing of CL arguments
    	clArgs = new CommandLineArgs();
    	JCommander jc = new JCommander(clArgs);
    	final IDefaultProvider DEFAULT_PROVIDER = new DefaultCommandLineArgsProvider();
    	
    	// Set provider for default values of command line arguments 
    	jc.setDefaultProvider(DEFAULT_PROVIDER);
    	jc.setColumnSize(100);
    	
    	try {
    		// Try to parse the arguments
    		jc.parse(args);
    	} catch (ParameterException e) {
    		// Print error message, jc.usage() is called automatically
    		LOG.error("{}. Refer to the available options and their explanation below.", e.getMessage());
    	}
    	
    	if (clArgs.preprocess) {
    		// Go into the preprocessing phase
    		preprocess();
    	}
    	else if (clArgs.spDetection) {
    		// Go into the detection of stay points
    		detectStayPoints();
    	}
    	else if (clArgs.clustering) {
    		// Go into the clustering task
    		clustering();
    	}
    	else if (clArgs.buildFramework) {
    		// Create shared framework based on clustering results
    		buildFramework();
    	}
    	else if (clArgs.buildUserGraphs) {
    		// Create hierarchical graph for each user
    		buildHierarchicalGraphs();
    	}
    	else if (clArgs.calcSimilarity) {
    		// Calculate spatial similarity between users
    		calculateSimilarity();
    	}
    	else if (clArgs.evaluation && !clArgs.automation) {
    		// Evaluation only works with a run of similarity measurement beforehand
    		LOG.info("The evaluation can only be performed in connection with the similarity measurement. Run this program with the command line switch {} or {} to get more information on how to run the similarity measurement.",
    				CommandLineArgs.HELP, CommandLineArgs.HELP_LONG);
    		return;
    	}
    	else if (clArgs.automation) {
    		if (clArgs.evaluation) {
    			// Start the automation
    			automate();
    		} else {
    			// The automation task needs the evaluation switch
        		LOG.info("The automation requires the evaluation. The evaluation can be enabled with the {} or {} command line switch. Run this program with the command line switch {} or {} to get more information on how to run the automation.",
        				new Object[] { CommandLineArgs.EVALUATION, CommandLineArgs.EVALUATION_LONG, CommandLineArgs.HELP, CommandLineArgs.HELP_LONG });
        		
        		// Ask the user if the evaluation should be enabled and the automation should run
        	    System.out.print("Do you want the system to enable the evaluation (y/n)? ");

        	    // Open standard input stream
        	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        		
        		String runAutomationWithEvaluation = null;
        		try {
        			runAutomationWithEvaluation = br.readLine();
        		} catch (IOException ioe) {
        			LOG.error("An error occurred while reading your input: {}", ioe);
        		}
        		
        		// User decided to start automation with evaluation
        		if (runAutomationWithEvaluation.equals("y") || runAutomationWithEvaluation.equals("Y")) {
        			LOG.debug("The evaluation is enabled by the system. Starting automation task.");
        			automate();
        		}
        		// User decided to end the program
        		else {
        			LOG.debug("Automation aborted by the user.");
        			return;
        		}
    		}
    	}
    	else {
    		// Print a help text
    		jc.usage();
    	}
    }
    
    /**
     * Starts the automation which processes the following tasks: clustering of stay points, 
     * building of the shared framework, building of a hierarchical graph for each user, 
     * similarity measurement. After each run a clean up of useless resources is done.
     */
    private static void automate() {
    	LOG.info("Begin automation task.");
    	
    	currentOpticsXi = clArgs.clusteringOpticsXi;
    	double opticsXiMax = (clArgs.clusteringOpticsXiMaxValue > ELKIClusterer.MAX_OPTICS_XI ? ELKIClusterer.MAX_OPTICS_XI : clArgs.clusteringOpticsXiMaxValue);
    	currentOpticsMinPoints = clArgs.clusteringOpticsMinPts;
    	int opticsMinPointsMax = clArgs.clusteringOpticsMinPtsMaxValue;
    	
    	LOG.info("optics-xi-start: {}, optics-xi-min-points-start: {}, optics-xi-step-size: {}, optics-min-points-step-size: {}",
    			new Object[] { 
    				df.format(currentOpticsXi), df.format(currentOpticsMinPoints), 
    				df.format(clArgs.clusteringOpticsXiStepSize), df.format(clArgs.clusteringOpticsMinPtsStepSize)
    			});
    	
    	while (currentOpticsXi <= opticsXiMax) {
    		while (currentOpticsMinPoints <= opticsMinPointsMax) {
    			// Run automation for each combination of opticsXi and opticsMinPoints
    			LOG.info("Run automation with optics-xi: {}, optics-xi-min-points: {}", 
    					df.format(currentOpticsXi), df.format(currentOpticsMinPoints));
    			
    			try {
	    			// Step 1: Clustering
	    			clustering(clArgs.clusteringInFile, clArgs.clusteringOutDir, currentOpticsXi, currentOpticsMinPoints);
	    			
	    			// Step 2: Build shared framework
	    			buildFramework();
	    			
	    			// Step 3: Build user hierarchical graphs
	    			buildHierarchicalGraphs();
	    			
	    			// Step 4: Calculate similarity between all users and create evaluation results afterwards
	    			calculateSimilarity();
    			} catch (Exception e) {
					LOG.error("An error occurred during the automation task. Jumping to the next pass.", e);
				} finally {
	    			// Step 5: Clean up for the next run
	    			cleanAutomationResults();
	    			
	    			// Increase the currentOpticsMinPoints for the next run if desired
	    			if (clArgs.clusteringOpticsMinPtsStepSize > 0)
	    				currentOpticsMinPoints += clArgs.clusteringOpticsMinPtsStepSize;
	    			else
	    				break;
				}
    		}
    		// Reset current values
    		currentOpticsMinPoints = clArgs.clusteringOpticsMinPts;
    		
    		// Increase currentOpticsXi for the next run if desired
    		if (clArgs.clusteringOpticsXiStepSize > 0)
				currentOpticsXi += clArgs.clusteringOpticsXiStepSize;
			else
				break;
    	}
    	
    	LOG.info("End automation task.");
    }
    
    /**
     * Deletes the results of the clustering task and the graph database
     * which holds the shared framework and the hierarchical graphs.
     */
    private static void cleanAutomationResults() {
    	LOG.info("Deleting unneccessary automation results.");
    	
    	// Stop the graph database because it gets deleted
    	DBUtil.closeGraph();
    	
    	// Remove clustering results
    	FileUtil.deleteFileOrDirectory(clArgs.clusteringOutDir);
    	
    	// Remove graph database
    	FileUtil.deleteFileOrDirectory(DBUtil.getNeo4jPath());
    }
    
    /**
     * Preprocesses the data given in the files defined in the 
     * MAIN_PROP_FILE or via command line arguments.
     */
    private static void preprocess() {
    	LOG.info("Begin preprocessing of data.");
    	
    	LOG.debug("Create a IterativeDirectoryReader.");
    	// Create a reader for directories
    	IterativeDirectoryReader iterativeDirectoryReader = (IterativeDirectoryReader) ReaderFactory.instance().getIterativeDirectoryReader();
    	iterativeDirectoryReader.setDirectoryName(clArgs.preprocessInDir);
    	iterativeDirectoryReader.setPathInChildDirectory(clArgs.preprocessPathInChildDir);
    	
    	LOG.debug("Create a IterativeFileReader.");
    	// Create a reader for text files and set an appropriate file filter
    	IterativeFileReader iterativeFileReader = (IterativeFileReader) ReaderFactory.instance().getIterativeFileReader();
    	iterativeFileReader.setFileFilter(FileUtil.acceptVisibleFilesFilter(false, true));
    	
    	LOG.debug("Create a TextFileLineReader.");
    	// Create a reader for text files, the first six lines are not relevant in this data set
    	TextFileLineReader textFileLineReader = (TextFileLineReader) ReaderFactory.instance().getTextFileLineReader();
    	textFileLineReader.setOffset(clArgs.preprocessLineOffset);
    	
    	LOG.debug("Create a GpsLogLineProcessor.");
    	// Create a processor for user points
    	GpsLogLineProcessor processor = new GpsLogLineProcessor(clArgs.preprocessOutFile);
    	
    	// Build reader chain
    	iterativeDirectoryReader.setReader(iterativeFileReader);
    	iterativeFileReader.setReader(textFileLineReader);
    	textFileLineReader.setProcessor(processor);
    	
    	iterativeDirectoryReader.attach(processor, 
    				new Interests[] {
    					Interests.NewChildDirectory,
    					Interests.HasFinished
    				});
    	
    	LOG.debug("Read GPS logs. Save the data in a new file {}.", clArgs.preprocessOutFile);
    	// Read and save the data
    	iterativeDirectoryReader.read();

    	LOG.info("Finished preprocessing of data.");
    }
    
    /**
     * Detect stay points with the raw location data given in the database.
     */
    private static void detectStayPoints() {
    	LOG.info("Begin detection of stay points.");
    	
    	DBUtil.open();
    	
    	LOG.debug("Create a UserReader.");
    	// Create a reader for users
    	UserReader userReader = (UserReader) ReaderFactory.instance().getUserReader();
    	
    	LOG.info("Create a GeoStayPointProcessor with distance threshold {} and time threshold {}.", clArgs.spdDistanceThreshold, clArgs.spdTimeThreshold);
    	// Create a processor for stay points
    	GeoStayPointProcessor processor = new GeoStayPointProcessor(clArgs.spdDistanceThreshold, clArgs.spdTimeThreshold, false);
    	
    	LOG.debug("Initialize the UserReader with the GeoStayPointProcessor.");
    	// Initialize the reader
    	userReader.setProcessor(processor);
    	
    	LOG.debug("Read users out of the database with a minimum of {} and a maximum of {} points.", userReader.getMinUserPoints(), userReader.getMaxUserPoints());
    	// Read and save the data
    	userReader.read();
    	
    	LOG.info("Finished detection of stay points.");
    }
    
    /**
     * Starts the clustering of stay points with ELKI with the given command line 
     * arguments.
     */
    private static void clustering() {
    	clustering(clArgs.clusteringInFile, clArgs.clusteringOutDir,
    			clArgs.clusteringOpticsXi, clArgs.clusteringOpticsMinPts);
    }
    
    /**
     * Starts the clustering of stay points with ELKI with the given
     * parameters.
     */
    private static void clustering(String inputFile, String outputDir, double opticsXi, int opticsMinPoints) {
    	LOG.info("Begin clustering of stay points.");
    	
    	ELKIClusterer.cluster(inputFile, outputDir,	opticsXi, opticsMinPoints);
    	
    	LOG.info("Finished clustering of stay points.");
    }
    
    /**
     * Reads the clustering results of ELKI, builds the shared framework
     * and persists it in a graph database.
     */
    private static void buildFramework() {
    	LOG.info("Begin building of shared framework.");
    	
    	LOG.debug("Create a IterativeFileReader.");
    	IterativeFileReader iterativeFileReader = (IterativeFileReader) ReaderFactory.instance().getIterativeFileReader();
    	LOG.debug("Create a TextFileLineReader.");
    	TextFileLineReader textFileLineReader = (TextFileLineReader) ReaderFactory.instance().getTextFileLineReader();
    	
    	// Setup IterativeFileReader
    	File dir = new File(clArgs.buildFrameworkInDir);
    	iterativeFileReader.setFile(dir);
    	iterativeFileReader.setFileFilter(FileUtil.acceptOnlyClusterFilesFilter());
    	iterativeFileReader.setReader(textFileLineReader);
    	
    	// Setup processor
    	LOG.debug("Create a SharedFrameworkProcessor.");
    	SharedFrameworkProcessor sharedFrameworkProcessor = new SharedFrameworkProcessor();
    	textFileLineReader.setProcessor(sharedFrameworkProcessor);
    	// Get informed about the finishing of each cluster (file) to reset the processor's status
    	textFileLineReader.attach(sharedFrameworkProcessor, Interests.HasFinished);
    	// Get informed about the finishing of all clusters to give each cluster in the generated graph a pretty id
    	iterativeFileReader.attach(sharedFrameworkProcessor, Interests.HasFinished);
    	
    	iterativeFileReader.read();
    	
    	LOG.info("Finished building shared framework.");
    }
    
    /**
     * Based on the stay points of each user and the shared framework this method
     * builds a hierarchical graph for each user.
     */
    private static void buildHierarchicalGraphs() {
    	LOG.info("Begin building of hierarchical graph for each user.");
    	
    	DBUtil.open();
    	
    	// Create a reader for users
    	LOG.debug("Create a UserReader.");
    	UserReader userReader = (UserReader) ReaderFactory.instance().getUserReader();

		// Create the processor to test
    	LOG.debug("Create HierarchicalGraphProcessor.");
		HierarchicalGraphProcessor hgProc = new HierarchicalGraphProcessor();
		
		// Setup the reader
		userReader.setProcessor(hgProc);
		userReader.attach(hgProc, Interests.UserFinished);
		
		// Read the user resources to process
		LOG.debug("Read users out of the database with a minimum of {} and a maximum of {} points.", userReader.getMinUserPoints(), userReader.getMaxUserPoints());
		userReader.read();
    	
    	LOG.info("Finished building of hierarchical graphs.");
    }
    
    /**
     * Calculates the spatial similarity between users based on their hierarchical graphs.
     */
    private static void calculateSimilarity() {
    	LOG.info("Begin calculating spatial similarity between users.");
    	
		// Get a list of all users
    	Neo4JUserDAO uDao = (Neo4JUserDAO) DAOFactory.instance().getUserDAO();
		List<Node> users = uDao.findAll();
		
		int usersCount = users.size();
    	LOG.debug("Found {} users.", usersCount);
		
		// Instantiate all needed classes for similarity measurement
		Neo4JSequenceExtractor ex = new Neo4JSequenceExtractor();
		ex.setFromLevel(clArgs.calcSimilarityFromLevel);
		ex.setToLevel(clArgs.calcSimilarityToLevel);
		
		Neo4JSequenceMatcher matcher = new Neo4JSequenceMatcher(
				clArgs.calcSimilaritySplitThreshold, 
				clArgs.calcSimilarityMinSequenceLength,
				clArgs.calcSimilarityTempConstraintThreshold);
		Neo4JSimilarityAnalyzer analyzer = new Neo4JSimilarityAnalyzer();
		
		// Create matrix that holds similarity results
		double[][] similarityResults = new double[usersCount][usersCount];
		
		// Go through all pair-wise user combinations
    	for (int i = 0; i < usersCount; i++) {
			Node userOne = users.get(i);
			Object userOneId = uDao.getUserId(userOne);
			
			// Set current first user for sequence extraction
			ex.setUserNodeOne(userOne);
			
			for (int j = i + 1; j < usersCount; j++) {
				Node userTwo = users.get(j);
				Object userTwoId = uDao.getUserId(userTwo);
				LOG.info("Calculate similarity between user [{}] and [{}].", userOneId, userTwoId);
				
				double similarity = 0.0;
				
				// Step 1: Extract cluster sequences of two users based on their hierarchical graphs
				// Set current second user for sequence extraction
				ex.setUserNodeTwo(userTwo);
				Map<Integer, SequenceWrapper> clusterSequences = null;
				
				try {
					// Start extraction of cluster sequences
					LOG.info("Step 1: Extraction of cluster sequences from level {} to level {}.", clArgs.calcSimilarityFromLevel, clArgs.calcSimilarityToLevel);
					clusterSequences = ex.extract();
				} catch (Exception e) {
					LOG.error("An error occurred while extracting the sequences of common clusters of user [{}] and [{}]:\n{}", 
							new Object[] {userOneId, userTwoId, e});
					LOG.debug("Similarity measurement stopped for users [{}] and [{}].", userOneId, userTwoId);
				}
				
				// Step 2: Match the extracted cluster sequences to find maximal length similar sequences
				Map<Integer, List<Sequence<SimilarSequenceCluster>>> maxLengthSimilarSequences = null;
				
				// Matching is only possible if there is a valid result of step 1 
				if (clusterSequences != null && !clusterSequences.isEmpty()) {
					matcher.setSequencesOnLevel(clusterSequences);
					
					try {
						// Start matching of cluster sequences
						LOG.info("Step 2: Matching of cluster sequences.");
						maxLengthSimilarSequences = matcher.match();
					} catch (Exception e) {
						LOG.error("An error occurred while matching the sequences of common clusters of user [{}] and [{}]:\n{}", 
								new Object[] {userOneId, userTwoId, e});
						LOG.debug("Similarity measurement stopped for users [{}] and [{}].", userOneId, userTwoId);
					}
				
					// Step 3: Compute spatial similarity between the current two users
					// Similarity measurement is only possible with a valid result of step 2
					if (maxLengthSimilarSequences != null && !maxLengthSimilarSequences.isEmpty()) {
						analyzer.setUserNodeOne(userOne);
						analyzer.setUserNodeTwo(userTwo);
						analyzer.setMaximalLengthSimilarSequencesOnLevel(maxLengthSimilarSequences);
						
						try {
							// Start similarity measurement
							LOG.info("Step 3: Similarity measurement.");
							similarity = analyzer.analyze();
							LOG.debug("Final similarity score: {}", similarity);
							
							// Save similarity value in result matrix
							similarityResults[i][j] = similarity;
						} catch (Exception e) {
							LOG.error("An error occurred while measuring similarity between user [{}] and [{}]:\n{}", 
									new Object[] {userOneId, userTwoId, e});
							LOG.debug("Similarity measurement stopped for users [{}] and [{}].", userOneId, userTwoId);
						}
						
					}	// END: step 3
				}	// END: step 2
			}	// END: user two loop
		}	// END: user one loop
    	
    	LOG.info("Finished calculating spatial similarity.");
    	
    	// Normalize the similarity scores from 0 to 1
    	if (clArgs.normalization) {
	    	LOG.info("Normalizing similarity scores.");
	    	similarityResults = SimilarityNormalizer.normalize(similarityResults);
    	}
    	
    	// The evaluation is requested
    	if (clArgs.evaluation) {
    		LOG.info("Evaluation is enabled.");
    		
    		// Calculate simple evaluation
    		LOG.info("Calculate similarity evaluation values.");
    		SimilarityEvaluation evaluation = SimilarityEvaluator.evaluate(similarityResults);
    		
    		// Build up other information to include in the evaluation files
    		List<String> otherInformation = new ArrayList<String>();
    		if (clArgs.automation) {
    			// Clustering information is only included if the automation task is running
    			otherInformation.add(CommandLineArgs.CLUSTERING_OPTICS_XI);
    			otherInformation.add(String.valueOf(df.format(currentOpticsXi)));
    			otherInformation.add(CommandLineArgs.CLUSTERING_OPTICS_MIN_POINTS);
    			otherInformation.add(String.valueOf(df.format(currentOpticsMinPoints)));
    		}
    		
    		// Add simple metrics from similarity measurement
    		otherInformation.add(SimilarityEvaluation.SIMILARITY_MIN);
    		otherInformation.add(String.valueOf(df.format(evaluation.getMin())));
    		otherInformation.add(SimilarityEvaluation.SIMILARITY_MAX);
    		otherInformation.add(String.valueOf(df.format(evaluation.getMax())));
    		otherInformation.add(SimilarityEvaluation.SIMILARITY_MEAN);
    		otherInformation.add(String.valueOf(df.format(evaluation.getSimilarityMean())));
    		otherInformation.add(SimilarityEvaluation.SIMILAR_USER_PAIRS);
    		otherInformation.add(String.valueOf(evaluation.getSimilarUserPairs()));
    		
    		// Write similarity results in a file
    		LOG.info("Writing evaluation data to a file.");
    		ArrayToCsvWriter.writeDoubles(similarityResults, clArgs.evaluationOutDir, otherInformation.toArray(new String[0]));
    	}
    	// Evaluation is not requested, write the similarity scores into the graph database
    	else {
    		LOG.info("Write similarity scores to the graph database.");
    		
    		for (int i = 0; i < similarityResults.length; i++) {
    			// Get first user by id
    			Node userOne = uDao.findUserById(i);
    			for (int j = i + 1; j < similarityResults.length; j++) {
    				double similarityScore = similarityResults[i][j];
    				// User pairs that are not similar (i.e., have a similarity score of zero) do not get a connection
    				if (similarityScore > 0) {
	    				// Get second user by id
	    				Node userTwo = uDao.findUserById(j);
	    				
	    				// Add similarity relationship
	    				uDao.connectSimilarUsers(userOne, userTwo, similarityScore);
    				}
    			}
    		}
    	}
    }
}
