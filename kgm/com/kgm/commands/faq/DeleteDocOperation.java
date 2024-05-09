package com.kgm.commands.faq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;

/**
 * [SR150421-027][20150811][ymjang] PLM system �������� - Manual ��ȸ ������� �߰�
 */
public class DeleteDocOperation extends AbstractAIFOperation {

	private TCSession session;
	private HashMap<String, Object> dataMap;

	public DeleteDocOperation(HashMap<String, Object> dataMap) {
		this.session = (TCSession) AIFUtility.getDefaultSession();
		this.dataMap = dataMap;
	}

	@Override
	public void executeOperation() throws Exception {

		Markpoint markpoint = null;

		try {
			markpoint = new Markpoint(session);

			TCComponentItem deleteItem = (TCComponentItem) dataMap.get("deleteItem");
			TCComponentItemRevision itemRevision = deleteItem.getLatestItemRevision();

			List<TCComponent> componentList = new ArrayList<TCComponent>();
			componentList.add(itemRevision);

			AIFComponentContext[] contexts = deleteItem.whereReferenced();
			for (AIFComponentContext context : contexts) {
				InterfaceAIFComponent aifComponent = context.getComponent();
				if (aifComponent instanceof TCComponentFolder) {
					TCComponentFolder folder = (TCComponentFolder) aifComponent;
					folder.cutOperation("contents", new TCComponent[]{deleteItem});
				}
			}

			// Dataset ����
			TCComponent[] components = itemRevision.getRelatedComponents("IMAN_specification");
			for (TCComponent component : components) {
				TCComponentDataset dataset = (TCComponentDataset) component;
				itemRevision.remove("IMAN_specification", dataset);
				dataset.delete();
			}
			
			// ������ �Ǵ� ������ ����
			String revisionId = itemRevision.getProperty("item_revision_id");
			if (revisionId.equals("A")) {
				itemRevision.getItem().delete();
			} else {
				itemRevision.delete();
			}

			markpoint.forget();
		} catch (Exception e) {
			markpoint.rollBack();

			throw e;
		}
	}

}
