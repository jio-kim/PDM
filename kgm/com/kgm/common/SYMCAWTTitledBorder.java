package com.kgm.common;

import java.awt.Color;
import java.awt.Font;

import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 * Ÿ��Ʋ ������ Ÿ��Ʋ Į�� blue�� ���� ��.
 * @Copyright : S-PALM
 * @author   : ������
 * @since    : 2012. 3. 26.
 * Package ID : com.pungkang.common.SpalmTitledBorder.java
 */
public class SYMCAWTTitledBorder extends TitledBorder {

	private static final long serialVersionUID = 1L;
	
	public SYMCAWTTitledBorder(String title){
		super(title);
		setTitleColor(Color.BLUE);
		setTitleFont(new Font("���� ���", Font.BOLD, 13));
    }
	
	public SYMCAWTTitledBorder(Border border){
		super(border);
		setTitleColor(Color.BLUE);
		setTitleFont(new Font("���� ���", Font.BOLD, 13));
	}
	
	public SYMCAWTTitledBorder(Border border, String title){
		super(border, title);
		setTitleColor(Color.BLUE);
		setTitleFont(new Font("���� ���", Font.BOLD, 13));
    }
}
