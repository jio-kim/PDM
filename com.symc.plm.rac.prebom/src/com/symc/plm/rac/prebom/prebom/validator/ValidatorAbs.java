package com.symc.plm.rac.prebom.prebom.validator;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.StringUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.util.Registry;

/**
 * Abstract Validation Class
 * 
 * 1. Item 중복 Check
 * 2. LOV 값 Check
 */
public abstract class ValidatorAbs
{
	/** Create Type */
	public static final int TYPE_VALID_CREATE = 0;
	/** Modify Type */
	public static final int TYPE_VALID_MODIFY = 1;
	/** LOV Value Map */
	protected HashMap<String, String[]> lovMap;
	/** TC Registry */
	protected Registry registry;

	/**
	 * LOV값 초기화
	 * 
	 * @param szLovNames
	 *        : LOV Names
	 */
	public ValidatorAbs(String[][] szLovNames)
	{
		this.registry = Registry.getRegistry(this);
		if (szLovNames != null && szLovNames.length != 0)
		{
			this.lovMap = new HashMap<String, String[]>();
			for (int i = 0; i < szLovNames.length; i++)
			{
				try
				{
					//TCComponentListOfValues listofvalue = TCComponentListOfValuesType.findLOVByName(CustomUtil.getTCSession(), szLovNames[i][1]);
					TCComponentListOfValues listofvalue = TCComponentListOfValuesType.findLOVByName(szLovNames[i][1]);

					// UOM은 Object이므로 별도처리
					if ("Unit of Measures".equals(szLovNames[i][1]))
					{
						Object[] objs = listofvalue.getListOfValues().getListOfValues();
						String[] lovValues = new String[objs.length];
						for (int j = 0; j < objs.length; j++)
						{
							lovValues[j] = objs[j].toString();
						}
					}
					else
					{
						String[] lovValues = listofvalue.getListOfValues().getStringListOfValues();
						this.lovMap.put(szLovNames[i][0], lovValues);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Item 중복 Check
	 * LOV 값 Check
	 * 
	 * @param attrMap : Part 속성 Map
	 * @param nType   : Validation Type
	 * @return
	 * @throws Exception
	 */
	public String validate(HashMap<String, Object> attrMap, int nType) throws Exception
	{
		StringBuffer bufMessage = new StringBuffer();
		if (nType == TYPE_VALID_CREATE)
		{
			String strItemID = (String) attrMap.get("item_id");
			if (CustomUtil.isEmpty(strItemID))
			{
				// Item ID가 유효하지 않습니다.
				bufMessage.append(registry.getString("ValidatorAbs.MSG.notValideItemId") + "\n");
			}
			else
			{
			    ArrayList<String> queryNames = new ArrayList<String>();
			    ArrayList<String> queryValues = new ArrayList<String>();

			    queryNames.add("ItemID");
			    queryValues.add(strItemID);
int a = 0;
			    if (attrMap.containsKey(PropertyConstant.ATTR_NAME_ITEMTYPE))
			    {
			        queryNames.add("Type");
			        queryValues.add(attrMap.get(PropertyConstant.ATTR_NAME_ITEMTYPE).toString());
			    }

			    TCComponent[] searchedList = CustomUtil.queryComponent("Item...", queryNames.toArray(new String[0]), queryValues.toArray(new String[0]));

				if (searchedList != null && searchedList.length > 0)
				{
					// Item ID가 존재합니다.
					bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.existItemId", new String[] { strItemID }) + "\n");
				}

			}
		}
		bufMessage.append(this.validateLOV(attrMap));
		return bufMessage.toString();
	}

	/**
	 * 입력된 값이 LOV 에 명시된 항목인지 Check
	 * 
	 * @param attrMap : Part 속성 Map
	 * @return
	 */
	public String validateLOV(HashMap<String, Object> attrMap)
	{
		if (this.lovMap == null || this.lovMap.size() == 0)
		{
			return "";
		}
		String[] szKey = attrMap.keySet().toArray(new String[attrMap.size()]);
		StringBuffer bufMessage = new StringBuffer();
		for (int i = 0; i < szKey.length; i++)
		{
			if (this.lovMap.containsKey(szKey[i]))
			{
				boolean isValidValue = false;
				String[] szValues = this.lovMap.get(szKey[i]);
				String value = (String) attrMap.get(szKey[i]);
				if (szValues == null || szValues.length == 0 || value == null || "".equals(value))
				{
					continue;
				}
				for (int j = 0; j < szValues.length; j++)
				{
					if (value.equals(szValues[j]))
					{
						isValidValue = true;
						break;
					}
				}
				if (!isValidValue)
				{
					// szKey[i] + 속성값이 List에 존재하지 않습니다.
					bufMessage.append(StringUtil.getString(registry, "ValidatorAbs.MSG.notExistAttrValue", new String[] { szKey[i] }) + "\n");
				}
			}
		}
		return bufMessage.toString();
	}

	/**
	 * Validator Skip 여부
	 * 
	 * @method setValidatorSkip
	 * @date 2013. 4. 17.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	public void setValidatorSkip(boolean check)
	{

	}
}
