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

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;

import net.sf.microlog.core.IOUtil;
import net.sf.microlog.core.Level;
import net.sf.microlog.core.MicrologConstants;
import net.sf.microlog.core.appender.AbstractAppender;

/**
 * An <code>Appender</code> that logs via UDP (Datagram) to a remote host. Each
 * logging is sent to the host at the time of logging (no buffer). The class
 * uses a <code>Datagram</code> that is re-used and filled with new data each
 * time a message is sent.
 * <p>
 * The appender can be configured with {@value #HOST_PROPERTY} for the
 * host and {@value #PORT_PROPERTY} for the port.
 * 
 * 
 * This class requires MIDP 2.0 or better.
 * 
 * Note: This was from the beginning called GPRSAppender, which was contributed
 * by Marius de Beer.
 * 
 * @author Karsten Ohme
 * @author Johan Karlsson (johan.karlsson@jayway.se)
 * @author Marius de Beer
 * @since 0.6
 */
public class DatagramAppender extends AbstractAppender {

	public static final String HOST_PROPERTY = "host";

	public static final String PORT_PROPERTY = "port";

	public static final String[] PROPERTY_NAMES = {
			DatagramAppender.HOST_PROPERTY, DatagramAppender.PORT_PROPERTY };
	
	/**
	 * The default port to be used for logging.
	 */
	public static final int DEFAULT_DATAGRAM_PORT = 1023;

	/**
	 * This is the default datagram size.
	 */
	static final int DEFAULT_DATAGRAM_SIZE = 128;

	String host = MicrologConstants.DEFAULT_HOST;

	int port = DEFAULT_DATAGRAM_PORT;

	private String encoding = "ASCII";

	private int datagramSize = DEFAULT_DATAGRAM_SIZE;
	
	protected DatagramConnection connection;

	/**
	 * This is one datagram that is used over and over again.
	 */
	private Datagram datagram;
	

	/**
	 * @see net.sf.microlog.core.appender.AbstractAppender#open()
	 */
	public void open() throws IOException {

		StringBuffer connectionStringBuffer = new StringBuffer(32);
		connectionStringBuffer.append("datagram://");
		connectionStringBuffer.append(host);
		connectionStringBuffer.append(':');
		connectionStringBuffer.append(port);
		connection = (DatagramConnection) Connector.open(connectionStringBuffer
				.toString());
		logOpen = true;

	}

	/**
	 * Do the logging.
	 * 
	 * @param level
	 *            the level to use for the logging.
	 * @param message
	 *            the message to log.
	 * @param t
	 *            the exception to log.
	 */
	public void doLog(String clientID, String name, long time, Level level,
			Object message, Throwable t) {
		if (logOpen && formatter != null) {
			String logMessage = formatter.format(clientID, name, time, level,
					message, t);
			sendMessage(logMessage);
		}
	}

	/**
	 * Send the message to the defined host.
	 * 
	 * @param message
	 *            the message to send.
	 */
	protected void sendMessage(String message) {
		try {
			if (datagram == null) {
				datagram = connection.newDatagram(datagramSize);
			}

			datagram.setData(message.getBytes(encoding), 0, message.length());
			connection.send(datagram);
		} catch (IOException e) {
			System.err.println("Could not send the Datagram: " + e);
		}
	}

	/**
	 * No Effect
	 * 
	 * @see net.sf.microlog.core.Appender#clear()
	 */
	public void clear() {
	}

	/**
	 * @see net.sf.microlog.core.appender.AbstractAppender#close()
	 * @throws IOException
	 *             if the close operation failed.
	 */
	public void close() throws IOException {
		IOUtil.closeSilent(connection);
		logOpen = false;
	}

	/**
	 * Get the size of the log. The size is the number of items logged.
	 * 
	 * @return the size of the log.
	 */
	public long getLogSize() {
		return SIZE_UNDEFINED;
	}

	/**
	 * Set the host to connect to. Note that this is the host without the port
	 * number.
	 * 
	 * @param host
	 *            the host to set
	 * @throws IllegalArgumentException
	 *             if the <code>host</code> is null.
	 * 
	 */
	public void setHost(String host) throws IllegalArgumentException {
		if (host == null) {
			throw new IllegalArgumentException("The host must not be null.");
		}

		this.host = host;
	}
	
	/**
	 * Get the host to connect to.
	 * 
	 * @return the host to connect to.
	 */
	public String getHost(){
		return this.host;
	}

	/**
	 * Set the port that is used for the connection.
	 * 
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Set the encoding to be used when creating the byte array data that is
	 * sent in the <code>Datagram</code>.
	 * 
	 * @param encoding
	 *            the encoding to set
	 * @throws IllegalArgumentException
	 *             if the encoding is null or the length of the encoding
	 *             <code>String</code> is shorter than 1.
	 */
	public void setEncoding(String encoding) throws IllegalArgumentException {
		if (encoding == null || (encoding != null && encoding.length() < 1)) {
			throw new IllegalArgumentException(
					"The encoding must not be null and the length greater than 1");
		}

		this.encoding = encoding;
	}

	/**
	 * Set the <code>DatagramConnection</code> to be used. The connection must
	 * be open. If the log is open this call is ignored.
	 * 
	 * This should only be used for testing purposes.
	 * 
	 * @param connection
	 *            the connection to set
	 */
	void setConnection(DatagramConnection connection) {
		if (!logOpen) {
			this.connection = connection;
		}
	}

	public String[] getPropertyNames() {
		return DatagramAppender.PROPERTY_NAMES;
	}

	public void setProperty(String name, String value)
			throws IllegalArgumentException {

		if (name.equals(HOST_PROPERTY)) {
			setHost(value);
		} else if (name.equals(PORT_PROPERTY)) {
			setPort(Integer.parseInt(value));
		}
	}

}
