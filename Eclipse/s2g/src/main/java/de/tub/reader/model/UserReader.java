package de.tub.reader.model;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.data.model.User;
import de.tub.observer.Interests;
import de.tub.observer.Subject;
import de.tub.processor.IProcessor;
import de.tub.processor.staypoint.GeoStayPointProcessor;

/**
 * This reader reads user resources from a
 * database and delegates the processing of 
 * each user to the given processor. A valid 
 * connection to the database is necessary.
 * <p />
 * This reader informs its observers after 
 * finishing each user resource with the
 * <code>UserFinished</code> interest.
 * 
 * @author Sebastian Oelke
 *
 */
public class UserReader extends Subject implements IUserReader, Cloneable {

	private static final Logger LOG = LoggerFactory.getLogger(UserReader.class);
	
	private IProcessor<User> processor;
	
	// All users should be requested if not explicitly specified
	private int minUserPoints, maxUserPoints = Integer.MAX_VALUE;
	private int simulationData;
	
	/**
	 * Standard constructor.
	 */
	public UserReader() {}
	
	/**
	 * Copy constructor for use with the Prototype design pattern.
	 * If you use <code>clone()</code> this constructor has to be used
	 * and all instance variables have to be copied.
	 *  
	 * @param userReader The <code>UserReader</code> which has to be copied.
	 * @see de.tub.reader.model.UserReader#clone()
	 */
	public UserReader(final UserReader userReader) {
		this.minUserPoints = userReader.minUserPoints;
		this.maxUserPoints = userReader.maxUserPoints;
		this.simulationData = userReader.simulationData;
	}
	
	@Override
	public void read() {
		// No processor specified
		if (processor == null) {
			LOG.error("You have to specify a Processor (e.g., StayPointProcessor) for this Reader to work. A Reader is only " +
					"responsible for accessing a data resource. The processing is done by a Processor.");
			return;
		}

//		-- Version 1 of reading users out of database
//		Leads to an overhead of about 10 sec./1000 users because for 
//		each user a new database select query is generated
		
//		ModelListener<User> modelListener = new ModelListener<User>() {
//            public void onModel(User user) {
//            	userCount++;
//            	// Give the user instance to the processor
//                processor.newData(user);
//                simulationData += processor.getStayPointsOfUser();
//                if (userCount % 1000 == 0)
//                	LOG.info("Processed {} users.", userCount);
//            }
//        };
//		// Start reading of users from database
//		User.findWith(modelListener, "posts_in_data_count BETWEEN ? AND ?", minUserPosts, max);
//
//      --
		
        // If the upper bound was set to -1 it is disabled
        int max = maxUserPoints;
        if (max == -1) max = Integer.MAX_VALUE;
		
		// Start reading users from database
        // This generates a query which gathers all users at once
        List<User> users = User.where("points_in_data_count BETWEEN ? AND ?", minUserPoints, max);
        int size = users.size();
        
        // Check if processor is in simulation mode and cast it appropriately
        boolean simulationMode = false;
        GeoStayPointProcessor geoProcessor = null;
        if (processor instanceof GeoStayPointProcessor) {
        	geoProcessor = (GeoStayPointProcessor) processor; 
        	if (geoProcessor.isSimulation())
        		simulationMode = true;
        }
        
        for (int i = 0; i < size; i++) {
        	User user = users.get(i);
        	// Give the user instance to the processor
            processor.newData(user);
            
            // Notify all observers that the reader finished reading this user resource
         	notifyObservers(Interests.UserFinished, user.getId());
            
            // Collect simulation data
            if (simulationMode) { 
            	simulationData += geoProcessor.getSimulationData();
            }
        }
        
		if (simulationMode) {
			LOG.info("Number of stay points: {}", simulationData);
			simulationData = 0;
		}
	}
	
	//###################################################################
	// Setters & Getters
	//###################################################################
	
	@Override
	public IProcessor<User> getProcessor() {
		return processor;
	}

	@Override
	public void setProcessor(IProcessor<User> processor) {
		this.processor = processor;
	}

	/**
	 * Returns the minimum points of a user to request.
	 * 
	 * @return the minimum points of a user.
	 */
	public int getMinUserPoints() {
		return minUserPoints;
	}

	/**
	 * Sets the minimum points of a user to request.
	 * 
	 * @param minUserPoints the minimum points of a user.
	 */
	public void setMinUserPoints(int minUserPoints) {
		this.minUserPoints = minUserPoints;
	}

	/**
	 * Returns the maximum points of a user to request.
	 * 
	 * @return the maximum points of a user.
	 */
	public int getMaxUserPoints() {
		return maxUserPoints;
	}

	/**
	 * Sets the maximum points of a user to request. A value of 
	 * -1 disables the upper bound.
	 * 
	 * @param maxUserPoints the maximum points of a user.
	 */
	public void setMaxUserPoints(int maxUserPoints) {
		this.maxUserPoints = maxUserPoints;
	}
	
	//###################################################################
	// Other
	//###################################################################

	/**
	 * This method clones an instance of the <code>UserReader</code>
	 * class. Therefore it copies all of its properties to a new
	 * instance. 
	 *  
	 * @see java.lang.Object#clone()
	 */
	@Override
	public IUserReader clone() throws CloneNotSupportedException {
		return new UserReader(this);
	}
}
