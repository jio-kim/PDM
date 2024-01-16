package com.ssangyong.commands.bomviewer;

import java.util.ArrayList;
import java.util.HashMap;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;

public class BOMViewerOperation extends AbstractAIFOperation {
	private String sPUID = "";
	
	public BOMViewerOperation(TCComponent cParent) {
		if (cParent instanceof TCComponentItemRevision) {
			TCComponentItemRevision tirParent = (TCComponentItemRevision)cParent;
			this.sPUID = tirParent.getUid();
		}
	}

	@Override
	public void executeOperation() throws Exception {
		BOMViewerDao dao = new BOMViewerDao();
		ArrayList<HashMap<String, String>> result = dao.selectBOMViewer(sPUID);
		
		storeOperationResult(result);
	}
}
