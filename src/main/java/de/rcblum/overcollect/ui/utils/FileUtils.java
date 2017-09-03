package de.rcblum.overcollect.ui.utils;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

	public static class OWFileFilter extends FileFilter {

		String ext = null;

		public OWFileFilter(String ext) {
			this.ext = ext;
		}

		@Override
		public boolean accept(File f) {
			if (f == null) {
				LOGGER.info(f.toString());
			}
			return (f != null && f.toString().endsWith(this.ext)) || (f != null && f.isDirectory()) ? true : false;
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
