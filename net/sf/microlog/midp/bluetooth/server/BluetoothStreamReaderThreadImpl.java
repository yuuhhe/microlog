package net.sf.microlog.midp.bluetooth.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.bluetooth.RemoteDevice;

import net.sf.microlog.midp.bluetooth.BluetoothServerListener;

/**
 * This object is created for each client connecting to the MicroLog Bluetooth
 * server. It reads from the stream and notifies the BluetoothServerListener
 * that a message has been received.
 * 
 * @author Jarle Hansen (hansjar@gmail.com)
 * 
 */
public class BluetoothStreamReaderThreadImpl implements Runnable,
		BluetoothStreamReaderThread {
	private final BluetoothServerListener serverListener;
	private final DataInputStream input;
	private final RemoteDevice remoteDevice;
	private int connectionId;

	private AtomicBoolean connectionClosed = new AtomicBoolean(false);
	private String name;
	private String address;

	public BluetoothStreamReaderThreadImpl(
			final BluetoothServerListener listener,
			final DataInputStream input, final RemoteDevice remoteDevice) {
		this.serverListener = listener;
		this.input = input;
		this.remoteDevice = remoteDevice;
	}

	/**
	 * A unique ID for a specific connection. This Id is used to close and
	 * remove this connection from the <code>BluetoothConnectionsUtil</code>
	 * class.
	 */
	public synchronized void setConnectionId(final int connectionId) {
		this.connectionId = connectionId;
	}

	/**
	 * Reads from the stream and sends the result to the
	 * <code>BluetoothServerListener</code> object. If the [STOP] command is
	 * received it will call the
	 * <code>BluetoothConnectionsUtil.gracefulShutdown</code>, that will stop
	 * the Bluetooth server.
	 * 
	 */
	public void run() {
		try {
			name = remoteDevice.getFriendlyName(false);
			address = remoteDevice.getBluetoothAddress();
			
			System.out.println("New client connected, Bluetooth address: "
					+ address + (name == null ? "" : " - name:" + name));

			serverListener.clientAccepted(address, name);

			boolean stopReading = false;
			String message = input.readUTF();

			while (!stopReading && message != null) {
				if (message.compareTo("[STOP]") == 0) {
					BluetoothConnectionHandler.UTIL.gracefulShutdown(serverListener);
					break;
				}

				serverListener.messageReceived(message);
				message = input.readUTF();
			}
		} catch (IOException io) {
			System.err
					.println("Failed data from the client. It is probably disconnected. "
							+ io);
		} finally {
			closeConnection();
		}
	}

	/**
	 * Closes the client connection and removes the connection from the
	 * <code>BluetoothConnectionsUtil</code> list.
	 */
	public synchronized void closeConnection() {
		if (!connectionClosed.get()) {
			serverListener.clientDisconnected(address, name);
			if (input != null) {
				try {
					input.close();
				} catch (IOException io) {
					System.err.println("Failed to close: " + io);
				}
			}

			BluetoothConnectionHandler.UTIL.removeConnection(connectionId);
			connectionClosed.set(true);
		}
	}
}
