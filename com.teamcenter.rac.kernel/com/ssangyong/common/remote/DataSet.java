package com.ssangyong.common.remote;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


@SuppressWarnings("serial")
public class DataSet extends HashMap<String, Object> {

	public DataSet() {
	}

	public void setObject(String s, Object obj) {
		super.put(s, obj);
	}

	@SuppressWarnings("rawtypes")
	public void setCollection(String s, Collection collection) {
		super.put(s, collection);
	}

	public void setString(String s, String s1) {
		super.put(s, s1);
	}

	public void setStrings(String s, String as[]) {
		super.put(s, as);
	}

	public void setBoolean(String s, boolean flag) {
		super.put(s, new Boolean(flag));
	}

	public void setChar(String s, char c) {
		super.put(s, new Character(c));
	}

	public void setByte(String s, byte byte0) {
		super.put(s, new Byte(byte0));
	}

	public void setBytes(String s, byte abyte0[]) {
		super.put(s, abyte0);
	}

	public void setDouble(String s, double d) {
		super.put(s, new Double(d));
	}

	public void setFloat(String s, float f) {
		super.put(s, new Float(f));
	}

	public void setInt(String s, int i) {
		super.put(s, new Integer(i));
	}

	public void setLong(String s, long l) {
		super.put(s, new Long(l));
	}

	public void setShort(String s, short word0) {
		super.put(s, new Short(word0));
	}

	public Object getObject(String s) {
		return super.get(s);
	}

	@SuppressWarnings("rawtypes")
	public Collection getCollection(String s) throws DataTypeException {
		Object obj = getObject(s);
		if (obj == null)
			return new ArrayList();
		if (obj instanceof Collection)
			return (Collection) obj;
		else
			throw new DataTypeException();
	}

	public String getString(String s) {
		Object obj = getObject(s);
		if (obj == null)
			return "";
		if (obj instanceof Boolean)
			return String.valueOf(((Boolean) obj).booleanValue());
		if (obj instanceof Character)
			return String.valueOf(((Character) obj).charValue());
		if (obj instanceof Number)
			return ((Number) obj).toString();
		if (obj instanceof byte[])
			return new String((byte[]) obj);
		if (obj instanceof String[])
			return ((String[]) obj)[0];
		else
			return (String) obj;
	}

	public String[] getStrings(String s) throws DataTypeException {
		Object obj = getObject(s);
		if (obj == null)
			return new String[0];
		if (obj instanceof String[])
			return (String[]) obj;
		if (obj instanceof String)
			return (new String[] { (String) obj });
		else
			throw new DataTypeException();
	}

	public boolean getBoolean(String s) throws DataTypeException {
		Object obj = getObject(s);
		if (obj == null)
			return false;
		if (obj instanceof Boolean)
			return ((Boolean) obj).booleanValue();
		if (obj instanceof String)
			return (new Boolean((String) obj)).booleanValue();
		else
			throw new DataTypeException();
	}

	public char getChar(String s) throws DataTypeException {
		Object obj = getObject(s);
		if (obj == null)
			return '\0';
		if (obj instanceof Character)
			return ((Character) obj).charValue();
		if (obj instanceof String)
			return ((String) obj).charAt(0);
		else
			throw new DataTypeException();
	}

	public byte getByte(String s) throws DataTypeException {
		Object obj = getObject(s);
		if (obj == null)
			return 0;
		if (obj instanceof Number)
			return ((Number) obj).byteValue();
		if (obj instanceof String)
			return (new Byte((String) obj)).byteValue();
		else
			throw new DataTypeException();
	}

	public byte[] getBytes(String s) throws DataTypeException {
		Object obj = getObject(s);
		if (obj == null)
			return new byte[0];
		if (obj instanceof byte[])
			return (byte[]) obj;
		if (obj instanceof Byte)
			return (new byte[] { ((Byte) obj).byteValue() });
		if (obj instanceof String)
			return ((String) obj).getBytes();
		else
			throw new DataTypeException();
	}

	public double getDouble(String s) throws DataTypeException {
		Object obj = getObject(s);
		if (obj == null)
			return 0.0D;
		if (obj instanceof Number)
			return ((Number) obj).doubleValue();
		if (obj instanceof String)
			return Double.parseDouble((String) obj);
		else
			throw new DataTypeException();
	}

	public float getFloat(String s) throws DataTypeException {
		Object obj = getObject(s);
		if (obj == null)
			return 0.0F;
		if (obj instanceof Number)
			return ((Number) obj).floatValue();
		if (obj instanceof String)
			return Float.parseFloat((String) obj);
		else
			throw new DataTypeException();
	}

	public int getInt(String s) throws DataTypeException {
		Object obj = getObject(s);
		if (obj == null)
			return 0;
		if (obj instanceof Number)
			return ((Number) obj).intValue();
		if (obj instanceof String)
			return Integer.parseInt((String) obj);
		else
			throw new DataTypeException();
	}

	public long getLong(String s) throws DataTypeException {
		Object obj = getObject(s);
		if (obj == null)
			return 0L;
		if (obj instanceof Number)
			return ((Number) obj).longValue();
		if (obj instanceof String)
			return Long.parseLong((String) obj);
		else
			throw new DataTypeException();
	}

	public short getShort(String s) throws DataTypeException {
		Object obj = getObject(s);
		if (obj == null)
			return 0;
		if (obj instanceof Number)
			return ((Number) obj).shortValue();
		if (obj instanceof String)
			return Short.parseShort((String) obj);
		else
			throw new DataTypeException();
	}
}
