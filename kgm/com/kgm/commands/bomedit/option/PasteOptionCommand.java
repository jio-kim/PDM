package com.kgm.commands.bomedit.option;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.kgm.common.utils.variant.OptionManager;
import com.kgm.common.utils.variant.VariantOption;
import com.kgm.common.utils.variant.VariantValue;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.SYMCBOMLine;
import com.teamcenter.rac.kernel.SYMCBOMWindow;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.pse.variants.modularvariants.ConditionElement;
import com.teamcenter.rac.pse.variants.modularvariants.ConstraintsModel;
import com.teamcenter.rac.util.MessageBox;

public class PasteOptionCommand extends AbstractAIFCommand {
	private boolean isValidate1LV = true;
	
	protected void executeCommand() throws Exception {
		TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		Markpoint mp = null;
		
		try {
			mp = new Markpoint(session);
			
			TCComponent[] compTargetObjs = this.getTargets();
			boolean isValidated = true;
			if (compTargetObjs != null && compTargetObjs[0] instanceof SYMCBOMLine) {
				SYMCBOMLine bomLineTemp = (SYMCBOMLine)compTargetObjs[0];
				SYMCBOMWindow bomWindow = (SYMCBOMWindow) bomLineTemp.window();
				
				// BOM Window Top Line이 Function Type일 경우에만 수행
				if (!bomWindow.getTopBOMLine().getItem().getType().equals("S7_Function")) {
					MessageBox.post("Check the top bom line.", "You should place Function on Top.", "Option Paste Error", MessageBox.ERROR);
					isValidated = false;
				}
			}
			
			// 현재 Clipboard의 Text가 현재 BOM Window에서 유효한 Option Set인지 확인
			if (isValidated) {
				HashMap<String, VariantOption> optionMap = new HashMap<String, VariantOption>();
				Vector<String[]> userDefineErrorList = new Vector<String[]>();
				Vector<String[]> moduleConstratintsList = new Vector<String[]>();
				
				SYMCBOMLine bomLineTemp = (SYMCBOMLine)compTargetObjs[0];
				SYMCBOMWindow bomWindow = (SYMCBOMWindow) bomLineTemp.window();
				
				// 1. Top Line(Function)에 정의되어있는 Option Set을 가져온다.
				OptionManager mgrOption = new OptionManager(bomWindow.getTopBOMLine(), false);
				ArrayList<VariantOption> optionSet = mgrOption.getOptionSet(bomWindow.getTopBOMLine(), optionMap, userDefineErrorList, moduleConstratintsList, false, false);
				
				// 2. 현재 Clipboard에 있는 Text를 분리
				String sClipboardString = getClipboardContents();
				ConditionElement[] elements = ConstraintsModel.parseACondition(sClipboardString);
				
				if (elements == null || elements.length == 0) {
					MessageBox.post("Check the clipboard text.", "Invalid Option Value is included.", "Option Paste Error", MessageBox.ERROR);
					isValidated = false;
				}
				
				// 3. 분리된 각 Option Category 및 Value가 현재 Top Line에 정의된 Option Set 내에 존재하는지, 사용가능한 Option인지 확인
				if (isValidated) {
					for (int inx = 0; inx < elements.length; inx++) {
						boolean isValidatedOptionSet = false;
						String sOptionValueClipboard = elements[inx].value;
						
						for (int jnx = 0; jnx < optionSet.size(); jnx++) {
							HashMap<String, VariantValue> valueMap = optionSet.get(jnx).getValueMap();
							
							Iterator<String> iterator = valueMap.keySet().iterator();
							while (iterator.hasNext()) {
								String sKey = (String) iterator.next();
								VariantValue value = valueMap.get(sKey);
								if (value.getValueName().equals(sOptionValueClipboard) && value.getValueStatus() == VariantValue.VALUE_USE) {
									isValidatedOptionSet = true;
									break;
								}
							}
							
							if (isValidatedOptionSet) {
								break;
							}
						}
						
						if (!isValidatedOptionSet) {
							MessageBox.post("Check the clipboard text.", "Invalid Option Value is included.", "Option Paste Error", MessageBox.ERROR);
							isValidated = false;
							break;
						}
					}
				}
				
				if (isValidated) {
					ExecutorService executor = Executors.newFixedThreadPool(100);
					
					for (int inx = 0; inx < compTargetObjs.length; inx++) {
						if (compTargetObjs[inx] instanceof SYMCBOMLine) {
							SYMCBOMLine bomLine = (SYMCBOMLine) compTargetObjs[inx];
							
							PasteOptionValidationThread thread = new PasteOptionValidationThread(bomLine);
							executor.execute(thread);
						}
					}
					
					executor.shutdown();
					while (!executor.isTerminated()) {
					}
					
					isValidated = getIsValidate1LV();
				}
				
				// 선택한 BOM Line에 Clipboard에 복사된 Option을 입력 (Save는 하지 않음)
				if (isValidated) {
					SYMCBOMLine[] bomLines = new SYMCBOMLine[compTargetObjs.length];
					for (int inx = 0; inx < compTargetObjs.length; inx++) {
						bomLines[inx] = (SYMCBOMLine) compTargetObjs[inx];
					}
					
					pasteOptionFromClipboard(bomLines);
				}
			}
			
			mp.forget();
		} catch (Exception e) {
			mp.rollBack();
		}
	}

	public TCComponent[] getTargets() {
		AbstractAIFUIApplication abstractaifuiapplication = AIFDesktop.getActiveDesktop().getCurrentApplication();

		AIFComponentContext aaifcomponentcontext[] = abstractaifuiapplication.getTargetContexts();

		if (aaifcomponentcontext != null) {
			TCComponent[] components = new TCComponent[aaifcomponentcontext.length];

			for (int inx = 0; inx < aaifcomponentcontext.length; inx++) {
				components[inx] = (TCComponent) aaifcomponentcontext[inx].getComponent();
			}

			return components;
		}

		return null;
	}

	public String getClipboardContents() {
		String result = "";
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		// odd: the Object param of getContents is not currently used
		Transferable contents = clipboard.getContents(null);
		boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
		if (hasTransferableText) {
			try {
				result = (String) contents.getTransferData(DataFlavor.stringFlavor);
			} catch (Exception ex) {
				System.out.println(ex);
				ex.printStackTrace();
			}
		}
		return result;
	}
	
	public void pasteOptionFromClipboard(SYMCBOMLine[] bomLines) throws Exception {
		String sClipboardString = getClipboardContents();
		
		ExecutorService executor = Executors.newFixedThreadPool(100);
		for (int inx = 0; inx < bomLines.length; inx++) {
			PasteOptionThread thread = new PasteOptionThread(bomLines[inx], sClipboardString);
			executor.execute(thread);
		}
		
		executor.shutdown();
		while (!executor.isTerminated()) {
		}
	}
	
	class PasteOptionThread extends Thread {
		private SYMCBOMLine bomLine;
		private String sClipboardString;
		public PasteOptionThread(SYMCBOMLine bomLine, String sClipboardString) {
			this.bomLine = bomLine;
			this.sClipboardString = sClipboardString;
		}
		
		public void run() {
			try {
				bomLine.setMVLCondition(sClipboardString);
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean getIsValidate1LV() {
		return this.isValidate1LV;
	}
	
	private void setIsValidate1LV(boolean isValidate1LV) {
		this.isValidate1LV = isValidate1LV;
	}
	
	class PasteOptionValidationThread extends Thread {
		private SYMCBOMLine bomLine;
		public PasteOptionValidationThread(SYMCBOMLine bomLine) {
			this.bomLine = bomLine;
		}
		
		public void run() {
			try {
				SYMCBOMLine parentBOMLine = (SYMCBOMLine) bomLine.parent();
				
				// FMP 하위 1LV Part만 Option을 가져야 하므로 선택한 BOM Line이 모두 FMP 하위 1LV인지 확인
				if (getIsValidate1LV() && !parentBOMLine.getItem().getType().equals("S7_FunctionMast")) {
					MessageBox.post("Check Selected Part.", "Invalid BOM Line is selected.", "Option Paste Error", MessageBox.ERROR);
					setIsValidate1LV(false);
					return;
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
	}
}
