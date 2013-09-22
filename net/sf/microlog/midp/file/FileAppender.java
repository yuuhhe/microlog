/*
 * Copyright 2008 The Microlog project @sourceforge.net
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
package net.sf.microlog.midp.file;

import java.io.IOException;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import net.sf.microlog.core.IOUtil;
import net.sf.microlog.core.appender.AbstractFileAppender;

/**
 * A class that logs to a file. The class uses the FileConnection API from
 * JSR-75.
 * <p>
 * 
 * The file name can be passed with the property
 * <code>microlog.appender.FileAppender.filename</code>.
 * 
 * The directory can be passed with the property
 * <code>microlog.appender.FileAppender.directory</code>
 * 
 * The directory is possible to set with the <code>setDirectory()</code> method.
 * If this is not set the default directory is used. The default directory is
 * fetched by calling <code>FileSystemRegistry.listRoots()</code>, where the
 * first root is used.
 * 
 * @author Johan Karlsson
 * @author Karsten Ohme
 * @since 0.1
 */
public class FileAppender extends AbstractFileAppender {

	/**
	 * The protocol to be used for opening a <code>FileConnection</code> object.
	 */
	public static final String FILE_PROTOCOL = "file:///";

	/**
	 * The <code>FileConnection</code> for accessing the log file.
	 */
	protected FileConnection fileConnection;

	public FileAppender() {
		super();
	}

	/**
	 * Create the <code>fileURI</code> to be used as a log file.
	 * 
	 * @return the <code>fileURI</code>.
	 */
	protected String createFileURI() {
		StringBuffer fileURIStringBuffer = new StringBuffer(
				DEFAULT_STRING_BUFFER_SIZE);
		fileURIStringBuffer.append(FILE_PROTOCOL);

		boolean fileNameContainsPath = (fileName.indexOf('/') != -1)
				|| (fileName.indexOf('\\') != -1);

		if (!fileNameContainsPath) {
			setDirectoryAsFirstRoot();
		}

		if (directory != null) {
			fileURIStringBuffer.append(directory);
		}

		fileURIStringBuffer.append(fileName);
		String fileURI = fileURIStringBuffer.toString();
		return fileURI;
	}

	/**
	 * Set the <code>directory</code> member variable to the first directory
	 * found by <code>FileSystemRegistry.listRoots()</code>.
	 */
	private void setDirectoryAsFirstRoot() {
		try {
			Enumeration rootsEnum = FileSystemRegistry.listRoots();

			if (rootsEnum.hasMoreElements()) {
				directory = (String) rootsEnum.nextElement();
			} else {
				System.err.println("No root directory is found.");
			}

		} catch (SecurityException e) {
			System.err.println("Not allowed to list the roots. " + e);
		}
	}

	/**
	 * Create the file from the specified <code>fileURI</code>. If the file
	 * already exists, no file is created.
	 * 
	 * @param fileURI
	 *            the <code>fileURI</code> to use for creation.
	 * @throws IOException
	 *             if the creation fails.
	 */
	protected void createFile(String fileURI) throws IOException {
		fileConnection = (FileConnection) Connector.open(fileURI,
				Connector.READ_WRITE);
		if (!fileConnection.exists()) {
			fileConnection.create();
		}

		System.out.println("The created file is " + fileConnection.getURL());
	}

	/**
	 * Open the <code>OutputStream</code> for the created file. The member
	 * variable <code>outputStream</code> shall be set after this method has
	 * been called.
	 * 
	 * @throws IOException
	 */
	protected synchronized void openOutputStream() throws IOException {
		if (fileConnectionIsSet) {
			outputStream = fileConnection.openOutputStream(fileConnection.fileSize());
			logOpen = true;
		}
	}

	/**
	 * @see net.sf.microlog.core.appender.AbstractAppender#clear()
	 */
	public synchronized void clear() {
		if (fileConnection != null && fileConnection.isOpen()) {
			try {
				fileConnection.truncate(0);
			} catch (IOException e) {
				System.err.println("Failed to clear the log " + e);
			}
		}
	}

	/**
	 * @see net.sf.microlog.core.appender.AbstractAppender#close()
	 */
	public synchronized void close() throws IOException {
		if (logOpen) {
			IOUtil.closeSilent(outputStream);
			IOUtil.closeSilent(fileConnection);
			logOpen = false;
		}
	}

	/**
	 * Get the size of the log. This is equivalent of calling
	 * <code>fileSize()</code> on the created <code>FileConnection</code>.
	 * 
	 * @return the size of the log.
	 */
	public synchronized long getLogSize() {

		long logSize = SIZE_UNDEFINED;

		if (logOpen) {
			try {
				outputStream.flush();
				logSize = fileConnection.fileSize();
			} catch (IOException e) {
				System.err.println("Failed to get the logsize " + e);
			}
		}

		return logSize;
	}

	/**
	 * Get the total size. The total size is fetched by calling
	 * <code>totalSize()</code> on the created <code>FileConnection</code>.
	 * 
	 * @return the total size of the file system the connection's target resides
	 *         on.
	 */
	public synchronized long totalSize() {
		long totalSize = SIZE_UNDEFINED;

		if (logOpen) {
			try {
				outputStream.flush();
				totalSize = fileConnection.totalSize();
			} catch (IOException e) {
				System.err.println("Failed to get the total size." + e);
			}
		}

		return totalSize;
	}

	/**
	 * Get the used size. The total size is fetched by calling
	 * <code>usedSize()</code> on the created <code>FileConnection</code>.
	 * 
	 * @return Determines the used memory of a file system the connection's
	 *         target resides on. This may only be an estimate and may vary
	 *         based on platform-specific file system blocking and metadata
	 *         information.
	 */
	public synchronized long usedSize() {
		long usedSize = SIZE_UNDEFINED;

		if (logOpen) {
			try {
				outputStream.flush();
				usedSize = fileConnection.usedSize();
			} catch (IOException e) {
				System.err.println("Failed to get the total size. " + e);
			}
		}

		return usedSize;
	}

	/**
	 * Get the URL of the file that is opened, i.e. a call is made to
	 * <code>getURL()</code> on the opened <code>FileConnection</code>.
	 * 
	 * @return the URL of the opened connection. If no connection is opened, an
	 *         empty <code>String</code> is returned.
	 */
	public synchronized String getURL() {
		String url = "";

		if (fileConnection != null) {
			url = fileConnection.getURL();
		}

		return url;
	}

	/**
	 * @param fileConnection
	 *            the fileConnection to set
	 */
	synchronized void setFileConnection(FileConnection fileConnection) {
		this.fileConnection = fileConnection;
		if (this.fileConnection != null) {
			fileConnectionIsSet = true;
		} else {
			fileConnectionIsSet = false;
		}
	}

}
