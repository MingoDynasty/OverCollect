package de.rcblum.overcollect.extract;

import java.awt.AWTException;
import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.rcblum.overcollect.OverWatchCollectorApp;

/**
 * JUnit tests for OverWatchCollectorAppTest.
 */
public class OverWatchCollectorAppTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(OverWatchCollectorAppTest.class);

	/**
	 * Simple test to ensure that the application can start up.
	 */
	@Test
	public void testOverWatchCollectorApp() throws AWTException, IOException {
		LOGGER.info("Running: testOverWatchCollectorApp");

		final String[] args = new String[0];
		OverWatchCollectorApp.main(args);

		LOGGER.info("Finished: testOverWatchCollectorApp");
	}
}
