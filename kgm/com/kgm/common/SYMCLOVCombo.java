package com.kgm.common;

import java.util.ArrayList;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMDisplayUtil;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCSession;

public class SYMCLOVCombo extends Combo {

	private TCSession session = CustomUtil.getTCSession();
	
	private ControlDecoration decoration;
	
	public final static int VIEW_VALUE_DESC = 0;
	public final static int VIEW_VALUE = 1;
	public final static int VIEW_DESC = 2;
	private int viewType;
	
	private ArrayList<String[]> list = new ArrayList<String[]>();
	
	public SYMCLOVCombo(Composite parent) {
		this(parent, SWT.BORDER | SWT.READ_ONLY);
	}
	
	public SYMCLOVCombo(Composite parent, int style) {
		this(parent, style, null);
	}

	public SYMCLOVCombo(Composite parent, String lovName) {
		this(parent,  SWT.BORDER | SWT.READ_ONLY, lovName, false);
	}
	
	public SYMCLOVCombo(Composite parent, String lovName, boolean mandatory) {
		this(parent,  SWT.BORDER | SWT.READ_ONLY, lovName, mandatory);
	}

	public SYMCLOVCombo(Composite parent, int style, String lovName) {
		this(parent, style, lovName, false);
	}
	
    public SYMCLOVCombo(Composite parent, String lovName, int viewType, boolean mandatory) {
        this(parent,  SWT.BORDER | SWT.READ_ONLY, lovName, viewType, mandatory);
    }

	public SYMCLOVCombo(Composite parent, int style, String lovName, boolean mandatory) {
		this(parent, style, lovName, VIEW_VALUE_DESC, mandatory);
	}
	
	public SYMCLOVCombo(Composite parent, int style, String lovName, int viewType, boolean mandatory) {
		super(parent, style);
		this.viewType = viewType;
		comboValueSetting(lovName);
		setMandatory(mandatory);
	}

	/**
	 * "org.eclipse.swt.widgets." 패키지 이외 패키지에서도 상속 받을수 있게 checkSubclass 메소드 재정의
	 * 
	 * @Copyright : S-PALM
	 * @author : 권오규
	 * @since : 2013. 1. 8.
	 * @override
	 * @see org.eclipse.swt.widgets.Combo#checkSubclass()
	 */
	@Override
	protected void checkSubclass() {
	}

	/**
	 * 
	 * @Copyright : S-PALM
	 * @author : 권오규
	 * @since : 2013. 1. 8.
	 */
	public void setMandatory(boolean mandatory) {
		if(mandatory && decoration == null) {
			decoration = SYMDisplayUtil.setRequiredFieldSymbol(this);
		}
		if(decoration == null) {
			return;
		}
		if(mandatory) {
			decoration.show();
		} else {
			decoration.hide();
		}
		
		redraw();
	}

	/**
	 * 
	 * @Copyright : S-PALM
	 * @author : 권오규
	 * @since : 2013. 1. 7.
	 * @param lovName
	 */
	private void comboValueSetting(String lovName) {
		setItems(new String[0]);
		try {
			if (lovName != null) {
				TCComponentListOfValuesType listofvaluestype = (TCComponentListOfValuesType) session
						.getTypeComponent("ListOfValues");
				TCComponentListOfValues[] listofvalues = listofvaluestype.find(lovName);
				if(listofvalues == null || listofvalues.length == 0) {
					return;
				}
				TCComponentListOfValues listofvalue = listofvalues[0];
				
				//String[] lovDipValues = listofvalue.getListOfValues().getLOVDisplayValues();
				
				String[] lovValues = listofvalue.getListOfValues().getStringListOfValues();
				String[] lovDesces = listofvalue.getListOfValues().getDescriptions();
				if( lovValues != null ) {
				    comboValueSetting(lovValues, lovDesces);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getText() {
		int idx = getSelectionIndex();
		if(idx == -1)
			return "";
		return list.get(idx)[0];
	}
	
	public String getTextDesc() {
		int idx = getSelectionIndex();
		if(idx == -1)
			return "";
		return list.get(idx)[1];
	}
	
	public void setText(String value) {
		int idx = 0;
		for(int i = 0 ; i < list.size() ; i++) {
			if(list.get(i)[0].equals(value)) {
				idx = i;
			}
		}
		super.select(idx);
	}
	
    public void setTextDesc(String desc) {
        int idx = 0;
        for(int i = 0 ; i < list.size() ; i++) {
            if(list.get(i)[1].equals(desc)) {
                idx = i;
            }
        }
        super.select(idx);
    }
    

	
	/**
	 * 
	 * @Copyright : S-PALM
	 * @author : 권오규
	 * @since : 2013. 1. 8.
	 * @param values
	 */
	private void comboValueSetting(String[] values, String[] desces) {
		list.clear();
		add("", "");
		if(desces == null) {
			for(int i = 0 ; i < values.length ; i++) {
				String value = values[i];
				add(value, null);
			}
		} else {
			for(int i = 0 ; i < values.length ; i++) {
				String value = values[i];
				String desc = desces[i];
				add(value, desc);
			}
		}
		if(values != null && values.length > 0) {
			select(0);
		}
	}
	
	public void add(String string) {
	}
	
	public void add(String string, int index) {
	}
	
	public void add(String value, String desc) {
		list.add(new String[]{value, desc});
		String text = null;
		if(viewType == VIEW_VALUE_DESC && desc != null && !desc.equals("")) {
			text = value + " (" + desc + ")";
		} else if(viewType == VIEW_DESC) {
		    text = desc;
		} else {
			text = value;
		}
		super.add(text);
	}
	
}
