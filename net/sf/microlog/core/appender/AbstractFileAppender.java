/*
 * Copyright 2009 The Microlog project @sourceforge.net
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.microlog.core.appender;

import java.io.IOException;
import java.io.OutputStream;

import net.sf.microlog.core.Level;

/**
 * This is the abstract class that is common for all platforms. It uses template
 * methods to force the inherited classes to implement them.
 * 
 * @author Johan Karlsson (johan.karlsson@jayway.se)
 * @since 2.0
 * 
 */
public abstract class AbstractFileAppender extends AbstractAppender {

	public static final String FILE_NAME_PROPERTY = "filename";

	public static final String LINE_SEPARATOR_PROPERTY = "lineseparator";

	/**
	 * The default log filename.
	 */
	public static final String DEFAULT_FILENAME = "microlog.txt";

	public static final String DEFAULT_LINE_SEPARATOR = "\r\n";

	/**
	 * The default buffer size for <code>StringBuffer</code> objects.
	 */
	public static final int DEFAULT_STRING_BUFFER_SIZE = 256;

	public static final String[] PROPERTY_NAMES = { FILE_NAME_PROPERTY,
			LINE_SEPARATOR_PROPERTY };

	public static final String[] DEFAULT_VALUES = { DEFAULT_FILENAME,
			DEFAULT_LINE_SEPARATOR };

	protected String lineSeparator = System.getProperty("line.separator");

	protected String directory;

	protected String fileName = DEFAULT_FILENAME;

	protected OutputStream outputStream;

	protected boolean fileConnectionIsSet = false;
	
	
	/**
	 * @see net.sf.microlog.core.appender.AbstractAppender#open()
	 */
	public synchronized void open() throws IOException {

		if (!fileConnectionIsSet) {
			String fileURI = createFileURI();
			createFile(fileURI);

			fileConnectionIsSet = true;
		}

		openOutputStream();

		logOpen = true;
	}

	abstract protected String createFileURI();

	abstract protected void createFile(String fileURI) throws IOException;

	abstract protected void openOutputStream() throws IOException;

	/**
	 * @see net.sf.microlog.core.appender.AbstractAppender#clear()
	 */
	abstract public void clear();

	/**
	 * @see net.sf.microlog.core.appender.AbstractAppender#close()
	 */
	abstract public void close() throws IOException;

	/**
	 * @see net.sf.microlog.core.Appender#doLog(String, String, long, Level,
	 *      Object, Throwable)
	 */
	public synchronized void doLog(String clientID, String name, long time,
			Level level, Object message, Throwable t) {
		
		if (logOpen && formatter != null) {
			String logString = formatter.format(clientID, name, time, level,
					message, t);
			try {
				byte[] stringData = logString.getBytes();
				outputStream.write(stringData);
				if(lineSeparator == null){
					lineSeparator = DEFAULT_LINE_SEPARATOR;
				}
				outputStream.write(lineSeparator.getBytes());
				outputStream.flush();
			} catch (IOException e) {
				System.err.println("Failed to log message " + e);
			}
		}
	}

	/**
	 * @see net.sf.microlog.core.Appender#getLogSize()
	 */
	abstract public long getLogSize();

	/**
	 * Get the filename of the logfile.
	 * 
	 * @return the fileName
	 */
	public synchronized String getFileName() {
		return fileName;
	}

	/**
	 * Set the filename of the logfile. It could be the full path of the file,
	 * like "C:/other/microlog.txt" or only the filename. If only the filename
	 * is specified, the first directory of the <code>Enumeration</code> from a
	 * call to <code>FileSystemRegistry.listRoots()</code> is used.
	 * 
	 * Note that changing this after the logfile has been opened has no effect.
	 * 
	 * @param fileName
	 *            the fileName to set
	 * @throws IllegalArgumentException
	 *             if the filename is null.
	 */
	public synchronized void setFileName(String fileName)
			throws IllegalArgumentException {
		if (fileName == null) {
			throw new IllegalArgumentException("The filename must not be null.");
		}

		this.fileName = fileName;
	}

	/**
	 * Get the line separator.
	 * 
	 * @return the lineSeparator
	 */
	public synchronized String getLineSeparator() {
		return lineSeparator;
	}

	/**
	 * Set the line separator.
	 * 
	 * @param lineSeparator
	 *            the lineSeparator to set
	 */
	public synchronized void setLineSeparator(String lineSeparator)
			throws IllegalArgumentException {
		if (lineSeparator == null) {
			throw new IllegalArgumentException(
					"The line separator must not be null.");
		}

		this.lineSeparator = lineSeparator;
	}

	public String[] getPropertyNames() {
		return PROPERTY_NAMES;
	}

	public void setProperty(String name, String value)
			throws IllegalArgumentException {
		super.setProperty(name, value);

		if (name.equals(AbstractFileAppender.FILE_NAME_PROPERTY)) {
			setFileName(value);
		}else if(name.equals(LINE_SEPARATOR_PROPERTY)){
			setLineSeparator(value);
		}
	}

}
