package com.symc.plm.rac.prebom.masterlist.operation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.utils.SYMTcUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.common.util.BomUtil;
import com.symc.plm.rac.prebom.masterlist.model.MasterListDataMapper;
import com.symc.plm.rac.prebom.masterlist.model.StoredOptionSet;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;

public class BOMLoadWithDateOperation extends AbstractAIFOperation {

	private TCComponentBOMLine fmpLine = null;
//	private OSpec ospec = null;
	private ArrayList<String> essentialNames = null;
	private HashMap<String, StoredOptionSet> storedOptionSetMap = null;
	private Date date = null;
	private WaitProgressBar waitBar = null;
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private HashMap<String, Object> data = new HashMap();
	
	public static String DATA_STORED_OPTION_SET = "STORED_OPTION_SET";
	public static String DATA_ERROR = "ERROR";
	public static String DATA_MAPPER = "DATA_MAPPER";
	public static String DATA_CHILD_ROW_KEY = "DATA_CHILD_ROW_KEY";
	public static String DATA_DATE = "DATE";
	public static String DATA_OSPEC = "OSPEC";
	
	
	public BOMLoadWithDateOperation(TCComponentBOMLine fmpLine, 
			ArrayList<String> essentialNames, 
			Date date, WaitProgressBar waitBar){
		this.fmpLine = fmpLine;
		this.essentialNames = essentialNames;
		this.date = date;
		this.waitBar = waitBar;
	}
	
	private void setMsg(String msg){
		if( waitBar != null){
			waitBar.setStatus(msg);
		}
	}
	@Override
	public void executeOperation() throws Exception {
		storeOperationResult(data);
		
		setMsg("Searching Pre-Product...");
		TCComponentItemRevision preProductRevision = (TCComponentItemRevision)BomUtil.getParent(fmpLine.getItemRevision(), TypeConstant.S7_PREPRODUCTREVISIONTYPE);
		if( preProductRevision == null){
			data.put(DATA_ERROR, new Exception("Could not find PreProduct."));
			return;
		}
		String product_project_code = preProductRevision.getStringProperty("s7_PROJECT_CODE");
		
		preProductRevision = BomUtil.getItemRevisionOnReleaseDate(preProductRevision.getItem(), date);
		if( preProductRevision == null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			data.put(DATA_ERROR, new Exception("Could not found the Pre-Product[" + sdf.format(date) + "]"));
			return;
		}
		String ospecNo = preProductRevision.getProperty(PropertyConstant.ATTR_NAME_OSPECNO);
		if( ospecNo == null || ospecNo.equals("")){
			data.put(DATA_ERROR, new Exception("Could not found OSpec."));
			return;
		}
		
		TCSession session = fmpLine.getSession();
		int t = ospecNo.lastIndexOf("-");
		String ospecId = ospecNo.substring(0, t);
		String ver = ospecNo.substring(t + 1);
		TCComponentItemRevision ospecRevision = SYMTcUtil.findItemRevision(session, ospecId, ver);
		OSpec ospec = BomUtil.getOSpec(ospecRevision);
		data.put(DATA_OSPEC, ospec);
		
//		storedOptionSetMap = new HashMap();
		storedOptionSetMap = BomUtil.getOptionSet(ospec);
//		setOptionSet(preProductRevision, storedOptionSetMap);
		data.put(DATA_STORED_OPTION_SET, storedOptionSetMap);
		data.put(DATA_DATE, date);
		
		//Release±‚¡ÿ¿« BOM Load
//		TCComponentBOMWindowType windowType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
        TCComponentBOMWindow bomWindow = null;
//        TCComponentItemRevision latestReleasedRevision = null;
        try{
        	TCComponentBOMLine topLine = BomUtil.getBomLine(fmpLine.getItem(), date);
        	if( topLine != null){
        		bomWindow = topLine.window();
        	}else{
        		throw new Exception("Could not found Released Item Revision.");
        	}
        	setMsg("Loading Released BOM Info.");
        	MasterListDataMapper releaseDataMapper = new MasterListDataMapper(topLine, ospec, essentialNames, false);
            BOMLoadOperation.loadChildMap(releaseDataMapper, topLine, storedOptionSetMap, product_project_code);
        	
            data.put(DATA_MAPPER, releaseDataMapper);
            
        }catch(Exception e){
        	data.put(DATA_ERROR, e);
        }finally{
        	if( bomWindow != null){
        		bomWindow.close();
        	}
        }
	}

}
