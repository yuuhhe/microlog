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

package net.sf.microlog.midp.bluetooth.server;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import net.sf.microlog.midp.bluetooth.BluetoothServerListener;

/**
 * The <code>BluetoothSerialServerThread</code> is used for receiving data from
 * a <code>BluetoothSerialAppender</code>. It is intended to be used for servers
 * implemented both in Java & in Java ME (CLDC). When a client connects it will
 * create a new <code>BluetoothStreamReaderThreadImpl</code> object, so each
 * client has its own thread.
 * 
 * @author Johan Karlsson (johan.karlsson@jayway.se)
 * @author Jarle Hansen (hansjar@gmail.com)
 * @since 0.9
 */
public class BluetoothSerialServerThread implements Runnable {

	private static final String DEFAULT_BT_UUID_STRING = "80d41dda939840c41b605d69043dab6";
	
	private final Executor executor = Executors.newCachedThreadPool();
	
	
	private final LocalDevice local;
	private BluetoothServerListener serverListener = new DefaultServerListenerImpl();

	/**
	 * Create a <code>BluetoothSerialServerThread</code> object.
	 */
	public BluetoothSerialServerThread() {
		local = getDiscoverableLocalDevice();
	}
	
	public String getLocalBtAddress() {
		return local.getBluetoothAddress();
	}

	/**
	 * Try to make the device discoverable. If not the clients can still connect
	 * using the service search method.
	 * 
	 * @return the Bluetooth local device.
	 * @throws BluetoothStateException
	 */
	private LocalDevice getDiscoverableLocalDevice() {
		LocalDevice local;

		try {
			local = LocalDevice.getLocalDevice();
			if (!local.setDiscoverable(DiscoveryAgent.GIAC)) {
				System.out.println("Failed to change to the "
						+ "discoverable mode");
			}
		} catch (BluetoothStateException bse) {
			throw new IllegalStateException(
					"Failed to init the Bluetooth connection. " + bse);
		}

		return local;
	}

	/**
	 * Set the <code>BluetoothMessageReceiver</code> that is notified every time
	 * a message is received from the client.
	 * 
	 * @param serverListener
	 *            the serverListner to be notified.
	 */
	public void setServerListener(BluetoothServerListener serverListener) {
		if (serverListener != null) {
			this.serverListener = serverListener;
		}
	}

	/**
	 * Implementation of the <code>Runnable</code> interface. The acceptAndOpen
	 * will block until a new client is connected. Each new connection will
	 * start a new thread.
	 */
	public void run() {
		while (true) {
			StreamConnectionNotifier notifier = null;
			StreamConnection connection = null;
			
			try {
				notifier = createNotifier();
				notifyListenerServerStarted(notifier);

				// Blocking call
				connection = notifier.acceptAndOpen();
				final RemoteDevice remoteDevice = RemoteDevice
						.getRemoteDevice(connection);
				BluetoothStreamReaderThreadImpl bluetoothConnection = new BluetoothStreamReaderThreadImpl(
						serverListener, connection.openDataInputStream(),
						remoteDevice);
				BluetoothConnectionHandler.UTIL.addConnection(bluetoothConnection);

				executor.execute(bluetoothConnection);
			} catch (IOException io) {
				System.err.println("Unable to start service: " + io);
			} finally {
				try {
					if (notifier != null) {
						notifier.close();
					}
					if (connection != null) {
						connection.close();
					}
				} catch (IOException io) {
					System.err.println("Failed to close: " + io);
				}
			}
		}
	}

	/**
	 * Opens the StreamConnectionNotifier.
	 * 
	 * @return Server connection opened for clients.
	 * @throws IOException
	 */
	private StreamConnectionNotifier createNotifier() throws IOException {
		return (StreamConnectionNotifier) Connector.open("btspp://localhost:"
				+ DEFAULT_BT_UUID_STRING);
	}

	/**
	 * Notifies the BluetoothServerListener that the server has started with the
	 * connection url that the clients can connect to.
	 * 
	 * @param notifier
	 */
	private void notifyListenerServerStarted(
			final StreamConnectionNotifier notifier) {
		final ServiceRecord serviceRecord = local.getRecord(notifier);

		serverListener.serverStarted(serviceRecord.getConnectionURL(
				ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false));
	}
}