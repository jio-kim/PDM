/**
 * BOM �ϰ� Upload Dialog
 * ����Ŭ����(BWXLSImpDialog)��  ������ ����� ����( ���� �߰� ���߽� ���� ����� Override�Ͽ� ���� �Ͽ��� ��)
 * �۾�Option�� bundlework_locale_ko_KR.properties�� ���� �Ǿ� ����
 */
package com.kgm.common.bundlework.imp;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import com.kgm.common.SYMCClass;
import com.kgm.common.bundlework.BWXLSImpDialog;
import com.kgm.common.bundlework.bwutil.BWOptionCombinationUtil;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.kgm.common.utils.StringUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;

public class BWPartBOMImpDialog extends BWXLSImpDialog
{
  
  public BWPartBOMImpDialog(Shell parent, int style)
  {
    super(parent, style, BWPartBOMImpDialog.class);
  }
  
  @Override
  public void dialogOpen()
  {
      super.dialogOpen();
      
      
      // Dialog ����� BOMWindow Close
      this.shell.addDisposeListener(new DisposeListener(){

          @Override
          public void widgetDisposed(DisposeEvent e) {

            try
            {
              for( int i = 0 ; i < bomWindowList.size() ; i++)
              {
                TCComponentBOMWindow bomWindow = bomWindowList.get(i);
                if(bomWindow != null)
                {
                  bomWindow.close();
                  bomWindow = null;
                }
              }
            }
            catch(TCException ex)
            {
              ex.printStackTrace();
            }
          }
          
        });
      
      
  }
  
  /**
   * Validation ��ó��
   * 
   * �������� �ʴ� Revision�� ��� Error ó��
   * Condition���� �Է��� ����  Top Assy�� �ݵ�� FunctionMaster Part�� ���=> ���� Part�� Function Part
   * 
   */
  @Override
  public void validatePost()
  {
	  if(!headerModel.isExistAttr(CLASS_TYPE_BOMLINE, "bl_variant_condition"))
	  {
		  return;
	  }  
      TreeItem[] szTopItems = super.tree.getItems();
      for( int i = 0 ; i < szTopItems.length ; i++)
      {    	  	  
          ManualTreeItem topItem =  (ManualTreeItem)szTopItems[i];
    	  try
    	  {    	          	     
              TCComponentItemRevision targetRevision = CustomUtil.findItemRevision("ItemRevision", topItem.getItemID(),  topItem.getBWItemAttrValue(CLASS_TYPE_REVISION, ITEM_ATTR_REVISIONID));
              
              if( targetRevision == null)
              {
            	  throw new Exception("Part Is Not Exist.");
              }
              
              
              if( !SYMCClass.S7_FNCMASTPARTREVISIONTYPE.equals(targetRevision.getType()))
              {
                  continue;
              }  
		      // Latest Working Revision Rule
		      TCComponentRevisionRule revRule = SYMTcUtil.getRevisionRule(targetRevision.getSession(), "Latest Working");
	    	  TCComponent[] imanComps = targetRevision.whereUsed(TCComponent.WHERE_USED_CONFIGURED, revRule);
	    	  TCComponentItemRevision fncRev = null;
	    	  for (int j = 0; j < imanComps.length; j++) 
	    	  {
	    		  if( SYMCClass.S7_FNCPARTREVISIONTYPE.equals( imanComps[j].getType()))
	    		  {
	    			  fncRev = (TCComponentItemRevision)imanComps[j];
	    			  break;
	    		  }
	    	  }
	    	  if( fncRev == null )
	    	  {
	    		  throw new Exception("Function Part Is Not Found.");
	    	  }	    	  
	    	  // Option Loading
	    	  this.getOption(fncRev, topItem);
    	  }
    	  catch(Exception e)
    	  {
    	      //e.printStackTrace();
    		  topItem.setStatus(STATUS_ERROR, e.getMessage());
              continue;
    	  }
      }
  }
  
  /**
   * Function�� Setting�� Option �� Getter
   * 
   * @param fncRev : Function Revision
   * @param topItem : Top TreeItem
   * @throws Exception 
   */
  private void getOption(TCComponentItemRevision fncRev, ManualTreeItem topItem) throws Exception
  {
		try
		{
	        TCComponentBOMWindowType windowType = (TCComponentBOMWindowType) fncRev.getSession().getTypeComponent("BOMWindow");
			// Latest Working Revision Rule
			TCComponentRevisionRule revRule = SYMTcUtil.getRevisionRule(fncRev.getSession(), "Latest Working");
			TCComponentBOMWindow bomWindow = windowType.create(revRule);
	        
	        this.bomWindowList.add(bomWindow);
	        
	        TCComponentBOMLine topLine = bomWindow.setWindowTopLine(null, (TCComponentItemRevision) fncRev, null, null);
	        // Function�� Setting�� Option �� 
	        HashMap<String, String> optionMap = BWOptionCombinationUtil.getOptionMapList(topLine);
	        // ���� treeItem Condition Check
	        validateCondition(topItem, optionMap, true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}

  }

  /**
   * Function Master ���� Part�� �Էµ� Condition ������ �ùٸ��� Check
   * 
   * @param treeItem
   * @param optionMap
   * @param isTop
   */
  private void validateCondition(ManualTreeItem treeItem, HashMap<String, String> optionMap, boolean isTop)
  {
	  if( !isTop)
	  {
		  String strCondition = treeItem.getBWItemAttrValue(CLASS_TYPE_BOMLINE, "bl_variant_condition");		  
		  // condition ��ȿ�� �˻� �� Combination ���� ���� ���		  
		  try {		      
		      if(!CustomUtil.isEmpty(strCondition)) {
		          treeItem.setBWItemAttrValue(CLASS_TYPE_BOMLINE, "bl_variant_condition",this.checkOptionCondition(optionMap, strCondition));
		      }
		  } catch(Exception e) {		      
			  treeItem.setStatus(STATUS_ERROR, e.getMessage());
		  }
	  }
      TreeItem[] childItems = treeItem.getItems();
      for (int i = 0; i < childItems.length; i++)
      {
          ManualTreeItem cItem = (ManualTreeItem) childItems[i];
          this.validateCondition(cItem, optionMap, false);
      }
  }
  
  private String checkOptionCondition(HashMap<String, String> optionMap, String strCondition) throws Exception 
  {
      String[] ortStrConditions = this.getOrStrConditions(strCondition);
      String convertStatement = this.generateOptionStatement(optionMap, ortStrConditions);
      return convertStatement;
  }
  
  /**
   * OPTION_RESULT ���忡�� or, OR�� �迭 Ÿ������ �����Ѵ�.
   * 
   * @method getConvertStrCondition
   * @date 2013. 2. 8.
   * @param
   * @return String[]
   * @exception
   * @throws
   * @see
   */
  private String[] getOrStrConditions(String strCondition) 
  {     
      return StringUtil.getSplitString(strCondition, "\n");
  }
  
 /**
  *  OPTION_RESULT ������ OR �迭�� �и��� ������ AND ���� üũ, ���� �� TC��� ������ �����Ѵ�. ������ ���Ŀ�
  * ���Ǵ� ��ü Option Value���� �����Ѵ�. (Option �������� ���ϱ� ���� �����Ѵ�.)
  * 
  * @method generateOptionStatement 
  * @date 2013. 3. 26.
  * @param
  * @return String
  * @exception
  * @throws
  * @see
  */
  private String  generateOptionStatement(HashMap<String, String> optionMap, String[] orStrConditions) throws Exception 
  {
      ArrayList<String> optionValueList = new ArrayList<String>();
      
      String optionStatement = "";
      if (orStrConditions == null || orStrConditions.length == 0) 
      {          
          throw new Exception("����Ͻ� Excel������ OPTION_RESULT ������ �����ϴ�.");
      }
      for (int i = 0; i < orStrConditions.length; i++) 
      {
          if (i > 0) 
          {
              optionStatement += " or ";
          }
          String[] andStrConditions = this.getAndStrConditions(orStrConditions[i]);
          if (andStrConditions != null) 
          {
              for (int j = 0; j < andStrConditions.length; j++) 
              {
                  if (j > 0) 
                  {
                      optionStatement += " and ";
                  }
                  if (!"".equals(andStrConditions[j])) 
                  {
                      optionValueList.add(andStrConditions[j]);
                      if (i > 0) {
                          optionStatement += " ";
                      }
                      String optionValue = null;
                      try {
                          optionValue = this.getSetOptionItemKeyValue(optionMap, andStrConditions[j]);                           
                      }catch(Exception e) {
                          e.printStackTrace();
                      } finally {
                          // Function�� �ɼǼ����� �������������� Exception ó��
                          if(optionValue == null) {
                              throw new Exception("OPTION ������ ��Ȯ���� �ʽ��ϴ�.");
                          }
                      }
                      optionStatement += optionValue;
                  }
              }
              
              for (int j = 0; j < andStrConditions.length; j++) {
            	  String sStrCategory = "";
            	  sStrCategory = andStrConditions[j].substring(0, 3);
            	  
            	  for (int k = 0; k < andStrConditions.length; k++) {
					  String sTempStrCategory = "";
					  sTempStrCategory = andStrConditions[k].substring(0, 3);
					
					  if (!andStrConditions[j].equals(andStrConditions[k])) {
						  if (sStrCategory.equals(sTempStrCategory)) {
							 throw new Exception("OPTION�� ���� Category�� �ߺ����� �Ǿ����ϴ�.");
						  }
					  }
				  }
			  }
          }
      }
      if (optionValueList.size() == 0) {          
          throw new Exception("����Ͻ� Excel������ OPTION_RESULT ������ �����ϴ�.");
      }  
      return optionStatement;
  }
  
  /**
   * ������ ���� Key / Value���� �����Ѵ�.
   * 
   * --> 00021:A01 = "A0101"
   * 
   * @method getSetOptionItemKeyValue
   * @date 2013. 2. 8.
   * @param
   * @return String
   * @exception
   * @throws
   * @see
   */
  private String getSetOptionItemKeyValue(HashMap<String, String> optionMap, String value) {
      if(optionMap.get(value) == null) {
          return null;
      }
      return optionMap.get(value) + " = \"" + value + "\"";
  }

  /**
   * '\n'(OR)�� �迭�� �� ������ ������ AND �迭 ������ �����Ѵ�.
   * 
   * @method getOrStrConditions
   * @date 2013. 2. 8.
   * @param
   * @return String[]
   * @exception
   * @throws
   * @see
   */
  private String[] getAndStrConditions(String orString) {
      if (orString == null) {
          return null;
      }
      // StringUtil.replace(orString, "AND", "and");
      orString = orString.replaceAll("AND", "&");
      orString = orString.replaceAll("and", "&");
      String[] andStrConditions = StringUtil.getSplitString(orString, "&");
      for (int i = 0; andStrConditions != null && i < andStrConditions.length; i++) {
          andStrConditions[i] = StringUtil.nullToString(andStrConditions[i]); // ����Trimó��
      }
      return andStrConditions;
  }

}
