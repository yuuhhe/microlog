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
package net.sf.microlog.midp.bluetooth;

import java.io.IOException;

import net.sf.microlog.core.Appender;
import net.sf.microlog.core.Level;
import net.sf.microlog.core.appender.AbstractAppender;
import net.sf.microproperties.Properties;

/**
 * The <code>BluetoothSerialAppender</code> log using a Bluetooth serial
 * connection (btspp). It can be used in two different modes: try to locate the
 * Bluetooth logger server through regular Bluetooth lookup or by specifying the
 * exact url of the server. The latter version is useful for devices that fails
 * to lookup the server, for instance SonyEricsson P990i. If configured using a
 * property file the server url can be set using the property
 * <code>microlog.appender.BluetoothSerialAppender.serverUrl</code>
 * 
 * @author Johan Karlsson (johan.karlsson@jayway.se)
 * @author Jarle Hansen (hansjar@gmail.com)
 * @since 0.6
 */
public class BluetoothSerialAppender extends AbstractAppender {
	public static final String SERVER_URL_STRING = "microlog.appender.BluetoothSerialAppender.serverUrl";
	
	// Example of the complete BTADDRESS_PROPERTY: microlog.appender.BluetoothSerialAppender.btAddress=002608BDB48C
	public static final String BTADDRESS_PROPERTY = "btAddress";

	private BluetoothConnectionHandler bluetoothConnectionHandler = new BluetoothConnectionHandlerImpl();

	/**
	 * Default constructor. If it is used with the
	 * microlog.appender.BluetoothSerialAppender.btAddress property it will
	 * connect to the specified address. If no address is found in the Microlog
	 * configuration it will use the selectService method. We do recommend to
	 * add the Server Bluetooth address in the configuration, try to avoid using
	 * the selectService method since the implementation is not consistent and
	 * does not work on certain mobile devices.
	 */
	public BluetoothSerialAppender() {
	}
	
	/**
	 * Used for testing purposes only!!
	 * 
	 * @param bluetoothConnectionHandler
	 */
	BluetoothSerialAppender(final BluetoothConnectionHandler bluetoothConnectionHandler) {
		this.bluetoothConnectionHandler = bluetoothConnectionHandler;
		logOpen = true;
	}

	/**
	 * Constructor that lets you specify the Bluetooth logger server instead of
	 * using the lookup service. This is useful if you have a device that fails
	 * to locate the Bluetooth logger server. Experienced with SonyEricsson
	 * P990i. This solution will require the entire address, including the port
	 * number that can/will change when restarting the server application.
	 * 
	 * @param serverUrl
	 *            The server to connect to
	 */
	public BluetoothSerialAppender(String serverUrl) {
		bluetoothConnectionHandler.setConnectionString(serverUrl);
	}

	/**
	 * Constructor that lets you specify the Bluetooth Address of the server.
	 * This has several benefits. First it provides better performance than the
	 * default constructur, since a device search will normally take about 8-10
	 * seconds. Secondly it avoids hardcoding the server channel like the second
	 * constructor that receives a serverUrl String. The problem with hardcoding
	 * the channel is that you do not have any guarantee that it will be the
	 * same every time.
	 * 
	 * @param remoteDevice
	 *            The server to connect to, contains a Bluetooth Address
	 */
	public BluetoothSerialAppender(final BluetoothRemoteDevice remoteDevice) {
		bluetoothConnectionHandler.findAndSetConnectionString(remoteDevice);
	}

	/**
	 * Clear the log. This has not affect for this appender.
	 */
	public void clear() {
		// Do nothing
	}

	/**
	 * Closes the Bluetooth connection. The Bluetooth server application will
	 * continue to run and accept new connections.
	 */
	public void close() throws IOException {
		bluetoothConnectionHandler.close();
		logOpen = false;
	}

	/**
	 * Shuts down the Bluetooth server.
	 * 
	 * @throws IOException
	 */
	public synchronized void shutdownLoggingService() throws IOException {
		bluetoothConnectionHandler.shutdownLoggingService();
		logOpen = false;
	}

	/**
	 * Formats and writes to log statement to the stream.
	 */
	public synchronized void doLog(String clientID, String name, long time,
			Level level, Object message, Throwable t) {
		if (logOpen && formatter != null) {
			bluetoothConnectionHandler.writeLogToStream(formatter.format(
					bluetoothConnectionHandler.getBluetoothClientID(clientID),
					"", time, level, message, t));
		}
	}

	/**
	 * Open the log, i.e. open the Bluetooth connection to the log server.
	 */
	public synchronized void open() throws IOException {
		logOpen = bluetoothConnectionHandler.openConnection();
	}

	/**
	 * Get the size of the. Always returns <code>Appender.SIZE_UNDEFINED</code>.
	 */
	public long getLogSize() {
		return Appender.SIZE_UNDEFINED;
	}

	/**
	 * Configure the BluetoothSerialAppender.
	 * <p>
	 * If the device for some reason fails to locate a bluetooth logger server
	 * the connection url can set hard using the property
	 * <code>microlog.appender.BluetoothSerialAppender.serverUrl</code>. We
	 * recommend setting the
	 * <code><code>microlog.appender.BluetoothSerialAppender.btAddress</code>
	 * instead of this property, this will avoid hardcoding the server port.
	 * 
	 * 
	 * @param properties
	 *            Properties to configure with
	 */
	public synchronized void configure(Properties properties) {
		// Set the record store name from Properties
		String serverUrlString = properties.getProperty(SERVER_URL_STRING);
		if (serverUrlString != null && serverUrlString.length() > 0) {
			bluetoothConnectionHandler.setConnectionString(serverUrlString);
		}
	}

	/**
	 * @see Appender#getPropertyNames()
	 */
	public String[] getPropertyNames() {
		return new String[] { BTADDRESS_PROPERTY };
	}

	/**
	 * @see Appender#setProperty(String, String)
	 */
	public void setProperty(String name, String value)
			throws IllegalArgumentException {
		if (name != null && BTADDRESS_PROPERTY.equals(name)) {
			bluetoothConnectionHandler
					.findAndSetConnectionString(BluetoothRemoteDevice
							.setAddress(value));
		}
	}
}
