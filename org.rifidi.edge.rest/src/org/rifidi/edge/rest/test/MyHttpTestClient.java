package org.rifidi.edge.rest.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class MyHttpTestClient {

	private String USER_AGENT = "Mozilla/5.0";

	private String strProtocol = "http";
	private String strHost = "localhost";
	private String strPort = "8111";

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		MyHttpTestClient myHttpTestClient = new MyHttpTestClient();

		String strCommand = "llrpmessage/LLRP_1/1";

		myHttpTestClient.sendPost(strCommand);

	}

	private boolean sendPost(String strCommand) throws FileNotFoundException,
			ClientProtocolException, IOException {

		boolean success = false;
		HttpClient httpclient = new DefaultHttpClient();

		try {

			String strUrl = strProtocol + "://" + strHost + ":" + strPort + "/"
					+ strCommand;

			HttpPost httpPost = new HttpPost(strUrl);

			String strXml = "<llrp:SET_READER_CONFIG xmlns:llrp=\"http://www.llrp.org/ltk/schema/core/encoding/xml/1.0\" Version=\"1\" MessageID=\"0\">"
					+ "<llrp:ResetToFactoryDefault>0</llrp:ResetToFactoryDefault>"
					+ "<llrp:ReaderEventNotificationSpec>"
					+ "<llrp:EventNotificationState>"
					+ "<llrp:EventType>GPI_Event</llrp:EventType>"
					+ "<llrp:NotificationState>1</llrp:NotificationState>"
					+ "</llrp:EventNotificationState>"
					+ "</llrp:ReaderEventNotificationSpec>"
					+ "<llrp:ROReportSpec>"
					+ "<llrp:ROReportTrigger>None</llrp:ROReportTrigger>"
					+ "<llrp:N>0</llrp:N>"
					+ "<llrp:TagReportContentSelector>"
					+ "<llrp:EnableROSpecID>1</llrp:EnableROSpecID>"
					+ "<llrp:EnableSpecIndex>0</llrp:EnableSpecIndex>"
					+ "<llrp:EnableInventoryParameterSpecID>0</llrp:EnableInventoryParameterSpecID>"
					+ "<llrp:EnableAntennaID>1</llrp:EnableAntennaID>"
					+ "<llrp:EnableChannelIndex>0</llrp:EnableChannelIndex>"
					+ "<llrp:EnablePeakRSSI>1</llrp:EnablePeakRSSI>"
					+ "<llrp:EnableFirstSeenTimestamp>1</llrp:EnableFirstSeenTimestamp>"
					+ "<llrp:EnableLastSeenTimestamp>1</llrp:EnableLastSeenTimestamp>"
					+ "<llrp:EnableTagSeenCount>1</llrp:EnableTagSeenCount>"
					+ "<llrp:EnableAccessSpecID>1</llrp:EnableAccessSpecID>"
					+ "<llrp:C1G2EPCMemorySelector>"
					+ "<llrp:EnableCRC>0</llrp:EnableCRC>"
					+ "<llrp:EnablePCBits>0</llrp:EnablePCBits>"
					+ "</llrp:C1G2EPCMemorySelector>"
					+ "</llrp:TagReportContentSelector>"
					+ "</llrp:ROReportSpec>"
					+ "<llrp:AccessReportSpec>"
					+ "<llrp:AccessReportTrigger>Whenever_ROReport_Is_Generated</llrp:AccessReportTrigger>"
					+ "</llrp:AccessReportSpec>"
					+ "<llrp:GPIPortCurrentState>"
					+ "<llrp:GPIPortNum>1</llrp:GPIPortNum>"
					+ "<llrp:Config>1</llrp:Config>"
					+ "<llrp:State>Low</llrp:State>"
					+ "</llrp:GPIPortCurrentState>"
					+ "<llrp:GPIPortCurrentState>"
					+ "<llrp:GPIPortNum>2</llrp:GPIPortNum>"
					+ "<llrp:Config>1</llrp:Config>"
					+ "<llrp:State>Low</llrp:State>"
					+ "</llrp:GPIPortCurrentState>"
					+ "<llrp:GPIPortCurrentState>"
					+ "<llrp:GPIPortNum>3</llrp:GPIPortNum>"
					+ "<llrp:Config>1</llrp:Config>"
					+ "<llrp:State>Low</llrp:State>"
					+ "</llrp:GPIPortCurrentState>"
					+ "<llrp:EventsAndReports>"
					+ "<llrp:HoldEventsAndReportsUponReconnect>0</llrp:HoldEventsAndReportsUponReconnect>"
					+ "</llrp:EventsAndReports>" 
					+ "</llrp:SET_READER_CONFIG>";

			// File file = new File(fileName.trim());

			// InputStream is = new
			// ByteArrayInputStream(strXml.getBytes("UTF-8"));
			InputStream is = new ByteArrayInputStream(strXml.getBytes());

			InputStreamEntity reqEntity = new InputStreamEntity(
			// new FileInputStream(file), -1);
					is, -1);
			reqEntity.setContentType("application/xml");
			reqEntity.setChunked(true);

			httpPost.setEntity(reqEntity);

			System.out
					.println("Executing request " + httpPost.getRequestLine());
			HttpResponse httpResponse = httpclient.execute(httpPost);
			HttpEntity resEntity = httpResponse.getEntity();

			System.out.println("----------------------------------------");
			System.out.println(httpResponse.getStatusLine());
			
			if (httpResponse.getStatusLine().getStatusCode() == 200) {

				success = true;
				
				BufferedReader in = new BufferedReader(new InputStreamReader(
						resEntity.getContent()));
				String inputLine;
				StringBuffer stringBufferResponse = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					stringBufferResponse.append(inputLine);
				}
				in.close();
				
				// print response
				System.out.println(stringBufferResponse.toString());

			} else {
				//TODO
			}
			
			if (resEntity != null) {
				System.out.println("Response content length: "
						+ resEntity.getContentLength());
				System.out.println("Chunked?: " + resEntity.isChunked());
			}

			return success;

		} finally {

			 //httpclient.getConnectionManager().shutdown();
		}

	}

	

	/*
	 * public void processXml(String xml) throws SAXException,
	 * ParserConfigurationException, IOException {
	 * 
	 * // obtain and configure a SAX based parser SAXParserFactory
	 * saxParserFactory = SAXParserFactory.newInstance();
	 * 
	 * // obtain object for SAX parser SAXParser saxParser =
	 * saxParserFactory.newSAXParser();
	 * 
	 * // create the reader (scanner) XMLReader xmlreader =
	 * saxParser.getXMLReader();
	 * 
	 * MyResponseHandler myResponseHandler = new MyResponseHandler();
	 * 
	 * // assign our handler xmlreader.setContentHandler(myResponseHandler);
	 * 
	 * // perform the synchronous parse xmlreader.parse(new InputSource(new
	 * StringReader(xml)));
	 * 
	 * System.out.println("myResponseHandler.getMessageValue(): " +
	 * myResponseHandler.getMessageValue());
	 * 
	 * if (myResponseHandler.getMessageValue().equals(SUCCESS_MESSAGE)) {
	 * 
	 * // Do whatever logic with success outcome } else {
	 * 
	 * // Not a success outcome }
	 * 
	 * }
	 */

}
