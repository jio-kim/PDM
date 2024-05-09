package com.kgm.commands.variantoptioneditor;

import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.pse.common.BOMTreeTable;

public class MVConditionCommand extends AbstractAIFCommand {

	private TCComponentBOMLine paramOfTCComponentBOMLine;
	private BOMTreeTable paramBOMTreeTable;

	public MVConditionCommand() throws Exception {
		InterfaceAIFComponent[] targets = AIFUtility.getCurrentApplication().getTargetComponents();
		TCComponent comp = (TCComponent) targets[0];
		TCComponentBOMLine bomline = (TCComponentBOMLine) comp;

		AIFUtility.getCurrentApplication().getTargetComponents();
		this.paramOfTCComponentBOMLine = bomline.parent();
		Shell shell = AIFUtility.getCurrentApplication().getDesktop().getShell();
		MVConditionDialog mvConditionDialog = new MVConditionDialog(shell, paramOfTCComponentBOMLine, paramBOMTreeTable);
		mvConditionDialog.open();

	}

	/**
	 * BomView 를 획득.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 12. 20.
	 * @param context
	 * @throws Exception
	 */
	// private void getBomLine(AIFComponentContext[] context) throws Exception {
	// if (context.length > 0) {
	// TCComponentBOMViewRevision bomViewRevision = (TCComponentBOMViewRevision) context[0]
	// .getComponent();
	// TCComponentRevisionRuleType revisionRuleType = (TCComponentRevisionRuleType) session
	// .getTypeComponent("RevisionRule");
	// TCComponentRevisionRule revisionRule = revisionRuleType.getDefaultRule();
	// TCComponentBOMWindowType bomWindowType = (TCComponentBOMWindowType) session
	// .getTypeComponent("BOMWindow");
	// TCComponentBOMWindow bomWindow = bomWindowType.create(revisionRule);
	// TCComponentBOMLine bomTopLine = bomWindow.setWindowTopLine(null, null, null,
	// bomViewRevision);
	// getBomVtr.addElement(bomTopLine);
	// this.findChildren(bomTopLine); // 하위 컴포넌트들을 찾는다.
	//
	// bomWindow.close();
	// }
	// }

	/**
	 * 자식 BOM Line를 BOM Vector에 담음.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 12. 20.
	 * @param bomLine
	 * @throws Exception
	 */
	// private void findChildren(TCComponentBOMLine bomLine) throws Exception {
	// AIFComponentContext[] bomLineContext = bomLine.getChildren();
	// if (bomLineContext.length != 0) {
	// for (int i = 0; i < bomLineContext.length; i++) {
	// InterfaceAIFComponent con = bomLineContext[i].getComponent();
	// TCComponentBOMLine bom = (TCComponentBOMLine) con;
	// getBomVtr.addElement(bom);
	// this.findChildren((TCComponentBOMLine) bomLineContext[i].getComponent());
	// }
	// }
	// }
}
