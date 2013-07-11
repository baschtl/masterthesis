package de.tub.observer;

import static org.junit.Assert.*;

import org.junit.Test;

public class ObserverTest {

	@Test
	public void testObserver() {
		// Setup subject and observer 
		SimpleSubject s = new SimpleSubject();
		SimpleObserver o = new SimpleObserver();
		s.attach(o, Interests.HasFinished);
		
		// Test values before notifying observers
		assertEquals(null, o.getCallingSubject());
		assertEquals(null, o.getInterest());
		assertEquals(null, o.getReceivedMessage());
		
		s.callObservers();
		
		// Test values after notifying observers
		assertEquals(s, o.getCallingSubject());
		assertEquals(Interests.HasFinished, o.getInterest());
		assertEquals(SimpleSubject.SIMPLE_MESSAGE, o.getReceivedMessage());
		
		// Detach observer, reset it and test again
		s.detach(o, Interests.HasFinished);
		o.reset();
		
		s.callObservers();
		
		// Test values after detach observer
		assertEquals(null, o.getCallingSubject());
		assertEquals(null, o.getInterest());
		assertEquals(null, o.getReceivedMessage());
	}

}
