package com.ssangyong.commands.ec.fncepl.operation;

import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.commands.ec.fncepl.model.FncEplCheckData;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.DatasetService;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

/**
 * Function EPL Check 리스트 Row 수정
 * 
 * @author baek
 * 
 */
public class FncEplCheckModifyOperation extends AbstractAIFOperation {
	private FncEplCheckData inputData = null;
	private FncEplCheckData selectedRowData = null;
	private TCSession tcSession = null;

	public FncEplCheckModifyOperation(FncEplCheckData inputData, FncEplCheckData selectedRowData) {
		this.inputData = inputData;
		this.selectedRowData = selectedRowData;
		this.tcSession = CustomUtil.getTCSession();
	}

	@Override
	public void executeOperation() throws Exception {
		try {

			/**
			 * 첨부파일 처리
			 */
			TCComponentDataset attachDataSetComp = createDataset();
			String newAttachFilePuid = attachDataSetComp != null ? attachDataSetComp.getUid() : null;
			String savedAttachFilePuid = selectedRowData.getAttachFilePuid() != null && !selectedRowData.getAttachFilePuid().isEmpty() ? selectedRowData
					.getAttachFilePuid() : null;
			// 변경된 첨부파일이 없을 경우
			if (newAttachFilePuid == null) {
				boolean isAttachFileDelete = inputData.isAttachFileDelete();
				// 첨부 삭제 버튼을 눌렀을 경우
				if (isAttachFileDelete) {
					// 첨부파일 DataSet 삭제
					deleteDataSetComp(savedAttachFilePuid);
				} else
					newAttachFilePuid = savedAttachFilePuid;
			}

			/**
			 * DB Table Row 정보 Update
			 */
			String prodName = inputData.getProdNo();
			String prodNo = prodName.substring(0, prodName.indexOf(" ("));
			String prodDispName = prodName.substring(prodName.indexOf(" (") + 2, prodName.length() - 1);

			DataSet ds = new DataSet();
			String fncEplPuid = selectedRowData.getFncEplPuid();
			ds.put("PUID", fncEplPuid);
			ds.put("FUNCTION_NO", inputData.getFunctionNo());
			ds.put("PROD_NO", prodNo);
			ds.put("PROD_NAME", prodDispName);
			ds.put("ECO_TYPE", inputData.getEcoType());
			ds.put("APPLY_ECO_NO", inputData.getApplyEcoNo());
			ds.put("ADD_ECO_PUBLISH", inputData.getAddEcoPublish());
			ds.put("DESCRIPTION", inputData.getDescription());
			ds.put("ATTACH_FILE_PUID", newAttachFilePuid);
			ds.put("BASE_DATE", inputData.getBaseDate());
			
			CustomECODao dao = new CustomECODao();
			dao.updateFncEplCheck(ds);
			
			/**
			 * 선택된 Row Data 업데이트
			 */
			selectedRowData.setFunctionNo(inputData.getFunctionNo().concat(prodNo));
			selectedRowData.setProdNo(prodNo);
			selectedRowData.setEcoType(inputData.getEcoType());
			selectedRowData.setApplyEcoNo(inputData.getApplyEcoNo());
			selectedRowData.setAddEcoPublish(inputData.getAddEcoPublish());
			selectedRowData.setDescription(inputData.getDescription());
			selectedRowData.setAttachFilePuid(newAttachFilePuid);
			selectedRowData.setBaseDate(inputData.getBaseDate());
			selectedRowData.setProdDspName(prodName);

			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Data was saved Successfully", "Information", MessageBox.INFORMATION);
		} catch (Exception ex) {
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), ex.toString(), "Error", MessageBox.ERROR);
		}
	}

	private TCComponentDataset createDataset() throws Exception {
		String attachFilePath = inputData.getAttachFilePath();
		if (attachFilePath == null)
			return null;
		DatasetService.createService(CustomUtil.getTCSession());
		TCComponentDataset dataSet = DatasetService.createDataset(attachFilePath);
		return dataSet;
	}

	/**
	 * DatatSet 삭제
	 * 
	 * @param dataSetPuid
	 * @throws Exception
	 */
	private void deleteDataSetComp(String dataSetPuid) throws Exception {
		if (dataSetPuid == null || dataSetPuid.isEmpty())
			return;
		// 파일 삭제
		TCComponent attachComp = tcSession.stringToComponent(dataSetPuid);
		if (attachComp == null) {
			return;
		}
		TCAccessControlService acService = tcSession.getTCAccessControlService();
		boolean isDelete = acService.checkPrivilege(attachComp, TCAccessControlService.DELETE);
		if (!isDelete)
			return;
		attachComp.delete();
	}

}
