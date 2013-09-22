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
import java.util.Vector;

import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessagePart;
import javax.wireless.messaging.MultipartMessage;

import net.sf.microlog.core.Appender;

/**
 * Log messages into a buffer and send it as an MMS (MultipartsMessage) when
 * triggered.
 * 
 * The sending could be triggered manually or when a message is logged at a
 * certain level, typically when a ERROR or FATAL message has been logged. It is
 * possible to set the trigger level by changing the property
 * <code>triggerLevel</code>.
 * 
 * The size of the buffer is set by the property <code>bufferSize</code>.
 * 
 * This class requires an implementation of JSR-205 (WMA 2.0 or better).
 * 
 * Note: this requires an implementation of JSR-205 on the target platform.
 * 
 * @author Johan Karlsson (johan.karlsson@jayway.se)
 * @since 0.6
 */
public class MMSBufferAppender extends AbstractMessageAppender {

	private static final String CONTENT_LOCATION = "contentLocation";

	public static final String CONTENT_ID_PROPERTY = "contentID";

	public static final String MIME_TYPE_PROPERTY = "mimeType";

	public static final String ENCODING_PROPERTY = "encoding";

	public static final String PRIORITY_PROPERTY = "priority";

	public static final String SUBJECT_PROPERTY = "subject";

	public final static String HIGH_PRIORITY = "high";

	public final static String NORMAL_PRIORITY = "normal";

	public final static String LOW_PRIORITY = "low";

	public static final String DEFAULT_SUBJECT = "Microlog log";

	public static final String DEFAULT_PRIORITY = HIGH_PRIORITY;

	public static final String DEFAULT_ENCODING = "UTF-8";

	public static final String DEFAULT_MIME_TYPE = "plain/text";

	private static final String DEFAULT_CONTENT_ID = "Microlog";

	private static final String DEFAULT_CONTENT_LOCATION = "Microlog.txt";

	public static final int DEFAULT_MMS_BUFFER_SIZE = 20;

	public static final String[] PROPERTY_NAMES = {
			MMSBufferAppender.SUBJECT_PROPERTY,
			MMSBufferAppender.PRIORITY_PROPERTY,
			MMSBufferAppender.ENCODING_PROPERTY,
			MMSBufferAppender.MIME_TYPE_PROPERTY,
			MMSBufferAppender.CONTENT_ID_PROPERTY,
			MMSBufferAppender.CONTENT_LOCATION };

	private final Vector addressVector = new Vector();

	private String subject = DEFAULT_SUBJECT;

	private String priority = HIGH_PRIORITY;

	private String encoding = DEFAULT_ENCODING;

	private String mimeType = DEFAULT_MIME_TYPE;

	private String contentId = DEFAULT_CONTENT_ID;

	private String contentLocation = DEFAULT_CONTENT_LOCATION;

	/**
	 * The default constructor.
	 * 
	 * The buffer size is set to 20.
	 */
	public MMSBufferAppender() {
		super();
		super.setBufferSize(DEFAULT_MMS_BUFFER_SIZE);
		lineSeparator = "\r\n";
	}

	/**
	 * @see net.sf.microlog.midp.wma.AbstractMessageAppender#open()
	 */
	public synchronized void open() throws IOException {
		String connectionString = "mms://:sf.net.microlog";
		openConnection(connectionString);
		logOpen = true;
	}

	/**
	 * Send the current log.
	 * 
	 */
	synchronized void sendLog(String messageContent) {
		if (addressVector != null && addressVector.size() > 0) {
			MultipartMessage message = (MultipartMessage) messageConnection
					.newMessage(MessageConnection.MULTIPART_MESSAGE);
			message.setSubject(subject);
			message.setHeader("X-Mms-Priority", priority);

			try {
				byte[] byteData;
				if (encoding != null && encoding.length() > 0) {
					byteData = messageContent.getBytes(encoding);
				} else {
					byteData = messageContent.getBytes();
				}
				MessagePart messagePart = new MessagePart(byteData, mimeType,
						contentId, contentLocation, encoding);
				message.addMessagePart(messagePart);

				for (int index = 0; index < addressVector.size(); index++) {
					Object address = addressVector.elementAt(index);
					message.addAddress("to", address.toString());
				}

				messageConnection.send(message);
			} catch (InterruptedIOException e) {
				System.err.println("Interrupted while sending the log. " + e);
			} catch (IOException e) {
				System.err.println("Failed to send the log. " + e);
			}

		} else {
			System.err.println("A message receiver is not set.");
		}
	}

	/**
	 * Add an address that the log will be sent to. The address could be a phone
	 * number or an e-mail address.
	 * 
	 * Note: not all network operators support sending an MMS as an e-mail.
	 * Please contact your operator to make sure that this is supported before
	 * filing a bug report to Microlog.
	 * 
	 * @param address
	 *            the address to add. The address must have a length > 0 & not
	 *            null to be added. No check is done whether the address is
	 *            correct.
	 * @throws IllegalArgumentException
	 *             if the address is null or the length is less than 1.
	 */
	public void addAddress(String address) throws IllegalArgumentException {
		if (address == null || (address != null && address.length() < 1)) {
			throw new IllegalArgumentException(
					"The address must not be null and have a length that is greater than 1");
		}

		if (!address.startsWith("mms://")) {
			address = "mms://" + address;
		}

		addressVector.addElement(address);

	}

	/**
	 * Removes the specified address.
	 * 
	 * @param address
	 *            the address to remove. The address must have a length > 0 &
	 *            not null to be removed.
	 */
	public void removeAddress(String address) {
		if (address != null && address.length() > 0) {
			addressVector.removeElement(address);
		}
	}

	/**
	 * Remove all the addresses.
	 */
	public void removeAllAddreses() {
		addressVector.removeAllElements();
	}

	/**
	 * Set the subject of the message.
	 * 
	 * @param subject
	 *            the subject to set
	 * 
	 */
	public synchronized void setSubject(String subject)
			throws IllegalArgumentException {
		if (subject == null) {
			throw new IllegalArgumentException("The subject must not be null.");
		}

		this.subject = subject;
	}

	/**
	 * Get the priority of message to send.
	 * 
	 * @return the priority
	 */
	public String getPriority() {
		return priority;
	}

	/**
	 * Set the priority of the message to send. Use one of the constants
	 * HIGH_PRIORITY, NORMAL_PRIORITY or LOW_PRIORITY.
	 * 
	 * @param priority
	 *            the priority to set
	 * @throws IllegalArgumentException
	 *             if the priority is null.
	 */
	public void setPriority(String priority) throws IllegalArgumentException {
		if (priority == null) {
			throw new IllegalArgumentException("The priority must not be null.");
		}

		if (priority.compareTo(HIGH_PRIORITY) == 0
				|| priority.compareTo(NORMAL_PRIORITY) == 0
				|| priority.compareTo(LOW_PRIORITY) == 0) {
			this.priority = priority;
		}
	}

	/**
	 * Set the encoding for the message to be sent.
	 * 
	 * @param encoding
	 *            the encoding to set
	 */
	public synchronized void setEncoding(String encoding)
			throws IllegalArgumentException {
		if (encoding == null) {
			throw new IllegalArgumentException("The encoding must not be null.");
		}

		this.encoding = encoding;
	}

	/**
	 * Set the mime type to be used for the message to be sent.
	 * 
	 * @param mimeType
	 *            the mimeType to set
	 * @throws IllegalArgumentException
	 *             if the <code>mimeType</code> is null.
	 */
	public synchronized void setMimeType(String mimeType)
			throws IllegalArgumentException {
		if (mimeType == null) {
			throw new IllegalArgumentException("The mimeType msut not be null.");
		}

		this.mimeType = mimeType;
	}

	/**
	 * Set the content id for the message to be sent.
	 * 
	 * @param contentId
	 *            the contentId to set
	 */
	public synchronized void setContentId(String contentId)
			throws IllegalArgumentException {
		if (contentId == null) {
			throw new IllegalArgumentException(
					"The contentId must not be null.");
		}

		this.contentId = contentId;

	}

	/**
	 * Set the content location.
	 * 
	 * @param contentLocation
	 *            the contentLocation to set
	 */
	public synchronized void setContentLocation(String contentLocation)
			throws IllegalArgumentException {
		if (contentLocation == null) {
			throw new IllegalArgumentException(
					"The contentLocation must not be null.");
		}

		this.contentLocation = contentLocation;

	}
	
	/**
	 * @see Appender#getPropertyNames()
	 */
	public String[] getPropertyNames() {
		return MMSBufferAppender.PROPERTY_NAMES;
	}
	
	/**
	 * @see Appender#setProperty(String, String)
	 */
	public void setProperty(String name, String value)
			throws IllegalArgumentException {
		super.setProperty(name, value);
		
		if(name.equals(MMSBufferAppender.SUBJECT_PROPERTY)){
			setSubject(value);
		}else if(name.equals(MMSBufferAppender.PRIORITY_PROPERTY)){
			setPriority(value);
		}else if(name.equals(MMSBufferAppender.ENCODING_PROPERTY)){
			setEncoding(value);
		}else if(name.equals(MMSBufferAppender.MIME_TYPE_PROPERTY)){
			setMimeType(value);
		}else if(name.equals(MMSBufferAppender.CONTENT_ID_PROPERTY)){
			setContentId(value);
		}else if(name.equals(MMSBufferAppender.CONTENT_LOCATION)){
			setContentLocation(value);
		}
	}
	
}
