package com.symc.plm.rac.prebom.prebom.rendering;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.symc.plm.rac.prebom.common.viewer.AbstractPreSYMCViewer;
import com.symc.plm.rac.prebom.prebom.view.prefuncmaster.PreFuncMasterInfoPanel;
import com.teamcenter.rac.util.IPageComplete;

/**
 * Function Master Part ViewerTab에서 사용하는 Rendering Class
 * 
 * ### 설정 방법 #####
 * properties) viewer.properties => S7_PreFuncMasterRevision.COMPONENTVIEWER=com.symc.plm.rac.prebom.prebom.rendering.SYMCPreFuncMasterRevisionRendering
 * preference) defaultViewerConfig => S7_PreFuncMasterRevision.SYMCPropertyViewer
 */
public class SYMCPreFuncMasterRevisionRendering extends AbstractPreSYMCViewer implements IPageComplete
{

	private PreFuncMasterInfoPanel infoPanel;

	public SYMCPreFuncMasterRevisionRendering(Composite parent)
	{
		super(parent);
	}

	/**
	 * 화면 생성
	 */
	@Override
	public void createPanel(Composite parent)
	{
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		infoPanel = new PreFuncMasterInfoPanel(sc, SWT.NONE, true);
		infoPanel.setViewMode();

		// Composite composite = infoPanel.getComposite();
		sc.setContent(infoPanel);
		sc.setMinSize(800, 600);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

	}

	/**
	 * 저장 여부 확인
	 */
	@Override
	public boolean isDirty()
	{
		return infoPanel.isModified();
	}

	@Override
	public boolean isPageComplete()
	{

		return true;
	}

	@Override
	public void load()
	{

	}

	public void setControlReadWrite(Control control)
	{
		super.setControlReadWrite(control);
		infoPanel.setViewMode();
	}

	@Override
	public void save()
	{

		infoPanel.saveAction();
	}

	@Override
	public boolean isSavable()
	{

		return infoPanel.isSavable();
	}
}
