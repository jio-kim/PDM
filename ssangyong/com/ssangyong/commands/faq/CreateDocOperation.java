package com.ssangyong.commands.faq;

import java.util.HashMap;

import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

/**
 * [SR150421-027][20150811][ymjang] PLM system 개선사항 - Manual 조회 관리기능 추가
 * [NON-SR][20150825][ymjang] 폴더 붙여넣기 오류 Folder 추가 로직 삭제
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

			// Item 생성
			TCComponentItem item = createItem();
			TCComponentItemRevision itemRevision = null;
			if (item != null) {
				itemRevision = item.getLatestItemRevision();
				createDataset(itemRevision);
			}
			
			// FAQ 
			// [20150825][ymjang] 폴더 붙여넣기 오류 Folder 추가 로직 삭제
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

		// 문서 아이템 id
		String doc_id = (String) dataMap.get("doc_id");

		// 문서 아이템 리비전 id
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
		
		// 첨부파일
		String doc_name = (String) dataMap.get("doc_name");
		String file_location = (String) dataMap.get("file_location");
		if (!file_location.isEmpty()) {
			TCComponentDataset dataset = CustomUtil.createDataset(file_location, doc_name);
			itemRevision.add("IMAN_specification", dataset);
		}
	}

}
