package de.rcblum.overcollect.configuration;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import de.rcblum.overcollect.data.OWMatch;
import de.rcblum.overcollect.extract.ocr.Glyph;
import de.rcblum.overcollect.utils.ApplicationException;

public class OWLib {
	private static final Logger LOGGER = LoggerFactory.getLogger(OWLib.class);

	// TODO: system properties is a horrendous way to deal with this.
	// We have a configuration.properties file... why don't we just use that??
	// Consider creating a (static?) class that exclusively handles the
	// configuration.properties.
	static {
		if (System.getProperties().getProperty("owcollect.temp.dir") == null)
			System.getProperties().setProperty("owcollect.temp.dir", "tmp");
		if (System.getProperties().getProperty("owcollect.ui.dateformat") == null)
			System.getProperties().setProperty("owcollect.ui.dateformat", "yyyy-MM-dd HH:mm:ss");
		if (System.getProperties().getProperty("owcollect.match.dir") == null)
			System.getProperties().setProperty("owcollect.match.dir", "capture");
		if (System.getProperties().getProperty("owcollect.data.dir") == null)
			System.getProperties().setProperty("owcollect.data.dir", "data");
		if (System.getProperties().getProperty("owcollect.image.dir") == null)
			System.getProperties().setProperty("owcollect.image.dir", "images");
		if (System.getProperties().getProperty("owcollect.lib.dir") == null)
			System.getProperties().setProperty("owcollect.lib.dir", "/lib/owdata");

		// Path libPath =
		// Paths.get(System.getProperties().getProperty("owcollect.lib.dir"));
		Path dataPath = Paths.get(System.getProperties().getProperty("owcollect.data.dir"));
		Path imagePath = Paths.get(System.getProperties().getProperty("owcollect.image.dir"));
		Path matchPath = Paths.get(System.getProperties().getProperty("owcollect.match.dir"));
		Path tempDir = Paths.get(System.getProperties().getProperty("owcollect.temp.dir"));

		try {
			// Make Paths if necessary
			if (!Files.exists(matchPath))
				Files.createDirectories(matchPath);
			if (!Files.exists(dataPath))
				Files.createDirectories(dataPath);
			if (!Files.exists(imagePath))
				Files.createDirectories(imagePath);
			if (!Files.exists(tempDir))
				Files.createDirectories(tempDir);
		} catch (IOException e) {
			LOGGER.error("IOException: ", e);
		}
	}
	//
	// Static attributes
	//

	public static final String VERSION_STRING = "0.2.1-alpha";

	// TODO: is this assignment even necessary?
	// private static String defaultFolder = Paths.get("lib", "owdata").toString();

	private static Map<String, OWLib> instances = new HashMap<>();

	// TODO: is there really any benefit to possibly having multiple instances of
	// OWLib?
	public static OWLib getInstance() {
		// defaultFolder = System.getProperties().getProperty("owcollect.lib.dir");
		final String defaultFolder = System.getProperties().getProperty("owcollect.lib.dir");
		if (instances.get(defaultFolder) == null) {
			instances.put(defaultFolder, new OWLib(defaultFolder));
		}
		return instances.get(defaultFolder);
	}

	//
	// Instance attributes
	//

	private static Path libPath = null;

	private List<String> supportedScreenResolutions = null;

	private Map<String, Map<String, OWItem>> items = null;

	private Map<String, OWMatch> matches = null;

	private static AppConfig appConfig = null;

	// TODO: eventually use appConfig instead of config.
	@Deprecated
	private Properties config = null;

	private List<String> accounts = null;

	private String selectedAccount = null;

	private List<String> seasons = null;

	private String selectedSeason = null;

	private OWLib(Path libPath) {
		LOGGER.debug("libPath: {}", libPath);

		this.libPath = Objects.requireNonNull(libPath);
		if (!Files.exists(this.libPath)) {
			throw new IllegalArgumentException(this.libPath.toAbsolutePath().toString() + " does not exists");
		}
		this.supportedScreenResolutions = new ArrayList<>(10);
		this.items = new HashMap<>();
		this.config = new Properties();
		// try (InputStream in =
		// Files.newInputStream(this.libPath.resolve("configuration.properties"))) {
		LOGGER.debug("Loading config at: {}", OWLib.configPath);
		try (InputStream in = Files.newInputStream(OWLib.configPath)) {
			this.config.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		init();
	}

	private OWLib(String libPath) {
		this(getFileFromClasspath(libPath));
	}

	public static final String DEFAULT_LIB_PATH = "/lib/owdata";
	public static final String CONFIG_FILENAME = "configuration.properties";
	public static Path configPath = null;

	public static void setupLib() throws ApplicationException {

		LOGGER.debug("Looking for library directory at: {}", appConfig.getLibPath());
		OWLib.libPath = Paths.get(appConfig.getLibPath());
		// OWLib.libPath = OWLib.getFileFromClasspath(appConfig.getLibPath());
		LOGGER.debug("libPath: {}", OWLib.libPath);

		if (isUsingJar()) {
			extractResource("lib");
		}
	}

	public static void loadApplicationConfiguration() {
		LOGGER.info("Loading application configuration...");

		// first, try to search for the config file in the current working directory
		final Path configPath = Paths.get(getCurrentWorkingDirectory(), CONFIG_FILENAME);

		final AppConfigManager appConfigManager = AppConfigManager.getInstance();
		final AppConfig appConfig = appConfigManager.loadConfig(configPath);

		// whether to use a specific libPath or not
		boolean libPathFromConfig = false;
		if (appConfig == null) {
			LOGGER.debug("No configuration file found.");
		}
		// the config file should preferably have a "libPath" specified
		else if (appConfig.getLibPath() == null) {
			LOGGER.debug("Configuration file found, but no libPath entry.");
		}
		// ideal scenario; we find and use the config file
		else {
			LOGGER.debug("Configuration file found.");
			libPathFromConfig = true;

			OWLib.appConfig = appConfig;
			OWLib.configPath = configPath;
		}

		if (!libPathFromConfig) {
			// copy default config file and use it
			final Path src = OWLib.getFileFromClasspath(AppConfigManager.DEFAULT_CONFIG_FILENAME);
			final Path dst = Paths.get(getCurrentWorkingDirectory(), CONFIG_FILENAME);

			LOGGER.debug("Coypying config file src={}, dst={}.", src, dst);
			try {
				FileUtils.copyFile(src.toFile(), dst.toFile());
			} catch (IOException ioe) {
				LOGGER.error("Failed to copy file.", ioe);
			}

			// have to reload the config at the new destination
			OWLib.appConfig = appConfigManager.loadConfig(dst);

			// have to save this for future use
			OWLib.configPath = dst;
		}
		appConfigManager.logAppConfig();

		LOGGER.info("Application configuration loaded.");
	}

	/**
	 * A bit of a hack-ish way to get the current running JAR. Possibly due to being
	 * a Spring Boot JAR and our classes being in a subfolder within the JAR instead
	 * of in the root?
	 */
	private static JarFile getCurrentRunningJarFile() throws ApplicationException, IOException {

		// e.g.: jar:file:/C:/path/to/OverCollect-0.0.1-SNAPSHOT.jar!/BOOT-INF/classes!/
		final URL url = OWLib.class.getProtectionDomain().getCodeSource().getLocation();
		LOGGER.trace("url: {}", url);

		if (!"jar".equals(url.getProtocol())) {
			throw new ApplicationException("Not a JAR file? Found protocol: " + url.getProtocol());
		}

		final JarURLConnection jarConnection = (JarURLConnection) url.openConnection();

		final URL url2 = jarConnection.getJarFileURL();
		// e.g.: jar:file:/C:/path/to/OverCollect-0.0.1-SNAPSHOT.jar!/BOOT-INF/classes
		LOGGER.trace("url2: {}", url2);
		// e.g.: file:/C:/path/to/OverCollect-0.0.1-SNAPSHOT.jar!/BOOT-INF/classes
		LOGGER.trace("url2.getFile(): {}", url2.getFile());

		final JarURLConnection jarConnection2 = (JarURLConnection) url2.openConnection();
		final URL url3 = jarConnection2.getJarFileURL();
		// e.g.: file:/C:/path/to/OverCollect-0.0.1-SNAPSHOT.jar
		LOGGER.trace("url3: {}", url3);
		// e.g.: /C:/path/to/OverCollect-0.0.1-SNAPSHOT.jar
		LOGGER.trace("url3.getFile(): {}", url3.getFile());

		final File file = new File(url3.getFile());
		// e.g. C:\path\to\OverCollect-0.0.1-SNAPSHOT.jar
		LOGGER.trace("file: {}", file);

		final JarFile jarFile = new JarFile(file);
		// e.g.: C:\path\to\OverCollect-0.0.1-SNAPSHOT.jar
		LOGGER.trace("jarFile: {}", jarFile.getName());
		return jarFile;
	}

	public static String getCurrentWorkingDirectory() {
		return System.getProperty("user.dir");
	}

	public static void extractResource(String name) throws ApplicationException {
		final Path outputPath = Paths.get(getCurrentWorkingDirectory());
		extractResource(outputPath, name);
	}

	/**
	 * Due to how we use Java File/Path objects everywhere, it's easier to simply
	 * extract the resources directory than refactor all that code which is almost
	 * everywhere...
	 * 
	 * @throws ApplicationException
	 *             If failed to get the current running JAR.
	 */
	public static void extractResource(Path outputPath, String name) throws ApplicationException {
		LOGGER.debug("Extracting resource ({}) to directory : {}", name, outputPath);

		boolean bSuccess = false;
		JarFile jarFile = null;
		try {
			jarFile = getCurrentRunningJarFile();
			final Enumeration<JarEntry> enums = jarFile.entries();

			while (enums.hasMoreElements()) {
				final JarEntry entry = enums.nextElement();
				if (!entry.getName().startsWith("BOOT-INF/classes/" + name)) {
					LOGGER.trace("Skipping entry: {}", entry.getName());
					continue;
				}

				LOGGER.trace("Found matching entry: {}", entry.getName());

				// Since this is a Spring Boot JAR, our files are located in
				// "BOOT-INF/classes/"... but we don't need to copy that prefix.
				final String rawEntryName = entry.getName().split("BOOT-INF/classes/")[1];
				final Path destination = outputPath.resolve(rawEntryName);

				final File fileToWrite = destination.toFile();
				if (entry.isDirectory()) {
					LOGGER.trace("Entry was simply a directory. Making directories: {}", destination);
					fileToWrite.mkdirs();
					continue;
				}

				LOGGER.trace("Copying file to: {}", fileToWrite);
				final InputStream in = jarFile.getInputStream(entry);
				final OutputStream out = new FileOutputStream(fileToWrite);
				IOUtils.copy(in, out);
				bSuccess = true;
			}
		} catch (IOException ioe) {
			LOGGER.warn("IOException: ", ioe);
		} finally {
			IOUtils.closeQuietly(jarFile);
		}

		if (bSuccess) {
			LOGGER.debug("Successfully extracted resources directory.");
		} else {
			LOGGER.error("Failed to extract resources directory.");
		}
	}

	public static boolean isUsingJar() {

		// e.g.: jar:file:/C:/path/to/OverCollect-0.0.1-SNAPSHOT.jar!/BOOT-INF/classes!/
		final URL url = OWLib.class.getProtectionDomain().getCodeSource().getLocation();
		LOGGER.trace("url: {}", url);
		LOGGER.trace("protocol: {}", url.getProtocol());

		if ("jar".equals(url.getProtocol())) {
			LOGGER.debug("Is using JAR.");
			return true;
		}
		LOGGER.debug("Is not using JAR.");
		return false;

		// final ClassPathResource resource = new ClassPathResource(name);
		// try {
		// LOGGER.debug("resource.getFile(): {}", resource.getFile());
		// LOGGER.debug("File is within FileSystem. Continuing with default
		// implementation...");
		// return false;
		// } catch (IOException e) {
		// LOGGER.warn("File is NOT within FileSystem (probably inside JAR). Extracting
		// to local...");
		// }
		// return true;
	}

	// TODO: this implementation and design is a bit hacky, but it will do for now.
	// Eventually want to re-design this so that we extractResource() when we search
	// for the config file. Once we have the config file then we should base our
	// implementation off that. (e.g. allow the config file to specify a custom work
	// directory;
	public static Path getFileFromClasspath(String name) {
		LOGGER.debug("Searching for resource on the classpath: {}", name);

		boolean isExtractedJar = false;

		// TODO: come up with a better implementation than using ClassPathResource.
		// This one reference requires us to pull in entire springframework core as a
		// dependency, lol.
		File file = null;
		final ClassPathResource resource = new ClassPathResource(name);
		try {
			LOGGER.debug("resource.getFile(): {}", resource.getFile());
			LOGGER.debug("File is within FileSystem. Continuing with default implementation...");
			file = resource.getFile();
		} catch (IOException e) {
			LOGGER.warn("File is NOT within FileSystem (probably inside JAR). Extracting to local...");
			try {
				extractResource(name);
				isExtractedJar = true;
			} catch (Exception ex) {
				LOGGER.error("Exception: ", ex);
			}
		}

		if (isExtractedJar) {
			final Path outputPath = Paths.get(getCurrentWorkingDirectory(), name);
			file = outputPath.toFile();
		} else {
			// final URL url = OWLib.class.getResource(name);
			// LOGGER.debug("url: {}", url);
			// file = new File(url.getFile());
		}
		LOGGER.debug("file: {}", file);

		if (file.exists()) {
			LOGGER.debug("Successfully found resource: {}", file);
			return file.toPath();
		}
		LOGGER.error("Failed to find resource on the classpath: {}", name);
		return null;
	}

	public void addAccount(String accountName) {
		this.accounts.add(accountName);
		this.saveConfig();
	}

	public void addSeason(String seasonName) {
		if (!this.seasons.contains(seasonName)) {
			if (this.seasons.size() == 0)
				this.setActiveSeason(seasonName);
			this.seasons.add(seasonName);
			this.saveConfig();
		}
	}

	public void addMatch(OWMatch match) {
		Objects.requireNonNull(match);
		this.matches.put(match.getMatchId(), match);
	}

	public List<String> getAccounts() {
		return this.accounts;
	}

	public List<String> getSeasons() {
		return this.seasons;
	}

	public String getActiveAccount() {
		return this.selectedAccount;
	}

	public String getActiveSeason() {
		return this.selectedSeason;
	}

	public boolean getBoolean(String key) {
		return Boolean.valueOf(this.config.getProperty(key, "false"));
	}

	public String getDebugDir() {
		String ddirName = getString("debug.dir", "debug");
		Path ddir = Paths.get(ddirName);
		if (!Files.exists(ddir)) {
			try {
				Files.createDirectories(ddir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return ddirName;
	}

	public List<OWItem> getDropItems(int width, int height) {
		return this.items.get(width + "x" + height) != null ? this.items.get(width + "x" + height).values().stream()
				.filter(i -> i.drop()).collect(Collectors.toList()) : new LinkedList<>();
	}

	public List<OWItem> getHeroes() {
		return this.getItems("1920x1080").stream().filter(i -> i.isHero()).collect(Collectors.toList());
	}

	public int getInteger(String key, int defaultValue) {
		String val = this.config.getProperty(key, String.valueOf(defaultValue));
		return val.matches("^-?\\d+$") ? Integer.valueOf(val) : defaultValue;
	}

	public OWItem getItem(Dimension screenResolution, String itemName) {
		String res = ((int) screenResolution.getWidth()) + "x" + ((int) screenResolution.getHeight());
		return this.getItem(res, itemName);
	}

	public OWItem getItem(int width, int height, String itemName) {
		String res = width + "x" + height;
		return this.getItem(res, itemName);
	}

	public OWItem getItem(String screenResolution, String itemName) {
		return this.items.get(screenResolution).get(itemName);
	}

	public List<String> getItemNames(Dimension screenResolution) {
		String res = ((int) screenResolution.getWidth()) + "x" + ((int) screenResolution.getHeight());
		return this.getItemNames(res);
	}

	public List<String> getItemNames(String screenResolution) {
		return new ArrayList<>(this.items.get(screenResolution).keySet());
	}

	public List<OWItem> getItems(int width, int height) {
		return this.getItems(width + "x" + height);

	}

	public List<OWItem> getItems(String res) {
		return this.items.get(res) != null ? new ArrayList<>(this.items.get(res).values()) : null;
	}

	public Path getLibPath() {
		return libPath;
	}

	public List<String> getMaps() {
		return this.items.get("1920x1080").values().stream().filter(i -> i.isMap()).map(i -> i.getItemName())
				.collect(Collectors.toList());
	}

	public OWMatch getMatch(String matchId) {
		return this.matches.get(matchId);
	}

	public List<OWMatch> getMatches() {
		return new ArrayList<>(this.matches.values());
	}

	public List<String> getMatchIds() {
		return new ArrayList<>(this.matches.keySet());
	}

	public List<String> getMatchIndicators() {
		return this.items.get("1920x1080").values().stream().filter(i -> i.isMatchIndicator()).map(i -> i.getItemName())
				.collect(Collectors.toList());
	}

	public List<Glyph> getPrimaryFontGlyphs() {
		return this.items.get("ocr_primary_font") != null ? this.items.get("ocr_primary_font").values().stream()
				.filter(i -> i.hasGlyph()).map(i -> i.getGlyph()).collect(Collectors.toList()) : null;
	}

	public int getSecondaryFontBaseSize() {
		List<Glyph> g = getSecondaryFontGlyphs();
		return g.size() > 0 ? g.get(0).getBaseFontSize() : 57;
	}

	public List<Glyph> getSecondaryFontGlyphs() {
		return this.items.get("ocr_secondary_font") != null
				? this.items.get("ocr_secondary_font").values().stream().filter(i -> i.hasGlyph())
						.map(i -> i.getGlyph()).collect(Collectors.toList())
				: null;
	}

	public String getString(String key, String defaultString) {
		return this.config.getProperty(key) != null ? this.config.getProperty(key) : defaultString;
	}

	public List<String> getSupportedScreenResolutions() {
		return this.supportedScreenResolutions;
	}

	public Path getTempPath() {
		return Paths.get(System.getProperties().getProperty("owcollect.temp.dir"));
	}

	private void init() {
		LOGGER.debug("Initializing class: {}", this.getClass().getName());
		this.loadAccounts();
		this.loadSeasons();

		this.findAllResolutions();
		this.findAllConfigItems();
		this.findAllMatches();
		LOGGER.debug("Class initialized: {}", this.getClass().getName());
	}

	private void loadAccounts() {
		LOGGER.info("Loading accounts...");
		this.accounts = new LinkedList<>(this.config.getProperty("accounts") != null
				? Arrays.asList(this.config.getProperty("accounts").split(","))
				: new LinkedList<>());
		for (String account : this.accounts) {
			LOGGER.debug("Found account: {}", account);
		}

		this.selectedAccount = this.config.getProperty("activeAccount");
		LOGGER.debug("Selected account: {}", this.selectedAccount);
	}

	private void loadSeasons() {
		LOGGER.info("Loading seasons...");
		this.seasons = new LinkedList<>(this.config.getProperty("seasons") != null
				? Arrays.asList(this.config.getProperty("seasons").split(","))
				: new LinkedList<>());
		for (String season : this.seasons) {
			LOGGER.debug("Found season: {}", season);
		}

		this.selectedSeason = this.config.getProperty("activeSeason");
		LOGGER.debug("Selected season: {}", this.selectedSeason);
	}

	private void findAllResolutions() {
		LOGGER.info("Finding all resolutions...");
		File[] resolutionFolders = this.libPath.toFile().listFiles();
		Arrays.sort(resolutionFolders);

		for (File res : resolutionFolders) {
			if (res.isDirectory()) {
				String[] dimensionStrings = res.getName().split("x");
				if (dimensionStrings.length == 2 && dimensionStrings[0].matches("\\d+")
						&& dimensionStrings[1].matches("\\d+")) {
					LOGGER.warn("Shouldn't something go into this empty block???");
				}
				LOGGER.trace("Found resolution: {}", res.getPath());
				this.supportedScreenResolutions.add(res.getName());
			}
		}
	}

	private void findAllConfigItems() {
		LOGGER.info("Finding all config items...");
		for (String res : this.supportedScreenResolutions) {
			this.items.put(res, new HashMap<>());
			File[] items = this.libPath.resolve(res).toFile().listFiles();
			Arrays.sort(items);
			for (File item : items) {
				if (item.exists() && item.isDirectory()) {
					LOGGER.trace("Found config item: {}", item.getPath());
					this.items.get(res).put(item.getName(), new OWItem(res, item.getName(), this.libPath.toString()));
				}

			}
		}
	}

	private void findAllMatches() {
		// Find all Matches
		LOGGER.info("Finding all matches...");
		this.matches = new HashMap<>();
		File[] matchFiles = Paths.get(System.getProperties().getProperty("owcollect.data.dir")).toFile().listFiles();
		for (File match : matchFiles) {
			LOGGER.debug("Found match: {}", match);
			OWMatch m = OWMatch.fromJsonFile(match);
			if (m != null) {
				this.matches.put(m.getMatchId(), m);
				if (m.getAccount() == null && this.getActiveAccount() != null) {
					m.setAccount(this.getActiveAccount());
					OWMatch.toJsonFile(m, match);
				}
				// Fix older versions with no season
				if (m.getSeason() == null && this.getActiveSeason() != null) {
					m.setSeason(this.getActiveSeason());
					OWMatch.toJsonFile(m, match);
				}
			}
		}
	}

	private void saveConfig() {
		this.config.setProperty("accounts", String.join(",", this.accounts));
		this.config.setProperty("activeAccount", this.selectedAccount);
		this.config.setProperty("seasons", String.join(",", this.seasons));
		this.config.setProperty("activeSeason", this.selectedSeason);
		// try (OutputStream os =
		// Files.newOutputStream(this.libPath.resolve("configuration.properties"))) {
		try (OutputStream os = new FileOutputStream(OWLib.configPath.toFile())) {
			this.config.store(os, "");
		} catch (IOException e) {
			LOGGER.error("Failed to save config.", e);
		}

	}

	public void setActiveAccount(String account) {
		if (!this.accounts.contains(account))
			this.accounts.add(account);
		this.selectedAccount = account;
		this.config.setProperty("activeAccount", this.selectedAccount);
		this.saveConfig();
	}

	public void setActiveSeason(String season) {
		if (this.seasons.size() == 0) {
			for (OWMatch m : this.matches.values()) {
				// Fix older versions with no season
				if (m.getSeason() == null && season != null) {
					Path match = Paths.get(System.getProperties().getProperty("owcollect.data.dir"),
							m.getMatchId() + ".json");
					m.setSeason(season);
					OWMatch.toJsonFile(m, match.toFile());
				}
			}
		}
		if (!this.seasons.contains(season))
			this.seasons.add(season);
		this.selectedSeason = season;
		this.config.setProperty("activeSeason", this.selectedSeason);
		this.saveConfig();
	}

	public boolean supportScreenResolution(Dimension screenResolution) {
		String res = ((int) screenResolution.getWidth()) + "x" + ((int) screenResolution.getHeight());
		return this.supportScreenResolution(res);
	}

	public boolean supportScreenResolution(int width, int height) {
		String res = width + "x" + height;
		return this.supportScreenResolution(res);
	}

	public boolean supportScreenResolution(String screenResolution) {
		return this.supportedScreenResolutions.contains(screenResolution);
	}
}
