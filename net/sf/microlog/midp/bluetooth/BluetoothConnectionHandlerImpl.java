package net.sf.microlog.midp.bluetooth;

import java.io.DataOutputStream;
import java.io.IOException;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

/**
 * Handles the Bluetooth communication. Opens, closes and sends messages with
 * Bluetooth.
 * 
 * @author Jarle Hansen (hansjar@gmail.com)
 * @since 2.2
 */
class BluetoothConnectionHandlerImpl implements BluetoothConnectionHandler {
	private String connectionString = null;

	private StreamConnection connection = null;
	private DataOutputStream dataOutputStream = null;

	private String bluetoothClientID = null;

	private BluetoothServiceSearch serviceSearch = BluetoothServiceSearch
			.newInstance();

	BluetoothConnectionHandlerImpl() {
	}

	public void findAndSetConnectionString(
			final BluetoothRemoteDevice remoteDevice) {
		connectionString = serviceSearch.getLoggerServiceString(remoteDevice);
	}

	public void setConnectionString(final String connectionString) {
		this.connectionString = connectionString;
	}

	/**
	 * Opens a Bluetooth connection to the server application. If no connection
	 * String is available, it will use the selectService method. This is not
	 * recommended!
	 */
	public boolean openConnection() {
		boolean connectionOpen = false;

		if (connectionString == null && !serviceSearch.hasServiceError()) {
			connectionString = serviceSearch.getLoggerServiceString();
		}

		try {
			connectionOpen = openOutputStream();
		} catch (IOException e) {
			System.err
					.println("Failed to connect to the Bluetooth log server with connection string "
							+ connectionString + ' ' + e);
		}

		return connectionOpen;
	}

	/**
	 * Tries to open the DataOutputStream. This is only done if the
	 * connectionString is not null and there has not occured any service error.
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean openOutputStream() throws IOException {
		boolean openedOutputStream = false;

		if (connectionString != null && !serviceSearch.hasServiceError()) {
			connection = (StreamConnection) Connector.open(connectionString);
			dataOutputStream = connection.openDataOutputStream();

			openedOutputStream = true;
		}

		return openedOutputStream;
	}

	/**
	 * Shuts down the logging service. On the server side this will close the
	 * desktop application.
	 */
	public void shutdownLoggingService() throws IOException {
		if (dataOutputStream != null) {
			try {
				dataOutputStream.writeUTF("[STOP]");
			} catch (IOException io) {
				System.err
						.println("Failed to send [STOP] to the Bluetooth server, "
								+ io);
			} finally {
				close();
			}
		}
	}

	/**
	 * Close the log.
	 */
	public void close() throws IOException {
		if (dataOutputStream != null) {
			try {
				dataOutputStream.close();
			} catch (IOException e) {
				System.err
						.println("Failed to terminate the dataOutputStream in a controlled way."
								+ e);
			}
		}

		if (connection != null) {
			try {
				connection.close();
			} catch (IOException e) {
				System.err.println("Failed to close the log " + e);
			}
		}
	}

	/**
	 * Writes the formatted log statement to the output stream.
	 */
	public void writeLogToStream(final String formattedLogStatement) {
		if (dataOutputStream != null) {
			try {
				dataOutputStream.writeUTF(formattedLogStatement);
				dataOutputStream.flush();
			} catch (IOException io) {
				System.err.println("Unable to log to the output stream. " + io);
			}
		}
	}

	/**
	 * Returns the clientID that will be used to identify the connected client.
	 * If the clientID is not set from the user it will try to use the Bluetooth
	 * friendly name instead.
	 * 
	 * @param clientID
	 *            the client id for the <code>BluetoothAppender</code>.
	 * 
	 * @return the id <code>String</code>
	 */
	public String getBluetoothClientID(final String clientID) {
		if (bluetoothClientID == null) {
			if (clientID == null || clientID.length() == 0) {
				bluetoothClientID = getBluetoothFriendlyName();
			}

			if (clientID != null || bluetoothClientID == null) {
				bluetoothClientID = clientID;
			}
		}

		return bluetoothClientID;
	}

	/**
	 * Tries to retrieve the Bluetooth friendly name. If this is successful the
	 * first time the same name will be returned each time.
	 * 
	 * @return the Bluetooth friendly name, that is the name specified by the
	 *         user.
	 */
	private String getBluetoothFriendlyName() {
		String bluetoothFriendlyName = "";

		try {
			bluetoothFriendlyName = LocalDevice.getLocalDevice()
					.getFriendlyName();
		} catch (BluetoothStateException bse) {
			System.err.println("Unable to get the Bluetooth friendly name, "
					+ bse);
		}

		return bluetoothFriendlyName;
	}
}
