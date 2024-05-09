package com.kgm.common.swtsearch;

import org.eclipse.swt.SWT;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
//import com.swtdesigner.SWTResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.kgm.common.SYMLabel;

public class SWTDataSetSearch extends Dialog {

	@SuppressWarnings("unused")
    private List list;

	/**
	 * 생성자.
	 * @copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2013. 1. 7.
	 * @param parent
	 */
	public SWTDataSetSearch(Shell parent, List list) {
		super(parent);
		this.list = list;
	}
	
	/**
	 * Start Dialog.
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2013. 1. 7.
	 */
	public void open(){
		Shell shell = new Shell(getParent());
		shell.setText("DataSet Search.");
		
		shell.setLayout(new GridLayout(2, false));
		shell.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		
		Group mainGroup = new Group(shell, SWT.NONE);
		mainGroup.setText("Search Panel");
	    mainGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		mainGroup.setBounds(0, 0, 746, 285);
		mainGroup.setLayout(new GridLayout(2, false));
		
		SYMLabel nameLabel = new SYMLabel(mainGroup, SWT.RIGHT_TO_LEFT);
		nameLabel.setText("DataSet Name");
		
		Text nameText = new Text(mainGroup, SWT.NONE | SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		shell.open();
	}
}
