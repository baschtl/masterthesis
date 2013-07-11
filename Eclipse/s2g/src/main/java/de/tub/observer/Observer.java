package de.tub.observer;

/**
 * This interface is part of the Observer design pattern. Classes that
 * implement this interface become observers that can observe Subjects.
 * 
 * @see de.tub.observer.Subject
 * @author Sebastian Oelke
 *
 */
public interface Observer {

	/**
	 * Called by an observed subject when its state has changed.
	 * 
	 * @param theSubject the subject that notifies this observer.
	 * @param interest the interest this observer subscribed to at the subject.
	 * @param arg the argument passed from the subject to the observer.
	 */
	public void update(Subject theSubject, Interests interest, Object arg);
	
}
