package de.tub.similarity;

import java.util.LinkedList;
import java.util.List;

/**
 * The <code>Sequence</code> class is backed by a 
 * <code>LinkedList</code> and can be used in connection
 * with the <code>SequenceCluster</code> to map a
 * sequence of clusters visited by a user.
 * 
 * @author Sebastian Oelke
 * 
 * @see de.tub.similarity.SequenceCluster SequenceCluster
 * @see java.util.LinkedList LinkedList
 *
 */
public class Sequence<E> {
	
	private LinkedList<E> sequence = new LinkedList<E>();
	
	public Sequence() {}
	
	/**
	 * Initializes this sequence with the given list of elements of the same type.
	 * 
	 * @param initialSequence the list of elements to use when initializing this sequence.
	 */
	public Sequence(List<E> initialSequence) {
		this.sequence = new LinkedList<E>(initialSequence);
	}
	
	/**
	 * Initializes this sequence with the given sequence of the same type.
	 * 
	 * @param initialSequence the sequence to use when initializing this sequence.
	 */
	public Sequence(Sequence<E> initialSequence) {
		this.sequence = initialSequence.sequence;
	}
	
	/**
	 * Adds a cluster to this sequence.
	 * 
	 * @param cluster the cluster to add to this sequence.
	 * @return <code>true</code> if the sequence changed as a result 
	 * to the call, <code>false</code> otherwise.
	 */
	public Sequence<E> addCluster(E cluster) {
		sequence.add(cluster);
		return new Sequence<E>(sequence);
	}
	
	/**
	 * Removes the given cluster from this sequence.
	 *  
	 * @param cluster the cluster to remove.
	 * @return <code>true</code> if the sequence contained the cluster. 
	 */
	public boolean removeCluster(E cluster) {
		return sequence.remove(cluster);
	}
	
	/**
	 * Returns the cluster at the position of the index.
	 * 
	 * @param index the index of the cluster in this sequence to receive.
	 * @return the cluster at the specified index in this sequence.
	 */
	public E getCluster(int index) {
		return sequence.get(index);
	}
	
	/**
	 * Returns the number of elements in this sequence.
	 * 
	 * @return the number of elements in the sequence.
	 */
	public int size() {
		return sequence.size();
	}
	
	/**
	 * Returns <code>true</code> if this sequence contains no
	 * clusters.
	 * 
	 * @return <code>true</code> if this sequence contains no clusters, <code>false</code> otherwise.
	 */
	public boolean isEmpty() {
		return sequence.isEmpty();
	}
	
	/**
	 * Uses the <code>sublist()</code> method of the underlying 
	 * <code>LinkedList</code> to create a sub-sequence. This method 
	 * returns a new sequence instance that consists of the elements
	 * of the extracted sub-sequence.
	 * 
	 * @param fromIndex the start index of the sub-sequence, inclusive.
	 * @param toIndex the end index of the sub-sequence, exclusive.
	 * @return a new sequence holding the elements of the extracted 
	 * sub-sequence.
	 * 
	 * @see java.util.LinkedList#subList(int, int)
	 */
	public Sequence<E> subList(int fromIndex, int toIndex) {
		return new Sequence<E>(sequence.subList(fromIndex, toIndex));
	}
	
	//###################################################################
	// hashCode, equals & toString
	//###################################################################
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		
		result = prime * result
				+ ((sequence == null) ? 0 : sequence.hashCode());
		
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (!(obj instanceof Sequence))
			return false;
		
		Sequence<E> other = (Sequence<E>) obj;
		if (sequence == null) {
			if (other.sequence != null)
				return false;
		} else if (!sequence.equals(other.sequence))
			return false;
		
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("\n--> ")
				.append(Sequence.class.getSimpleName())
				.append(" with ")
				.append(this.size())
				.append(" elements:\n");
		
		for (int i = 0; i < this.size(); i++) {
			E current = this.getCluster(i);
			builder.append("[")
					.append(i)
					.append("] ")
					.append(current.toString())
					.append("\n");
		}
		
		return builder.toString();
	}
}
