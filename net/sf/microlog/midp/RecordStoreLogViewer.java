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

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import net.sf.microlog.core.MicrologConstants;
import net.sf.microproperties.Properties;
import net.sf.microproperties.midp.MidletProperties;

/**
 * A MIDlet that is used for viewing a log created with the RecordStoreAppender.
 * 
 * @author Johan Karlsson (johan.karlsson@jayway.se)
 * @author Darius Katz
 */
public class RecordStoreLogViewer extends MIDlet implements CommandListener {

	private static final int MAX_NAME_LENGTH = 32;

	private final Display display;

	private final Form logScreen;

	private final Form preferenceScreen;

	private final TextField nameField;

	private final Command loadLogCommand = new LoadLogCommand();

	private final Command clearLogCommand = new ClearLogCommand();

	private final Command exitCommand = new ExitCommand();

	private final Command preferenceCommand = new PreferenceCommand();

	private final Command preferenceOkCommand = new PreferenceOkCommand();

	private final Command preferenceCancelCommand = new PreferenceCancelCommand();

	private final Command switchComparatorCommand = new SwitchComparatorCommand(
			(AbstractCommand) loadLogCommand);
	
	RecordStoreLogLoader loader;

	/**
	 * Create a RecordStoreLogViewer.
	 */
	public RecordStoreLogViewer() {
		super();
		display = Display.getDisplay(this);

		logScreen = new Form("Log content");
		logScreen.addCommand(loadLogCommand);
		logScreen.addCommand(clearLogCommand);
		logScreen.addCommand(preferenceCommand);
		logScreen.addCommand(exitCommand);
		logScreen.addCommand(switchComparatorCommand);
		logScreen.setCommandListener(this);

		preferenceScreen = new Form("Preferences");
		nameField = new TextField("RecordStore name ", null, MAX_NAME_LENGTH,
				TextField.ANY);
		preferenceScreen.append(nameField);
		preferenceScreen.addCommand(preferenceOkCommand);
		preferenceScreen.addCommand(preferenceCancelCommand);
		preferenceScreen.addCommand(exitCommand);
		preferenceScreen.setCommandListener(this);

	}

	/**
	 * Start the MIDlet.
	 * 
	 * @throws MIDletStateChangeException
	 *             if the MIDlet fails to change the state.
	 */
	protected void startApp() throws MIDletStateChangeException {
		Properties properties = new MidletProperties(this, MicrologConstants.DEFAULT_PROPERTY_FILE);
		loader = new RecordStoreLogLoader(properties);
		display.setCurrent(logScreen);
	}

	/**
	 * Pause the MIDlet.
	 */
	protected void pauseApp() {
	}

	/**
	 * Destroy the MIDlet.
	 * 
	 * @param unconditional
	 *            If true when this method is called, the MIDlet must cleanup
	 *            and release all resources. If false the MIDlet may throw
	 *            <code>MIDletStateChangeException</code> to indicate it does
	 *            not want to be destroyed at this time.
	 * @throws MIDletStateChangeException
	 *             if the MIDlet fails to change the state.
	 */
	protected void destroyApp(boolean unconditional)
			throws MIDletStateChangeException {
	}

	

	/**
	 * Implementation of the CommandListener interface.
	 * 
	 * @param cmd
	 *            the command.
	 * @param displayable
	 *            the displayable that generated the command action.
	 */
	public void commandAction(Command cmd, Displayable displayable) {
		if (cmd instanceof AbstractCommand) {
			AbstractCommand myCmd = (AbstractCommand) cmd;
			myCmd.execute();
		}
	}

	/**
	 * The super class for all the Command objects.
	 * 
	 * @author Johan Karlsson
	 */
	protected abstract class AbstractCommand extends Command {

		/**
		 * Create an AbstractCommand.
		 * 
		 * @param label
		 *            the label to use.
		 * @param commandType
		 *            the type of <code>Command</code>.
		 * @param priority
		 *            the priority of the <code>Command</code>.
		 */
		public AbstractCommand(String label, int commandType, int priority) {
			super(label, commandType, priority);
		}

		/**
		 * Create an AbstractCommand.
		 * 
		 * @param shortLabel
		 *            the short label to use.
		 * @param longLabel
		 *            the long label to use.
		 * @param commandType
		 *            the type of <code>Command</code>.
		 * @param priority
		 *            the priority of the <code>Command</code>.
		 */
		public AbstractCommand(String shortLabel, String longLabel,
				int commandType, int priority) {
			super(shortLabel, longLabel, commandType, priority);
		}

		/**
		 * Execute the command.
		 */
		public abstract void execute();
	}

	/**
	 * A command that loads the log.
	 * 
	 * @author Johan Karlsson
	 */
	protected class LoadLogCommand extends AbstractCommand {

		/**
		 * Create a LoadLogCommand object.
		 */
		public LoadLogCommand() {
			super("Load", "Load Log", Command.ITEM, 1);
		}

		/**
		 * Execute the command.
		 * 
		 * @see net.sf.microlog.midp.RecordStoreLogViewer.AbstractCommand#execute()
		 */
		public void execute() {
			logScreen.deleteAll();
			String logContent = loader.getLogContent();
			logScreen.append(logContent);
		}

	}

	/**
	 * A command that clears the log.
	 * 
	 * @author Johan Karlsson
	 */
	protected class ClearLogCommand extends AbstractCommand {

		/**
		 * Create a ClearLogCommand.
		 */
		public ClearLogCommand() {
			super("Clear", "Clear Log", Command.ITEM, 2);
		}

		/**
		 * Execute the command.
		 * 
		 * @see net.sf.microlog.midp.RecordStoreLogViewer.AbstractCommand#execute()
		 */
		public void execute() {
			logScreen.deleteAll();
			loader.clearLog();
		}
	}

	/**
	 * A command that moves us to the preference screen.
	 * 
	 * @author Johan Karlsson
	 */
	protected class PreferenceCommand extends AbstractCommand {

		/**
		 * Create an PreferenceCommand.
		 */
		public PreferenceCommand() {
			super("Preferences", Command.ITEM, 3);
		}

		/**
		 * @see net.sf.microlog.midp.RecordStoreLogViewer.AbstractCommand#execute()
		 */
		public void execute() {
			nameField.setString(loader.getRecordStoreName());
			display.setCurrent(preferenceScreen);
		}

	}

	/**
	 * A command that exit the MIDlet.
	 * 
	 * @author Johan Karlsson
	 */
	protected class ExitCommand extends AbstractCommand {

		/**
		 * Create an PreferenceCancelCommand.
		 */
		public ExitCommand() {
			super("Exit", Command.EXIT, 1);
		}

		/**
		 * @see net.sf.microlog.midp.RecordStoreLogViewer.AbstractCommand#execute()
		 */
		public void execute() {
			notifyDestroyed();
		}
	}

	/**
	 * Accept the preferences.
	 * 
	 * @author Johan Karlsson
	 */
	protected class PreferenceOkCommand extends AbstractCommand {

		/**
		 * Create an PreferenceCancelCommand.
		 */
		public PreferenceOkCommand() {
			super("OK", Command.OK, 1);
		}

		/**
		 * @see net.sf.microlog.midp.RecordStoreLogViewer.AbstractCommand#execute()
		 */
		public void execute() {
			String newName = nameField.getString();
			if (newName != null && newName.length() > 0) {
				loader.setRecordStoreName(newName);
			} else {
				showInfoAlert("Please enter a name.", null);
			}
		}

	}

	/**
	 * Cancel the preferences.
	 * 
	 * @author Johan Karlsson
	 */
	protected class PreferenceCancelCommand extends AbstractCommand {

		/**
		 * Create an PreferenceCancelCommand.
		 */
		public PreferenceCancelCommand() {
			super("Cancel", Command.CANCEL, 1);
		}

		/**
		 * @see net.sf.microlog.midp.RecordStoreLogViewer.AbstractCommand#execute()
		 */
		public void execute() {
			display.setCurrent(logScreen);
		}
	}

	/**
	 * Switch between RecordComparators. The
	 * 
	 * @author Darius Katz
	 */
	protected class SwitchComparatorCommand extends AbstractCommand {

		private final AbstractCommand performThisCommand;

		/**
		 * Create a SwitchComparatorCommand.
		 * 
		 * @param command
		 *            The AbstractCommand to perform after switching
		 *            RecordComparator. Ignored if <code>null</code>.
		 */
		public SwitchComparatorCommand(AbstractCommand command) {
			super("Switch sort-order", Command.ITEM, 3);
			performThisCommand = command;
		}

		/**
		 * @see net.sf.microlog.midp.RecordStoreLogViewer.AbstractCommand#execute()
		 */
		public void execute() {
			loader.switchSortOrder();

			if (performThisCommand != null) {
				performThisCommand.execute();
			}
		}
	}

	/**
	 * Show an information alert.
	 * 
	 * @param message
	 *            the message to show.
	 * @param exception
	 *            an optional exception to append to the message. Could be null.
	 */
	protected void showInfoAlert(String message, Throwable exception) {
		StringBuffer buffer = new StringBuffer(message);
		if (exception != null) {
			buffer.append("\r\n");
			buffer.append(exception);
		}

		Alert alert = new Alert("Info", buffer.toString(), null, AlertType.INFO);
		display.setCurrent(alert, display.getCurrent());
	}

}
