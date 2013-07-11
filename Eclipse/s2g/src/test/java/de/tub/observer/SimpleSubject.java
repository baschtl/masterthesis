package de.tub.observer;

public class SimpleSubject extends Subject {

	public static final String SIMPLE_MESSAGE = "A message comes around.";
	
	/**
	 * Calls all attached observers.
	 */
	public void callObservers() {
		notifyObservers(Interests.HasFinished, SIMPLE_MESSAGE);
	}
	
}
