<%@page language="java" contentType="text/plain; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.ssangyong.dao.ECOInfoDao,java.util.*,com.ssangyong.common.remote.DataSet"%>
<%
	String sPartNo = (String) request.getParameter("PART_NO");

	ECOInfoDao dao = new ECOInfoDao();
	DataSet ds = new DataSet();
	ds.put("PART_NO", sPartNo);
	String sConnectedFunction = dao.isConnectedFunction(ds);
%>
VALUE=<%=sConnectedFunction%>