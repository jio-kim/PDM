package com.ssangyong.commands.migration;

import java.io.File;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.ssangyong.common.OperationAbortedListener;
import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.DatasetService;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class ProductMigrationOperation extends AbstractAIFOperation implements OperationAbortedListener {
	private TCSession session;
	private Registry registry;
	private DefaultTableModel model;
	private boolean goOnError, aborted;
	private TCComponentItem item;
	private int line;
	private JProgressBar progressBar;
	private MigrationDialog dialog;

	public ProductMigrationOperation(MigrationDialog dialog, Registry registry, TCSession session, JTable table, boolean goOnError, JProgressBar progressBar) {
		this.session = session;
		this.registry = registry;
		this.progressBar = progressBar;
		this.model = (DefaultTableModel)table.getModel();
		this.goOnError = goOnError;
		this.dialog = dialog;
		setStartMessage("업로드 시작...");
	}

	public void executeOperation() {
		
		for (int i = 0; i < model.getRowCount(); i++) {
			if (aborted) {
				return;
			}
			try {
				line = i;
				String name = (String)model.getValueAt(i, 3);
				String id = (String)model.getValueAt(i, 0);
				String desc = (String)model.getValueAt(i, 34);
				
				progressBar.setString(id + " 생성 중...");
				progressBar.setValue(i + 1);
				
				item = CustomUtil.createItem(SYMCClass.ITEM_TYPE, id, SYMCClass.ITEM_REV_ID, name, desc);
				setProperties(i, item.getLatestItemRevision());
				attachTemplate(i, item.getLatestItemRevision());
				model.setValueAt(new String("OK"), line, model.getColumnCount() - 2);
			} catch (Exception e) {
				e.printStackTrace();
				model.setValueAt(new String("Fail"), line, model.getColumnCount() - 2);
				model.setValueAt(e.getMessage(), line, model.getColumnCount() - 1);
				if (!goOnError) {
					return;
				} else {
					continue;
				}
			}
		}
		MessageBox.post(dialog, "업로드가 완료 되었습니다. 결과를 확인 하세요.", "알림", MessageBox.INFORMATION);
	}

	private void setProperties(int i, TCComponentItemRevision revision) throws Exception {
		//		PK4_ProductRevisionAttribute.ARRAY=pk4_plm_code,object_name,object_desc,pk4_type,pk4_form,pk4_hoching_meter,pk4_hoching_inch,
		//	    pk4_hoching_etc,pk4_pitch_meter,pk4_pitch_inch,pk4_pitch_etc,pk4_material,pk4_stool,pk4_height,pk4_tap_type,pk4_processing_type,
		//	    pk4_heat_treatment,pk4_c_h,pk4_surface_treatment,pk4_surface_spec,pk4_dwg_meterial,pk4_dwg_classification,pk4_cut_design_weight,
		//	    23pk4_cut_steel_weight,pk4_nut_weight,pk4_product_design_weight,pk4_product_steel_weight,pk4_short_weight,pk4_dev_type,pk4_unit,
		//	    pk4_external_diameter,pk4_processing_sequence,pk4_customer,pk4_customer_no,pk4_real_user

		TCProperty[] p = revision.getTCProperties(registry.getStringArray("PK4_ProductRevisionAttribute.ARRAY"));
		p[0].setStringValueData((String)model.getValueAt(i, 1));
		p[3].setStringValueData((String)model.getValueAt(i, 2));
		p[4].setStringValueData((String)model.getValueAt(i, 4));
		p[5].setStringValueData((String)model.getValueAt(i, 5));
		p[6].setStringValueData((String)model.getValueAt(i, 7));
		p[7].setStringValueData((String)model.getValueAt(i, 9));
		p[8].setStringValueData((String)model.getValueAt(i, 6));
		p[9].setStringValueData((String)model.getValueAt(i, 8));
		p[10].setStringValueData((String)model.getValueAt(i, 10));
		p[11].setStringValueData((String)model.getValueAt(i, 11));
		p[12].setStringValueData((String)model.getValueAt(i, 12));
		p[13].setStringValueData((String)model.getValueAt(i, 13));
		p[14].setStringValueData((String)model.getValueAt(i, 14));
		p[15].setStringValueData((String)model.getValueAt(i, 15));
		p[16].setStringValueData((String)model.getValueAt(i, 16));
		p[17].setStringValueData((String)model.getValueAt(i, 17));
		p[18].setStringValueData((String)model.getValueAt(i, 18));
		p[19].setStringValueData((String)model.getValueAt(i, 19));
		p[20].setStringValueData((String)model.getValueAt(i, 20));
		p[21].setStringValueData((String)model.getValueAt(i, 21));
		p[22].setStringValueData((String)model.getValueAt(i, 22));
		p[23].setStringValueData((String)model.getValueAt(i, 24));
		p[24].setStringValueData((String)model.getValueAt(i, 26));
		p[25].setStringValueData((String)model.getValueAt(i, 23));
		p[26].setStringValueData((String)model.getValueAt(i, 25));
		p[27].setStringValueData((String)model.getValueAt(i, 27));
		p[28].setStringValueData((String)model.getValueAt(i, 28));
		p[29].setStringValueData((String)model.getValueAt(i, 29));
		p[30].setStringValueData((String)model.getValueAt(i, 30));
		p[31].setStringValueData((String)model.getValueAt(i, 31));
		p[32].setStringValueData((String)model.getValueAt(i, 32));
		p[33].setStringValueData((String)model.getValueAt(i, 33));
		p[34].setStringValueData((String)model.getValueAt(i, 36));

		revision.setTCProperties(p);
		revision.refresh();

		// My Teamcenter트리에서 디폴트 디스플레이는 object_string 인데,
		// 디스플레이 속성을 pk4_product_code 로 변경 함
		item.setProperty("pk4_product_code", (String)model.getValueAt(i, 1));
		item.refresh();
	}

	private void attachTemplate(int i, TCComponentItemRevision revision) throws Exception {
		String path = registry.getString("MigrationRootDirectory");
		String name = ((String)model.getValueAt(i, 35)).trim();
		if (name.length() > 0) {
			DatasetService.createService(session);
			File file = new File(path + java.io.File.separator + name);
			DatasetService.createDataset(revision, SYMCClass.RELATED_DWG_REL, file.getAbsolutePath());
		}
	}

	public void operationAborted() {
		aborted = true;
	}

	public boolean checkValidate() {
		return true;
	}
}
