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

import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

import net.sf.microlog.core.Appender;
import net.sf.microlog.core.IOUtil;
import net.sf.microlog.core.Level;
import net.sf.microlog.core.appender.AbstractAppender;

/**
 * This appender writes to a socket, using a <code>SocketConnection</code> or a
 * <code>SecureSocketConnection</code>. The log data itself is written using
 * <code>DataOutputStream</code> and the method <code>writeUTF()</code>.
 * 
 * This class requires MIDP 2.0 or better.
 * 
 * @author Johan Karlsson (johan.karlsson@jayway.se)
 * @since 0.6
 */
public class SocketAppender extends AbstractAppender {

	public static final String DEFAULT_SERVER_NAME = "127.0.0.1";
	public static final int DEFAULT_SOCKET_PORT = 1234;
	public static final int DEFAULT_LINGER_TIME = 10;
	public static final String SOCKET_PROTOCOL_STRING = "socket://";
	public static final String SECURE_SOCKET_PROTOCOL_STRING = "ssl://";

	public static final String SERVER_NAME_PROPERTY = "serverName";

	public static final String SOCKET_PORT_PROPERTY = "socketPort";

	public static final String[] PROPERTY_NAMES = {
			SocketAppender.SERVER_NAME_PROPERTY,
			SocketAppender.SOCKET_PORT_PROPERTY };

	private String protocol = SOCKET_PROTOCOL_STRING;

	private String serverName = SocketAppender.DEFAULT_SERVER_NAME;
	private int port = DEFAULT_SOCKET_PORT;
	private int linger = DEFAULT_LINGER_TIME;

	private SocketConnection socketConnection;
	private DataOutputStream dataOutputStream;

	/**
	 * @see net.sf.microlog.core.appender.AbstractAppender#clear()
	 */
	public void clear() {
		// Do nothing, since we are not able to clear the log on the serverside.
	}

	/**
	 * Close the log.
	 * 
	 * @see net.sf.microlog.core.appender.AbstractAppender#close()
	 */
	public synchronized void close() throws IOException {
		IOUtil.closeSilent(dataOutputStream);
		IOUtil.closeSilent(socketConnection);
		logOpen = false;
	}

	/**
	 * Do the actual loagging.
	 * 
	 * @see net.sf.microlog.core.appender.AbstractAppender#doLog(String, String,
	 *      long, net.sf.microlog.core.Level, java.lang.Object,
	 *      java.lang.Throwable)
	 */
	public synchronized void doLog(String clientID, String name, long time,
			Level level, Object message, Throwable t) {
		if (logOpen && dataOutputStream != null && formatter != null) {
			try {
				dataOutputStream.writeUTF(formatter.format(clientID, name,
						time, level, message, t));
			} catch (IOException e) {
				logOpen = false;
				System.err.println("Could not write data to server " + e
						+ " => closing the log");
			}
		}
	}

	/**
	 * @see net.sf.microlog.core.appender.AbstractAppender#open()
	 */
	public synchronized void open() throws IOException {

		if (socketConnection == null && serverName != null) {
			String connectionString = protocol + serverName + ':' + port;

			socketConnection = (SocketConnection) Connector
					.open(connectionString);
			socketConnection.setSocketOption(SocketConnection.LINGER, linger);
			dataOutputStream = socketConnection.openDataOutputStream();
			logOpen = true;

		}
	}
	

	/**
	 * Get the log size which in this case is <code>SIZE_UNDEFINED</code>.
	 * 
	 * @see net.sf.microlog.core.Appender#getLogSize()
	 */
	public long getLogSize() {
		return SIZE_UNDEFINED;
	}

	/**
	 * Get the protocol to be used for connection. This could be "socket://" or
	 * "ssl://".
	 * 
	 * @return the protocol to be used.
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * Get the protocol to be used for connection.
	 * 
	 * Use one of the constants: SOCKET_PROTOCOL_STRING or
	 * SECURE_SOCKET_PROTOCOL_STRING. This is ignored if the log is open, or the
	 * protocol is null, or the protocol is not allowed.
	 * 
	 * @param protocol
	 *            the protocol to set.
	 * @throws IllegalArgumentException
	 *             if the <code>protocol</code> is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if the <code>protocol</code> is other than "http://" or
	 *             "https://".
	 */
	public void setProtocol(String protocol) throws IllegalArgumentException {
		if (protocol == null) {
			throw new IllegalArgumentException("The protocol must not be null.");
		}

		if ((protocol.compareTo(SOCKET_PROTOCOL_STRING) == 0)
				|| (protocol.compareTo(SECURE_SOCKET_PROTOCOL_STRING) == 0)) {
			throw new IllegalArgumentException(
					"The protocol must be http:// or https://");
		}

		if (!logOpen) {
			this.protocol = protocol;
		}
	}

	/**
	 * Get the port that is used for the socket.
	 * 
	 * @return the port that is used for the socket.
	 */
	public synchronized int getPort() {
		return port;
	}

	/**
	 * Get the port that is used for the socket.
	 * 
	 * @param port
	 *            the port to set
	 */
	public synchronized void setPort(int port) {
		if (!logOpen) {
			this.port = port;
		}
	}

	/**
	 * Get the socket option <code>SocketConnection.LINGER</code>.
	 * 
	 * @return the linger option. If the socket connection is not open, this
	 *         valid is not valid.
	 */
	public synchronized int getLinger() {
		if (socketConnection != null) {
			try {
				linger = socketConnection
						.getSocketOption(SocketConnection.LINGER);
			} catch (IllegalArgumentException e) {
				System.err
						.println("Failed to get the socket option SocketConnection.LINGER "
								+ e.getMessage());
			} catch (IOException e) {
				System.err
						.println("Failed to get the socket option SocketConnection.LINGER "
								+ e.getMessage());
			}
		}

		return linger;
	}

	/**
	 * Set the socket option <code>SocketConnection.LINGER</code>. This is
	 * ignored if the log is open.
	 * 
	 * @param linger
	 *            the linger to set
	 * @see SocketConnection#LINGER
	 */
	public synchronized void setLinger(int linger) {
		if (!logOpen) {
			this.linger = linger;
		}
	}

	/**
	 * Get server name.
	 * 
	 * @return the serverName
	 */
	public synchronized String getServerName() {
		return serverName;
	}

	/**
	 * Set the server name. This is ignored if the log is open.
	 * 
	 * @param serverName
	 *            the serverName to set
	 * @throws IllegalArgumentException
	 *             if the <code>serverName</code> is <code>null</code>.
	 */
	public synchronized void setServerName(String serverName)
			throws IllegalArgumentException {
		if (serverName == null) {
			throw new IllegalArgumentException(
					"The serverName must not be null.");
		}

		if (!logOpen) {
			this.serverName = serverName;
		}
	}

	/**
	 * Set the <code>SocketConnection</code> to use. The
	 * <code>SocketConnection</code> must be open.
	 * 
	 * Note: this should only be used for testing purposes.
	 * 
	 * @param socketConnection
	 *            the socketConnection to set
	 */
	public synchronized void setSocketConnection(
			SocketConnection socketConnection) throws IllegalArgumentException {
		if (socketConnection == null) {
			throw new IllegalArgumentException(
					"The socketConnection must not be null.");
		}
		this.socketConnection = socketConnection;
	}

	/**
	 * Get the <code>DataOutputStream</code> that is used for logging.
	 * 
	 * @return the dataOutputStream
	 */
	public synchronized DataOutputStream getDataOutputStream() {
		return dataOutputStream;
	}

	/**
	 * Set the <code>DataOutputStream</code> that shall be used for logging. If
	 * the log is open this is ignored.
	 * 
	 * @param dataOutputStream
	 *            the dataOutputStream to set
	 */
	public synchronized void setDataOutputStream(
			DataOutputStream dataOutputStream) throws IllegalArgumentException {
		if (dataOutputStream == null) {
			throw new IllegalArgumentException(
					"The dataOutputStream must not be null.");
		}

		if (!logOpen) {
			this.dataOutputStream = dataOutputStream;
		}
	}

	/**
	 * @see Appender#getPropertyNames()
	 */
	public String[] getPropertyNames() {
		return SocketAppender.PROPERTY_NAMES;
	}

	/**
	 * @see Appender#setProperty(String, String)
	 */
	public void setProperty(String name, String value)
			throws IllegalArgumentException {
		super.setProperty(name, value);

		if (name.equals(SocketAppender.SERVER_NAME_PROPERTY)) {
			setServerName(name);
		} else if (name.equals(SocketAppender.SOCKET_PORT_PROPERTY)) {
			setPort(Integer.parseInt(value));
		}
	}

}
