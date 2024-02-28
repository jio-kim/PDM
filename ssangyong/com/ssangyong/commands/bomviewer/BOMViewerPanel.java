package com.ssangyong.commands.bomviewer;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.util.MessageBox;

@SuppressWarnings("unchecked")
public class BOMViewerPanel extends Composite {
	private TCComponent component;
	private BOMViewerTablePanel tablePanel;
	
	/**
	 * Constructor
	 * @param parent
	 * @param component
	 * @param style
	 */
	public BOMViewerPanel(Composite parent, int style) {
		super(parent, style);
		setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		
		intializeUI();
		
		this.layout();
	}

	/**
	 * Init UI
	 */
	private void intializeUI() {
		setLayout(new GridLayout());
		setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Table 영역 생성
		createTable();
	}
	
	/**
	 * Table 영역 생성
	 */
	private void createTable() {
		tablePanel = new BOMViewerTablePanel(this, SWT.NONE);
		tablePanel.setLayout(new GridLayout());
		tablePanel.setLayoutData(new GridData(GridData.FILL_BOTH));
	}
	/**
	 * 
	 * @param component
	 */
	public void setComponent(TCComponent component) {
		this.component = component;
	}
	
	/**
	 * 검색
	 */
	public void search() {
		// Search
		try {
			TCComponent target = null;
			if (component == null) {
				return;
			}
			
			// [20240228] 선택한 개체가 없을 경우 MessageBox 표시 
			if (component.getType().equals(BOMViewerConstants.TYPE_FUNCTION) || component.getType().equals(BOMViewerConstants.TYPE_FMP) || component.getType().equals(BOMViewerConstants.TYPE_VEHPART)) {
				TCComponentItem temp = (TCComponentItem) component;
				target = temp.getLatestItemRevision();
			} else if (component.getType().equals(BOMViewerConstants.TYPE_FUNCTION_REV) || component.getType().equals(BOMViewerConstants.TYPE_FMP_REV) || component.getType().equals(BOMViewerConstants.TYPE_VEHPART_REV)) {
				target = component;
			} else {
				MessageBox.post(AIFDesktop.getActiveDesktop(), "개체가 선택되지 않았습니다.", "확인", MessageBox.INFORMATION);
				return;
			}
			
			BOMViewerOperation operation = new BOMViewerOperation(target);
			operation.executeOperation();
			ArrayList<HashMap<String, String>> resultData = (ArrayList<HashMap<String, String>>) operation.getOperationResult();
			tablePanel.setResultData(resultData);
			tablePanel.setFilter();	// Data Filter를 위한 Setting.
			tablePanel.setInput();
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
	}
}
