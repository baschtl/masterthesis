package de.tub.similarity;

/**
 * A similar sequence cluster is used to wrap a cluster of 
 * a similar with the relevant information
 * needed for further processing. This information includes
 * the id of the cluster, the number of times a user successively
 * stayed in the cluster as well as to fields for the arrival and leaving times
 * respectively.
 * <p />
 * The <code>SimilarSequenceCluster</code> extends the <code>SequenceCluster</code>
 * with a second arrival and leaving time. Hence, it can hold the
 * information of time of two logical sequence clusters which can
 * be useful to check if consecutive clusters of a sequence redeem 
 * the temporal constraint that is calculated after matching similar sequences.
 * 
 * @author Sebastian Oelke
 *
 */
public class SimilarSequenceCluster extends SequenceCluster {

	private long secondArrivalTime;
	private long secondLeavingTime;
	
	public SimilarSequenceCluster() {}
	
	public SimilarSequenceCluster(String id, int successivelyInCluster, 
			long arrivalTime, long leavingTime, 
			long secondArrivalTime, long secondLeavingTime) {
		
		this.id = id;
		this.successivelyInCluster = successivelyInCluster;
		this.arrivalTime = arrivalTime;
		this.leavingTime = leavingTime;
		this.secondArrivalTime = secondArrivalTime;
		this.secondLeavingTime = secondLeavingTime;
	}
	
	//###################################################################
	// Setters & Getters
	//###################################################################

	/**
	 * @return the second arrival time of this similar sequence cluster.
	 */
	public long getSecondArrivalTime() {
		return secondArrivalTime;
	}
	/**
	 * @param secondArrivalTime the second arrival time of this similar sequence cluster to set.
	 */
	public void setSecondArrivalTime(long secondArrivalTime) {
		this.secondArrivalTime = secondArrivalTime;
	}
	/**
	 * @return the second leaving time of this similar sequence cluster.
	 */
	public long getSecondLeavingTime() {
		return secondLeavingTime;
	}
	/**
	 * @param secondLeavingTime the second leaving time of this similar sequence cluster to set.
	 */
	public void setSecondLeavingTime(long secondLeavingTime) {
		this.secondLeavingTime = secondLeavingTime;
	}
	
	//###################################################################
	// hashCode, equals & toString
	//###################################################################
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		
		result = prime * result
				+ (int) (secondArrivalTime ^ (secondArrivalTime >>> 32));
		result = prime * result
				+ (int) (secondLeavingTime ^ (secondLeavingTime >>> 32));
		
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (!super.equals(obj))
			return false;
		
		if (!(obj instanceof SimilarSequenceCluster))
			return false;
		
		SimilarSequenceCluster other = (SimilarSequenceCluster) obj;
		if (secondArrivalTime != other.secondArrivalTime)
			return false;
		if (secondLeavingTime != other.secondLeavingTime)
			return false;
		
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append(SimilarSequenceCluster.class.getSimpleName())
				.append("[ id=")
				.append(this.getId())
				.append(", successively in cluster=")
				.append(this.getSuccessivelyInCluster())
				.append(", arrival time=")
				.append(this.getArrivalTime())
				.append(", leaving time=")
				.append(this.getLeavingTime())
				.append(", secondArrivalTime=")
				.append(secondArrivalTime)
				.append(", secondLeavingTime=")
				.append(this.secondLeavingTime)
				.append(" ]");
		
		return builder.toString();
	}
}
