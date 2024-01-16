package com.ssangyong.commands.workflow.workflowsignoperation;

import java.awt.Cursor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.ssangyong.commands.workflow.SSANGYONGDecisionDialog;
import com.ssangyong.commands.workflow.workflowsignoperation.form.JxlsTestFormBeans;
import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.NamedReferenceContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinition;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * ****************************************************************************************************
 * 개요: 결제완료시 결제정보 입력<BR>
 * 설명: ECR결제 완료시 infodba 계정에서 엑셀파일을 가져와서 결제정보를 입력 
 *****************************************************************************************************
 */
@SuppressWarnings("rawtypes")
public class EcrWorkflowOperation extends AbstractAIFOperation {
	private TCSession session;
	private Registry registry;
	private TCComponent component;
	private Vector userVector = new Vector();
	private Vector dateVector = new Vector();
	private Vector formInfoVector = new Vector();
	private Vector processVector = new Vector();
	private TCComponentTcFile[] imanFiles;
	private TCComponentDataset newDataset1;
	private TCComponentDataset findDataset;

	private Workbook myWorkbook;
	private HashMap hash = new HashMap();

	private SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyy-MM-dd");

	private SSANGYONGDecisionDialog decisionDialog;
	
	public EcrWorkflowOperation( SSANGYONGDecisionDialog decisionDialog, TCComponent comp) throws TCException {
		this.session = comp.getSession();
		registry = Registry.getRegistry( this );
		component = comp;
		myWorkbook = null;
		this.decisionDialog = decisionDialog;
	}

	/**
	 * 1. 결제승인시 프로세스의 결제자정보를 읽어옴
	 * 2. infodba의 설계변경요청서를 내려받아서 결제자정보를 입력
	 */
	@SuppressWarnings("unchecked")
	public void executeOperation() throws Exception {

		decisionDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		try {
			if (component instanceof TCComponentProcess) {
				processVector.addElement(component);
			} else if (component instanceof TCComponent) {
				AIFComponentContext[] context = ((TCComponent)component).whereReferenced();
				for (int j = 0; j < context.length; j++) {
					if (context[j].getComponent() instanceof TCComponentProcess)
						processVector.addElement(context[j].getComponent());
				}
			}
			session.setReadyStatus();
			if(processVector.size() != 0)
				getData(((TCComponentProcess)processVector.elementAt(0)).getRootTask(), 0);
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		TCPreferenceService tcpreferenceservice = session.getPreferenceService();
		//String[] compareArray = tcpreferenceservice.getStringArray(0, "SPALM_CR_ExcelFind");
		//String[] compareArray = tcpreferenceservice.getStringValuesAtLocation("SPALM_CR_ExcelFind", TCPreferenceLocation.convertLocationFromLegacy(TCPreferenceService.TC_preference_all));
		String[] compareArray = tcpreferenceservice.getStringValues("SPALM_CR_ExcelFind");
		String[] user = new String[2];

		//TCPreferenceService tcpreferenceservice1 = session.getPreferenceService();
		//String[] taskName = tcpreferenceservice1.getStringArray(0, "SPALM_CR_Task");
		//String[] taskName = tcpreferenceservice1.getStringValuesAtLocation("SPALM_CR_Task", TCPreferenceLocation.convertLocationFromLegacy(TCPreferenceService.TC_preference_all));
		String[] taskName = tcpreferenceservice.getStringValues("SPALM_CR_Task");


		for (int j = 0; j < taskName.length; j++) {
			if( hash.get( taskName[j] )!= null){
				if(hash.get( taskName[j] ) instanceof String[])
					user = (String[]) hash.get( taskName[j] );
				userVector.add( user[0] );
				dateVector.add( user[1] );

			}else{
				userVector.add("전결");
				dateVector.add( " " );
			}
		}
		getFormInfo(component);

		String[] txtKey = compareArray;
		String[] txtValue = new String[userVector.size()+dateVector.size()+formInfoVector.size()];

		int value = userVector.size()+dateVector.size();
		int getDate=0;
		for(int i=0;i<value;i++)
		{
			if(i<userVector.size())
				txtValue[i] = userVector.get(i).toString();
			else{
				txtValue[i]=dateVector.get(getDate).toString();
				getDate++;

			}
		}

		int infocnt = 0;
		for (int i = userVector.size()+dateVector.size(); i < txtValue.length; i++) {
			{
				txtValue[i] = formInfoVector.get(infocnt).toString();
				infocnt++;
			}
		}

		JxlsTestFormBeans beans = new JxlsTestFormBeans();
		beans.setKey(txtKey);
		beans.setValue(txtValue);

		read(beans);

		decisionDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * 폼정보를 폼정보벡터에 추가
	 * @param component ECR 컴포넌트
	 */
	@SuppressWarnings("unchecked")
	private void getFormInfo(TCComponent component) {
		try {
			TCComponentForm form = (TCComponentForm)component.getRelatedComponent("IMAN_master_form");

			String[] crProperty = registry.getStringArray("WorkflowSign_ECRAttribute.ARRAY");
			for (int i = 0; i < crProperty.length; i++) {
				if(!crProperty[i].startsWith("s2_"))
				{
					formInfoVector.addElement(component.getProperty(crProperty[i]));
				}
				else if(crProperty[i].endsWith("Date"))
				{
					if(form.getDateProperty(crProperty[i]) != null)
						formInfoVector.addElement(simpleDateFormat.format(form.getDateProperty(crProperty[i])));
					else
						formInfoVector.addElement(form.getProperty(""));
				}
				else
				{
					if(form.getProperty(crProperty[i]) != null)
						formInfoVector.addElement(form.getProperty(crProperty[i]));
					else
						formInfoVector.addElement(form.getProperty(""));
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 원본 엑셀파일을 읽어드림
	 * @param beans Key와 Velue 값이 저장되있는 클래스
	 * @throws BiffException
	 * @throws IOException
	 * @throws RowsExceededException
	 * @throws WriteException
	 */
	@SuppressWarnings("unchecked")
	private void read(JxlsTestFormBeans beans) throws Exception {
		File file = getXls();
		ArrayList arr = new ArrayList();
		Workbook myWorkbook = WorkbookFactory.create(new FileInputStream(file.getPath()));
		Sheet mySheet = myWorkbook.getSheetAt(0);

		for(int i=0;i <= mySheet.getLastRowNum();i++){	// 행의 갯수 만큼 돌려서
			Row row = mySheet.getRow(i);
			if(row != null){
				for(int j=0;j < row.getLastCellNum() ;j++){	// 열의 갯수 만큼 돌려서
					Cell myCell = row.getCell(j); // 셀의 행과 열의 정보를 가져온 후...
					String CellVaule = myCell.toString();
					int rowInt = i;
					int colInt = j;

					if(CellVaule.startsWith("${")){
						HashMap map = new HashMap();
						map.put("key", CellVaule);
						map.put("row", rowInt);
						map.put("col", colInt);
						arr.add(map);
					}
				}
			}
		}
		write(arr, myWorkbook, beans, file);			
	}

	/**
	 * infodba 계정에서 데이터셋을 가져옴
	 * @param component2 설계변경요청서 엑셀파일
	 * @return
	 */
	private File getXls() {
		TCComponent[] findTCComponents=null;
		File file=null;
		try{
			TCPreferenceService service = session.getPreferenceService();
			//findTCComponents = findTCComponents(session,service.getString(TCPreferenceService.TC_preference_site, "SPALM_CR_AttachFile_Name"),"infodba");
			findTCComponents = findTCComponents(session, service.getStringValueAtLocation("SPALM_CR_AttachFile_Name", TCPreferenceLocation.convertLocationFromLegacy(TCPreferenceService.TC_preference_site)), "infodba");
			findDataset = (TCComponentDataset)findTCComponents[0];
			imanFiles = ((TCComponentDataset)findDataset).getTcFiles();
			TCComponentTcFile[] files = imanFiles;		
			File directory = new File(registry.getString("TCExportDir"));

			file = files[0].getFile(directory.getAbsolutePath());

			return file;
		} catch (Exception e) {
			MessageBox.post(registry.getString("no.Dataset"),"DataSet Find Error",MessageBox.ERROR);
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 데이터셋 검색
	 * @param session 세션
	 * @param compName 데이터셋 이름
	 * @param user 데이터셋 소유자      
	 * @return 데이터셋
	 */
	public TCComponent[] findTCComponents(TCSession session, String compName, String compType) throws Exception
	{
		String entryNames[] = {
				registry.getString("Name.QUERY"), registry.getString("OwningUser.QUERY")
		};
		String entryValues[] = {
				compName, compType
		};
		return CustomUtil.queryComponent(CustomUtil.getTextServerString(session, "k_find_dataset_name"), entryNames, entryValues);
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
	private void write(ArrayList arr, Workbook readWorkbook,JxlsTestFormBeans beans,File file) throws IOException {
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
			HashMap map = (HashMap)arr.get(i);
			String key = (String)map.get("key");
			int row = (Integer)map.get("row");
			int col = (Integer)map.get("col");
			sheet.getRow(row).getCell(col).setCellValue( "" );
			for(int j=0;j<beans.getKey().length;j++){
				if(key.trim().equalsIgnoreCase(beans.getKey()[j].trim())){
					String beansValue = beans.getValue()[j].trim();
					CellStyle cellstyle = myWorkbook.createCellStyle();
					cellstyle.setAlignment( CellStyle.ALIGN_LEFT );
					cellstyle.setVerticalAlignment( CellStyle.VERTICAL_CENTER );
					cellstyle.setBorderBottom( CellStyle.BORDER_THIN );
					if(check(key.trim()))
						sheet.getRow(row).getCell(col).setCellValue( beansValue );                    	
					else{
						sheet.getRow(row).getCell(col).setCellValue( beansValue );
						sheet.getRow(row).getCell(col).setCellStyle( cellstyle );
					}
				}
			}

		}

		myWorkbook.write( new FileOutputStream( file.getPath() ) ); // 준비된 정보를 엑셀 포멧에 맞게 작성

		file.delete();
		File result = new File(path,newfile);
		result.renameTo(new File(path,tempName));

		File file2 = new File(result.getParent(),tempName);
		System.out.println(file2.getPath());

		try {
			setDataset(file2,component);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 워크플로우의 타스크 이름과 승인자가 할당된 타스크를 비교
	 * @param key '${'로 시작되는 문자열
	 * @return
	 */
	private boolean check(String key) {
		TCPreferenceService tcpreferenceservice;
		try {
			tcpreferenceservice = session.getPreferenceService();
			//String[] compareArray = tcpreferenceservice.getStringArray(0, "SPALM_CR_ExcelFind");
			//String[] compareArray = tcpreferenceservice.getStringValuesAtLocation("SPALM_CR_ExcelFind", TCPreferenceLocation.convertLocationFromLegacy(TCPreferenceService.TC_preference_all));
			String[] compareArray = tcpreferenceservice.getStringValues("SPALM_CR_ExcelFind");
			String[] str = new String[16];
			for (int i = 0; i < str.length; i++) {
				str[i] = compareArray[i];
			}
			for (int i = 0; i < str.length; i++) {
				if(key.equalsIgnoreCase(str[i].trim()))
					return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 데이터셋 셋팅
	 * @param file2 새로 작성된 엑셀파일
	 * @param component 데이터셋을 추가할 컴포넌트
	 * @throws Exception
	 */
	private void setDataset(File file2, TCComponent component) throws Exception {

		String datasetTypeName = findDataset.getType();
		TCComponentDataset newDataset = ((TCComponentDatasetType)session.getTypeComponent("Dataset")).create(file2.getName().substring(0,file2.getName().lastIndexOf(".")), "", datasetTypeName);
		TCComponentDatasetDefinition def = findDataset.getDatasetDefinitionComponent();
		NamedReferenceContext namedRefTypes[] = def.getNamedReferenceContexts();
		setNamedRef(file2,newDataset, namedRefTypes);

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
					if(comps[j] instanceof TCComponentTcFile)
						setFile(file2,newDataset,(TCComponentTcFile)comps[j], namedRefTypes[i]);
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
	private void setFile(File file2,TCComponentDataset newDataset,TCComponentTcFile componentTcFile,NamedReferenceContext namedReferenceContext) throws TCException {

		String datasetType = newDataset.getType();
		newDataset1 = ((TCComponentDatasetType)session.getTypeComponent("Dataset")).create("설계변경요청서", "", datasetType);


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
		newDataset1.setFiles(as, as1, as2, as3, 512);


		TCComponentItem item = (TCComponentItem)component;
		item.add("IMAN_reference", newDataset1);

		lfile.delete();


	}


	/**
	 * 데이터셋에 연결되있는 레퍼런스를 검색
	 * @param datasetComponent 데이터셋 컴포넌트
	 * @return 데이터셋과 관련된 레버런트타입
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Vector getAllNamedRefTypeArray(TCComponentDataset datasetComponent) throws Exception {
		Vector s = new Vector();
		NamedReferenceContext[] namedRefContext = null;
		try {
			namedRefContext = datasetComponent.getDatasetDefinitionComponent().getNamedReferenceContexts();
			for (int i = 0; i < namedRefContext.length; i++) {
				String s1 = namedRefContext[i].getNamedReference();
				s.addElement(s1);
			}
		}
		catch (Exception e) {
			throw e;
		}
		return s;
	}

	/**
	 * 프로세스에서 결제자정보와 날짜를 가져옴
	 * @param task 결제정보를 찾을 타스크
	 * @param level 하위 타스크가 달려있는지를 확인
	 */
	@SuppressWarnings("unchecked")
	private void getData(TCComponentTask task, int level) {
		try {

			task.refresh();
			TCProperty property = task.getProcess().getTCProperty("owning_user");
			TCComponentUser user1 = (TCComponentUser)property.getReferenceValue();
			System.out.println( "userId = " + user1.getUserId() );
			String user2 = user1.toString();
			if(userVector.size() == 0)
			{
				userVector.addElement(user2.substring(0, user2.lastIndexOf("(")-1));					
				dateVector.addElement(simpleDateFormat.format(task.getDateProperty("creation_date")));
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
				String TaskStr = task.toString();
				TCComponentUser user = (TCComponentUser)task.getResponsibleParty();

				String[] userInfo = new String[2];
				userInfo[0] = user.toString().substring(0, user.toString().lastIndexOf("(")-1);
				userInfo[1] = simpleDateFormat.format(task.getDateProperty("last_mod_date"));

				hash.put( TaskStr, userInfo );

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
	@SuppressWarnings("unchecked")
	private void taskInfo(TCComponentTask task ,TCComponentSignoff signoff) {
		try {
			String TaskStr = task.getParent().toString();
			TCComponentUser user = signoff.getGroupMember().getUser();
			String[] userInfo = new String[2];
			userInfo[0] = user.toString().substring(0, user.toString().lastIndexOf("(")-1);
			userInfo[1] = simpleDateFormat.format(signoff.getDecisionDate());

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
	public String getUserInfomation(TCComponentUser imancomponentuser) {
		String user = imancomponentuser.toString().substring(0,imancomponentuser.toString().lastIndexOf('(')-1);
		return user;
	}

	/**
	 * 승인된 날짜 정보 가져오기
	 * @param task 결제정보를 가져올 타스크
	 * @param isCreation 어떤날짜를 가져올자 결정
	 * @return 결제날짜
	 */
	public String getDateInfomation(TCComponentTask task, boolean isCreation) {
		String dateProperty = "";
		if (isCreation) {
			dateProperty = "creation_date";
		} else {
			dateProperty = "last_mod_date";
		}
		try {
			return simpleDateFormat.format(task.getDateProperty(dateProperty));
		} catch (TCException e) {
			return null;
		}
	}

}