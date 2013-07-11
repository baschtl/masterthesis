package de.tub.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.reader.directory.IDirectoryReader;
import de.tub.reader.directory.IterativeDirectoryReader;
import de.tub.reader.file.IFileReader;
import de.tub.reader.file.ITextFileReader;
import de.tub.reader.file.IterativeFileReader;
import de.tub.reader.file.TextFileLineReader;
import de.tub.reader.model.IUserReader;
import de.tub.reader.model.UserReader;


/**
 * This class is an implementation of a Reader Factory. 
 * It is part of the Abstract Factory design pattern. 
 * Because there is only one <code>ReaderFactory</code> 
 * per application this class is implemented using the 
 * Singleton design pattern.
 * 
 * @author Sebastian Oelke
 *
 */
public class ReaderFactory extends AbstractReaderFactory {
	
	private static final Logger LOG = LoggerFactory.getLogger(ReaderFactory.class);
	
	private static ReaderFactory instance;
	
	private ReaderFactory() {
		// Initialize DAO registration with call to super constructor
		super();
		
		// Add initial prototypes to Reader registration
		addReader(TextFileLineReader.class, new TextFileLineReader());
		addReader(IterativeFileReader.class, new IterativeFileReader());
		addReader(IterativeDirectoryReader.class, new IterativeDirectoryReader());
		addReader(UserReader.class, new UserReader());
	}
	
	/**
	 * Get an instance of the <code>ReaderFactory</code>.
	 * 
	 * @return an instance of the <code>ReaderFactory</code>.
	 */
	public synchronized static ReaderFactory instance() {
		if (instance == null)
			instance = new ReaderFactory();
		
		return instance;
	}
	
	/**
	 * Returns an instance of the <code>TextFileLineReader</code> class.
	 * If it was not possible to create the instance 
	 * <code>null</code> is returned.
	 * 
	 * @return an instance of the <code>TextFileLineReader</code> class.
	 */
	public ITextFileReader getTextFileLineReader() {
		TextFileLineReader textFileLineReader = (TextFileLineReader) getReader(TextFileLineReader.class);
		try {
			return textFileLineReader.clone();
		} catch (CloneNotSupportedException e) {
			LOG.error("Cloning {} did not work:\n{}", TextFileLineReader.class, e);
			return null;
		}
	}
	
	/**
	 * Returns an instance of the <code>IterativeFileReader</code> class.
	 * If it was not possible to create the instance 
	 * <code>null</code> is returned.
	 * 
	 * @return an instance of the <code>IterativeFileReader</code> class.
	 */
	public IFileReader getIterativeFileReader() {
		IterativeFileReader iterativeFileReader = (IterativeFileReader) getReader(IterativeFileReader.class);
		try {
			return iterativeFileReader.clone();
		} catch (CloneNotSupportedException e) {
			LOG.error("Cloning {} did not work:\n{}", IterativeFileReader.class, e);
			return null;
		}
	}
	
	/**
	 * Returns an instance of the <code>IterativeDirectoryReader</code> class.
	 * If it was not possible to create the instance 
	 * <code>null</code> is returned.
	 * 
	 * @return an instance of the <code>IterativeDirectoryReader</code> class.
	 */
	public IDirectoryReader getIterativeDirectoryReader() {
		IterativeDirectoryReader iterativeDirectoryReader = (IterativeDirectoryReader) getReader(IterativeDirectoryReader.class);
		try {
			return iterativeDirectoryReader.clone();
		} catch (CloneNotSupportedException e) {
			LOG.error("Cloning {} did not work:\n{}", IterativeDirectoryReader.class, e);
			return null;
		}
	}
	
	/**
	 * Returns an instance of the <code>UserReader</code> class.
	 * If it was not possible to create the instance 
	 * <code>null</code> is returned.
	 * 
	 * @return an instance of the <code>UserReader</code> class.
	 */
	public IUserReader getUserReader() {
		UserReader UserReader = (UserReader) getReader(UserReader.class);
		try {
			return UserReader.clone();
		} catch (CloneNotSupportedException e) {
			LOG.error("Cloning {} did not work:\n{}", UserReader.class, e);
			return null;
		}
	}
}
