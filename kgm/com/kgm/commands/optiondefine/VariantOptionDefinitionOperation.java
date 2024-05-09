package com.kgm.commands.optiondefine;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.DatasetService;
import com.kgm.common.utils.variant.VariantValue;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentFormType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.pse.variants.modularvariants.CustomMVPanel;
import com.teamcenter.rac.pse.variants.modularvariants.MVLLexer;
import com.teamcenter.rac.pse.variants.modularvariants.OVEOption;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Utilities;

/**
 * 카테고리에 옵션 값을 추가, 삭제 또는 옵션 설명을 수정한다.
 * 
 * @author slobbie
 *
 */
public class VariantOptionDefinitionOperation extends AbstractAIFOperation {
	
	public static final int CREATE_OPTION = 100;
	public static final int UPDATE_OPTION = 200;
	private TCComponentBOMLine targetLine = null;
	private String optionName = null;
	private String optionDesc = null;
	private File selectedFile = null;
	private DefaultTableModel model = null;
	private int mode = -1;
	
	
	public VariantOptionDefinitionOperation(TCComponentBOMLine targetLine, File selectedFile, 
			String optionName, String optionDesc, DefaultTableModel model, int mode){
		this.targetLine = targetLine;
		this.selectedFile = selectedFile;
		this.optionName = optionName;
		this.optionDesc = optionDesc;
		this.model = model;
		this.mode = mode;
	}
	
	@SuppressWarnings({ "rawtypes", "unused" })
    @Override
	public void executeOperation() throws Exception {
		
		// Mode == CREATE_OPTION 이면 새로운 옵션을 추가
		// Mode == UPDATE_OPTION 이면 옵션 수정.
		String valueNameAll = "";
		OVEOption oveOption = null;
		for( int i = 0; i < model.getRowCount(); i++){
			VariantValue value = (VariantValue)model.getValueAt(i, 0);
			if( oveOption == null){
				if( value.getOption() != null)
					oveOption = value.getOption().getOveOption();
			}
			
			String valueName = (String)model.getValueAt(i, 1);
			String valueDesc = (String)model.getValueAt(i, 2);
			
			//내부적으로 Merge 쿼리를 이용하므로 insert 또는 Update를 함.
			HashMap valueDescMap = new HashMap();
			
			SYMCRemoteUtil remote = new SYMCRemoteUtil();
			try{
				DataSet ds = new DataSet();
				ds.put("code_name", valueName);
				ds.put("code_desc", valueDesc);
				remote.execute("com.kgm.service.VariantService", "insertVariantValueDesc", ds);
				
			}catch( Exception e){
				//Desc 입력 안된건 무시.
				e.printStackTrace();
			}
			valueNameAll += MVLLexer.mvlQuoteString(valueName) + ( i + 1 == model.getRowCount() ? "":", ") ;
		}
		
		String name = MVLLexer.mvlQuoteId(optionName, false);
		String desc = optionDesc;
        if(desc.length() > 0)
        	desc = (new StringBuilder()).append(" ").append(MVLLexer.mvlQuoteString(desc)).append(" ").toString();
        else
        	desc = " ";
		String s = "public ";
		s += name;

		try {
			s += " string " + desc + " = " + valueNameAll;
			TCVariantService tcvariantservice = targetLine.getSession().getVariantService();
			if( mode == CREATE_OPTION){
				tcvariantservice.lineDefineOption(targetLine, s);
			}else if( mode == UPDATE_OPTION){
				CustomMVPanel.changeOption(tcvariantservice, targetLine, oveOption, s);
			}
			targetLine.window().save();
			targetLine.refresh();
			
			if( selectedFile != null){
//				Option Request Based
				TCComponentForm form = null;
				TCComponentDataset[] dataSets = null;
				TCComponent[] com = CustomUtil.queryComponent("General...", new String[]{"Type", "Name"}, new String[]{"Option Request Based", optionName});
				
				//기존에 생성한 Form이 존재 할경우에는 추가함.
				if( com != null && com.length > 0){
					
					Vector<TCComponentDataset> vec = new Vector<TCComponentDataset>();
					form = (TCComponentForm)com[0];
					AIFComponentContext[] contexts = form.getRelated("s7_CONTENTS");
					for( int i = 0; contexts != null && i < contexts.length; i++){
						AIFComponentContext context = contexts[i];
						vec.add((TCComponentDataset)context.getComponent());
					}
					dataSets = vec.toArray(new TCComponentDataset[vec.size()]);
					DatasetService.createService(targetLine.getSession());
					TCComponentDataset dataSet = DatasetService.createDataset(selectedFile.getPath());
					TCComponentDataset[] ds = new TCComponentDataset[vec.size() + 1];
					System.arraycopy(dataSets, 0, ds, 0, dataSets.length);
					ds[dataSets.length] = dataSet;
					form.setRelated("s7_CONTENTS", ds);
					
				}else{//새로 생성해야 하는 경우
					createForm(selectedFile, optionName);
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
			MessageBox.post(Utilities.getCurrentWindow(), e.getDetailsMessage(), "INFORMATION", MessageBox.WARNING);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 옵션명과 동일하게 Form을 생성하고 파일을 Dataset에 셋.
	 * 
	 * @param optionName
	 * @throws Exception
	 */
	private void createForm(File file, String optionName) throws Exception{
		TCSession session = targetLine.getSession();
		DatasetService.createService(session);
		TCComponentDataset dataSet = DatasetService.createDataset(file.getPath());
		TCComponentFormType typeComponent = (TCComponentFormType)session.getTypeComponent("Form");
//		"Option Request Based"
		TCComponentForm form = typeComponent.create(optionName, "", "S7_OptionReqBased");
		form.setRelated("s7_CONTENTS", new TCComponent[]{ dataSet });
	}
}
