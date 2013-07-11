package de.tub.observer;

/**
 * Defines interests which observers can subscribe to at a subject.
 * 
 * @author Sebastian Oelke
 *
 */
public enum Interests {
	/**
	 * Is used to inform observers about the finishing of the processing of a subject.
	 */
	HasFinished,
	/**
	 * Is used to inform observers about the start of processing of a new directory of
	 * the file system.
	 */
	NewChildDirectory,
	/**
	 * Is used to inform observers about the end of processing a user.
	 */
	UserFinished
}
