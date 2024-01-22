package com.symc.plm.rac.prebom.prebom.rendering;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.symc.plm.rac.prebom.common.viewer.AbstractPreSYMCViewer;
import com.symc.plm.rac.prebom.prebom.view.preproduct.PreProductInfoPanel;
import com.teamcenter.rac.util.IPageComplete;


/**
 * Product Part ViewerTab에서 사용하는 Rendering Class
 * 
 * ### 설정 방법 #####
 * properties) viewer.properties => S7_PreProductRevision.COMPONENTVIEWER=com.symc.plm.rac.prebom.prebom.rendering.SYMCPreProductRevisionRendering
 * preference) defaultViewerConfig => S7_PreProductRevision.SYMCPropertyViewer
 */
public class SYMCPreProductRevisionRendering extends AbstractPreSYMCViewer implements IPageComplete
{

	private PreProductInfoPanel infoPanel;

	public SYMCPreProductRevisionRendering(Composite parent)
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
		infoPanel = new PreProductInfoPanel(sc, SWT.NONE, true);
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
