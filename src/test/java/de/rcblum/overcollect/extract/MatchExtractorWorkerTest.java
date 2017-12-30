package de.rcblum.overcollect.extract;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.rcblum.overcollect.configuration.OWLib;
import de.rcblum.overcollect.extract.MatchExtractor.MatchExtractWorker;
import de.rcblum.overcollect.utils.ApplicationException;

/**
 * JUnit tests for MatchExtractorWorker.
 */
public class MatchExtractorWorkerTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(MatchExtractorWorkerTest.class);

	private static boolean isExistingApplicationConfiguration = false;

	@BeforeClass
	public static void setup() throws ApplicationException {
		isExistingApplicationConfiguration = OWLib.isExistingApplicationConfiguration();

		OWLib.loadApplicationConfiguration();
		OWLib.setupLib();
	}

	/**
	 * Test to ensure that createScreenExtract() can successfully extract team and
	 * enemy SR values.
	 */
	@Test
	public void testCreateScreenExtract() throws IOException {
		LOGGER.info("Running: testCreateScreenExtract");

		// Setup all the paths necessary to construct a MatchExtractor instance
		final Path resourcesPath = Paths.get("src/test/resources");
		final Path basePath = resourcesPath.resolve("matchExtractorTest");

		final Path matchRoot = basePath.resolve("capture");
		final Path imageRoot = basePath.resolve("images");
		final Path dataRoot = basePath.resolve("data");

		final Path matchPath = matchRoot.resolve("7d2fcee9-9e02-4b18-93c6-cd54af2fe6d3");
		final Path imagePath = imageRoot.resolve("7d2fcee9-9e02-4b18-93c6-cd54af2fe6d3");

		final Path image = matchPath.resolve("Temple of Anubis.png");
		final Path ocrConfig = matchPath.resolve("Temple of Anubis.ocr");

		// =============================
		// TODO: all of this is only needed because it schedules itself...
		// Maybe alternatively move the MatchExtractorWorker into its own class file so
		// I can simply create it directly?
		// =============================
		// Once constructed, we should expect it to schedule itself (I don't agree with
		// this implementation... but that's what it does)
		final MatchExtractor matchExtractor = new MatchExtractor(matchRoot, imageRoot, dataRoot);

		// Give it some time for the schedule to tick at least once. This will give the
		// MatchExtractor a chance to attempt to extract our test match.
		try {
			// TODO: using sleep is not an ideal way to test this. Perhaps consider a
			// mocking framework instead?
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
		// =============================

		final MatchExtractWorker matchExtractorWorker = matchExtractor.new MatchExtractWorker(matchPath, imagePath,
				dataRoot);
		final ScreenExtract screenExtract = matchExtractorWorker.createScreenExtract(image, ocrConfig);
		final String teamSr = screenExtract.getValue("teamSR");
		final String enemySr = screenExtract.getValue("enemySR");

		LOGGER.debug("teamSr: {}", teamSr);
		LOGGER.debug("enemySr: {}", enemySr);

		assertEquals("2586", teamSr);
		assertEquals("2589", enemySr);

		LOGGER.info("Finished: testCreateScreenExtract");
	}

	@AfterClass
	public static void tearDown() throws ApplicationException {
		if (!isExistingApplicationConfiguration) {
			TestUtils.cleanupConfigurationFile();
		}
	}
}
