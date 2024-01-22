package com.symc.plm.rac.prebom.prebom.rendering;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.symc.plm.rac.prebom.common.viewer.AbstractPreSYMCViewer;
import com.symc.plm.rac.prebom.prebom.view.prefunction.PreFunctionInfoPanel;
import com.teamcenter.rac.util.IPageComplete;

/**
 * Function Part ViewerTab에서 사용하는 Rendering Class
 * 
 * ### 설정 방법 #####
 * properties) viewer.properties => S7_PreFunctionRevision.COMPONENTVIEWER=com.symc.plm.rac.prebom.prebom.rendering.SYMCPreFunctionRevisionRendering
 * preference) defaultViewerConfig => S7_PreFunctionRevision.SYMCPropertyViewer
 */
public class SYMCPreFunctionRevisionRendering extends AbstractPreSYMCViewer implements IPageComplete
{

  private PreFunctionInfoPanel infoPanel;


  public SYMCPreFunctionRevisionRendering(Composite parent)
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
    infoPanel = new PreFunctionInfoPanel(sc, SWT.NONE, true);
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
