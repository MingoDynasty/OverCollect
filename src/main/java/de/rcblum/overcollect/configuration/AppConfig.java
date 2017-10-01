package de.rcblum.overcollect.configuration;

import java.nio.file.Path;
import java.util.List;

/**
 * This class represents the configuration of the Application. It is mostly just
 * a boring POJO.
 */
public class AppConfig {

	/*
	 * Note: this is the only non-config property in this class. This variable is so
	 * that this class has a reference to where it exists on the file system.
	 */
	private Path configPath;

	private String libPath;

	private List<String> accounts;
	private String activeAccount;

	private List<String> seasons;
	private String activeSeason;

	private int duplicateThreshold;
	private long captureInterval;

	private boolean dropDuplicateFilter;
	private String enginesCapture;

	private boolean debugCapture;
	private String debugDir;
	private boolean debugExtraction;
	private boolean debugFilter;

	/**
	 * @return the configPath
	 */
	public Path getConfigPath() {
		return configPath;
	}

	/**
	 * @param configPath
	 *            the configPath to set
	 */
	public void setConfigPath(Path configPath) {
		this.configPath = configPath;
	}

	/**
	 * @return the libPath
	 */
	public String getLibPath() {
		return libPath;
	}

	/**
	 * @param libPath
	 *            the libPath to set
	 */
	public void setLibPath(String libPath) {
		this.libPath = libPath;
	}

	public final List<String> getAccounts() {
		return accounts;
	}

	public final void setAccounts(List<String> accounts) {
		this.accounts = accounts;
	}

	public final String getActiveAccount() {
		return activeAccount;
	}

	public final void setActiveAccount(String activeAccount) {
		this.activeAccount = activeAccount;
	}

	public final List<String> getSeasons() {
		return seasons;
	}

	public final void setSeasons(List<String> seasons) {
		this.seasons = seasons;
	}

	public final String getActiveSeason() {
		return activeSeason;
	}

	public final void setActiveSeason(String activeSeason) {
		this.activeSeason = activeSeason;
	}

	public final int getDuplicateThreshold() {
		return duplicateThreshold;
	}

	public final void setDuplicateThreshold(int duplicateThreshold) {
		this.duplicateThreshold = duplicateThreshold;
	}

	public final long getCaptureInterval() {
		return captureInterval;
	}

	public final void setCaptureInterval(long captureInterval) {
		this.captureInterval = captureInterval;
	}

	public final boolean isDropDuplicateFilter() {
		return dropDuplicateFilter;
	}

	public final void setDropDuplicateFilter(boolean dropDuplicateFilter) {
		this.dropDuplicateFilter = dropDuplicateFilter;
	}

	public final String getEnginesCapture() {
		return enginesCapture;
	}

	public final void setEnginesCapture(String enginesCapture) {
		this.enginesCapture = enginesCapture;
	}

	public final boolean isDebugCapture() {
		return debugCapture;
	}

	public final void setDebugCapture(boolean debugCapture) {
		this.debugCapture = debugCapture;
	}

	public final boolean isDebugExtraction() {
		return debugExtraction;
	}

	public final void setDebugExtraction(boolean debugExtraction) {
		this.debugExtraction = debugExtraction;
	}

	public final String getDebugDir() {
		return debugDir;
	}

	public final void setDebugDir(String debugDir) {
		this.debugDir = debugDir;
	}

	public final boolean isDebugFilter() {
		return debugFilter;
	}

	public final void setDebugFilter(boolean debugFilter) {
		this.debugFilter = debugFilter;
	}
}
