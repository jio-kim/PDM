package com.ssangyong.commands.ec.ecostatus.utility;

import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.theme.ModernNatTableThemeConfiguration;

public class TableStylingThemeConfiguration extends ModernNatTableThemeConfiguration {
	{
		this.cHeaderHAlign = HorizontalAlignmentEnum.CENTER;
		this.cHeaderSelectionCellPainter = null;
	}
}