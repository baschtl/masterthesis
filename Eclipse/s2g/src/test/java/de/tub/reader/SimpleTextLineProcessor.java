package de.tub.reader;

import java.util.ArrayList;
import java.util.List;

import de.tub.processor.IProcessor;

public class SimpleTextLineProcessor implements IProcessor<String> {

	private ArrayList<String> lines = new ArrayList<String>();
	
	public List<String> getLines() {
		return lines;
	}

	@Override
	public void newData(String data) {
		lines.add(data);
	}

	@Override
	public void finish() {}

}
