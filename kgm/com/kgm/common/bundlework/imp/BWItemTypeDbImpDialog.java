/**
 * MRO Ref BOM �Ӽ� �ϰ� Upload Dialog
 * ����Ŭ����(BWXLSImpDialog)��  ������ ����� ����( ���� �߰� ���߽� ���� ����� Override�Ͽ� ���� �Ͽ��� ��)
 * �۾�Option�� dialogs_locale_ko_KR.properties�� ���� �Ǿ� ����
 */
package com.kgm.common.bundlework.imp;

import java.util.HashMap;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import com.kgm.commands.partmaster.validator.FncMastPartValidator;
import com.kgm.commands.partmaster.validator.FncPartValidator;
import com.kgm.commands.partmaster.validator.MatPartValidator;
import com.kgm.commands.partmaster.validator.ProductPartValidator;
import com.kgm.commands.partmaster.validator.ProjectValidator;
import com.kgm.commands.partmaster.validator.StdPartValidator;
import com.kgm.commands.partmaster.validator.TechDocValidator;
import com.kgm.commands.partmaster.validator.ValidatorAbs;
import com.kgm.commands.partmaster.validator.VariantPartValidator;
import com.kgm.commands.partmaster.validator.VehiclePartValidator;
import com.kgm.common.SYMCClass;
import com.kgm.common.utils.CustomUtil;

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
     * Upload ���� ���翩�� Check
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
        
        
        // Dataset Type Validate ����
        if(CLASS_TYPE_DATASET.equals(treeItem.getItemType())) {
            return;
        }
        // validator skip ����
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
