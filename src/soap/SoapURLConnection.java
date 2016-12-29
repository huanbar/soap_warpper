package soap;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class SoapURLConnection {
	static String ENCODING = "UTF-8";

	/**
	 * soap11
	 * 
	 * @param url
	 *            接口地址wsdl文件中soap:address location指向
	 * @param namespace
	 *            命名空间 wsdl文件中xmlns:ns指向
	 * @param user
	 *            用户名
	 * @param password
	 *            密码
	 * @param body
	 *            body调用方法的参数封装字符串
	 * @param action
	 *            调用方法
	 * @return String
	 */
	public static String soap(String url, String namespace, String user, String password, String body, String action) {
		StringBuffer req = new StringBuffer();
		req.append(
				"<?xml version='1.0' encoding='UTF-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Header><nsl:AuthenticationToken xmlns:nsl=\"");
		req.append(namespace);
		req.append("\"><nsl:Username>");
		req.append(user);
		req.append("</nsl:Username><nsl:Password>");
		req.append(password);
		req.append("</nsl:Password></nsl:AuthenticationToken></soapenv:Header><soapenv:Body><ns2:");
		req.append(action);
		req.append(" xmlns:ns2=\"");
		req.append(namespace);
		req.append("\">");
		req.append(body);
		req.append("</ns2:");
		req.append(action);
		req.append("></soapenv:Body></soapenv:Envelope>");
		String result = httpRequest(url, req.toString(), action);
		SAXReader saxReader = new SAXReader();
		Document document;
		try {
			document = saxReader.read(new ByteArrayInputStream(result.getBytes(ENCODING)));
			Element root = document.getRootElement();
			Element resp = root.element("Body");
			return resp.getStringValue();
		} catch (Exception e) {
			System.out.println("crm response error: " + result);
		}
		return result;
	}

	/**
	 * 发起http请求并获取结果
	 * 
	 * @param reqUrl
	 *            请求地址 
	 * @param outputStr
	 *            提交的数据
	 * @param action
	 *            soap方法
	 * @return String
	 */
	public static String httpRequest(String reqUrl, String outputStr, String action) {

		final StringBuilder stringBuffer = new StringBuilder(255);
		URLConnection conn = null;
		try {
			URL url = new URL(reqUrl);
			conn = url.openConnection();
			if (conn instanceof HttpsURLConnection) {
				// 不验证ip
				((HttpsURLConnection) conn).setHostnameVerifier(new HostnameVerifier() {
					@Override
					public boolean verify(String arg0, SSLSession arg1) {
						return true;
					}
				});
				// 信任证书
				TrustManager[] tm = { new X509TrustManager() {
					public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					}

					public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					}

					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}

				} };
				SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
				sslContext.init(null, tm, new java.security.SecureRandom());
				// 从SSLContext对象中得到SSLSocketFactory对象
				SSLSocketFactory ssf = sslContext.getSocketFactory();

				((HttpsURLConnection) conn).setSSLSocketFactory(ssf);

			}
			((HttpURLConnection) conn).setRequestMethod("POST");

			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestProperty("Content-Type", "text/xml;charset=" + ENCODING);
			conn.setRequestProperty("Content-Length", String.valueOf(outputStr.getBytes(ENCODING).length));
			conn.setRequestProperty("Accept-Encoding: ", "gzip,deflate");
			conn.setRequestProperty("SOAPAction: \"urn:", action);
			conn.setRequestProperty("Connection: ", "Keep-Alive");
			OutputStream outputStream = ((HttpURLConnection) conn).getOutputStream();
			outputStream.write(outputStr.getBytes(ENCODING));
			outputStream.close();

			final BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), ENCODING));

			String line;

			while ((line = in.readLine()) != null) {
				stringBuffer.append(line);
				stringBuffer.append("\n");
			}

		} catch (ConnectException ce) {
			System.out.println("The Web Service  connection timed out.");
		} catch (Exception e) {
			System.out.println("The Web Service http  request error: " + e.getMessage());
		} finally {

			if (conn != null && conn instanceof HttpURLConnection) {
				((HttpURLConnection) conn).disconnect();
			}
		}
		return stringBuffer.toString();

	}

	public static void main(String[] args) {
		String url = "http://10.111.221.11:8085/axis2/services/RiskVal.RiskValHttpSoap11Endpoint/";
		String namespace = "http://webservice";
		String action = "sinM2";
		String user = "aain";
		String password = "1s2a3";
		String body = "<basedate>20150105</basedate><code>0000000000000000000000000</code>";
		String resp = soap(url, namespace, user, password, body, action);
		System.out.println(resp);
	}
}
