/**
 * 
 */
package com.symc.plm.me.sdv.view.body;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVText;
import com.symc.plm.me.sdv.operation.body.CopyToAlternativeBOPInitOperation;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.util.Registry;

/**
 *
 */
public class CopyToAlternativeBOPView extends AbstractSDVViewPane {
	private Registry registry = null;
    private SDVText textTargetBOP;
    private SDVText textAltPrefix;
    private Button btnWithEI;

	/**
	 * @param parent
	 * @param style
	 * @param id
	 * @wbp.parser.constructor
	 */
	public CopyToAlternativeBOPView(Composite parent, int style, String id) {
		super(parent, style, id);
	}

	/**
	 * @param parent
	 * @param style
	 * @param id
	 * @param configId
	 */
	public CopyToAlternativeBOPView(Composite parent, int style, String id, int configId) {
		super(parent, style, id, configId);
	}

	/**
	 * @param parent
	 * @param style
	 * @param id
	 * @param configId
	 * @param order
	 */
	public CopyToAlternativeBOPView(Composite parent, int style, String id, int configId, String order) {
		super(parent, style, id, configId, order);
	}

    /* (non-Javadoc)
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initUI()
     */
	@Override
	protected void initUI(Composite parent)	{
		registry = Registry.getRegistry(CopyToAlternativeBOPView.class);

		Composite mainView = new Composite(parent, SWT.NONE);
        mainView.setLayout(new GridLayout(1, false));
        
        Group copyToAltGroup = new Group(mainView, SWT.NONE);
        copyToAltGroup.setLayout(new GridLayout(3, false));
        copyToAltGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        
        Label lblTargetBOP = new Label(copyToAltGroup, SWT.NONE);
        lblTargetBOP.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblTargetBOP.setText(registry.getString("CopyToAlternativeBOP.TargetItem.LABEL", "Target BOP"));
        
        textTargetBOP = new SDVText(copyToAltGroup, SWT.BORDER | SWT.READ_ONLY);
        textTargetBOP.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        textTargetBOP.setMandatory(true);
        
        Label lblDestinationBOP = new Label(copyToAltGroup, SWT.NONE);
        lblDestinationBOP.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblDestinationBOP.setText(registry.getString("CopyToAlternativeBOP.AltPrefix.LABEL", "Alternative Prefix"));
        
        textAltPrefix = new SDVText(copyToAltGroup, SWT.BORDER | SWT.SINGLE);
        GridData gd_textDestinationBOP = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_textDestinationBOP.widthHint = 80;
        textAltPrefix.setLayoutData(gd_textDestinationBOP);
        textAltPrefix.setTextLimit(4);
        textAltPrefix.setInputType(SDVText.ENGUPPERNUM);
        textAltPrefix.setMandatory(true);
        new Label(copyToAltGroup, SWT.NONE);
        
        btnWithEI = new Button(copyToAltGroup, SWT.CHECK);
        btnWithEI.setText(registry.getString("CopyToAlternativeBOP.ContainEI.LABEL", "with EndItem ?"));
        new Label(copyToAltGroup, SWT.NONE);
        new Label(copyToAltGroup, SWT.NONE);
	}

	/* (non-Javadoc)
	 * @see org.sdv.core.ui.view.AbstractSDVViewPane#setLocalDataMap(org.sdv.core.common.data.IDataMap)
	 */
	@Override
	public void setLocalDataMap(IDataMap dataMap) {
	}

	/* (non-Javadoc)
	 * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalDataMap()
	 */
	@Override
	public IDataMap getLocalDataMap() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalSelectDataMap()
	 */
	@Override
	public IDataMap getLocalSelectDataMap() {
		RawDataMap targetData = new RawDataMap();

		targetData.put(SDVPropertyConstant.WELDOP_REV_TARGET_OP, textTargetBOP.getData(), IData.OBJECT_FIELD);
		targetData.put(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX, textAltPrefix.getText());
		targetData.put("ContainedEI", btnWithEI.getSelection(), IData.BOOLEAN_FIELD);

		return targetData;
	}

	/* (non-Javadoc)
	 * @see org.sdv.core.ui.view.AbstractSDVViewPane#getInitOperation()
	 */
	@Override
	public AbstractSDVInitOperation getInitOperation() {
		return new CopyToAlternativeBOPInitOperation();
	}

	/* (non-Javadoc)
	 * @see org.sdv.core.ui.view.AbstractSDVViewPane#initalizeLocalData(int, org.sdv.core.common.IViewPane, org.sdv.core.common.data.IDataSet)
	 */
	@Override
	public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
		// 오퍼의 결과를 화면에 설정하는 함수
		if (result == SDVInitEvent.INIT_SUCCESS)
		{
			if (dataset != null)
			{
					Object targetObject = (TCComponentBOMLine) dataset.getValue("CopyAltTargetItem");

					if (targetObject == null)
						return;

					try
					{
						textTargetBOP.setText(((TCComponentBOPLine) targetObject).toString());
						textTargetBOP.setData(targetObject);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.sdv.core.ui.view.AbstractSDVViewPane#uiLoadCompleted()
	 */
	@Override
	public void uiLoadCompleted() {
	}

}
