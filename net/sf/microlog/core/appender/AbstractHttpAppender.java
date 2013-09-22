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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import net.sf.microlog.core.Appender;
import net.sf.microlog.core.Level;

/**
 * The <code>AbstractHttpAppender</code> contains everything that is common for
 * all HTTP appenders.
 * 
 * @author Johan Karlsson (johan.karlsson@jayway.se)
 * @since 2.0
 */
public abstract class AbstractHttpAppender extends AbstractAppender {

	/**
	 * Create an <code>AbstractHttpAppender</code>.
	 */
	public AbstractHttpAppender() {
		super();

	}

	/**
	 * The URL to post the logging to.
	 */
	protected String postURL;

	/**
	 * Set the URL that is used for posting the messages to the server.
	 * 
	 * @param postURL
	 *            the postURL to set
	 * @throws IllegalArgumentException
	 *             if the <code>postURl</code> is <code>null</code>.
	 */
	public void setPostURL(String postURL) throws IllegalArgumentException {
		if (postURL == null) {
			throw new IllegalArgumentException("The postURL must not be null.");
		}

		this.postURL = postURL;
	}

	/**
	 * @see net.sf.microlog.core.appender.AbstractAppender#open()
	 */
	public void open() throws IOException {
		logOpen = true;
	}

	/**
	 * @see net.sf.microlog.core.appender.AbstractAppender#clear()
	 */
	public void clear() {
		// Not supported
	}

	/**
	 * @see net.sf.microlog.core.appender.AbstractAppender#close()
	 */
	public void close() throws IOException {
		logOpen = false;
	}

	/**
	 * @see net.sf.microlog.core.appender.AbstractAppender#doLog(java.lang.String,
	 *      java.lang.String, long, net.sf.microlog.core.Level,
	 *      java.lang.Object, java.lang.Throwable)
	 */
	public void doLog(String clientID, String name, long time, Level level,
			Object message, Throwable t) {

		if (logOpen && formatter != null) {
			try {
				OutputStream outputStream = connect();	
				PrintStream printStream = new PrintStream(outputStream);

				String logString = formatter.format(clientID, name, time,
						level, message, t);
				
				printStream.print(logString);

				getResponse();

				disconnect();

			} catch (IOException e) {
				System.err.println("Failed to write log to server " + e);
			}

		}
	}

	abstract protected OutputStream connect() throws IOException;

	abstract protected InputStream getResponse() throws IOException;

	abstract protected void disconnect() throws IOException;

	/**
	 * @see net.sf.microlog.core.Appender#getLogSize()
	 */
	public long getLogSize() {
		return Appender.SIZE_UNDEFINED;
	}

}
