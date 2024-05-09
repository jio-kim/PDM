package com.kgm.commands.faq;

import java.util.HashMap;

import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

/**
 * [SR150421-027][20150811][ymjang] PLM system �������� - Manual ��ȸ ������� �߰�
 * [NON-SR][20150825][ymjang] ���� �ٿ��ֱ� ���� Folder �߰� ���� ����
 */
public class CreateDocOperation extends AbstractAIFOperation {

	private Registry registry;
	private TCSession session;
	private HashMap<String, Object> dataMap;

	public CreateDocOperation(HashMap<String, Object> dataMap) {
		this.registry = Registry.getRegistry(this);
		this.session = (TCSession) AIFUtility.getDefaultSession();
		this.dataMap = dataMap;
	}

	@Override
	public void executeOperation() throws Exception {

		Markpoint markpoint = null;

		try {
			markpoint = new Markpoint(session);

			// Item ����
			TCComponentItem item = createItem();
			TCComponentItemRevision itemRevision = null;
			if (item != null) {
				itemRevision = item.getLatestItemRevision();
				createDataset(itemRevision);
			}
			
			// FAQ 
			// [20150825][ymjang] ���� �ٿ��ֱ� ���� Folder �߰� ���� ����
//			TCComponentFolder[] folders = CustomUtil.findFolder("FAQDoc", "General Folder", "infodba");
//			folders[0].add("contents", new TCComponent[] { item });
			
			markpoint.forget();
		} catch (Exception e) {
			markpoint.rollBack();

			throw e;
		} 
	}

	public TCComponentItem createItem() throws Exception {
		TCComponentItem item = null;

		// ���� ������ id
		String doc_id = (String) dataMap.get("doc_id");

		// ���� ������ ������ id
		String doc_rev_id = (String) dataMap.get("doc_rev_id");

		// Item type
		String doc_type = (String) dataMap.get("doc_type");

		// Item Name
		String doc_name = (String) dataMap.get("doc_name");

		// Description
		String description = (String) dataMap.get("file_location");

		item = CustomUtil.createItem(doc_type, doc_id, doc_rev_id, doc_name, description);
		
		return item;
	}

	public void createDataset(TCComponentItemRevision itemRevision) throws Exception {
		
		// ÷������
		String doc_name = (String) dataMap.get("doc_name");
		String file_location = (String) dataMap.get("file_location");
		if (!file_location.isEmpty()) {
			TCComponentDataset dataset = CustomUtil.createDataset(file_location, doc_name);
			itemRevision.add("IMAN_specification", dataset);
		}
	}

}
