package net.sf.microlog.midp.bluetooth;

import javax.bluetooth.RemoteDevice;

/**
 * Contain the Bluetooth address of the server. This is used in the service
 * search.
 * 
 * @author Jarle Hansen (hansjar@gmail.com)
 * 
 */
public class BluetoothRemoteDevice extends RemoteDevice {
	private BluetoothRemoteDevice(final String bluetoothAddress) {
		super(bluetoothAddress);
	}

	public static BluetoothRemoteDevice setAddress(final String bluetoothAddress) {
		return new BluetoothRemoteDevice(bluetoothAddress);
	}
}
