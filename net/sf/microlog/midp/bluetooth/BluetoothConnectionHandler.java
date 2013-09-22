package net.sf.microlog.midp.bluetooth;

import java.io.IOException;

interface BluetoothConnectionHandler {
	void setConnectionString(final String serverUrl);
	void findAndSetConnectionString(final BluetoothRemoteDevice remoteDevice);
	void shutdownLoggingService() throws IOException;
	void close() throws IOException;
	void writeLogToStream(final String formattedLogStatement);
	String getBluetoothClientID(final String clientID);
	boolean openConnection();
}
