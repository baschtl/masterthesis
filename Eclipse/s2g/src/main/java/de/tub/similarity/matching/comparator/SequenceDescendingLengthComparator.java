package de.tub.similarity.matching.comparator;

import java.util.Comparator;

import de.tub.similarity.Sequence;

/**
 * This comparator compares the lengths of two given sequences.
 * It can be used to sort a list of similar sequences into a list of those
 * similar sequences with descending length.
 * 
 * @author Sebastian Oelke
 *
 */
public class SequenceDescendingLengthComparator<T> implements Comparator<Sequence<T>> {

	@Override
	public int compare(Sequence<T> seq1, Sequence<T> seq2) {
		int seq1Size = seq1.size();
		int seq2Size = seq2.size();
		
		return (seq1Size > seq2Size ? -1 : (seq1Size == seq2Size ? 0 : 1));
	}
}
