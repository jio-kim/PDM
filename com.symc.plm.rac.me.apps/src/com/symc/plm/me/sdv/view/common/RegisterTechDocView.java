package com.symc.plm.me.sdv.view.common;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import com.kgm.commands.ec.SYMCECConstant;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVLOVUtils;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.controls.SWTComboBox;
import com.teamcenter.soa.client.model.LovValue;

@SuppressWarnings({ "unused" })
public class RegisterTechDocView extends AbstractSDVViewPane {
    private static Table tableAttach;
    private Text textDescription;
    private Button btnAdd, btnDelete;
    public Group grpTechDoc, grpAttach;
    public SWTComboBox comboDoc;
    public Combo comboSecret;
    private Table table;
    private static TableItem[] tableItemData;
    private static  File file;
    private static String filePath;
    private static String addFilePath = "";
    private static int itemCount = 0;

    private TCSession tcSession = null;
    
    private IDataMap curDataMap = null;

    private int currentConfigId = 0;

    TCComponentItem targetItem;
    RegisterTechDocView techDocView;

    private static boolean isFileModified = false;

    public boolean isFileModified() {
        return isFileModified;
    }

    /**
     * @wbp.parser.constructor
     */
    public RegisterTechDocView(Composite parent, int style, String id) {
        super(parent, style, id);
        
    }

    public RegisterTechDocView(Composite parent, int style, String id, int configId) {
        super(parent, style, id, configId, null);
    }

    public RegisterTechDocView(Composite parent, int style, String id, int configId, String order) {
        super(parent, style, id, DEFAULT_CONFIG_ID, order);
        currentConfigId = configId;
    }

    /********************************************************************/
    /******************* Attach File ****************************************/
    /********************************************************************/

    private void addFile() {
        FileDialog fileDialog = new FileDialog(tableAttach.getShell(), SWT.OPEN);
        fileDialog.setFilterExtensions(new String[] { "*.xls;", "*.doc;", "*.ppt;", "*.txt", "*.jpg", "*.bmp", "*.*" });
        fileDialog.setFilterNames(new String[] { "MSExcel (*.xls)", "MSWord (*.doc)", "MSPowerPoint (*.ppt)", "Text (*.txt)", "Image (*.jpg)", "Image (*.bmp)", "All Files (*.*)" });
        String name = fileDialog.open();
        if (name == null)
            return;
        File file = new File(name);
        if (!file.exists()) {
            MessageBox.post(tableAttach.getShell(), "File " + file.getName() + " " + " Does_not_exist", "ERROR", MessageBox.ERROR);
            return;
        }
        TableItem item = new TableItem(tableAttach, SWT.NONE);
        item.setText(0, file.getName());
        item.setText(1, CustomUtil.getTCSession().getUserName());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        item.setText(2, dateFormat.format(new Date()));
        item.setData("path", file.getAbsolutePath());
        
        filePath = file.getAbsolutePath();
        
        addPath(filePath);
        
        isFileModified = true;

    }
    
    private void addPath(String filePath) {
        
        
        addFilePath += filePath+ "/";
        
        System.out.println(addFilePath);
    }

    public static void createDatasetAndMakerelation(TCComponentItemRevision revision) throws Exception {
        TCComponent[] references = revision.getRelatedComponents("IMAN_reference");
        for (TCComponent reference : references) {
            if (reference instanceof TCComponentDataset) {
                revision.remove(SYMCECConstant.DATASET_REL, reference);
            }
        }
        
        TableItem[] items = tableAttach.getItems();
        
        int itemCount = items.length;
        for (int i = 0; i < itemCount; i++) {
            SDVBOPUtilities.createService(CustomUtil.getTCSession());
            TCComponentDataset dataSet = SDVBOPUtilities.createDataset((String) items[i].getData("path"));
            revision.add("IMAN_reference", dataSet);
        }

        isFileModified = false;
    }

    public void createDatasetAndMakerelation(TCComponentItem item) throws Exception {
        TCComponent[] references = item.getRelatedComponents("IMAN_reference");
        for (TCComponent reference : references) {
            if (reference instanceof TCComponentDataset) {
                item.remove(SYMCECConstant.ITEM_DATASET_REL, reference);
            }
        }
        
        TableItem[] items = tableAttach.getItems();
        int itemCount = items.length;
        for (int i = 0; i < itemCount; i++) {
            SDVBOPUtilities.createService(CustomUtil.getTCSession());
            TCComponentDataset dataSet = SDVBOPUtilities.createDataset((String) items[i].getData("path"));
            item.add("IMAN_reference", dataSet);
        }

        isFileModified = false;
    }

    private void deleteFile() {
        
        TableItem[] items = tableAttach.getSelection();
        
        itemCount = tableAttach.getSelectionIndex();
        
        if (items.length == 0)
            return;
        items[0].dispose();
        
        deletePath(itemCount);
        
        isFileModified = true;
    }

    private void deletePath(int itemCount) {
        
        String[] splitPath = addFilePath.split("/");
        
        String resultPath = splitPath[itemCount];

        addFilePath = addFilePath.replace(resultPath+"/", "");
        
        
        System.out.println("after delete:"+addFilePath);
    }

    public void resizeTable() {
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableAttach.setLayoutData(layoutData);
    }

    public void roadDataSet(TCComponentItemRevision revision) throws Exception {
        if (tableAttach.getItemCount() > 0)
            tableAttach.removeAll();

        TCComponent[] references = revision.getRelatedComponents("IMAN_reference");
        for (TCComponent reference : references) {
            if (reference instanceof TCComponentDataset) {

                TCComponentDataset dataset = (TCComponentDataset) reference;

                SDVBOPUtilities.createService(CustomUtil.getTCSession());
                File[] files = SDVBOPUtilities.getFiles(dataset);
                for (File file : files) {
                    TableItem item = new TableItem(tableAttach, SWT.NONE);
                    item.setText(0, file.getName());
                    item.setText(1, CustomUtil.getTCSession().getUserName());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    item.setText(2, dateFormat.format(new Date()));
                    item.setData("path", file.getAbsolutePath());
                    System.out.println(file.getAbsolutePath());
                }
            }
        }
    }

    public void roadDataSet(TCComponentItem revision) throws Exception {
        if (tableAttach.getItemCount() > 0)
            tableAttach.removeAll();

        TCComponent[] references = revision.getRelatedComponents("IMAN_reference");
        for (TCComponent reference : references) {
            if (reference instanceof TCComponentDataset) {

                TCComponentDataset dataset = (TCComponentDataset) reference;

                SDVBOPUtilities.createService(CustomUtil.getTCSession());
                File[] files = SDVBOPUtilities.getFiles(dataset);
                for (File file : files) {
                    TableItem item = new TableItem(tableAttach, SWT.NONE);
                    item.setText(0, file.getName());
                    item.setText(1, CustomUtil.getTCSession().getUserName());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    item.setText(2, dateFormat.format(new Date()));
                    item.setData("path", file.getAbsolutePath());
                    System.out.println(file.getAbsolutePath());
                }
            }
        }
    }

    /********************************************************************/
    /******************* Attach File ***************************************/
    /********************************************************************/

    @Override
    protected void initUI(Composite parent) {
        tcSession = SDVBOPUtilities.getTCSession();
        Registry registry = Registry.getRegistry(this);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        grpTechDoc = new Group(composite, SWT.NONE);
        GridData gd_grpTechDoc = new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1);
        gd_grpTechDoc.heightHint = 186;
        gd_grpTechDoc.widthHint = 655;
        grpTechDoc.setLayoutData(gd_grpTechDoc);
        grpTechDoc.setText("Tech Doc.");
        grpTechDoc.setLayout(new GridLayout(1, false));

        Composite groupComposite = new Composite(grpTechDoc, SWT.NONE);
        groupComposite.setLayout(new GridLayout(4, false));
        GridData gd_composite = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_composite.heightHint = 174;
        gd_composite.widthHint = 680;
        groupComposite.setLayoutData(gd_composite);

        Label lblDocType = new Label(groupComposite, SWT.NONE);
        lblDocType.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
        lblDocType.setText("Doc. Type");

        comboDoc = new SWTComboBox(groupComposite, SWT.BORDER);
        comboValueSetting(comboDoc, "M7_TECH_DOC_TYPE");
        GridData gd_comboDoc = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
        gd_comboDoc.widthHint = 80;
        comboDoc.setLayoutData(gd_comboDoc);
        
        
        
        Label lblSecret = new Label(groupComposite, SWT.NONE);
        lblSecret.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, true, 1, 1));
        lblSecret.setText("Secret Level");
        
        comboSecret = new Combo(groupComposite, SWT.BORDER);
        comboSecret.setItems(new String[] {"Secret", "Super-Secret", "Top-Secret"});
        comboSecret.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));

        Label lblDescrtiption = new Label(groupComposite, SWT.NONE);
        lblDescrtiption.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        lblDescrtiption.setText("Description");

        textDescription = new Text(groupComposite, SWT.BORDER);
        GridData gd_textDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
        gd_textDescription.widthHint = 618;
        gd_textDescription.heightHint = 80;
        textDescription.setLayoutData(gd_textDescription);

        grpAttach = new Group(composite, SWT.NONE);
        GridData gd_grpAttach = new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1);
        gd_grpAttach.heightHint = 204;
        gd_grpAttach.widthHint = 690;
        grpAttach.setLayoutData(gd_grpAttach);
        grpAttach.setText("Attachment Doc.");
        grpAttach.setLayout(new GridLayout(3, false));
        // createComposite();

        Label label = new Label(grpAttach, SWT.RIGHT);
        GridData gd_label = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_label.heightHint = -56;
        label.setLayoutData(gd_label);

        btnAdd = new Button(grpAttach, SWT.NONE);
        btnAdd.setText("Add");
        btnAdd.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                addFile();
            }
        });

        btnDelete = new Button(grpAttach, SWT.NONE);
        btnDelete.setText("Delete");
        btnDelete.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                deleteFile();
            }
        });

        tableAttach = new Table(grpAttach, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        tableAttach.setLinesVisible(true);
        tableAttach.setHeaderVisible(true);
        GridData gd_tableAttach = new GridData(SWT.FILL, SWT.CENTER, true, true, 3, 1);
        gd_tableAttach.heightHint = 162;
        gd_tableAttach.minimumHeight = 80;
        tableAttach.setLayoutData(gd_tableAttach);

        TableColumn tblclmn = new TableColumn(tableAttach, SWT.NONE);
        tblclmn.setWidth(363);
        tblclmn.setText("File Name");

        TableColumn tblclmn_1 = new TableColumn(tableAttach, SWT.NONE);
        tblclmn_1.setWidth(150);
        tblclmn_1.setText("Creator");

        TableColumn tblclmn_2 = new TableColumn(tableAttach, SWT.NONE);
        tblclmn_2.setWidth(150);
        tblclmn_2.setText("Creation Date");

        tableItemData = tableAttach.getItems();
    }

    private void comboValueSetting(SWTComboBox combo, String lovName) {
        try {
            if (lovName != null) {

                List<LovValue> lovValues = SDVLOVUtils.getLOVValues(lovName);
                if (lovValues != null) {
                    for (LovValue lov : lovValues) {
                        String desc = lov.getDescription();
                        combo.addItem(desc);
                        // combo.addItem(desc);
                    }
                }
            }
            combo.setAutoCompleteSuggestive(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 작성후 저장된 Data
     * 
     * @method saveData
     * @date 2013. 11. 20.
     * @param
     * @return Map<String,Object>
     * @exception
     * @throws
     * @see
     */
    private IDataMap saveData() {

        RawDataMap savedDataMap = new RawDataMap();
         List<String> dwgNoList = new ArrayList<String>();
         for (TableItem item : tableAttach.getItems()) {
         String itemText = item.getText();
         if (itemText.equals(""))
         continue;
         dwgNoList.add(itemText);
         }
        
        savedDataMap.put(SDVPropertyConstant.M7_TECH_DOC_TYPE, this.comboDoc.getSelectedItem(), IData.STRING_FIELD);
        savedDataMap.put(SDVPropertyConstant.IP_CLASSIFICATION, this.comboSecret.getText());
        savedDataMap.put(SDVPropertyConstant.ITEM_OBJECT_DESC, this.textDescription.getText());
        
        savedDataMap.put("filePath", addFilePath);
        
        addFilePath = "";

        return savedDataMap;
    }

    @Override
    public void setLocalDataMap(IDataMap dataMap) {

    }

    @Override
    public IDataMap getLocalDataMap() {
        curDataMap = saveData();
        return curDataMap;
    }
    

    @Override
    public IDataMap getLocalSelectDataMap() {

        curDataMap = saveData();
        return curDataMap;
    }

    @Override
    public Composite getRootContext() {
        return null;
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return new InitOperation();
    }

    @Override
    public void initalizeData(int result, IViewPane owner, IDataSet dataset) {

    }

    @Override
    public void uiLoadCompleted() {

    }

    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        if (result == SDVInitEvent.INIT_FAILED)
            return;
        if (dataset == null)
            return;
        IDataMap dataMap = dataset.getDataMap(this.getId());
        loadInitData(dataMap);
        
    }

    private void loadInitData(IDataMap paramters) {
        if(paramters == null){
            return;
        }
    }

    /**
     * 초기 Data Load Operation
     * Class Name : InitOperation
     * Class Description :
     * 
     * @date 2013. 12. 3.
     * 
     */
    public class InitOperation extends AbstractSDVInitOperation implements InterfaceAIFOperationListener {

        public InitOperation() {
            addOperationListener(this);
        }

        @Override
        public void executeOperation() throws Exception {
            final IDataMap displayDataMap = new RawDataMap();
            
//            if(!(AIFUtility.getCurrentApplication() instanceof TCComponentItemRevision)){
//                
//            }
            

            
            
            DataSet viewDataSet = new DataSet();
            viewDataSet.addDataMap(RegisterTechDocView.this.getId(), displayDataMap);
            setData(viewDataSet);
        }

        @Override
        public void startOperation(String paramString) {
            
        }
        
        @Override
        public void endOperation() {

        }


    }

}
