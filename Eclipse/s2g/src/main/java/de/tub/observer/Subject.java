package de.tub.observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is part of the Observer design pattern. It designs
 * the subject which can be observed by multiple Observers.
 * 
 * @see de.tub.observer.Observer
 * @author Sebastian Oelke
 *
 */
public abstract class Subject {
	
	private static final Logger LOG = LoggerFactory.getLogger(Subject.class);
	
	private HashMap<Interests, List<Observer>> observers = new HashMap<Interests, List<Observer>>();
	
	/**
	 * Attaches the given observer to the list of observers with the given
	 * interest. If the observer is <code>null</code> false is returned.
	 * 
	 * @param observer the observer to attach.
	 * @param interest the observer is attached to the list of observers with this interest.
	 * @return <code>true</code> if the observer for the
	 * given interest was attached, otherwise <code>false</code>.
	 */
	public boolean attach(Observer observer, Interests interest) {
		if (observer == null) return false;
		
		// Get observer list for given interest
		List<Observer> list = observers.get(interest);
		LOG.debug("Attach observer: {} with interest: {}", observer, interest);
		
		// Check if there is already an observer list for this interest
		if (list == null) {
			// If not, create a new list and add it to the observers hash map
			LOG.debug("No observer list for the interest {}. Create a new one.", interest);
			list = new ArrayList<Observer>();
			observers.put(interest, list);
		}
		
		return list.add(observer);
	}
	
	/**
	 * This is a convenience method to attach an observer to multiple
	 * interests at once. If the given observer or the interests are 
	 * <code>null</code> or the interests array is empty this method 
	 * returns without result.
	 * 
	 * @param observer the observer to attach.
	 * @param interests the interests to which to attach with the observer.
	 */
	public void attach(Observer observer, Interests[] interests) {
		if (observer == null || interests == null || interests.length == 0) return;
		
		for (int i = 0; i < interests.length; i++) {
			LOG.debug("Attached: {}", attach(observer, interests[i]));
		}
	}
	
	/**
	 * Detaches the given observer from the list of observers attached with 
	 * the given interest. If the observer is <code>null</code> or there are no
	 * observers with the given interest present <code>false</code> is returned.
	 * A value of <code>true</code> is returned if there was an observer for the
	 * given interest present before detaching it.
	 * 
	 * @param observer the observer to detach.
	 * @param interest an observer attached with this interest is getting detached.
	 * @return <code>true</code> if there was an observer for the
	 * given interest present before detaching it, otherwise <code>false</code>.
	 */
	public boolean detach(Observer observer, Interests interest) {
		if (observer == null) return false;
		
		// Get observer list for given interest
		List<Observer> list = observers.get(interest);
		LOG.debug("Detach observer: {} with interest: {}", observer, interest);
		
		// Check if there is already an observer list for this interest
		if (list == null) {
			LOG.debug("No observer list for the interest {}. Return here.", interest);
			return false;
		}
		
		// Remove observer
		boolean result = list.remove(observer);
		// Clean up observer list for this interest if there is no observer anymore
		if (list.isEmpty())
			observers.remove(interest);
		
		return result;
	}
	
	/**
	 * Notifies all observers that are attached at this subject with the given interest.
	 * If there are no observers attached with the given interest this method returns
	 * with no result.
	 *  
	 * @param interest observers attached with this interest are notified.
	 * @param arg an argument passed to the observers.
	 */
	public void notifyObservers(Interests interest, Object arg) {
		// Get observer list for given interest
		List<Observer> list = observers.get(interest);
		LOG.debug("Notify all observers with Interest: {}, Argument: {}", interest, arg);
		
		// Check if there is already an observer list for this interest
		if (list == null || list.isEmpty())
			return;
		
		// Notify all observers
		for (int i = 0; i < list.size(); i++)
			list.get(i).update(this, interest, arg);
	}
}
