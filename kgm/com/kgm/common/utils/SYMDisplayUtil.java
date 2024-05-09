package com.kgm.common.utils;

import java.util.HashMap;


import javax.swing.ImageIcon;
import javax.swing.JDialog;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

//import com.swtdesigner.SWTResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.common.TCTypeRenderer;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.ImageUtilities;
import com.teamcenter.rac.util.Registry;

public class SYMDisplayUtil {
	
   public static void centerToScreen(Shell shell)
    {
		Rectangle shellRect = shell.getBounds();
		Rectangle dispRect = Display.getDefault().getBounds();
		int x = (dispRect.width - shellRect.width)/2;
		int y = (dispRect.height - shellRect.height)/2;
		shell.setLocation(x, y);
    }	
	
	public static void centerToParent(Shell parentShell, Shell childShell){
		Rectangle shlParent = parentShell.getBounds();
		Rectangle shlChild = childShell.getBounds();
		int x = parentShell.getLocation().x + (shlParent.width - shlChild.width)/2;
		int y = parentShell.getLocation().y + (shlParent.height - shlChild.height)/2;
		childShell.setLocation(x, y);
		SYMDisplayUtil.doNotDisposeByESC(childShell);
	}
	
	
	public static void centerToParent(Shell parentShell, JDialog aifCommandDialog){
	    Rectangle shlParent = parentShell.getBounds();
	    java.awt.Rectangle shlChild = aifCommandDialog.getBounds();
    	int x = parentShell.getLocation().x + (shlParent.width - shlChild.width)/2;
    	int y = parentShell.getLocation().y + (shlParent.height - shlChild.height)/2;
    	aifCommandDialog.setLocation(x, y);
	}
	
	/**
	 * Method setTableEditor.���̺� ������ ����
	 * @param table ���̺�
	 * @param editor ������
	 * @param item ���̺� ������
	 * @param column Į�� ��ġ
	 */
	public static void setTableTextEditor(final Table table, final TableEditor editor, TableItem item, final int column) {
		/* ������ ������ �����͸� �����Ų�� */
		Control oldEditor = editor.getEditor();
		if (oldEditor != null)
			oldEditor.dispose();

		/* ���ܸ� ���� üũ */
		if (item == null)
			return;

		final Text newEditor = new Text(table, SWT.NONE);
		newEditor.setText(item.getText(column));
		newEditor.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				Text text = (Text) editor.getEditor();
				editor.getItem().setText(column, text.getText());
			}
		});
		
		newEditor.addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent e) {
				if(e.keyCode == 13){
					newEditor.dispose();
				}
			}
			
			public void keyPressed(KeyEvent e) {
			}
		});
		newEditor.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				newEditor.dispose();
			}
		});
		newEditor.selectAll();
		newEditor.setFocus();
		editor.setEditor(newEditor, item, column);
	}
	

	/**
	 * ��ǰ ���̺� �� Renderer
	 * @param table
	 * @return
	 * @author s.j park
	 */
	public static final TableViewer setItemTableViewer(Table table){
	    final String STCPART_REVISION = "STCPartRevision";
        final String STCSW_REVISION = "STCSWRevision";
        final String STCNPI_REVISION = "STCNPIRevision";
        final String STCDOCUMENT_REVISION = "STCDocumentRevision";
	    TableViewer t_viewer = new TableViewer(table);
	    
	    t_viewer.setLabelProvider(new ITableLabelProvider()
	    {
	
	        @Override
	        public Image getColumnImage(Object element, int columnIndex) {
				TCComponentItemRevision target_rev = (TCComponentItemRevision)element;
				switch(columnIndex){
				case 0:
				    if(target_rev.getType().equals(STCPART_REVISION)){
						return SWTResourceManager.getImage(SYMDisplayUtil.class, "/com/ssangyong/common/images/itemrevision_16.png");
				    }else if(target_rev.getType().equals(STCDOCUMENT_REVISION)){
						return SWTResourceManager.getImage(SYMDisplayUtil.class, "/com/ssangyong/common/images/document_16.png");
				    }else if(target_rev.getType().equals(STCSW_REVISION)){
				        return SWTResourceManager.getImage(SYMDisplayUtil.class, "/com/ssangyong/common/images/SoftwareRevision16.png");
				    }else if(target_rev.getType().equals(STCNPI_REVISION)){
				        return SWTResourceManager.getImage(SYMDisplayUtil.class, "/com/ssangyong/common/images/stcnpirevision_16.png");
				    }
				}
				
				return null;
			}
	
	        @Override
	        public String getColumnText(Object element, int columnIndex)
	        {
	            try
	            {
	                TCComponentItemRevision target_rev = (TCComponentItemRevision)element;
	                switch(columnIndex){
	                    case 0:
	                        String item_id = "";
                            if(target_rev.getType().equals(STCPART_REVISION) || target_rev.getType().equals(STCNPI_REVISION))
                                item_id = target_rev.getItem().getProperty("STCStandardDrawingNumber");    //���浵��
                            else
                                item_id = target_rev.getProperty("item_id");   //������ȣ, sw
                            return item_id;
	                    case 1:
	                        return target_rev.getProperty("item_revision_id");		//������
	                    case 2:
	                        return target_rev.getProperty("object_name");			//�����۸�
	                   }
	            }
	            catch (Exception e)
	            {
	               e.printStackTrace();
	            }
	            return null;
	        }
	
	        @Override
	        public void addListener(ILabelProviderListener paramILabelProviderListener)
	        {
	            
	        }
	
	        @Override
	        public void dispose()
	        {
	            
	        }
	
	        @Override
	        public boolean isLabelProperty(Object paramObject, String paramString)
	        {
	            return false;
	        }
	
	        @Override
	        public void removeListener(ILabelProviderListener paramILabelProviderListener)
	        {
	            
	        }
	    });
	    return t_viewer;
	}

	/**
	 * ����� ���̺� �� Renderer
	 * @param table
	 * @return
	 * @author s.j park
	 */
	public static final TableViewer setUserTableViewer(Table table){
	    TableViewer t_viewer = new TableViewer(table);
	    
	    t_viewer.setLabelProvider(new ITableLabelProvider()
	    {
	
	        @Override
	        public Image getColumnImage(Object element, int columnIndex) {
				switch(columnIndex){
				case 1:
						return SWTResourceManager.getImage(SYMDisplayUtil.class, "/com/ssangyong/common/images/user_16.png");
				}
				
				return null;
			}
	
	        @SuppressWarnings("unchecked")
            @Override
	        public String getColumnText(Object element, int columnIndex)
	        {
	            try
	            {
	                HashMap<String, String> target_rev = (HashMap<String, String>)element;
	                switch(columnIndex){
	                	case 0:
	                		return target_rev.get("deptNm");		//�μ���
	                    case 1:
	                        return target_rev.get("userId");		//���(TC userID)
	                    case 2:
	                        return target_rev.get("userNm");		//�̸�
	                    case 3:
	                        return target_rev.get("position");		//��å
	                    case 4:
	                    	return target_rev.get("etc");			//����������� or ���
	                   }
	            }
	            catch (Exception e)
	            {
	               e.printStackTrace();
	            }
	            return null;
	        }
	
	        @Override
	        public void addListener(ILabelProviderListener paramILabelProviderListener)
	        {
	            
	        }
	
	        @Override
	        public void dispose()
	        {
	            
	        }
	
	        @Override
	        public boolean isLabelProperty(Object paramObject, String paramString)
	        {
	            return false;
	        }
	
	        @Override
	        public void removeListener(ILabelProviderListener paramILabelProviderListener)
	        {
	            
	        }
	    });
	    return t_viewer;
	}

	/**
	 * �ʵ��� ���� ���� Byte���� �Ѱ��ָ� Key Event�� ����Ʈ�� ����Ͽ� Ű�� ���� �ɾ���.
	 * @param e
	 * @param fieldValue
	 * @param byteCount
	 * @author s.j park
	 */
	public static final void setTextLimit(Event e, String fieldValue, int byteCount)
	{
        if(e.keyCode != 8 && e.keyCode != 127 && e.keyCode != 37 && e.keyCode != 39)
        {
            int keySize = SYMStringUtil.getByteSizeToComplex(e.text);
            int byteSize = SYMStringUtil.getByteSizeToComplex(fieldValue);

            if(byteSize + keySize > byteCount)
            {
                e.doit = false;
                return;
            }
            else if (byteSize >= byteCount)
            {
                e.doit = false;
                return;
            }
        }
	}
	
	public static ControlDecoration setRequiredFieldSymbol (Control comp) {
		ControlDecoration controldecoration = new ControlDecoration(comp, 0x20080);
    	FieldDecorationRegistry default1 = FieldDecorationRegistry.getDefault();
    	Registry registry = Registry.getRegistry("com.kgm.common.common");
    	default1.registerFieldDecoration("CONTROL_MANDATORY", "CONTROL_MANDATORY", registry.getImage("CONTROL_MANDATORY"));
		return setRequiredFieldSymbol(controldecoration, comp, "CONTROL_MANDATORY", null, false);
	}
	
	public static ControlDecoration setRequiredFieldSymbol(ControlDecoration controldecoration, Control comp, String style, String desc, boolean isShowOnlyFocus){
    	FieldDecoration requiredDecorator = FieldDecorationRegistry.getDefault().getFieldDecoration(style);
    	if(desc==null){
    		controldecoration.setDescriptionText(requiredDecorator.getDescription());
    	}else{
    		controldecoration.setDescriptionText(desc);
    	}
        controldecoration.setImage(requiredDecorator.getImage());
        controldecoration.setShowOnlyOnFocus(isShowOnlyFocus);
        controldecoration.setShowHover(true);
        return controldecoration;
    }   
	
	  /**
     * �ʼ� �ʵ� ������Ʈ Decotation 
     * @param comp
     * @param style
     * @author s.j park
     */
    public static ControlDecoration setRequiredFieldSymbol(Control comp, String style, boolean isShowOnlyFocus){
    	return setRequiredFieldSymbol(comp, style, null, isShowOnlyFocus);
    }
    
    public static ControlDecoration setRequiredFieldSymbol(Control comp, String style, String desc, boolean isShowOnlyFocus){
    	ControlDecoration controldecoration = new ControlDecoration(comp, 0x20080);
    	FieldDecoration requiredDecorator = FieldDecorationRegistry.getDefault().getFieldDecoration(style);
    	if(desc==null){
    		controldecoration.setDescriptionText(requiredDecorator.getDescription());
    	}else{
    		controldecoration.setDescriptionText(desc);
    	}
        controldecoration.setImage(requiredDecorator.getImage());
        controldecoration.setShowOnlyOnFocus(isShowOnlyFocus);
        controldecoration.setShowHover(true);
        return controldecoration;
    }  
    
     
    
    
    /**
     * SWT UI��ü�� �� �Է��� Enable���� ���� �����Ѵ�.
     * @param uiComponent
     * @param value
     * @param isEnabled
     */
    public static void setViewValue(Object uiComponent, String value, boolean isEnabled){
    	if(uiComponent instanceof org.eclipse.swt.widgets.Combo){
    		org.eclipse.swt.widgets.Combo combo = (org.eclipse.swt.widgets.Combo)uiComponent;
    		if(value == null || value.equals("")){
    			combo.select(0);
    			combo.setEnabled(isEnabled);
				return;
    		}
    		for(int i=0; i<combo.getItemCount(); i++){
				if(combo.getItem(i).equals(value)){
					combo.select(i);
					combo.setEnabled(isEnabled);
					return;
				}
			}
    	}else if(uiComponent instanceof org.eclipse.swt.widgets.Text){
    		org.eclipse.swt.widgets.Text txt = (org.eclipse.swt.widgets.Text)uiComponent;
    		txt.setText(value);
    		txt.setEnabled(isEnabled);
    	}else if(uiComponent instanceof org.eclipse.swt.widgets.Button){
    		
    	}else if(uiComponent instanceof org.eclipse.swt.widgets.Composite){
    		org.eclipse.swt.widgets.Composite composite = (org.eclipse.swt.widgets.Composite)uiComponent; 
    		Control[] control = composite.getChildren();
    		if(control.length == 2 && control[0] instanceof org.eclipse.swt.widgets.Button
    				&& control[1] instanceof org.eclipse.swt.widgets.Button){ 
    			
    			if(value.toUpperCase().equals("TRUE")){
    				value = "YES";
    			}else if(value.toUpperCase().equals("FALSE")){
    				value = "NO";
    			}
    			// Button�� Radio����, CheckBox������ �������� �������⸦ ���Ŀ� �߰��ؾ���.
    			if(((org.eclipse.swt.widgets.Button)control[0]).getText().toUpperCase().equals(value)){
    				((org.eclipse.swt.widgets.Button)control[0]).setSelection(true);
    				((org.eclipse.swt.widgets.Button)control[1]).setSelection(false);
    			}else{
    				((org.eclipse.swt.widgets.Button)control[0]).setSelection(false);
    				((org.eclipse.swt.widgets.Button)control[1]).setSelection(true);
    			}
    			control[0].setEnabled(isEnabled);
    			control[1].setEnabled(isEnabled);
    			composite.setEnabled(isEnabled);
    		}
    	}
    }

    /**
     * ������ Type�� �ش��ϴ� Icon�� �������� Function
     * @param target �ش� Type�� Target
     * @return AWT ImageIcon
     */
    public static ImageIcon getTcTypeAWTIcon(InterfaceAIFComponent target)
    {
        return getTcTypeAWTIcon((TCComponent)target);
    }
    
    /**
     * ������ Tpyp�� �ش��ϴ� SWT Image�� �������� Function
     * @param target �ش� Type�� Target
     * @return SWT Image
     */
    public static Image getTCTypeSWTIcon(InterfaceAIFComponent target)
    {
        return getTCTypeSWTIcon((TCComponent)target);
    }

    /**
     * ������ Type�� �ش��ϴ� Icon�� �������� Function
     * @param target �ش� Type�� Target
     * @return AWT ImageIcon
     */
    public static ImageIcon getTcTypeAWTIcon(TCComponent target)
    {
        ImageIcon returnImageIcon = null;
        returnImageIcon = TCTypeRenderer.getTypeIcon(target.getTypeComponent(), "" );
        return returnImageIcon;
    }

    /**
     * ������ Tpyp�� �ش��ϴ� SWT Image�� �������� Function
     * @param target �ش� Type�� Target
     * @return SWT Image
     */
    public static Image getTCTypeSWTIcon(TCComponent target)
    {
        Image returnImage = null;
        returnImage = ImageUtilities.getImageDescriptor(getTcTypeAWTIcon(target)).createImage();
        return returnImage;
    }

   
    /**
     * ������ Tpyp�� �ش��ϴ� AWT ImageIcon�� �������� Function
     * @param strType Component�� Object_type �Ӽ�
     * @return
     * @throws Exception
     */
    public static ImageIcon getTcTypeAWTIcon(String strType) throws Exception
    {
        ImageIcon returnImage = null;
        Registry r = Registry.getRegistry("com.teamcenter.rac.common.common");
        returnImage = r.getImageIcon(strType + ".ICON");
        return returnImage;
    }
    
    /**
     * ������ Type�� �ش��ϴ� SWT Image�� �������� Function
     * @param strType Component�� Object_type �Ӽ�
     * @return
     * @throws Exception
     */
    public static Image getTCTypeSWTIcon(String strType) throws Exception
    {
        Image returnImage = null;
        returnImage = ImageUtilities.getImageDescriptor(getTcTypeAWTIcon(strType)).createImage();
        return returnImage;
    }

    /**
     * SWT�� �⺻ ����� ESC�� ������ ȭ���� �ݱ�� ������ �����ϴ� �Լ�
     * 
     * @param shell
     */
    public static void doNotDisposeByESC(Shell shell)
    {
        shell.addListener(SWT.Traverse, new Listener()
        {
            public void handleEvent(Event event)
            {
                if(event.detail == SWT.TRAVERSE_ESCAPE)
                {
                    event.doit = false;
                }
            }
        });
    }
    
}
