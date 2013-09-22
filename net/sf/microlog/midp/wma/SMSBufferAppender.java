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
package net.sf.microlog.midp.wma;

import java.io.IOException;
import java.io.InterruptedIOException;

import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import net.sf.microlog.core.Appender;
import net.sf.microproperties.Properties;

/**
 * Log messages into a buffer and send it as an SMS (TextMessage) when
 * triggered.
 * 
 * The sending could be triggered manually or when a message is logged at a
 * certain level, typically when a ERROR or FATAL message has been logged. It is
 * possible to set the trigger level by changing the property
 * <code>triggerLevel</code>.
 * 
 * The size of the buffer is set by the property <code>bufferSize</code>.
 * 
 * Note: this requires an implementation of JSR-120 on the target platform.
 * 
 * @author Johan Karlsson (johan.karlsson@jayway.se)
 * @since 0.6
 */
public class SMSBufferAppender extends AbstractMessageAppender {

	public static final String MESSAGE_RECEIVER_PROPERTY = "messageReceiver";

	public static final String DEFAULT_MESSAGE_RECEIVER = "";

	public static final String[] PROPERTY_NAMES = { SMSBufferAppender.MESSAGE_RECEIVER_PROPERTY };

	public static final String[] DEFAULT_VALUES = { DEFAULT_MESSAGE_RECEIVER };

	private String messageReceiver;

	/**
	 * The default constructor for <code>SMSBufferAppender</code>.
	 */
	public SMSBufferAppender() {
		super();
		lineSeparator = new String(new char[] { GSM_7_BIT_LF });
	}

	/**
	 * @see net.sf.microlog.midp.wma.AbstractMessageAppender#open()
	 */
	public synchronized void open() throws IOException {
		String connectionString = "sms://" + messageReceiver;
		openConnection(connectionString);
		logOpen = true;
	}

	/**
	 * Send the current log.
	 * 
	 */
	synchronized void sendLog(String messageContent) {
		if (messageReceiver != null) {
			TextMessage message = (TextMessage) messageConnection
					.newMessage(MessageConnection.TEXT_MESSAGE);

			message.setPayloadText(messageContent);

			try {
				messageConnection.send(message);
			} catch (InterruptedIOException e) {
				System.err.println("Interrupted while sendinf the log " + e);
			} catch (IOException e) {
				System.err.println("Failed to send the log " + e);
			}

		} else {
			System.err.println("A message receiver is not set.");
		}
	}

	/**
	 * Get the message receiver. This should be a valid telephone number.
	 * 
	 * @return the messageReceiver
	 */
	public synchronized String getMessageReceiver() {
		return messageReceiver;
	}

	/**
	 * Set the message receiver. This should be a valid telephone number.
	 * 
	 * @param messageReceiver
	 *            the messageReceiver to set.
	 * @throws IllegalArgumentException
	 *             if the <code>messageReceiver</code> is null.
	 */
	public synchronized void setMessageReceiver(String messageReceiver)
			throws IllegalArgumentException {
		if (messageReceiver == null) {
			throw new IllegalArgumentException(
					"The messageReceiver must not be null");
		}

		this.messageReceiver = messageReceiver;
	}

	/**
	 * Configure the <code>SMSBufferAppender</code> with the supplied properties
	 * object.
	 */
	public synchronized void configure(Properties properties) {
		if (properties != null) {
			String newMessageReceiver = properties
					.getProperty(SMSBufferAppender.MESSAGE_RECEIVER_PROPERTY);
			if (newMessageReceiver != null) {
				this.messageReceiver = newMessageReceiver;
			}
		}
	}

	/**
	 * @see Appender#getPropertyNames()
	 */
	public String[] getPropertyNames() {
		return SMSBufferAppender.PROPERTY_NAMES;
	}

	public void setProperty(String name, String value)
			throws IllegalArgumentException {
		super.setProperty(name, value);

		if (name.equals(MESSAGE_RECEIVER_PROPERTY)) {
			setMessageReceiver(value);
		}
	}

}
