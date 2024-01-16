package com.ssangyong.commands.partmaster.editparts;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.ssangyong.common.utils.progressbar.WaitProgressor;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.util.MessageBox;

public class EditMultiPartsManager
{
	private ArrayList<EditMultiPartComponent> partComponentList;
	private ArrayList<TCComponentItemRevision> revisionList;
	private String[] editProperties;
	private Table table;
	private Shell shell;
	public static int OK = 1;
	public static int APPLY = 2;

	public EditMultiPartsManager(ArrayList<TCComponentItemRevision> _revisionList, String[] _editProperties, Table _table)
	{
		revisionList = _revisionList;
		editProperties = _editProperties;
		table = _table;
		shell = table.getShell();
		partComponentList = new ArrayList<EditMultiPartComponent>();
	}

	public void loadData()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				WaitProgressor waitProgressor = new WaitProgressor(shell);
				waitProgressor.start();
				try
				{
					partComponentList = new ArrayList<EditMultiPartComponent>();
					shell.getDisplay().syncExec(new Runnable()
					{
						@Override
						public void run()
						{
							table.removeAll();
						}
					});
					int total = revisionList.size();
					int count = 1;
					for (TCComponentItemRevision revision : revisionList)
					{
						waitProgressor.setMessage("데이터 로딩 중... \n[" + count++ + "/" + total + "] " + revision.toString());//상위에 메시지 띄우기.
						EditMultiPartComponent editMultiPartComponent = new EditMultiPartComponent(revision, table);

						final TCProperty[] properties = revision.getTCProperties(editProperties);
						for (TCProperty property : properties)
						{
							if (property != null)
							{
								Object propertyObject = property.getPropertyData();
								if (property.getPropertyType() == TCProperty.PROP_double)
								{
									propertyObject = property.getUIFValue();
								} else if (property.getPropertyType() == TCProperty.PROP_untyped_reference || property.getPropertyType() == TCProperty.PROP_typed_reference)
								{
									propertyObject = property.getReferenceValue();
								}
								editMultiPartComponent.setTableItemData(property.getPropertyName(), propertyObject, true);
							}
						}
						partComponentList.add(editMultiPartComponent);
					}
					waitProgressor.end();
				} catch (Exception e)
				{
					e.printStackTrace();
					partComponentList.clear();
					shell.getDisplay().syncExec(new Runnable()
					{
						@Override
						public void run()
						{
							table.removeAll();
						}
					});
					waitProgressor.setMessage(e.toString());
					waitProgressor.end(false);
				}
			}
		}).start();
	}

	public void setData(String propertyName, Object propertyData) throws Exception
	{
		for (EditMultiPartComponent editMultiPartComponent : partComponentList)
		{
			editMultiPartComponent.setTableItemData(propertyName, propertyData);
		}
	}

	public void save(final int action)
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				WaitProgressor waitProgressor = new WaitProgressor(shell);
				waitProgressor.start();
				try
				{
					int total = partComponentList.size();
					int count = 1;
					for (EditMultiPartComponent editMultiPartComponent : partComponentList)
					{
						waitProgressor.setMessage("데이터 저장 중... \n[" + count++ + "/" + total + "] " + editMultiPartComponent.getRevision().toString());//상위에 메시지 띄우기.
						editMultiPartComponent.save();
					}
					waitProgressor.end();
					shell.getDisplay().syncExec(new Runnable()
					{
						@Override
						public void run()
						{
							MessageBox.post(shell, "저장이 완료되었습니다.", "완료", MessageBox.INFORMATION);
							if (action == OK)
							{
								shell.dispose();
							} else if (action == APPLY)
							{
								loadData();
							}
						}
					});
				} catch (Exception e)
				{
					e.printStackTrace();
					waitProgressor.setMessage(e.toString());
					waitProgressor.end(false);
				}
			}
		}).start();
	}
}
