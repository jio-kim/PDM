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
	 * Method setTableEditor.테이블 에디터 설정
	 * @param table 테이블
	 * @param editor 에디터
	 * @param item 테이블 아이템
	 * @param column 칼럼 위치
	 */
	public static void setTableTextEditor(final Table table, final TableEditor editor, TableItem item, final int column) {
		/* 이전에 생성된 에디터를 종료시킨다 */
		Control oldEditor = editor.getEditor();
		if (oldEditor != null)
			oldEditor.dispose();

		/* 예외를 위한 체크 */
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
	 * 부품 테이블 뷰 Renderer
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
                                item_id = target_rev.getItem().getProperty("STCStandardDrawingNumber");    //국방도번
                            else
                                item_id = target_rev.getProperty("item_id");   //문서번호, sw
                            return item_id;
	                    case 1:
	                        return target_rev.getProperty("item_revision_id");		//리비젼
	                    case 2:
	                        return target_rev.getProperty("object_name");			//아이템명
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
	 * 사용자 테이블 뷰 Renderer
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
	                		return target_rev.get("deptNm");		//부서명
	                    case 1:
	                        return target_rev.get("userId");		//사번(TC userID)
	                    case 2:
	                        return target_rev.get("userNm");		//이름
	                    case 3:
	                        return target_rev.get("position");		//직책
	                    case 4:
	                    	return target_rev.get("etc");			//등급판정사유 or 비고
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
	 * 필드의 값과 제안 Byte수를 넘겨주면 Key Event가 바이트를 계산하여 키인 제안 걸어줌.
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
     * 필수 필드 컴포넌트 Decotation 
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
     * SWT UI객체에 값 입력후 Enable할지 여부 설정한다.
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
    			// Button이 Radio인지, CheckBox인지에 대한정보 가져오기를 차후에 추가해야함.
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
     * 팀센터 Type에 해당하는 Icon을 가져오는 Function
     * @param target 해당 Type의 Target
     * @return AWT ImageIcon
     */
    public static ImageIcon getTcTypeAWTIcon(InterfaceAIFComponent target)
    {
        return getTcTypeAWTIcon((TCComponent)target);
    }
    
    /**
     * 팀센터 Tpyp에 해당하는 SWT Image를 가져오는 Function
     * @param target 해당 Type의 Target
     * @return SWT Image
     */
    public static Image getTCTypeSWTIcon(InterfaceAIFComponent target)
    {
        return getTCTypeSWTIcon((TCComponent)target);
    }

    /**
     * 팀센터 Type에 해당하는 Icon을 가져오는 Function
     * @param target 해당 Type의 Target
     * @return AWT ImageIcon
     */
    public static ImageIcon getTcTypeAWTIcon(TCComponent target)
    {
        ImageIcon returnImageIcon = null;
        returnImageIcon = TCTypeRenderer.getTypeIcon(target.getTypeComponent(), "" );
        return returnImageIcon;
    }

    /**
     * 팀센터 Tpyp에 해당하는 SWT Image를 가져오는 Function
     * @param target 해당 Type의 Target
     * @return SWT Image
     */
    public static Image getTCTypeSWTIcon(TCComponent target)
    {
        Image returnImage = null;
        returnImage = ImageUtilities.getImageDescriptor(getTcTypeAWTIcon(target)).createImage();
        return returnImage;
    }

   
    /**
     * 팀센터 Tpyp에 해당하는 AWT ImageIcon를 가져오는 Function
     * @param strType Component의 Object_type 속성
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
     * 팀센터 Type에 해당하는 SWT Image를 가져오는 Function
     * @param strType Component의 Object_type 속성
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
     * SWT의 기본 기능인 ESC를 누를때 화면이 닫기는 현상을 제거하는 함수
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
