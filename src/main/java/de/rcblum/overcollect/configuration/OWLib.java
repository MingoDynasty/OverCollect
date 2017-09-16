package de.rcblum.overcollect.configuration;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.rcblum.overcollect.data.OWMatch;
import de.rcblum.overcollect.extract.ocr.Glyph;

public class OWLib {
	private static final Logger LOGGER = LoggerFactory.getLogger(OWLib.class);

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
			System.getProperties().setProperty("owcollect.lib.dir", "lib" + File.separator + "owdata");

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//
	// Static attributes
	//

	public static final String VERSION_STRING = "0.2.1-alpha";

	// TODO: is this assignment even necessary?
	// private static String defaultFolder = Paths.get("lib", "owdata").toString();

	private static Map<String, OWLib> instances = new HashMap<>();

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

	private Path libPath = null;

	private List<String> supportedScreenResolutions = null;

	private Map<String, Map<String, OWItem>> items = null;

	private Map<String, OWMatch> matches = null;

	private Properties config = null;

	private List<String> accounts = null;

	private String selectedAccount = null;

	private List<String> seasons = null;

	private String selectedSeason = null;

	private OWLib() {
		this(Paths.get("lib", "owdata"));
	}

	private OWLib(Path libPath) {
		this.libPath = Objects.requireNonNull(libPath);
		if (!Files.exists(this.libPath))
			throw new IllegalArgumentException(this.libPath.toAbsolutePath().toString() + " does no exists");
		this.supportedScreenResolutions = new ArrayList<>(10);
		this.items = new HashMap<>();
		this.config = new Properties();
		try (InputStream in = Files.newInputStream(this.libPath.resolve("configuration.properties"))) {
			this.config.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		init();
	}

	private OWLib(String libPath) {
		this(Paths.get(libPath));
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
		try (OutputStream os = Files.newOutputStream(this.libPath.resolve("configuration.properties"))) {
			this.config.store(os, "");
		} catch (IOException e) {
			e.printStackTrace();
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
