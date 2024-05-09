package com.symc.plm.rac.prebom.optionedit.operation;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.kgm.common.WaitProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;
import com.kgm.common.utils.variant.VariantErrorCheck;
import com.kgm.common.utils.variant.VariantOption;
import com.kgm.common.utils.variant.VariantValue;
import com.symc.plm.rac.prebom.common.util.OptionManager;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.pse.variants.modularvariants.ConditionElement;
import com.teamcenter.rac.pse.variants.modularvariants.ConstraintsModel;
import com.teamcenter.rac.pse.variants.modularvariants.MVLLexer;
import com.teamcenter.rac.pse.variants.modularvariants.OptionConstraint;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * ���� ������ �ɼ� ������ BOMLine(ItemRevision)�� �����Ѵ�.
 * ������ Item Type�� Product Type�� ��쿡 Ư�� �ɼ��� Unuse(Uncheck) ���·� �����ϸ�, 
 * ���� Variant �Ǵ� Function������ �ش�  �ɼ��� Unuse���·� �����Ѵ�.
 * 
 * @author slobbie
 *
 */
@SuppressWarnings({"unused", "rawtypes", "unchecked"})
public class PreFmpOptionSetOperation extends AbstractAIFOperation {

//    private ArrayList<VariantOption> globalOptionSet = null;
	private ArrayList<VariantOption> enableOptionSet= null;
	private ArrayList<VariantOption> selectedLineOptionSet = null;
	private TCComponentBOMLine selectedLine = null;
	private Vector<String[]> userDefineErrorList = null;
	private Vector<String[]> moduleConstraintList = null;
	private OptionManager manager = null;
	private ArrayList<VariantOption> optionSetToDelete = null;
	private Vector<Vector> allData = null;
	private WaitProgressBar waitProgress = null;
	
	private JTable moduleConstraintTable = null;
	private JTable userDefineErrorTable = null;
	
	private int operationType = -1;
	public static final int OPTION_SAVE = 100;
	public static final int USER_DEFINE_ERROR_DELETE = 200;
	public static final int MODULE_CONSTRAINT_DELETE = 300;
	private JDialog parent = null;
	
	public PreFmpOptionSetOperation(ArrayList<VariantOption> enableOptionSet, 
			ArrayList<VariantOption> selectedLineOptionSet, TCComponentBOMLine selectedLine, 
			Vector<String[]> userDefineErrorList,Vector<String[]> moduleConstraintList, OptionManager manager, ArrayList<VariantOption> optionSetToDelete,
			Vector<Vector> allData, JDialog parent, WaitProgressBar waitProgress){
		this.enableOptionSet = enableOptionSet;
		this.selectedLineOptionSet = selectedLineOptionSet;
		this.selectedLine = selectedLine;
		this.userDefineErrorList = userDefineErrorList;
		this.moduleConstraintList = moduleConstraintList;
		this.manager = manager;
		this.optionSetToDelete = optionSetToDelete;
		this.allData = allData;
		this.parent = parent;
		this.waitProgress = waitProgress;
		this.operationType = OPTION_SAVE;
	}
	
	public PreFmpOptionSetOperation(TCComponentBOMLine selectedLine, 
			Vector<String[]> userDefineErrorList,Vector<String[]> moduleConstraintList, OptionManager manager,JTable moduleConstraintTable,
			WaitProgressBar waitProgress, int operationType){
		this.selectedLine = selectedLine;
		this.userDefineErrorList = userDefineErrorList;
		this.moduleConstraintList = moduleConstraintList;
		this.manager = manager;
		this.waitProgress = waitProgress;
		this.operationType = operationType;
		if( operationType == USER_DEFINE_ERROR_DELETE){
			this.userDefineErrorTable = moduleConstraintTable;
		}else if( operationType == MODULE_CONSTRAINT_DELETE){
			this.moduleConstraintTable = moduleConstraintTable;
		}
	}
	
	@Override
	public void executeOperation() throws Exception {
		try{
			if( operationType == OPTION_SAVE){
				optionApply();
				MessageBox.post(AIFUtility.getActiveDesktop(), "���������� ���� �Ǿ����ϴ�.", "INFORMATION", MessageBox.WARNING);
			}else if( operationType == USER_DEFINE_ERROR_DELETE ){
				deleteUserDefineError();
			}else if( operationType == MODULE_CONSTRAINT_DELETE ){
				deleteModuleConstraint();
			}
			
			if( waitProgress != null){
				waitProgress.dispose();
			}
			
			if( parent != null){
				parent.dispose();
			}
		}catch( Exception e){
			e.printStackTrace();
			waitProgress.setShowButton(true);
			throw e;
			//waitProgress.close("Fail", false);
		}
		
	}

	/**
	 * Dialog���� ������ �ɼ��� ������ ����.
	 * 
	 * @throws TCException
	 */
	private void optionApply() throws TCException{
		
		waitProgress.setStatus("Option updating...", true);
		apply(allData, userDefineErrorList, selectedLine);
		
		//Product Ÿ���� Product�� ���°��� checked ==> unChecked ���·� �ٲ� ���, 
		//������ Variant, FunctionŸ����
		//������� Value�� ���Ͽ�, ��� unUsed�� �����Ѵ�.
//		String type = selectedLine.getItem().getType();
//		if( type.equals(TcDefinition.PRODUCT_ITEM_TYPE)){
//			
//			waitProgress.setStatus("applying to children(VARIANT, FUNCTION)...", true);
//			Vector<VariantValue> tmpList = new Vector();
//			int size = VariantCheckBoxTableCellEditor.unUsedValueList.size();
//			System.out.println("unUsedValueList size = " + size);
//			for( int i = 0; i < VariantCheckBoxTableCellEditor.unUsedValueList.size(); i++){
//				VariantValue value = VariantCheckBoxTableCellEditor.unUsedValueList.get(i);
//				if( value.isChanged()){
//					tmpList.add(value);
//				}
//			}
//			
////			SRME:: [][20140812] swyoon  Prouct, Variant, Function�� �ɼ� ���� �ӵ� ����(�������� ��뿩�� üũ ����).			
////			if( !tmpList.isEmpty()){
////				manager.applyToChild(selectedLine, tmpList);
////			}
//			
//		}
			
	}
	
	/**
	 * ���ǵ� �ɼ�, ��ȿ���� BOM line�� �ݿ��ϰ� ������.
	 * @param allData
	 * @param userDefineErrorList
	 * @param selectedLine
	 * @throws TCException
	 */
	private void apply(Vector<Vector> allData, Vector<String[]> userDefineErrorList, TCComponentBOMLine selectedLine) throws TCException{
		
		
//		InterfaceAIFComponent[] targets = AIFUtility.getCurrentApplication().getTargetComponents();
//		if( targets[0] instanceof TCComponentBOMLine){
			try{
				
				//������ ���� �Ǿ� �ִ� �ɼ��� ������.
				ArrayList appliedOption = new ArrayList();
				for( VariantOption option :selectedLineOptionSet){
					appliedOption.add(option.getOptionName());
				}
				
				Registry registry = Registry.getRegistry("com.symc.plm.rac.prebom.optionedit.optionedit");
				
				HashMap<String, VariantErrorCheck> notUseErrorMap = new HashMap();
				HashMap<String, VariantErrorCheck> notDefineErrorMap = new HashMap();
				
				TCVariantService tcvariantservice = selectedLine.getSession().getVariantService();
				
				int curNum = 0;
				//������ �ɼ� �ڵ尪�� Ȯ���մϴ�.
				waitProgress.setStatus(registry.getString("variant.loadingOptionCode"), true);
				for(Vector row : allData){
					curNum++;
					VariantValue value = (VariantValue)row.get(0);
					VariantOption option = value.getOption();
					
					if( value.getValueStatus() == VariantValue.VALUE_USE ){
						
						if( !value.isNew() ){
							continue;
						}
						if( appliedOption.contains(option.getOptionName())){
							continue;
						}
						
						waitProgress.setStatus(StringUtil.getString(registry, "variant.addOption", new String[]{option.getOptionName()}), true);
						String s = OptionManager.getOptionString(option);
						
						try{
							tcvariantservice.lineDefineOption(selectedLine, s);
						}catch(TCException e){
							e.printStackTrace();
						}finally{
							appliedOption.add(option.getOptionName());
							selectedLine.refresh();
						}
						
						//üũ�ڽ��� ���� �� ���
					}else if(value.getValueStatus() == VariantValue.VALUE_NOT_USE){
						
						VariantErrorCheck notUseErrorcheck = notUseErrorMap.get(option.getOptionName());
						if( notUseErrorcheck == null){
							notUseErrorcheck = new VariantErrorCheck();
							notUseErrorcheck.type = "inform";
							notUseErrorcheck.message = VariantValue.TC_MESSAGE_NOT_USE;
						}
						
//						condition �߰�						
						ConditionElement condition = new ConditionElement();
						if( notUseErrorcheck.getConditionSize() == 0 ){
							condition.ifOrAnd = "if";
						}else{
							condition.ifOrAnd = "or";
						}
						condition.item = selectedLine.getItem().getProperty("item_id");
						condition.op = "=";
						condition.option = MVLLexer.mvlQuoteId(option.getOptionName(), false);
						condition.value = value.getValueName();
						condition.valueIsString = true;
						condition.fullName = condition.item + ":" + condition.option;
				        notUseErrorcheck.addCondition( condition );		
				        
				        notUseErrorMap.put(option.getOptionName(), notUseErrorcheck);
						//�ɼ� Ʈ������ �ɼ����̺�� �̵� ���� ���� ���� Value
					}else{
						
						VariantErrorCheck notDefineErrorcheck = notDefineErrorMap.get(option.getOptionName());
						if( notDefineErrorcheck == null){
							notDefineErrorcheck = new VariantErrorCheck();
							notDefineErrorcheck.type = "inform";
							notDefineErrorcheck.message = VariantValue.TC_MESSAGE_NOT_DEFINE;
						}
						
//						condition �߰�						
						ConditionElement condition = new ConditionElement();
						if( notDefineErrorcheck.getConditionSize() == 0 ){
							condition.ifOrAnd = "if";
						}else{
							condition.ifOrAnd = "or";
						}
						condition.item = selectedLine.getItem().getProperty("item_id");
						condition.op = "=";
						condition.option = MVLLexer.mvlQuoteId(option.getOptionName(), false);
						condition.value = value.getValueName();
						condition.valueIsString = true;
						condition.fullName = condition.item + ":" + condition.option;
						notDefineErrorcheck.addCondition( condition );
						
						notDefineErrorMap.put(option.getOptionName(), notDefineErrorcheck);
					}
				}
				
				selectedLine.window().save();
				selectedLine.refresh();
				
			    //������ �ִ� �ɼǰ��� ��� �� ���� �ش� �ɼ��� ���ŵ� 
		        ArrayList deletedOption = new ArrayList();
		        for( VariantOption option : optionSetToDelete){
		        	
		        	if( deletedOption.contains(option.getOptionName())) continue;
		        	
		        	VariantErrorCheck notDefineErrorcheck = new VariantErrorCheck();
					notDefineErrorcheck.type = "inform";
					notDefineErrorcheck.message = VariantValue.TC_MESSAGE_NOT_DEFINE;
		        	
		        	try{
		        		List<VariantValue> values = option.getValues();
		        		for( VariantValue value : values){
//							condition �߰�						
							ConditionElement condition = new ConditionElement();
							if( notDefineErrorcheck.getConditionSize() == 0 ){
								condition.ifOrAnd = "if";
							}else{
								condition.ifOrAnd = "or";
							}
							condition.item = selectedLine.getItem().getProperty("item_id");
							condition.op = "=";
							condition.option = MVLLexer.mvlQuoteId(option.getOptionName(), false);
							condition.value = value.getValueName();
							condition.valueIsString = true;
							condition.fullName = condition.item + ":" + condition.option;
							notDefineErrorcheck.addCondition( condition );
		        		}
		        		notDefineErrorMap.put(option.getOptionName(), notDefineErrorcheck);
		        		
		        	}catch(TCException tce){
		        		waitProgress.setStatus(StringUtil.getString(registry, "variant.canNotDeleteOption", new String[]{option.getOptionName()}), true);
		        		tce.printStackTrace();
		        		throw tce;
		        	}finally{
		        		deletedOption.add(option.getOptionName());
		        	}
		        }
		        //�����Ǿ�� �� �ɼǼ� �ʱ�ȭ.
		        optionSetToDelete.clear();
				
				StringBuilder sb = new StringBuilder();
				Set<String> set = notUseErrorMap.keySet();
				Iterator<String> its = set.iterator();
				while( its.hasNext()){
					String key = its.next();
					VariantErrorCheck notUseErrorcheck = notUseErrorMap.get(key);
					String msg = VariantValue.TC_MESSAGE_NOT_USE;
					ConditionElement[] elements = notUseErrorcheck.getCondition();
					for( int i = 0; elements != null && i < elements.length; i++){
						if( i == 0 ){
							msg += "[";
						}
						msg += (i > 0 ? ", ":"") + elements[i].value;
						if( i == elements.length-1 ){
							msg += "]";
						}
					}
					notUseErrorcheck.message = msg;		
			        notUseErrorcheck.appendConstraints(sb);
				}
				
				set = notDefineErrorMap.keySet();
				its = set.iterator();
				while( its.hasNext()){
					String key = its.next();
					VariantErrorCheck notDefineErrorcheck = notDefineErrorMap.get(key);
					String msg = VariantValue.TC_MESSAGE_NOT_DEFINE;
					ConditionElement[] elements = notDefineErrorcheck.getCondition();
					for( int i = 0; elements != null && i < elements.length; i++){
						if( i == 0 ){
							msg += "[";
						}
						msg += (i > 0 ? ", ":"") + elements[i].value;
						if( i == elements.length-1 ){
							msg += "]";
						}
					}
					notDefineErrorcheck.message = msg;	
					notDefineErrorcheck.appendConstraints(sb);
				}
				
//		        waitProgress.setStatus(registry.getString("variant.checkUserDefineError"), true);
//		        //����� ���� ���� üũ
//		        VariantErrorCheck userDefineErrorcheck = null;
//		        if( userDefineErrorList != null && !userDefineErrorList.isEmpty()){
//		        	for( String[] errorInfo : userDefineErrorList){
//		        		ConditionElement condition = new ConditionElement();
//		        		
//		        		if( errorInfo[0] != null && !errorInfo[0].equals("")){
//		         			if( userDefineErrorcheck != null){
//		        				userDefineErrorcheck.appendConstraints(sb);
//		        			}
//		        			userDefineErrorcheck = new VariantErrorCheck();
//		        			userDefineErrorcheck.type = errorInfo[0];
//		        		}
//		        		if( errorInfo[1] != null && !errorInfo[1].equals("")){
//		        			userDefineErrorcheck.message = errorInfo[1];
//		        			
//		        		}
//						condition.ifOrAnd = errorInfo[2];
//						condition.item = errorInfo[3];
//						condition.op = errorInfo[5];
//						condition.option = errorInfo[4];
//						condition.value = errorInfo[6];
//						condition.valueIsString = true;
//						condition.fullName = errorInfo[3] + ":" + errorInfo[4];
//						userDefineErrorcheck.addCondition( condition );
//		        	}
//		        	userDefineErrorcheck.appendConstraints(sb);
//		        }
//		        
//		        waitProgress.setStatus(registry.getString("variant.checkConstraint"), true);
//		        //���� ��� ���� ����
//		        OptionConstraint moduleConstraintCheck = null;
//		        if( moduleConstraintList != null && !moduleConstraintList.isEmpty()){
//		        	Vector<ConditionElement> conditionVec = new Vector();
//		        	for( String[] moduleConstraint : moduleConstraintList){
//		        		ConditionElement condition = new ConditionElement();
//		        		if( moduleConstraint[0] != null && !moduleConstraint[0].equals("")){
//		        			if( moduleConstraintCheck != null){
//		        				ConditionElement[] conditionElms = conditionVec.toArray(new ConditionElement[conditionVec.size()]);
//		        				moduleConstraintCheck.setCondition(conditionElms);
//		        				if(conditionElms != null && conditionElms.length > 0)
//		        		        {
//		        		            sb.append("if ");
//		        		            ConstraintsModel.appendCondition(conditionElms, sb);
//		        		            sb.append(" then\n ");
//		    			        	moduleConstraintCheck.appendConstraint(sb);
//		    			        	sb.append("\nendif");
//		        		        }
//		        		        sb.append((char)13);
//		        				conditionVec.clear();
//		        			}
//		         			moduleConstraintCheck = new OptionConstraint();
//		         			moduleConstraintCheck.type = moduleConstraint[0];
//		         			moduleConstraintCheck.fullName = moduleConstraint[4] + ":" + moduleConstraint[1];
//		         			moduleConstraintCheck.item = moduleConstraint[4];
//		         			moduleConstraintCheck.option = moduleConstraint[1];
//		         			moduleConstraintCheck.value = moduleConstraint[2];
//		         			moduleConstraintCheck.valueIsString = true;
//		        		}
//						condition.ifOrAnd = moduleConstraint[3];
//						condition.item = moduleConstraint[4];
//						condition.op = moduleConstraint[6];
//						condition.option = moduleConstraint[5];
//						condition.value = moduleConstraint[7];
//						condition.valueIsString = true;
//						condition.fullName = moduleConstraint[4] + ":" + moduleConstraint[5];
//						conditionVec.add( condition );
//		        	}
//		        	ConditionElement[] conditionElms = conditionVec.toArray(new ConditionElement[conditionVec.size()]);
//    				moduleConstraintCheck.setCondition(conditionElms);
//    		        if(conditionElms != null && conditionElms.length > 0)
//    		        {
//    		            sb.append("if ");
//    		            ConstraintsModel.appendCondition(conditionElms, sb);
//    		            sb.append(" then\n ");
//			        	moduleConstraintCheck.appendConstraint(sb);
//			        	sb.append("\nendif");
//    		        }
//    		        sb.append((char)13);
//		        }
		        
		        try{
		        	
		        	waitProgress.setStatus(registry.getString("variant.addValidation"), true);
			        if( notUseErrorMap.size() > 0 || notDefineErrorMap.size() > 0 
//			        		|| ( userDefineErrorcheck != null && userDefineErrorcheck.getConditionSize() > 0 )
			        		|| (moduleConstraintList != null && !moduleConstraintList.isEmpty())){
			        	tcvariantservice.setLineMvl(selectedLine, sb.toString());
			        }else{
			        	tcvariantservice.setLineMvl(selectedLine, "");
			        }
		        }catch(TCException tce){
		        	
		        	waitProgress.setStatus(tce.getDetailsMessage());
		        	System.out.println(sb.toString());
		        	tce.printStackTrace();
		        	throw tce;
		        }finally{
			        //�����Ǿ��� ��ȿ�� üũ���� �ٲ�����Ƿ� Window�� �����ؾ� �ɼ� ������ ������.
			        selectedLine.window().save();
//			        selectedLine.window().refresh();
			        selectedLine.refresh();
		        }
				
			}catch( TCException e){
				e.printStackTrace();
				throw e;
			}finally{
				selectedLine.window().save();
//				selectedLine.window().refresh();
				selectedLine.refresh();
			}
			
//		}
	}
	
	/**
	 * ����� ���� ������ ������ ȣ��.
	 * @throws TCException 
	 */
	private void deleteUserDefineError() throws TCException{
		
		DefaultTableModel model = (DefaultTableModel)userDefineErrorTable.getModel();
		int selectedIdx = userDefineErrorTable.getSelectedRow();
		
		if( selectedIdx < 0 ) return;
		
		TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>)userDefineErrorTable.getRowSorter();
		int modelIdx = sorter.convertRowIndexToModel(selectedIdx);
		
		//�𵨿��� ��Ȯ�� index�� ã�´�.
		int keyIndex = -1;
		for( int i = modelIdx; i >= 0; i--){
			Object obj = model.getValueAt(i, 0);
			if( obj != null && !obj.equals("")){
				keyIndex = i;
				break;
			}
		}
		
		//�񱳿����� ����� ErrorCheck��ü�� ����.
		VariantErrorCheck userDefineErrorcheck = new VariantErrorCheck();
		userDefineErrorcheck.type = (String)model.getValueAt(keyIndex, 0);
		userDefineErrorcheck.message = (String)model.getValueAt(keyIndex, 1);
		
		Vector<ConditionElement> conditionVec = new Vector();
		ConditionElement condition = new ConditionElement();
		condition.ifOrAnd = (String)model.getValueAt(keyIndex, 2);
		condition.item = (String)model.getValueAt(keyIndex, 3);
		condition.option = (String)model.getValueAt(keyIndex, 4);
		condition.op = (String)model.getValueAt(keyIndex, 5);
		condition.value = (String)model.getValueAt(keyIndex, 6);
		condition.valueIsString = true;
		condition.fullName = (String)model.getValueAt(keyIndex, 3) + ":" + (String)model.getValueAt(keyIndex, 4);
		conditionVec.add( condition );
		
		for( int i = keyIndex + 1; i < model.getRowCount(); i++){
			Object obj = model.getValueAt(i, 0);
			if( obj != null && !obj.equals("")){
				break;
			}
			condition = new ConditionElement();
			condition.ifOrAnd = (String)model.getValueAt(i, 2);
			condition.item = (String)model.getValueAt(i, 3);
			condition.option = (String)model.getValueAt(i, 4);
			condition.op = (String)model.getValueAt(i, 5);
			condition.value = (String)model.getValueAt(i, 6);
			condition.valueIsString = true;
			condition.fullName = (String)model.getValueAt(i, 3) + ":" + (String)model.getValueAt(i, 4);
			conditionVec.add( condition );
		}
		ConditionElement[] conditionElms = conditionVec.toArray(new ConditionElement[conditionVec.size()]);
		userDefineErrorcheck.setCondition(conditionElms);
		
		String lineMvl = CustomUtil.getTCSession().getVariantService().askLineMvl(selectedLine);
//		ConstraintsModel constraintsModel = new ConstraintsModel(selectedLine.getItem().getProperty("item_id"), lineMvl, selectedLine, CustomUtil.getTCSession().getVariantService());
		ConstraintsModel constraintsModel = new ConstraintsModel(selectedLine.getItem().getProperty("item_id"), lineMvl, null,selectedLine, CustomUtil.getTCSession().getVariantService());
		
		if(!constraintsModel.parse()){
        	throw new TCException("Condition�� �Ľ� �� �� �����ϴ�.");
        }
		
		int latestIdx = -1;
		Vector<String[]> tmpVec = new Vector();
		Vector<String[]> filteredErrorChecks = new Vector();
		ConditionElement[] userDefineConditions = userDefineErrorcheck.getCondition();
		String[][] errorChecks = constraintsModel.errorChecksTableData();
		for( int i = 0; errorChecks != null && i < errorChecks.length; i++){
			
			String[] errorCheck = errorChecks[i];
			
			if( (errorCheck[1] != null && !errorCheck[1].equals(""))){
				if( !tmpVec.isEmpty() && tmpVec.size() != userDefineConditions.length){
					filteredErrorChecks.addAll(tmpVec);
					tmpVec.clear();
				}
			}
			if( userDefineErrorcheck.type.equals(errorCheck[0]) 
					&& userDefineErrorcheck.message.equals(errorCheck[1])){
				
				boolean isFound = false;
				for( ConditionElement con :userDefineConditions){
					if( con.ifOrAnd.equals(errorCheck[2]) && con.item.equals(errorCheck[3])
							&& con.option.equals(errorCheck[4]) && con.op.equals(errorCheck[5])
							&& con.value.equals(errorCheck[6])){
						latestIdx = i;
						isFound = true;
						tmpVec.add(errorCheck);
						break;
					}else{
					}
				}
				
				//�񱳿�[������]���� ������ ErrorCheck�� �ٸ��͸� ������.
				if( !isFound ){
					filteredErrorChecks.add(errorCheck);
				}
			}else{
				if( (errorCheck[1] == null || errorCheck[1].equals("")) && i == latestIdx + 1){
					latestIdx = i;
					continue;
				}
				filteredErrorChecks.add(errorCheck);
			}
		}
		
		VariantErrorCheck filteredErrorcheck = null;
		Vector<VariantErrorCheck> newErrorChecks = new Vector();
		Vector<ConditionElement> conditions = new Vector();
		for( int i = 0; i < filteredErrorChecks.size(); i++){
			
			if( "if".equals(filteredErrorChecks.get(i)[2])){
				if( filteredErrorcheck != null){
					if( !newErrorChecks.contains(filteredErrorcheck))
						newErrorChecks.add(filteredErrorcheck);
				}
				filteredErrorcheck = new VariantErrorCheck();
				filteredErrorcheck.type =  filteredErrorChecks.get(i)[0];
				filteredErrorcheck.message = filteredErrorChecks.get(i)[1];
			}
			
			ConditionElement newCondition = new ConditionElement();
			newCondition.ifOrAnd = filteredErrorChecks.get(i)[2];
			newCondition.item = filteredErrorChecks.get(i)[3];
			newCondition.option = filteredErrorChecks.get(i)[4];
			newCondition.op = filteredErrorChecks.get(i)[5];
			newCondition.value = filteredErrorChecks.get(i)[6];
			newCondition.valueIsString = true;
			newCondition.fullName = filteredErrorChecks.get(i)[3] + ":" + filteredErrorChecks.get(i)[4];
			filteredErrorcheck.addCondition(newCondition);
		}
		if( !newErrorChecks.contains(filteredErrorcheck))
			newErrorChecks.add(filteredErrorcheck);
		
		StringBuilder sb = new StringBuilder();
		for( VariantErrorCheck eCheck : newErrorChecks){
			eCheck.appendConstraints(sb);
		}
		
		OptionConstraint[] optionConstraints = constraintsModel.moduleConstraints();
		for( int i = 0; optionConstraints != null && i < optionConstraints.length; i++){
			ConditionElement[] conElms = optionConstraints[i].getCondition();
			if(conElms != null && conElms.length > 0)
	        {
	            sb.append("if ");
	            ConstraintsModel.appendCondition(conditionElms, sb);
	            sb.append(" then\n ");
	            optionConstraints[i].appendConstraint(sb);
	        	sb.append("\nendif");
	        }
	        sb.append('\n');
		}

		if( !newErrorChecks.isEmpty() || (optionConstraints != null && optionConstraints.length > 0)){
        	selectedLine.getSession().getVariantService().setLineMvl(selectedLine, sb.toString());
        }else{
        	selectedLine.getSession().getVariantService().setLineMvl(selectedLine, "");
        }
			
	}
	
	/**
	 * ��� �ɼ� ���������� ���� �Ǿ����� �����.
	 * OOTB���� ���Ǵ� ����� ������ �� ����.
	 * @throws TCException 
	 * 
	 */
	private void deleteModuleConstraint() throws TCException{
		DefaultTableModel model = (DefaultTableModel)moduleConstraintTable.getModel();
		int selectedIdx = moduleConstraintTable.getSelectedRow();
		
		if( selectedIdx < 0 ) return;
		
		TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>)moduleConstraintTable.getRowSorter();
		int modelIdx = sorter.convertRowIndexToModel(selectedIdx);
		
		int keyIndex = -1;
		for( int i = modelIdx; i >= 0; i--){
			Object obj = model.getValueAt(i, 0);
			if( obj != null && !obj.equals("")){
				keyIndex = i;
				break;
			}
		}
		
		//�񱳷����� �����ϱ� ���� ������ moduleConstraint�� ������  --- Start
		OptionConstraint moduleConstraintCheck = new OptionConstraint();
		moduleConstraintCheck.type = (String)model.getValueAt(keyIndex, 0);
		moduleConstraintCheck.fullName = (String)model.getValueAt(keyIndex, 4) + ":" + (String)model.getValueAt(keyIndex, 1);
		moduleConstraintCheck.item = (String)model.getValueAt(keyIndex, 4);
		moduleConstraintCheck.option = (String)model.getValueAt(keyIndex, 1);
		moduleConstraintCheck.value = (String)model.getValueAt(keyIndex, 2);
		
		Vector<ConditionElement> conditionVec = new Vector();
		ConditionElement condition = new ConditionElement();
		condition.ifOrAnd = (String)model.getValueAt(keyIndex, 3);
		condition.item = (String)model.getValueAt(keyIndex, 4);
		condition.op = (String)model.getValueAt(keyIndex, 6);
		condition.option = (String)model.getValueAt(keyIndex, 5);
		condition.value = (String)model.getValueAt(keyIndex, 7);
		condition.valueIsString = true;
		condition.fullName = (String)model.getValueAt(keyIndex, 4) + ":" + (String)model.getValueAt(keyIndex, 5);
		conditionVec.add( condition );
		
		for( int i = keyIndex + 1; i < model.getRowCount(); i++){
			Object obj = model.getValueAt(i, 0);
			if( obj != null && !obj.equals("")){
				break;
			}
			condition = new ConditionElement();
			condition.ifOrAnd = (String)model.getValueAt(i, 3);
			condition.item = (String)model.getValueAt(i, 4);
			condition.op = (String)model.getValueAt(i, 6);
			condition.option = (String)model.getValueAt(i, 5);
			condition.value = (String)model.getValueAt(i, 7);
			condition.valueIsString = true;
			condition.fullName = (String)model.getValueAt(i, 4) + ":" + (String)model.getValueAt(i, 5);
			conditionVec.add( condition );
		}
		ConditionElement[] conditionElms = conditionVec.toArray(new ConditionElement[conditionVec.size()]);
		moduleConstraintCheck.setCondition(conditionElms);
		//�񱳷����� �����ϱ� ���� ������ moduleConstraint�� ������  --- End
		
		String lineMvl = CustomUtil.getTCSession().getVariantService().askLineMvl(selectedLine);
//		ConstraintsModel constraintsModel = new ConstraintsModel(selectedLine.getItem().getProperty("item_id"), lineMvl, selectedLine, CustomUtil.getTCSession().getVariantService());
		ConstraintsModel constraintsModel = new ConstraintsModel(selectedLine.getItem().getProperty("item_id"), lineMvl, null, selectedLine, CustomUtil.getTCSession().getVariantService());
		
		if(!constraintsModel.parse()){
        	throw new TCException("Condition�� �Ľ� �� �� �����ϴ�.");
        }
		
		//���� ������ BOM���ο� ������ ��� ���� ������ �������� ����� ���� ���ǰ� ���Ͽ� ���� �Ǿ����� ��
		//���� ������ ã�´�.
		OptionConstraint constToDelete = null;
		OptionConstraint[] moduleConstraints = constraintsModel.moduleConstraints();
		for( OptionConstraint moduleConstraint : moduleConstraints){
			
			if( moduleConstraint.type.equals(moduleConstraintCheck.type) 
					&& moduleConstraint.item.equals(moduleConstraintCheck.item)
					&& moduleConstraint.option.equals(moduleConstraintCheck.option)
					&& moduleConstraint.value.equals(moduleConstraintCheck.value)){
				
				int equalCount = 0;
				ConditionElement[] conditions = moduleConstraint.getCondition();
				ConditionElement[] curConditions = moduleConstraintCheck.getCondition();
				for( ConditionElement con : conditions){
					for( ConditionElement curCon : curConditions){
						if( con.ifOrAnd.equals(curCon.ifOrAnd)
								&& con.item.equals(curCon.item)
								&& con.op.equals(curCon.op)
								&& con.option.equals(curCon.option)
								&& con.value.equals(curCon.value)){
							equalCount++;
						}
								
					}
				}
				
				if( equalCount == conditions.length){
					constToDelete = moduleConstraint;
					break;
				}
			}
		}
		if( constToDelete != null){
			
			//�ش� ���� ���� ����
			constraintsModel.removeConstraint(constToDelete);
			
			//���̺� ������Ʈ.
			try{
				Vector<String[]> tmpCostList = OptionManager.getModuleConstraints(selectedLine, true);
				moduleConstraintList.clear();
				moduleConstraintList.addAll(tmpCostList);
				Vector<Vector> moduleConstraintData = new Vector();
				for( String[] moduleConstraint: moduleConstraintList){
					Vector row = new Vector();
					for( String str : moduleConstraint){
						row.add(str == null ? "":str);
					}
					moduleConstraintData.add(row);
				}
				
				Vector moduleConstraintHeader = new Vector();
				Enumeration columns = moduleConstraintTable.getColumnModel().getColumns();
				while(columns.hasMoreElements()){
					TableColumn tc = (TableColumn)columns.nextElement();
					moduleConstraintHeader.add(tc.getHeaderValue());
				}
				model.setDataVector(moduleConstraintData, moduleConstraintHeader);
			}catch(TCException tce){
				tce.printStackTrace();
			}
		}
	}
}
