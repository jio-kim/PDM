package com.kgm.commands.downdataset;

import java.io.File;
import java.util.Date;

import javax.swing.DefaultListModel;

import com.kgm.common.attachfile.AttachFileComponent;
import com.kgm.common.operation.SYMCAWTAbstractCreateOperation;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class DownDataSetOperation extends SYMCAWTAbstractCreateOperation {

	private String path;
	private TCSession session = CustomUtil.getTCSession();
	private String login_user;
	private Date creation_date;
	private String uid;
	private String dataset_name;
	private String item_id;

	/**
	 * ������.
	 * 
	 * @copyright : S-PALM
	 * @author : �ǻ��
	 * @since : 2013. 1. 8.
	 * @param dialog
	 */
	public DownDataSetOperation(AbstractAIFDialog dialog, String path) {
		super(dialog);
		this.path = path;

		login_user = session.getUser().toDisplayString();
	}

	@SuppressWarnings("rawtypes")
    @Override
	public void createItem() throws Exception {
		DownDataSetInfoPanel infoPanel = ((DownDataSetDialog) dialog).getInfoPanel();
		DefaultListModel model = (DefaultListModel) infoPanel.getAttachFilePanel().getDropList().getModel();
		int modelSize = model.getSize();
		AttachFileComponent attachFileComp = null;
		boolean flag = false;
		for (int i = 0; i < modelSize; i++) {
			attachFileComp = (AttachFileComponent) model.getElementAt(i);
			AIFComponentContext aifContext = null;
			
			if (attachFileComp.getAttachObject() instanceof AIFComponentContext) {
				aifContext = (AIFComponentContext)attachFileComp.getAttachObject();
			}
			
			if (attachFileComp.isAIFComponentContext()) {
				try {
					TCComponentDataset dataset = (TCComponentDataset) ((AIFComponentContext) attachFileComp
							.getAttachObject()).getComponent();
					creation_date = dataset.getDateProperty("creation_date");
					uid = dataset.getUid();
					item_id = dataset.getProperty("object_name");
					// [20131213]
					// dataset�� ���� file�� ��ϵǾ� ���� �� �ְ�, �� ������ ���� ����� �޶��� �� �����Ƿ� dataset type�� ���� filter��� �߰�.
					// SYMTcUtil.getImanFile() �� ECO B�� download ��� �� ������ �κ�.
					//TCComponentTcFile[] imanFile = dataset.getTcFiles();
                    TCComponentTcFile[] imanFile = SYMTcUtil.getImanFile(dataset);
					File[] file = null;
					// imanfile�� ���� ��� �߰� check 
					//if (imanFile.length > 0) {
					if (imanFile != null && imanFile.length > 0) {
						file = dataset.getFiles(CustomUtil.getNamedRefType(dataset, imanFile[0]), path);
						
						// [SR150415-006][2015.04.17][jclee] 2D������ ���ϸ� �ڿ� Dataset Revision�� �ش� Part Revision�� ECO No�� �ٿ��� Rename
						String sECONo = "";
						for (int inx = 0; inx < file.length; inx++) {
							String sFileName = "";
							String sFileExt = "";
							int iDot = -1;
							
							iDot = file[inx].getName().indexOf('.');
							
							sFileName = file[inx].getName().substring(0, iDot);
							sFileExt = file[inx].getName().substring(iDot + 1, file[inx].getName().length());
							
							if (sFileName != null && sFileExt != null) {
								if (sFileExt.toUpperCase().equals("CATDRAWING") || sFileExt.toUpperCase().equals("PDF")) {
									InterfaceAIFComponent parentComponent = aifContext.getParentComponent();
									sECONo = ((TCComponentDataset)aifContext.getComponent()).getProperty("s7_ECO_NO");
//									if (parentComponent instanceof TCComponent) {
//										sECONo = ((TCComponent) parentComponent).getReferenceProperty("s7_ECO_NO").getProperty("item_id");
//									}
									
									// [SR151204-017][20151209][jclee] 2D Dataset Download �� File Name�� Part Name �߰�
									String sPartName = "";
									// 1. Dataset�� Where Referenced�� ��ȸ�Ͽ� ����� ItemRevision�� �����´�.
									AIFComponentContext[] whereReferenced = dataset.whereReferenced();
									if (whereReferenced.length > 0) {
										for (int jnx = 0; jnx < whereReferenced.length; jnx++) {
											InterfaceAIFComponent component = whereReferenced[jnx].getComponent();
											if (component instanceof TCComponentItemRevision) {
												TCComponentItemRevision revision = (TCComponentItemRevision)component;
												sPartName = revision.getProperty("object_name");
												break;
											}
										}
									}
									
									if (sPartName == null || sPartName.equals("") || sPartName.length() == 0) {
										MessageBox.post(dialog, "DataSet Down �� ���� �߻�.", "����", MessageBox.ERROR);
									}
									
									sFileName = item_id.replace('/', '_') + "_" + sPartName + (sECONo == null || sECONo.equals("") ? "" : "_") + sECONo;
									file[inx].renameTo(new File(this.path + "\\" + sFileName + "." + sFileExt));
								}
							}
						}
						
						dataset_name = file[0].getName();
					}

					/** DownLoad Log Create */
					int ii = downDataSetlogInsert();
					if (ii != -1) {
						flag = true;
					} else {
						flag = false;
					}
				} catch (Exception ex) {
					MessageBox.post(ex);
					return;
				}
			}
		}
		
		if(flag){
			MessageBox.post(dialog, "Dwonload DataSet Log �Է� �Ϸ�.", "�˸�", MessageBox.INFORMATION);
		}else{
			MessageBox.post(dialog, "DataSet Down Log �Է� �� ���� �߻�.", "����", MessageBox.ERROR);
		}

		/** List Clear */
		model.removeAllElements();
		infoPanel.getAttachFilePanel().getDropList().updateUI();
	}

	@Override
	public void endOperation() throws Exception {
	}

	/**
	 * DownLoad Log Create.
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǻ��
	 * @since : 2013. 1. 8.
	 * @param file
	 */
	private int downDataSetlogInsert() {
		
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("item_id", item_id);
		ds.put("login_user", login_user);
		ds.put("creation_date", creation_date);
		ds.put("dataset_uid", uid);
		ds.put("dataset_name", dataset_name);
		ds.put("down_path", path);
		
		int ii = -1;
		try {
			
			// [20131216] Integer downDataSetlogInsert(DataSet ds) �κ� �ݿ�. ArrayList�� �ƴϰ� Integer��. 
			ii = (Integer)remote.execute("com.kgm.service.DownDataSetService", "downDataSetlogInsert", ds);
			
//			ArrayList list = (ArrayList)remote.execute("com.kgm.service.DownDataSetService", "downDataSetlogInsert", ds);
//
//			if (list == null) {
//				return ii;
//			}
//
//			ii = (Integer) list.get(0);
			System.out.println("flag : " + ii);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ii;
	}

	@Override
	public void setProperties() throws Exception {
	}

	@Override
	public void startOperation() throws Exception {
	}

}
