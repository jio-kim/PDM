package com.ssangyong.common;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class CloneTableItem
{
	public static void clone(TableItem fromItem, TableItem toItem)
	{
		toItem.setChecked(fromItem.getChecked());
		toItem.setGrayed(fromItem.getGrayed());
		//120511 이영훈 수정 - 개발구매 진행 중 정렬 시 Data가 사라지는 문제
		toItem.setData(fromItem.getData());
		toItem.setData("UID", fromItem.getData("UID"));
		toItem.setData("ID", fromItem.getData("ID"));
		toItem.setData("BODY", fromItem.getData("BODY"));
		toItem.setData("DataSet", fromItem.getData("DataSet"));
		toItem.setData("TCProject", fromItem.getData("TCProject"));
		toItem.setData("STCTotalSheet", fromItem.getData("STCTotalSheet"));
		toItem.setData("objrev", fromItem.getData("objrev"));
		toItem.setData("distIntUid", fromItem.getData("distIntUid"));
		toItem.setData("distRecvUid", fromItem.getData("distRecvUid"));
		toItem.setData("obj", fromItem.getData("obj"));
		toItem.setData("problem", fromItem.getData("problem"));
		toItem.setData("item", fromItem.getData("item"));
		toItem.setData("mpn", fromItem.getData("mpn"));
		//120511 이영훈 수정 - 끝	

		toItem.setFont(fromItem.getFont());
		toItem.setForeground(fromItem.getForeground());
		toItem.setBackground(fromItem.getBackground());

		Table table = fromItem.getParent();
		for (int i = 0; i < table.getColumnCount(); i++)
		{
			toItem.setText(i, fromItem.getText(i));
			toItem.setImage(i, fromItem.getImage(i));
			toItem.setFont(i, fromItem.getFont(i));
			toItem.setForeground(i, fromItem.getForeground(i));
			toItem.setBackground(i, fromItem.getBackground(i));
		}
	}
}
