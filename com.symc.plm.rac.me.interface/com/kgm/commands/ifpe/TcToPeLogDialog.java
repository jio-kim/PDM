package com.kgm.commands.ifpe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class TcToPeLogDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextArea logArea = null;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			TcToPeLogDialog dialog = new TcToPeLogDialog(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public TcToPeLogDialog(JDialog parent) {
		super(parent);
		setTitle("Log");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			logArea = new JTextArea();
			
			JScrollPane pane = new JScrollPane();
			pane.setViewportView(logArea);
			pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			pane.getViewport().setBackground(Color.WHITE);
			contentPanel.add(pane, BorderLayout.CENTER);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton cancelButton = new JButton("Close");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						TcToPeLogDialog.this.dispose();
					}
				});
				cancelButton.setActionCommand("Close");
				buttonPane.add(cancelButton);
			}
		}
	}

	public JTextArea getLogArea() {
		return logArea;
	}

}
