package de.tub.reader.file;

import java.io.File;
import java.io.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tub.observer.Interests;
import de.tub.observer.Subject;
import de.tub.processor.IProcessor;
import de.tub.reader.IReader;


/**
 * This Reader reads all files in the given directory line by line
 * and delegates the processing of each line to the given Processor.
 * The files to read can be specified with an instance of <code>FileFilter</code>.
 * The default behavior is to read all files in the given directory.
 * 
 * @author Sebastian Oelke
 *
 */
public class IterativeFileReader extends Subject implements IFileReader, Cloneable {
	
	private static final Logger LOG = LoggerFactory.getLogger(IterativeFileReader.class);
	
	private File fileDirectory;
	private FileFilter fileFilter;
	private IProcessor<String> processor;
	private IReader reader;
	
	/**
	 * Standard constructor.
	 */
	public IterativeFileReader() {}
	
	/**
	 * Copy constructor for use with the Prototype design pattern.
	 * If you use <code>clone()</code> this constructor has to be used
	 * and all instance variables have to be copied.
	 *  
	 * @param iterativeFileReader The <code>IterativeFileReader</code> which has to be copied.
	 * @see de.tub.reader.file.IterativeFileReader#clone()
	 */
	public IterativeFileReader(final IterativeFileReader iterativeFileReader) {
		this.fileDirectory = iterativeFileReader.getFile();
		this.fileFilter = iterativeFileReader.getFileFilter();
	}
	
	@Override
	public void read() {
		// This Reader only supports either a reader or a processor, not both...
		if (processor != null && reader != null) {
			LOG.error("You have specified a Processor and a Reader. This IterativeFileReader only " +
					"supports either a Reader or a Processor, not both.");
			return;
		}
		// ... but one of them should be specified
		if (processor == null && reader == null) {
			LOG.error("You have to specify either a Processor or a Reader. This IterativeFileReader " +
					"supports the delegation to another Reader (e.g., the TextFileLineReader) " +
					"or the processing of the read files by a Processor.");
			return;
		}
		// No file to read specified or file does not exist or is not a directory
		if (fileDirectory == null || !fileDirectory.exists() || fileDirectory.isFile()) {
			LOG.error("This IterativeFileReader requires a File instance that refers to an existing directory " +
					"from which to iteratively read files.");
			return;
		}
		// No file filter specified, all files are read
		if (fileFilter == null) {
			LOG.info("This IterativeFileReader uses a FileFilter instance to filter out files that should not be read " +
					"within the given directory. You did not specify a file filter. This means that all files are read (including hidden files).");
		}
		
		// Get children of root directory, filter files with the given file filter,
		// if the file filter is null all files are read
		File[] children = fileDirectory.listFiles(fileFilter);
			
		if (children != null) {
			int childsNumber = children.length;
			
			// Process all children
			for (int i = 0; i < childsNumber; i++) {
				File child = children[i];
				
				// Invoke reader or processor
				if (reader != null) {
					LOG.debug("Delegate {} to {}.", child.getPath(), reader.getClass().getSimpleName());
					delegateToReader(child);
				} else if (processor != null) {
					LOG.debug("Delegate {} to {}.", child.getPath(), processor.getClass().getSimpleName());
					delegateToProcessor(child);
				}
			}
			
			// Notify all observers that the reader finished all resources
			notifyObservers(Interests.HasFinished, null);
		} else {
			LOG.error("An error occurred while reading the children of the directory {}. Stopping further execution.", fileDirectory.getPath());
			return;
		}	
	}
	
	/**
	 * Delegates the given resource to a reader.
	 * 
	 * @param delegatedResource the resource to delegate
	 */
	private void delegateToReader(File delegatedResource) {
		if (reader != null) {
			if (reader instanceof ITextFileReader) {
				ITextFileReader cReader = (ITextFileReader) reader;
				cReader.setResourceName(delegatedResource.getPath());
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
	
	@Override
	public void setFile(File file) {
		this.fileDirectory = file;
	}

	@Override
	public File getFile() {
		return fileDirectory;
	}
	
	/**
	 * Returns the file filter used by this reader. 
	 * 
	 * @return the fileFilter the file filter to use when reading in files.
	 */
	public FileFilter getFileFilter() {
		return fileFilter;
	}

	/**
	 * Sets the file filter used by this reader. 
	 * 
	 * @param fileFilter the fileFilter to set
	 */
	public void setFileFilter(FileFilter fileFilter) {
		this.fileFilter = fileFilter;
	}
	
	//###################################################################
	// Other
	//###################################################################

	/**
	 * This method clones an instance of the <code>IterativeFileReader</code>
	 * class. Therefore it copies all of its properties to a new
	 * instance. 
	 *  
	 * @see java.lang.Object#clone()
	 */
	@Override
	public IFileReader clone() throws CloneNotSupportedException {
		return new IterativeFileReader(this);
	}

}
