package de.tub.similarity;


/**
 * A sequence cluster is used to wrap a cluster of 
 * a user's hierarchical graph with the relevant information
 * needed for sequence extraction. This information includes
 * the id of the cluster, the number of times a user successively
 * stayed in the cluster as well as the arrival and leaving times.
 * <p />
 * This call implements the <code>Cloneable</code> interface. Therefore,
 * the method <code>clone</code> is implemented and can be used to clone
 * an instance of the <code>SequenceCluster</code> class to a new one.
 * 
 * @author Sebastian Oelke
 *
 */
public class SequenceCluster {

	protected String id;
	protected int successivelyInCluster = 1;
	protected long arrivalTime;
	protected long leavingTime;
	
	public SequenceCluster() {}
	
	public SequenceCluster(String id) {
		this.id = id;
	}
	
	//###################################################################
	// Setters & Getters
	//###################################################################
	
	/**
	 * @return the id of the sequence cluster.
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id of the sequence cluster to set.
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the number of times a subject stayed successively in the cluster.
	 */
	public int getSuccessivelyInCluster() {
		return successivelyInCluster;
	}
	/**
	 * @param successivelyInCluster set the number of times a subject stayed 
	 * successively in the cluster.
	 */
	public void setSuccessivelyInCluster(int successivelyInCluster) {
		this.successivelyInCluster = successivelyInCluster;
	}
	/**
	 * @return the arrival time in the sequence cluster.
	 */
	public long getArrivalTime() {
		return arrivalTime;
	}
	/**
	 * @param arrivalTime the arrival time to set for the sequence cluster.
	 */
	public void setArrivalTime(long arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	/**
	 * @return the leaving time in the sequence cluster.
	 */
	public long getLeavingTime() {
		return leavingTime;
	}
	/**
	 * @param leavingTime the leaving time to set for the sequence cluster.
	 */
	public void setLeavingTime(long leavingTime) {
		this.leavingTime = leavingTime;
	}
	
	/**
	 * Increases the number of times a subject stayed 
	 * successively in the cluster.
	 * 
	 * @return the number after increasing it.
	 */
	public int incSuccessivelyInCluster() {
		this.successivelyInCluster++;
		return this.successivelyInCluster;
	}

	//###################################################################
	// hashCode, equals & toString
	//###################################################################
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		
		result = prime * result + (int) (arrivalTime ^ (arrivalTime >>> 32));
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (int) (leavingTime ^ (leavingTime >>> 32));
		result = prime * result + successivelyInCluster;
		
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (!(obj instanceof SequenceCluster))
			return false;
		
		SequenceCluster other = (SequenceCluster) obj;
		if (arrivalTime != other.arrivalTime)
			return false;
		
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		
		if (leavingTime != other.leavingTime)
			return false;
		
		if (successivelyInCluster != other.successivelyInCluster)
			return false;
		
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append(SequenceCluster.class.getSimpleName())
				.append("[ id=")
				.append(this.id)
				.append(", successively in cluster=")
				.append(this.successivelyInCluster)
				.append(", arrival time=")
				.append(this.arrivalTime)
				.append(", leaving time=")
				.append(this.leavingTime)
				.append(" ]");
		
		return builder.toString();
	}
}
