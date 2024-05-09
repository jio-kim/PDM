package com.kgm.common;

import java.awt.Color;
import java.awt.Font;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.UIUtilities;

public class SimpleProgressBar extends JWindow{
	/** */
	private static final long serialVersionUID = 1L;
	private Window parent;
	private JLabel label;
	private String message = "잠시만 기다리세요...";

	public SimpleProgressBar(Window parent, String message) {
		super(parent);
		this.parent = parent;
		this.message = message;
		init();
	}
	
	public SimpleProgressBar(Window parent) {
		super(parent);
		this.parent = parent;
		init();
	}

	private void init() {
		setAlwaysOnTop(true);
		setLocationRelativeTo(parent);
		JPanel panel = new JPanel(new HorizontalLayout(10,5,5,5,5));
		panel.setBackground(Color.white);
		Font font = new Font("맑은 고딕", Font.BOLD, 13);
		Registry registry = Registry.getRegistry(this);
		label = new JLabel(message, registry.getImageIcon("Loading1.ICON"), JLabel.CENTER);
		label.setFont(font);
		panel.setBackground(Color.white);
		panel.setBorder(BorderFactory.createTitledBorder(""));
		panel.add("unbound.bind", label);
		add(panel);
		pack();
		UIUtilities.centerToScreen(this);
		pack();
		repaint();
		setVisible(true);
	}

	public void setMessage(String str) {
		label.setText(str);
		repaint();
		pack();
	}
	
	public void closeProgressBar(){
		setVisible(false);
		dispose();
	}
}
