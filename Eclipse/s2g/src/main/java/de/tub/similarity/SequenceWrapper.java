package de.tub.similarity;

/**
 * The <code>SequenceWrapper</code> class wraps the 
 * cluster sequences of two users.
 * 
 * @author Sebastian Oelke
 * 
 * @see de.tub.similarity.Sequence Sequence
 * @see de.tub.similarity.SequenceCluster SequenceCluster
 *
 */
public class SequenceWrapper {
	
	private Sequence<SequenceCluster> firstSequence, secondSequence;

	public SequenceWrapper () {}
	
	public SequenceWrapper(Sequence<SequenceCluster> firstSequence, 
							Sequence<SequenceCluster> secondSequence) {
		this.firstSequence = firstSequence;
		this.secondSequence = secondSequence;
	}
	
	//###################################################################
	// Setters & Getters
	//###################################################################
	
	/**
	 * Returns the sequence of the first user.
	 * 
	 * @return the sequence of the first user.
	 */
	public Sequence<SequenceCluster> getFirstSequence() {
		return firstSequence;
	}

	/**
	 * Sets the sequence of the first user.
	 * 
	 * @param firstSequence the sequence of the first user.
	 */
	public void setFirstSequence(Sequence<SequenceCluster> firstSequence) {
		this.firstSequence = firstSequence;
	}

	/**
	 * Returns the sequence of the second user.
	 * 
	 * @return the sequence of the second user.
	 */
	public Sequence<SequenceCluster> getSecondSequence() {
		return secondSequence;
	}

	/**
	 * Sets the sequence of the second user.
	 * 
	 * @param secondSequence the sequence of the second user.
	 */
	public void setSecondSequence(Sequence<SequenceCluster> secondSequence) {
		this.secondSequence = secondSequence;
	}
	
}
