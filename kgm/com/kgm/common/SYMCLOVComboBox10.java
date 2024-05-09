package com.kgm.common;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMDisplayUtil;
import com.teamcenter.rac.kernel.ListOfValuesInfo;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.controls.SWTComboBox;

public class SYMCLOVComboBox10 extends SWTComboBox {

	private TCSession session = null;
	private TCComponentListOfValues listofvalue = null;
	private ControlDecoration decoration;
	private String lovSeparator = ";";
	
	public final static int VIEW_VALUE_DESC = 0;
	public final static int VIEW_VALUE = 1;
	public final static int VIEW_DESC = 2;
	private int viewType;
	
	private HashMap<String, String[]> list = new HashMap<String, String[]>();
	
	public SYMCLOVComboBox10(Composite parent) {
		this(parent, SWT.BORDER | SWT.READ_ONLY);
	}
	
	public SYMCLOVComboBox10(Composite parent, int style) {
		this(parent, style, null);
	}

	public SYMCLOVComboBox10(Composite parent, String lovName) {
		this(parent,  SWT.BORDER | SWT.READ_ONLY, lovName, false);
	}
	
	public SYMCLOVComboBox10(Composite parent, String lovName, boolean mandatory) {
		this(parent,  SWT.BORDER | SWT.READ_ONLY, lovName, mandatory);
	}

	public SYMCLOVComboBox10(Composite parent, int style, String lovName) {
		this(parent, style, lovName, false);
	}
	
    public SYMCLOVComboBox10(Composite parent, String lovName, int viewType, boolean mandatory) {
        this(parent,  SWT.BORDER | SWT.READ_ONLY, lovName, viewType, mandatory);
    }

	public SYMCLOVComboBox10(Composite parent, int style, String lovName, boolean mandatory) {
		this(parent, style, lovName, VIEW_VALUE_DESC, mandatory);
	}
	
	public SYMCLOVComboBox10(Composite parent, int style, String lovName, int viewType, boolean mandatory) {
		super(parent, style);
		session = CustomUtil.getTCSession();
		TCPreferenceService prefService = session.getPreferenceService();
		lovSeparator = prefService.getStringValue("LOV_value_desc_separator");
		this.viewType = viewType;
		comboValueSetting(lovName);
		setMandatory(mandatory);
	}

	public SYMCLOVComboBox10(Composite parent, int style, TCSession session, String lovName) {
		super(parent, style);
		this.session = session;
		if (session == null)
			this.session = CustomUtil.getTCSession();
		TCPreferenceService prefService = session.getPreferenceService();
		lovSeparator = prefService.getStringValue("LOV_value_desc_separator");
		this.viewType = VIEW_VALUE_DESC;
		comboValueSetting(lovName);
		setMandatory(false);
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
//		setItems(new String[0]);
		try {
			if (lovName != null) {
				if (session == null)
					throw new NullPointerException("Session is null.");
				
				TCComponentListOfValuesType listofvaluestype = (TCComponentListOfValuesType) session.getTypeComponent("ListOfValues");
				TCComponentListOfValues[] listofvalues = listofvaluestype.find(lovName);
				if(listofvalues == null || listofvalues.length == 0) {
					return;
				}
				listofvalue = listofvalues[0];

				//String[] lovDipValues = listofvalue.getListOfValues().getLOVDisplayValues();
				ListOfValuesInfo lovValuesInfo = listofvalue.getListOfValues();
				Object[] lovValueObjects = lovValuesInfo.getListOfValues();
				String[] dispValues = lovValuesInfo.getLOVDisplayValues();
				ArrayList<String> lovValueList = new ArrayList<String>();
//				if (! lovValueObjects[0].equals(dispValues[0]))
//				{
    				for (int i = 0; i < lovValueObjects.length; i++)
    				{
    				    if (lovValueObjects[i] instanceof TCComponent)
    				        lovValueList.add(((TCComponent) lovValueObjects[i]).toDisplayString());
    				    else
    				        lovValueList.add(dispValues[i]);
				    }
//				}
/*				for (Object lovValueObject : lovValueObjects)
//				{
//					if (lovValueObject instanceof TCComponent)
//						lovValueList.add(((TCComponent) lovValueObject).toDisplayString());
//				}*/

				String[] lovValues = lovValueList.size() > 0 ? lovValueList.toArray(new String[0]) : listofvalue.getListOfValues().getStringListOfValues();
				String[] lovDesces = listofvalue.isDescriptionShown() ? listofvalue.getListOfValues().getDescriptions() : null;

				if( lovValues != null ) {
				    comboValueSetting(lovValues, lovDesces);
				}

//				if (lovValueList.size() > 0) {
//				    list.clear();
//				    
//				    for (int i = 0; i < lovValueList.size(); i++)
//				    {
//				        if (lovValueObjects[i] instanceof TCComponent)
//				            list.put(lovValueList.get(i), new String[]{((TCComponent) lovValueObjects[i]).toDisplayString()});
//				        else
//				            list.put(lovValueList.get(i), new String[]{lovValuesInfo.getStringListOfValues()[i], lovDesces != null ? lovDesces[i] : ""});
//				    }
//				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getText() {
	    if (getSelectedItem() != null)
	        return list.get(getSelectedItem()) == null ? "" : list.get(getSelectedItem())[0];
//		for (int i = 0; i < getItemCount(); i++)
//		{
//			if (getItemAt(i).equals(getSelectedItem()))
//					return list.get(i)[0];
//		}
		return "";
	}
	
	public String getTextDesc() {
        if (getSelectedItem() != null)
            return list.get(getSelectedItem())[1];
//		for (int i = 0; i < getItemCount(); i++)
//		{
//			if (getItemAt(i).equals(getSelectedItem()))
//					return list.get(i)[1];
//		}
		return "";
//		int idx = getSelectionIndex();
//		if(idx == -1)
//			return "";
//		return list.get(idx)[1];
	}
	
	public void setText(String value) {
	    if (list.containsKey(value)) {
	        setSelectedItem(value);
	        return;
	    }

	    for (String key : list.keySet())
	    {
	        if (list.get(key)[0].equals(value)) {
	            setSelectedItem(key);
	            break;
	        }
	    }

/*	    String text = null;
		@SuppressWarnings("unused")
		int idx = 0;
		for(int i = 0 ; i < list.size() ; i++) {
			if(list.get(i)[0].equals(value)) {
				idx = i;

				if(viewType == VIEW_VALUE_DESC) {
//					text = list.get(i)[0] + lovSeparator + " " + list.get(i)[1];
                    text = list.get(i)[0] + (list.get(i)[1] == null || list.get(i)[1].equals("") ? "" : (lovSeparator + " " + list.get(i)[1]));
				} else if(viewType == VIEW_DESC) {
				    text = list.get(i)[1];
				} else {
					text = list.get(i)[0];
				}
			}
		}
		setSelectedItem(text);
//		super.select(idx);
*/

	}
	
    public void setTextDesc(String desc) {
        if (list.containsKey(desc)) {
            setSelectedItem(desc);
            return;
        }

        for (String key : list.keySet())
        {
            if (list.get(key)[1].equals(desc)) {
                setSelectedItem(key);
                break;
            }
        }
/*
		String text = null;
        @SuppressWarnings("unused")
		int idx = 0;
        for(int i = 0 ; i < list.size() ; i++) {
            if(list.get(i)[1].equals(desc)) {
                idx = i;

				if(viewType == VIEW_VALUE_DESC) {
//					text = list.get(i)[0] + lovSeparator + " " + list.get(i)[1];
                    text = list.get(i)[0] + (list.get(i)[1] == null || list.get(i)[1].equals("") ? "" : (lovSeparator + " " + list.get(i)[1]));
				} else if(viewType == VIEW_DESC) {
				    text = list.get(i)[1];
				} else {
					text = list.get(i)[0];
				}
            }
        }
        setSelectedItem(text);
//        super.select(idx);
*/
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
			setSelectedItem("");
//			select(0);
		}
	}
	
	public void add(String value, String desc) {
//		list.add(new String[]{value, desc});
		String text = null;
		if(viewType == VIEW_VALUE_DESC && desc != null && !desc.equals("")) {
//			text = value + lovSeparator + " " + desc;
            text = value + (desc == null ? "" : (lovSeparator + " " + desc));
		} else if(viewType == VIEW_DESC) {
		    text = desc;
		} else {
			text = value;
		}
        list.put(text, new String[]{value, desc});
		//super.add(text);
		addItem(text);
	}

	public String getSelectedString() {
		return getText();
	}

	public void setSelectedIndex(int idx) {
		String text = null;
		String key = null;

		key = list.keySet().toArray()[idx].toString();

		if(viewType == VIEW_VALUE_DESC) {
//			text = list.get(idx)[0] + lovSeparator + " " + list.get(idx)[1];
            text = list.get(key)[0] + (list.get(key)[1] == null ? "" : (lovSeparator + " " + list.get(key)[1]));
		} else if(viewType == VIEW_DESC) {
		    text = list.get(key)[1];
		} else {
			text = list.get(key)[0];
		}

		setSelectedItem(text);
	}

	public void setSelectedString(String property) {
		setText(property);
	}

	public TCComponentListOfValues getLovComponent() {
		return listofvalue;
	}

	public Object getSelectedObject() {
		return getText();
	}

	public String getSelectedDisplayString() {
		return (String) getSelectedItem();
	}
}
