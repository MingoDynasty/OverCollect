package de.rcblum.overcollect.configuration;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to manage application configurations.
 */
public class AppConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigManager.class);

	public static final String DEFAULT_CONFIG_FILENAME = "default.properties";
	private static AppConfigManager appConfigManager;

	private AppConfig appConfig = null;

	// TODO: should be a better way of keeping this class a singleton...
	public static AppConfigManager getInstance() {
		if (appConfigManager == null) {
			appConfigManager = new AppConfigManager();
		}
		return appConfigManager;
	}

	private List<String> strSplitToList(String str) {
		if (str == null) {
			return null;
		}
		return Arrays.asList(str.split("\\s*,\\s*"));
	}

	public AppConfig loadConfig(Path path) {
		LOGGER.debug("Loading configuration file: {}", path);

		final File file = path.toFile();
		if (!file.exists() || !file.isFile()) {
			LOGGER.warn("Configuration file not found: {}", file);
			return null;
		}

		this.appConfig = new AppConfig();
		final Configurations configs = new Configurations();
		try {
			final PropertiesConfiguration config = configs.properties(file);

			this.appConfig.setConfigPath(path);

			this.appConfig.setLibPath(config.getString("libPath"));

			this.appConfig.setAccounts(strSplitToList(config.getString("accounts")));
			this.appConfig.setSeasons(strSplitToList(config.getString("seasons")));

			this.appConfig.setActiveAccount(config.getString("activeAccount"));

			this.appConfig.setActiveSeason(config.getString("activeSeason"));
			this.appConfig.setDuplicateThreshold(config.getInt("duplicateThreshold"));
			this.appConfig.setCaptureInterval(config.getLong("captureInterval"));
			this.appConfig.setDropDuplicateFilter(config.getBoolean("dropDuplicateFilter"));
			this.appConfig.setEnginesCapture(config.getString("engines.capture"));
			this.appConfig.setDebugCapture(config.getBoolean("debug.capture"));
			this.appConfig.setDebugDir(config.getString("debug.dir"));
			this.appConfig.setDebugExtraction(config.getBoolean("debug.extraction"));
			this.appConfig.setDebugFilter(config.getBoolean("debug.filter"));
		} catch (ConfigurationException cex) {
			LOGGER.error("Failed to load configuration.", cex);
		}

		return this.appConfig;
	}

	public void saveConfig() {
		// TODO
	}

	public void logAppConfig() {
		this.logAppConfig(this.appConfig);
	}

	public void logAppConfig(AppConfig appConfig) {
		LOGGER.debug("Logging application config contents:");
		LOGGER.debug("libPath: {}", appConfig.getLibPath());
		LOGGER.debug("accounts: {}", appConfig.getAccounts());
		LOGGER.debug("activeAccount: {}", appConfig.getActiveAccount());
		LOGGER.debug("seasons: {}", appConfig.getSeasons());
		LOGGER.debug("activeSeason: {}", appConfig.getActiveSeason());
		LOGGER.debug("duplicateThreshold: {}", appConfig.getDuplicateThreshold());
		LOGGER.debug("captureInterval: {}", appConfig.getCaptureInterval());
		LOGGER.debug("dropDuplicateFilter: {}", appConfig.isDropDuplicateFilter());
		LOGGER.debug("enginesCapture: {}", appConfig.getEnginesCapture());
		LOGGER.debug("debugCapture: {}", appConfig.isDebugCapture());
		LOGGER.debug("debugDir: {}", appConfig.getDebugDir());
		LOGGER.debug("debugExtraction: {}", appConfig.isDebugExtraction());
		LOGGER.debug("debugFilter: {}", appConfig.isDebugFilter());
	}

	/**
	 * @return the appConfig
	 */
	public AppConfig getAppConfig() {
		return appConfig;
	}

	/**
	 * @param appConfig
	 *            the appConfig to set
	 */
	public void setAppConfig(AppConfig appConfig) {
		this.appConfig = appConfig;
	}

}
