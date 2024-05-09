package com.symc.plm.rac.prebom.common.util;

import java.util.List;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.controls.SWTComboBox;
import com.teamcenter.soa.client.model.LovValue;

public class SDVLOVUtils {

    /**
     * Lov 이름으로 Lov 목록 가져오기
     *
     * @method getLOVValues
     * @date 2013. 10. 18.
     * @param
     * @return List<LovValue>
     * @exception
     * @throws
     * @see
     */
    public static List<LovValue> getLOVValues(String lovName) throws TCException {
        TCComponentListOfValuesType type = (TCComponentListOfValuesType)getTCSession().getTypeComponent("ListOfValues");
        TCComponentListOfValues[] values = type.find(lovName);

        return values[0].getListOfValues().getValues();
    }

    public static String getLovValueDesciption(String lovName, String value) throws TCException {
        String description = null;
        List<LovValue> values =  getLOVValues(lovName);
        if(values != null) {
            for(LovValue lovValue : values) {
                if(lovValue.getValue().equals(value)) {
                    description = lovValue.getDescription();
                    break;
                }
            }
        }

        return description;
    }

    public static SWTComboBox comboValueSetting(SWTComboBox combo, List<LovValue> lovList) {
        SWTComboBox combobox = combo;
        try {
            if (lovList != null) {
                for (LovValue lovValue : lovList) {
                    combobox.addItem(lovValue.getDisplayValue() + " (" + lovValue.getDescription() + ")", lovValue.getDisplayValue());
                }
            }
            combobox.setAutoCompleteSuggestive(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return combobox;
    }
    
    
    /**
    *
    * @method getTCSession
    * @date 2013. 10. 18.
    * @param
    * @return TCSession
    * @exception
    * @throws
    * @see
    */
   public static TCSession getTCSession() {
       return (TCSession) AIFUtility.getSessionManager().getDefaultSession();
   }

   public static SWTComboBox comboValueSetting(SWTComboBox combo, String[] lovValues) {
	   
	   for(String str : lovValues) {
		   combo.addItem(str);
	   }
	return combo;
	   
   }
   public static SWTComboBox comboValueSetting(SWTComboBox combo, String lovName) {

	   SWTComboBox combobox = combo;
	   boolean isShown = true;
		try {

			if (lovName != null) {
				TCComponentListOfValuesType listofvaluestype = (TCComponentListOfValuesType)getTCSession().getTypeComponent("ListOfValues");
				TCComponentListOfValues[] listofvalues = listofvaluestype.find(lovName);
				
				if(listofvalues == null || listofvalues.length == 0) {
					return combobox;
				}
				TCComponentListOfValues listofvalue = listofvalues[0];
				isShown = listofvalue.isDescriptionShown();
				String[] lovValues = listofvalue.getListOfValues().getStringListOfValues();
				String[] lovDesces = listofvalue.getListOfValues().getDescriptions();
				int i = 0;
				
				for(String lovValue : lovValues){
					
					if(isShown) {
						
						combobox.addItem(lovValue+" (" + lovDesces[i] + ")", lovValue);
						i++;
					}else{
						combobox.addItem(lovValue, lovValue);
					}
					
				}
			}
			combobox.getTextField().setEditable(false);
			combobox.setAutoCompleteSuggestive(false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return combobox;
	}


   /**
    * Resource 상위 LOV 정보로 하위 LOV의 목록 가져오기
    *
    * @method getChildResourceLOVValues
    * @date 2013. 11. 21.
    * @param shopType ex) A, B, P
    * @param resourceType ex) EQUIP, TOOL
    * @param findKey Findkey is property value like category or category_mainClass, sample : EXT, JIG, EXT_A, JIG_JG
    * @return List<LovValue>
 * @throws Exception 
 * @exception
    * @throws 
    * @see
    */
   public static List<LovValue> getChildResourceLOVValues(String bopType, String resourceType, String findKey) throws Exception {
       if(bopType == null || bopType.isEmpty()) {
           throw new Exception("BopType is Required");
       }
       
       if(resourceType == null || resourceType.isEmpty()) {
           throw new Exception("ResourceType is Required");
       }
       
       String lovName = "M7_" + bopType.toUpperCase() + "_" + resourceType.toUpperCase();
       
       if(findKey != null && !findKey.isEmpty()) {
           lovName = lovName + "_" + findKey.toUpperCase();
       }
      
       return getLOVValues(lovName);      
   }

   public static void setMandatory(Control con) {
       ControlDecoration dec = new ControlDecoration(con, SWT.TOP | SWT.RIGHT);
       Registry registry = Registry.getRegistry("com.kgm.common.common");
       dec.setImage(registry.getImage("CONTROL_MANDATORY"));
       dec.setDescriptionText("This value will be required.");
   }
}

