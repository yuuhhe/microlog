package net.sf.microlog.midp.bluetooth.server;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.microlog.midp.bluetooth.BluetoothServerListener;

/**
 * Keeps track of all connected clients.
 * 
 * @author Jarle Hansen (hansjar@gmail.com)
 * 
 */
public enum BluetoothConnectionHandler {
	UTIL;

	private final Map<Integer, BluetoothStreamReaderThread> connectionMap = new ConcurrentHashMap<Integer, BluetoothStreamReaderThread>();
	private AtomicInteger threadCounter = new AtomicInteger(0);
	private AtomicBoolean shutdown = new AtomicBoolean(false);
	
	private BluetoothConnectionHandler() {
	}
	
	/**
	 * For testing purposes only!!!
	 */
	Map<Integer, BluetoothStreamReaderThread> getConnectionMapTestOnly() {
		threadCounter.set(0);
		return connectionMap;
	}
	
	/**
	 * Removes a connection from the connectionList.
	 * 
	 * @param connectionId
	 */
	void removeConnection(final int connectionId) {
		connectionMap.remove(connectionId);
	}

	/**
	 * Adds a connection to the connectionList.
	 * 
	 * @param bluetoothConnection
	 */
	void addConnection(
			final BluetoothStreamReaderThread bluetoothConnection) {
		if (!shutdown.get()) {
			final int connectionId = threadCounter.incrementAndGet();
			connectionMap.put(connectionId, bluetoothConnection);
			bluetoothConnection.setConnectionId(connectionId);
		}
	}

	/**
	 * When the [STOP] command is sent, the server will gracefully shut down.
	 * This means it will loop through the connectionList and shut down all
	 * active connections before taking down the Bluetooth server.
	 * 
	 * @param bluetoothServerListener
	 */
	void gracefulShutdown(
			final BluetoothServerListener bluetoothServerListener) {
		if (!shutdown.get()) {
			shutdown.set(true);
			
			Collection<BluetoothStreamReaderThread> connectionMapValues = connectionMap
					.values();
			for (BluetoothStreamReaderThread bluetoothStreamReaderThread : connectionMapValues) {
				bluetoothStreamReaderThread.closeConnection();
			}
			
			System.out.println("Number of open connections at shutdown: "
					+ connectionMap.size());
			
			if (bluetoothServerListener != null) {
				bluetoothServerListener.shutdown();
			}
		}
	}
}
