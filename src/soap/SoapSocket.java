package soap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class SoapSocket {
	public static String soap() {
		try {
			InetAddress addr = InetAddress.getByName("10.101.101.110");
			Socket socket = new Socket(addr, 8085);
			BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
			wr.write("POST /axis2/services/RiskValue.RiskValueHttpSoap11Endpoint/ HTTP/1.1\r\n");
			wr.write("Accept-Encoding: gzip,deflate\r\n");
			wr.write("Content-Type: text/xml;charset=UTF-8\r\n");
			wr.write("SOAPAction: \"urn:singleM2\"\r\n");
			wr.write("Content-Length: 1062\r\n");
			wr.write("Host: 10.101.221.71:8085\r\n");
			wr.write("Connection: Keep-Alive\r\n");
			wr.write("User-Agent: Socket (java 1.6)\r\n");
			wr.write("\r\n");
			wr.write("<?xml version='1.0' encoding='UTF-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Header><nsl:AuthenticationToken xmlns:nsl=\"http://webservice\"><nsl:Username>aaa</nsl:Username><nsl:Password>123</nsl:Password></nsl:AuthenticationToken></soapenv:Header><soapenv:Body><singleM2 xmlns=\"http://webservice\"><arg0 xmlns=\"\">00001</arg0></singleM2></soapenv:Body></soapenv:Envelope>");

			wr.flush();
			Long l1 = System.currentTimeMillis();
			socket.shutdownOutput();
			BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
			}
			Long l2 = System.currentTimeMillis();
			// System.out.println((l2 - l1) / 1000);
			wr.close();
			rd.close();
			socket.close();

			SAXReader saxReader = new SAXReader();
			Document document;
			try {
				String out = response.toString();
				out = out.substring(out.indexOf("<?xml version"));
				out = out.substring(0, out.length() - 1);
				document = saxReader.read(new ByteArrayInputStream(out.getBytes("UTF-8")));
				Element root = document.getRootElement();

				Element body = root.element("Body");
				return body.getStringValue();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			return response.toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		String resp = soap();
		System.out.println(resp);
	}
}
