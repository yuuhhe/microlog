package net.sf.microlog.server.btspp.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.sf.microlog.midp.bluetooth.BluetoothServerListener;
import net.sf.microlog.midp.bluetooth.server.BluetoothSerialServerThread;
import net.sf.microlog.midp.bluetooth.server.SystemInfo;

public class MicrologBluetoothServerUI implements BluetoothServerListener {

	private final static String newline = "\n";

	private final String btAddress;

	public JFrame frame;
	private JTextField serverURLTextField;
	private JLabel statusLabel;

	private List<String> connectedClients = new ArrayList<String>();

	private Object clientConnectionLock = new Object();

	private JTextArea textArea;
	private JList clientList;
	private JScrollPane logScrollPane;

	/**
	 * Launch the application
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MicrologBluetoothServerUI window = new MicrologBluetoothServerUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * 
	 * @wbp.parser.entryPoint
	 */
	public MicrologBluetoothServerUI() {
		initialize();

		BluetoothSerialServerThread btThread = new BluetoothSerialServerThread();
		btThread.setServerListener(this);

		btAddress = btThread.getLocalBtAddress();

		Executor executor = Executors.newSingleThreadExecutor();
		executor.execute(btThread);
	}

	public void serverStarted(String url) {
		getServerURLTextField().setText(url);
		getStatusLabel().setText("Running");
	}

	public void clientAccepted(String address, String name) {
		synchronized (clientConnectionLock) {
			System.out.println("clientAccepted()");
			connectedClients.add(name);
			getClientList().setListData(connectedClients.toArray());
		}
	}

	public synchronized void messageReceived(String message) {
		if (message != null && message.length() > 0) {
			JTextArea textArea = getTextArea();
			textArea.append(message);
			textArea.append(newline);

			textArea.setCaretPosition(textArea.getDocument().getLength());
		}
	}

	public synchronized void clientDisconnected(String address, String name) {
		synchronized (clientConnectionLock) {
			System.out.println("clientDisconnected()");
			connectedClients.remove(name);
			getClientList().setListData(connectedClients.toArray());
		}
	}

	public void shutdown() {
		System.exit(0);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		validateRunningOs();

		frame = new JFrame();
		frame.setTitle("Microlog Bluetooth Server");
		{
			JMenuBar menuBar = new JMenuBar();
			frame.setJMenuBar(menuBar);
			{
				JMenu mnFile = new JMenu("File");
				menuBar.add(mnFile);
				{
					JMenuItem mntmSaveLogAs = new JMenuItem("Save Log As ...");
					mntmSaveLogAs.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							showSaveLogFileAsDialog();
						}
					});
					{
						JMenuItem mntmClearLog = new JMenuItem("Clear Log");
						mntmClearLog.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								getTextArea().setText("");
							}
						});
						mnFile.add(mntmClearLog);
					}
					mnFile.add(mntmSaveLogAs);
				}
				{
					JMenuItem mntmExit = new JMenuItem("Exit");
					mntmExit.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							System.exit(0);
						}
					});
					mnFile.add(mntmExit);
				}
			}
			{
				JMenu mnHelp = new JMenu("Help");
				menuBar.add(mnHelp);
				{
					JMenuItem mntmBtAddress = new JMenuItem(
							"Server Bluetooth address");
					mntmBtAddress.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							JOptionPane.showMessageDialog(null, btAddress,
									"Server Bluetooth address",
									JOptionPane.INFORMATION_MESSAGE);
						}
					});
					mnHelp.add(mntmBtAddress);
					JMenuItem mntmAbout = new JMenuItem("About");
					mntmAbout.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							AboutDialog aboutDialog = new AboutDialog();
							aboutDialog.setModal(true);
							aboutDialog.setVisible(true);
						}
					});
					mnHelp.add(mntmAbout);
				}
			}
		}
		frame.setBounds(100, 100, 793, 499);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		{
			JPanel topPanel = new JPanel();
			FlowLayout flowLayout = (FlowLayout) topPanel.getLayout();
			flowLayout.setAlignment(FlowLayout.LEFT);
			frame.getContentPane().add(topPanel, BorderLayout.NORTH);
			{
				JLabel lblServerUrl = new JLabel("Server URL:");
				topPanel.add(lblServerUrl);
			}
			{
				serverURLTextField = new JTextField();
				serverURLTextField.setEditable(false);
				topPanel.add(serverURLTextField);
				serverURLTextField.setColumns(40);
			}
		}
		{
			JPanel statusPanel = new JPanel();
			FlowLayout flowLayout = (FlowLayout) statusPanel.getLayout();
			flowLayout.setAlignment(FlowLayout.LEFT);
			frame.getContentPane().add(statusPanel, BorderLayout.SOUTH);
			{
				JLabel lblServerStatus = new JLabel("Server Status:");
				statusPanel.add(lblServerStatus);
			}
			{
				statusLabel = new JLabel("Not Started");
				statusPanel.add(statusLabel);
			}
		}
		{
			JSplitPane splitPane = new JSplitPane();
			frame.getContentPane().add(splitPane, BorderLayout.CENTER);
			{
				JScrollPane clientScrollPane = new JScrollPane();
				splitPane.setLeftComponent(clientScrollPane);
				{
					clientList = new JList();
					clientList.setListData(connectedClients.toArray());
					clientScrollPane.setViewportView(clientList);
				}
			}
			{
				logScrollPane = new JScrollPane();
				splitPane.setRightComponent(logScrollPane);
				{
					textArea = new JTextArea();
					logScrollPane.setViewportView(textArea);
				}
			}
		}
	}

	protected JTextArea getTextArea() {
		return textArea;
	}

	/**
	 * Save the log file as ...
	 */
	private void showSaveLogFileAsDialog() {
		// Create a file chooser
		final JFileChooser fileChooser = new JFileChooser();

		// In response to a button click:
		int returnVal = fileChooser.showSaveDialog(frame);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();

			saveLogFile(file);
		}
	}

	private void saveLogFile(File file) {

		// Create the file if it does not exists
		if (!file.exists()) {
			try {
				boolean created = file.createNewFile();

				if (!created) {
					// TODO inform the user that it is trying to overwrite an
					// existing file.
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Writer output = null;

		try {
			output = new BufferedWriter(new FileWriter(file));
			String logText = getTextArea().getText();
			output.write(logText);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Shows an error message if the application is started in Mac OS X. This is
	 * due to problems with the BlueCove Bluetooth library in this operating
	 * system. The application is still started after the error message.
	 */
	private void validateRunningOs() {
		final String os = SystemInfo.OS_NAME.getValue();

		if (os != null && os.contains("Mac")) {
			JOptionPane
					.showMessageDialog(
							null,
							"The Microlog Bluetooth Server does not function correctly on Mac OS X because of the underlying Bluetooth library.",
							"Microlog Bluetooth Server",
							JOptionPane.ERROR_MESSAGE);
		}
	}

	protected JTextField getServerURLTextField() {
		return serverURLTextField;
	}

	protected JLabel getStatusLabel() {
		return statusLabel;
	}

	protected JList getClientList() {
		return clientList;
	}

	protected JScrollPane getLogScrollPane() {
		return logScrollPane;
	}
}
