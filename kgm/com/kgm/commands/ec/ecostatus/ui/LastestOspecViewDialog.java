package com.kgm.commands.ec.ecostatus.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Panel;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.border.EmptyBorder;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wb.swt.SWTResourceManager;

import com.kgm.commands.ec.SYMCECConstant;
import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.commands.ec.ecostatus.model.EcoStatusData;
import com.kgm.commands.ospec.op.OSpec;
import com.kgm.commands.ospec.op.OpUtil;
import com.kgm.commands.ospec.panel.OSpecTable;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.remote.DataSet;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.DatasetService;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.IPropertyName;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;

/**
 *최신 O/Spec 조회 Dialog
 * @author baek
 *
 */
public class LastestOspecViewDialog extends Dialog {

	private Panel embbedTopPanel;
	private Container contentPane;
	private EcoStatusData stdInformData = null;
	private TCSession tcSession = null;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public LastestOspecViewDialog(EcoStatusData stdInformData, Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
		this.stdInformData = stdInformData;
		tcSession = CustomUtil.getTCSession();
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("최신 O/Spec 조회");
	}


	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		Composite container = (Composite) super.createDialogArea(parent);

		container.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		container.setBackgroundMode(SWT.INHERIT_FORCE);

		GridLayout gl_container = new GridLayout(1, false);
		gl_container.marginWidth = 0;
		gl_container.marginHeight = 0;
		gl_container.verticalSpacing = 0;
		container.setLayout(gl_container);

		FormToolkit toolkit = new FormToolkit(parent.getDisplay());

		Section sectionbBasic = toolkit.createSection(container, Section.TITLE_BAR);
		GridData gd_sectionbBasic = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_sectionbBasic.minimumWidth = 0;
		sectionbBasic.setLayoutData(gd_sectionbBasic);

		Composite compositeCenter = toolkit.createComposite(sectionbBasic, SWT.WRAP | SWT.BORDER | SWT.EMBEDDED);
		compositeCenter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		sectionbBasic.setClient(compositeCenter);
		compositeCenter.setLayout(new FillLayout(SWT.HORIZONTAL));

		Frame frameEmbedded = SWT_AWT.new_Frame(compositeCenter);
		embbedTopPanel = new Panel(new BorderLayout()) {
			private static final long serialVersionUID = 1L;

			public void update(java.awt.Graphics g) {
				/* Do not erase the background */
				paint(g);
			}
		};
		frameEmbedded.add(embbedTopPanel);
		JRootPane rootPane = new JRootPane();
		rootPane.getContentPane().setBackground(Color.WHITE);
		embbedTopPanel.add(rootPane);
		contentPane = rootPane.getContentPane();

		LoadLatestOspecViewOperation op = new LoadLatestOspecViewOperation();
		tcSession.queueOperation(op);

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		createButton(parent, IDialogConstants.OK_ID, "Close", true);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(1200, 700);
	}

	/**
	 * 최신 Ospec 을 조회한다.
	 */
	public class LoadLatestOspecViewOperation extends AbstractAIFOperation {
		private WaitProgressBar waitProgress;

		@Override
		public void executeOperation() throws Exception {
			try {
				waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
				waitProgress.start();
				waitProgress.setWindowSize(300, 200);
				waitProgress.setStatus("Loading...");
				ArrayList<HashMap<String, Object>> ospecRevList = getOspecRevList("OSI-".concat(stdInformData.getProjectId()));

				HashMap<String, Object> ospecRevValue = ospecRevList.get(ospecRevList.size() - 1);
				String revPuid = (String) ospecRevValue.get("REV_PUID");

				TCComponent ospecRev = tcSession.stringToComponent(revPuid);
				OSpec ospec = getOSpec((TCComponentItemRevision) ospecRev);

				OSpecTable ospecTb = new OSpecTable(ospec, null);

				JPanel contentPanel = ospecTb.getOspecTable();
				contentPane.setLayout(new BorderLayout());
				contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
				contentPane.add(contentPanel, BorderLayout.CENTER);

				embbedTopPanel.validate();
				embbedTopPanel.revalidate();
				embbedTopPanel.repaint();
				waitProgress.setStatus("Complete");
				waitProgress.close();
			} catch (Exception ex) {
				if (waitProgress != null) {
					waitProgress.setStatus("＠ Error Message : ");
					waitProgress.setStatus(ex.toString());
					waitProgress.close("Error", false);
				}
				setAbortRequested(true);
				ex.printStackTrace();
			}

		}

		/**
		 * OSPEC Revision 정보 리스트
		 * 
		 * @param ds
		 * @return
		 * @throws Exception
		 */
		public ArrayList<HashMap<String, Object>> getOspecRevList(String ospecId) throws Exception {
			ArrayList<HashMap<String, Object>> resultList = null;
			DataSet ds = new DataSet();
			ds.put("OSPEC_ID", ospecId);
			CustomECODao dao = new CustomECODao();
			resultList = dao.getOspecRevList(ds);
			return resultList;
		}

		public OSpec getOSpec(TCComponentItemRevision ospecRev) throws Exception {
			String ospecStr = ospecRev.getProperty(IPropertyName.ITEM_ID) + "-" + ospecRev.getProperty(IPropertyName.ITEM_REVISION_ID);
			OSpec ospec = null;
			AIFComponentContext[] context = ospecRev.getChildren(SYMCECConstant.ITEM_DATASET_REL);
			for (int i = 0; context != null && i < context.length; i++) {
				TCComponentDataset ds = (TCComponentDataset) context[i].getComponent();
				if (ospecStr.equals(ds.getProperty("object_name"))) {
					File[] files = DatasetService.getFiles(ds);
					ospec = OpUtil.getOSpec(files[0]);
					break;
				}
			}
			return ospec;
		}

	}

}
