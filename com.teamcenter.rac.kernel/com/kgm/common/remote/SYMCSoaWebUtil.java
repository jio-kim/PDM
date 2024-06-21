package com.kgm.common.remote;


import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

/**
 * HTTP ����� ���� �����κ��� �����͸� client���� applet���� ó���ϰ� ���ִ� Class.
 * <P>
 * ����Ͻ� ������ applet���κ��� �и��Ͽ� WAS�� ��ġ��Ű��, Client������ ������ applet���� �ܼ��� ȭ����� �����θ��� ó���ϵ��� �ϴ� util class.
 * </P>
 * <P>
 * ������ ����Ͻ� ������ applet�� ���� ���� �Ʒ��� ����.
 * </P>
 * <P>
 * <BLOCKQUOTE>
 * 
 * <PRE>
 * 
 * DataSet paramData = new DataSet(); paramData.setString("param1", "param1_value"); paramData.setString("param2", "param2_value"); paramData.setString("param3", "param3_value"); ... XxxBean xxxBean =
 * new XxxBean(); DataSet resultData = xxxBean.methodName(paramData);
 * 
 * </PRE>
 * 
 * </BLOCKQUOTE>
 * </P>
 * <P>
 * AppletRemoteUtil�� ����� ��� ���� �ҽ��� �Ʒ��� ���� �ٲ�.
 * </P>
 * <P>
 * <BLOCKQUOTE>
 * 
 * <PRE>
 * 
 * String httpProtocolUrl = "http://fmea.gdnps.com:8088"; String encoding = "EUC-KR"; DataSet paramData = new DataSet(); paramData.setString("param1", "param1_value"); paramData.setString("param2",
 * "param2_value"); paramData.setString("param3", "param3_value"); ... AppletRemoteUtil appletRemoteUtil = new AppletRemoteUtil(httpProtocolUrl, encoding); DataSet resultData =
 * (DataSet)appletRemoteUtil.execute("aaa.bbb.XxxBean", "methodName", paramData);
 * 
 * </PRE>
 * 
 * </BLOCKQUOTE>
 * </P>
 * <P>
 * ���� ���� : �Ʒ��� ���� ���� ������ ������.
 * <UL>
 * <LI>method���� �Ķ���Ͱ� �ʿ��ϴٸ� �ݵ�� DataSet ��ü�� ��Ƽ� �Ѱܾ� ��. �Ķ���ʹ� ���� paraData �ϳ��� �� �� ����</LI>
 * <LI>method�� �Ķ���Ͱ� ���� ��쿡�� null�� ����ϸ� ��</LI>
 * <LI>DataSet���� ����� �� �ִ� Ÿ���� ���� String�� ������. �Ʒ��� Ÿ�Կ��� Ÿ���� ����� �������� �Ұ��ϸ� �Ʒ��� ���� String Ÿ������ ����ȯ�� ������
 * ���� �κ������� ������ �߱��� �� ����
 * <UL>
 * <LI>boolean</LI>
 * <LI>byte</LI>
 * <LI>byte[]</LI>
 * <LI>char</LI>
 * <LI>double</LI>
 * <LI>float</LI>
 * <LI>int</LI>
 * <LI>long</LI>
 * <LI>short</LI>
 * </UL>
 * </LI>
 * <LI>return���� ���� Object�θ� return �ǹǷ� ��Ȳ�� �´� ����ȯ�� �ʿ��� ���� ����</LI>
 * </UL>
 * </P>
 * <P>
 * 
 */
public class SYMCSoaWebUtil {

	private String strAppletHomeUrl = null;

	private String strEncoding = "ISO-8859-1";

	public SYMCSoaWebUtil() {
		try {
			boolean isLocal = false;
			String isLocalSOA = System.getProperty("isLocalSOA");
			if(isLocalSOA != null)
			{
				isLocal = isLocalSOA.equalsIgnoreCase("true");
			}
			if (isLocal) {
				//this.strAppletHomeUrl = "http://localhost:7090/soaweb/HomeServlet";
				this.strAppletHomeUrl = "http://localhost:8080/soaweb/HomeServlet";
			} else {
				// Registry registry = Registry.getRegistry("site_specific");
				// String portalWebServer = registry.getString("portalWebServer");
				// [20240621][UPGRADE] preference���� Server IP �� ���������� ����
				TCSession session = (TCSession) AIFUtility.getSessionManager().getDefaultSession();
				TCPreferenceService tcpreferenceservice = session.getPreferenceService();
				String portalWebServer = tcpreferenceservice.getStringValue("WEB_HOST_VIP");
				this.strAppletHomeUrl = "http://" + portalWebServer + ":7070/soaweb/HomeServlet";
			}
			
			this.strEncoding = "EUC-KR";
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getEncoding(){
	    return this.strEncoding;
	}

	/**
	 * ������. hostURL�� ����.
	 * 
	 * @copyright : S-PALM
	 * @author : �ǻ��
	 * @since : 2012. 12. 13.
	 * @param hostURL
	 */
	public SYMCSoaWebUtil(String servletUrl) {
		this.strAppletHomeUrl = servletUrl;
		this.strEncoding = "EUC-KR";
	}
	
	/**
	 * HTTP ����� ���� �����κ��� �����͸� client���� applet���� ó���ϰ� �����ϴ� method.
	 * <P>
	 * 
	 * @param class_name
	 *            ó���� JavaBeans class name
	 * @param method
	 *            ó���� JavaBeans���� ȣ���� method
	 * @param paramData
	 *            ȣ���� method���� �ʿ��� �Ķ���͵��� ��Ƴ��� DataSet
	 */
	public Object execute(String class_name, String method, DataSet paramData) throws Exception {
		return execute(class_name, method, paramData, true);
	}

	/**
	 * HTTP ����� ���� �����κ��� �����͸� client���� applet���� ó���ϰ� �����ϴ� method.
	 * <P>
	 * 
	 * @param class_name
	 *            ó���� JavaBeans class name
	 * @param method
	 *            ó���� JavaBeans���� ȣ���� method
	 * @param paramData
	 *            ȣ���� method���� �ʿ��� �Ķ���͵��� ��Ƴ��� DataSet
	 * @param blnReturn
	 *            ȣ���� method�� void Ÿ���� ��� return�� ���ٴ� �ǹ̷� false�� �Ѱ���. �̶� ����� null�� return��.
	 */
	public Object execute(String class_name, String method, DataSet paramData, boolean blnReturn) throws Exception {
		Object obj = null;
		ObjectOutputStream output = null;
		ObjectInputStream input = null;

		try {
			if (paramData == null) {
				paramData = new DataSet();
			}

			paramData.setString("class_name", class_name);
			paramData.setString("method", method);
			paramData.setBoolean("blnReturn", blnReturn);

			String strParameter = "";

			URL url = new URL(strAppletHomeUrl);
			URLConnection urlConn = url.openConnection();

			urlConn.setDoOutput(true);
			urlConn.setDoInput(true);
			urlConn.setUseCaches(false);

			urlConn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
			urlConn.setRequestProperty("Content-length", String.valueOf(strParameter.length()));

			output = new ObjectOutputStream(urlConn.getOutputStream());
			output.writeObject(paramData);

			input = new ObjectInputStream(new BufferedInputStream(urlConn.getInputStream()));

			obj = input.readObject();

			if (obj instanceof Exception) {
				throw (Exception) obj;
			}
		} finally {
			if (output != null)
				output.close();
			if (input != null)
				input.close();
		}

		return obj;
	}
}
