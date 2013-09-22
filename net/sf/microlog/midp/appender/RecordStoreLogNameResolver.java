/*
 * Copyright 2009 The Microlog project @sourceforge.net
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

import net.sf.microlog.core.PropertyConfigurator;
import net.sf.microlog.midp.MIDPConstants;
import net.sf.microproperties.Properties;

/**
 * The <code>RecordStoreLogNameResolver</code> is used to resolve what record
 * store name to use.
 * 
 * @author Johan Karlsson
 * 
 */
public class RecordStoreLogNameResolver {

	/**
	 * Fetch the record store name.
	 * 
	 * @param properties
	 *            the properties to use for getting the name.
	 * @return the name of the record store log.
	 */
	public static String fetchRecordStoreName(Properties properties) {
		String recordStoreName = null;
		
		if (properties != null) {
			String propertyName = PropertyConfigurator.APPENDER_KEY + ".RecordStoreAppender."
					+ RecordStoreAppender.RECORD_STORE_NAME_PROPERTY;
			String recordStoreNameProperty = properties.getProperty(propertyName);
			
			if (recordStoreNameProperty != null
					&& recordStoreNameProperty.length() > 0
					&& recordStoreNameProperty.length() < 32) {
				recordStoreName = recordStoreNameProperty;
			}
		}

		if (recordStoreName == null) {
			recordStoreName = MIDPConstants.RECORD_STORE_DEFAULT_NAME;
		}

		return recordStoreName;
	}
}
