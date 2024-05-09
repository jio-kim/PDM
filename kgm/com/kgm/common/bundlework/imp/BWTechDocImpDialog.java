/**
 * Tech Doc 속성 일괄 Upload Dialog
 * 상위클래스(BWXLSImpDialog)와  상이한 기능은 없음( 차후 추가 개발시 세부 기능을 Override하여 구현 하여야 함)
 * 작업Option은 bundlework_locale_ko_KR.properties에 정의 되어 있음
 */
package com.kgm.common.bundlework.imp;

import java.util.HashMap;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import com.kgm.commands.partmaster.validator.TechDocValidator;
import com.kgm.commands.partmaster.validator.VehiclePartValidator;
import com.kgm.common.bundlework.BWXLSImpDialog;
import com.kgm.common.utils.CustomUtil;

@SuppressWarnings({"rawtypes", "unchecked"})
public class BWTechDocImpDialog extends BWXLSImpDialog
{
  
  public BWTechDocImpDialog(Shell parent, int style)
  {
    super(parent, style, BWTechDocImpDialog.class);
  }
  
  @Override
  public void dialogOpen()
  {
      super.dialogOpen();
  }

  

  /**
   * 생성전 Validation Check
   */
  @Override
  public void validatePost() throws Exception {
      
	  TechDocValidator validator = new TechDocValidator(null);
      TreeItem[] szTopItems = super.tree.getItems();
      for (int i = 0; i < szTopItems.length; i++) {

          // Top TreeItem
          ManualTreeItem topTreeItem = (ManualTreeItem) szTopItems[i];
          this.validationTechDoc(topTreeItem, validator);
      }
  }


  /**
   * 개별 Validation Check
   * 
   * @param treeItem
   * @param validator
   */
  private void validationTechDoc(ManualTreeItem treeItem, TechDocValidator validator) {
	  
	  if(treeItem.getItemType().equals(ManualTreeItem.ITEM_TYPE_TCDATASET))
		  return;
	  
      HashMap modelMap = treeItem.getModelMap();
      HashMap<String, Object> revionMap = (HashMap<String, Object>) modelMap.get(CLASS_TYPE_ITEM);

      revionMap.put("item_id", treeItem.getItemID());
      revionMap.put("object_name", treeItem.getBWItemAttrValue(CLASS_TYPE_ITEM, "object_name"));

      try {
    	  // Tech Doc Validation Check
          String strMessage = validator.validate(revionMap, VehiclePartValidator.TYPE_VALID_CREATE);
          if (!CustomUtil.isEmpty(strMessage))
              treeItem.setStatus(STATUS_ERROR, strMessage.replaceAll("\\n", ", "));
      } catch (Exception e) {
          e.printStackTrace();
      }

      TreeItem[] childItems = treeItem.getItems();
      for (int i = 0; i < childItems.length; i++) {
          ManualTreeItem cItem = (ManualTreeItem) childItems[i];
          this.validationTechDoc(cItem, validator);

      }
  }

  

  
}
