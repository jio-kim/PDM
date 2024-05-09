/**
 * Standard Part �Ӽ� �ϰ� Upload Dialog
 * ����Ŭ����(BWXLSImpDialog)��  ������ ����� ����( ���� �߰� ���߽� ���� ����� Override�Ͽ� ���� �Ͽ��� ��)
 * �۾�Option�� bundlework_locale_ko_KR.properties�� ���� �Ǿ� ����
 */
package com.kgm.common.bundlework.imp;

import java.util.HashMap;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import com.kgm.commands.partmaster.validator.StdPartValidator;
import com.kgm.commands.partmaster.validator.VehiclePartValidator;
import com.kgm.common.bundlework.BWXLSImpDialog;
import com.kgm.common.utils.CustomUtil;

@SuppressWarnings({"unused", "rawtypes", "unchecked"})
public class BWStandardPartImpDialog extends BWXLSImpDialog
{
  
  public BWStandardPartImpDialog(Shell parent, int style)
  {
    super(parent, style, BWStandardPartImpDialog.class);
  }
  
  @Override
  public void dialogOpen()
  {
      super.dialogOpen();
  }

  

  /**
   * ������ Validation Check
   */
  @Override
  public void validatePost() throws Exception {
//	  
//      String[][] szLovNames = { { "uom_tag", "Unit of Measures" }, { "s7_MATURITY", "S7_MATURITY" }};
//      StdPartValidator validator = new StdPartValidator(szLovNames);
//      TreeItem[] szTopItems = super.tree.getItems();
//      for (int i = 0; i < szTopItems.length; i++) {
//
//          // Top TreeItem
//          ManualTreeItem topTreeItem = (ManualTreeItem) szTopItems[i];
//          this.validationStandardPart(topTreeItem, validator);
//      }
//      
      
  }


  /**
   * Standard Part ���� Validation Check
   * 
   * @param treeItem
   * @param validator
   */
  private void validationStandardPart(ManualTreeItem treeItem, StdPartValidator validator) {
      HashMap modelMap = treeItem.getModelMap();
      HashMap<String, Object> revionMap = (HashMap<String, Object>) modelMap.get(CLASS_TYPE_REVISION);

      revionMap.put("item_id", treeItem.getItemID());
      revionMap.put("object_name", treeItem.getBWItemAttrValue(CLASS_TYPE_ITEM, "object_name"));

      try {
    	  // Standard Part Validation Check
          String strMessage = validator.validate(revionMap, VehiclePartValidator.TYPE_VALID_CREATE);
          if (!CustomUtil.isEmpty(strMessage))
              treeItem.setStatus(STATUS_ERROR, strMessage.replaceAll("\\n", ", "));
      } catch (Exception e) {
          e.printStackTrace();
      }

      TreeItem[] childItems = treeItem.getItems();
      for (int i = 0; i < childItems.length; i++) {
          ManualTreeItem cItem = (ManualTreeItem) childItems[i];
          this.validationStandardPart(cItem, validator);

      }
  }

  
}
