package com.symc.plm.rac.prebom.dcs.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * [20160512][ymjang] 윈도우 탐색기의 폴더옵션에서 확장자 숨기기 처리가 되어 있는 경우, 파일명을 변경하면 확장자 인식 못함 현상 개선. --> 확장자 필더 적용 기능 추가
 *
 */
public class DCSUIUtil {

	public DCSUIUtil() {

	}

	public static void openShell(Shell parentShell, Shell childShell) {
		childShell.open();
		childShell.layout();
		Display display = parentShell.getDisplay();
		while (!childShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public static String openFileDialog(Shell shell, String fileName) {
		FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
		fileDialog.setFileName(fileName);
		fileDialog.setOverwrite(true);

		return fileDialog.open();
	}

	/**
	 * [20160512][ymjang] 윈도우 탐색기의 폴더옵션에서 확장자 숨기기 처리가 되어 있는 경우, 
	 * 파일명을 변경하면 확장자 인식 못함 현상 개선. --> 확장자 필더 적용 기능 추가
	 * @param shell
	 * @param fileName
	 * @param filterName
	 * @return
	 */
	public static String openFileDialogWithFilter(Shell shell, String fileName, String filterName) {
		FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
		fileDialog.setFileName(fileName);
		fileDialog.setFilterNames(new String [] {filterName});
		fileDialog.setFilterExtensions(new String [] {filterName});
		fileDialog.setOverwrite(true);

		return fileDialog.open();
	}
	
	public static void centerToParent(Shell parentShell, Shell childShell) {
		Rectangle parentRectangle = parentShell.getBounds();
		Rectangle childRectangle = childShell.getBounds();
		int x = parentShell.getLocation().x + (parentRectangle.width - childRectangle.width) / 2;
		int y = parentShell.getLocation().y + (parentRectangle.height - childRectangle.height) / 2;
		childShell.setLocation(x, y);
	}

}
