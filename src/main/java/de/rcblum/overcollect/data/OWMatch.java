package de.rcblum.overcollect.data;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.rcblum.overcollect.utils.Helper;

public class OWMatch {
	private static final Logger LOGGER = LoggerFactory.getLogger(OWMatch.class);

	public enum Result {
		VICTORY, DEFEAT, DRAW;
	}

	// private static final DateTimeFormatter dateTimeFormatter =
	// DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

	public static OWMatch fromJsonFile(File jFile) {
		OWMatch match = null;
		Gson g = new Gson();
		try {
			String text = new String(Files.readAllBytes(jFile.toPath()), StandardCharsets.UTF_8);
			match = g.fromJson(text, OWMatch.class);
		} catch (IOException e) {
			LOGGER.error("Exception: ", e);
		}
		return match;
	}

	public static void toJsonFile(OWMatch m, File jFile) {
		Gson g = new GsonBuilder().setPrettyPrinting().create();
		try {
			Files.write(jFile.toPath(), g.toJson(m).getBytes("UTF-8"));
		} catch (IOException e) {
			LOGGER.error("Exception: ", e);
		}
	}

	private String matchId = null;

	private String account = null;

	private String season = null;

	private String startTime = null;

	private String endTime = null;

	private Result result = Result.VICTORY;

	private String teamSr = null;

	private String enemySr = null;

	private String sr = null;

	private String map = null;

	private int stacksize = 1;

	private List<OWCharacterStats> characterStats = new LinkedList<>();

	public OWMatch(String matchId) {
		this.matchId = matchId;
	}

	public void addCharacterStats(OWCharacterStats cStats) {
		this.characterStats.add(cStats);
	}

	public String getAccount() {
		return account;
	}

	public String getSeason() {
		return season;
	}

	public List<OWCharacterStats> getCharacterStats() {
		return characterStats;
	}

	public Date getEndTime() {
		try {
			return getMyDateFormat().parse(this.endTime);
		} catch (ParseException | NullPointerException e) {
			LOGGER.error("Exception: ", e);
		}
		return null;
	}

	public String getEnemySr() {
		return enemySr;
	}

	public String getMap() {
		return map;
	}

	public String getMatchId() {
		return matchId;
	}

	public Result getResult() {
		return result;
	}

	public int getSr() {
		if (Helper.isInteger(this.sr))
			return Integer.valueOf(this.sr);
		return -1;
	}

	public int getStacksize() {
		return stacksize;
	}

	private static SimpleDateFormat getMyDateFormat() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	public Date getStartTime() {
		try {
			return getMyDateFormat().parse(this.startTime);
		} catch (ParseException | NullPointerException e) {
			LOGGER.error("Exception: ", e);
		}
		return null;
	}

	public String getTeamSr() {
		return teamSr;
	}

	public boolean isDefeat() {
		return result == Result.DEFEAT;
	}

	public boolean isDraw() {
		return result == Result.DRAW;
	}

	public boolean isVictory() {
		return result == Result.VICTORY;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public void setSeason(String season) {
		this.season = season;
	}

	public void setCharacterStats(List<OWCharacterStats> characterStats) {
		this.characterStats = characterStats;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public void setEnemySr(String enemySr) {
		this.enemySr = enemySr;
	}

	public void setMap(String map) {
		this.map = map;
	}

	public void setMatchId(String matchId) {
		this.matchId = matchId;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public void setSr(String sr) {
		this.sr = sr;
	}

	public void setStacksize(int stacksize) {
		this.stacksize = stacksize;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public void setTeamSr(String teamSr) {
		this.teamSr = teamSr;
	}

	public String toJson() {
		Gson g = new GsonBuilder().setPrettyPrinting().create();
		return g.toJson(this);
	}
}
