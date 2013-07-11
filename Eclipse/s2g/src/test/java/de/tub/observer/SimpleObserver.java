package de.tub.observer;

public class SimpleObserver implements Observer {

	private String receivedMessage;
	private Subject callingSubject;
	private Interests interest;
	
	@Override
	public void update(Subject theSubject, Interests interest, Object arg) {
		receivedMessage = (String) arg;
		callingSubject = theSubject;
		this.interest = interest;
	}

	public void reset() {
		receivedMessage = null;
		callingSubject = null;
		this.interest = null;
	}
	
	/**
	 * @return the receivedMessage
	 */
	public String getReceivedMessage() {
		return receivedMessage;
	}

	/**
	 * @return the callingSubject
	 */
	public Subject getCallingSubject() {
		return callingSubject;
	}

	/**
	 * @return the interest
	 */
	public Interests getInterest() {
		return interest;
	}

}
