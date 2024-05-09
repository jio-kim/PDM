package com.kgm.commands.ec.eco;

import java.net.ConnectException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.kgm.Activator;
import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.kgm.common.utils.StringUtil;
import com.kgm.common.utils.TcDefinition;
import com.kgm.rac.kernel.SYMCECODwgData;
import com.kgm.viewer.AbstractSYMCViewer;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.IPageComplete;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 *  [SR180130-033][LJG]
 *  1. E-BOM Part Master(Eng. Info) 중 "Responsibility" => "DWG Creator" 로 변경
 *  2. Responsibility Filed 내 LOV 값 추가 : Supplier, Collaboration, SYMC
 *  3. 신규 part 생성 시 기존 LOV Black BOX, Gray Box, White Box 선택불가 처리
 *  4. Revision Up 시 기존 Responsibiliy 값 삭제 => 설계 재지정하도록 처리
 */
public class ECODWGSWTRendering extends AbstractSYMCViewer implements IPageComplete {

    private Composite composite;
    private Button downloadButton;
    private Button checkAllButton;
    private Button check2DButton;
    private Button check3DButton;
    private Button checkSoftButton;
    private String downPath;
    
    private Table table;
    private Color modifiableColor;
    private Color modifiedColor;
    private ArrayList<TableItem> modifiedTableItems;
    private static final int CHANGE_DESC = 13;

    private Composite editComp;
    
    public ECODWGSWTRendering(Composite parent) {
        super(parent);
    }

    @Override
    public void createPanel(Composite parent) {
        Display display = getDisplay();
        modifiedColor = new Color(display, 255, 225, 225);
        modifiableColor = new Color(display, 218, 237, 190);
        
        composite = new Composite(parent, SWT.None);
        GridLayout gl = new GridLayout(1, false);
        composite.setLayout(gl);
        
        this.initDownloadControl();
        this.initTableControl();
        this.initEditor(composite);
    }

    /**
     * Download Button Component
     * 
     * @method downloadComponent
     * @date 2013. 2. 19.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void initDownloadControl() {
        Composite buttonsComposite = new Composite(composite, SWT.NONE);
        GridLayout buttonGl = new GridLayout(6, false);
        buttonsComposite.setLayout(buttonGl);
        GridData gdButtons = new GridData(SWT.NONE);
        gdButtons.grabExcessHorizontalSpace = true;
        checkAllButton = new Button(buttonsComposite, SWT.CHECK);
        checkAllButton.setVisible(true);
        Label emptyLabel = new Label(buttonsComposite, SWT.CENTER);
        emptyLabel.setText("Select ALL     ");
        check2DButton = new Button(buttonsComposite, SWT.CHECK);
        check2DButton.setText("2D");
        check2DButton.setSelection(true);
        check3DButton = new Button(buttonsComposite, SWT.CHECK);
        check3DButton.setText("3D");
        checkSoftButton = new Button(buttonsComposite, SWT.CHECK);
        checkSoftButton.setText("Software");
        check3DButton.setSelection(true);
        downloadButton = new Button(buttonsComposite, SWT.NONE);
        downloadButton.setText("Download");
        downloadButton.setImage(Activator.imageDescriptorFromPlugin("com.teamcenter.rac.common", "icons/save_16.png").createImage());
        downloadButton.setVisible(true);
        this.addButtonsListener();
    }

    /**
     * 버튼 이벤트 Listener
     * 
     * @method addButtonsListener
     * @date 2013. 2. 20.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void addButtonsListener() {
        // Check All Button
        this.checkAllButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                final boolean checked = ((Button) e.getSource()).getSelection();
                new Job("Check All Button Job") {
                    @Override
                    protected IStatus run(IProgressMonitor arg0) {
                        tableAllCheck(checked);
                        return Status.OK_STATUS;
                    }
                }.schedule();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        // Download Button
        this.downloadButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                new Job("Download Button Job") {
                    @Override
                    protected IStatus run(IProgressMonitor arg0) {
                        
                        final ArrayList<TCComponentItemRevision> downTargetRevs = new ArrayList<TCComponentItemRevision>();
                        @SuppressWarnings("unchecked")
                        final ArrayList<String> dsFilterTypes = (ArrayList<String>)TcDefinition.CAT_DOWN_FILTER_TYPE.clone();
                        final TCComponent ecoRev = (TCComponent)AIFUtility.getCurrentApplication().getTargetComponent();
                        
                        composite.getDisplay().syncExec(new Runnable() {
                            public void run() {
                                TableItem[] checkItems = table.getSelection();
                                if(checkItems == null || checkItems.length == 0) {
                                    MessageBox.post(getShell(), "Select Table Item for Download.", "Download", MessageBox.INFORMATION);
                                    return;
                                }
                                DirectoryDialog directoryDialog = new DirectoryDialog(composite.getShell());
                                downPath = directoryDialog.open();
                                if (downPath == null) {
                                    return;
                                }
                                setCursor(new Cursor(composite.getShell().getDisplay(), SWT.CURSOR_WAIT));
                                boolean select3D = check3DButton.getSelection();
                                boolean select2D = check2DButton.getSelection();                                
                                if(!checkSoftButton.getSelection()) {
                                    dsFilterTypes.remove("Zip");
                                }
                                if(!select2D) {
                                    dsFilterTypes.remove("CATDrawing");
                                }
                                if(!select3D) {
                                    dsFilterTypes.remove("CATPart");
                                    dsFilterTypes.remove("CATProduct");
                                }
                                if(!select2D && !select3D) {
                                    dsFilterTypes.remove("catia");
                                }
                                TCSession session = ecoRev.getSession();
                                for (TableItem item : checkItems) {
                                    try {
                                        downTargetRevs.add((TCComponentItemRevision)session.stringToComponent(((SYMCECODwgData)item.getData()).getRevUid()));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                        
                        if(downPath == null) {
                            return Status.CANCEL_STATUS;
                        }
                        for(TCComponentItemRevision rev : downTargetRevs) {
                            try {
                                SYMTcUtil.getDatasetFiles(rev, dsFilterTypes, downPath, ecoRev.getDateProperty("date_released"));
                            } catch (final Exception e) {
                                e.printStackTrace();
                                composite.getDisplay().syncExec(new Runnable() {
                                    public void run() {
                                        MessageBox.post(composite.getShell(), e.getMessage(), "Download", MessageBox.ERROR);
                                    }
                                });
                            }
                        }
                        
                        composite.getDisplay().syncExec(new Runnable() {
                            public void run() {
                                MessageBox.post(composite.getShell(), "Download completed!", "Download", MessageBox.INFORMATION);
                                setCursor(new Cursor(composite.getShell().getDisplay(), SWT.CURSOR_ARROW));
                            }
                        });
                        return Status.OK_STATUS;
                    }
                }.schedule();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
    }

    /**
     * Table Component
     * 
     * @method initTableControl
     * @date 2013. 2. 19.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void initTableControl() {
        GridData gdTable = new GridData(SWT.FILL, SWT.FILL, true, true);
        gdTable.grabExcessHorizontalSpace = true;
        table = new Table(composite, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        createTableColumn("No", 50);
        createTableColumn("Proj.", 50);
        createTableColumn("Model Type", 100);
        createTableColumn("Part Origin", 25);
        createTableColumn("Part No", 120);
        createTableColumn("Revision No", 80);
        createTableColumn("Part Name", 240);
        createTableColumn("DWG Creator", 85); //[SR180130-033][LJG] "Responsibility" => "DWG Creator" 로 변경
        createTableColumn("CAT Product", 80);
        createTableColumn("2D", 55);
        createTableColumn("3D", 40);
        createTableColumn("ZIP", 40);
        createTableColumn("S/Mode", 80);
        createTableColumn("Change Desc", 180);
        // Table Listener 추가
        this.addTableListener();
        table.setLayoutData(gdTable);
        // 테이블 전체 체크
        this.tableAllCheck(true);
    }
    
    /**
     * Table Listsner
     * 
     * @method addTableListener
     * @date 2013. 2. 19.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void addTableListener() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {                
                if(isEditable() && e.button == 1) {
                    processTableEdit(e);
                }
            }
        });      
    }

    /**
     * Cell Edit 편집
     * 
     * @method addCellEditEvent
     * @date 2013. 3. 6.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void processTableEdit(MouseEvent event) {
        final TableEditor editor = new TableEditor(table);
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;
        Point pt = new Point(event.x, event.y);
        int row = table.getSelectionIndex();
        if(row == -1) {
            return;
        }
        final TableItem item = table.getItem(row);
        Rectangle rect = item.getBounds(CHANGE_DESC);
        if (rect.contains(pt)) {
            Scrollable cellComponent = getCellComponent(item, CHANGE_DESC);
            if (cellComponent == null) {
                return;
            } else if (cellComponent instanceof Text) {
                final Text rendererComponent = (Text) cellComponent;
                Listener componentListener = new Listener() {
                    public void handleEvent(final Event e) {
                        switch (e.type) {
                        case SWT.FocusOut:
                            setDescriptionValue(item, rendererComponent.getText());
                            rendererComponent.dispose();
                            break;
                        case SWT.Traverse:
                            switch (e.detail) {
                            case SWT.TRAVERSE_RETURN:
                                setDescriptionValue(item, rendererComponent.getText());
                            case SWT.TRAVERSE_ESCAPE:
                                rendererComponent.dispose();
                                e.doit = false;
                            }
                            break;
                        }
                    }
                };
                rendererComponent.addListener(SWT.FocusOut, componentListener);
                rendererComponent.addListener(SWT.Traverse, componentListener);
                editor.setEditor(rendererComponent, item, CHANGE_DESC);
                rendererComponent.setText(item.getText(CHANGE_DESC));
                //rendererComponent.selectAll();
                rendererComponent.setFocus();
            }
            return;
        }
    }

    /**
     * Cell 편집 속성 제어
     * 
     * @method getCellComponent
     * @date 2013. 3. 5.
     * @param
     * @return Scrollable
     * @exception
     * @throws
     * @see
     */
    public Scrollable getCellComponent(TableItem item, int colunIndex) {
        switch (colunIndex) {
        case CHANGE_DESC:
            return new Text(table, SWT.NONE);
        default:
            return null;
        }
    }

    /**
     * Cell Component 변경 후 속성 업데이트
     * 
     * @method setItemDataValue
     * @date 2013. 3. 5.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void setDescriptionValue(TableItem item, String value) {
        value = StringUtil.nullToString(value);
        item.setText(CHANGE_DESC, value); // Set Text
        SYMCECODwgData editData = (SYMCECODwgData) item.getData();
        if(value.equals(editData.getChangeDesc())) {
            item.setBackground(CHANGE_DESC, modifiableColor);
            modifiedTableItems.remove(item);
        } else {
            item.setBackground(CHANGE_DESC, modifiedColor);
            modifiedTableItems.add(item);
        }
    }

    /**
     * 편집 가능한 Cell Color 설정
     * 
     * @method setModifiableCellColor
     * @date 2013. 3. 5.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setModifiableCellColor(TableItem item, int colIndex) {
        if(isEditable()) {
            item.setBackground(colIndex, modifiableColor);
        } else {
            item.setBackground(colIndex, table.getBackground());
        }
    }

    /**
     * 컬럼 생성
     * 
     * @method createTableColumn
     * @date 2013. 2. 19.
     * @param
     * @return TableColumn
     * @exception
     * @throws
     * @see
     */
    private TableColumn createTableColumn(String columnName, int width) {
        TableColumn column = new TableColumn(table, SWT.CENTER);
        column.setText(columnName);
        column.setWidth(width);
        column.setResizable(true);
        column.setMoveable(true);
        return column;
    }

    /**
     * 테이블 전체 체크
     * 
     * @method tableAllCheckEable
     * @date 2013. 2. 19.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void tableAllCheck(boolean checked) {
        final boolean allChecked = checked;
        composite.getDisplay().syncExec(new Runnable() {
            public void run() {
                if (allChecked) {
                    table.selectAll();
                } else {
                    table.deselectAll();
                }
            }
        });
    }
    
    /**
     * [SR141006-015][2014.11.11][jclee] ECO B지 Multi 입력 기능 추가
     */
    private void initEditor(Composite parent) {
        Registry registry = Registry.getRegistry("com.kgm.common.common");

        editComp = new Composite(parent, SWT.None);
        GridData gdEdit = new GridData(SWT.LEAD, SWT.CENTER, true, false);
        editComp.setLayoutData(gdEdit);
        GridLayout layout = new GridLayout(4, false);
        editComp.setLayout(layout);
        
        GridData gdLabel = new GridData(SWT.END, SWT.CENTER, false, false);
        gdLabel.widthHint = 80;
        Label lDesc = new Label(editComp, SWT.None | SWT.RIGHT_TO_LEFT);
        lDesc.setText("Change Desc");
        lDesc.setLayoutData(gdLabel);
        
        GridData gdComp = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        gdComp.widthHint = 300;
        final Text tDesc = new Text(editComp, SWT.BORDER);
        tDesc.setLayoutData(gdComp);
        
        GridData gdApply = new GridData();
        gdApply.heightHint = 22;
        gdApply.widthHint = 22;
        Button bDesc = new Button(editComp, SWT.PUSH);
        bDesc.setImage(registry.getImage("Apply_16.ICON"));
        bDesc.setLayoutData(gdApply);
        bDesc.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
            	setMultiDescProperties(tDesc.getText());
            }
        });
        
        Button bRevDesc = new Button(editComp, SWT.PUSH);
        bRevDesc.setImage(registry.getImage("Revision_16.ICON"));
        bRevDesc.setLayoutData(gdApply);
        bRevDesc.setToolTipText("Input Change Description with Revision Property");
        bRevDesc.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseDown(MouseEvent e) {
        		setEPLChangeDescPropertyFromRev();
        	}
        });
    }
    
    /**
     * [SR151204-016][20151209][jclee] DWG Change Desc에 Revision의 Change Desc 입력
     */
    private void setEPLChangeDescPropertyFromRev() {
    	for (int inx = 0; inx < table.getItemCount(); inx++) {
    		TableItem item = table.getItem(inx);
    		SYMCECODwgData data = (SYMCECODwgData)item.getData();
    		
    		
    		
    		String sRevChangeDesc = "";
    		String sChangeDescription = item.getText(CHANGE_DESC);
    		String sPartNo = data.getPartNo();
    		String sPartRevNo = data.getRevisionNo();

    		if (!(sChangeDescription == null || sChangeDescription.equals("") || sChangeDescription.length() == 0)) {
				continue;
			}
    		
    		sRevChangeDesc = getRevChangeDesc(sPartNo, sPartRevNo);
    		setDescriptionValue(item, sRevChangeDesc);
    	}
    }

    /**
     * Rev 속성의 Change Description 반환
     * @param sPartNo
     * @param sPartRevNo
     * @return
     */
    private String getRevChangeDesc(String sPartNo, String sPartRevNo) {
    	String sRevChangeDesc = "";
    	try {
    		CustomECODao dao = new CustomECODao();
    		sRevChangeDesc = dao.getChangeDescription(sPartNo, sPartRevNo);
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		
    	return sRevChangeDesc;
    }
    
    private void setMultiDescProperties(String value) {
        int[] selRows = table.getSelectionIndices();
        for(int r : selRows) {
        	TableItem item = (TableItem)table.getItem(r);
            setDescriptionValue(item, value);
        }
    }
    

    public boolean isPageComplete() {
        return true;
    }

    /**
     * load Data
     */
    @Override
    public void load() {
        new Job("Load...") {
            protected IStatus run(IProgressMonitor arg0) {
                try {
                    getDisplay().syncExec(new Runnable() {
                        public void run() {
                            setCursor(new Cursor(getShell().getDisplay(), SWT.CURSOR_WAIT));
                        }
                    });
                    setTableData();
                    return Status.OK_STATUS;
                } catch(final Exception e) {
                    getDisplay().syncExec(new Runnable() {
                        public void run() {
                            MessageBox.post(getShell(), e.getMessage(), "Notification", 2);
                        }
                    });
                    return Status.CANCEL_STATUS;
                } finally {
                    getDisplay().syncExec(new Runnable() {
                        public void run() {
                            setCursor(new Cursor(getShell().getDisplay(), SWT.CURSOR_ARROW));
                        }
                    });
                }
            }
        }.schedule();
    }

    /**
     * 서버에서 가져온 데이터를 테이블에 등록한다.
     * 
     * @method setTableData
     * @date 2013. 2. 20.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private void setTableData() throws Exception {
        SYMCRemoteUtil remote = new SYMCRemoteUtil();
        DataSet ds = new DataSet();
        ds.put("ecoNo", AIFUtility.getCurrentApplication().getTargetComponent().getProperty("item_id"));
        final ArrayList<SYMCECODwgData> data = (ArrayList<SYMCECODwgData>) remote.execute("com.kgm.service.ECOHistoryService", "selectECODwgList", ds);
        // Modify List Clear
        if (modifiedTableItems == null) {
            modifiedTableItems = new ArrayList<TableItem>();
        } else {
            modifiedTableItems.clear();
        }
        getDisplay().syncExec(new Runnable() {
            public void run() {
                table.removeAll(); // 테이블 데이터 삭제
                if (data == null || data.size() == 0) {
                    return;
                }
                for (int i = 0; i < data.size(); i++) {
                    TableItem item = new TableItem(table, SWT.None);
                    String[] rowItemData = new String[14];
                    // No.
                    rowItemData[0] = (i + 1) + "";
                    // Proj.
                    rowItemData[1] = data.get(i).getProject();
                    // Model Type
                    rowItemData[2] = data.get(i).getModelType();
                    // Part Origin.
                    rowItemData[3] = data.get(i).getPartOrigin();
                    // Part No.
                    rowItemData[4] = data.get(i).getPartNo();
                    // Revision No.
                    rowItemData[5] = data.get(i).getRevisionNo();
                    // Part Name
                    rowItemData[6] = data.get(i).getPartName();
                    // Responsibility
                    rowItemData[7] = data.get(i).getResponsibility();
                    // CAT Product
                    rowItemData[8] = data.get(i).getCatProduct();
                    // 2D
                    rowItemData[9] = data.get(i).getHas2d();
                    // 3D
                    rowItemData[10] = data.get(i).getHas3d();
                    // ZIP
                    rowItemData[11] = data.get(i).getZip();
                    // S/MODE
                    rowItemData[12] = data.get(i).getsMode();
                    // Chang Desc
                    rowItemData[13] = data.get(i).getChangeDesc();

                    item.setText(rowItemData);
                    item.setData(data.get(i));
                    setModifiableCellColor(item, CHANGE_DESC);
                }
            }
        });
    }

    @Override
    public void save() {
        if (modifiedTableItems != null && modifiedTableItems.size() > 0) {
            composite.getDisplay().syncExec(new Runnable() {
                public void run() {
                    SYMCRemoteUtil remote = new SYMCRemoteUtil();
                    try {
                        ArrayList<SYMCECODwgData> saveData = new ArrayList<SYMCECODwgData>();
                        for(TableItem item : modifiedTableItems) {
                            SYMCECODwgData dwgData = (SYMCECODwgData)item.getData();
                            dwgData.setChangeDesc(item.getText(CHANGE_DESC));
                            saveData.add(dwgData);
                        }
                        setCursor(new Cursor(composite.getShell().getDisplay(), SWT.CURSOR_WAIT));
                        DataSet ds = new DataSet();
                        ds.put("bomEditData", saveData);
                        Boolean updateStatus = (Boolean) remote.execute("com.kgm.service.ECOHistoryService", "updateECODwgProperties", ds);
                        if (updateStatus == Boolean.FALSE) {
                            throw new Exception("Properties Update Error");
                        }
                        setTableData(); // 테이블 데이터 Refresh.
                    } catch (ConnectException ce) {
                        ce.printStackTrace();
                        MessageBox.post(composite.getShell(), ce.getMessage(), "Notification", 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                        MessageBox.post(composite.getShell(), e.getMessage(), "Notification", 1);
                    } finally {
                        setCursor(new Cursor(composite.getShell().getDisplay(), SWT.CURSOR_ARROW));
                    }
                }
            });
        }

    }
    
    public void setEditable(boolean flag) {
        super.setEditable(flag);
        Control[] children = editComp.getChildren();
        for(Control child : children) {
            child.setEnabled(flag);
        }
    }

    public void setControlReadOnly(Control composite) {
        setEditable(false);
    }

    public void setControlReadWrite(Control composite) {
        setEditable(true);
    }

    @Override
    public boolean isSavable() {
        return true;
    }

    /**
     * 테이블 수정데이터가 있는지 확인한다.
     */
    @Override
    public boolean isDirty() {
        if (modifiedTableItems.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

}
