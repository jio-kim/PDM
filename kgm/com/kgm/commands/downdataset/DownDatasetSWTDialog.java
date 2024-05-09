package com.kgm.commands.downdataset;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.SYMCClass;
import com.kgm.common.attachfile.AttachFilePanel;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.VerticalLayout;
import com.teamcenter.rac.util.dialog.AbstractSWTDialog;

@SuppressWarnings({"unused", "rawtypes", "unchecked"})
public class DownDatasetSWTDialog extends AbstractSWTDialog {

	private InterfaceAIFComponent[] comps;
    private List dataSetList;
	private HashMap dataSetHash = new HashMap();
	private Registry registry = Registry.getRegistry(this);
	private TCSession session = CustomUtil.getTCSession();

	private AttachFilePanel attachFilePanel;
	private Display display;

	/**
	 * 생성자.
	 * 
	 * @copyright : S-PALM
	 * @author : 권상기
	 * @since : 2013. 1. 7.
	 * @param arg0
	 */
	public DownDatasetSWTDialog(Shell parent, InterfaceAIFComponent[] comps) {
		super(parent);
		this.comps = comps;
	}

	@Override
	protected void createDialogWindow(Composite paramComposite) {
		getShell().setText("Download DataSet");
		paramComposite.setLayout(new FillLayout());

		Composite composite = new Composite(paramComposite, SWT.EMBEDDED | SWT.NONE);
		RowLayout layout = new RowLayout();
		layout.marginHeight = 450;
		layout.marginWidth = 200;

		composite.setLayout(layout);

		Frame frame = SWT_AWT.new_Frame(composite);
		frame.setLayout(new VerticalLayout());

		attachFilePanel = new AttachFilePanel();
		attachFilePanel.setRelationType(SYMCClass.SPECIFICATION_REL);
		attachFilePanel.setTitledBorder("Download File");
		attachFilePanel.addChoosableFileFilter(SYMCClass.DATASET_FILTER_DOC(), registry
				.getString("Doc.TEXT"));
		attachFilePanel.setQuerySaveAs(CustomUtil.getTextServerString(session, "k_find_dataset_name"), null,
				AttachFilePanel.DATASET_TYPE_ARRAY, null, null, null);
		attachFilePanel.setVisibleModifybutton(false);
		attachFilePanel.setPreferredSize(new Dimension(450, 200));
		attachFilePanel.setVisibleAddFileButton(false);

		frame.add("unbound.bind", attachFilePanel);

		/** 선택 대상 List Add */
		listAdd();
	}

	/**
	 * Download 받을 선택한 데이터셋 들을 List에 등록.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2013. 1. 7.
	 */
	private void listAdd() {
		try {
			int compsSize = comps.length;
			ArrayList list = new ArrayList();
			TCComponent comp = null;
			for (int i = 0; i < compsSize; i++) {
				comp = (TCComponent) comps[i];
				if (comp instanceof TCComponentDataset) {
					list.add((TCComponentDataset) comp);
					dataSetHash.put(comp.toDisplayString(), comp);
				}
			}

			int listSize = list.size();
			TCComponent[] comps = new TCComponent[listSize];
			for (int j = 0; j < listSize; j++) {
				comps[j] = (TCComponent) list.get(j);
			}
			attachFilePanel.addAttachList(comps);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void okPressed() {
		System.out.println("ok btn click");
	}
}
