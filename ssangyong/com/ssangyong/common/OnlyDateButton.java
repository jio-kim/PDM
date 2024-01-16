package com.ssangyong.common;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCDateFormat;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.AIFImageIcon;
import com.teamcenter.rac.util.AbstractPopupButton;
import com.teamcenter.rac.util.ButtonLayout;
import com.teamcenter.rac.util.Painter;
import com.teamcenter.rac.util.Registry;

public class OnlyDateButton extends AbstractPopupButton implements SYMCInterfaceComponent {

	private static final long serialVersionUID = 1L;
	
	private OnlyCalendarPanel calendarPanel;
	private boolean lovStyle;
	private boolean doTearoff;
	private Calendar calendar;
	private String calendarTitle;

	private String displayFormat;
	private TCDateFormat displayFormatter;
	private TCDateFormat returnFormatter;
	private String nullDate = "날짜가 설정 안됨";
	private static final String defaultDateFormat = new String("yyyy-MM-dd");

	private boolean dateIsSet;

	protected boolean mandatory;

	public OnlyDateButton() {
		this(CustomUtil.getTCSession());
	}
	
	public OnlyDateButton(TCSession session) {
		this(session, null);
	}

	public OnlyDateButton(TCSession session, Date date) {
		this(session, date, defaultDateFormat, false);
	}
	
	public OnlyDateButton(boolean mandatory) {
		this(CustomUtil.getTCSession());
		setMandatory(mandatory);
	}

	public OnlyDateButton(TCSession session, Date date, String dateFormat, boolean flag) {
		calendar = new GregorianCalendar();
		if(date == null) {
			calendar.clear();
		} else {
			calendar.setTime(date);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			dateIsSet = true;
		}
		this.displayFormat = dateFormat;
		this.lovStyle = flag;
		doTearoff = false;
		if(displayFormat != null) {
			displayFormatter = session.askTCDateFormat();
		}
		returnFormatter = session.askTCDateFormat();
		if(lovStyle) {
			setMargin(new Insets(0, 3, 0, 3));
			setBorder(BorderFactory.createEtchedBorder());
		}
		updateButtonText();

		calendarPanel = new OnlyCalendarPanel(date);
		setIcon(AIFImageIcon.getImageIcon(this, "images/calendar_16.png"));
		setHorizontalTextPosition(2);
		Font font = getFont();
		setFont(new Font(font.getName(), 0, font.getSize() - 2));
	}

	public void allowTearoff(boolean flag) {
		doTearoff = flag;
	}

	public void initPopupWindow() {
		getPanel().add("Center", calendarPanel);
		calendarPanel.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeevent) {
				setDate(calendarPanel.getTime());
			}
		});
		Registry registry = Registry.getRegistry("com.teamcenter.rac.util.util");
		JPanel btnPanel = new JPanel(new ButtonLayout());
		JButton okBtn = new JButton(registry.getString("ok"));
		okBtn.setFont(getFont());
		okBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				calendarPanel.fireStateChanged();
				postDown();
			}
		});
		JButton clearBtn = new JButton(registry.getString("clear"));
		clearBtn.setFont(getFont());
		clearBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setDate(null);
				postDown();
			}
		});
		btnPanel.add("left", okBtn);
		btnPanel.add("right", clearBtn);
		if(calendarTitle != null) {
			setPopupTitle(calendarTitle);
		}
		getPanel().add("South", btnPanel);
	}

	public void postUp() {
		super.postUp();
		if(doTearoff) {
			tearoff();
		}
	}

	public void setDate(Date date) {
		if(date == null) {
			calendar.clear();
			dateIsSet = false;
		} else {
			calendar.setTime(date);
			calendarPanel.setTime(date);
			dateIsSet = true;
		}
		updateButtonText();
	}

	public Date getDate() {
		if(dateIsSet) {
			return calendar.getTime();
		}
		return null;
	}

	public String getDateString() {
		if(dateIsSet) {
			return returnFormatter.format(calendar.getTime());
		}
		return null;
	}

	public String getDisplayFormat() {
		return displayFormat;
	}
	
	public void setDefaultDisplayFormat() {
		if(dateIsSet) {
			returnFormatter.format(calendar.getTime());
		}
	}

	public void setTitle(String s) {
		calendarTitle = s;
	}

	public void setNullDateString(String s) {
		nullDate = s;
		updateButtonText();
	}

	protected void updateButtonText() {
		if(dateIsSet) {
			String s = displayFormatter.format(calendar.getTime());
			setText(s);
		} else {
			setText(nullDate);
		}
		validate();
		repaint();
	}

	public void setMandatory(boolean flag) {
		mandatory = flag;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void paint(Graphics g) {
		super.paint(g);
		if(mandatory) {
			Painter.paintIsRequired(this, g);
		}
	}

	@Override
	public Object getValue() {
		return getDate();
	}

}