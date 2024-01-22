/**
 * 
 */
package com.symc.plm.me.sdv.service.migration.model.tcdata;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.Node;

import com.symc.plm.me.sdv.service.migration.job.peif.DefaultValidationUtil;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.ActivityMasterData;
import com.symc.plm.me.sdv.service.migration.model.tcdata.bop.OperationItemData;
import com.symc.plm.me.utils.BundleUtil;
import com.teamcenter.rac.kernel.TCComponentBOMLine;

/**
 * Class Name : TCData
 * Class Description :
 * 
 * @date 2013. 11. 14.
 * 
 */
public abstract class TCData extends TreeItem {

    public static final String TC_TYPE_CLASS_NAME_SHOP = "SHOP"; // SHOP CLASS TYPE
    public static final String TC_TYPE_CLASS_NAME_LINE = "LINE"; // LINE CLASS TYPE
    public static final String TC_TYPE_CLASS_NAME_OPERATION = "OPERATION"; // 공법 CLASS TYPE
    public static final String TC_TYPE_CLASS_NAME_ACTIVITY = "ACTIVITY";
    public static final String TC_TYPE_CLASS_NAME_ACTIVITY_SUB = "ACTIVITY_SUB";
    public static final String TC_TYPE_CLASS_NAME_TOOL = "TOOL";
    public static final String TC_TYPE_CLASS_NAME_EQUIPMENT = "EQUIPMENT";
	public static final String TC_TYPE_CLASS_NAME_END_ITEM = "END_ITEM";
	public static final String TC_TYPE_CLASS_NAME_SUBSIDIARY = "SUBSIDIARY";

    // STATUS
    public static final int STATUS_STANDBY = 0;
    public static final int STATUS_INPROGRESS = 1;
    public static final int STATUS_VALIDATE_COMPLETED = 2;
    public static final int STATUS_EXECUTE_COMPLETED = 3;
    public static final int STATUS_COMPLETED = 4;
    public static final int STATUS_ERROR = 5;
    public static final int STATUS_WARNING = 6;
    public static final int STATUS_SKIP = 7;
    public static final String[] STATUS_STR = new String[] { "STANDBY", "INPROGRESS", "VALIDATE_COMPLETED", "EXECUTE_COMPLETED", "COMPLETED", "ERROR", "WARNING", "SKIP" };
    
    private String[] changeInformationFlags;
    
    // DecidedChagneType
    public static final int DECIDED_NO_CHANGE = 0;
    public static final int DECIDED_ADD = 1;
    public static final int DECIDED_REMOVE = 2;
    public static final int DECIDED_REVISE = 3;
    public static final int DECIDED_REPLACE = 4;
    public static final String[] CHANGE_TYPE_STR = new String[] { "NO_CHANGE", "ADD", "REMOVE", "REVISE", "REPLACE"};
    private int decidedChagneType = 0;		// Validation 결과 변경 형태를 기록하는 변수 기본값은 No Change 임.

	// Item Class Type (Item(Item+revision) or Dataset)
    protected String classType;
    // TreeItem level
    protected int nLevel = -1;
    // 해당 TreeItem의 상태값
    protected int nStatus = 0;
    // 상태 메세지
    protected String statusMassage;

    // Tree Colums
    protected TreeColumn[] columns;
    
    protected TCComponentBOMLine bopBomLine;
    protected Node bomLineNode;
	protected Node masterDataNode;
	
	private boolean haveMajorError = false;

	public TCData(Tree parentTree, int index, String classType, TreeColumn[] columns) {
        super(parentTree, SWT.NONE, index);
        init(parentTree, classType, columns);
    }

    public TCData(TCData parentItem, int index, String classType, TreeColumn[] columns) {
        super(parentItem, SWT.NONE, index);
        init(parentItem, classType, columns);
    }

    protected void init(Widget parentTreeObject, String classType, TreeColumn[] columns) {
    	
    	changeInformationFlags = new String[]{"*", "*", "*", "*", "*", "*", "*", "*", "*"};
    	
        this.classType = classType;
        this.columns = columns;
        if (parentTreeObject instanceof TreeItem) {
            ((TreeItem) parentTreeObject).setExpanded(true);
        }
        setClassImage();
        this.setStatus(STATUS_STANDBY);
    }

    protected abstract void setClassImage();

    /**
     * @return the classType
     */
    public String getClassType() {
        return classType;
    }

    /**
     * Level Setter
     */
    public void setLevel(int nLevel) {
        this.nLevel = nLevel;
    }

    /**
     * Level Getter
     */
    public int getLevel() {
        return this.nLevel;
    }

    public void setStatus(int nStatus) {
        setStatus(nStatus, BundleUtil.nullToString(statusMassage));
    }

    /**
     * Tree Status
     * 
     * @method getStatus
     * @date 2013. 11. 26.
     * @param
     * @return int
     * @exception
     * @throws
     * @see
     */
    public int getStatus() {
        return nStatus;
    }

    /**
     * Status Setter
     * 
     * @param nStatus
     */
    public void setStatus(int nStatus, String statusMassage) {
        this.nStatus = nStatus;
        this.statusMassage = statusMassage;
        int statusColumnIndex = columns.length - 1;
        // 상태문자 추가
        statusMassage = "[" + getStatusStr() + "] " + statusMassage;
        this.setText(statusColumnIndex, statusMassage);
        // Error 인경우 Background 설정
        if (this.nStatus == STATUS_ERROR) {
            this.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_RED));
        } else if (this.nStatus == STATUS_WARNING) {
            this.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
        } else if (this.nStatus == STATUS_VALIDATE_COMPLETED) {
            this.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        } else if (this.nStatus == STATUS_EXECUTE_COMPLETED) {
            this.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
        } else if (this.nStatus == STATUS_SKIP) {
            this.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
        } else {
            this.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        }
    }

    /**
     * Sync 상태 메세지를 가져온다.
     * 
     * @method getStatusMessage
     * @date 2013. 11. 26.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String getSyncStatusMessage(Shell shell) {
        final ArrayList<String> message = new ArrayList<String>();
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                message.add(getStatusMessage());
            }
        });
        if (message.size() > 0) {
            return message.get(0);
        } else {
            return "";
        }
    }

    /**
     * Sync getText
     * 
     * @method getSyncText
     * @date 2013. 11. 26.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String getSyncText(Shell shell) {
        final ArrayList<String> text = new ArrayList<String>();
        shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                text.add(getText());
            }
        });
        if (text.size() > 0) {
            return text.get(0);
        } else {
            return "";
        }

    }

    /**
     * 상태 메세지를 가져온다.
     * 
     * @method getStatusMessage
     * @date 2013. 11. 26.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String getStatusMessage() {
        int statusColumnIndex = columns.length - 1;
        return this.getText(statusColumnIndex);
    }

    /**
     * 상태 코드를 가지고 상태 문자를 가져온다.
     * 
     * @method getStatusStr
     * @date 2013. 11. 27.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String getStatusStr() {
        return STATUS_STR[nStatus];
    }

    /**
     * 필수 Override Method
     */
    @Override
    protected void checkSubclass() {
    }
    
    public void setValidationResult(DefaultValidationUtil validationUtil){
    	
    	int validationResultChangeType = validationUtil.getValidationResultChangeType();
    	if(validationResultChangeType==TCData.DECIDED_NO_CHANGE){
    		TreeItem parentItem = getParentItem();
    		if(parentItem instanceof OperationItemData){
    			OperationItemData tempOperationItemData = (OperationItemData)parentItem;
    			// 이유여하를 막론하고 Parent Operatoin이 추가된 경우 이면
    			// Child Node는 모두 추가로 처리 한다.
    			if(tempOperationItemData.getDecidedChagneType()==TCData.DECIDED_ADD){
    				setDecidedChagneType(TCData.DECIDED_ADD);	
    			}
    		}else if(parentItem instanceof ActivityMasterData){
    			// 이유여하를 막론하고 Parent Activity Master가 추가된 경우 이면
    			// Child Node는 모두 추가로 처리 한다.
    			ActivityMasterData tempActivityMasterData = (ActivityMasterData)parentItem;
    			if(tempActivityMasterData.getDecidedChagneType()==TCData.DECIDED_ADD){
    				setDecidedChagneType(TCData.DECIDED_ADD);	
    			}    				
    		}    		
    	}else{
    		setDecidedChagneType(validationResultChangeType);
    	}

    	boolean isBOMLineAttChanged = validationUtil.isBOMAttributeChanged();
    	setBOMAttributeChangeFlag(isBOMLineAttChanged);

    	// End Item의 경우 Master Data에 대한 편집을 하지 않으므로 관리 대상이 아님.
    	String currentClassType = getClassType();
    	if(currentClassType!=null){
    		if(currentClassType.trim().equalsIgnoreCase(TCData.TC_TYPE_CLASS_NAME_END_ITEM)==false){
    			boolean isAttChanged = validationUtil.isMasterDataChanged();
    			setAttributeChangeFlag(isAttChanged);
    		}
    	}
    	
    	setHaveMajorError(validationUtil.haveMajorError);
    	
    	updateChangeStatusText();


    }

    /**
     * 현재 Node의 Attribute 변경 여부를 changeInformationFlags에 기록 한다.
     * @param isChanged 변경된 경우 true를 입력한다.
     */
    public void setAttributeChangeFlag(boolean isChanged){
    	
    	if(isChanged==true){
    		changeInformationFlags[0] = "A";
    	}else{
    		changeInformationFlags[0] = "*";
    	}
    	updateChangeStatusText();
    	setParentFlags();
    }
    
    public boolean getAttributeChangeFlag(){
    	
    	boolean isSet = false;
    	if(changeInformationFlags[0].trim().equalsIgnoreCase("A")==true){
    		isSet = true;
    	}
    	
    	return isSet;
    }

    /**
     * 현재 Node의 BOMLine Attribute가 변경되었는지 기록 한다.
     * @param isAdded
     */
    public void setBOMAttributeChangeFlag(boolean isAdded){
    	
    	if(isAdded==true){
    		changeInformationFlags[1] = "BA";
    	}else{
    		changeInformationFlags[1] = "*";
    	}
    	updateChangeStatusText();
    	setParentFlags();
    }
    
    public boolean getBOMAttributeChangeFlag(){
    	
    	boolean isSet = false;
    	if(changeInformationFlags[1].trim().equalsIgnoreCase("BA")==true){
    		isSet = true;
    	}
    	
    	return isSet;
    }
    
    /**
	 * Validation 결과로 결정된 변경 Type의 값을 설정하는 함수
	 * @param decidedChagneType
	 */
	public void setDecidedChagneType(int decidedChagneType) {
		
		this.decidedChagneType = decidedChagneType;
		
		if(decidedChagneType==TCData.DECIDED_NO_CHANGE){
			changeInformationFlags[2] = "*";
		}else if(decidedChagneType==TCData.DECIDED_ADD){
			changeInformationFlags[2] = "+";
		}else if(decidedChagneType==TCData.DECIDED_REMOVE){
			changeInformationFlags[2] = "-";
		}else if(decidedChagneType==TCData.DECIDED_REPLACE){
			changeInformationFlags[2] = "C";
		}else if(decidedChagneType==TCData.DECIDED_REVISE){
			changeInformationFlags[2] = "R";
		}
		updateChangeStatusText();
		
		TreeItem parentTreeItem = getParentItem();
		if(parentTreeItem!=null){
			if(decidedChagneType==DECIDED_ADD){
				((TCData)parentTreeItem).setChildNodeAddedFlag(true);
			} else if(decidedChagneType==DECIDED_REMOVE){
				((TCData)parentTreeItem).setChildNodeRemovedFlag(true);
			} else if(decidedChagneType==DECIDED_REVISE){
				((TCData)parentTreeItem).setChildNodeRevisedFlag(true);
			} else if(decidedChagneType==DECIDED_REPLACE){
				((TCData)parentTreeItem).setChildNodeReplacedFlag(true);
			}
			
			((TCData)parentTreeItem).updateChangeStatusText();
			setParentFlags();
		}
	
	}

	/**
	 * Validation 결과로 결정된 변경 Type의 값을 읽어 오는 함수
	 * @return
	 */
	public int getDecidedChagneType() {
		return decidedChagneType;
	}

	/**
     * 현재 Node의 Child Node가 추가된 것이 있는지 changeInformationFlags에 기록 한다.
     * @param isAdded
     */
    public void setChildNodeAddedFlag(boolean isAdded){
    	
    	if(isAdded==true){
    		changeInformationFlags[3] = "+";
    	}else{
    		changeInformationFlags[3] = "*";
    	}
    	updateChangeStatusText();
    }
    
    public boolean getChildNodeAddedFlag(){
    	
    	boolean isSet = false;
    	if(changeInformationFlags[3].trim().equalsIgnoreCase("+")==true){
    		isSet = true;
    	}
    	
    	return isSet;
    }
    
    /**
     * 현재 Node의 Child Node가 제거된 것이 있는지 changeInformationFlags에 기록 한다.
     * @param isRemoved
     */
    public void setChildNodeRemovedFlag(boolean isRemoved){
    	
    	if(isRemoved==true){
    		changeInformationFlags[4] = "-";
    	}else{
    		changeInformationFlags[4] = "*";
    	}
    	updateChangeStatusText();
   }
    
    public boolean getChildNodeRemovedFlag(){
    	
    	boolean isSet = false;
    	if(changeInformationFlags[4].trim().equalsIgnoreCase("-")==true){
    		isSet = true;
    	}
    	
    	return isSet;
    }
    
    /**
     * 현재 Node의 Child Node가 Replace된 것이 있는지 changeInformationFlags에 기록 한다.
     * @param isReplaced
     */
    public void setChildNodeReplacedFlag(boolean isReplaced){
    	
    	if(isReplaced==true){
    		changeInformationFlags[5] = "C";
    	}else{
    		changeInformationFlags[5] = "*";
    	}
    	updateChangeStatusText();
    }
    
    public boolean getChildNodeReplacedFlag(){
    	
    	boolean isSet = false;
    	if(changeInformationFlags[5].trim().equalsIgnoreCase("C")==true){
    		isSet = true;
    	}
    	
    	return isSet;
    }
    
    /**
     * 현재 Node의 Child Node가 Revise된 것이 있는지 changeInformationFlags에 기록 한다.
     * @param isRevised
     */
    public void setChildNodeRevisedFlag(boolean isRevised){
    	
    	if(isRevised==true){
    		changeInformationFlags[6] = "R";
    	}else{
    		changeInformationFlags[6] = "*";
    	}
    	updateChangeStatusText();
    }
    
    public boolean getChildNodeRevisedFlag(){

    	boolean isSet = false;
    	if(changeInformationFlags[6].trim().equalsIgnoreCase("R")==true){
    		isSet = true;
    	}
    	
    	return isSet;
    }
    
    /**
     * 현재 Node의 Child Node의 BOMLine Attribute 변경 여부를 기록 한다.
     * @param isChildBOMLineChanged
     */
    public void setChildAttributeChangedFlag(boolean isChildBOMLineChanged){
    	
    	if(isChildBOMLineChanged==true){
    		changeInformationFlags[7] = "A";
    	}else{
    		changeInformationFlags[7] = "*";
    	}
    	updateChangeStatusText();
    }
    
    public boolean getChildAttributeChangedFlag(){

    	boolean isSet = false;
    	if(changeInformationFlags[7].trim().equalsIgnoreCase("A")==true){
    		isSet = true;
    	}
    	
    	return isSet;
    }
    
    /**
     * 현재 Node의 Child Node의 BOMLine Attribute 변경 여부를 기록 한다.
     * @param isChildBOMLineChanged
     */
    public void setChildBOMLineChangedFlag(boolean isChildBOMLineChanged){
    	
    	if(isChildBOMLineChanged==true){
    		changeInformationFlags[8] = "CB";
    	}else{
    		changeInformationFlags[8] = "*";
    	}
    	updateChangeStatusText();
    }
    
    public boolean getChildBOMLineChangedFlag(){

    	boolean isSet = false;
    	if(changeInformationFlags[8].trim().equalsIgnoreCase("CB")==true){
    		isSet = true;
    	}
    	
    	return isSet;
    }
    
    /**
     * 현재 Node의 Change Flag에 변화가 있을때 변경된 Flag의 내용을 Parent Node의 Change Flag에 반영한다.
     * 
     * 이 함수는 현재 Node의 변경으로 인해 상위 Node에기록될 내용이 있으면 함께 기록하는 기능이 수행되므로 선택적인 재귀 호출이 되는 형태이다.
     * 이렇게 하는 이유는 Tc Structure Data와 N/F(Neutral Format Data) Tree Node를 전개 하면서 Validation 된 내용을 이용해 Interface 처리를 수행 할때
     * 개정및 변경처리의 편의성을 가지기 위함이다. 
     */
    private void setParentFlags(){
    	
    	TCData parentTCData =null;
    	if(getParentItem() == null || (getParentItem()==null && (getParentItem() instanceof TCData)==false) ){
    		return;
    	}else{
    		parentTCData = (TCData)getParentItem();
    	}    	
    	
    	String currentClassType = getClassType();
    	
    	boolean isAttributeChenged = getAttributeChangeFlag();						// 0
    	boolean isBOMAttributeChenged = getBOMAttributeChangeFlag();		// 1
		int parentChangeType = parentTCData.getDecidedChagneType();
    	
    	if(isAttributeChenged==true){

    		//--------------------------------------------------------------------------------------
    		// Attribute가 바뀐다는것은 곳 Current Node가 개정 된다는것을 의미 한다.
    		//--------------------------------------------------------------------------------------
    		parentTCData.setChildAttributeChangedFlag(true);
    		
	    	if(currentClassType.trim().equalsIgnoreCase(TCData.TC_TYPE_CLASS_NAME_ACTIVITY_SUB)){
	    		// 쌍용의 개정 Rule에 의해 Sub Activity가 추가 되거나 변경되면 Activity Root 부터 새로 구성한다. 
	    		parentTCData.setChildNodeRemovedFlag(true);
	    	}else if(currentClassType.trim().equalsIgnoreCase(TCData.TC_TYPE_CLASS_NAME_ACTIVITY)){
	    		// 쌍용의 개정 Rule에 의해 Sub Activity가 추가 되거나 변경되면 Activity Root 부터 새로 구성하므로 기존의 Activity Root Node를 제거해야한다.
	    		parentTCData.setChildNodeRemovedFlag(true);
	    	}
	    	
	    	int currentChangeType = getDecidedChagneType();
	    	if(currentChangeType == TCData.DECIDED_NO_CHANGE){
	    		setDecidedChagneType(TCData.DECIDED_REVISE);
	    	}
    	}
    	
    	// 현재 Node의 BOMLine Attribute가 변경되면 Parent Node에 Child Node의 BOMLine Attribute가 변경되었음을
    	// 기록하도록 한다.
    	if(isBOMAttributeChenged==true){
   			parentTCData.setChildBOMLineChangedFlag(true);
   			
   			if(parentTCData.classType == TCData.TC_TYPE_CLASS_NAME_OPERATION){
   				parentChangeType = parentTCData.getDecidedChagneType();
   	       		if(parentChangeType == TCData.DECIDED_NO_CHANGE){
   	       			parentTCData.setDecidedChagneType(DECIDED_REVISE);
   	       		}
   			}
    	}

    	parentChangeType = parentTCData.getDecidedChagneType();

    	// Parent Type 구분없이 공통으로 적용될 내용 
		if(decidedChagneType==TCData.DECIDED_NO_CHANGE){
			
		}else if(decidedChagneType==TCData.DECIDED_ADD){
			parentTCData.setChildNodeAddedFlag(true);
			if(parentChangeType == TCData.DECIDED_NO_CHANGE){
				parentTCData.setDecidedChagneType(DECIDED_REVISE);
			}    
		}else if(decidedChagneType==TCData.DECIDED_REMOVE){
			parentTCData.setChildNodeRemovedFlag(true);
			if(parentChangeType == TCData.DECIDED_NO_CHANGE){
				parentTCData.setDecidedChagneType(DECIDED_REVISE);
			}   
		}else if(decidedChagneType==TCData.DECIDED_REPLACE){
			parentTCData.setChildNodeReplacedFlag(true);
			if(parentChangeType == TCData.DECIDED_NO_CHANGE){
				parentTCData.setDecidedChagneType(DECIDED_REVISE);
			}   
		}else if(decidedChagneType==TCData.DECIDED_REVISE){
			parentTCData.setChildNodeRevisedFlag(true);
		}
    	
		// Parent가 Operation인 경우 적용될 내용
    	if(parentTCData.getClassType() == TCData.TC_TYPE_CLASS_NAME_OPERATION){
    		if(decidedChagneType==TCData.DECIDED_NO_CHANGE){
    		}else if(decidedChagneType==TCData.DECIDED_ADD){
    		}else if(decidedChagneType==TCData.DECIDED_REMOVE){
    		}else if(decidedChagneType==TCData.DECIDED_REPLACE){
    		}else if(decidedChagneType==TCData.DECIDED_REVISE){
   	       		if(parentChangeType == TCData.DECIDED_NO_CHANGE){
   	       			parentTCData.setDecidedChagneType(DECIDED_REVISE);
   	       		}
    		}
    	}
       	
       	if(parentTCData!=null){
       		parentTCData.updateChangeStatusText();
       	}

    }
    
    /**
     * Tree Node의 Validation을 진행 하는 과정에 Validation 결과를 Tree Node에 표시되는 Text에 반영해주는 함수임.
     * Attribute가 변경되거나 ChildNode가 추가, 제거, 변경, 개정된 상태를 문자로 표시되는데 이것이 변경 되도록 한다.
     */
	public void updateChangeStatusText() {
		
		String titleStrings = getFirstText((String[])null);
		String newString	 = titleStrings.trim() + " "+ getChangeStatusText();
		
		super.setText(newString);
	}

	@Override
	public void setText(String[] strings) {
		
		String titleStrings = null;
			titleStrings = getFirstText(strings);
		
		if(strings!=null && strings.length>=1){
			strings[0] = titleStrings.trim() + " "+ getChangeStatusText();
		}
		
		super.setText(strings);
	}
	
	
    
    @Override
	public void setText(int index, String string) {
    	if(index==0){
    		String titleString = null;
			titleString = getFirstText(new String[]{string});
		
			if(titleString!=null && titleString.length()>=1){
				string = titleString.trim() + " "+ getChangeStatusText();
			}
    	}
		super.setText(index, string);
	}

	@Override
	public void setText(String string) {
		
		String titleString = null;
		titleString = getFirstText(new String[]{string});
	
		if(titleString!=null && titleString.length()>=1){
			string = titleString.trim() + " "+ getChangeStatusText();
		}
		
		super.setText(string);
	}

	private String getFirstText(String[] strings){
    	
		String titleString = null;
		
		if(strings!=null && strings.length>0 && strings[0]!=null){
			titleString = strings[0].trim();
		}else{
			String tempStr = getText(0);
			if(tempStr!=null && tempStr.trim().length()>0){
				titleString = tempStr.trim(); 
			}
		}
		
		String tempStringA = null;
		
		if(titleString!=null){
			tempStringA = titleString.trim();
		}
		
		int keyIndex = tempStringA.trim().lastIndexOf("[");
		
		if(keyIndex>-1){
			tempStringA = titleString.trim().substring(0, (keyIndex-1));
			
			if(tempStringA!=null){
				tempStringA= tempStringA.trim();
			}else{
				tempStringA = "";
			}
		}
		
		return tempStringA;
    }
    
    private String getChangeStatusText(){
    	
    	String changeStatusStr = "["
    			+changeInformationFlags[0]+" "
    			+changeInformationFlags[1]+" "
    			+changeInformationFlags[2]+" "
    			+changeInformationFlags[3]+" "
    			+changeInformationFlags[4]+" "
    			+changeInformationFlags[5]+" "
    			+changeInformationFlags[6]+" "
    			+changeInformationFlags[7]+" "
    			+changeInformationFlags[8]+"]";
    	
    	return changeStatusStr;
    }
    
    /**
     * @return the bopBomLine
     */
    public TCComponentBOMLine getBopBomLine() {
        return bopBomLine;
    }

    /**
     * @param bopBomLine
     *            the bomLine to set
     */
    public void setBopBomLine(TCComponentBOMLine bopBomLine) {
        this.bopBomLine = bopBomLine;
    }
    
    public Node getBomLineNode() {
		return bomLineNode;
	}

	public void setBomLineNode(Node bomLineNode) {
		this.bomLineNode = bomLineNode;
	}

	public Node getMasterDataNode() {
		return masterDataNode;
	}

	public void setMasterDataNode(Node masterDataNode) {
		this.masterDataNode = masterDataNode;
	}
    
    public boolean isHaveMajorError() {
		return haveMajorError;
	}

	public void setHaveMajorError(boolean haveMajorError) {
		this.haveMajorError = haveMajorError;
		if(this.haveMajorError==true){
			setStatus(TCData.STATUS_ERROR);
		}
	}
}
