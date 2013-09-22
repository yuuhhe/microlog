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

import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;

import net.sf.microlog.core.CyclicBuffer;
import net.sf.microlog.core.Level;
import net.sf.microlog.core.appender.AbstractAppender;

/**
 * This is the superclass for the message based appenders (with cyclic buffers).
 * 
 * @author Johan Karlsson (johan.karlsson@jayway.se)
 */
public abstract class AbstractMessageAppender extends AbstractAppender {

	public static final int DEFAULT_MESSAGE_BUFFER = 255;

	/**
	 * The default buffer size for a message appender.
	 */
	public static final int DEFAULT_BUFFER_SIZE = 10;

	/**
	 * The line feed used by SMS messages.
	 */
	public static final char GSM_7_BIT_LF = 0x0A;

	private int bufferSize = DEFAULT_BUFFER_SIZE;
	private CyclicBuffer buffer = new CyclicBuffer(DEFAULT_BUFFER_SIZE);
	private Level triggerLevel = Level.ERROR;
	protected String lineSeparator = new String(new char[] { GSM_7_BIT_LF });
	protected MessageConnection messageConnection;
	
	public AbstractMessageAppender(){
		super();
		
	}

	/**
	 * Open the log.
	 * 
	 * @see net.sf.microlog.core.appender.AbstractAppender#open()
	 */
	public abstract void open() throws IOException;

	/**
	 * Clear the log, i.e. the buffer is cleared.
	 * 
	 * @see net.sf.microlog.core.appender.AbstractAppender#clear()
	 */
	public synchronized void clear() {
		if (buffer != null) {
			buffer.clear();
		}
	}

	/**
	 * Close the log.
	 * 
	 * @see net.sf.microlog.core.appender.AbstractAppender#close()
	 */
	public synchronized void close() throws IOException {
		if (messageConnection != null) {
			messageConnection.close();
		}
		logOpen = false;
	}

	/**
	 * Perform the actual logging.
	 * 
	 * @see net.sf.microlog.core.appender.AbstractAppender#doLog(String, String,
	 *      long, net.sf.microlog.core.Level, java.lang.Object, java.lang.Throwable)
	 */
	public synchronized void doLog(String clientID, String name, long time,
			Level level, Object message, Throwable t) {

		if (logOpen && formatter != null) {
			buffer.add(formatter.format(clientID, "", time, level, message, t));
		}

		if (level.toInt() >= triggerLevel.toInt()) {
			final String messageContent = createMessageContent();
			new Thread(new Runnable() {
				public void run() {
					sendLog(messageContent);
				}
			}).start();
		}
	}

	abstract void sendLog(String messageContent);

	/**
	 * Open the <code>MessageConnection</code> to use for sending messages.
	 * 
	 * @throws IOException
	 *             if we fail to open the connection.
	 */
	protected synchronized void openConnection(String connectionString)
			throws IOException {
		if (messageConnection == null) {
			messageConnection = (MessageConnection) Connector
					.open(connectionString);
		}
	}

	/**
	 * Create a message that is a concatenation of all the messages stored in
	 * the buffer. The buffer is also cleared.
	 * 
	 * @return a <code>String</code> that contains the message.
	 */
	protected synchronized String createMessageContent() {

		// Create the message from the buffer.
		StringBuffer messageContentBuffer = new StringBuffer(DEFAULT_MESSAGE_BUFFER);
		messageContentBuffer.append("Microlog: ");

		Object currentLogItem = buffer.get();

		while (currentLogItem != null) {
			messageContentBuffer.append(currentLogItem);
			if (lineSeparator != null) {
				messageContentBuffer.append(lineSeparator);
			}
			currentLogItem = buffer.get();
		}

		return messageContentBuffer.toString();
	}

	/**
	 * 
	 * @see net.sf.microlog.core.Appender#getLogSize()
	 */
	public synchronized long getLogSize() {
		int logSize = 0;

		if (buffer != null) {
			buffer.length();
		}

		return logSize;
	}

	/**
	 * Set the buffer size, i.e. how many log messages that are stored.
	 * 
	 * @return the bufferSize
	 */
	public synchronized int getBufferSize() {
		return bufferSize;
	}

	/**
	 * Get the buffer size, i.e. how many log messages that are stored.
	 * 
	 * @param bufferSize
	 *            the bufferSize to set
	 * @throws IllegalArgumentException
	 *             if the <code>bufferSize</code> is less than 1.
	 */
	public synchronized void setBufferSize(int bufferSize)
			throws IllegalArgumentException {
		if (bufferSize < 1) {
			throw new IllegalArgumentException(
					"The bufferSize must not be less than 1.");
		}

		if (buffer == null) {
			buffer = new CyclicBuffer(bufferSize);
		} else if (buffer != null && bufferSize != buffer.getBufferSize()) {
			buffer = new CyclicBuffer(bufferSize);
		}
	}

	/**
	 * Set the level which should trigger sending an SMS.
	 * 
	 * @return the triggerLevel
	 */
	public Level getTriggerLevel() {
		return triggerLevel;
	}

	/**
	 * Get the level which should trigger sending an SMS.
	 * 
	 * @param triggerLevel
	 *            the triggerLevel to set
	 */
	public void setTriggerLevel(Level triggerLevel)
			throws IllegalArgumentException {
		if (triggerLevel == null) {
			throw new IllegalArgumentException(
					"The triggerLevel must not be null.");
		}

		this.triggerLevel = triggerLevel;

	}

	/**
	 * Get the line separator used between each log message.
	 * 
	 * @param lineSeparator
	 *            the lineSeparator to set
	 * @throws IllegalArgumentException
	 *             if the <code>lineSeparator</code> is <code>null</code>.
	 */
	public synchronized void setLineSeparator(String lineSeparator)
			throws IllegalArgumentException {
		if (lineSeparator == null) {
			throw new IllegalArgumentException(
					"The lineSeparator must not be null.");
		}

		this.lineSeparator = lineSeparator;
	}

	/**
	 * Set the <code>MessageConnection</code>. This should be used for
	 * testing purposes only.
	 * 
	 * @param messageConnection
	 *            the messageConnection to set
	 */
	synchronized void setMessageConnection(MessageConnection messageConnection) {
		this.messageConnection = messageConnection;
	}

}