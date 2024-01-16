/**
 * MRO Ref BOM 속성 일괄 Upload Dialog
 * 상위클래스(BWXLSImpDialog)와  상이한 기능은 없음( 차후 추가 개발시 세부 기능을 Override하여 구현 하여야 함)
 * 작업Option은 dialogs_locale_ko_KR.properties에 정의 되어 있음
 */
package com.ssangyong.common.bundlework.imp;

import java.util.HashMap;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import com.ssangyong.commands.partmaster.validator.FncMastPartValidator;
import com.ssangyong.commands.partmaster.validator.FncPartValidator;
import com.ssangyong.commands.partmaster.validator.MatPartValidator;
import com.ssangyong.commands.partmaster.validator.ProductPartValidator;
import com.ssangyong.commands.partmaster.validator.ProjectValidator;
import com.ssangyong.commands.partmaster.validator.StdPartValidator;
import com.ssangyong.commands.partmaster.validator.TechDocValidator;
import com.ssangyong.commands.partmaster.validator.ValidatorAbs;
import com.ssangyong.commands.partmaster.validator.VariantPartValidator;
import com.ssangyong.commands.partmaster.validator.VehiclePartValidator;
import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.utils.CustomUtil;

public class BWItemTypeDbImpDialog extends BWPartImpDialog {

    public BWItemTypeDbImpDialog(Shell parent, int style) {
        super(parent, style, BWItemTypeDbImpDialog.class);
    }

    @Override
    public void dialogOpen() {
        super.dialogOpen();
    }

    @Override
    public void validatePost() throws Exception {
        ValidatorAbs validator = null;
        if (SYMCClass.S7_PROJECTTYPE.equals(this.strTargetItemType)) {
            validator = new ProjectValidator(super.szLovNames);
        } else if (SYMCClass.S7_PRODUCTPARTTYPE.equals(this.strTargetItemType)) {
            validator = new ProductPartValidator(super.szLovNames);
        } else if (SYMCClass.S7_FNCPARTTYPE.equals(this.strTargetItemType)) {
            validator = new FncPartValidator(super.szLovNames);
        } else if (SYMCClass.S7_FNCMASTPARTTYPE.equals(this.strTargetItemType)) {
            validator = new FncMastPartValidator(super.szLovNames);
        } else if (SYMCClass.S7_STDPARTTYPE.equals(this.strTargetItemType)) {
            validator = new StdPartValidator(super.szLovNames);
        } else if (SYMCClass.S7_VARIANTPARTTYPE.equals(this.strTargetItemType)) {
            validator = new VariantPartValidator(super.szLovNames);
        } else if (SYMCClass.S7_VEHPARTTYPE.equals(this.strTargetItemType)) {
            validator = new VehiclePartValidator(super.szLovNames);
        } else if (SYMCClass.S7_MATPARTTYPE.equals(this.strTargetItemType)) {
            validator = new MatPartValidator(super.szLovNames);
        } else if (SYMCClass.S7_TECHDOCTYPE.equals(this.strTargetItemType)) {
            validator = new TechDocValidator(super.szLovNames);
        }
        TreeItem[] szTopItems = super.tree.getItems();
        for (int i = 0; i < szTopItems.length; i++) {
            // Top TreeItem
            ManualTreeItem topTreeItem = (ManualTreeItem) szTopItems[i];
            this.validationPart(topTreeItem, validator);
        }
    }

    /**
     * Upload 파일 존재여부 Check
     * 
     * @param treeItem
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void validationPart(ManualTreeItem treeItem, ValidatorAbs validator) {
        HashMap modelMap = treeItem.getModelMap();
        HashMap<String, Object> revionMap = (HashMap<String, Object>) modelMap.get(CLASS_TYPE_REVISION);
        revionMap.put("item_id", treeItem.getItemID());
        revionMap.put("object_name", treeItem.getBWItemAttrValue(CLASS_TYPE_ITEM, "object_name"));
        revionMap.put("uom_tag", treeItem.getBWItemAttrValue(CLASS_TYPE_ITEM, "uom_tag"));
        
        
        // Dataset Type Validate 제외
        if(CLASS_TYPE_DATASET.equals(treeItem.getItemType())) {
            return;
        }
        // validator skip 설정
        validator.setValidatorSkip(true);       
        try {
            String strMessage = validator.validate(revionMap, VehiclePartValidator.TYPE_VALID_MODIFY);
            if (!CustomUtil.isEmpty(strMessage))
                treeItem.setStatus(STATUS_ERROR, strMessage.replaceAll("\\n", ", "));
        } catch (Exception e) {
            e.printStackTrace();
        }
        TreeItem[] childItems = treeItem.getItems();
        for (int i = 0; i < childItems.length; i++) {
            ManualTreeItem cItem = (ManualTreeItem) childItems[i];
            this.validationPart(cItem, validator);
        }
    }
    
    @Override
    public void syncItemState(final ManualTreeItem treeItem, final int nStatus, final String strMessage)
    {
        shell.getDisplay().syncExec(new Runnable()
        {
            
            public void run()
            {
                if(treeItem.getItemType().equals(ManualTreeItem.ITEM_TYPE_TCITEM)) {
                    syncSetItemTextField("** [" + treeItem.getBWItemAttrValue(CLASS_TYPE_ITEM, ITEM_ATTR_CNT) + "/" + totalItemCnt + "]   " + treeItem.getItemID() + " ** \n");
                }                
                if( strMessage == null ) {
                    treeItem.setStatus(nStatus);
                } else {
                    treeItem.setStatus(nStatus, strMessage);
                }
            }
            
        });
    }
}
