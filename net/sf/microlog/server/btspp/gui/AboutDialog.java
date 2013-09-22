package net.sf.microlog.server.btspp.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import net.sf.microlog.midp.bluetooth.server.SystemInfo;

public class AboutDialog extends JDialog {
	private static final long serialVersionUID = -7774906639142895613L;
	
	private final JPanel contentPanel = new JPanel();
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			AboutDialog dialog = new AboutDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public AboutDialog() {
		setResizable(false);
		setTitle("About");
		setBounds(100, 100, 480, 263);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new BorderLayout(0, 0));
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JTextArea systemInfo = new JTextArea("System info:\n");
			systemInfo.setEditable(false);
			systemInfo.setEnabled(true);
			
			systemInfo.append("-OS:\t" + SystemInfo.OS_NAME.getValue() + "\n");
			systemInfo.append("-Architecture:\t" + SystemInfo.OS_ARCHITECTURE.getValue() + "\n");
			systemInfo.append("-Version:\t" + SystemInfo.OS_VERSION.getValue() + "\n");
			systemInfo.append("-Java version:\t" + SystemInfo.JAVA_VERSION.getValue() + "\n");
			
			JLabel lblThisIsA = new JLabel("Microlog Bluetooth Server GUI by Johan Karlsson & Jarle Hansen");

			
			contentPanel.add(systemInfo, BorderLayout.NORTH);
			lblThisIsA.setHorizontalAlignment(SwingConstants.CENTER);
			contentPanel.add(lblThisIsA, BorderLayout.CENTER);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}

}
