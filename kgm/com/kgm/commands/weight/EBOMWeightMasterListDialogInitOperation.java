/**
 * 
 */
package com.kgm.commands.weight;

import java.util.ArrayList;

import com.kgm.common.WaitProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;

/**
 * [SR170707-024] E-BOM Weight Report ���� ��û
 * @Copyright : Plmsoft
 * @author   : ������
 * @since    : 2017. 7. 10.
 * Package ID : com.kgm.commands.weight.EBOMWeightMasterListDialogInitOperation.java
 */
public class EBOMWeightMasterListDialogInitOperation extends AbstractAIFOperation
{
	/* (non-Javadoc)
	 * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
	 */
	@Override
	public void executeOperation() throws Exception {
		try
		{
			ArrayList<TCComponentItem>allProductList = getAllProductList();

			storeOperationResult(allProductList);
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	/**
	 * Product Item �˻�
	 * @Copyright : Plmsoft
	 * @author : ������
	 * @since  : 2017. 7. 10.
	 * @return
	 * @throws Exception
	 */
	private ArrayList<TCComponentItem> getAllProductList() throws Exception {
		try
		{
			ArrayList<TCComponentItem> productLists = new ArrayList<TCComponentItem>();

			TCComponent[] findProducts = CustomUtil.queryComponent("Item...", new String[]{"Type", "Item ID"}, new String[]{TypeConstant.S7_PRODUCTTYPE, "*"});

			for (TCComponent findProduct : findProducts)
			{
				productLists.add((TCComponentItem) findProduct);
			}

			return productLists;
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}
}