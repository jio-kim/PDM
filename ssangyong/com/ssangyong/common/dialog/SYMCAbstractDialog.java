package com.ssangyong.common.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.ssangyong.common.utils.SYMDisplayUtil;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.dialog.AbstractSWTDialog;

public abstract class SYMCAbstractDialog extends AbstractSWTDialog {
	
	private int scrollBarWidthSize = 0;
	private int scrollBarHeightSize = SWT.DEFAULT;
	private Point parentShellSize;
	
	private Composite paramComposite;
	private Registry parentRegistry;
	
	private boolean okButtonVisible;
	protected Button okButton;
	private boolean applyButtonVisible;
	protected Button applyButton;
	protected Button cancelButton;
	
	protected boolean isApplyPressed = true;

	public SYMCAbstractDialog(Shell paramShell, int paramInt) {
		super(paramShell, paramInt);
		parentRegistry = Registry.getRegistry("com.ssangyong.common.common");
		okButtonVisible = true;
		applyButtonVisible = true;
	}

	public SYMCAbstractDialog(Shell paramShell) {
		super(paramShell);
		parentRegistry = Registry.getRegistry("com.ssangyong.common.common");
		okButtonVisible = true;
		applyButtonVisible = true;
	}

	/**
	 * paramComposite 하위에 ScrolledComposite 추가후 createDialogPanel() 메소드호출하여 하위 패널 추가 , 스크롤처리.
	 * 하위 클래스는 createDialogPanel()를 재정의하면 됨.
	 * @Copyright : S-PALM
	 * @author : 권오규
	 * @since  : 2013. 1. 10.
	 * @override
	 * @see com.teamcenter.rac.util.dialog.AbstractSWTDialog#createDialogWindow(org.eclipse.swt.widgets.Composite)
	 * @param paramComposite
	 */
	@Override
	protected void createDialogWindow(Composite paramComposite) {
		this.paramComposite = paramComposite;
		Shell dialogParentShell = getShell();
		dialogParentShell.setBackground(dialogParentShell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		dialogParentShell.setBackgroundMode(SWT.INHERIT_FORCE);
		FillLayout fillLayout = new FillLayout(SWT.NONE);

		paramComposite.setLayout(fillLayout);

		final ScrolledComposite scrolledComposite = new ScrolledComposite(paramComposite, SWT.NONE | SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.getVerticalBar().setIncrement(10);
		scrolledComposite.setLayout(fillLayout);
		if (parentShellSize != null) {
			scrolledComposite.setSize(parentShellSize);
			scrollBarWidthSize = parentShellSize.x;
		}
		final Composite createDialogPanel = createDialogPanel(scrolledComposite);
		scrolledComposite.setContent(createDialogPanel);
		scrolledComposite.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				int x;
				int y;
				
				if (scrollBarWidthSize != 0) {
					x = scrollBarWidthSize;
					y = scrollBarHeightSize;
				} else {
					x = createDialogPanel.getClientArea().x;
//					y = createDialogPanel.getClientArea().y;
					y = SWT.DEFAULT;
				}
				scrolledComposite.setMinSize(createDialogPanel.computeSize(x, y));
			}
		});
		
	}

	protected void setScrollHeightAtResize(int y) {
	    scrollBarHeightSize = y;
	}
	
	/**
	 * paramComposite 의 크기를 재정의 하기위한 메소드.
	 * @Copyright : S-PALM
	 * @author : 권오규
	 * @since  : 2013. 1. 10.
	 * @param width
	 * @param height
	 */
	protected void setParentDialogCompositeSize(Point size) {
		parentShellSize = size;
	}

	/**
	 * SWTDilaog 의 타이틀 및 이미지지정을 위한 메소드.
	 * @Copyright : S-PALM
	 * @author : 권오규
	 * @since  : 2013. 1. 10.
	 * @param title
	 * @param image
	 */
	protected void setDialogTextAndImage(String title, Image image) {
		Shell dialogParentShell = getShell();
		dialogParentShell.setText(title);
		dialogParentShell.setImage(image);
	}
	
	protected Control createButtonBar(Composite paramComposite) {
		
		Composite localMainComposite = new Composite(paramComposite, 0);
		GridLayout localMainGridLayout = new GridLayout();
		localMainGridLayout.numColumns = 1;
		localMainGridLayout.makeColumnsEqualWidth = false;
		localMainGridLayout.marginWidth = 5;
		localMainGridLayout.marginHeight = 0;
		localMainGridLayout.marginTop = 0;
		localMainGridLayout.marginBottom = 0;
		
		GridData localGridData = new GridData(GridData.FILL_HORIZONTAL);
		localMainComposite.setLayout(localMainGridLayout);
		localMainComposite.setLayoutData(localGridData);
		
		Label separator = new Label (localMainComposite, SWT.SEPARATOR | SWT.HORIZONTAL); 
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite localComposite = new Composite(paramComposite, 0);
		GridLayout localGridLayout = new GridLayout();
		localGridLayout.numColumns = 1;
		localGridLayout.makeColumnsEqualWidth = false;
		localGridLayout.marginWidth = 0;
		localGridLayout.marginHeight = 0;
		localGridLayout.marginTop = 5;
		localGridLayout.marginBottom = 5;
		localGridLayout.horizontalSpacing = convertHorizontalDLUsToPixels(4);
		localGridLayout.verticalSpacing = convertVerticalDLUsToPixels(4);
		
		localComposite.setLayout(localGridLayout);
		localGridData = new GridData(128);
		localComposite.setLayoutData(localGridData);
		
		createButtonsForButtonBar(localComposite);
		
		return localMainComposite;
	}
	
    protected Composite getParentDialogComposite() {
        return this.paramComposite;
    }
    
    @Override
    protected Control createContents(Composite parent) {
        if (this.parentShellSize != null) {
            parent.setSize(this.parentShellSize.x + 40, this.parentShellSize.y + 30);
            SYMDisplayUtil.centerToParent(parent.getParent().getShell(), (Shell)parent);
        }
        Composite composite = new Composite(parent, 0);
        GridLayout layout = new GridLayout();
        layout.makeColumnsEqualWidth = true;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(1808));
        applyDialogFont(composite);

        initializeDialogUnits(composite);
        this.dialogArea = createDialogArea(composite);
        this.buttonBar = createButtonBar(composite);
        afterCreateContents();
        return composite;
    }
    
    public void setOKButtonVisible(boolean visible) {
        okButtonVisible = visible;
    }

    public void setApplyButtonVisible(boolean visible) {
    	applyButtonVisible = visible;
    }

	/**
	 * DefaultButton에 이미지 추가 및 적용버튼 추가
	 * @Copyright : S-PALM
	 * @author : 권오규
	 * @since  : 2013. 1. 10.
	 * @override
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
	    if(okButtonVisible) {
    		okButton = createButton(parent, 0, IDialogConstants.OK_LABEL, true);
    		okButton.setImage(parentRegistry.getImage("OK_16.ICON"));
	    }
		if(applyButtonVisible) {
			applyButton = createButton(parent, 2, parentRegistry.getString("Apply.TEXT"), false);
			applyButton.setImage(parentRegistry.getImage("Apply_16.ICON"));
		}
		cancelButton = createButton(parent, 1, IDialogConstants.CANCEL_LABEL, false);
		cancelButton.setImage(parentRegistry.getImage("Cancel_16.ICON"));
		cancelButton.setFocus();
	}
	
    /**
     * UI Panel 생성 메소드
     * Implements Method
     * @Copyright : S-PALM
     * @author : 권오규
     * @since  : 2013. 1. 10.
     * @param parentScrolledComposite
     * @return
     */
    abstract protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite);

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == 0) {
			okPressed();
		} else if (1 == buttonId) {
			cancelPressed();
		} else if (2 == buttonId) {
			applyPressed();
		}
	}

	@Override
	protected void okPressed() {
        if (!validationCheck()) {
            return;
        }
        
        isApplyPressed = false;
        
        if(apply()) {
            super.okPressed();
        }
	}

	/**
	 * 적용 버튼을 클릭했을때 이벤트 처리
	 * 하위 클래스에서 상속받아 재정의
	 * @Copyright : S-PALM
	 * @author : 권오규
	 * @since  : 2013. 1. 10.
	 */
	protected void applyPressed() {
        if (!validationCheck()) {
            return;
        }
        
        
        isApplyPressed = true;
        
        apply();
	}
	
	protected void afterCreateContents() {
	}

    /**
     * Validation 체크 메소드
     * Implements Method
     * @Copyright : S-PALM
     * @author : 권오규
     * @since  : 2013. 1. 10.
     * @return
     */
    abstract protected boolean validationCheck();

	/**
	 * 확인, 적용 버튼 누를경우 처리.<br>
	 * 적용 완료되었으면 true를 반환하도록 한다.
	 * @Copyright : S-PALM
	 * @author : 권오규
	 * @since  : 2013. 1. 10.
	 */
	abstract protected boolean apply();

	/**
	 * UI Panel
	 * Implements Method
	 * @Copyright : S-PALM
	 * @author : 권오규
	 * @since  : 2013. 1. 10.
	 * @return
	 
	abstract protected Composite getUIPanel();*/
	
}
