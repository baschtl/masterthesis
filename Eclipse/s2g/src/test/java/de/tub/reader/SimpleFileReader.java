package de.tub.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.tub.processor.IProcessor;
import de.tub.reader.file.IFileReader;

public class SimpleFileReader implements IFileReader {

	private ArrayList<File> files = new ArrayList<File>();
	
	public List<File> getFiles() {
		return files;
	}
	
	@Override
	public void setFile(File file) {
		files.add(file);
	}

	@Override
	public File getFile() {
		return null;
	}

	@Override
	public IProcessor<String> getProcessor() {
		return null;
	}

	@Override
	public void setProcessor(IProcessor<String> processor) {
	}

	@Override
	public IReader getReader() {
		return null;
	}

	@Override
	public void setReader(IReader reader) {
	}

	@Override
	public void read() {

	}

}
