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

import net.sf.microlog.core.Level;
import net.sf.microlog.core.MicrologConstants;
import net.sf.microlog.core.SyslogMessage;

/**
 * This <code>Appender</code> is used for sending the log messages to a syslog
 * daemon. It is basically an UDP Datagram that is sent on port 514. The format
 * of the mssage is described in rfc 3164. Each time a message is logged, the
 * message is sent directly to the server. A <code>DatagramConnection</code>
 * is used, which requires MIDP 2.0.
 * 
 * This has been tested with the Kiwi syslog daemon for Windows (freeware
 * edition). For more information: http://www.kiwisyslog.com/ Note: the Kiwi
 * syslog daemon does not parse the header field by default (v 8.3.30). The
 * <code>SyslogAppender</code> has the possibility to switch the header
 * generation on/off. This is done with the <code>setHeader()</code> method.
 * 
 * @author Johan Karlsson (johan.karlsson@jayway.se)
 * @since 0.6
 * 
 */
public class SyslogAppender extends DatagramAppender {
	
	String hostname = MicrologConstants.DEFAULT_HOST;
	
	SyslogMessage syslogMessage = new SyslogMessage();

	/**
	 * Create a <code>SyslogAppender</code> with the default port for syslog
	 * (514).
	 */
	public SyslogAppender() {
		super.setPort(SyslogMessage.DEFAULT_SYSLOG_PORT);
		
		syslogMessage.setTag(MicrologConstants.DEFAULT_SYSLOG_TAG);
		syslogMessage.setFacility(SyslogMessage.FACILITY_USER_LEVEL_MESSAGE);
		syslogMessage.setSeverity(SyslogMessage.SEVERITY_DEBUG);

		String hostNameProperty = System.getProperty("microedition.hostname");
		if (hostNameProperty != null && hostNameProperty.length() > 0) {
			hostname = hostNameProperty;
		}
	}

	/**
	 * Do the logging.
	 * @param level
	 *            the level to use for the logging.
	 * @param message
	 *            the message to log.
	 * @param t
	 *            the exception to log.
	 */
	public void doLog(String clientID, String name, long time, Level level,
			Object message, Throwable t) {
		if (logOpen && formatter != null) {
			sendMessage(syslogMessage.createMessageData(formatter.format(clientID, name, time, level, message, t)));
		}
	}

	/**
	 * Set the facility that is used when sending message.
	 * 
	 * @param facility
	 *            the facility to set
	 * @throws IllegalArgumentException
	 *             if the facility is not a valid one.
	 */
	public void setFacility(byte facility) {
		syslogMessage.setFacility(facility);
	}

	/**
	 * Get the severity at which the message shall be sent.
	 * 
	 * @param severity
	 *            the severity to set
	 * @throws IllegalArgumentException
	 *             if the severity is not a valid severity.
	 */
	public void setSeverity(byte severity) throws IllegalArgumentException {
		syslogMessage.setSeverity(severity);
	}

	/**
	 * Indicates whether the HEADER part of the message. If this is true, the
	 * HEADER part is created.
	 * 
	 * @param header
	 *            the addHeader to set
	 */
	public void setHeader(boolean header) {
		syslogMessage.setHeader(header);
	}

	/**
	 * Set the hostname to use for the HOSTNAME field of the syslog message.
	 * 
	 * @param hostname
	 *            the hostname to set
	 * @throws IllegalArgumentException
	 *             if the <code>hostname</code> is <code>null</code> or the
	 *             length is less than 1
	 */
	public void setHostname(String hostname) throws IllegalArgumentException {
		syslogMessage.setHostname(hostname);
	}

	/**
	 * Set the tag that is used for the TAG field in the MSG part of the
	 * message. The TAG length must not exceed 32 chars.
	 * 
	 * @param tag
	 *            the tag to set
	 * @throws IllegalArgumentException
	 *             if the tag is null or the length is incorrect.
	 */
	public void setTag(String tag) throws IllegalArgumentException {
		syslogMessage.setTag(tag);
	}
	
}
