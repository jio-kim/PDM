package com.kgm.commands.workflow.newprocess;

import java.awt.Frame;
import java.util.ArrayList;

import com.kgm.common.SimpleProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFApplication;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentRevisionRuleType;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * 결재 요청시 벨리데이션 로직이 들어감
 * @Copyright : S-PALM
 * @author   : 이정건
 * @since    : 2012. 3. 20.
 * Package ID : com.pungkang.commands.workflow.newprocess.NewProcessCommand.java
 */
@SuppressWarnings({"unused", "rawtypes", "unchecked"})
public class NewProcessCommand extends AbstractAIFCommand{
	private Frame parent;
    private AbstractAIFApplication application;
	private TCComponent[] targetComponent;
	private Registry registry = Registry.getRegistry(this);
	private SimpleProgressBar bar;

	public NewProcessCommand(){
		//this(AIFUtility.getActiveDesktop(), AIFUtility.getTargetComponents());
		this(AIFUtility.getActiveDesktop(), AIFUtility.getCurrentApplication().getTargetComponents());
	}

	public NewProcessCommand(Frame parent, InterfaceAIFComponent[] target){
		this.parent = parent;
		this.application = AIFUtility.getCurrentApplication();
		try{
			bar = new SimpleProgressBar(parent, "결재 대상에 대한 정보를 확인 중 입니다...");
			boolean ecoCheck = true;
			
			targetComponent = checkComponent(target);

			if(targetComponent == null || !ecoCheck){
				bar.closeProgressBar();
			}
			else{
				NewProcessDialog workflowDialog = new NewProcessDialog(parent, targetComponent);
				workflowDialog.setModal(true);
				bar.closeProgressBar();
				setRunnable(workflowDialog);
			}
		} catch(Exception ex){
			bar.closeProgressBar();
			MessageBox.post(parent, ex, true);
		}
	}

	public TCComponent[] checkComponent(InterfaceAIFComponent[] target) throws Exception{
		ArrayList targetList = new ArrayList();
		if(target == null || target.length == 0){
			bar.closeProgressBar();
			MessageBox.post(parent, registry.getString("Message8"), registry.getString("Message.TITLE"), MessageBox.INFORMATION);
			return null;
		}

		if(!((TCComponent)target[0]).isTypeOf("ItemRevision")){
			bar.closeProgressBar();
			MessageBox.post(parent, registry.getString("Message10"), registry.getString("Message.TITLE"), MessageBox.INFORMATION);
			return null;
		}

		for(int i = 0; i < target.length; i++){
			TCComponent targetComponent = (TCComponent)target[i];
			//String classType = targetComponent.getClassType();
			String classType = targetComponent.getType();
			if(classType.equals("BOMLine")){
				targetComponent = ((TCComponentBOMLine)targetComponent).getItemRevision();
			}
			if(isCheckOut(targetComponent, null)){
				bar.closeProgressBar();
				MessageBox.post(parent, "선택한 대상 또는 하위에 Check-out 된 대상이 존재합니다.\n"
						+ "확인하시고 Check-in 또는 cancel Check-out을 하시고 재 결재 요청 하십시오.\n\n대상 : \n   ▶ " + targetComponent.toString(), registry.getString("Message.TITLE"), MessageBox.INFORMATION);
				return null;
			}
			if(CustomUtil.isReleased(targetComponent)){
				bar.closeProgressBar();
				MessageBox.post(parent, "선택한 대상은 결재 완료 되었습니다.\n대상 : " + targetComponent.toString(), registry.getString("Message.TITLE"), MessageBox.INFORMATION);
				return null;
			}
			if(CustomUtil.isInProcess(targetComponent)){
				bar.closeProgressBar();
				MessageBox.post(parent, "선택한 대상은 현재 결재 진행 중 입니다.\n대상 : " + targetComponent.toString(), registry.getString("Message.TITLE"), MessageBox.INFORMATION);
				return null;
			}
			if(!checkBOM(targetComponent))
			{
				return null;
			}

			//			// 결재 대상이 ECO일 경우 solution item PseudoFolder 하위에 있는 단품/BOM을 체크
			//			if(target[0].getType().equals(PK.ECO_TYPE)){
			//				TCComponent[] solutionItems = ((TCComponent)target[0]).getRelatedComponents("EC_solution_item_rel");
			//				if(solutionItems != null && solutionItems.length != 0){
			//					for(int k=0; k<solutionItems.length; k++){
			//						if(!checkBOM(solutionItems[k]))
			//						{
			//							return null;
			//						}
			//					}
			//				}
			//			}

			targetList.add(targetComponent);
		}
		TCComponent[] targetComponents = new TCComponent[targetList.size()];
		for(int i = 0; i < targetList.size(); i++){
			targetComponents[i] = (TCComponent)targetList.get(i);
		}
		return targetComponents;
	}

	private boolean checkBOM(TCComponent revision) throws Exception{
		//assay bom인지 체크한다. 단품은 불가.
		AIFComponentContext[] acc = revision.getChildren("structure_revisions");
		if(acc == null || acc.length < 1){
			//BOM이 아니면 체크할게 없으므로 true
			return true;
		}
		if(acc.length > 1){
			bar.closeProgressBar();
			MessageBox.post(parent, "두개의 BOM View Revision이 붙어 있습니다. 확인하시기 바랍니다.\n대상 : " + revision.toString(), registry.getString("Message.TITLE"), MessageBox.INFORMATION);
			return false;
		}
		//BOM을 결재 올린다는 메시지를 보여줌.
		//		MessageBox messageBox = new MessageBox(parent, "선택하신 대상은 BOM 입니다.\n"
		//				+ "선택된 하위의 모든 대상이 결재 대상으로 자동 첨부 됩니다.\n"
		//				+ "\"확인\"을 클릭하면 계속 진행 됩니다.", null, registry.getString("Message.TITLE"), MessageBox.INFORMATION, true);
		//		messageBox.setVisible(true);

		//		TCComponent[] parentComp = revision.whereUsed(TCComponent.WHERE_USED_ALL);
		//		if(parentComp != null && parentComp.length > 0)
		//		{
		//			bar.closeProgressBar();
		//			int r = ConfirmationDialog.post((JFrame)parent, "알림", "선택하신 대상은 최상위 Assay(제품)가 아닙니다.\n"
		//					+ "최상위 Assay를 선택하시면 하위의 모든 대상도 함께 결재 됩니다.\n"
		//					+ "현재 선택한 대상을 결재 요청하시면 현재 대상의 하위 대상들만 결재가 됩니다.(상위 부품은 결재 안됨)\n"
		//					+ "계속 하시겠습니까?");
		//			if(r != ConfirmationDialog.YES)
		//			{
		//				return false;
		//			}
		//		}
		TCSession session = revision.getSession();
		TCComponentRevisionRuleType revisionRuleType = (TCComponentRevisionRuleType)session.getTypeComponent("RevisionRule");
		TCComponentRevisionRule rule = revisionRuleType.getDefaultRule();

		TCComponentBOMWindowType bomWindowType = (TCComponentBOMWindowType)session.getTypeComponent("BOMWindow");
		TCComponentBOMWindow bomWindow = bomWindowType.create(rule);
		TCComponentBOMLine topLine = bomWindow.setWindowTopLine(null, (TCComponentItemRevision)revision, null, null);
		//        ArrayList quantityErrorList = new ArrayList();
		//		ArrayList writeErrorList = new ArrayList();
		ArrayList checkoutErrorList = new ArrayList();
		ArrayList inProcessErrorList = new ArrayList();

		getCheckChildBOM(topLine, checkoutErrorList, inProcessErrorList);
		if(!checkoutErrorList.isEmpty() || !inProcessErrorList.isEmpty())
		{
			String checkoutErrorMessage = "";
			String writeErrorMessage = "";
			String inProcessErrorMessage = "";
			String detailMessage = "";
			for(int i = 0; i < checkoutErrorList.size(); i++)
			{
				if(i == 0)
				{
					checkoutErrorMessage = "※ 선택된 대상의 하위중에 check-out 된 대상이 존재합니다.\n";
					detailMessage += "⊙ Check-out 대상 리스트.\n";
				}
				detailMessage += "   ▶ " + checkoutErrorList.get(i) + " (" + ((TCComponent)checkoutErrorList.get(i)).getType() + ")\n";
			}
			if(!detailMessage.equals(""))
			{
				detailMessage += "\n";
			}
			for(int i = 0; i < inProcessErrorList.size(); i++)
			{
				if(i == 0)
				{
					writeErrorMessage = "※ 선택된 대상의 하위중에 결재중인 대상이 존재합니다.\n";
					detailMessage += "⊙ 결재중인  리스트.\n";
				}
				detailMessage += "   ▶ " + inProcessErrorList.get(i) + "\n";
			}
			if(!detailMessage.equals(""))
			{
				detailMessage += "\n";
			}

			// 수량 체크하는 부분       
			//            for(int i = 0; i < quantityErrorList.size(); i++)
			//            {
			//                if(i == 0)
			//                {
			//                    quantityErrorMessage = "※ 선택된 대상의 하위중에 수량이 입력되지 않은 대상이 존재합니다.\n";
			//                    detailMessage += "● 수량이 입력되지 않은 대상 리스트.\n";
			//                }
			//                detailMessage += "   ⊙ " + quantityErrorList.get(i) + "\n";
			//            }
			bar.closeProgressBar();
			MessageBox.post(parent, "BOM 하위 결재 대상에 대하여 아래 에러를 발견 하였습니다.\n문제를 확인하고 수정 후 재 결재 요청 해주시기 바랍니다.\n\n"
					+ checkoutErrorMessage + writeErrorMessage + inProcessErrorMessage
					+ "\n상세 내용은 아래 \"상세정보...\"를 이용하여 보시기 바랍니다.",
					detailMessage, registry.getString("Message.TITLE"), MessageBox.INFORMATION);
			return false;
		}
		return true;
	}

	/**
	 * 쓰기 권한, check-out, 결재중인것을 구별함.
	 * @param parentLine TCComponentBOMLine
	 * @param okList ArrayList
	 * @param writeErrorList ArrayList
	 * @param quantityErrorList ArrayList
	 * @throws Exception
	 */
	private void getCheckChildBOM(TCComponentBOMLine parentLine, ArrayList checkoutErrorList, ArrayList inProcessErrorList) throws Exception
	{
		AIFComponentContext[] childAcc = parentLine.getChildren();
		for(int i = 0; i < childAcc.length; i++)
		{
			TCComponentBOMLine childLine = (TCComponentBOMLine)childAcc[i].getComponent();
			TCComponentItemRevision childRevision = childLine.getItemRevision();
			//			if(CustomUtil.isReleased(childRevision) || CustomUtil.isInProcess(childRevision))
			//			{
			//				continue;
			//			}
			//check out 체크...
			isCheckOut(childRevision, checkoutErrorList);
			//쓰기권한 체크...
			//			if(!childRevision.okToModify())
			//			{
			//				if(!writeErrorList.contains(childRevision))
			//				{
			//					writeErrorList.add(childRevision);
			//				}
			//			}
			//결재중인 아이템이 있는지 체크...
			if(CustomUtil.isInProcess(childRevision))
			{
				if(!inProcessErrorList.contains(childRevision))
				{
					inProcessErrorList.add(childRevision);
				}
			}
			if(childLine.hasChildren())
			{
				getCheckChildBOM(childLine, checkoutErrorList, inProcessErrorList);
			}
		}
	}

	private boolean isCheckOut(TCComponent comp, ArrayList checkoutList) throws Exception
	{
		if(CustomUtil.isTargetCheckOut(comp))
		{
			if(checkoutList == null)
			{
				return true;
			} else
			{
				if(!checkoutList.contains(comp))
				{
					checkoutList.add(comp);
				}
			}
		}
		AIFComponentContext[] child = comp.getChildren();
		for(int i = 0; i < child.length; i++)
		{
			TCComponent childComp = (TCComponent)child[i].getComponent();
			if(CustomUtil.isTargetCheckOut(childComp))
			{
				if(checkoutList == null)
				{
					return true;
				} else
				{
					if(!checkoutList.contains(childComp))
					{
						checkoutList.add(childComp);
					}
				}
			}
		}
		return false;
	}
}
