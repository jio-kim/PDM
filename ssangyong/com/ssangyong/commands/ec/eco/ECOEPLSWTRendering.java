package com.ssangyong.commands.ec.eco;

import java.net.ConnectException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.common.SYMCLOVCombo;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.SYMDisplayUtil;
import com.ssangyong.common.utils.StringUtil;
import com.ssangyong.rac.kernel.SYMCBOMEditData;
import com.ssangyong.viewer.AbstractSYMCViewer;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PlatformHelper;
import com.teamcenter.rac.util.Registry;

/**
 * [20140417] Project code Old/New 분리 대응
 * @author bs
 *
 */
public class ECOEPLSWTRendering extends AbstractSYMCViewer {

	private ECOEPLSWTOptionsDialog ecoOptionsDialog; 

	private Table table;
	private Color evenColor;
	private Color modifiedColor;
	private Color modifiableColor;
	private Color modifiableEvenColor;
	private ArrayList<EPLTableItem> modifiedTableData;
	/** 컬럼 인덱스 */
	// Old or New Part No
	public static final int PART_NO = 7;
	// IC - OLD/NEW - LOV
	public static final int IC = 10;
	// PLT Stk - OLD -LOV
	public static final int PLT_STK = 19;
	// A/S Stk - OLD-LOV
	public static final int AS_STK = 20;
	// Cost - New - Key In
	public static final int COST = 21;
	// Tool - New - LOV
	public static final int TOOL = 22;
	// OPTIONS - OLD/NEW - POPUP
	public static final int OPTIONS = 24;
	// Change Desc - OLD/NEW - Key In
	public static final int CHANGE_DESC = 25;
	// EPL_ID
	//public static final int EPL_ID = 24;
	// OLD_NEW
	//public static final int OLD_NEW = 25;

	private Composite editComp;

	private boolean isClickedChgDesc = false;

	public ECOEPLSWTRendering(Composite parent) {
		super(parent);
	}

	/**
	 * Panel 생성
	 */
	@Override
	public void createPanel(Composite parent) {
		Display display = parent.getDisplay();
		evenColor = new Color(display, 192, 214, 248);//하늘색
		modifiedColor = new Color(display, 255, 225, 225);//분홍
		modifiableColor = new Color(display, 218, 237, 190);//초록
		modifiableEvenColor = new Color(display, 255, 255, 132);//노랑

		// Button btn = new Button(parent, SWT.PUSH);
		Composite composite = new Composite(parent, SWT.None);
		GridLayout mainLayout = new GridLayout(1, false);
		mainLayout.marginWidth = 0;
		mainLayout.marginHeight = 0;
		composite.setLayout(mainLayout);
		//initButtons(composite);
		initTable(composite);
		initEditor(composite);
		parent.pack();
	}

	public void setEditable(boolean flag) {
		super.setEditable(flag);
		Control[] children = editComp.getChildren();
		for(Control child : children) {
			child.setEnabled(flag);
		}
	}

	/*private void initButtons(Composite parent) {
        btnRollback = new Button(parent, SWT.PUSH);
        btnRollback.setText("Undo");
        btnRollback.setImage(AbstractUIPlugin.imageDescriptorFromPlugin("com.teamcenter.rac.common", "icons/ic_undo_changes_16.png").createImage());
        GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
        btnRollback.setLayoutData(gd);
        btnRollback.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                new Job("Download Button Job") {

                    private int tableRow = -1;
                    private SYMCBOMEditData bomEditData = null;

                    @Override
                    protected IStatus run(IProgressMonitor arg0) {
                        getDisplay().syncExec(new Runnable() {
                            public void run() {
                                tableRow = table.getSelectionIndex();
                                if(tableRow == -1) {
                                    MessageBox.post(getShell(), "Select Rollback ECO EPL Line.", "ECO 1Step Undo", MessageBox.INFORMATION);
                                    return;
                                }
                                EPLTableItem eplItem = (EPLTableItem)table.getItem(tableRow);
                                bomEditData = eplItem.getBOMEditData();
                            }
                        });

                        if(bomEditData == null) {
                            return Status.CANCEL_STATUS;
                        }

                        ECORollbackOperation ecoRollback = new ECORollbackOperation(bomEditData);
                        if(ecoRollback.getResult()) {
                            removeRow(tableRow);
                        }

                        return Status.OK_STATUS;
                    }
                }.schedule();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
    }

    private void removeRow(final int tableRow) {
        getDisplay().syncExec(new Runnable() {
            public void run() {
                if(tableRow % 2 == 1) {
                    table.remove(tableRow);
                    table.remove(tableRow - 1);
                } else {
                    table.remove(tableRow + 1);
                    table.remove(tableRow);
                }
                table.setSelection(-1);
                TableItem[] items = table.getItems();
                for(int i = 0 ; i < items.length ; i++) {
                    EPLTableItem eplItem = (EPLTableItem)items[i];
                    eplItem.setRowProperty();
                }
                table.redraw();
            }
        });
    }*/

	/**
	 * [20240306][UPGRADE] 컬럼사이즈 조절
	 * @param parent
	 */
	private void initTable(Composite parent) {
		table = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(gd);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		createTableColumn("No", 40);
		createTableColumn("Proj.", 50);
		//createTableColumn("Find No", 65);
		//createTableColumn("C/T", 35);
		createTableColumn("Find No", 66);
		createTableColumn("C/T", 36);
		createTableColumn("Parent No", 105);
		createTableColumn("Parent Rev", 40);
		createTableColumn("Part Origin", 25);
		createTableColumn("Part No", 100);
		createTableColumn("Part Rev", 40);
		createTableColumn("Part Name", 170);
		createTableColumn("IC", 42);
		createTableColumn("Supply Mode", 60);
		createTableColumn("QTY", 30);
		createTableColumn("ALT", 20);
		createTableColumn("SEL", 20);
		createTableColumn("CAT", 50);
		createTableColumn("Color", 25);
		createTableColumn("Color Section", 60);
		createTableColumn("Module Code", 47);
		createTableColumn("PLT Stk", 65);
		createTableColumn("A/S Stk", 65);
		createTableColumn("Cost", 50);
		createTableColumn("Tool", 42);
		createTableColumn("Shown-On", 100);
		createTableColumn("Options", 130);
		createTableColumn("Change Desc", 150);

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if(isEditable() && e.button == 1) {
					processCellEdit(e);
				}
			}
		});
		/*if(parent.getParent() != null) {
            Composite owner = parent.getParent().getParent();
            table.setSize(owner.getClientArea().width, owner.getClientArea().height);
        }*/

		//[20170525][ljg] Ctrl+C 로 Table에서 Part Name 컬럼의 값을 복사할 수 있도록
		table.addKeyListener(new KeyListener() {
			
		@Override
		public void keyReleased(KeyEvent e) {
				if(table.getSelectionCount() == 1){
					if( e.keyCode == 'c' && ( e.stateMask & SWT.MODIFIER_MASK ) == SWT.CTRL ) {
						TableItem ti = table.getItem(table.getSelectionIndex());
						String part_name = ti.getText(9);

						Clipboard clipboard = new Clipboard(PlatformHelper.getCurrentDisplay());
						String rtfData = "{\\rtf1\\b\\i " + part_name + "}";
						TextTransfer textTransfer = TextTransfer.getInstance();
						RTFTransfer rtfTransfer = RTFTransfer.getInstance();
						Transfer[] transfers = new Transfer[]{textTransfer, rtfTransfer};
						Object[] data = new Object[]{part_name, rtfData};
						clipboard.setContents(data, transfers);
						clipboard.dispose();
					}
				}
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
			}
		});
	}

	/**
	 * Create Table Column
	 * 
	 * @method createTableColumn
	 * @date 2013. 3. 4.
	 * @param
	 * @return TableColumn
	 * @exception
	 * @throws
	 * @see
	 */
	private TableColumn createTableColumn(String columnName, int width) {
		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setText(columnName);
		column.setWidth(width);
		column.setResizable(true);
		column.setMoveable(true);
		return column;
	}

	private void initEditor(Composite parent) {
		Registry registry = Registry.getRegistry("com.ssangyong.common.common");
		GridData gdApply = new GridData();
		gdApply.heightHint = 22;
		gdApply.widthHint = 22;
		GridData gdLabel = new GridData(SWT.END, SWT.CENTER, false, false);
		gdLabel.widthHint = 80;
		GridData gdComp = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		gdComp.widthHint = 120;

		editComp = new Composite(parent, SWT.None);
		GridData gdEdit = new GridData(SWT.LEAD, SWT.CENTER, true, false);
		editComp.setLayoutData(gdEdit);

		// [SR151204-016][20151209][jclee] Revision의 Change Desc을 불러오는 버튼 추가
		//        GridLayout layout = new GridLayout(9, false);
		GridLayout layout = new GridLayout(10, false);
		editComp.setLayout(layout);
		Label lPLTStk = new Label(editComp, SWT.None | SWT.RIGHT_TO_LEFT);
		lPLTStk.setText("PLT Stk");
		lPLTStk.setLayoutData(gdLabel);
		final SYMCLOVCombo cPLTStk = new SYMCLOVCombo(editComp, "S7_PLANT_AS_STOCK", SYMCLOVCombo.VIEW_DESC, false);
		cPLTStk.setLayoutData(gdComp);
		Button bPLTStk = new Button(editComp, SWT.PUSH);
		bPLTStk.setImage(registry.getImage("Apply_16.ICON"));
		bPLTStk.setLayoutData(gdApply);
		bPLTStk.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				setEPLProperties(PLT_STK, cPLTStk.getTextDesc());
			}
		});
		Label lCost = new Label(editComp, SWT.None | SWT.RIGHT_TO_LEFT);
		lCost.setText("Cost");
		lCost.setLayoutData(gdLabel);
		final Text tCost = new Text(editComp, SWT.BORDER);
		tCost.setLayoutData(gdComp);
		/*
		 * 20220629 
		 * Cost값은 10byte까지 입력 할 수 있음
		 * 10byte 초과 입력 후 저장시 입력값이 사라지는 현상 발생
		 * 기존에 입력 제한이 없어서 추가 하였음
		 * */ 
		tCost.setTextLimit(10);
		tCost.addVerifyListener(new VerifyListener(){
			@Override
			public void verifyText(VerifyEvent paramVerifyEvent)
			{
				String keyString = paramVerifyEvent.text;
				int costLength = tCost.getText().getBytes().length + keyString.getBytes().length;
				if(costLength > 10){
					paramVerifyEvent.doit = false;
				}
			}
		});
		
		Button bCost = new Button(editComp, SWT.PUSH);
		bCost.setImage(registry.getImage("Apply_16.ICON"));
		bCost.setLayoutData(gdApply);
		bCost.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if(tCost.getText().getBytes().length > 10){
					MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Cost값은 각각 한글 5자 영어 10자 총 10byte까지 입력 가능 합니다." , "WARNING", MessageBox.WARNING);
					return;
				}
				setEPLProperties(COST, tCost.getText());
			}
		});
		Label lIC = new Label(editComp, SWT.None | SWT.RIGHT_TO_LEFT);
		lIC.setText("IC");
		lIC.setLayoutData(gdLabel);
		final SYMCLOVCombo cIC = new SYMCLOVCombo(editComp, "S7_YN");
		cIC.setLayoutData(gdComp);
		Button bIC = new Button(editComp, SWT.PUSH);
		bIC.setImage(registry.getImage("Apply_16.ICON"));
		GridData gdIC = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gdIC.horizontalSpan = 2;
		gdIC.widthHint = 22;
		gdIC.heightHint = 22;
		bIC.setLayoutData(gdIC);
		bIC.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				setEPLProperties(IC, cIC.getText());
			}
		});
		Label lASStk = new Label(editComp, SWT.None | SWT.RIGHT_TO_LEFT);
		lASStk.setText("AS Stk");
		lASStk.setLayoutData(gdLabel);
		final SYMCLOVCombo cASStk = new SYMCLOVCombo(editComp, "S7_PLANT_AS_STOCK", SYMCLOVCombo.VIEW_DESC, false);
		cASStk.setLayoutData(gdComp);
		Button bASStk = new Button(editComp, SWT.PUSH);
		bASStk.setImage(registry.getImage("Apply_16.ICON"));
		bASStk.setLayoutData(gdApply);
		bASStk.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				setEPLProperties(AS_STK, cASStk.getTextDesc());
			}
		});
		Label lTool = new Label(editComp, SWT.None | SWT.RIGHT_TO_LEFT);
		lTool.setText("Tool");
		lTool.setLayoutData(gdLabel);
		final SYMCLOVCombo cTool = new SYMCLOVCombo(editComp, "S7_YN");
		cTool.setLayoutData(gdComp);
		Button bTool = new Button(editComp, SWT.PUSH);
		bTool.setImage(registry.getImage("Apply_16.ICON"));
		bTool.setLayoutData(gdApply);
		bTool.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				setEPLProperties(TOOL, cTool.getText());
			}
		});
		Label lDesc = new Label(editComp, SWT.None | SWT.RIGHT_TO_LEFT);
		lDesc.setText("Change Desc");
		lDesc.setLayoutData(gdLabel);
		final Text tDesc = new Text(editComp, SWT.BORDER);
		//GridData gdDesc = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		tDesc.setLayoutData(gdComp);
		/*
		 * 20220629 
		 * Change Desc값은 1000byte까지 입력 할 수 있음
		 * 1000byte 초과 입력 후 저장시 입력값이 사라지는 현상 발생
		 * 기존에 입력 제한이 없어서 추가 하였음
		 * */
		tDesc.setTextLimit(1000);
		tDesc.addVerifyListener(new VerifyListener(){
			@Override
			public void verifyText(VerifyEvent paramVerifyEvent)
			{
				String keyString = paramVerifyEvent.text;
				int costLength = tDesc.getText().getBytes().length + keyString.getBytes().length;
				if(costLength > 1000){
					paramVerifyEvent.doit = false;
				}
			}
		});

		Button bDesc = new Button(editComp, SWT.PUSH);
		bDesc.setImage(registry.getImage("Apply_16.ICON"));
		bDesc.setLayoutData(gdApply);
		bDesc.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if(tDesc.getText().getBytes().length > 1000){
					MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Change Desc값은 각각 한글 500자 영어 1000자 총 1000byte까지 입력 가능 합니다.", "WARNING", MessageBox.WARNING);
					return;
				}
				setEPLProperties(CHANGE_DESC, tDesc.getText());
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

	private void setEPLProperties(int col, String value) {
		int[] selRows = table.getSelectionIndices();
		for(int r : selRows) {
			EPLTableItem item = (EPLTableItem)table.getItem(r);
			setItemDataValue(item, r, col, value);
		}
	}

	/**
	 * [SR151204-016][20151209][jclee] EPL Change Desc에 Revision의 Change Desc 입력
	 */
	private void setEPLChangeDescPropertyFromRev() {
		for (int inx = 0; inx < table.getItemCount(); inx++) {
			EPLTableItem item = (EPLTableItem)table.getItem(inx);

			if (item.isOld()) {
				continue;
			}

			String sRevChangeDesc = "";
			String sCT = item.bomEditData.getChangeType();
			String sChangeDescription = item.getText(CHANGE_DESC);
			String sOldPartNo = item.bomEditData.getPartNoOld();
			String sOldPartRevNo = item.bomEditData.getPartRevOld();
			String sNewPartNo = item.bomEditData.getPartNoNew();
			String sNewPartRevNo = item.bomEditData.getPartRevNew();

			if (!(sChangeDescription == null || sChangeDescription.equals("") || sChangeDescription.length() == 0)) {
				continue;
			}

			if (sCT.equals("F1") || sCT.equals("F2") || sCT.equals("D")) {
				continue;
			}

			if (sNewPartNo.equals(sOldPartNo) && sNewPartRevNo.equals(sOldPartRevNo)) {
				continue;
			}

			sRevChangeDesc = getRevChangeDesc(sNewPartNo, sNewPartRevNo);
			setItemDataValue(item, inx, CHANGE_DESC, sRevChangeDesc);
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

	/**
	 * Window Open
	 * 
	 * @method openPropertyWindow
	 * @date 2013. 3. 4.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see

    public void openPropertyWindow() {
        Color bgColor = SWTResourceManager.getColor(SWT.COLOR_WHITE);

        final Shell shell = new Shell(parent.getShell(), SWT.NO_TRIM);
        shell.setBackground(bgColor);
        shell.setSize(200, 200);
        shell.setLayout(new GridLayout(1, false));

        GridData gdTitle = new GridData(SWT.FILL, SWT.FILL, true, false);
        gdTitle.heightHint = 20;
        GridData gSeparator = new GridData(SWT.FILL, SWT.FILL, true, false);

        Label title = new Label(shell, SWT.CENTER);
        Font titleFont = SWTResourceManager.getBoldFont(shell.getFont());
        title.setFont(titleFont);
        title.setText("I/C");
        title.setBackground(bgColor);
        title.setLayoutData(gdTitle);
        title.pack();
        Label lSeparator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        lSeparator.pack();
        lSeparator.setBackground(bgColor);
        lSeparator.setLayoutData(gSeparator);

        try {
            TCComponentListOfValuesType listofvaluestype = (TCComponentListOfValuesType) CustomUtil.getTCSession().getTypeComponent("ListOfValues");
            TCComponentListOfValues[] listofvalues = listofvaluestype.find("S7_Supply_Mode");
            TCComponentListOfValues listofvalue = listofvalues[0];
            String[] lovValues = listofvalue.getListOfValues().getStringListOfValues();
            for (String lovValue : lovValues) {
                Button btn = new Button(shell, SWT.RADIO);
                btn.setText(lovValue);
                btn.setBackground(bgColor);
                btn.pack();
            }
        } catch (TCException e) {
            e.printStackTrace();
        }

        lSeparator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        lSeparator.pack();
        lSeparator.setBackground(bgColor);
        lSeparator.setLayoutData(gSeparator);

        Composite bComposite = new Composite(shell, SWT.None);
        bComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
        Button bSave = new Button(bComposite, SWT.PUSH);
        bSave.setText("Save");
        bSave.pack();
        Button bCancel = new Button(bComposite, SWT.PUSH);
        bCancel.setText("Cancel");
        bCancel.pack();

        shell.addListener(SWT.MouseDown, new Listener() {
            @Override
            public void handleEvent(Event event) {
                shell.dispose();
            }
        });
        shell.pack();
        org.eclipse.swt.graphics.Point curMousePoint = org.eclipse.swt.widgets.Display.getDefault().getCursorLocation();
        shell.setLocation(curMousePoint.x, curMousePoint.y);
        shell.open();
    }*/

	/**
	 * Table Cell Mouse Down Event
	 * 
	 * @method processCellEdit 
	 * @date 2013. 3. 25.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	private void processCellEdit(MouseEvent event) {
		// 체크아웃 유무 확인
		if (!isEditable()) {
			return;
		}
		int[] selInxs = table.getSelectionIndices();
		if(selInxs != null && selInxs.length > 1) {
			return;
		}
		final TableEditor editor = new TableEditor(table);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;        
		Point pt = new Point(event.x, event.y);
		final int row = table.getSelectionIndex();
		if(row == -1 || row > table.getItemCount() - 1) {
			return;
		}
		final EPLTableItem item = (EPLTableItem)table.getItem(row);
		for (int i = 8 ; i < table.getColumnCount() ; i++) {
			Rectangle rect = item.getBounds(i);
			if (!rect.contains(pt)) {
				continue;
			}
			final int column = i;
			if(column == OPTIONS && event.count > 1) {
				openOptionPopup(row);
				return;
			}

			if(column == CHANGE_DESC && event.count == 1 && !isClickedChgDesc) {
				MessageBox.post("Ctrl + V 는 유효하지 않습니다.\n(Ctrl + V 시 저장하지 않은 입력 내용이 삭제됩니다.)\n여러 Row에 입력 시 하단의 입력 툴을 사용 바랍니다.", "Warning", MessageBox.WARNING);
				isClickedChgDesc = true;
			}

			Scrollable cellComponent = getCellComponent(item, column);
			if (cellComponent == null) {
				continue;
			} else {
				final Control rendererComponent = (Control)cellComponent;
				Listener componentListener = new Listener() {
					public void handleEvent(final Event e) {
						String newValue = null;
						if(rendererComponent instanceof SYMCLOVCombo) {
							if(column == PLT_STK || column == AS_STK) {
								newValue = ((SYMCLOVCombo)rendererComponent).getTextDesc();
							} else {
								newValue = ((SYMCLOVCombo)rendererComponent).getText();
							}
						} else if(rendererComponent instanceof Text) {	
							newValue = ((Text)rendererComponent).getText();
						}
						switch (e.type) {
						case SWT.FocusOut:
							setItemDataValue(item, row, column, newValue);
							rendererComponent.dispose();
							break;
						case SWT.Selection:
							setItemDataValue(item, row, column, newValue);
							break;
						case SWT.Traverse:
							switch (e.detail) {
							case SWT.TRAVERSE_RETURN:
								setItemDataValue(item, row, column, newValue);
							case SWT.TRAVERSE_ESCAPE:
								rendererComponent.dispose();
								e.doit = false;
							}
							break;
						}
					}
				};
				/*
				 * 20220629 
				 * Cost값은 10byte까지 입력 할 수 있음
				 * 10byte 초과 입력 후 저장시 입력값이 사라지는 현상 발생
				 * 기존에 입력 제한이 없어서 추가 하였음
				 * */
				if(column == COST){
					final Text costText = ((Text)rendererComponent);
					costText.setTextLimit(10);
					costText.addVerifyListener(new VerifyListener(){
						@Override
						public void verifyText(VerifyEvent paramVerifyEvent)
						{
							String keyString = paramVerifyEvent.text;
							int costLength = costText.getText().getBytes().length + keyString.getBytes().length;
							if(costLength > 10){
								paramVerifyEvent.doit = false;
							}
						}
					});
				}
				/*
				 * 20220629 
				 * CHANGE_DESC 1000byte까지 입력 할 수 있음
				 * 1000byte 초과 입력 후 저장시 입력값이 사라지는 현상 발생 
				 * 기존에 입력 제한이 없어서 추가 하였음
				 * */
				if(column == CHANGE_DESC){
					final Text changeDescText = ((Text)rendererComponent);
					changeDescText.setTextLimit(1000);
					changeDescText.addVerifyListener(new VerifyListener(){
						@Override
						public void verifyText(VerifyEvent paramVerifyEvent)
						{
							String keyString = paramVerifyEvent.text;
							int costLength = changeDescText.getText().getBytes().length + keyString.getBytes().length;
							if(costLength > 1000){
								paramVerifyEvent.doit = false;
							}
						}
					});					
				}
				rendererComponent.addListener(SWT.FocusOut, componentListener);
				rendererComponent.addListener(SWT.Traverse, componentListener);
				editor.setEditor(rendererComponent, item, column);
				if(rendererComponent instanceof SYMCLOVCombo) {
					if(column == PLT_STK || column == AS_STK) {
						((SYMCLOVCombo)rendererComponent).setTextDesc(item.getText(column));
					} else {
						((SYMCLOVCombo)rendererComponent).setText(item.getText(column));
					}
				} else if(rendererComponent instanceof Text) {
					((Text)rendererComponent).setText(item.getText(column));
				}
				rendererComponent.setFocus();

				return;
			}
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
	public Scrollable getCellComponent(EPLTableItem item, int colunIndex) {
		SYMCBOMEditData bomEditData = item.getBOMEditData();
		switch (colunIndex) {
		case IC:// NEW, OLD 둘다 등록
			if(bomEditData.isReplace() && !bomEditData.getPartTypeNew().equals("S7_FunctionMast")) {
				return new SYMCLOVCombo(table, "S7_YN");
			}
			return null;
		case PLT_STK:// OLD 등록
		case AS_STK:// OLD 등록
			if(item.isOld() && (bomEditData.isReplace() || bomEditData.getChangeType().equals(SYMCBOMEditData.BOM_CUT))) {
				return new SYMCLOVCombo(table, "S7_PLANT_AS_STOCK", SYMCLOVCombo.VIEW_DESC, false);
			}
			return null;
		case COST:// NEW 등록
			if(!item.isOld() && item.hasPart()) {
				return new Text(table, SWT.NONE);
			}
			return null;
		case TOOL:// NEW 등록
			if(!item.isOld() && item.hasPart()) {
				return new SYMCLOVCombo(table, "S7_YN");
			}
			return null;
		case CHANGE_DESC:// NEW, OLD 둘다 등록
			String ct = bomEditData.getChangeType();
			/**
			 * [SR141016-007][jclee][2014.11.05] FMP는 Change Desc 편집 불가 처리.
			 */
			String parentType = bomEditData.getParentType();
			if(!"".equals(StringUtil.nullToString(item.getText(PART_NO)))
					&& !parentType.equals("S7_Function")) {
				if(item.isOld() && ct.equals("D")) {
					return new Text(table, SWT.NONE);
				} else if(!item.isOld() && !ct.equals("D")) {
					return new Text(table, SWT.NONE);
				}
			}
			return null;
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
	public void setItemDataValue(EPLTableItem item, int row, int colunIndex, String value) {
//		System.out.println("setItemDataValue");
		value = StringUtil.nullToString(value);
		SYMCBOMEditData editData = item.getBOMEditData();
		boolean modified = false;
		switch (colunIndex) {
		case IC:// NEW, OLD 둘다 등록
			if(!editData.isReplace() || editData.getPartTypeNew().equals("S7_FunctionMast")) {
				return;
			}
			item.setText(colunIndex, value); // Set Text
			if (item.isOld()) {
				if (!value.equals(StringUtil.nullToString(editData.getIcOld()))) {
					modified = true;
				}
			} else {
				if (!value.equals(StringUtil.nullToString(editData.getIcNew()))) {
					modified = true;
				}
			}
			break;
		case PLT_STK:// OLD 등록
			if (item.isOld() && item.hasPart()) {
				item.setText(colunIndex, value); // Set Text
				if (!value.equals(StringUtil.nullToString(editData.getPltStkOld()))) {
					modified = true;
				}
			}
			break;
		case AS_STK:// OLD 등록
			if (item.isOld() && item.hasPart()) {
				item.setText(colunIndex, value); // Set Text
				if (!value.equals(StringUtil.nullToString(editData.getAsStkOld()))) {
					modified = true;
				}
			}
			break;
		case COST:// NEW 등록
			if (!item.isOld() && item.hasPart()) {
				item.setText(colunIndex, value); // Set Text
				/*[20220629]
				 * Cost 10byte 초과시 메시지 출력 추가 
				 * */
				if(value.getBytes().length > 10){
					MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Cost값은 각각 한글 5자 영어 10자 총 10byte까지 입력 가능 합니다.", "WARNING", MessageBox.WARNING);
					return;
				}
				if (!value.equals(StringUtil.nullToString(editData.getCostNew()))) {
					modified = true;
				}
			}
			break;
		case TOOL:// NEW 등록
			if (!item.isOld() && item.hasPart()) {
				item.setText(colunIndex, value); // Set Text
				if (!value.equals(StringUtil.nullToString(editData.getToolNew()))) {
					modified = true;
				}
			}
			break;
		case CHANGE_DESC:// NEW, OLD 둘다 등록
			String ct = editData.getChangeType();
			/*
			 * [20220629]
			 * CHANGE DESC 1000byte 초과시 메시지 출력 추가  
			 */
			if(value.getBytes().length > 1000){
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Change Desc값은 각각 한글 500자 영어 1000자 총 1000byte까지 입력 가능 합니다.(1000byte)" + value.getBytes().length +"byte 입니다."   , "WARNING", MessageBox.WARNING);
				return;
			}
			if (item.isOld() && ct.equals("D")) {
				item.setText(colunIndex, value); // Set Text
				if (!value.equals(StringUtil.nullToString(editData.getChgDesc()))) {
//					item.bomEditData.setChgDesc(value);	// Save 시 Change Description 을 가져오기 위해 미리 Item에 값 Setting
					modified = true;
				}
			} else if(!item.isOld() && !ct.equals("D")){
				item.setText(colunIndex, value); // Set Text
				if (!value.equals(StringUtil.nullToString(editData.getChgDesc()))) {
//					item.bomEditData.setChgDesc(value);//[20200113 CSH]값을 set 하면 동일 Value 두번 적용하면 Modified로 인지하지 않음. 주적 처리
					modified = true;
				}
			}
			break;
		default:
		}
		if(modified) {
			item.setBackground(colunIndex, modifiedColor); // Modify Color
			if (!modifiedTableData.contains(item)) {
				modifiedTableData.add(item);
			}
		} else {
			int div = row % 4;
			boolean isEven = div == 3 || div == 4;
			if(isEven) {
				item.setBackground(colunIndex, modifiableEvenColor); // Modify Color
			} else {
				item.setBackground(colunIndex, modifiableColor); // Modify Color
			}
			if(modifiedTableData.contains(item) && !eplItemChanged(editData, item)) {
				modifiedTableData.remove(item);
			}
		}
	}

	private boolean eplItemChanged(SYMCBOMEditData editData, EPLTableItem eplData) {
		if(eplData.isOld()) {
			if (!StringUtil.nullToString(editData.getIcOld()).equals(StringUtil.nullToString(eplData.getText(IC)))) {
				return true;
			}
			if (!StringUtil.nullToString(editData.getPltStkOld()).equals(StringUtil.nullToString(eplData.getText(PLT_STK)))) {
				return true;
			}
			if (!StringUtil.nullToString(editData.getAsStkOld()).equals(StringUtil.nullToString(eplData.getText(AS_STK)))) {
				return true;
			}
		} else {
			if (!StringUtil.nullToString(editData.getIcNew()).equals(StringUtil.nullToString(eplData.getText(IC)))) {
				return true;
			}
			if (!StringUtil.nullToString(editData.getCostNew()).equals(StringUtil.nullToString(eplData.getText(COST)))) {
				return true;
			}
			if (!StringUtil.nullToString(editData.getToolNew()).equals(StringUtil.nullToString(eplData.getText(TOOL)))) {
				return true;
			}
		}
		if (!StringUtil.nullToString(editData.getChgDesc()).equals(StringUtil.nullToString(eplData.getText(CHANGE_DESC)))) {
			return true;
		}
		return false;
	}

	/**
	 * 1. Modify Cell 의 Cell Color 를 변경한다., 변경리스트에 업데이트 할 변경정보를 등록한다.
	 * 
	 * @method setModifyCell
	 * @date 2013. 3. 5.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see

    private void setModifyCell(SYMCBOMEditData editData, EPLTableItem item, int colunIndex) {
        item.setBackground(colunIndex, modifyColor); // Modify Color
        // Modify Item 등록(업데이트시 사용)
        if (!modifyTableDataRows.contains(item.getData())) {
            modifyTableDataRows.add(item.getBOMEditData());
        }
    }*/

	public void setControlReadOnly(Control composite) {
		//btnRollback.setEnabled(false);
		setEditable(false);
	}

	public void setControlReadWrite(Control composite) {
		//btnRollback.setEnabled(true);
		setEditable(true);
	}

	@Override
	public void load() {
		new Job("EPL Load...") {
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				modifiedTableData =  new ArrayList<ECOEPLSWTRendering.EPLTableItem>();
				try {
					getDisplay().syncExec(new Runnable() {
						public void run() {
							setCursor(new Cursor(getShell().getDisplay(), SWT.CURSOR_WAIT));
						}
					});

					setTableData();
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
				return Status.OK_STATUS;
			}

		}.schedule();
	}

	/**
	 * DB에서 테이블 데이터를 가져와 랜더링한다.
	 * 
	 * @method setTableData
	 * @date 2013. 3. 4.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	@SuppressWarnings("unchecked")
	private void setTableData() throws Exception {
		// Modify List Clear
		if (modifiedTableData == null) {
			modifiedTableData = new ArrayList<EPLTableItem>();
		} else {
			modifiedTableData.clear();
		}

		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		//        SYMCRemoteUtil remote = new SYMCRemoteUtil("http://localhost:8080/ssangyongweb/HomeServlet");
		DataSet ds = new DataSet();
		ds.put("ecoNo", AIFUtility.getCurrentApplication().getTargetComponent().getProperty("item_id"));
		final ArrayList<SYMCBOMEditData> rows = (ArrayList<SYMCBOMEditData>) remote.execute("com.ssangyong.service.ECOHistoryService", "selectECOEplList", ds);

		getDisplay().syncExec(new Runnable() {
			public void run() {
				try {
					table.removeAll(); // 테이블 데이터 삭제
					if (rows == null || rows.size() == 0) {
						return;
					}
					ArrayList<String> addedEPLs = new ArrayList<String>();
					for (int i = 0; i < rows.size(); i++) {
						String eplId = rows.get(i).getEplId();
						if(!addedEPLs.contains(eplId)) {
							new EPLTableItem(rows.get(i), true);
							new EPLTableItem(rows.get(i), false);
							addedEPLs.add(eplId);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void save() {
//		System.out.println("C Save Start....");
		if (modifiedTableData != null && modifiedTableData.size() > 0) {
//			System.out.println("  C modified data : " + modifiedTableData.size() + " 건");
			getDisplay().syncExec(new Runnable() {
				public void run() {
					ArrayList<SYMCBOMEditData> changedList = new ArrayList<SYMCBOMEditData>();
					for(EPLTableItem item : modifiedTableData) {
						SYMCBOMEditData changeData = null;
						int dataIndex = changedList.indexOf(item);
						if(dataIndex == -1) {
							changeData = item.getBOMEditData();
							changedList.add(changeData);
						} else {
							changeData = changedList.get(dataIndex);
						}
						if(item.isOld()) {
							// [2015.04.02][jclee] PLT STK, AS STK 수정 후 Save 시 Change Desc가 사라지는 문제 해결.
							// OLD쪽 Item에는 Chg Desc이 적혀있지 않기 때문에 Item에서 getText로 값을 가져올 경우 공백으로 사라져버리는 문제가 있음.
							// Item이 갖고있는 BOM Edit Data에서 Change Desc을 가져와 값을 셋팅해준다.
							/*[2020.01.14][CSH] 위 문제 해결 로직으로 인해 다른 문제 점 발생. chgDesc 입력 값이 사라지는 문제 발생으로 특정 사용자의 클레임이 심함.
							 * Save 하기전에 setItemDataValue가 두번 호출되면 modified가 false로 바뀌어 입력값이 사라지게 됨.
							 * 이문제는 chgdesc를 old와 new를 구분하지 않고 공통 사용으로 인한 문제임.
							 * chgDesc도 기존 처럼 getText로 값을 받고 chgType이 D인 경우에만 changeData에 set 하도록 변경하여 문제 해결.
							*/
							
//							String sChgDesc = item.getBOMEditData().getChgDesc();

							changeData.setIcOld(StringUtil.nullToString(item.getText(IC)));
							changeData.setPltStkOld(StringUtil.nullToString(item.getText(PLT_STK)));
							changeData.setAsStkOld(StringUtil.nullToString(item.getText(AS_STK)));
							if (changeData.getChangeType().equals("D")) {
								changeData.setChgDesc(StringUtil.nullToString(item.getText(CHANGE_DESC)));
							}
//							changeData.setChgDesc(StringUtil.nullToString(sChgDesc));
						} else {
							changeData.setIcNew(StringUtil.nullToString(item.getText(IC)));
							changeData.setCostNew(StringUtil.nullToString(item.getText(COST)));
							changeData.setToolNew(StringUtil.nullToString(item.getText(TOOL)));
							changeData.setChgDesc(StringUtil.nullToString(item.getText(CHANGE_DESC)));
//							System.out.println("  > "+item.getText(CHANGE_DESC));
						}
					}
					SYMCRemoteUtil remote = new SYMCRemoteUtil();
					try {
						setCursor(new Cursor(getShell().getDisplay(), SWT.CURSOR_WAIT));
						System.out.println("  DB Update Start...");
						DataSet ds = new DataSet();
						ds.put("bomEditData", changedList);
						Boolean updateStatus = (Boolean) remote.execute("com.ssangyong.service.ECOHistoryService", "updateECOEPLProperties", ds);
						if (updateStatus == Boolean.FALSE) {
							throw new Exception("Properties Update Error");
						}
						System.out.println("  DB Update End...");
					} catch (ConnectException ce) {
						ce.printStackTrace();
						MessageBox.post(getShell(), ce.getMessage(), "Notification", 1);
					} catch (Exception e) {
						e.printStackTrace();
						MessageBox.post(getShell(), e.getMessage(), "Notification", 1);
					} finally {
						//load();
						setCursor(new Cursor(getShell().getDisplay(), SWT.CURSOR_ARROW));
					}
				}
			});
		}
//		System.out.println("C Save End....");
	}

	@Override
	public boolean isSavable() {
		return false;
	}

	/**
	 * 테이블 수정데이터가 있는지 확인한다.
	 */
	@Override
	public boolean isDirty() {
//		System.out.println("C isDirty...."+modifiedTableData.size());
		if (modifiedTableData.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	private class EPLTableItem extends TableItem {

		private SYMCBOMEditData bomEditData;
		private boolean isOld;
		private boolean hasPart;

		private EPLTableItem(SYMCBOMEditData bomEditData, boolean isOld) {
			super(table, SWT.None);
			this.bomEditData = bomEditData;
			this.isOld = isOld;
			if(isOld) {
				setOldData();
			} else {
				setNewData();
			}
			setRowProperty();
		}

		private SYMCBOMEditData getBOMEditData() {
			return bomEditData;
		}

		private boolean isOld() {
			return isOld;
		}

		private boolean hasPart() {
			return hasPart;
		}

		private void setOldData() {
			if("".equals(StringUtil.nullToString(bomEditData.getPartNoOld()))) {
				return; 
			}
			hasPart = true;

			String[] rowOldItemData = new String[26];
			// No.
			rowOldItemData[0] = getRowNo() + "";
			// Proj.
			// [20140417] Project code Old/New 분리 대응
			rowOldItemData[1] = bomEditData.getProjectOld();
			// SEQ
			rowOldItemData[2] = bomEditData.getSeqOld();
			// C/T
			rowOldItemData[3] = bomEditData.getChangeType().equals("D") ? bomEditData.getChangeType() : "";
			// Parent No, Parent Rev
			rowOldItemData[4] = bomEditData.getParentNo();
			rowOldItemData[5] = bomEditData.getParentRev();
			// Part Origin
			rowOldItemData[6] = bomEditData.getPartOriginOld();
			// Part No
			rowOldItemData[7] = bomEditData.getPartNoOld();
			// Part Rev
			rowOldItemData[8] = bomEditData.getPartRevOld();
			// Part Name
			rowOldItemData[9] = bomEditData.getPartNameOld();
			// IC
			rowOldItemData[10] = bomEditData.getIcOld();
			// Supply Mode
			rowOldItemData[11] = bomEditData.getSupplyModeOld();
			// QTY
			rowOldItemData[12] = bomEditData.getQtyOld();
			// ALT
			rowOldItemData[13] = bomEditData.getAltOld();
			// SEL
			rowOldItemData[14] = bomEditData.getSelOld();
			// CAT
			rowOldItemData[15] = bomEditData.getCatOld();
			// Color
			rowOldItemData[16] = bomEditData.getColorIdOld();
			// Color Section
			rowOldItemData[17] = bomEditData.getColorSectionOld();
			// Module Code
			rowOldItemData[18] = bomEditData.getModuleCodeOld();
			// PLT Stk
			rowOldItemData[19] = bomEditData.getPltStkOld();
			// A/S Stk
			rowOldItemData[20] = bomEditData.getAsStkOld();
			// Cost
			rowOldItemData[21] = "";
			// Tool
			rowOldItemData[22] = "";
			// Shown-On
			rowOldItemData[23] = bomEditData.getShownOnOld();
			// Options
			rowOldItemData[24] = bomEditData.getVcOld() != null ? bomEditData.getVcOld().toString() : "";                       
			// Change Desc
			rowOldItemData[25] = bomEditData.getChangeType().equals("D") ? bomEditData.getChgDesc() : "";

			setText(rowOldItemData);
		}

		private void setNewData() {
			if("".equals(StringUtil.nullToString(bomEditData.getPartNoNew()))) {
				return;
			}
			hasPart = true;

			String[] rowNewItemData = new String[26];
			// No.
			rowNewItemData[0] = getRowNo() + "";
			// Proj.
			// [20140417] Project code Old/New 분리 대응
			rowNewItemData[1] = bomEditData.getProjectNew();
			// SEQ
			rowNewItemData[2] = bomEditData.getSeqNew();
			// C/T
			if (!"".equals(StringUtil.nullToString(bomEditData.getPartNoNew()))) {
				rowNewItemData[3] = bomEditData.getChangeType();
			}
			// Parent No, Parent Rev
			if (!"".equals(StringUtil.nullToString(bomEditData.getPartNoNew()))) {
				rowNewItemData[4] = bomEditData.getParentNo();
				rowNewItemData[5] = bomEditData.getParentRev();
			}
			// Part Origin
			rowNewItemData[6] = bomEditData.getPartOriginNew();
			// Part No
			rowNewItemData[7] = bomEditData.getPartNoNew();
			// Part Rev
			rowNewItemData[8] = bomEditData.getPartRevNew();
			// Part Name
			rowNewItemData[9] = bomEditData.getPartNameNew();
			// IC
			rowNewItemData[10] = bomEditData.getIcNew();
			// Supply Mode
			rowNewItemData[11] = bomEditData.getSupplyModeNew();
			// QTY
			rowNewItemData[12] = bomEditData.getQtyNew();
			// ALT
			rowNewItemData[13] = bomEditData.getAltNew();
			// SEL
			rowNewItemData[14] = bomEditData.getSelNew();
			// CAT
			rowNewItemData[15] = bomEditData.getCatNew();
			// Color
			rowNewItemData[16] = bomEditData.getColorIdNew();
			// Color Section
			rowNewItemData[17] = bomEditData.getColorSectionNew();
			// Module Code
			rowNewItemData[18] = bomEditData.getModuleCodeNew();
			// PLT Stk
			rowNewItemData[19] = ""; // rows.get(i).getPltStkOld();
			// A/S Stk
			rowNewItemData[20] = ""; // rows.get(i).getAsStkOld();
			// Cost
			rowNewItemData[21] = bomEditData.getCostNew();
			// Tool
			rowNewItemData[22] = bomEditData.getToolNew();
			// Shown-On
			rowNewItemData[23] = bomEditData.getShownOnNew();
			// Options
			rowNewItemData[24] = bomEditData.getVcNew() != null ? bomEditData.getVcNew().toString() : "";
			// Change Desc
			rowNewItemData[25] = bomEditData.getChgDesc();

			setText(rowNewItemData);
		}

		private int getRowNo() {
			int row = table.indexOf(this);
			return row / 2 + 1;
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

        private void setModifiableCellColor(TableItem item, SYMCBOMEditData bomEditData, boolean isEvenRow, boolean isOld) {
            if(bomEditData.isReplace()) {
                item.setBackground(IC, isEvenRow ? modifiableEvenColor : modifiableColor);
            }
            if (isOld && (bomEditData.isReplace() || bomEditData.getChangeType().equals(SYMCBOMEditData.BOM_CUT))) { // OLD
                item.setBackground(PLT_STK, isEvenRow ? modifiableEvenColor : modifiableColor);
                item.setBackground(AS_STK, isEvenRow ? modifiableEvenColor : modifiableColor);
            } else if(!isOld && (item.getText(PART_NO) != null && !item.getText(PART_NO).equals(""))){ // NEW
                item.setBackground(COST, isEvenRow ? modifiableEvenColor : modifiableColor);
                item.setBackground(TOOL, isEvenRow ? modifiableEvenColor : modifiableColor);
            }
            if(item.getText(PART_NO) != null && !item.getText(PART_NO).equals("")) {
                item.setBackground(CHANGE_DESC, isEvenRow ? modifiableEvenColor : modifiableColor);
            }
        }*/

		public void setRowProperty() {
			int rowNum = getRowNo();
			if(hasPart) {
				setText(0, rowNum + "");
			}
			String ct = bomEditData.getChangeType();
			String parentType = bomEditData.getParentType();
			if(rowNum % 2 == 0) {
				if(bomEditData.isReplace() && !bomEditData.getPartTypeNew().equals("S7_FunctionMast") && isEditable()) {
					setBackground(IC, modifiableEvenColor);
				}
				if(hasPart && isEditable()) {
					if(isOld) {
						setBackground(PLT_STK, modifiableEvenColor);
						setBackground(AS_STK, modifiableEvenColor);
						if(ct.equals("D") && !parentType.equals("S7_Function")) {
							setBackground(CHANGE_DESC, modifiableEvenColor);
						}
					} else {
						setBackground(COST, modifiableEvenColor);
						setBackground(TOOL, modifiableEvenColor);
						if(!ct.equals("D") && !parentType.equals("S7_Function")) {
							setBackground(CHANGE_DESC, modifiableEvenColor);
						}
					}
				}
				setBackground(evenColor);
			} else {
				if(bomEditData.isReplace() && !bomEditData.getPartTypeNew().equals("S7_FunctionMast") && isEditable()) {
					setBackground(IC, modifiableColor);
				}
				if(hasPart && isEditable()) {
					if(isOld) {
						setBackground(PLT_STK, modifiableColor);
						setBackground(AS_STK, modifiableColor);
						if(ct.equals("D") && !parentType.equals("S7_Function")) {
							setBackground(CHANGE_DESC, modifiableColor);
						}
					} else {
						setBackground(COST, modifiableColor);
						setBackground(TOOL, modifiableColor);
						if(!ct.equals("D") && !parentType.equals("S7_Function")) {
							setBackground(CHANGE_DESC, modifiableColor);
						}
					}
				}
				setBackground(table.getBackground());
			}
		}

		protected void checkSubclass() {
		}

	}

	/**
	 * Optios View
	 * 
	 * @method openOptionPopup 
	 * @date 2013. 4. 3.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	private void openOptionPopup(int rowIndex) {
		//ECOEPLSWTOptionsDialog ecoOptionsDialog = new ECOEPLSWTOptionsDialog(getShell(), SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM);
		EPLTableItem selectedItem = (EPLTableItem)table.getItem(rowIndex);
		SYMCBOMEditData editData = selectedItem.getBOMEditData();
		if(editData.getVcNew() == null && editData.getVcOld() == null) {
			return;
		}
		TableItem oldItem = null;
		TableItem newItem = null;
		if(selectedItem.isOld) {
			oldItem = (EPLTableItem)table.getItem(rowIndex);
			newItem = table.getItem(rowIndex+1);
		} else {
			oldItem = table.getItem(rowIndex-1);
			newItem = table.getItem(rowIndex);            
		}
		this.openOptionDialog(oldItem, newItem);

	}

	/**
	 * Options Dialog Open
	 * 
	 * @method openOptionDialog 
	 * @date 2013. 4. 3.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	private void openOptionDialog(TableItem oldItem, TableItem newItem) {
		if(ecoOptionsDialog == null || ecoOptionsDialog.shlEcoOptionsDialog.isDisposed()) {
			ecoOptionsDialog = new ECOEPLSWTOptionsDialog(getShell(), SWT.TITLE  | SWT.DIALOG_TRIM, oldItem, newItem);
			ecoOptionsDialog.open();
		} else {
			ecoOptionsDialog.setOptions(oldItem, newItem);
			SYMDisplayUtil.centerToParent(getShell().getDisplay().getActiveShell(), ecoOptionsDialog.shlEcoOptionsDialog);
		}
	}

}
