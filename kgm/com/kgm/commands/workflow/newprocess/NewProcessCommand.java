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
 * ���� ��û�� �������̼� ������ ��
 * @Copyright : S-PALM
 * @author   : ������
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
			bar = new SimpleProgressBar(parent, "���� ��� ���� ������ Ȯ�� �� �Դϴ�...");
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
				MessageBox.post(parent, "������ ��� �Ǵ� ������ Check-out �� ����� �����մϴ�.\n"
						+ "Ȯ���Ͻð� Check-in �Ǵ� cancel Check-out�� �Ͻð� �� ���� ��û �Ͻʽÿ�.\n\n��� : \n   �� " + targetComponent.toString(), registry.getString("Message.TITLE"), MessageBox.INFORMATION);
				return null;
			}
			if(CustomUtil.isReleased(targetComponent)){
				bar.closeProgressBar();
				MessageBox.post(parent, "������ ����� ���� �Ϸ� �Ǿ����ϴ�.\n��� : " + targetComponent.toString(), registry.getString("Message.TITLE"), MessageBox.INFORMATION);
				return null;
			}
			if(CustomUtil.isInProcess(targetComponent)){
				bar.closeProgressBar();
				MessageBox.post(parent, "������ ����� ���� ���� ���� �� �Դϴ�.\n��� : " + targetComponent.toString(), registry.getString("Message.TITLE"), MessageBox.INFORMATION);
				return null;
			}
			if(!checkBOM(targetComponent))
			{
				return null;
			}

			//			// ���� ����� ECO�� ��� solution item PseudoFolder ������ �ִ� ��ǰ/BOM�� üũ
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
		//assay bom���� üũ�Ѵ�. ��ǰ�� �Ұ�.
		AIFComponentContext[] acc = revision.getChildren("structure_revisions");
		if(acc == null || acc.length < 1){
			//BOM�� �ƴϸ� üũ�Ұ� �����Ƿ� true
			return true;
		}
		if(acc.length > 1){
			bar.closeProgressBar();
			MessageBox.post(parent, "�ΰ��� BOM View Revision�� �پ� �ֽ��ϴ�. Ȯ���Ͻñ� �ٶ��ϴ�.\n��� : " + revision.toString(), registry.getString("Message.TITLE"), MessageBox.INFORMATION);
			return false;
		}
		//BOM�� ���� �ø��ٴ� �޽����� ������.
		//		MessageBox messageBox = new MessageBox(parent, "�����Ͻ� ����� BOM �Դϴ�.\n"
		//				+ "���õ� ������ ��� ����� ���� ������� �ڵ� ÷�� �˴ϴ�.\n"
		//				+ "\"Ȯ��\"�� Ŭ���ϸ� ��� ���� �˴ϴ�.", null, registry.getString("Message.TITLE"), MessageBox.INFORMATION, true);
		//		messageBox.setVisible(true);

		//		TCComponent[] parentComp = revision.whereUsed(TCComponent.WHERE_USED_ALL);
		//		if(parentComp != null && parentComp.length > 0)
		//		{
		//			bar.closeProgressBar();
		//			int r = ConfirmationDialog.post((JFrame)parent, "�˸�", "�����Ͻ� ����� �ֻ��� Assay(��ǰ)�� �ƴմϴ�.\n"
		//					+ "�ֻ��� Assay�� �����Ͻø� ������ ��� ��� �Բ� ���� �˴ϴ�.\n"
		//					+ "���� ������ ����� ���� ��û�Ͻø� ���� ����� ���� ���鸸 ���簡 �˴ϴ�.(���� ��ǰ�� ���� �ȵ�)\n"
		//					+ "��� �Ͻðڽ��ϱ�?");
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
					checkoutErrorMessage = "�� ���õ� ����� �����߿� check-out �� ����� �����մϴ�.\n";
					detailMessage += "�� Check-out ��� ����Ʈ.\n";
				}
				detailMessage += "   �� " + checkoutErrorList.get(i) + " (" + ((TCComponent)checkoutErrorList.get(i)).getType() + ")\n";
			}
			if(!detailMessage.equals(""))
			{
				detailMessage += "\n";
			}
			for(int i = 0; i < inProcessErrorList.size(); i++)
			{
				if(i == 0)
				{
					writeErrorMessage = "�� ���õ� ����� �����߿� �������� ����� �����մϴ�.\n";
					detailMessage += "�� ��������  ����Ʈ.\n";
				}
				detailMessage += "   �� " + inProcessErrorList.get(i) + "\n";
			}
			if(!detailMessage.equals(""))
			{
				detailMessage += "\n";
			}

			// ���� üũ�ϴ� �κ�       
			//            for(int i = 0; i < quantityErrorList.size(); i++)
			//            {
			//                if(i == 0)
			//                {
			//                    quantityErrorMessage = "�� ���õ� ����� �����߿� ������ �Էµ��� ���� ����� �����մϴ�.\n";
			//                    detailMessage += "�� ������ �Էµ��� ���� ��� ����Ʈ.\n";
			//                }
			//                detailMessage += "   �� " + quantityErrorList.get(i) + "\n";
			//            }
			bar.closeProgressBar();
			MessageBox.post(parent, "BOM ���� ���� ��� ���Ͽ� �Ʒ� ������ �߰� �Ͽ����ϴ�.\n������ Ȯ���ϰ� ���� �� �� ���� ��û ���ֽñ� �ٶ��ϴ�.\n\n"
					+ checkoutErrorMessage + writeErrorMessage + inProcessErrorMessage
					+ "\n�� ������ �Ʒ� \"������...\"�� �̿��Ͽ� ���ñ� �ٶ��ϴ�.",
					detailMessage, registry.getString("Message.TITLE"), MessageBox.INFORMATION);
			return false;
		}
		return true;
	}

	/**
	 * ���� ����, check-out, �������ΰ��� ������.
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
			//check out üũ...
			isCheckOut(childRevision, checkoutErrorList);
			//������� üũ...
			//			if(!childRevision.okToModify())
			//			{
			//				if(!writeErrorList.contains(childRevision))
			//				{
			//					writeErrorList.add(childRevision);
			//				}
			//			}
			//�������� �������� �ִ��� üũ...
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
