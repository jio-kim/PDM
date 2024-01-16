package com.ssangyong.commands.partmaster.editparts;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemRevisionType;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;

public class EditMultiPartComponent
{
	private TCComponentItemRevision revision;
	private TCComponentItemRevisionType revisionType;
	private Table table;
	private TableItem tableItem;

	public EditMultiPartComponent(TCComponentItemRevision _revision, Table _table)
	{
		revision = _revision;
		revisionType = (TCComponentItemRevisionType) revision.getTypeComponent();
		table = _table;
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				tableItem = new TableItem(table, SWT.NONE);
			}
		});
	}

	public void setTableItemData(final String propertyName, final Object propertyData) throws Exception
	{
		setTableItemData(propertyName, propertyData, false);
	}

	public void setTableItemData(final String propertyName, final Object propertyData, final boolean isLoading) throws Exception
	{
		if (!isLoading)
		{
			//속성별 체크 로직.. Exception으로 에러 처리.
			//shown on part에서 체크 로직
			if (propertyName.equals("s7_SHOWN_PART_NO"))
			{
				String drwStat = revision.getProperty("s7_DRW_STAT");
				//drw stat가 H인데 shown on part가 없으면 안됨. 오류 처리.
				if (drwStat.equals("H") && propertyData == null)
				{
					new Exception("'Drw Status' 값이 'H'인 경우 'Shown On No.'를 입력하셔야 합니다.\nRevision : " + revision.toDisplayString());
				}
			}
			//system code의 체크 로직...
			if (propertyName.equals("s7_BUDGET_CODE"))
			{
				String mainName = revision.getProperty("s7_MAIN_NAME");
				String subName = revision.getProperty("s7_SUB_NAME");
				if (!mainName.equals(""))
				{
					// Main Name이 BIP COMPL, BIW COMPL이 아니면서 System Code를 000으로 선택한 경우에 대한 Validation
					if (!((mainName.equals("BIP COMPL") || mainName.equals("BIW COMPL")) && (subName == null || subName.equals("") || subName.length() == 0)))
					{
						if (propertyData.equals("000"))
						{
							// System Code '000'은 "BIP COMPL", "BIW COMPL"에만 선택할 수 있습니다.
							new Exception("System Code '000'은 'BIP COMPL', 'BIW COMPL'에만 선택할 수 있습니다.\nRevision : " + revision.toDisplayString());
						}
					} else
					{
						if (subName == null || subName.equals("") || subName.length() == 0)
						{
							if (!propertyData.equals("000"))
							{
								// "BIP COMPL", "BIW COMPL"은 System Code '000'만 선택 가능
								new Exception("System Code '000'만 선택해주십시요.\nRevision : " + revision.toDisplayString());
							}
						}
					}
				}
			}
		}
		String tempString = "";
		if (propertyData != null)
		{
			TCPropertyDescriptor tcPropertyDescriptor = revisionType.getPropertyDescriptor(propertyName);
			int type = tcPropertyDescriptor.getType();
			switch (type)
			{
				case TCProperty.PROP_string:
					tempString = propertyData.toString();
					break;
				case TCProperty.PROP_double:
					String tmpDouble = propertyData.equals("")?"0":propertyData.toString();
					tempString = new BigDecimal(tmpDouble+"").toString();
					break;
				case TCProperty.PROP_typed_reference:
				case TCProperty.PROP_untyped_reference:
					if (propertyData != null)
					{
						tempString = ((TCComponent) propertyData).getProperty("item_id");
					}
					break;
			}
		}
		final String propertyString = tempString;
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				if (!table.isSelected(table.indexOf(tableItem)) && !isLoading)
				{
					return;
				}
				TableColumn cTableColumn = null;
				for (TableColumn tableColumn : table.getColumns())
				{
					if (tableColumn.getData().equals(propertyName))
					{
						cTableColumn = tableColumn;
						break;
					}
				}
				if (cTableColumn == null)
				{
					return;
				}
				tableItem.setText(table.indexOf(cTableColumn), propertyString);
				tableItem.setData(propertyName, propertyData);
				if (isLoading)
				{
					tableItem.setData("OLD_" + propertyName, propertyString);
				}
				int c = table.indexOf(cTableColumn);
				if (!propertyString.equals(tableItem.getData("OLD_" + propertyName)) && c != 1 && c != 2 && c != 3)
				{
					tableItem.setBackground(c, SWTResourceManager.getColor(200, 200, 255));
				} else
				{
					if (c != 1 && c != 2 && c != 3)
					{
						tableItem.setBackground(c, SWTResourceManager.getColor(SWT.COLOR_WHITE));
					} else
					{
						tableItem.setBackground(c, SWTResourceManager.getColor(240, 240, 240));
					}
				}
			}
		});
	}

	public void save() throws Exception
	{
		final Hashtable<String, Object> propertyDataHash = new Hashtable<String, Object>();
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (TableColumn tableColumn : table.getColumns())
				{
					int c = table.indexOf(tableColumn);
					if (c == 1 || c == 2 || c == 3)
					{
						continue;
					}
					String propertyName = (String) tableColumn.getData();
					//변경 유무 확인.
					String uiTextData = tableItem.getText(table.indexOf(tableColumn));
					if(uiTextData.equals(tableItem.getData("OLD_" + propertyName)))
					{
						continue;
					}
					Object propertyData = tableItem.getData(propertyName);
					ArrayList<Object> tempList = new ArrayList<Object>();
					tempList.add(propertyData);
					propertyDataHash.put(propertyName, tempList);
				}
			}
		});
		Enumeration<String> enums = propertyDataHash.keys();
		while(enums.hasMoreElements())
		{
			String propertyName = (String) enums.nextElement();
			ArrayList<Object> tempList = (ArrayList<Object>) propertyDataHash.get(propertyName);
			Object propertyData = tempList.get(0);
			TCPropertyDescriptor tcPropertyDescriptor = revisionType.getPropertyDescriptor(propertyName);
			int type = tcPropertyDescriptor.getType();
			switch (type)
			{
				case TCProperty.PROP_string:
					String propertyValue = propertyData == null?"":propertyData.toString();
					revision.setStringProperty(propertyName, propertyValue);
					break;
				case TCProperty.PROP_double:
					String tmpDoubleString = (propertyData==null || propertyData.equals(""))?"0":propertyData.toString();
					revision.setDoubleProperty(propertyName, new BigDecimal(tmpDoubleString+"").doubleValue());
					break;
				case TCProperty.PROP_typed_reference:
				case TCProperty.PROP_untyped_reference:
					revision.setReferenceProperty(propertyName, (TCComponent) propertyData);
					break;
			}

		}
	}

	/**
	 * @return the revision
	 */
	public TCComponentItemRevision getRevision()
	{
		return revision;
	}
}
