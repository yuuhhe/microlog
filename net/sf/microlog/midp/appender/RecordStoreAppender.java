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
package net.sf.microlog.midp.appender;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;

import net.sf.microlog.core.Appender;
import net.sf.microlog.core.Formatter;
import net.sf.microlog.core.Level;
import net.sf.microlog.core.appender.AbstractAppender;
import net.sf.microlog.midp.DescendingComparator;
import net.sf.microlog.midp.MIDPConstants;

/**
 * An Appender that appends the logging to the record store.
 * 
 * <p>
 * The maximum log entry size can be set with the parameter
 * <code>microlog.appender.RecordStoreAppender.maxLogEntries</code> The file
 * name can be passed with the property
 * <code>microlog.appender.RecordStoreAppender.recordStoreName</code>.
 * 
 * @author Johan Karlsson
 * @author Darius Katz
 * @author Karsten Ohme
 * @since 0.1
 */
public class RecordStoreAppender extends AbstractAppender {

	public static final String RECORD_STORE_NAME_PROPERTY = "recordStoreName";

	public static final String RECORD_STORE_MAX_RECORD_STORE_ENTRIES = "maxRecordStoreEntries";

	public static final String[] PROPERTY_NAMES = {
			RecordStoreAppender.RECORD_STORE_NAME_PROPERTY,
			RecordStoreAppender.RECORD_STORE_MAX_RECORD_STORE_ENTRIES };

	/**
	 * The number of default maximum log entries.
	 */
	public static final int RECORD_STORE_DEFAULT_MAX_ENTRIES = 20;

	/**
	 * RecordStore of this appender.
	 */
	private RecordStore logRecordStore;

	/**
	 * The RecordStore name of this appender.
	 */
	private String recordStoreName = MIDPConstants.RECORD_STORE_DEFAULT_NAME;

	// variables used by the limited record entries functionality
	private int maxRecordEntries = RECORD_STORE_DEFAULT_MAX_ENTRIES;
	private int currentOldestEntry;
	private int[] limitedRecordIDs;

	ByteArrayOutputStream byteArrayOutputStream;

	DataOutputStream dataOutputStream;

	/**
	 * Create a RecordStoreAppender with the default name and limited record
	 * size.
	 */
	public RecordStoreAppender() {
		super();
		byteArrayOutputStream = new ByteArrayOutputStream(64);
		dataOutputStream = new DataOutputStream(byteArrayOutputStream);
	}

	/**
	 * Get the recordstore name to use for logging.
	 * 
	 * @return the recordStoreName
	 */
	public synchronized String getRecordStoreName() {
		return recordStoreName;
	}

	/**
	 * Set the recordstore name to use for logging.
	 * 
	 * Note: this has no effect if the log is opened or if the re.
	 * 
	 * @param recordStoreName
	 *            the recordStoreName to set
	 * @throws IllegalArgumentException
	 *             if the <code>recordStoreName</code> is null
	 */
	public synchronized void setRecordStoreName(String recordStoreName)
			throws IllegalArgumentException {

		if (recordStoreName == null) {
			throw new IllegalArgumentException(
					"The recordStoreName must not be null.");
		}

		if (logOpen == false) {
			this.recordStoreName = recordStoreName;
		}
	}

	/**
	 * Get the max number of recordstore entries.
	 * 
	 * @return the maxRecordEntries
	 */
	public synchronized int getMaxRecordEntries() {
		return maxRecordEntries;
	}

	/**
	 * Set the recordstore name to use for logging.
	 * 
	 * Note: this has no effect if the log is opened.
	 * 
	 * @param maxRecordEntries
	 *            the maxRecordEntries to set
	 */
	public synchronized void setMaxRecordEntries(int maxRecordEntries) {
		if (logOpen == false) {
			this.maxRecordEntries = maxRecordEntries;
		}
	}

	/**
	 * Do the logging.
	 * <p>
	 * Only executed by the master RecordStoreAppender.
	 * 
	 * @param clientID
	 *            the client id.
	 * @param name
	 *            the name of the logger.
	 * @param time
	 *            the relative time when the logging was done.
	 * @param level
	 *            the level to use for the logging.
	 * @param message
	 *            the message to log.
	 * @param t
	 *            the exception to log.
	 */
	public synchronized void doLog(String clientID, String name, long time, Level level,
			Object message, Throwable t) {

		if (logOpen && formatter != null) {
			byte[] data = createLogData(clientID, name, time, level, message,
					t, formatter);

			try {
				// Delete the oldest log entry
				if (limitedRecordIDs[currentOldestEntry] != -1) {
					logRecordStore
							.deleteRecord(limitedRecordIDs[currentOldestEntry]);
				}

				// Add the new entry
				int newRecId = logRecordStore.addRecord(data, 0, data.length);

				// Save the recordId for later
				limitedRecordIDs[currentOldestEntry] = newRecId;

				// Move pointer to the now oldest entry
				currentOldestEntry = (currentOldestEntry + 1)
						% maxRecordEntries;

			} catch (RecordStoreNotOpenException e) {
				System.err.println("RecordStore was not open " + e);
			} catch (RecordStoreFullException e) {
				System.err.println("RecordStore is full " + e);
			} catch (RecordStoreException e) {
				System.err.println("Failed to log to RecordStore " + e);
			}
		}
	}

	/**
	 * Create the log data.
	 * <p>
	 * Executed only by the master.
	 * 
	 * @param clientID
	 *            the client id.
	 * @param name
	 *            the name of the logger.
	 * @param time
	 *            the relative time when the logging was done.
	 * @param level
	 *            the level to use for the logging.
	 * @param message
	 *            the message to log.
	 * @param t
	 *            the exception to log.
	 * @param formatter
	 *            the formatter.
	 * 
	 * @return the formatted log entry.
	 */
	private synchronized byte[] createLogData(String clientID, String name, long time,
			Level level, Object message, Throwable t, Formatter formatter) {

		byte[] data = null;

		try {
			byteArrayOutputStream.reset();
			dataOutputStream.writeLong(time);
			dataOutputStream.writeUTF(formatter.format(clientID, name, time,
					level, message, t));
			data = byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			System.err.println("Failed to create the logdata " + e);
		}
		return data;
	}

	/**
	 * Clear the underlying RecordStore from data. Note if logging is done when
	 * executing this method, these new logging events are not cleared.
	 * 
	 * @see net.sf.microlog.core.appender.AbstractAppender#clear()
	 */
	public synchronized void clear() {

		try {
			RecordEnumeration enumeration = logRecordStore.enumerateRecords(
					null, null, false);
			while (enumeration.hasNextElement()) {
				int recordId = enumeration.nextRecordId();
				logRecordStore.deleteRecord(recordId);
			}
		} catch (RecordStoreNotOpenException e) {
			System.err.println("RecordStore not open " + e);
		} catch (InvalidRecordIDException e) {
			System.err.println("Invalid id " + e);
		} catch (RecordStoreException e) {
			System.err.println("Failed to delete record " + e);
		}

	}

	/**
	 * @see net.sf.microlog.core.appender.AbstractAppender#close()
	 */
	public synchronized void close() throws IOException {

		if (logOpen) {
			try {

				logRecordStore.closeRecordStore();

			} catch (RecordStoreNotOpenException e) {
				throw new IOException("The RecordStore was not open " + e);
			} catch (RecordStoreException e) {
				throw new IOException("Failed to close the RecordStore " + e);
			}

			logOpen = false;
		}

	}

	/**
	 * @see net.sf.microlog.core.appender.AbstractAppender#open()
	 */
	public synchronized void open() throws IOException {

		if (recordStoreName == null) {
			recordStoreName = MIDPConstants.RECORD_STORE_DEFAULT_NAME;
		}

		System.out.println("Log RecordStore: " + recordStoreName);

		try {

			logRecordStore = RecordStore.openRecordStore(recordStoreName, true);
			initLimitedEntries();

			logOpen = true;
		} catch (RecordStoreFullException e) {
			System.err.println("RecordStore is full " + e);
		} catch (RecordStoreNotFoundException e) {
			System.err.println("RecordStore was not found " + e);
		} catch (RecordStoreException e) {
			System.err.println("Failed to open the log " + e);
		}

	}

	/**
	 * Get the size of the log.
	 * 
	 * @return the size of the log.
	 */
	public synchronized long getLogSize() {
		long logSize = SIZE_UNDEFINED;

		if (logRecordStore != null) {
			try {
				int numRecords = logRecordStore.getNumRecords();
				if (numRecords != 0) {
					logSize = logRecordStore.getSize();
				} else if (numRecords == 0) {
					logSize = 0;
				}
			} catch (RecordStoreNotOpenException e) {
				System.err.println("RecordStore was not open " + e);
			}
		}

		return logSize;
	}

	/**
	 * Initialise the limited entries functionality
	 * <p>
	 * Only the master executes this.
	 */
	private synchronized void initLimitedEntries() {

		limitedRecordIDs = new int[maxRecordEntries];

		for (int i = 0; i < maxRecordEntries; i++)
			limitedRecordIDs[i] = -1;

		// Enumerate through all records. Copy/save timestamps and recordIDs
		// of the newest n records into the array(s) that keep track of
		// limited
		// entries and delete the rest of the records from the RecordStore.
		// (n = max no of log-enties)

		try {
			int arrayPointer = maxRecordEntries - 1;
			// only if not already open open it, i.e. we are reconfiguring
			// at
			// runtime
			if (!logOpen) {
				logRecordStore = RecordStore.openRecordStore(recordStoreName,
						true);
			}
			RecordEnumeration recordEnum = logRecordStore.enumerateRecords(
					null, new DescendingComparator(), false);

			while (recordEnum.hasNextElement()) {
				int recId = recordEnum.nextRecordId();
				if (arrayPointer >= 0) {
					// save recId
					limitedRecordIDs[arrayPointer] = recId;
					arrayPointer--;
				} else {
					// too old, just delete
					logRecordStore.deleteRecord(recId);
				}
			}
			recordEnum.destroy();
		} catch (RecordStoreNotFoundException e) {
			System.err.println("Failed to find recordstore. " + e);
		} catch (RecordStoreException e) {
			System.err.println("Something is wrong with the RecordStore. " + e);
		}

	}

	/**
	 * @see Appender#getPropertyNames()
	 */
	public synchronized String[] getPropertyNames() {
		return PROPERTY_NAMES;
	}

	/**
	 * @see Appender#setProperty(String, String)
	 */
	public synchronized void setProperty(String name, String value)
			throws IllegalArgumentException {
		super.setProperty(name, value);

		if (name.equals(RecordStoreAppender.RECORD_STORE_NAME_PROPERTY)) {
			setRecordStoreName(value);
		} else if (name
				.equals(RecordStoreAppender.RECORD_STORE_MAX_RECORD_STORE_ENTRIES)) {
			try {
				setMaxRecordEntries(Integer.parseInt(value));
			} catch (NumberFormatException e) {
				System.err
						.println("Could not parse the property "
								+ RECORD_STORE_MAX_RECORD_STORE_ENTRIES + " : "
								+ value);
			}
		}
	}

}
