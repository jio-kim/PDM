package com.ssangyong.common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JLabel;

@SuppressWarnings({"unused"})
public class SYMCAWTLabel extends JLabel implements SYMCInterfaceComponent {

	private static final long serialVersionUID = 1L;
	
	public Dimension DEFAULT_LABEL_SIZE = new Dimension(70,21);

	private boolean mandatory;

	public SYMCAWTLabel(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
		setLabelUI();
	}

	public SYMCAWTLabel(String text, int horizontalAlignment) {
		this(text, null, horizontalAlignment);
		setLabelUI();
	}
	
	public SYMCAWTLabel(String text) {
//        this(text, null, CENTER);
		this(text, null, RIGHT);
        setLabelUI();
    }
	
	public SYMCAWTLabel(String text, boolean mandatory) {
		this(text, null, RIGHT);
		
		this.mandatory = mandatory;
		setLabelUI();
	}
	
	public SYMCAWTLabel(Icon image, int horizontalAlignment) {
        this(null, image, horizontalAlignment);
        setLabelUI();
    }
	
	public SYMCAWTLabel(Icon image) {
//        this(null, image, CENTER);
		this(null, image, RIGHT);
        setLabelUI();
    }
	
	public SYMCAWTLabel() {
//        this("", null, CENTER);
		this("", null, RIGHT);
        setLabelUI();
    }
	
	protected void setLabelUI(){
		setOpaque(true);
		setBackground(Color.white);
		setForeground(Color.darkGray);
		setPreferredSize(DEFAULT_LABEL_SIZE);
		setFont(new Font("¸¼Àº °íµñ", Font.BOLD, 12));
	}
	
	public void setLabelSize(Dimension size){
		setPreferredSize(size);
		updateUI();
		repaint();
	}
	
	public void setLabelSize(int x, int y){
		setPreferredSize(new Dimension(x, y));
		updateUI();
		repaint();
	}

	@Override
	public Object getValue() {
		return getText();
	}
	
	public void paint(Graphics g)
	{
		super.paint(g);
		/*
		if(mandatory){
//			Painter.paintIsRequired(this, g);
			Dimension localDimension = this.getSize();
		    g.setColor(Color.red);
		    int i = 3;
		    int j = 4;
		    int k = 3;
		    int l = 2;
		    g.drawLine(i - k, j, i + k, j);
		    g.drawLine(i, j - k, i, j + k);
		    g.drawLine(i - l, j - l, i + l, j + l);
		    g.drawLine(i - l, j + l, i + l, j - l);
		}
		*/
	}
	
	public void setMandatory(boolean flag)
	{
		mandatory = flag;
	}
}
