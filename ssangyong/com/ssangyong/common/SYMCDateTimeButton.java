package com.ssangyong.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;

import com.ssangyong.common.utils.SYMDisplayUtil;
import com.teamcenter.rac.kernel.TCDateFormat;
import com.teamcenter.rac.kernel.TCSession;

public class SYMCDateTimeButton extends DateTime {

	private boolean mandatory;
	
	public SYMCDateTimeButton(Composite parent) {
		this(parent, SWT.DATE | SWT.LONG | SWT.DROP_DOWN);
	}
	
	public SYMCDateTimeButton(Composite parent, boolean mandatory) {
		this(parent, SWT.DATE | SWT.LONG | SWT.DROP_DOWN, false);
	}

	public SYMCDateTimeButton(Composite parent, GridData data) {
		this(parent, SWT.DATE | SWT.LONG | SWT.DROP_DOWN, false, data);
	}
	
	public SYMCDateTimeButton(Composite parent, boolean mandatory, GridData data) {
		this(parent, SWT.DATE | SWT.LONG | SWT.DROP_DOWN, false, data);
	}
	
	public SYMCDateTimeButton(Composite parent, int style) {
		super(parent, style);
		setUI();
	}
	
	public SYMCDateTimeButton(Composite parent, int style, boolean mandatory) {
		super(parent, style);
		this.mandatory = mandatory;
		setUI();
	}
	
	public SYMCDateTimeButton(Composite parent, int style, boolean mandatory, GridData data) {
		super(parent, style);
		this.mandatory = mandatory;
		setLayoutData(data);
		setUI();
	}

	/**
	 * 
	 * @Copyright : S-PALM
	 * @author : 권오규
	 * @since  : 2013. 1. 8.
	 * @override
	 * @see org.eclipse.swt.widgets.DateTime#checkSubclass()
	 */
	@Override
	protected void checkSubclass() {
	}
	
	public void setMandatory (boolean mandatory) {
		this.mandatory = mandatory;
		setUI();
	}
	
	private void setUI() {
		if(mandatory){
			SYMDisplayUtil.setRequiredFieldSymbol(this);
			redraw();
		}
	}
	
	/**
	 * TC에서 적용중인 Dateformat으로 날짜를 가지고 옴
	 * 단, 년,월,일까지만
	 * @param session
	 * @return
	 */
	public String getTCDate(TCSession session){
		String tcDate = super.getYear()+"-"+(super.getMonth()+1)+"-"+super.getDay()+"";
		TCDateFormat tcDateFormat = session.askTCDateFormat();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = sdf.parse(tcDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		tcDate = tcDateFormat.format(date);
		return tcDate;
	}
	
	public Date getDate(TCSession session){
		String tcDate = super.getYear()+"-"+(super.getMonth()+1)+"-"+super.getDay()+"";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = sdf.parse(tcDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return date;
	}
	
	
	
	/**
	 * TCComponent에서 Date 속성을 String으로 받은 경우 사용 
	 * @param dateString
	 * @param session
	 */
	public void setTCDate(String dateString, TCSession session){
		TCDateFormat tcDateFormat = session.askTCDateFormat();
		Date parseDate = null;
		try {
			if(dateString == null || dateString.equals(""))
				parseDate = new Date();
			else
				parseDate = tcDateFormat.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		SimpleDateFormat simpleDateformat = new SimpleDateFormat("yyyy");
		int year = Integer.parseInt(simpleDateformat.format(parseDate));
		simpleDateformat = new SimpleDateFormat("MM");
		int month = Integer.parseInt(simpleDateformat.format(parseDate))-1;
		simpleDateformat = new SimpleDateFormat("dd");
		int day = Integer.parseInt(simpleDateformat.format(parseDate));
		
		super.setDate(year, month, day);
	}
	
	
	public void setDate(Date date, TCSession session){
		SimpleDateFormat simpleDateformat = new SimpleDateFormat("yyyy");
		int year = Integer.parseInt(simpleDateformat.format(date));
		simpleDateformat = new SimpleDateFormat("MM");
		int month = Integer.parseInt(simpleDateformat.format(date))-1;
		simpleDateformat = new SimpleDateFormat("dd");
		int day = Integer.parseInt(simpleDateformat.format(date));
		
		super.setDate(year, month, day);
	}
	
	/**
	 * TCComponent에서 Date 속성을 String으로 받은 경우 사용 
	 * @param dateString
	 * @param session
	 */
	public void setTCDate(Date parseDate, TCSession session){

		if(parseDate == null)
			return;
		
		SimpleDateFormat simpleDateformat = new SimpleDateFormat("yyyy");
		int year = Integer.parseInt(simpleDateformat.format(parseDate));
		simpleDateformat = new SimpleDateFormat("MM");
		int month = Integer.parseInt(simpleDateformat.format(parseDate))-1;
		simpleDateformat = new SimpleDateFormat("dd");
		int day = Integer.parseInt(simpleDateformat.format(parseDate));
		
		super.setDate(year, month, day);
	}

	
	
	
}
