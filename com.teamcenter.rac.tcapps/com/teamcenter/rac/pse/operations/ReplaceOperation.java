package com.teamcenter.rac.pse.operations;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.SYMCBOMLine;
import com.teamcenter.rac.kernel.ServiceData;
import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentGDE;
import com.teamcenter.rac.kernel.TCComponentGDELine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.services.internal.rac.structuremanagement.RestructureService;
import com.teamcenter.services.internal.rac.structuremanagement._2014_12.Restructure.ReplaceItemsParameter;
/**
 * [UPGRADE] TC13 Upgrade 시 추가함.
 * SYMCEBomline의  replace 함수가 동작하지 않아서 Replace 하기 전 기능을 추가함 
 *
 */
public class ReplaceOperation extends AbstractAIFOperation {
   private final TCComponentBOMLine target;
   private final TCComponentItem item;
   private final TCComponentItemRevision rev;
   private final TCComponent bv;
   private final TCComponentGDE gde;
   private final int replaceOption;
   public static final int REPLACE_SINGLE = 0;
   public static final int REPLACE_SIBLINGS = 1;
   public static final int REPLACE_ALL = 2;

   public ReplaceOperation(TCComponentBOMLine targetBOMLine, TCComponentItem item, TCComponentItemRevision revision, TCComponent bomView, boolean replaceOption) {
      this.target = targetBOMLine;
      this.item = item;
      this.rev = revision;
      this.bv = bomView;
      if (replaceOption) {
         this.replaceOption = 1;
      } else {
         this.replaceOption = 0;
      }

      this.gde = null;
   }

   public ReplaceOperation(TCComponentBOMLine targetBOMLine, TCComponentItem item, TCComponentItemRevision revision, TCComponent bomView, TCComponentGDE gde, boolean replaceOption) {
      this.target = targetBOMLine;
      this.item = item;
      this.rev = revision;
      this.bv = bomView;
      this.gde = gde;
      if (replaceOption) {
         this.replaceOption = 1;
      } else {
         this.replaceOption = 0;
      }

   }

   public ReplaceOperation(TCComponentBOMLine targetBOMLine, TCComponentItem item, TCComponentItemRevision revision, TCComponent bomView, int replaceOption) {
      this.target = targetBOMLine;
      this.item = item;
      this.rev = revision;
      this.bv = bomView;
      this.replaceOption = replaceOption;
      this.gde = null;
   }

   public void executeOperation() throws TCException {
      Registry registry = Registry.getRegistry(this);
      boolean isReplaceAble = true;
      
      /**
       * Replace 시 BOM 체크 및 Replace 되는 Revision 에 ECO 첨부
       */
      if(target instanceof SYMCBOMLine)
      {
    	  SYMCBOMLine targetBomLine = (SYMCBOMLine)target;
    	  TCComponentBOMLine parentBOMLine = targetBomLine.parent();
    	   
    	  if (parentBOMLine != null && targetBomLine.isHistoryTarget(parentBOMLine)) {
    		  if(!targetBomLine.isHistoryChildAddOrReplacable(parentBOMLine, rev)) {
    			  isReplaceAble = false; 
    		  }else
    		  {
    			  if(!targetBomLine.isAddOrReplacable(parentBOMLine, rev)) {
    				  isReplaceAble = false; 
    			  }
    		  }
    	  }
      }
      
      if(!isReplaceAble)
    	  return ;
      
      
      if (this.replaceOption == 0 && this.gde != null) {
         try {
            ((TCComponentGDELine)this.target).replaceGDE(this.gde);
         } catch (Exception ex) {
            MessageBox.post(this.getCurrentDesktop(), ex, registry.getString("replaceError.TITLE"));
         }
      } else {
         TCSession session = this.target.getSession();
         RestructureService restructureService = RestructureService.getService(session);
         ReplaceItemsParameter preplaceParam = new ReplaceItemsParameter();
         preplaceParam.bomLine = this.target;
         preplaceParam.itemRevision = this.rev;
         preplaceParam.item = this.item;
         preplaceParam.viewType = this.bv;
         preplaceParam.replaceOption = this.replaceOption;
         ReplaceItemsParameter[] preplaceParams = new ReplaceItemsParameter[]{preplaceParam};
         ServiceData serviceData = restructureService.replaceItems(preplaceParams);
         if (!SoaUtil.handlePartialErrors(serviceData, AIFUtility.getActiveDesktop().getShell(), registry.getString("replaceError.TITLE"), false)) {
            return;
         }
      }

   }
}
