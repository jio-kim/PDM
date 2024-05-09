package com.kgm.rendering;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.kgm.commands.partmaster.productpart.ProdPartMasterInfoPanel;
import com.kgm.viewer.AbstractSYMCViewer;
import com.teamcenter.rac.util.IPageComplete;


/**
 * Product Part ViewerTab에서 사용하는 Rendering Class
 * 
 * ### 설정 방법 #####
 * properties) viewer.properties => S7_ProductRevision.COMPONENTVIEWER=com.kgm.rendering.SYMCProductRevisionRendering
 * preference) defaultViewerConfig => S7_ProductRevision.SYMCPropertyViewer
 */
public class SYMCProductpartRevisionRendering extends AbstractSYMCViewer implements IPageComplete
{

	private ProdPartMasterInfoPanel infoPanel;

	public SYMCProductpartRevisionRendering(Composite parent)
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
		infoPanel = new ProdPartMasterInfoPanel(sc, SWT.NONE, true);
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
	{ // TODO
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
