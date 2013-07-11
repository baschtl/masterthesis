package de.tub.reader;

import java.util.ArrayList;
import java.util.List;

import de.tub.data.model.User;
import de.tub.processor.IProcessor;

public class SimpleUserProcessor implements IProcessor<User> {

	private ArrayList<User> users = new ArrayList<User>();
	
	public List<User> getUsers() {
		return users;
	}

	@Override
	public void newData(User data) {
		users.add(data);
	}

	@Override
	public void finish() {}

}
