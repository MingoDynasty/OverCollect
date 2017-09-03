package de.rcblum.overcollect.misc;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.rcblum.overcollect.configuration.OWItem;
import de.rcblum.overcollect.configuration.OWLib;

public class Rescaler {
	private static final Logger LOGGER = LoggerFactory.getLogger(Rescaler.class);

	public static void main(String[] args) {
		rescale1080pTo1440p();
	}

	public static void rescale1080pTo1440p() {
		List<OWItem> items = OWLib.getInstance().getItems("1920x1080");
		float rescale = 1440f / 1080f;
		LOGGER.info("rescale: {}", rescale);
		for (OWItem owItem : items) {
			owItem.rescale(rescale);
		}
	}
}
