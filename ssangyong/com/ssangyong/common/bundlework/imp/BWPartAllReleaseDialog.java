package com.ssangyong.common.bundlework.imp;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import com.ssangyong.common.utils.DateUtil;
import com.ssangyong.common.utils.SYMTcUtil;
import com.ssangyong.common.utils.TxtReportFactory;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentEffectivity;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentReleaseStatus;
import com.teamcenter.rac.kernel.tcservices.TcEffectivityService;
import com.teamcenter.rac.util.MessageBox;

public class BWPartAllReleaseDialog extends BWPartImpDialog
{

	public BWPartAllReleaseDialog(Shell parent, int style)
	{
		super(parent, style, BWPartAllReleaseDialog.class);
	}

	@Override
	public void dialogOpen()
	{
		super.dialogOpen();

		this.bwOption.setBOMLineModifiable(false);
		this.bwOption.setBOMRearrange(false);
		this.bwOption.setBOMAvailable(false);
		// Dataset 包访 可记
		this.bwOption.setDSAvailable(false);
		this.bwOption.setDSChangable(false);
		// Item 包访 可记
		this.bwOption.setItemModifiable(false);
		// Revision 包访 可记
		this.bwOption.setRevCreatable(false);
		this.bwOption.setRevModifiable(false);
	}

	@Override
	public void validatePost() throws Exception
	{

	}

	@Override
	public void execute() throws Exception
	{
		// 角青 傈贸府
		executePre();

		if (this.nWraningCount > 0)
		{
			org.eclipse.swt.widgets.MessageBox box1 = new org.eclipse.swt.widgets.MessageBox(shell, SWT.OK | SWT.CANCEL | SWT.ICON_INFORMATION);
			box1.setMessage(this.nWraningCount + this.getTextBundle("WarningIgnore", "MSG", dlgClass));

			if (box1.open() != SWT.OK)
			{
				return;
			}
		}

		// 角青 滚瓢 Disable
		super.executeButton.setEnabled(false);
		// Excel 八祸 滚瓢 Disable
		super.searchButton.setEnabled(false);

		// Top TreeItem Array
		TreeItem[] szTopItems = super.tree.getItems();

		// TreeItem捞 粮犁窍瘤 臼绰 版快
		if (szTopItems == null || szTopItems.length == 0)
		{
			MessageBox.post(super.shell, super.getTextBundle("UploadInvalid", "MSG", dlgClass), "Notification", 2);
			return;
		}

		ExecutionJob job = new ExecutionJob(shell.getText(), szTopItems);
		job.schedule();

	}

	public void releaseData() throws Exception
	{

		ArrayList<TCComponentItemRevision> revSet = new ArrayList<TCComponentItemRevision>();
		ArrayList<ManualTreeItem> treeSet = new ArrayList<ManualTreeItem>();
		for (int i = 0; i < itemList.size(); i++)
		{
			ManualTreeItem treeItem = itemList.get(i);
			this.syncSetItemSelection(treeItem);

			String strRevID = treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, ITEM_ATTR_REVISIONID);

			// Top TreeItem狼 ItemRevision
			//TCComponentItemRevision targetRevision = this.tcItemRevSet.get(treeItem.getItemID() + "/" + strRevID);
			TCComponentItemRevision targetRevision= SYMTcUtil.findItemRevision(session, treeItem.getItemID(), strRevID);
		
			if (targetRevision == null)
			{
				this.syncItemState(treeItem, STATUS_ERROR, "This Revision is Null");
			}
			else if (SYMTcUtil.isInProcess(targetRevision) > 0)
			{
				this.syncItemState(treeItem, STATUS_ERROR, "This Revision is In Progress(WorkFlow)");
			} 
			else if (SYMTcUtil.isCheckedOut(targetRevision))
			{
				this.syncItemState(treeItem, STATUS_ERROR, "This Revision is CheckOut");
			} 
			else if (!SYMTcUtil.isWritable(targetRevision))
			{
				this.syncItemState(treeItem, STATUS_ERROR, "No Write Access Auth");
			} 
			else
			{
				revSet.add(targetRevision);
				treeSet.add(treeItem); 
			}

			if ((i + 1) == itemList.size() || revSet.size() == 100)
			{
				

				TCComponentItemRevision[] revisions = revSet.toArray(new TCComponentItemRevision[revSet.size()]);
				try
				{
					SYMTcUtil.selfRelease(revisions, "PSR");

					TCComponentReleaseStatus status = (TCComponentReleaseStatus) revisions[0].getRelatedComponent("release_status_list");
					if (status != null)
					{
						Date releaseDate = revisions[0].getDateProperty("date_released");

						Date adate[] = new Date[1];
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						String stringReleasDate = sdf.format(releaseDate);
						adate[0] = sdf.parse(stringReleasDate);

						TcEffectivityService.createReleaseStatusEffectivity(targetRevision.getSession(), (TCComponent) status, "", null, null, "", adate,
								TCComponentEffectivity.OpenEndedStatus.UP.getPropertyValue(), false);
					}
					
					
					this.syncItemState(treeItem, STATUS_COMPLETED, "Completed");
					
					
				}
				catch (Exception e)
				{
					for (int j = 0; j < treeSet.size(); j++)
					{
						this.syncItemState(treeSet.get(j), STATUS_ERROR, e.getMessage());
					}
					
					e.printStackTrace();
				}
				finally
				{
					
					System.out.println("revSet size:"+revSet.size());
					System.out.println("treeSet size:"+treeSet.size());
					
					revSet.clear();
					treeSet.clear();
				}
				
			}

		}

	}

	public class ExecutionJob extends Job
	{
		TreeItem[] szTopItems;

		public ExecutionJob(String name, TreeItem[] szTopItems)
		{
			super(name);
			this.szTopItems = szTopItems;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor)
		{

			try
			{

				releaseData();

				shell.getDisplay().syncExec(new Runnable()
				{

					public void run()
					{
						shell.setCursor(waitCursor);
						// **************************************//
						// Txt Upload Report 积己 //
						// --------------------------------------//

						// Report Factory Instance : HeaderNames, HeaderWidths, Num Column
						// Display Flag, Level Column Display Flag
						TxtReportFactory rptFactory = generateReport(true, true);

						if (strImageRoot == null || "".equals(strImageRoot))
						{
							strImageRoot = "c:/temp";

							File temp = new File(strImageRoot);
							if (!temp.exists())
							{
								temp.mkdirs();
							}
						}

						String strDate = DateUtil.getClientDay("yyMMddHHmm");
						String strFileName = "Import_" + strDate + ".log";
						// Upload Log File FullPath
						strLogFileFullPath = strImageRoot + File.separatorChar + strFileName;

						// Import Log File 积己
						rptFactory.saveReport(strLogFileFullPath);

						// Error Count, Warning Count
						int[] szErrorCount = { 0, 0 };
						for (int i = 0; i < szTopItems.length; i++)
						{

							// Top TreeItem
							ManualTreeItem topTreeItem = (ManualTreeItem) szTopItems[i];
							getErrorCount(topTreeItem, szErrorCount);
						}
						nWraningCount = szErrorCount[1];

						text.append("--------------------------\n");
						text.append("Warning : " + szErrorCount[1] + "\n\n");
						text.append("Error : " + szErrorCount[0] + "\n\n\n");
						text.append("[" + strLogFileFullPath + "] " + getTextBundle("LogCreated", "MSG", dlgClass) + "\n\n");

						if (szErrorCount[0] > 0)
						{
							text.append(getTextBundle("ActionNotCompleted", "MSG", dlgClass) + "\n");
							MessageBox.post(shell, getTextBundle("ActionNotCompleted", "MSG", dlgClass), "Error", 2);

						} else
						{
							text.append(getTextBundle("ActionCompleted", "MSG", dlgClass) + "\n");
							MessageBox.post(shell, getTextBundle("ActionCompleted", "MSG", dlgClass), "Notification", 2);
						}
						text.append("--------------------------\n");

						searchButton.setEnabled(true);
						viewLogButton.setEnabled(true);

					}
				});

			} catch (Exception e)
			{
				e.printStackTrace();
			} finally
			{

				try
				{
					// 角青 饶 贸府
					executePost();
				} catch (Exception e)
				{
					e.printStackTrace();
				}

				shell.getDisplay().syncExec(new Runnable()
				{

					public void run()
					{
						shell.setCursor(arrowCursor);
					}
				});

			}

			return new Status(IStatus.OK, "Exporting", "Job Completed");

		}

	}
	
    @Override
    public void importDataPost(ManualTreeItem treeItem) throws Exception
    {        
    }

}
