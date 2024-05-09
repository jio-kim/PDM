package com.kgm.commands.ec.fncepl.operation;

import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.commands.ec.fncepl.model.FncEplCheckData;
import com.kgm.common.remote.DataSet;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.DatasetService;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

/**
 * Function EPL Check ����Ʈ Row ����
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
			 * ÷������ ó��
			 */
			TCComponentDataset attachDataSetComp = createDataset();
			String newAttachFilePuid = attachDataSetComp != null ? attachDataSetComp.getUid() : null;
			String savedAttachFilePuid = selectedRowData.getAttachFilePuid() != null && !selectedRowData.getAttachFilePuid().isEmpty() ? selectedRowData
					.getAttachFilePuid() : null;
			// ����� ÷�������� ���� ���
			if (newAttachFilePuid == null) {
				boolean isAttachFileDelete = inputData.isAttachFileDelete();
				// ÷�� ���� ��ư�� ������ ���
				if (isAttachFileDelete) {
					// ÷������ DataSet ����
					deleteDataSetComp(savedAttachFilePuid);
				} else
					newAttachFilePuid = savedAttachFilePuid;
			}

			/**
			 * DB Table Row ���� Update
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
			 * ���õ� Row Data ������Ʈ
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
	 * DatatSet ����
	 * 
	 * @param dataSetPuid
	 * @throws Exception
	 */
	private void deleteDataSetComp(String dataSetPuid) throws Exception {
		if (dataSetPuid == null || dataSetPuid.isEmpty())
			return;
		// ���� ����
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
