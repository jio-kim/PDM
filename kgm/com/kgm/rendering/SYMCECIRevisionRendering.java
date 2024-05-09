package com.kgm.rendering;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;

import com.kgm.commands.ec.eci.ECISWTRendering;
import com.kgm.viewer.AbstractSYMCViewer;

public class SYMCECIRevisionRendering extends AbstractSYMCViewer{
	
	private ECISWTRendering eciInfoRender;
	
	/**
	 * 생성자
	 * @param parent
	 */
	public SYMCECIRevisionRendering(Composite parent) {
	    super(parent);
	}
	
	/**
	 * 저장
	 */
	@Override
    public void save() {//TODO
		eciInfoRender.save();
    }
    
    /**
     * 저장 여부 확인
     */
    @Override
    public boolean isDirty() { //TODO
    	return eciInfoRender.isModified();
    }
    
    /**
     * 화면 생성
     */
    @Override
    public void createPanel(Composite parent) {

    	ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        eciInfoRender = new ECISWTRendering(sc);
        
        /**
         * [SR140701-022] jclee, ECI View모드로 화면 오픈. 
         */
        eciInfoRender.setCreateMode(false);
//        Composite composite = eciInfoRender.getComposite();
        /**
         * [SR140701-022] jclee, ECI 화면 크기 조정.
         */
        sc.setContent(eciInfoRender);
        
        sc.setMinSize(720, 805);
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);
    }
    
    /**
     * Refresh
     */
    @Override
    public void load() {
    	eciInfoRender.setProperties();
    }

	@Override
	public boolean isSavable() {
		return eciInfoRender.isSavable();
	}
    
}
