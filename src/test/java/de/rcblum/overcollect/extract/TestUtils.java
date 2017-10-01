package de.rcblum.overcollect.extract;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.rcblum.overcollect.configuration.OWLib;

/**
 * Utilities for our JUnit tests.
 */
public class TestUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestUtils.class);

	public static void cleanupConfigurationFile() {
		LOGGER.debug("Cleaning up configuration file at: {}", OWLib.configPath);
		FileUtils.deleteQuietly(OWLib.configPath.toFile());
	}
}
