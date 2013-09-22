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

import javax.microedition.rms.RecordComparator;

/**
 * An ascending RecordComparator, based on the timestamp.
 * 
 * @author Darius Katz
 * @author Johan Karlsson (johan.karlsson@jayway.se)
 */
public class AscendingComparator implements RecordComparator {

	private static final int TIME_MASK = 0xFF;

	/**
	 * The compare() implementation, ascending based on the timestamp
	 */
	public int compare(byte[] entry1, byte[] entry2) {

		// Sort based on the timestamp which is the first long in the
		// data/stream
		long timestamp1 = ((long) (entry1[0] & TIME_MASK)) << 56
				| ((long) (entry1[1] & TIME_MASK)) << 48
				| ((long) (entry1[2] & TIME_MASK)) << 40
				| ((long) (entry1[3] & TIME_MASK)) << 32
				| ((long) (entry1[4] & TIME_MASK)) << 24
				| ((long) (entry1[5] & TIME_MASK)) << 16
				| ((long) (entry1[6] & TIME_MASK)) << 8 | (long) (entry1[7] & TIME_MASK);
		long timestamp2 = ((long) (entry2[0] & TIME_MASK)) << 56
				| ((long) (entry2[1] & TIME_MASK)) << 48
				| ((long) (entry2[2] & TIME_MASK)) << 40
				| ((long) (entry2[3] & TIME_MASK)) << 32
				| ((long) (entry2[4] & TIME_MASK)) << 24
				| ((long) (entry2[5] & TIME_MASK)) << 16
				| ((long) (entry2[6] & TIME_MASK)) << 8 | (long) (entry2[7] & TIME_MASK);

		if (timestamp1 < timestamp2) {
			return RecordComparator.PRECEDES;
		} else if (timestamp1 > timestamp2) {
			return RecordComparator.FOLLOWS;
		} else {
			return RecordComparator.EQUIVALENT;
		}
	}

}
