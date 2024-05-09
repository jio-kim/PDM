package com.kgm.rendering;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;

import com.kgm.commands.ec.eci.ECISWTRendering;
import com.kgm.viewer.AbstractSYMCViewer;

public class SYMCECIRevisionRendering extends AbstractSYMCViewer{
	
	private ECISWTRendering eciInfoRender;
	
	/**
	 * ������
	 * @param parent
	 */
	public SYMCECIRevisionRendering(Composite parent) {
	    super(parent);
	}
	
	/**
	 * ����
	 */
	@Override
    public void save() {//TODO
		eciInfoRender.save();
    }
    
    /**
     * ���� ���� Ȯ��
     */
    @Override
    public boolean isDirty() { //TODO
    	return eciInfoRender.isModified();
    }
    
    /**
     * ȭ�� ����
     */
    @Override
    public void createPanel(Composite parent) {

    	ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        eciInfoRender = new ECISWTRendering(sc);
        
        /**
         * [SR140701-022] jclee, ECI View���� ȭ�� ����. 
         */
        eciInfoRender.setCreateMode(false);
//        Composite composite = eciInfoRender.getComposite();
        /**
         * [SR140701-022] jclee, ECI ȭ�� ũ�� ����.
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
