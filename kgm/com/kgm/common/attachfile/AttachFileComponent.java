package com.kgm.common.attachfile;

import java.io.File;

import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;

/**
 * �� Class�� ÷�� ���� �߰� ������ ���Ǵ� component�� �����Ѵ�.<br>
 * JList�� data�� loading�� �Ǹ� AttachFilePanel���� ���ǰ� ���� ���� �� ������ ȣ��Ǿ� Operation���� ���� ���̴�.<br>
 * <br>
 * ���� ��ü�� ADD ���¸� �����ϰ� �ȴ�. DELETE�� NORMAL���´� ������ �� ����.<br>
 * dataset ��ü�� NORMAL ���¿� DELETE���¸� �����ϰ� �ȴ�. ADD ���´� ������ �� ����.<br>
 * <br>
 * @author park seho
 */
public class AttachFileComponent
{
	public static int NORMAL = 0;
	public static int ADD = 1;
	public static int DELETE = 2;

	private int state = 0;
	private Object fileObject = null;

	private String fileExtend = "";
	private String fileDatasetType = "";
	private String fileNamedReferenceType = "";
	private String newDatasetName = "";
	private String newDatasetDesc = "";

	/**
	 * file ��ü�� ���¸� �Է� �޴� ������
	 * @param file File
	 * @param _state int 0=normal, 1=add, 2=delete
	 */
	public AttachFileComponent(File file, String _fileDatasetType, int _state, String _newDatasetName, String _newDatasetDesc) throws Exception
	{
		fileObject = file;
		setState(_state);
		String fileName = file.getName();
		fileExtend = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
		fileDatasetType = _fileDatasetType;
		if(_newDatasetName == null || _newDatasetName.trim().equals(""))
		{
			setNewDatasetName(fileName.substring(0, fileName.lastIndexOf(".")));
		} else
		{
			setNewDatasetName(_newDatasetName);
		}
		if(_newDatasetDesc == null || _newDatasetDesc.trim().equals(""))
		{
			setNewDatasetDesc("Import Local File...[" + fileName + "]");
		}else
		{
			setNewDatasetDesc(_newDatasetDesc);
		}
		try
		{
			fileNamedReferenceType = CustomUtil.getNamedReferenceTypeString(fileDatasetType, fileExtend);
		} catch(Exception ex)
		{
		}
	}

	/**
	 * IMANComponentDataset ��ü�� ���¸� �Է¹޴� ������
	 * @param dataset IMANComponentDataset
	 * @param _state int 0=normal, 1=add, 2=delete
	 */
	public AttachFileComponent(AIFComponentContext dataset, int _state, String _newDatasetName, String _newDatasetDesc)
	{
		fileObject = dataset;
		state = _state;
		if(state == NORMAL || state == DELETE)
		{
			try
			{
				setNewDatasetName(dataset.getComponent().toString());
				setNewDatasetDesc(dataset.getComponent().getProperty("object_desc"));
			} catch(Exception ex)
			{
			}
		} else
		{
			if(_newDatasetName == null || _newDatasetName.trim().equals(""))
			{
				setNewDatasetName("copy_" + dataset.getComponent().toString());
			} else
			{
				setNewDatasetName(_newDatasetName);
			}
			if(_newDatasetDesc == null || _newDatasetDesc.trim().equals(""))
			{
				setNewDatasetDesc("Save As Dataset...[" + dataset.getComponent().toString() + "]");
			}else
			{
				setNewDatasetDesc(_newDatasetDesc);
			}
		}
	}

	public AttachFileComponent(TCComponentForm url, int _state)
	{
		fileObject = url;
		state = _state;
		try
		{
			setNewDatasetName(url.toString());
			setNewDatasetDesc(url.getProperty("object_desc"));
		} catch(Exception ex)
		{
		}
	}

	/**
	 * ���� �߰��� ��ü�� File ��ü���� Ȯ���ϴ� method
	 * @return boolean File��ü�̸� true�� �����Ѵ�.
	 */
	public boolean isFileComponent()
	{
		return fileObject instanceof File;
	}

	/**
	 * ���� �߰��� ��ü�� TCComponentDataset���� Ȯ���ϴ� method
	 * @return boolean TCComponentDataset ��ü�̸� true�� �����Ѵ�.
	 */
	public boolean isAIFComponentContext()
	{
		return fileObject instanceof AIFComponentContext;
	}

	/**
	 * ���� �߰��� ��ü�� Web link���� Ȯ��.
	 * @return boolean
	 */
	public boolean isWebLinkComponent()
	{
		return fileObject instanceof TCComponentForm;
	}

	/**
	 * ���� �߰��� ��ü�� �����Ͽ� �ش�.
	 * @return Object ���� �߰��� ��ü
	 */
	public Object getAttachObject()
	{
		return fileObject;
	}

	/**
	 * ���� ������ Dataset�� �̸��� �����ش�.
	 * @return String
	 */
	public String getNewDatasetName()
	{
		return newDatasetName;
	}

	/**
	 * ���� ������ Dataset�� Description�� �����ش�.
	 * @return String
	 */
	public String getNewDatasetDesc()
	{
		return newDatasetDesc;
	}

	/**
	 * ���� component�� ���¸� �����ִ� method
	 * @return int 0=normal, 1=add, 2=delete
	 */
	public int getState()
	{
		return state;
	}

	/**
	 * ���� ��ü�� dataset type�� �����ش�.
	 * @return String
	 * @throws Exception
	 */
	public String getDatasetType() throws Exception
	{
		if(isAIFComponentContext())
		{
			TCComponentDataset dataset = (TCComponentDataset)((AIFComponentContext)fileObject).getComponent();
			return dataset.getType();
		} else
		{
			return fileDatasetType;
		}
	}

	/**
	 * component�� ���� ���¸� �����ϴ� method
	 * @param i int 0=normal, 1=add, 2=delete
	 */
	public void setState(int i)
	{
		state = i;
	}

	/**
	 * ���� Ȯ���ڸ� �����ش�.
	 * @return String
	 */
	public String getFileExtend()
	{
		return fileExtend;
	}

	/**
	 * ���Ͽ� ���� dataset�� namedreference�� �����ش�.
	 * @return String
	 */
	public String getFileNamedReferenceType()
	{
		return fileNamedReferenceType;
	}

	/**
	 * ������ path�� �����ش�.
	 * @return String
	 */
	public String getFilePath()
	{
		return((File)fileObject).getPath();
	}

	/**
	 * ���ο� dataset�� �̸��� �Է��Ѵ�.
	 * @param _newDatasetName String
	 */
	public void setNewDatasetName(String _newDatasetName)
	{
		newDatasetName = CustomUtil.cutString(_newDatasetName, 32);
	}

	/**
	 * ���ο� dataset�� description�� �Է��Ѵ�.
	 * @param _newDatasetDesc String
	 */
	public void setNewDatasetDesc(String _newDatasetDesc)
	{
		newDatasetDesc = CustomUtil.cutString(_newDatasetDesc, 256);
	}

	public String toString()
	{
		if(isFileComponent())
		{
			return((File)fileObject).getAbsolutePath() + " [Dataset Name : " + getNewDatasetName() + "]";
		} else if(isAIFComponentContext())
		{
			String returnString = "";

			if(((AIFComponentContext)fileObject).getComponent() instanceof TCComponentDataset){
				TCComponentDataset dataset = (TCComponentDataset)((AIFComponentContext)fileObject).getComponent();
				try
				{
					TCComponentTcFile[] imanFile = dataset.getTcFiles();
					if(imanFile == null || imanFile.length == 0)
					{
						returnString = dataset.toString();
					} else
					{
						returnString = dataset.toString();
						for(int i = 0; i < imanFile.length; i++)
						{
							if(i == 0)
							{
								returnString += "[";
							}
							returnString += imanFile[i].toString();
							if((imanFile.length - 1) - i >= 1)
							{
								returnString += ", ";
							}
							if(i == imanFile.length - 1)
							{
								returnString += "]";
							}
						}
					}
				} catch(TCException ex)
				{
					returnString = dataset.toString();
				}
				if(state == ADD)
				{
					returnString += " [Dataset Name : " + getNewDatasetName() + "]";
				}
			}
			else if(((AIFComponentContext)fileObject).getComponent() instanceof TCComponentItemRevision){
				TCComponentItemRevision revision = (TCComponentItemRevision)((AIFComponentContext)fileObject).getComponent();
				try {
					returnString = revision.getProperty("object_string");
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
			else if(((AIFComponentContext)fileObject).getComponent() instanceof TCComponentItem){
				TCComponentItem item = (TCComponentItem)((AIFComponentContext)fileObject).getComponent();
				try {
					returnString = item.getProperty("object_string");
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
			return returnString;
		} else if(isWebLinkComponent())
		{
			return fileObject.toString();
		}else
		{
			return "No Object";
		}
	}

}
