package com.kgm.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;

/**
 * [SR140724-013][20140725] shcho, Product�� M-Product Function Sync ��� �߰��� ���� Class �ű� ����
 */
public interface FunctionMapper {
	public ArrayList<HashMap<String, String>> serchProductFunction(DataSet ds);
}
