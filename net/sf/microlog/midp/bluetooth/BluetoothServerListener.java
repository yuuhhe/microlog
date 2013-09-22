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

/**
 * The <code>BluetoothServerListener</code> is an interface for classes that
 * listens to status messages from the Bluetooth server.
 * 
 * @author Johan Karlsson (johan.karlsson@jayway.se)
 * @since 2.0
 */
public interface BluetoothServerListener {

	/**
	 * The server has started.
	 * 
	 * @param url
	 *            the URL of the server.
	 */
	public void serverStarted(String url);

	/**
	 * A new client has been accepted and connected.
	 * @param address TODO
	 * @param name TODO
	 */
	public void clientAccepted(String address, String name);

	/**
	 * This method is called when a message is received from the client.
	 * @param message
	 *            the message that was received.
	 */
	public void messageReceived(String message);

	/**
	 * The client has been disconnected.
	 * @param address TODO
	 */
	public void clientDisconnected(String address, String name);
	
	public void shutdown();
}
