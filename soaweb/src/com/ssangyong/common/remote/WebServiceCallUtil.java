package com.ssangyong.common.remote;


import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * HTTP 통신을 통해 서버로부터 데이터를 client단의 applet에서 처리하게 해주는 Class.
 * <P>
 * 비즈니스 로직을 applet으로부터 분리하여 WAS에 위치시키고, Client단으로 내려간 applet에는 단순히 화면관련 디자인만을 처리하도록 하는 util class.
 * </P>
 * <P>
 * 기존의 비즈니스 로직이 applet에 있을 경우는 아래와 같음.
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
 * AppletRemoteUtil을 사용할 경우 위의 소스는 아래와 같이 바뀜.
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
 * 주의 사항 : 아래와 같은 제약 조건이 존재함.
 * <UL>
 * <LI>method에서 파라미터가 필요하다면 반드시 DataSet 객체에 담아서 넘겨야 함. 파라미터는 오직 paraData 하나만 올 수 있음</LI>
 * <LI>method의 파라미터가 없을 경우에는 null을 사용하면 됨</LI>
 * <LI>DataSet에서 사용할 수 있는 타입은 오직 String만 가능함. 아래의 타입외의 타입은 사용이 전적으로 불가하며 아래와 같이 String 타입으로 형변환이 가능한
 * 경우라도 부분적으로 문제를 야기할 수 있음
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
 * <LI>return값은 오직 Object로만 return 되므로 상황에 맞는 형변환이 필요할 수도 있음</LI>
 * </UL>
 * </P>
 * <P>
 * 
 */
public class WebServiceCallUtil {

	private String strAppletHomeUrl = null;

	private String strEncoding = "ISO-8859-1";

	/**
	 * 생성자.
	 * 
	 * @copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 12. 13.
	 */
	public WebServiceCallUtil(boolean isSsangyongWeb) {
		
		String serviceName = null;
		if(isSsangyongWeb==true){
			serviceName = "ssangyongweb";
		}else{
			serviceName =  "soaweb";
		}

		try {
			boolean isDEV = false;
//			boolean isDEV = true;
			
			if (isDEV) {
				// 개발자 DeskTop Web Server (SsangyongWeb)
				this.strAppletHomeUrl = "http://localhost:7060/"+serviceName+"/HomeServlet";
			} else {
				// [NON-SR][20161103] taeku.jeong
				// 10.80.1.89를 통해 10.80.1.94를 할당받은 SOAWeb  web service에서
				// DB연결을위해 10.80.1.89에 접속하게되면 접속이 거부되는 현상이 발생되어
				// 운영의 경우 서버 연결주소를 localHost로 정의함.
				//String portalWebServer = "http://10.80.1.89"; // 초기에 설정 했던 운영 버서 부하분산
				String portalWebServer = "http://localhost";
				this.strAppletHomeUrl = portalWebServer.trim() + ":7001/"+serviceName+"/HomeServlet";
			}
			
			this.strEncoding = "EUC-KR";
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		System.out.println("this.strAppletHomeUrl = "+this.strAppletHomeUrl);
	}
	
	public String getEncoding(){
	    return this.strEncoding;
	}

	/**
	 * 생성자. hostURL을 받음.
	 * 
	 * @copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 12. 13.
	 * @param hostURL
	 */
	public WebServiceCallUtil(String servletUrl) {
		this.strAppletHomeUrl = servletUrl;
		this.strEncoding = "EUC-KR";
	}
	
	/**
	 * HTTP 통신을 통해 서버로부터 데이터를 client단의 applet에서 처리하게 실행하는 method.
	 * <P>
	 * 
	 * @param class_name
	 *            처리할 JavaBeans class name
	 * @param method
	 *            처리할 JavaBeans에서 호출할 method
	 * @param paramData
	 *            호출할 method에서 필요할 파라미터들을 모아놓은 DataSet
	 */
	public Object execute(String class_name, String method, DataSet paramData) throws Exception {
		return execute(class_name, method, paramData, true);
	}

	/**
	 * HTTP 통신을 통해 서버로부터 데이터를 client단의 applet에서 처리하게 실행하는 method.
	 * <P>
	 * 
	 * @param class_name
	 *            처리할 JavaBeans class name
	 * @param method
	 *            처리할 JavaBeans에서 호출할 method
	 * @param paramData
	 *            호출할 method에서 필요할 파라미터들을 모아놓은 DataSet
	 * @param blnReturn
	 *            호출할 method가 void 타입일 경우 return이 없다는 의미로 false를 넘겨줌. 이때 결과는 null을 return함.
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
			
			System.out.println("strAppletHomeUrl = "+strAppletHomeUrl);

			URL url = new URL(strAppletHomeUrl);
			URLConnection urlConn = url.openConnection();

			urlConn.setDoOutput(true);
			urlConn.setDoInput(true);
			urlConn.setUseCaches(false);

			urlConn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
			//urlConn.setRequestProperty("Content-length", String.valueOf(strParameter.length()));

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
