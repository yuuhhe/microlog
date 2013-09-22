package net.sf.microlog.midp.bluetooth;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

import net.sf.microlog.core.MicrologConstants;

/**
 * Responsible for the service searches. It is recommended to use the service
 * search if you know the Bluetooth address of the server. This will provide
 * better performance and avoid any issues with hardcoding the channel for the
 * Bluetooth connection.
 * 
 * @author Jarle Hansen (hansjar@gmail.com)
 * @since 2.0
 * 
 */
class BluetoothServiceSearch implements DiscoveryListener {
	private ServiceRecord serviceRecord;

	private boolean serviceSearchCompleted;
	private static final Object lock = new Object();

	// Useful for emulators that throw NullPointerException
	private boolean serviceError = false;

	/**
	 * Create a <code>BluetoothServiceSearch</code> object. Use the
	 * <code>newInstance()</code> to create new instances of
	 * <code>BluetoothServiceSearch</code> objects.
	 */
	private BluetoothServiceSearch() {
	}

	/**
	 * Get a new <code>BluetoothServiceSearch</code> instance.
	 * 
	 * @return a new <code>BluetoothServiceSearch</code> instance
	 */
	public static BluetoothServiceSearch newInstance() {
		return new BluetoothServiceSearch();
	}

	/**
	 * Get the <code>String</code> representing the Logger service.
	 * 
	 * @param remoteDevice
	 *            the <code>RemoteDevice</code>
	 * 
	 * @return the Logger service <code>String</code>
	 */
	String getLoggerServiceString(final BluetoothRemoteDevice remoteDevice) {
		return getConnectionString(remoteDevice, new UUID(
				MicrologConstants.DEFAULT_BT_UUID_STRING, false));
	}

	/**
	 * Returns the found connection String from the selectService method. Try to
	 * avoid using this method! Use getLoggerServiceString(final
	 * BluetoothRemoteDevice remoteDevice) instead.
	 * 
	 * @return
	 */
	String getLoggerServiceString() {
		return getConnectionStringSelectService();
	}

	/**
	 * If a service error has occured, this can happen in emulators, this will
	 * be true. Otherwise it should remain false and not affect the application.
	 * 
	 * @return
	 */
	boolean hasServiceError() {
		return serviceError;
	}

	/**
	 * Uses the selectService method to both search for available devices and
	 * services. This method is not recommended to be used since it is not a
	 * consistent implementation across different mobile platforms.
	 * 
	 * @return
	 */
	private String getConnectionStringSelectService() {
		System.err
				.println("No Server Bluetooth address or Server URL set, using the selectService. Please try to update your code to use a Server Bluetooth address instead!");
		
		String connectionString = null;

		try {
			DiscoveryAgent agent = LocalDevice.getLocalDevice()
					.getDiscoveryAgent();

			connectionString = agent.selectService(new UUID(
					MicrologConstants.DEFAULT_BT_UUID_STRING, false),
					ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
		} catch (BluetoothStateException bse) {
			System.err
					.println("Failed to connect to the Bluetooth log server. "
							+ bse);
			serviceError = true;
		}

		return connectionString;
	}

	/**
	 * By sending in a BluetoothRemoteDevice with a valid Bluetooth address, it
	 * is able to search for the Microlog service. It will return the entire
	 * connection String for the found service. This connection String is then
	 * later used to connect to the server.
	 * 
	 * @param remoteDevice
	 *            The server to connect to, contains a Bluetooth Address
	 * @return the connection <code>String</code>
	 */
	private String getConnectionString(
			final BluetoothRemoteDevice remoteDevice, final UUID uuid) {
		serviceSearchCompleted = false;

		try {
			LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(
					null, new UUID[] { uuid }, remoteDevice, this);

			try {
				synchronized (lock) {
					while (!serviceSearchCompleted) {
						lock.wait();
					}
				}
			} catch (InterruptedException ie) {
			}
		} catch (BluetoothStateException bse) {
			System.err.println("Unable to search for the service on device: "
					+ remoteDevice.getBluetoothAddress() + ", " + bse);
		} catch (NullPointerException npe) {
			System.err
					.println("NullPointer when trying to call searchServices(), are you running in an emulator? "
							+ npe);
			serviceError = true;
		}

		final String connectionString;

		if (serviceRecord != null) {
			connectionString = serviceRecord.getConnectionURL(
					ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
		} else {
			connectionString = null;
		}

		return connectionString;
	}

	/**
	 * Callback when the service search is finished.
	 * 
	 * @param transactionId
	 *            the transaction id.
	 * @param responseCode
	 *            the response code.
	 */
	public void serviceSearchCompleted(final int transactionId,
			final int responseCode) {
		synchronized (lock) {
			serviceSearchCompleted = true;
			lock.notifyAll();
		}
	}

	/**
	 * Callback when a service is discovered.
	 * 
	 * @param transactionId
	 *            the transaction id
	 * @param serviceRecords
	 *            the service records.
	 * 
	 *            TODO what if several Microlog services are detected?
	 */
	public void servicesDiscovered(final int transactionId,
			final ServiceRecord[] serviceRecords) {
		serviceRecord = serviceRecords[0];
	}

	/**
	 * Callback when a device is discovered.
	 * 
	 * @param remoteDevice
	 *            the <code>RemoteDevice</code> that was discovered.
	 * @param deviceClass
	 *            the <code>DeviceClass</code> of the discovered device.
	 */
	public void deviceDiscovered(RemoteDevice remoteDevice,
			DeviceClass deviceClass) {
		// Not used in service search
	}

	/**
	 * The inquiry is completed.
	 * 
	 * @param type
	 *            the type on inwuiry.
	 */
	public void inquiryCompleted(int type) {
	}
}
