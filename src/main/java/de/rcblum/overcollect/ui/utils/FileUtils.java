package de.rcblum.overcollect.ui.utils;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

	private FileUtils() {
	}

	public static class OWFileFilter extends FileFilter {

		String ext = null;

		public OWFileFilter(String ext) {
			this.ext = ext;
		}

		@Override
		public boolean accept(File file) {
			if (file == null) {
				LOGGER.info("File is null.");
			}
			return (file != null && file.toString().endsWith(this.ext)) || (file != null && file.isDirectory()) ? true
					: false;
		}

		@Override
		public String getDescription() {
			return "Spreadsheet Files (*" + this.ext + ")";
		}

		public String getExtension() {
			return ext;
		}

	}
}
