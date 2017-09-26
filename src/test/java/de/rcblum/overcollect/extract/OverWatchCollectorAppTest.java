package de.rcblum.overcollect.extract;

import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.rcblum.overcollect.OverWatchCollectorApp;
import de.rcblum.overcollect.capture.listener.ImageSource;
import de.rcblum.overcollect.configuration.OWLib;
import de.rcblum.overcollect.ui.JOverCollectFrame;

/**
 * JUnit tests for OverWatchCollectorAppTest.
 */
public class OverWatchCollectorAppTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(OverWatchCollectorAppTest.class);

	/**
	 * Simple test to ensure that the application can start up.
	 */
	// @Test
	public void testOverWatchCollectorApp() throws AWTException, IOException {
		LOGGER.info("Running: testOverWatchCollectorApp");

		final String[] args = new String[0];
		OverWatchCollectorApp.main(args);

		LOGGER.info("Finished: testOverWatchCollectorApp");
	}

	/**
	 * Test a full path of the OverWatchCollectorApp, from match start to match end.
	 */
	// @Test
	public void testOverWatchCollectorAppExecution() throws AWTException, IOException {
		LOGGER.info("Running: testOverWatchCollectorAppExecution");

		// Setup all the paths necessary to construct a MatchExtractor instance
		final Path resourcesPath = Paths.get("src/test/resources");
		final Path basePath = resourcesPath.resolve("matchExtractorTest");

		final Path matchRoot = basePath.resolve("capture");
		final Path imageRoot = basePath.resolve("images");

		final Path matchPath = matchRoot.resolve("7d2fcee9-9e02-4b18-93c6-cd54af2fe6d3");

		final Path pathMainMenu = matchPath.resolve("_main_menu.png");
		final Path pathMatchScreen = matchPath.resolve("Temple of Anubis.png");
		final Path pathDefeat = matchPath.resolve("_defeat.png");
		final Path pathSrScreen = matchPath.resolve("_sr_screen.png");

		final BufferedImage imageMainMenu = ImageIO.read(pathMainMenu.toFile());
		final BufferedImage imageMatchScreen = ImageIO.read(pathMatchScreen.toFile());
		final BufferedImage imageDefeat = ImageIO.read(pathDefeat.toFile());
		final BufferedImage imageSrScreen = ImageIO.read(pathSrScreen.toFile());

		final OverWatchCollectorApp app = new OverWatchCollectorApp();
		app.startCapture();
		final JOverCollectFrame jOverCollectFrame = new JOverCollectFrame(app);
		if (OWLib.getInstance().getAccounts().isEmpty()) {
			LOGGER.debug("No accounts found. Asking user to create account...");
			final String accountName = jOverCollectFrame.showAccountCreation();

			LOGGER.debug("Creating account: {}", accountName);
			OWLib.getInstance().setActiveAccount(accountName);
		}
		jOverCollectFrame.setVisible(true);

		// Load up our images into the queue
		final ImageSource imageSource = app.getCapture();
		imageSource.fireImage(imageMainMenu);
		imageSource.fireImage(imageMatchScreen);
		imageSource.fireImage(imageDefeat);
		imageSource.fireImage(imageSrScreen);
		imageSource.fireImage(imageMainMenu);

		try {
			// TODO: using sleep is not an ideal way to test this. Perhaps consider a
			// mocking framework instead?
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}

		LOGGER.info("Finished: testOverWatchCollectorAppExecution");
	}
}
