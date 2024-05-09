package com.kgm.common.utils;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class SimpleFilter extends FileFilter {
	private String extension;
	private String description;

	public SimpleFilter(String extension, String description) {
		this.extension = "." + extension.toLowerCase();
		this.description = description;
	}

	public boolean accept(File f) {
		if (f == null)
			return false;
		if (f.isDirectory())
			return true;
		return f.getName().toLowerCase().endsWith(extension);
	}

	public String getDescription() {
		return description;
	}
}
