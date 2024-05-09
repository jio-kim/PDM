package com.kgm.typehandler;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.HashMap;

import oracle.sql.StructDescriptor;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

/**
 * Procedure 결과 Type을 처리하기 위한 핸들러
 * [SR140722-022][2014. 5. 22.] swyoon 최초 등록
 */
@SuppressWarnings("rawtypes")
public class ModuleBomValidationTypeHandler implements TypeHandler<ArrayList> {

	@Override
	public ArrayList getResult(ResultSet arg0, String arg1) throws SQLException {
		return null;
	}

	@Override
	public ArrayList getResult(ResultSet arg0, int arg1) throws SQLException {
		return null;
	}

	@Override
	public ArrayList<HashMap<String, Object>> getResult(CallableStatement cs, int arg1) throws SQLException {

		ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

        final String typeName = "VALIDATION_ROW_TYPE";

        final StructDescriptor structDescriptor = StructDescriptor.createDescriptor(typeName.toUpperCase(), cs.getConnection());
        final ResultSetMetaData metaData = structDescriptor.getMetaData();

		Object[] data = (Object[]) ((Array) cs.getObject(arg1)).getArray();
        for(Object tmp : data) {
            Struct row = (Struct) tmp;
            // Attributes are index 1 based...
            int idx = 1;
            HashMap<String, Object> map = new HashMap<String, Object>();
            for(Object attribute : row.getAttributes()) {
            	map.put(metaData.getColumnName(idx), attribute);
                //System.out.println(metaData.getColumnName(idx) + " = " + attribute);
                ++idx;
            }
            if( !map.isEmpty()){
            	list.add(map);
            }
            //System.out.println("---");
        }
		return list;
	}

	@Override
	public void setParameter(PreparedStatement arg0, int arg1, ArrayList arg2,
			JdbcType arg3) throws SQLException {
	}

}