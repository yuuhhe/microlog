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

import java.io.IOException;

import javax.microedition.lcdui.Form;

import net.sf.microlog.core.Appender;
import net.sf.microlog.core.Level;
import net.sf.microlog.core.appender.AbstractAppender;

/**
 * An Appender that appends the logging to a Form.
 * 
 * Each logging is appended as a StringItem. The log is cleared by deleting all
 * the items in the Form.
 * 
 * @author Johan Karlsson (johan.karlsson@jayway.se)
 * @since 0.1
 */
public class FormAppender extends AbstractAppender {

	private static final String DEFAULT_LOGFORM_TITLE = "Microlog Form";

	private Form logForm;

	private long logSize;

	/**
	 * Create a <code>FormAppender</code> without a default logform.
	 */
	public FormAppender() {
		super();
	}

	/**
	 * Create a <code>FormAppender</code> that uses the specified
	 * <code>Form</code> to log.
	 * 
	 * @param logForm
	 *            the <code>Form</code> to log to.
	 * @throws IllegalArgumentException
	 *             if the <code>Form</code> is null.
	 * 
	 */
	public FormAppender(Form logForm) throws IllegalArgumentException {
		super();

		if (logForm == null) {
			throw new IllegalArgumentException("The logForm must not be null.");
		}

		this.logForm = logForm;
	}

	/**
	 * Get the <code>Form</code> that is used for logging.
	 * 
	 * @return Returns the logForm.
	 */
	public final Form getLogForm() {
		return logForm;
	}

	/**
	 * Set the <code>Form</code> that shall be used for logging. Note: the form
	 * is ignored if the log is open.
	 * 
	 * @param logForm
	 *            The logForm to set.
	 * @throws IllegalArgumentException
	 *             if the <code>Form</code> is null.
	 */
	public final void setLogForm(Form logForm) throws IllegalArgumentException {
		if (logForm == null) {
			throw new IllegalArgumentException("The logForm must not be null.");
		}
		
		synchronized (this.logForm) {
			this.logForm = logForm;
			logSize = 0;
		}
	}

	/**
	 * Do the logging.
	 * 
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
			logForm.append(formatter.format(clientID, name, time, level,
					message, t));
			logSize++;
		}
	}

	/**
	 * Clear the underlying RecordStore from data. Note if logging is done when
	 * executing this method, these new logging events are not cleared.
	 * 
	 * @see net.sf.microlog.core.appender.AbstractAppender#clear()
	 */
	public void clear() {
		if (logForm != null) {
			synchronized (this.logForm) {
				logForm.deleteAll();
				logSize = 0;
			}
		}
	}

	/**
	 * @see net.sf.microlog.core.appender.AbstractAppender#close()
	 */
	public void close() throws IOException {
		logOpen = false;
	}

	/**
	 * @see net.sf.microlog.core.appender.AbstractAppender#open()
	 */
	public synchronized void open() throws IOException {
		if (logForm == null) {
			this.logForm = new Form(DEFAULT_LOGFORM_TITLE);
		}
		logOpen = true;
	}

	/**
	 * Get the size of the log. The size is the number of items logged.
	 * 
	 * @return the size of the log.
	 */
	public long getLogSize() {
		return logSize;
	}
	
	/**
	 * @see Appender#getPropertyNames()
	 */
	public String[] getPropertyNames() {
		return null;
	}
	
	/**
	 * @see Appender#setProperty(String, String)
	 */
	public void setProperty(String name, String value)
			throws IllegalArgumentException {
		// Ignore since we have no properties to set.
	}
}
