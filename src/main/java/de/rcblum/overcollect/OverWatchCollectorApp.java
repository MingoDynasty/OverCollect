package de.rcblum.overcollect;

import java.awt.AWTException;
import java.awt.EventQueue;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.rcblum.overcollect.capture.RobotCaptureEngine;
import de.rcblum.overcollect.capture.listener.ImageListener;
import de.rcblum.overcollect.capture.listener.ImageSource;
import de.rcblum.overcollect.collect.FilterEngine;
import de.rcblum.overcollect.collect.MatchComposer;
import de.rcblum.overcollect.collect.listener.owmatch.OWMatchListener;
import de.rcblum.overcollect.configuration.OWLib;
import de.rcblum.overcollect.extract.MatchExtractor;
import de.rcblum.overcollect.extract.listener.OWMatchExtractionListener;
import de.rcblum.overcollect.extract.ocr.Glyph;
import de.rcblum.overcollect.ui.JOverCollectFrame;
import de.rcblum.overcollect.ui.setup.filter.JFilterSetup;
import de.rcblum.overcollect.ui.setup.filter.JFilterTest;
import de.rcblum.overcollect.ui.setup.ocr.JOCRSetup;

public class OverWatchCollectorApp {
	private static final Logger LOGGER = LoggerFactory.getLogger(OverWatchCollectorApp.class);

	public static void main(String[] args) throws AWTException, IOException {
		try {
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			// handle exception
		} catch (ClassNotFoundException e) {
			// handle exception
		} catch (InstantiationException e) {
			// handle exception
		} catch (IllegalAccessException e) {
			// handle exception
		}
		// java.util.Enumeration keys = UIManager.getDefaults().keys();
		// while (keys.hasMoreElements()) {
		// Object key = keys.nextElement();
		// Object value = UIManager.get (key);
		// if (value != null && value instanceof
		// javax.swing.plaf.FontUIResource)
		// UIManager.put (key, new
		// javax.swing.plaf.FontUIResource(UiStatics.OW_FONT_NORMAL.deriveFont(Font.PLAIN,
		// 18)));
		// if (value != null && value instanceof
		// javax.swing.plaf.ColorUIResource &&
		// (key.toString().toLowerCase().contains("background") ||
		// key.toString().toLowerCase().contains("border")))
		// UIManager.put (key, new
		// javax.swing.plaf.ColorUIResource(UiStatics.COLOR_BACKGROUND));
		// }
		LOGGER.debug("OverCollect Version: {}", OWLib.VERSION_STRING);
		LOGGER.debug("Made By Roland von Werden");
		LOGGER.debug("Copyright @2017");
		LOGGER.debug("Uses the folowing, not modified, Open Source Libraries:");
		LOGGER.debug("  GSON 2.8.0, Licenced under Apache 2.0 Licence");
		LOGGER.debug("  JGOODIES 1.8.0, Licenced under BSD 2-clause Licence");
		LOGGER.debug("");
		if (args.length > 0 && (args[0].equalsIgnoreCase("/?") || args[0].equalsIgnoreCase("/help"))) {
			LOGGER.debug("Commandline Arguments:");
			LOGGER.debug("/? or /help                 Show this help text");
			LOGGER.debug("/filter_setup               Start Filter configuration utility");
			LOGGER.debug("/filter_test                Start Testframework for existing filters");
			LOGGER.debug("/ocr_setup                  Start OCR configuration utility");
			LOGGER.debug("/glyph_update               Update glyph data unsing the Filter data");
			System.exit(0);
		} else if (args.length > 0 && args[0].equalsIgnoreCase("/filter_setup")) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						JFilterSetup frame = new JFilterSetup();
						frame.setVisible(true);
					} catch (Exception e) {
						LOGGER.error("Exception: ", e);
					}
				}
			});
		} else if (args.length > 0 && args[0].equalsIgnoreCase("/filter_test")) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						JFilterTest frame = new JFilterTest();
						frame.setVisible(true);
					} catch (Exception e) {
						LOGGER.error("Exception: ", e);
					}
				}
			});
		} else if (args.length > 0 && args[0].equalsIgnoreCase("/ocr_setup")) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						JOCRSetup frame = new JOCRSetup();
						frame.setVisible(true);
					} catch (Exception e) {
						LOGGER.error("Exception: ", e);
					}
				}
			});
		} else if (args.length > 0 && args[0].equalsIgnoreCase("/glyph_update")) {
			Glyph.main(args);
		} else {
			OverWatchCollectorApp app = new OverWatchCollectorApp();
			app.startCapture();
			JOverCollectFrame jOverCollectFrame = new JOverCollectFrame(app);
			if (OWLib.getInstance().getAccounts().isEmpty()) {
				LOGGER.debug("No accounts found. Asking user to create account...");
				String accountName = jOverCollectFrame.showAccountCreation();

				LOGGER.debug("Creating account: {}", accountName);
				OWLib.getInstance().setActiveAccount(accountName);
			}
			// JOwCaptureStatus mapPanel = new JOwCaptureStatus();
			// f.getContentPane().add(mapPanel);
			// f.pack();
			// f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jOverCollectFrame.setVisible(true);
			// app.addOWMatchListener(mapPanel);
			// app.addImageListener(mapPanel);

		}

		LOGGER.info("End main.");
	}

	private ImageSource captureEngine = null;

	private FilterEngine filterEngine = null;

	private MatchComposer matchComposer = null;

	private MatchExtractor extractor = null;

	public OverWatchCollectorApp() throws AWTException, IOException {
		OWLib.getInstance();
		Path libPath = Paths.get(System.getProperties().getProperty("owcollect.lib.dir"));
		Path dataPath = Paths.get(System.getProperties().getProperty("owcollect.data.dir"));
		Path imagePath = Paths.get(System.getProperties().getProperty("owcollect.image.dir"));
		Path matchPath = Paths.get(System.getProperties().getProperty("owcollect.match.dir"));
		Path tempDir = Paths.get(System.getProperties().getProperty("owcollect.temp.dir"));

		// Make Paths if necessary
		if (!matchPath.toFile().exists()) {
			Files.createDirectories(matchPath);
		}
		if (!dataPath.toFile().exists()) {
			Files.createDirectories(dataPath);
		}
		if (!imagePath.toFile().exists()) {
			Files.createDirectories(imagePath);
		}
		if (!tempDir.toFile().exists()) {
			Files.createDirectories(tempDir);
		}
		if (!libPath.toFile().exists()) {
			throw new FileNotFoundException(
					"Library \"" + libPath.toAbsolutePath().toString() + "\" not found, installation must be corrupt");
		}

		/**
		 * Capture Screenshots
		 */
		// captureEngine = new RobotCaptureEngine();
		try {
			String className = System.getProperties().getProperty("de.rcblum.overcollect.capture");
			className = className != null ? className
					: OWLib.getInstance().getString("engines.capture",
							"de.rcblum.overcollect.capture.RobotCaptureEngine");
			LOGGER.info("Loading capture engine: {}", className);
			Class<?> classObject = Class.forName(className);
			Object captureObject = classObject.newInstance();
			if (captureObject instanceof ImageSource)
				captureEngine = (ImageSource) captureObject;
			else
				throw new ClassNotFoundException("Class not anm instance of Image Source");
		} catch (NullPointerException | InstantiationException | IllegalAccessException | ClassNotFoundException e1) {
			LOGGER.error(
					"NullPointerException | InstantiationException | IllegalAccessException | ClassNotFoundException: ",
					e1);
			LOGGER.debug("Fallback: initializing RobotCaptureEngine");
			captureEngine = new RobotCaptureEngine();
		}

		/**
		 * Filter screenshots for relevant images
		 */
		filterEngine = new FilterEngine();

		/**
		 * Compose captured screenshots into matches, discard duplicates or out of order
		 * false positives
		 */
		matchComposer = new MatchComposer(System.getProperties().getProperty("owcollect.match.dir"));

		/**
		 * Link the filter engine to the capture process
		 */
		captureEngine.addImageListener(filterEngine);

		/**
		 * Link composer to filter engine
		 */
		filterEngine.addOWItemImageListener(matchComposer);

		try {
			extractor = new MatchExtractor(matchPath, imagePath, dataPath);
			matchComposer.addOWMatchListener(extractor);
		} catch (IOException e) {
			LOGGER.error("IOException: ", e);
		}
	}

	public void addImageListener(ImageListener listener) {
		this.captureEngine.addImageListener(listener);
	}

	public void addOWMatchExtractionListener(OWMatchExtractionListener listener) {

		this.extractor.addExtractionListener(listener);
	}

	public void addOWMatchListener(OWMatchListener listener) {
		this.matchComposer.addOWMatchListener(listener);
	}

	public boolean isRecording() {
		// TODO Auto-generated method stub
		return this.captureEngine.isRunning();
	}

	public void removeImageListener(ImageListener listener) {
		this.captureEngine.removeImageListener(listener);
	}

	public void removeOWMatchExtractionListener(OWMatchExtractionListener listener) {

		this.extractor.removeExtractionListener(listener);
	}

	public void removeOWMatchListener(OWMatchListener listener) {
		this.matchComposer.removeOWMatchListener(listener);
	}

	public void setLibraryPath(String libPath) {
		System.getProperties().setProperty("owcollect.lib.dir", libPath);
	}

	public void setMatchPath(String matchPath) {
		System.getProperties().setProperty("owcollect.match.dir", matchPath);
	}

	public void startCapture() {
		this.captureEngine.start();
	}

	public void stopCapture() {
		this.captureEngine.stop();
	}

	public ImageSource getCapture() {
		return this.captureEngine;
	}
}
