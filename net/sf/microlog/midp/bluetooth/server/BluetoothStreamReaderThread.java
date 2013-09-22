package net.sf.microlog.midp.bluetooth.server;

public interface BluetoothStreamReaderThread {
	public void closeConnection();
	public void setConnectionId(final int connectionId);
}
