// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.teamcenter.rac.pse.variants.sosvi;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.pse.variants.modularvariants.ConstraintsModel;
import com.teamcenter.rac.pse.variants.modularvariants.ErrorCheck;
import com.teamcenter.rac.util.MessageBox;

// Referenced classes of package com.teamcenter.rac.pse.variants.sosvi:
//            ValueSelection, InvalidValueException, SelectedOptionSet, SelectedOptionSetDialog

@SuppressWarnings({ "rawtypes", "unchecked", "serial", "unused" })
abstract class FixedValueSelection extends ValueSelection {

    private JComboBox jcombobox = null;
    private String[][] errorChecks = null;

    class FixedValueSelectionOperation extends AbstractAIFOperation {

        public void executeOperation() throws Exception {
            try {
                // 20130201. 윤성원
                // 옵션값의 설멸을 JList에 추가함.
                // start ------------------------------------
                if (selectedItem instanceof String) {
                    String str = (String) selectedItem;
                    
                    // Trim Option의 경우 "_"를 Description Separate로 사용하지 않도록 수정
                    Pattern p = Pattern.compile("[a-zA-Z0-9]{5}_STD|[a-zA-Z0-9]{5}_OPT");
                    Matcher m = p.matcher(str);
                    boolean isTrimOption = false;
                    while(m.find()) {
                    	String sTemp = m.group().trim();
                    	if (sTemp.equals(str)) {
							isTrimOption = true;
						}
                    }
                    
                    if (!isTrimOption && str.indexOf("_") > -1) {
                        str = str.substring(0, str.indexOf("_"));
                        if (str != null) {
                            str = str.trim();
                        }
                    }

                    setValue(str);
                } else {
                    setValue(selectedItem);
                }
                // setValue(selectedItem);
            } catch (InvalidValueException invalidvalueexception) {
                MessageBox.post(invalidvalueexception);
                return;
            } catch (TCException tcexception) {
                MessageBox.post(tcexception, true);
                return;
            }
        }

        private Object selectedItem;

        public FixedValueSelectionOperation(Object obj) {
            selectedItem = obj;
        }
    }

    private class ValueFromComboItemListener implements ItemListener {

        public void itemStateChanged(ItemEvent itemevent) {
            if (itemevent.getStateChange() == 2)
                return;
            Object obj = valueSelectionCombo.getSelectedItem();
            if (obj == null)
                return;
            if (obj.toString().equals(""))
                obj = null;
            if (obj == null && value == null)
                return;
            if (obj != null && value != null && obj.equals(value))
                return;
            TCComponentBOMLine tccomponentbomline = owningSos.getBomLine();
            if (tccomponentbomline != null) {
                FixedValueSelectionOperation fixedvalueselectionoperation = new FixedValueSelectionOperation(obj);
                tccomponentbomline.getSession().queueOperationAndWait(fixedvalueselectionoperation);
            }
            owningSos.getOwningDialog().refresh();
        }

        private JComboBox valueSelectionCombo;

        ValueFromComboItemListener(JComboBox jcombobox) {
            valueSelectionCombo = jcombobox;
        }
    }

    /**
     * DB에서 모든 Option Value 설명을 가져온다.
     * 
     * @return
     */
    private HashMap getDesc() {

        HashMap valueDescMap = new HashMap();

        SYMCRemoteUtil remote = new SYMCRemoteUtil();
        try {
            DataSet ds = new DataSet();
            ds.put("code_name", null);
            ArrayList<HashMap> list = (ArrayList) remote.execute("com.kgm.service.VariantService", "getVariantValueDesc", ds);
            if (list != null) {
                for (HashMap map : list) {
                    valueDescMap.put(map.get("CODE_NAME"), map.get("CODE_DESC"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return valueDescMap;
    }

    public JComboBox getJcombobox() {
        return jcombobox;
    }

    TableCellEditor getValueSelectionTableCellEditor() {
        // JComboBox jcombobox = new JComboBox();
        // jcombobox.addItem(new String());
        // for(int i = 0; i < getAllowableValues().length; i++)
        // jcombobox.addItem(getAllowableValues()[i]);

        // 20130201. 윤성원
        // 옵션값의 설멸을 보여주기위해 JList의 폭을 넓힘.
        // start ------------------------------------
        jcombobox = new JComboBox() {
            private boolean layingOut = false;

            public void doLayout() {
                try {
                    layingOut = true;
                    super.doLayout();
                } finally {
                    layingOut = false;
                }
            }

            public Dimension getSize() {
                Dimension dim = super.getSize();
                if (!layingOut)
                    dim.width = Math.max(dim.width, getPreferredSize().width);
                return dim;
            }
        };

        HashMap map = getDesc();
        // end ------------------------------------

        // Top에 설정된 옵션 중 제약조건이 걸리지 않은 옵션값만 표기되도록 수정 - Start

//        ArrayList allowableList = new ArrayList();
//        Object[] allowableValues = getAllowableValues();
//        for (Object obj : allowableValues) {
//            allowableList.add(obj);
//        }
//        try {
//            String lineMvl = variantService.askLineMvl(bomLine);
//            ConstraintsModel constraintsModel = new ConstraintsModel(bomLine.getItem().getProperty("item_id"), lineMvl, new HashMap(), bomLine, variantService);
//
//            if (!constraintsModel.parse()) {
//                throw new TCException("Condition을 파싱 할 수 없습니다.");
//            }
//
//            String[][] errorChecks = constraintsModel.errorChecksTableData();
//            ErrorCheck[] errorCheck = constraintsModel.errorChecks();
//            for (String[] error : errorChecks) {
//                allowableList.remove(error[6]);
//            }
//        } catch (TCException tce) {
//            tce.printStackTrace();
//        }
//        // End
        /**
         * [SR150522-021][2015.06.01][jclee] 속도 개선
         */
        // Top에 설정된 옵션 중 제약조건이 걸리지 않은 옵션값만 표기되도록 수정 - Start
        ArrayList allowableList = new ArrayList();
        Object[] allowableValues = getAllowableValues();
        for (Object obj : allowableValues) {
            allowableList.add(obj);
        }
        
        if (errorChecks != null) {
        	for (String[] error : errorChecks) {
        		allowableList.remove(error[6]);
        	}
		}
        // End

        jcombobox.addItem(new String());
        for (int i = 0; i < allowableList.size(); i++) {
            if (allowableList.get(i) instanceof String) {
                // 20130201. 윤성원
                // 옵션값의 설명을 추가함.
                // start ------------------------------------
                String variantValue = (String) allowableList.get(i);
                String desc = "";
                if (map.get(variantValue) == null) {
                    desc = "";
                } else {
                    desc = "_" + map.get(variantValue);
                }
                String str = variantValue + desc;
                // end ---------------------------------------
                jcombobox.addItem(str);
                continue;
            }
            jcombobox.addItem(allowableList.get(i));
        }

        jcombobox.addItemListener(new ValueFromComboItemListener(jcombobox));
        return new DefaultCellEditor(jcombobox);
    }

    protected FixedValueSelection(TCVariantService tcvariantservice, TCComponentBOMLine tccomponentbomline, TCComponent tccomponent,
            com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.BOMVariantConfigurationOption bomvariantconfigurationoption,
            SelectedOptionSet selectedoptionset) throws TCException {
        super(tcvariantservice, tccomponentbomline, tccomponent, bomvariantconfigurationoption, selectedoptionset);
    }

    protected FixedValueSelection(TCVariantService tcvariantservice, TCComponentBOMLine tccomponentbomline, TCComponent tccomponent, int i,
            SelectedOptionSet selectedoptionset) throws TCException {
        super(tcvariantservice, tccomponentbomline, tccomponent, i, selectedoptionset);
    }

    protected void validateValue(Object obj) throws InvalidValueException {
        if (obj == null)
            return;
        for (int i = 0; i < getAllowableValues().length; i++)
            if (getAllowableValues()[i].equals(obj))
                return;

        throw new InvalidValueException(getName(), obj, 0, getAllowableValues());
    }

    protected abstract Object[] getAllowableValues();
    
    public void setErrorChecks(String[][] errorChecks) {
    	this.errorChecks = errorChecks;
    }
}
