/*******************************************************************************
 * Copyright (c) 2015 Eclipse Foundation and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mikaël Barbero - initial implementation
 *******************************************************************************/
package org.eclipse.cbi.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * Class with utilities method for {@link Properties} objects.
 */
public class PropertiesReader {

	private final Properties properties;
	private final FileSystem fileSystem;

	/**
	 * Load the file at the given path as a property file.
	 * 
	 * @param path
	 *            the file to load
	 * @return a new {@link Properties} object.
	 * @throws IOException
	 *             if path can not be read or properties can not be loaded.
	 */
	public PropertiesReader(Properties properties, FileSystem fs) throws IOException {
		this.properties = properties;
		this.fileSystem = fs;
	}

	public static PropertiesReader create(Path path) throws IOException {
		Properties properties = new Properties();
		try (InputStream fis = new BufferedInputStream(Files.newInputStream(path))) {
			properties.load(fis);
		}
		return new PropertiesReader(properties, path.getFileSystem());
	}
	
	/**
	 * Returns the property value with key {@code propertyName} or the
	 * {@code defaultValue} if the key can not be found in the properties.
	 * 
	 * @param propertyName
	 *            the key of the property to get
	 * @param defaultValue
	 *            the default value if no value is associated with the key.
	 * @return the property value with key {@code propertyName} or the
	 *         {@code defaultValue} if the key can not be found in the
	 *         properties.
	 */
	public String getString(String propertyName, String defaultValue) {
		Objects.requireNonNull(defaultValue);
		final String propertyValue = properties.getProperty(propertyName);
		if (propertyValue == null) {
			return defaultValue;
		} else {
			return propertyValue;
		}
	}

	/**
	 * Returns the property value with key {@code propertyName}. If the
	 * key can not be found in the properties, it will throw an
	 * {@link IllegalStateException}.
	 * 
	 * @param propertyName
	 *            the key of the property to get
	 * @return the property value with key {@code propertyName}
	 * @throws IllegalStateException
	 *             If the key can not be found in the properties
	 */
	public String getString(String propertyName) {
		final String propertyValue = properties.getProperty(propertyName);
		if (propertyValue == null) {
			throw new IllegalStateException("Property '" + propertyName + "' is mandatory.");
		} else {
			return propertyValue;
		}
	}

	public Path getPath(String propertyName, String defaultValue) {
		return fileSystem.getPath(getString(propertyName, Objects.requireNonNull(defaultValue)).trim());
	}
	
	public Path getPath(String propertyName) {
		return fileSystem.getPath(getString(propertyName).trim());
	}
	
	public long getLong(String propertyName) {
		String propertyValue = getString(propertyName);
		try {
			return Long.parseLong(propertyValue);
		} catch (NumberFormatException e) {
			throw new IllegalStateException("Property '" + propertyName + "' must be a valid long integer (currently '" + propertyValue + "'");
		}
	}
	
	public long getLong(String propertyName, long defaultValue) {
		final String propertyValue = properties.getProperty(propertyName);
		if (propertyValue == null) {
			return defaultValue;
		} else {
			try {
				return Long.parseLong(propertyValue);
			} catch (NumberFormatException e) {
				throw new IllegalStateException("Property '" + propertyName + "' must be a valid long integer (currently '" + propertyValue + "'");
			}
		}
	}
	
	public int getInt(String propertyName) {
		String propertyValue = getString(propertyName);
		try {
			return Integer.parseInt(propertyValue);
		} catch (NumberFormatException e) {
			throw new IllegalStateException("Property '" + propertyName + "' must be a valid integer (currently '" + propertyValue + "'");
		}
	}
	
	public int getInt(String propertyName, int defaultValue) {
		final String propertyValue = properties.getProperty(propertyName);
		if (propertyValue == null) {
			return defaultValue;
		} else {
			try {
				return Integer.parseInt(propertyValue);
			} catch (NumberFormatException e) {
				throw new IllegalStateException("Property '" + propertyName + "' must be a valid integer (currently '" + propertyValue + "'");
			}
		}
	}
	
	/**
	 * Returns the path from property value with key {@code propertyName}. If
	 * the key can not be found in the properties or if the associated path
	 * doesn't exist, it will throw an {@link IllegalStateException}.
	 * 
	 * @param propertyName
	 *            the key of the property to get
	 * @return the property value with key {@code propertyName}
	 * @throws IllegalStateException
	 *             If the key can not be found in the properties or if the
	 *             associated path doesn't exist
	 */
	public Path getRegularFile(String propertyName) {
		Path path = getPath(propertyName);
		if (!Files.isRegularFile(path)) {
			throw new IllegalStateException("Property '" + propertyName + "' does not reference an existing regular file '" + path.toString() + "'");
		}
		return path;
	}
	
	public String getFileContent(String propertyName) {
		Path path = getRegularFile(propertyName);
		
		try {
			byte[] fileContents = Files.readAllBytes(path);
			return new String(fileContents, StandardCharsets.UTF_8).trim();
		} catch (IOException e) {
			throw new IllegalStateException("File '" + path + "' can't be read", e);
		}
	}

	public boolean getBoolean(String propertyName, boolean defaultValue) {
		final String propertyValue = properties.getProperty(propertyName);
		if (propertyValue == null) {
			return defaultValue;
		} else {
			return Boolean.valueOf(propertyValue);
		}
	}
	
	/**
	 * Returns a copy of the all the read properties
	 * @return
	 */
	public Map<String, String> toMap() {
		Builder<String, String> ret = ImmutableMap.builder();
		properties.entrySet().forEach(e -> ret.put(e.getKey().toString(), e.getValue().toString()));
		return ret.build();
	}
}
