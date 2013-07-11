package de.tub.reader;

import java.util.ArrayList;
import java.util.List;

import de.tub.processor.IProcessor;
import de.tub.reader.file.ITextFileReader;

public class SimpleTextFileReader implements ITextFileReader {

	private ArrayList<String> files = new ArrayList<String>();
	
	public List<String> getFiles() {
		return files;
	}

	@Override
	public void setResourceName(String resourceName) {
		files.add(resourceName);
	}

	@Override
	public String getResourceName() {
		return null;
	}

	@Override
	public IProcessor<String> getProcessor() {
		return null;
	}

	@Override
	public void setProcessor(IProcessor<String> processor) {}

	@Override
	public void read() {}

}