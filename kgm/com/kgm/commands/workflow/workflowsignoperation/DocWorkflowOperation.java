package com.kgm.commands.workflow.workflowsignoperation;

import java.awt.Cursor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import jxl.read.biff.BiffException;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.kgm.commands.workflow.SSANGYONGDecisionDialog;
import com.kgm.commands.workflow.workflowsignoperation.form.JxlsTestFormBeans;
import com.kgm.common.SYMCClass;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.NamedReferenceContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinition;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

/**
 * 결재 완료시 엑셀 파일에 결재 정보 입력
 * @Copyright : S-PALM
 * @author   : 이정건
 * @since    : 2012. 4. 27.
 * Package ID : com.pungkang.commands.workflow.workflowsignoperation.DocWorkflowOperation.java
 */
@SuppressWarnings("rawtypes")
public class DocWorkflowOperation extends AbstractAIFOperation {

	private TCSession session;

	private Registry registry = Registry.getRegistry(this);

	/** 현재 진행중인 workflowProcess */
	private TCComponent workflowProcess;

	/** excel 에 작성될 user 정보들 */
	private ArrayList<String> userArrayList = new ArrayList<String>();

	/** excel 에 작성될 날자 정보들 */
	private ArrayList<String> dateArrayList = new ArrayList<String>();

	/** TCComponentProcess들 */
	private ArrayList<InterfaceAIFComponent> wfprocessArrayList = new ArrayList<InterfaceAIFComponent>();

	private TCComponentTcFile[] imanFiles;
	private TCComponentDataset dataset1;
	private Workbook myWorkbook;
	private HashMap<String, String[]> hash = new HashMap<String, String[]>();

	private SSANGYONGDecisionDialog decisionDialog;

	private File file;

	/**
	 * 생성자
	 * @copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2012. 4. 27.
	 * @param decisionDialog
	 * @param comp
	 * @throws TCException
	 */
	public DocWorkflowOperation(SSANGYONGDecisionDialog decisionDialog, TCComponent comp) throws TCException {
		this.session = comp.getSession();
		this.workflowProcess = comp;
		this.decisionDialog = decisionDialog;
	}

	/**
	 * 결제승인시 프로세스의 결제자정보를 읽어서 엑셀파일에 입력
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2012. 4. 27.
	 * @override
	 * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
	 * @throws Exception
	 */
	@Override
	public void executeOperation() throws Exception {

		decisionDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		if(checkXls())
		{
			try {
				if (workflowProcess instanceof TCComponentProcess) {
					wfprocessArrayList.add(workflowProcess);
				} else if (workflowProcess instanceof TCComponent) {
					AIFComponentContext[] context = ((TCComponent)workflowProcess).whereReferenced();
					for (int j = 0; j < context.length; j++) {
						if (context[j].getComponent() instanceof TCComponentProcess)
							wfprocessArrayList.add(context[j].getComponent());
					}
				}
				session.setReadyStatus();
				if(wfprocessArrayList.size() != 0)
					getData(((TCComponentProcess)wfprocessArrayList.get(0)).getRootTask(), 0);
			}
			catch(Exception e) {
				e.printStackTrace();
			}

			String[] compareArray = registry.getStringArray("STANDARD_DOC_EXCEL");
			String[] user = new String[2];

			String[] taskName = registry.getStringArray("DOC_TASK.ARRAY");

			for (int j = 0; j < taskName.length; j++) {
				if( hash.get( taskName[j] )!= null){
					if(hash.get( taskName[j] ) instanceof String[])
						user = hash.get( taskName[j] );
					userArrayList.add( user[0] );
					dateArrayList.add( user[1] );

				}else{
					userArrayList.add("전결");
					dateArrayList.add("");
				}
			}

			String[] txtKey = compareArray;
			String[] txtValue = new String[userArrayList.size()+dateArrayList.size()];
			int value = userArrayList.size()+dateArrayList.size();
			int getDate = 0;
			for(int i=0;i<value;i++)
			{
				if(i<userArrayList.size())
					txtValue[i] = userArrayList.get(i).toString();
				else{
					txtValue[i] = dateArrayList.get(getDate).toString();
					getDate++;

				}
			}
			JxlsTestFormBeans beans = new JxlsTestFormBeans();
			beans.setKey(txtKey);
			beans.setValue(txtValue);

			read(beans);

			if(this.file != null){
				this.file.delete();
			}
		}

		decisionDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * 원본 엑셀파일을 읽어들임
	 * @param beans Key와 Velue 값이 저장되있는 클래스
	 * @throws BiffException
	 * @throws IOException
	 * @throws RowsExceededException
	 * @throws WriteException
	 */
    private void read(JxlsTestFormBeans beans) throws Exception  {
		File file;
		try {
			file = downloadXls(workflowProcess);
			if(file!=null)
			{
				ArrayList<HashMap<String, Comparable>> arr = new ArrayList<HashMap<String, Comparable>>();
				Workbook myWorkbook = WorkbookFactory.create(new FileInputStream(file.getPath()));
				Sheet mySheet = myWorkbook.getSheetAt(0);

				for(int i=0;i <= mySheet.getLastRowNum();i++){			// 행의 갯수 만큼 돌려서
					Row row = mySheet.getRow(i);
					if(row != null){
						for(int j=0;j < row.getLastCellNum() ;j++){	// 열의 갯수 만큼 돌려서
							Cell myCell = row.getCell(j); // 셀의 행과 열의 정보를 가져온 후...
							if( myCell !=null ){
								String cellValue = myCell.toString();								
								int rowInt = i;
								int colInt = j;

								if(cellValue.startsWith("${")){
									HashMap<String, Comparable> map = new HashMap<String, Comparable>();
									map.put("key", cellValue);
									map.put("row", rowInt);
									map.put("col", colInt);
									arr.add(map);
								}
							}
						}
					}
				}
				write(arr, myWorkbook, beans, file);	
				
				file.delete();
			}
		} catch (TCException e) {
			e.printStackTrace();
		}			
	}

	/**
	 * 가져온 데이터셋에 결제자 정보 입력
	 * @param arr 엑셀파일에서 ${  로 시작되는 문자열과 그 위치정보가 담긴 hashmap
	 * @param readWorkbook 엑셀파일의 정보
	 * @param beans Key와 Velue 값이 저장되있는 클래스
	 * @param file 로컬로 떨어뜨린 엑셀파일
	 * @throws RowsExceededException
	 * @throws WriteException
	 * @throws IOException
	 */
	private void write(ArrayList<HashMap<String, Comparable>> arr, Workbook readWorkbook, JxlsTestFormBeans beans, File file) throws IOException {
		String tempName = file.getName();
		String newfile = "temp_"+tempName;
		String path = file.getParent();
		Sheet sheet = null;

		try {
			myWorkbook = WorkbookFactory.create( new FileInputStream( file.getPath() ) );
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		sheet = myWorkbook.getSheetAt(0);

		for(int i=0;i < arr.size();i++){
			HashMap map = arr.get(i);
			String key = (String)map.get("key");
			int row = (Integer)map.get("row");
			int col = (Integer)map.get("col");
			sheet.getRow(row).getCell(col).setCellValue( "" );
			for(int j=0;j<beans.getKey().length;j++){
				if(key.trim().equalsIgnoreCase(beans.getKey()[j].trim())){
					String beansValue = beans.getValue()[j].trim();
					sheet.getRow(row).getCell(col).setCellValue( beansValue );                    	
				}
			}

		}

		myWorkbook.write( new FileOutputStream( file.getPath() ) ); // 준비된 정보를 엑셀 포멧에 맞게 작성

		file.delete();
		File result = new File(path,newfile);
		result.renameTo(new File(path,tempName));

		File file2 = new File(result.getParent(),tempName);

		try {
			setDataset(file2,workflowProcess);
			
			file2.delete();
			result.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 데이터셋 셋팅
	 * @param file2 새로 작성된 엑셀파일
	 * @param component 데이터셋을 추가할 컴포넌트
	 * @throws Exception
	 */
	private void setDataset(File file2, TCComponent component) throws Exception {
		TCComponent[] dataset = component.getRelatedComponents(SYMCClass.STANDARD_DOC_REL);
		TCComponentDataset  datasetcomponent=null; 
		for (int i = 0; i < dataset.length; i++) {
			if(dataset[i] instanceof TCComponentDataset)
			{	
				TCComponentDataset dataset1 = (TCComponentDataset)dataset[i];
				if(dataset1.getType().equalsIgnoreCase("MSEXCEL")||dataset1.getType().equalsIgnoreCase("MSEXCELX")){
					datasetcomponent = dataset1;
				}
			}
		}
		Vector<String> refNameVector = getAllNamedRefTypeArray(datasetcomponent);
		
		for (int j = 0; j < refNameVector.size(); j++) {
			datasetcomponent.removeNamedReference(refNameVector.elementAt(j).toString());
		}
		String datasetTypeName = datasetcomponent.getType();
		TCComponentDataset newDataset = ((TCComponentDatasetType)session.getTypeComponent("Dataset")).create(file2.getName().substring(0,file2.getName().lastIndexOf(".")), "", datasetTypeName);
		TCComponentDatasetDefinition def = datasetcomponent.getDatasetDefinitionComponent();
		NamedReferenceContext namedRefTypes[] = def.getNamedReferenceContexts();
		setNamedRef(file2,newDataset, namedRefTypes);
		
		file2.delete();
	}

	/**
	 * 데이터셋의 네임드 레퍼런스 셋팅
	 * @param file2 새로 작성된 엑셀 파일
	 * @param newDataset 생성할 데이터셋
	 * @param namedRefTypes 데이터셋의 레퍼런스 타입
	 * @throws TCException
	 */
	private void setNamedRef(File file2,TCComponentDataset newDataset,NamedReferenceContext[] namedRefTypes) throws TCException {
		TCComponent comps[] = imanFiles;
		int cnt=0;
		for(int i = 0; i < namedRefTypes.length; i++)
		{
			for(int j = 0; j < comps.length; j++)
			{
				if(cnt ==0)
					if(comps[j] instanceof TCComponentTcFile){
						setFile(file2,newDataset, namedRefTypes[i]);
					}
			}
			cnt++;
		}
	}

	/**
	 * 데이터셋 올림
	 * @param file2 새로 작성된 엑셀 파일
	 * @param newDataset 생성할 데이터셋
	 * @param namedReferenceContext 명명된참조의 내용
	 * @throws TCException
	 */
	private void setFile(File file2,TCComponentDataset newDataset,NamedReferenceContext namedReferenceContext) throws TCException {

		String workingPath = file2.getPath();
		File lfile = new File(workingPath);

		String as[] = {
				lfile.getPath()
		};
		String as1[] = {
				namedReferenceContext.getFileFormat()
		};
		String as2[] = {
				namedReferenceContext.getMimeType()
		};
		String as3[] = {
				namedReferenceContext.getNamedReference()
		};
		dataset1.setFiles(as, as1, as2, as3, 512);
		
		lfile.delete();
		file2.delete();
	}

	/**
	 * 데이터셋에 연결되있는 레퍼런스를 검색
	 * @param datasetComponent 데이터셋 컴포넌트
	 * @return 데이터셋과 관련된 레퍼런스타입
	 * @throws Exception
	 */
	private static Vector<String> getAllNamedRefTypeArray(TCComponentDataset datasetComponent) throws Exception {
		Vector<String> vector = new Vector<String>();
		NamedReferenceContext[] namedRefContext = null;
		try {
			namedRefContext = datasetComponent.getDatasetDefinitionComponent().getNamedReferenceContexts();
			for (int i = 0; i < namedRefContext.length; i++) {
				String s1 = namedRefContext[i].getNamedReference();
				vector.addElement(s1);
			}
		}
		catch (Exception e) {
			throw e;
		}
		return vector;
	}

	/**
	 * 프로세스에서 결제자정보와 날짜를 가져옴
	 * @param task 결제정보를 찾을 타스크
	 * @param level 하위 타스크가 달려있는지를 확인
	 */
	private void getData(TCComponentTask task, int level) {
		try {

			task.refresh();

			TCProperty property = task.getProcess().getTCProperty("owning_user");
			TCComponentUser user1 = (TCComponentUser)property.getReferenceValue();
			String user2 = user1.toString();
			if(userArrayList.size() == 0)
			{
				userArrayList.add(user2.substring(0, user2.lastIndexOf("(")-1));
				dateArrayList.add(SYMCClass.DATE_FORMAT.format(task.getDateProperty("creation_date")));
			}
			if (task.getTaskType().equals("EPMTask") || task.getTaskType().equals("EPMReviewTask")) {		       
				TCComponentTask[] subTask = task.getSubtasks();
				if (subTask.length > 0) {
					for (int i = 0; i < subTask.length; i++)
					{
						if(subTask[i].getTaskType().equals("EPMAddStatusTask"))
							continue;
						getData(subTask[i], level);
					}
				}
			} else if (task.getTaskType().equals("EPMDoTask")) {
				TCComponentUser user = (TCComponentUser)task.getResponsibleParty();
				userArrayList.add(getUserInfomation(user).toString());
				dateArrayList.add(getDateInfomation(task, true));
			} else if (task.getTaskType().equals("EPMPerformSignoffTask")) {
				TCComponentSignoff[] signoffs = task.getValidSignoffs();
				for (int j = 0; j < signoffs.length; j++) {
					TCComponentSignoff signoff = signoffs[j];
					signoff.refresh();
					taskInfo(task, signoff);
				}
			}			
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 타스크의 이름과 결제자정보, 날짜를 hashmap 에 입력
	 * @param task  결제정보를 찾을 타스크
	 * @param signoff Review 타스크의 결제정보를 불러옴
	 */
	private void taskInfo(TCComponentTask task, TCComponentSignoff signoff) {
		try {
			String TaskStr = task.getParent().toString();
			TCComponentUser user = signoff.getGroupMember().getUser();
			String[] userInfo = new String[2];
			userInfo[0] = user.toString().substring(0, user.toString().lastIndexOf("(")-1);
			userInfo[1] = CustomUtil.getTODAY();

			hash.put( TaskStr, userInfo );

		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 유저정보 가져오기
	 * @param imancomponentuser 사용자
	 * @return 사용자
	 */
	private String getUserInfomation(TCComponentUser imancomponentuser) {
		String user = imancomponentuser.toString().substring(0,imancomponentuser.toString().lastIndexOf('(')-1);
		return user;
	}

	/**
	 * 승인된 날짜 정보 가져오기
	 * @param task 결제정보를 가져올 타스크
	 * @param isCreation 어떤날짜를 가져올자 결정
	 * @return 승인 날짜
	 */
	private String getDateInfomation(TCComponentTask task, boolean isCreation) {
		String dateProperty = "";
		if (isCreation) {
			dateProperty = "creation_date";
		} else {
			dateProperty = "last_mod_date";
		}
		try {
			return SYMCClass.DATE_FORMAT.format(task.getDateProperty(dateProperty));
		} catch (TCException e) {
			return null;
		}
	}

	/**
	 * 이이템리비젼에 달려있는 데이터셋을 로컬로 copy
	 * @param component 결제 중인 컴포넌트
	 * @return 컴포넌트에 달려있는 엑셀파일
	 * @throws TCException
	 */
	private File downloadXls(TCComponent component) throws TCException {
		TCComponent[] dataset = component.getRelatedComponents(SYMCClass.STANDARD_DOC_REL);
		for (int i = 0; i < dataset.length; i++) {
			if(dataset[i] instanceof TCComponentDataset)
			{	
				dataset1 = (TCComponentDataset)dataset[i];
				if(dataset1.getType().equalsIgnoreCase("MSEXCEL") || dataset1.getType().equalsIgnoreCase("MSEXCELX")){
					imanFiles = ((TCComponentDataset)dataset[i]).getTcFiles();
				}
			}
		}

		TCComponentTcFile[] files = imanFiles;		

		File directory = new File(SYMCClass.TEMPDIRECTORY);
		
        if(!directory.exists()){
        	directory.mkdirs();
        }

		File file = files[0].getFile(directory.getAbsolutePath());

		this.file = file;
		return file;
	}


	/**
	 * 표준 양식 Excel 파일이 있는지 체크
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since  : 2012. 5. 2.
	 * @return 표준양식
	 * @throws TCException
	 */
	private boolean checkXls() throws TCException{
		boolean flag = false;
		TCComponent[] dataset = workflowProcess.getRelatedComponents(SYMCClass.STANDARD_DOC_REL);
		for (int i = 0; i < dataset.length; i++) {
			if(dataset[i] instanceof TCComponentDataset)
			{
				TCComponentDataset dataset1 = (TCComponentDataset)dataset[i];
				if(dataset1.getType().equalsIgnoreCase("MSEXCEL")||dataset1.getType().equalsIgnoreCase("MSEXCELX")){
					flag = true;
				}
			}
		}
		return flag;
	}
}