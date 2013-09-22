package net.sf.microlog.midp.bluetooth.server;

import net.sf.microlog.midp.bluetooth.BluetoothServerListener;

/**
 * The default implementation of the BluetoothServerListener. If a separate
 * BluetoothServerListener is set, this class is not used.
 * 
 * @author Jarle Hansen (hansjar@gmail.com)
 * 
 */
class DefaultServerListenerImpl implements BluetoothServerListener {

	DefaultServerListenerImpl() {
	}

	public void clientAccepted(String address, String name) {
	}

	public void clientDisconnected(String address, String name) {
	}

	public void messageReceived(String message) {
	}

	public void serverStarted(String url) {
	}

	public void shutdown() {
	}

}
