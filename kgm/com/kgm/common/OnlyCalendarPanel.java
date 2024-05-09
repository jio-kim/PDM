package com.kgm.common;

import com.teamcenter.rac.util.*;

import java.awt.*;
import java.awt.event.*;
import java.text.DateFormatSymbols;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicArrowButton;

@SuppressWarnings({"unused"})
public class OnlyCalendarPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private GregorianCalendar calendar;
	private GregorianCalendar selected;
	private DateButton[] dates;
	private JComboLabel month;
	private iTextField year;
	private BasicArrowButton prev;
	private BasicArrowButton next;
	private JToggleButton clearSelection;
	private ChangeEvent changeEvent;

	public OnlyCalendarPanel() {
		changeEvent = new ChangeEvent(this);
		constructPanel(new Date());
		setHourMinSec(0, 0, 0);
		setTime(calendar.getTime());
	}

	public OnlyCalendarPanel(Date date) {
		changeEvent = new ChangeEvent(this);
		if(date == null) {
			date = new Date();
		}
		constructPanel(date);
		setHourMinSec(0, 0, 0);
	}

	public void setTime(Date date) {
		if(date == null) {
			date = new Date();
		}
		calendar.setTime(date);
		selected.setTime(date);
		setHourMinSec(0, 0, 0);
		updateText();
		updateDatePanel();
	}

	public Date getTime() {
		return calendar.getTime();
	}

	public void setEnabled(boolean flag) {
		super.setEnabled(flag);
		for(int i = 0; i < dates.length; i++) {
			dates[i].setEnabled(flag);
		}
		year.setEnabled(flag);
		prev.setEnabled(flag);
		next.setEnabled(flag);
		clearSelection.setEnabled(flag);
	}

	private void constructPanel(Date date) {
		calendar = new GregorianCalendar();
		selected = new GregorianCalendar();
		DateFormatSymbols dateformatsymbols = new DateFormatSymbols();
		setLayout(new VerticalLayout(2, 2, 2, 2, 2));
		Font font = (new JLabel("foo")).getFont();
		Font font1 = new Font(font.getName(), 0, font.getSize() - 2);
		JPanel jpanel = new JPanel(new HorizontalLayout(1, 0, 0, 0, 0));
		prev = new BasicArrowButton(7);
		prev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				decMonth();
				updateText();
				updateDatePanel();
			}
		});
		jpanel.add("left", prev);
		month = new JComboLabel(dateformatsymbols.getMonths());
		month.setFont(font1);
		jpanel.add("left", month);
		year = new iTextField(4, 4, true);
		year.setFont(font1);
		TextFieldUpdater textfieldupdater = new TextFieldUpdater();
		year.addActionListener(textfieldupdater);
		year.addFocusListener(textfieldupdater);
		next = new BasicArrowButton(3);
		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				incMonth();
				updateText();
				updateDatePanel();
			}
		});
		jpanel.add("right", next);
		jpanel.add("right", year);
		add("top", jpanel);
		jpanel = new JPanel(new GridLayout(7, 7));
		String as[] = dateformatsymbols.getShortWeekdays();
		for(int i = 1; i <= 7; i++) {
			JLabel jlabel = new JLabel(String.valueOf(as[i].charAt(0)));
			jlabel.setHorizontalAlignment(0);
			jlabel.setFont(font1);
			jpanel.add(jlabel);
		}

		int j = 1;
		int k = 2;
		dates = new DateButton[42];
		ButtonGroup buttongroup = new ButtonGroup();
		for(int l = 0; l < 42; l++) {
			DateButton datebutton = new DateButton();
			datebutton.setNumber(l);
			datebutton.setFocusPainted(false);
			datebutton.setBorderPainted(false);
			datebutton.setMargin(new Insets(1, 1, 1, 1));
			datebutton.setFont(font1);
			dates[l] = datebutton;
//			String s = Integer.toString(k) + '.' + Integer.toString(j);
			jpanel.add(datebutton);
			buttongroup.add(datebutton);
			if(++j == 8) {
				j = 1;
				k++;
			}
		}
		clearSelection = new JToggleButton("clear");
		buttongroup.add(clearSelection);
		jpanel.setBorder(BorderFactory.createEtchedBorder());
		add("top", jpanel);
		jpanel = new JPanel(new GridLayout(1, 6));
		setTime(date);
		updateText();
		updateDatePanel();
	}

	private void decMonth() {
		if(calendar.get(1) == 1 && calendar.get(2) == 0) {
			return;
		} else {
			calendar.add(2, -1);
			return;
		}
	}

	private void incMonth() {
		if(calendar.get(1) == 9999 && calendar.get(2) == 11) {
			return;
		} else {
			calendar.add(2, 1);
			return;
		}
	}

	private void getAllText() {
		try {
			int i = Integer.parseInt(year.getText());
			if(i == 0)
				i = 1;
			calendar.set(1, i);
		} catch(NumberFormatException numberformatexception) {}
		updateText();
	}

	private void updateText() {
		month.setSelectedIndex(calendar.get(2));
		year.setText(String.valueOf(calendar.get(1)));
	}

	private void updateDatePanel() {
		int i = calendar.get(5);
		calendar.set(5, 1);
		int j = calendar.get(7) - 1;
		calendar.set(5, i);
		for(int k = 0; k <= j; k++)
			dates[k].setNumber(0);

		int l = calendar.getActualMaximum(5);
		for(int i1 = 0; i1 < l; i1++)
			dates[i1 + j].setNumber(i1 + 1);

		for(int j1 = j + l; j1 < 42; j1++)
			dates[j1].setNumber(0);

		if(calendar.get(2) == selected.get(2)) {
			dates[(j + selected.get(5)) - 1].setSelected(true);
		} else {
			clearSelection.setSelected(true);
		}
		invalidate();
		repaint();
	}

	public void addChangeListener(ChangeListener changelistener) {
		super.listenerList.add(javax.swing.event.ChangeListener.class, changelistener);
	}

	public void removeChangeListener(ChangeListener changelistener) {
		super.listenerList.remove(javax.swing.event.ChangeListener.class, changelistener);
	}

	public void setHourMinSec(int i, int j, int k) {
		calendar.set(Calendar.HOUR_OF_DAY, i);
		calendar.set(Calendar.MINUTE, j);
		calendar.set(Calendar.SECOND, k);
		selected.set(Calendar.HOUR_OF_DAY, i);
		selected.set(Calendar.MINUTE, j);
		selected.set(Calendar.SECOND, k);
		updateText();
	}

	public void fireStateChanged() {
		getAllText();
		Object aobj[] = super.listenerList.getListenerList();
		for(int i = aobj.length - 2; i >= 0; i -= 2)
			if(aobj[i] == (javax.swing.event.ChangeListener.class)) {
				((ChangeListener)aobj[i + 1]).stateChanged(changeEvent);
			}
	}

	private void changed() {
		fireStateChanged();
	}

	private class JComboLabel extends JLabel {

		private static final long serialVersionUID = 1L;
		private String texts[];

		public void setSelectedIndex(int i) {
			setText(texts[i]);
		}

		public JComboLabel(String as[]) {
			super(as[0]);
			texts = as;
		}
	}

	private class TextFieldUpdater extends FocusAdapter	implements ActionListener {

		public void actionPerformed(ActionEvent actionevent) {
			getAllText();
			updateDatePanel();
		}

		public void focusLost(FocusEvent focusevent) {
			getAllText();
			updateDatePanel();
		}

	}

	private class DateButton extends JToggleButton implements ChangeListener {

		private static final long serialVersionUID = 1L;
		
		int number;

		public void setNumber(int i) {
			number = i;
			if(i == 0) {
				setText("");
				((AbstractButton)this).setEnabled(true);
			} else {
				setText(Integer.toString(number));
				if(isEnabled())
					((AbstractButton)this).setEnabled(true);
			}
		}

		private void doPressed() {
			calendar.set(Calendar.DAY_OF_MONTH, number);
			selected.setTime(calendar.getTime());
			changed();
		}

		public void stateChanged(ChangeEvent changeevent) {
			setBorderPainted(isSelected());
		}

		public DateButton() {
			setNumber(0);
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					doPressed();
				}
			});
			((AbstractButton)this).addChangeListener(this);
		}

	}

}