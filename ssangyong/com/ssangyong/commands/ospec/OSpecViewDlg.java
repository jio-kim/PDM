package com.ssangyong.commands.ospec;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.teamcenter.rac.aif.AbstractAIFDialog;

public class OSpecViewDlg extends AbstractAIFDialog {

	private final JPanel contentPanel = new JPanel();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			OSpecViewDlg dialog = new OSpecViewDlg(null, null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public OSpecViewDlg(String title, JPanel ospecTablePanel) {
		setBounds(new Rectangle(0, 0, 1000, 600));
		setTitle(title);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			
//			fixedTable.getColumnModel().getColumn(3).setResizable(false);
//			fixedTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//			ospecViewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//		    fixedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		    ospecViewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//
//		    JScrollPane scroll = new JScrollPane(ospecViewTable);
//		    JViewport viewport = new JViewport();
//		    viewport.setView(fixedTable);
//		    viewport.setPreferredSize(fixedTable.getPreferredSize());
//		    scroll.setRowHeaderView(viewport);
//		    scroll.setCorner(JScrollPane.UPPER_LEFT_CORNER, fixedTable
//		        .getTableHeader());			
			
//			JScrollPane scrollPane = new JScrollPane(ospecViewTable);
			contentPanel.add(ospecTablePanel, BorderLayout.CENTER);
		}
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.setLayout(new FlowLayout(FlowLayout.TRAILING, 5, 5));
			{
				JButton closelButton = new JButton("Close");
				closelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				buttonPane.add(closelButton);
			}
		}
	}

}
