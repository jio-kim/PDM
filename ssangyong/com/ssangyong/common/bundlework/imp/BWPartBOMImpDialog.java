/**
 * BOM 일괄 Upload Dialog
 * 상위클래스(BWXLSImpDialog)와  상이한 기능은 없음( 차후 추가 개발시 세부 기능을 Override하여 구현 하여야 함)
 * 작업Option은 bundlework_locale_ko_KR.properties에 정의 되어 있음
 */
package com.ssangyong.common.bundlework.imp;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.bundlework.BWXLSImpDialog;
import com.ssangyong.common.bundlework.bwutil.BWOptionCombinationUtil;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.SYMTcUtil;
import com.ssangyong.common.utils.StringUtil;
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
      
      
      // Dialog 종료시 BOMWindow Close
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
   * Validation 후처리
   * 
   * 존재하지 않는 Revision인 경우 Error 처리
   * Condition정보 입력을 위해  Top Assy는 반드시 FunctionMaster Part만 허용=> 상위 Part가 Function Part
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
   * Function에 Setting된 Option 값 Getter
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
	        // Function에 Setting된 Option 값 
	        HashMap<String, String> optionMap = BWOptionCombinationUtil.getOptionMapList(topLine);
	        // 하위 treeItem Condition Check
	        validateCondition(topItem, optionMap, true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}

  }

  /**
   * Function Master 하위 Part에 입력된 Condition 정보가 올바른지 Check
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
		  // condition 유효성 검사 및 Combination 변경 수식 등록		  
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
   * OPTION_RESULT 문장에서 or, OR를 배열 타입으로 추출한다.
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
  *  OPTION_RESULT 수식을 OR 배열로 분리한 문구를 AND 연산 체크, 검증 및 TC등록 문구로 변경한다. 리턴은 수식에
  * 사용되는 전체 Option Value값을 리턴한다. (Option 설정값과 비교하기 위해 리턴한다.)
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
          throw new Exception("등록하실 Excel파일의 OPTION_RESULT 조건이 없습니다.");
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
                          // Function의 옵션설정이 존재하지않으면 Exception 처리
                          if(optionValue == null) {
                              throw new Exception("OPTION 설정이 정확하지 않습니다.");
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
							 throw new Exception("OPTION내 동일 Category가 중복선택 되었습니다.");
						  }
					  }
				  }
			  }
          }
      }
      if (optionValueList.size() == 0) {          
          throw new Exception("등록하실 Excel파일의 OPTION_RESULT 조건이 없습니다.");
      }  
      return optionStatement;
  }
  
  /**
   * 팀센터 조건 Key / Value값을 생성한다.
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
   * '\n'(OR)을 배열의 열 문장을 가지고 AND 배열 문구를 추출한다.
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
          andStrConditions[i] = StringUtil.nullToString(andStrConditions[i]); // 공백Trim처리
      }
      return andStrConditions;
  }

}
