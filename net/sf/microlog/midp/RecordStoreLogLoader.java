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
package net.sf.microlog.midp;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordComparator;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;

import net.sf.microlog.midp.appender.RecordStoreLogNameResolver;
import net.sf.microproperties.Properties;

/**
 * This class is used to load a log from the <code>RecordStore</code>, i.e. a
 * log that is created with a <code>RecordStoreAppender</code>.
 * 
 * @author Henrik Larne
 * @author Johan Karlsson (johan.karlsson@jayway.se)
 * 
 */
public class RecordStoreLogLoader {

	private final RecordComparator ascComparator = new AscendingComparator();
	private final RecordComparator descComparator = new DescendingComparator();
	private RecordComparator currentComparator = ascComparator;

	private String recordStoreName;

	private RecordStore logRecordStore;

	/**
	 * Create a <code>RecordStoreLogLoader</code>.
	 */
	public RecordStoreLogLoader() {
		recordStoreName = RecordStoreLogNameResolver.fetchRecordStoreName(null);
	}

	/**
	 * Create a <code>RecordStoreLogLoader</code> that uses the specified
	 * properties for setup.
	 * 
	 * @param properties
	 *            the <code>Properties</code> object to be used for setup.
	 */
	public RecordStoreLogLoader(Properties properties) {
		recordStoreName = RecordStoreLogNameResolver.fetchRecordStoreName(properties);
	}

	/**
	 * Set the name of the <code>RecordStore</code>.
	 * 
	 * @param recordStoreName
	 *            the new name for the <code>RecordStore</code>.
	 */
	public void setRecordStoreName(String recordStoreName) {
		try {
			this.recordStoreName = recordStoreName;
			RecordStore.openRecordStore(recordStoreName, true);
		} catch (RecordStoreFullException e) {
			System.err.println("RecordStore is full." + e);
		} catch (RecordStoreNotFoundException e) {
			System.err.println("RecordStore does not exists." + e);
		} catch (RecordStoreException e) {
			System.err.println("RecordStore is not workings." + e);
		}
	}

	/**
	 * Get the <code>RecordStore</code> name.
	 * 
	 * @return the <code>RecordStore</code> name.
	 */
	public String getRecordStoreName() {
		return recordStoreName;
	}

	/**
	 * Get the log content.
	 * 
	 * @return the log content.
	 */
	public String getLogContent() {

		StringBuffer logContent = new StringBuffer(512);

		try {
			System.out.println("Reading log data from: "+recordStoreName);
			logRecordStore = RecordStore.openRecordStore(recordStoreName, true);
			RecordEnumeration recordEnum = logRecordStore.enumerateRecords(
					null, currentComparator, false);
			while (recordEnum.hasNextElement()) {
				byte[] data = recordEnum.nextRecord();

				try {
					ByteArrayInputStream bais = new ByteArrayInputStream(data);
					DataInputStream is = new DataInputStream(bais);

					is.readLong();
					String logString = is.readUTF();
					logContent.append(logString + "\n");

					is.close();
					bais.close();
				} catch (IOException e) {
					System.err.println("Failed to load log " + e);
				}
			}
			recordEnum.destroy();
		} catch (RecordStoreNotFoundException e) {
			System.err.println("Could not find log data in " + recordStoreName
					+ " " + e);
		} catch (RecordStoreException e) {
			System.err.println("Could not open log data. " + e);
		} finally {
			closeLog();
		}

		return logContent.toString();
	}

	public void switchSortOrder() {
		if (currentComparator instanceof AscendingComparator) {
			currentComparator = descComparator;
		} else {
			currentComparator = ascComparator;
		}
	}

	/**
	 * Clear the log contained in the <code>RecordStore</code>.
	 */
	public void clearLog() {

		try {
			logRecordStore = RecordStore.openRecordStore(recordStoreName, true);
			RecordEnumeration enumeration = logRecordStore.enumerateRecords(
					null, null, false);
			while (enumeration.hasNextElement()) {
				int recordId = enumeration.nextRecordId();
				logRecordStore.deleteRecord(recordId);
			}

			logRecordStore = RecordStore.openRecordStore(recordStoreName, true);

		} catch (RecordStoreNotOpenException e) {
			System.err.println("RecordStore is not open. " + recordStoreName
					+ " " + e);
		} catch (InvalidRecordIDException e) {
			System.err.println("Invalid record id." + e);
		} catch (RecordStoreException e) {
			System.err.println("RecordStore not working." + e);
		} finally {
			closeLog();
		}
	}

	public int getNumLogItems() {
		int nofLogItems = 0;
		try {
			logRecordStore = RecordStore.openRecordStore(recordStoreName, true);
			nofLogItems = logRecordStore.getNumRecords();
		} catch (RecordStoreFullException e) {
			System.err.println("RecordStore is full "+e);
		} catch (RecordStoreNotFoundException e) {
			System.err.println("RecordStore not found "+e);
		} catch (RecordStoreException e) {
			System.err.println("Some problem with the RecordStore "+e);
		} 
		return nofLogItems;
	}

	/**
	 * Close the log.
	 */
	private void closeLog() {
		try {
			if (logRecordStore != null) {
				logRecordStore.closeRecordStore();
			}
		} catch (RecordStoreNotOpenException e) {
			System.err.println("RecordStore was not open " + e);
		} catch (RecordStoreException e) {
			System.err.println("Failed to close the RecordStore " + e);
		}
	}
}
