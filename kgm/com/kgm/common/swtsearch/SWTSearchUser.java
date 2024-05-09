package com.kgm.common.swtsearch;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.kgm.Activator;
import com.kgm.commands.ec.search.SearchUserDialog;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

/**
 * 사용자 검색 Composite
 */
@SuppressWarnings("unused")
public class SWTSearchUser extends Composite {
	private Text txtDisplay;
	private Button bSearchUser;

	private String sUserID;
	private String sUserName;
	private String sUserDisplayName;
	private TCComponentUser userComponent;

	private Registry registry;
    private TCSession session;
	
	public SWTSearchUser(Composite parent) {
		this(parent, null);
	}
	
	public SWTSearchUser(Composite parent, TCComponentUser userComponent) {
		super(parent, SWT.NONE);
		setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginLeft = 0;
		gridLayout.marginRight = 0;
		gridLayout.marginTop = 0;
		gridLayout.marginBottom = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		
		setLayout(gridLayout);
		setLayoutData(new GridData(GridData.FILL_BOTH));

        this.registry = Registry.getRegistry(Activator.class);
        this.session = CustomUtil.getTCSession();
        
        GridData gdText = new GridData(GridData.FILL_HORIZONTAL);
		
        GridData gdButton = new GridData();
        gdButton.heightHint = 22;
        gdButton.widthHint = 22;
        
		txtDisplay = new Text(this, SWT.BORDER);
		txtDisplay.setEditable(false);
		txtDisplay.setLayoutData(gdText);
		
		bSearchUser = new Button(this, SWT.PUSH);
		bSearchUser.setLayoutData(gdButton);
		bSearchUser.setImage(com.teamcenter.rac.common.Activator.getDefault().getImage("icons/search_16.png"));
		
		if (userComponent != null) {
			try {
				setUser(userComponent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		addListener();
	}
	
	private void addListener() {
		bSearchUser.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					SearchUserDialog dialog = new SearchUserDialog(getShell());
					int iResult = dialog.open();
					if (iResult == 0) {
						TCComponentGroupMember userGroupMember = dialog.getSelectedMember();
						setUser(userGroupMember.getUser());
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		});
		
		txtDisplay.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL || e.keyCode == SWT.BS) {
					setUserID("");
					setUserName("");
					setUserDisplayName("");
					txtDisplay.setText("");
					setUser(null);
				}
			}
		});
	}
	
	public void setButtonEnabled(boolean isEnabled) {
		bSearchUser.setEnabled(isEnabled);
	}
	
	public void setTextFieldEnabled(boolean isEnabled) {
		txtDisplay.setEnabled(isEnabled);
	}
	
	/**
	 * 
	 * @param sUserID
	 */
	public void setUserID(String sUserID) {
		this.sUserID = sUserID;
	}
	
	/**
	 * 
	 * @param sUserName
	 */
	public void setUserName(String sUserName) {
		this.sUserName = sUserName;
	}
	
	/**
	 * 
	 * @param sUserDisplayName
	 */
	public void setUserDisplayName(String sUserDisplayName) {
		this.sUserDisplayName = sUserDisplayName;
	}
	
	/**
	 * 사용자 설정
	 * @param userComponent
	 */
	public void setUser(TCComponentUser userComponent) {
		if (userComponent == null) {
			this.userComponent = null;
			return;
		}
		
		try {
			this.userComponent = userComponent;
			sUserID = userComponent.getUserId();
			sUserName = userComponent.getProperty("user_name");
			sUserDisplayName = sUserName + " (" + sUserID + ")";
			txtDisplay.setText(sUserDisplayName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public String getUserID() {
		return sUserID;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getUsetName() {
		return sUserName;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getUserDisplayName() {
		return sUserDisplayName;
	}
	
	/**
	 * 
	 * @return
	 */
	public TCComponentUser getUser() {
		return userComponent;
	}
	
	public Text getTxtDisplay()
	{
		return txtDisplay;
	}
}