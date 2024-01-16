package com.ssangyong.viewer;

import com.teamcenter.rac.aif.kernel.IPreferenceService;
import com.teamcenter.rac.util.OSGIUtil;
import com.teamcenter.rac.viewer.Activator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

/**
 * 
 * CheckInOutComposite Class Custom
 * 
 * @author KBY
 * 
 */
public class SYMCCheckInOutComposite extends Composite {

	private List<String> COMMAND_IDS = new ArrayList<String>(Arrays.asList(new String[] { "com.teamcenter.rac.checkOut", "com.teamcenter.rac.checkIn", "com.teamcenter.rac.saveCheckOut", "com.teamcenter.rac.cancelCheckOut" }));
	private Map<String, ToolBarManager> tbMgrTable;
	private static final String SITE_PREF_IMPLICIT_CO = "TC_Enable_Implicit_CO";

	public SYMCCheckInOutComposite(Composite paramComposite) {
		super(paramComposite, 0);
	}

	/**
	 * 
	 * @param commandId
	 */
	public void addCommandId(String commandId) {
		if (commandId == null || commandId.isEmpty()) {
			return;
		}

		COMMAND_IDS.add(commandId);
	}

	/**
	 * 
	 */
	public void createToolbar() {
		createCheckinToolbar(getParent());
	}

	private void createCheckinToolbar(Composite paramComposite) {
		checkAndAddImplicitCOCommand();

		setLayoutData(new GridData(4, 16777216, true, false));
		RowLayout localRowLayout = new RowLayout(256);
		localRowLayout.wrap = true;
		localRowLayout.fill = true;
		setLayout(localRowLayout);

		this.tbMgrTable = new HashMap<String, ToolBarManager>();
		Iterator<String> localIterator = this.COMMAND_IDS.iterator();
		while (localIterator.hasNext()) {
			String str = localIterator.next();
			ToolBarManager localToolBarManager = new ToolBarManager();
			localToolBarManager.createControl(this);
			localToolBarManager.update(true);
			this.tbMgrTable.put(str, localToolBarManager);
		}
	}

	private void checkAndAddImplicitCOCommand() {
		IPreferenceService localIPreferenceService = (IPreferenceService) OSGIUtil.getService(Activator.getDefault(), IPreferenceService.class);
		if (!(localIPreferenceService.getLogicalValue(SITE_PREF_IMPLICIT_CO).booleanValue())) {
			return;
		}

		this.COMMAND_IDS.add(0, "com.teamcenter.rac.implicitCheckOut");
	}

	public void dispose() {
		super.dispose();
		if ((this.tbMgrTable == null) || (this.tbMgrTable.isEmpty())) {
			return;
		}

		Set<Entry<String, ToolBarManager>> localSet = this.tbMgrTable.entrySet();
		Iterator<Entry<String, ToolBarManager>> localIterator = localSet.iterator();
		while (localIterator.hasNext()) {
			Entry<String, ToolBarManager> localEntry = (Entry<String, ToolBarManager>) localIterator.next();
			((ToolBarManager) localEntry.getValue()).dispose();
		}
	}

	public void panelLoaded() {
		if ((this.tbMgrTable == null) || (this.tbMgrTable.isEmpty())) {
			return;
		}

		Set<Entry<String, ToolBarManager>> localSet = this.tbMgrTable.entrySet();
		Iterator<Entry<String, ToolBarManager>> localIterator = localSet.iterator();
		while (localIterator.hasNext()) {
			Entry<String, ToolBarManager> localEntry = (Entry<String, ToolBarManager>) localIterator.next();
			ToolBarManager localToolBarManager = (ToolBarManager) localEntry.getValue();
			String str = (String) localEntry.getKey();
			if (localToolBarManager == null) {
				continue;
			}

			for (IContributionItem localIContributionItem : localToolBarManager.getItems()) {
				localIContributionItem.dispose();
			}

			localToolBarManager.removeAll();
			IContributionItem localIContributionItem = getCommandContribution(str);
			localToolBarManager.add(localIContributionItem);
			localToolBarManager.update(true);
		}

		layout(true);
		getParent().layout(true);
	}

	private IContributionItem getCommandContribution(String paramString) {
		CommandContributionItemParameter localCommandContributionItemParameter = new CommandContributionItemParameter(PlatformUI.getWorkbench(), "", paramString, 8);
		localCommandContributionItemParameter.mode = CommandContributionItem.MODE_FORCE_TEXT;

		return new CommandContributionItem(localCommandContributionItemParameter);
	}

	public void postSetVisible(boolean paramBoolean) {
		Object localObject = getLayoutData();
		if (localObject instanceof GridData) {
			((GridData) localObject).exclude = (!(paramBoolean));
		}

		getParent().layout(false);
	}

}