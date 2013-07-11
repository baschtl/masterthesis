package de.tub.reader.directory;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.observer.Interests;
import de.tub.observer.Subject;
import de.tub.processor.IProcessor;
import de.tub.reader.IReader;
import de.tub.reader.file.IFileReader;
import de.tub.util.FileUtil;

/**
 * This Reader reads all visible directories in the given 
 * root directory and delegates them to a given Reader or Processor.
 * There is also the possibility to accept files in addition to
 * directories.
 * <p />
 * It extends the <code>Subject</code> class to notify observers
 * about a new child directory within the root directory when it is
 * read and about the finishing of its work.
 * 
 * @author Sebastian Oelke
 *
 */
public class IterativeDirectoryReader extends Subject implements IDirectoryReader, Cloneable {
	
	private static final Logger LOG = LoggerFactory.getLogger(IterativeDirectoryReader.class);
	
	private String directoryName, pathInChildDirectory;
	private boolean acceptFiles = false;
	
	private IProcessor<String> processor;
	private IReader reader;
	
	/**
	 * Standard constructor.
	 */
	public IterativeDirectoryReader() {}
	
	/**
	 * Copy constructor for use with the Prototype design pattern.
	 * If you use <code>clone()</code> this constructor has to be used
	 * and all instance variables have to be copied.
	 *  
	 * @param iterativeFolderReader The <code>IterativeFolderReader</code> which has to be copied.
	 * @see de.tub.reader.directory.IterativeDirectoryReader#clone()
	 */
	public IterativeDirectoryReader(final IterativeDirectoryReader iterativeFolderReader) {
		this.directoryName = iterativeFolderReader.getDirectoryName();
		this.pathInChildDirectory = iterativeFolderReader.getPathInChildDirectory();
	}
	
	/**
	 * This method informs this reader's observers with the <code>NEW_CHILD_DIRECTORY_INTEREST</code>
	 * if a new child directory was found and is now being processed. Furthermore, observers are 
	 * notified with the <code>HAS_FINISHED_INTEREST</code> when this reader has finished its work.
	 */
	@Override
	public void read() {
		// This Reader only supports either a reader or a processor, not both...
		if (processor != null && reader != null) {
			LOG.error("You have specified a Processor and a Reader. This IterativeFolderReader only " +
					"supports either a Reader or a Processor, not both.");
			return;
		}
		// ... but one of them should be specified
		if (processor == null && reader == null) {
			LOG.error("You have to specify either a Processor or a Reader. This IterativeFolderReader " +
					"supports the delegation to another Reader (e.g., the IterativeTextFileReader) " +
					"or the processing of the read folders by a Processor.");
			return;
		}
		// No folder to read specified
		if (directoryName == null || directoryName.isEmpty()) {
			LOG.error("This IterativeFolderReader requires the name of and the path to a root folder/directory.");
			return;
		}
		
		// Read root folder 
		File dir = new File(directoryName);
		
		// Check if directory exists and is a real directory
		if (dir.exists() && dir.isDirectory()) {
			// Get children of root directory
			File[] children = dir.listFiles(FileUtil.acceptVisibleFilesFilter(true, acceptFiles));
			
			// Check for an error
			if (children != null) {
				int childsNumber = children.length;
				
				// Process all children
				for (int i = 0; i < childsNumber; i++) {
					File child = children[i];
					// Build final path to give to reader or processor
					String checkedPathInChildDirectory = "";
					if (pathInChildDirectory != null)
						checkedPathInChildDirectory = pathInChildDirectory;
					File finalFile = new File(child, checkedPathInChildDirectory);
					
					// Notify observers about new child directory
					notifyObservers(Interests.NewChildDirectory, String.valueOf(i));
					
					// Invoke reader or processor
					if (reader != null) {
						LOG.debug("Delegate {} to {}.", finalFile, reader.getClass().getSimpleName());
						delegateToReader(finalFile);
					} else if (processor != null) {
						LOG.debug("Delegate {} to {}.", finalFile, processor.getClass().getSimpleName());
						delegateToProcessor(finalFile);
					}
				}
			} else {
				LOG.error("An error occurred while reading the children of the directory {}. Stopping further execution.", directoryName);
				return;
			}
		} else {
			LOG.error("The given folder {} could not be found or is not a directory. Stopping further execution.", directoryName);
			return;
		}
		// Notify observers about end of this reader
		notifyObservers(Interests.HasFinished, null);
	}
	
	/**
	 * Delegates the given resource to a reader.
	 * 
	 * @param delegatedResource the resource to delegate
	 */
	private void delegateToReader(File delegatedResource) {
		if (reader != null) {
			if (reader instanceof IFileReader) {
				IFileReader cReader = (IFileReader) reader;
				cReader.setFile(delegatedResource);
				cReader.read();
			} else {
				LOG.info("The specified reader {} is not supported.", reader.getClass());
			}
		}
	}
	
	/**
	 * Delegates the given resource to a processor.
	 * <p />
	 * <b>Not implemented, yet.</b>
	 * 
	 * @param delegatedResource the resource to delegate
	 */
	private void delegateToProcessor(File delegatedResource) {
		throw new RuntimeException("The delegation to a processor is not yet supported. Given argument is " + delegatedResource + ".");
	}
	
	//###################################################################
	// Setters & Getters
	//###################################################################
	
	@Override
	public void setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
	}
	
	@Override
	public String getDirectoryName() {
		return directoryName;
	}
	
	/**
	 * Returns the path that is appended to each child in the root directory.
	 * 
	 * @return the pathInChildDirectory the path to append
	 */
	public String getPathInChildDirectory() {
		return pathInChildDirectory;
	}

	/**
	 * Sets the path that is appended to each child in the root directory.
	 * 
	 * @param pathInChildDirectory the path to append
	 */
	public void setPathInChildDirectory(String pathInChildDirectory) {
		this.pathInChildDirectory = pathInChildDirectory;
	}
	
	/**
	 * Returns the value for ignoring or accepting files
	 * in addition to directories by this Reader.
	 * 
	 * @return the ignoreFiles
	 */
	public boolean isAcceptFiles() {
		return acceptFiles;
	}

	/**
	 * Defines if files are accepted in addition to
	 * directories by this Reader.
	 * 
	 * @param acceptFiles the ignoreFiles to set
	 */
	public void setAcceptFiles(boolean acceptFiles) {
		this.acceptFiles = acceptFiles;
	}

	@Override
	public IProcessor<String> getProcessor() {
		return processor;
	}
	
	@Override
	public void setProcessor(IProcessor<String> processor) {
		this.processor = processor;
	}
	
	@Override
	public IReader getReader() {
		return reader;
	}
	
	@Override
	public void setReader(IReader reader) {
		this.reader = reader;
	}
	
	//###################################################################
	// Other
	//###################################################################

	/**
	 * This method clones an instance of the <code>IterativeFolderReader</code>
	 * class. Therefore it copies all of its properties to a new
	 * instance. 
	 *  
	 * @see java.lang.Object#clone()
	 */
	@Override
	public IDirectoryReader clone() throws CloneNotSupportedException {
		return new IterativeDirectoryReader(this);
	}

}
