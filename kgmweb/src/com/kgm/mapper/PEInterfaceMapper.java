package com.kgm.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;

/**
 * [������ 2�� ������] [20150907][ymjang] ������ ó���� �� ��� Part ���� ���¸� ����ϱ� ���� ���� ���̺� �����͸� �����Ѵ�.
 */
public interface PEInterfaceMapper {
	
	public  ArrayList<HashMap<String, Object>> getProductEndItemABSOccPuidList(DataSet ds) throws Exception;
	
}
