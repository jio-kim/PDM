package com.kgm.rendering;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.kgm.commands.partmaster.project.ProjectInfoPanel;
import com.kgm.viewer.AbstractSYMCViewer;
import com.teamcenter.rac.util.IPageComplete;

/**
 * Project Part ViewerTab���� ����ϴ� Rendering Class
 * 
 * ### ���� ��� #####
 * properties) viewer.properties => S7_ProjectRevision.COMPONENTVIEWER=com.kgm.rendering.SYMCProjectRevisionRendering
 * preference) defaultViewerConfig => S7_ProjectRevision.SYMCPropertyViewer
 */
public class SYMCProjectRevisionRendering extends AbstractSYMCViewer implements IPageComplete
{

	private ProjectInfoPanel infoPanel;

	public SYMCProjectRevisionRendering(Composite parent)
	{
		super(parent);
	}

	/**
	 * ȭ�� ����
	 */
	@Override
	public void createPanel(Composite parent)
	{
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		infoPanel = new ProjectInfoPanel(sc, SWT.NONE, true);
		infoPanel.setViewMode();

		// Composite composite = infoPanel.getComposite();
		sc.setContent(infoPanel);
		sc.setMinSize(800, 600);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

	}

	/**
	 * ���� ���� Ȯ��
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
