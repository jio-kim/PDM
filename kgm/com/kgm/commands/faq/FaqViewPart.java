package com.kgm.commands.faq;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.kgm.common.remote.DataSet;

import swing2swt.layout.BorderLayout;

import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;

/**
 * [SR150421-027][20150811][ymjang] PLM system 개선사항 - Manual 조회 관리기능 추가
 * [NON-SR][ymjang] 유형 항목 변경 --> 설계, 기관으로
 */
public class FaqViewPart extends ViewPart {

    private FaqQueryService faqQueryService;
    private AddfileQueryService addfileQueryService;
    private FaqView faqView;

    private Shell parentShell;

    private Table table;
	private Text textSearch;
	private Button btnRadioTitle;
	private Button btnRadioContent;

    public FaqViewPart() {
        parentShell = Display.getCurrent().getActiveShell();
        if (parentShell == null) {
            parentShell = Display.getCurrent().getShells()[0];
        }

        faqQueryService = new FaqQueryService();
        addfileQueryService = new AddfileQueryService();
        faqView = new FaqView(parentShell);
    }

    @Override
    public void createPartControl(Composite paramComposite) {
        initUI(paramComposite);
    }

    @Override
    public void setFocus() {

    }

    public void initUI(Composite paramComposite) {
        setTitleImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, FaqConstant.FAQ_IMAGE_PATH));

        Composite composite = new Composite(paramComposite, SWT.NONE);
        composite.setLayout(new BorderLayout(0, 0));

        Composite north_composite = new Composite(composite, SWT.NONE);
        north_composite.setLayoutData(BorderLayout.NORTH);
        north_composite.setLayout(new GridLayout(5, false));

        ToolBar toolBar = new ToolBar(north_composite, SWT.FLAT | SWT.RIGHT);

        ToolItem refreshToolItem = new ToolItem(toolBar, SWT.NONE);
        refreshToolItem.setToolTipText("Refresh");
        refreshToolItem.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, FaqConstant.REFRESH_IMAGE_PATH));
        
        textSearch = new Text(north_composite, SWT.BORDER);
        GridData gd_textSearch = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_textSearch.widthHint = 200;
        textSearch.setLayoutData(gd_textSearch);
        textSearch.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
        
        refreshToolItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
				DataSet dataSet = new DataSet();
				dataSet.put("NO-PARAM", null);
				refreshTable(dataSet);
            }
        });

        Button btnSearch = new Button(north_composite, SWT.NONE);
		btnSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doSearch();
			}
		});

		btnSearch.setToolTipText("Search");
		btnSearch.setImage(SWTResourceManager.getImage(FaqViewPart.class, "/icons/search_16.png"));
		GridData gd_btnSave = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		btnSearch.setLayoutData(gd_btnSave);
		this.getSite().getShell().setDefaultButton(btnSearch);


		btnRadioTitle = new Button(north_composite, SWT.RADIO);
		btnRadioTitle.setText("\uC81C\uBAA9");
		btnRadioTitle.setSelection(true);

		btnRadioContent = new Button(north_composite, SWT.RADIO);
		btnRadioContent.setText("\uC81C\uBAA9 + \uB0B4\uC6A9");

        
        Composite center_composite = new Composite(composite, SWT.NONE);
        center_composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
        center_composite.setLayoutData(BorderLayout.CENTER);
        center_composite.setLayout(new GridLayout(1, false));

        table = new Table(center_composite, SWT.BORDER | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                Table table = (Table) e.getSource();
                TableItem tableItem = table.getItem(table.getSelectionIndex());
                faqView.detailFaqView(tableItem);
            }
        });

        // 번호
        createTableColumn("\uBC88\uD638", 50, SWT.CENTER);
        // 유형
        createTableColumn("\uc720\ud615", 50, SWT.CENTER);        
        // 제목
        createTableColumn("\uC81C\uBAA9", 500, SWT.LEFT);
        // 작성자
        createTableColumn("\uc791\uc131\uc790", 100, SWT.CENTER);
        // 작성일시
        createTableColumn("\uc791\uc131\uc77c\uc2dc", 150, SWT.CENTER);
        
		DataSet dataSet = new DataSet();
		dataSet.put("NO-PARAM", null);
		refreshTable(dataSet);
    }

    /**
     *
     *
     * @method createTableColumn
     * @date 2014. 3. 20.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void createTableColumn(String columnName, int width, int align) {
        TableColumn tableColumn = new TableColumn(table, SWT.NONE);
        tableColumn.setText(columnName);
        tableColumn.setWidth(width);
		tableColumn.setAlignment(align);
    }

    /**
     *
     *
     * @method refreshTable
     * @date 2014. 3. 10.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    public void refreshTable(DataSet inputDataSet){
        table.removeAll();

        ArrayList<HashMap<String, Object>> faqList = faqQueryService.selectFaqList(inputDataSet);
        if (faqList == null) {
            //MessageDialog.openError(parentShell, "Error", "Error has occurred while getting the list of FAQ." + "\n" + "Connection refused." + "\n" + "Please contact to administrator.");
            return;
        }

        DataSet dataSet = null;
        for (int i = 0; i < faqList.size(); i++) {
            HashMap<String, Object> faqMap = faqList.get(i);

            String ouid = (String) faqMap.get("FAQ_PUID");
            BigDecimal seq = (BigDecimal) faqMap.get("FAQ_SEQ");
            String faq_type = (String) faqMap.get("FAQ_TYPE");
            String title = (String) faqMap.get("TITLE");
            String contents = (String) faqMap.get("CONTENTS");
            String create_user = (String) faqMap.get("CREATE_USER");
            String creation_date = (String) faqMap.get("CREATE_DATE");

            TableItem tableItem = new TableItem(table, SWT.NONE);
            tableItem.setText(0, String.valueOf(seq));
            tableItem.setText(1, faq_type.equals("1") ?  "설계"  : "기관");
            tableItem.setText(2, title);
            tableItem.setText(3, create_user);
            tableItem.setText(4, creation_date);
            
            // 첨부파일목록 조회
            dataSet = new DataSet();
            dataSet.put("addfile_puid", ouid);
            ArrayList<HashMap<String, Object>> addfileList = addfileQueryService.selectAddfileList(dataSet);
            
            tableItem.setData("addfile_puid", ouid);
            tableItem.setData("faq_type", faq_type);
            tableItem.setData("title", title);
            tableItem.setData("contents", contents);
            tableItem.setData("create_user", create_user);
            tableItem.setData("creation_date", creation_date);
            tableItem.setData("addfileList", addfileList);
        }
    }
    
    /**
     * 검색
     */
    protected void doSearch() {
		DataSet dataSet = new DataSet();
		boolean isTitleSelect = btnRadioTitle.getSelection();

		String inputText = textSearch.getText();

		if (!inputText.isEmpty()) {
			if (isTitleSelect) {
				dataSet.put("TITLE", inputText);
			} else {
				dataSet.put("CONTENTS", inputText);
			}
		}else
			dataSet.put("NO-PARAM", null);

		refreshTable(dataSet);

	}

}
