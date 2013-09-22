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

package net.sf.microlog.midp.appender;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import net.sf.microlog.core.Appender;
import net.sf.microlog.core.IOUtil;
import net.sf.microlog.core.appender.AbstractHttpAppender;

/**
 * This class uses the HTTP protocol to post the log messages to a server.
 * 
 * @author Johan Karlsson (johan.karlsson@jayway.se)
 */
public class HttpAppender extends AbstractHttpAppender {

	public static final String APPENDER_PROPERTY = "postURL";
	private static final String[] PROPERTY_NAMES = { APPENDER_PROPERTY };

	private HttpConnection connection;

	/**
	 * Create a <code>HttpConnectionAppender</code>
	 */
	public HttpAppender() {
		super();
	}

	/**
	 * Clear the log, i.e. do nothing since this is not applicable.
	 * 
	 * @see net.sf.microlog.core.appender.AbstractAppender#clear()
	 */
	public void clear() {
		// Do nothing
	}

	/**
	 * Open the log.
	 * 
	 * @see net.sf.microlog.core.appender.AbstractAppender#open()
	 */
	public void open() throws IOException {
		super.open();
	}

	/**
	 * Close the log.
	 * 
	 * @see net.sf.microlog.core.appender.AbstractAppender#close()
	 */
	public void close() throws IOException {
		IOUtil.closeSilent(connection);
		super.close();
	}

	protected OutputStream connect() throws IOException {
		connection = (HttpConnection) Connector.open(postURL);
		connection.setRequestMethod(HttpConnection.POST);
		return connection.openOutputStream();
	}

	protected InputStream getResponse() throws IOException {
		int responseCode = connection.getResponseCode();
		if (responseCode != HttpConnection.HTTP_OK) {
			throw new IOException("HTTP response code: " + responseCode);
		}
		return connection.openInputStream();
	}

	/**
	 * Discconnect from the server.
	 */
	protected void disconnect() throws IOException {
		connection.close();
	}

	/**
	 * @see Appender#getPropertyNames()
	 */
	public String[] getPropertyNames() {
		return PROPERTY_NAMES;
	}

	/**
	 * @see Appender#setProperty(String, String)
	 */
	public void setProperty(String name, String value) {
		if (name.equals(APPENDER_PROPERTY)) {
			this.setPostURL(value);
		}
	}

}
