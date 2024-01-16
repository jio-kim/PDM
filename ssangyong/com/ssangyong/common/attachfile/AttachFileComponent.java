package com.ssangyong.common.attachfile;

import java.io.File;

import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;

/**
 * 이 Class는 첨부 파일 추가 삭제에 사용되는 component를 정의한다.<br>
 * JList에 data가 loading이 되며 AttachFilePanel에서 사용되고 이후 생성 및 수정시 호출되어 Operation에서 사용될 것이다.<br>
 * <br>
 * 파일 객체는 ADD 상태만 존재하게 된다. DELETE와 NORMAL상태는 존재할 수 없다.<br>
 * dataset 객체는 NORMAL 상태와 DELETE상태만 존재하게 된다. ADD 상태는 존재할 수 없다.<br>
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
	 * file 객체와 상태를 입력 받는 생성자
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
	 * IMANComponentDataset 객체와 상태를 입력받는 생성자
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
	 * 현재 추가된 객체가 File 객체인지 확인하는 method
	 * @return boolean File객체이면 true를 리턴한다.
	 */
	public boolean isFileComponent()
	{
		return fileObject instanceof File;
	}

	/**
	 * 현재 추가된 객체가 TCComponentDataset인지 확인하는 method
	 * @return boolean TCComponentDataset 객체이면 true를 리턴한다.
	 */
	public boolean isAIFComponentContext()
	{
		return fileObject instanceof AIFComponentContext;
	}

	/**
	 * 현재 추가된 객체가 Web link인지 확인.
	 * @return boolean
	 */
	public boolean isWebLinkComponent()
	{
		return fileObject instanceof TCComponentForm;
	}

	/**
	 * 현재 추가된 객체를 리턴하여 준다.
	 * @return Object 현재 추가된 객체
	 */
	public Object getAttachObject()
	{
		return fileObject;
	}

	/**
	 * 새로 생성될 Dataset의 이름을 돌려준다.
	 * @return String
	 */
	public String getNewDatasetName()
	{
		return newDatasetName;
	}

	/**
	 * 새로 생성될 Dataset의 Description을 돌려준다.
	 * @return String
	 */
	public String getNewDatasetDesc()
	{
		return newDatasetDesc;
	}

	/**
	 * 현재 component의 상태를 돌려주는 method
	 * @return int 0=normal, 1=add, 2=delete
	 */
	public int getState()
	{
		return state;
	}

	/**
	 * 현재 객체의 dataset type을 돌려준다.
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
	 * component의 현재 상태를 변경하는 method
	 * @param i int 0=normal, 1=add, 2=delete
	 */
	public void setState(int i)
	{
		state = i;
	}

	/**
	 * 파일 확장자를 돌려준다.
	 * @return String
	 */
	public String getFileExtend()
	{
		return fileExtend;
	}

	/**
	 * 파일에 대한 dataset의 namedreference를 돌려준다.
	 * @return String
	 */
	public String getFileNamedReferenceType()
	{
		return fileNamedReferenceType;
	}

	/**
	 * 파일의 path를 돌려준다.
	 * @return String
	 */
	public String getFilePath()
	{
		return((File)fileObject).getPath();
	}

	/**
	 * 새로운 dataset의 이름을 입력한다.
	 * @param _newDatasetName String
	 */
	public void setNewDatasetName(String _newDatasetName)
	{
		newDatasetName = CustomUtil.cutString(_newDatasetName, 32);
	}

	/**
	 * 새로운 dataset의 description을 입력한다.
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
