package de.tub.processor;

/**
 * All implementing classes are able to simulate their processing
 * and collect data during the simulation.
 * 
 * @author Sebastian Oelke
 *
 */
public interface ISimulator<E> {

	/**
	 * Returns the data collected by this simulator.
	 * 
	 * @return the data collected during simulation.
	 */
	E getSimulationData();
	
	/**
	 * Returns <code>true</code> if this processor runs in 
	 * simulation mode.
	 * 
	 * @return <code>true</code> if in simulation mode, <code>false</code> otherwise.
	 */
	boolean isSimulation();
	
	/**
	 * Enables or disables the simulation mode for this entity.
	 * 
	 * @param simulate <code>true</code> to turn simulation mode on, <code>false</code> to turn it off.
	 */
	void setSimulation(boolean simulate);
	
}
