package de.rcblum.overcollect.utils;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Helper {

	private static final Logger LOGGER = LoggerFactory.getLogger(Helper.class);

	// TODO: not used?
	// private final SimpleDateFormat simpleDateFormat = new
	// SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static BufferedImage copy(BufferedImage img) {
		try {
			int newW = img.getWidth();
			int newH = img.getHeight();
			Image tmp = img;
			BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = dimg.createGraphics();
			g2d.drawImage(tmp, 0, 0, null);
			g2d.dispose();
			return dimg;
		} catch (Exception e) {
			LOGGER.error("Exception: ", e);
		}
		return null;
	}

	public static boolean isInteger(String val) {
		return val != null && val.matches("^-?\\d+$");
	}

	public static int toInteger(String value, int defaultValue) {
		return isInteger(value) ? Integer.valueOf(value) : defaultValue;
	}

	public static String insertBeforUpeprcase(String searched, String insert) {
		Objects.requireNonNull(searched);
		Objects.requireNonNull(insert);
		StringBuilder result = new StringBuilder();
		char[] chars = searched.toCharArray();
		for (char c : chars) {
			if (Character.isUpperCase(c))
				result.append(insert);
			result.append(c);
		}
		return result.toString();
	}

	/**
	 * @return the simpleDateFormatFile
	 */
	public static SimpleDateFormat getSimpleDateFormatFile() {
		return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	}
}
