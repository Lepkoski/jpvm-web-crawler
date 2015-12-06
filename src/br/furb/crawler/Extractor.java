package br.furb.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import jpvm.jpvmBuffer;
import jpvm.jpvmEnvironment;
import jpvm.jpvmException;
import jpvm.jpvmMessage;
import jpvm.jpvmTaskId;

public class Extractor {
	private static jpvmEnvironment environment;
	private static jpvmTaskId taskId;

	public static void main(String[] args) throws Throwable {
		
		if (args.length == 0) {
			extractHrefList("http://www.google.com.br");
			
			return;
		}
		
		environment = new jpvmEnvironment();
		taskId = environment.pvm_mytid();

		System.out.println("Task id = " + taskId.toString());

		if (environment.pvm_parent() == jpvmEnvironment.PvmNoParent) {
			throw new Exception("A classe " + Extractor.class.getName() + " não pode ser executada como master");
		} else {
			executar();
		}
	}

	private static void executar() throws jpvmException {
		System.out.println("Inicializado como escravo");

		jpvmMessage message = environment.pvm_recv();
		String str = message.buffer.upkstr();

		System.out.println("Recebeu: " + str);
		System.out.println("Com a tag: " + message.messageTag);
		System.out.println("Origem: " + message.sourceTid.toString());

		System.out.println("Preparando resposta...");
		JSONArray links = new JSONArray();
		links.put("http://www.furb.br");
		links.put("http://www.google.com.br");

		JSONObject response = new JSONObject();
		response.put("links", links);

		jpvmBuffer buffer = new jpvmBuffer();
		// buffer.pack(str + " cliente: " + environment.pvm_mytid().toString());
		buffer.pack(response.toString());

		environment.pvm_send(buffer, environment.pvm_parent(), 0);

		System.out.println("Resposta enviada");
	}

	private static List<String> extractHrefList(String url) {
		List<String> hrefList = new ArrayList<String>();

		loadHtml(url);

		return hrefList;
	}

	@SuppressWarnings({ "deprecation" })
	private static void loadHtml(String url) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		
		try {
			HttpResponse response = httpclient.execute(httpget);
			System.out.println("Login form get: " + response.getStatusLine());
			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
//				EntityUtils.consume(entity);
	            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	            try {
	                DocumentBuilder builder = factory.newDocumentBuilder();
	                Document doc = builder.parse(entity.getContent());
	                NodeList aList = doc.getElementsByTagName("a");
	                
	                for (int i = 0; i < aList.getLength(); i++) {
	                	System.out.println(aList.item(i));
	                }
	                
	            } catch (ParserConfigurationException e) {              
	                e.printStackTrace();
	            } catch (IllegalStateException e) {
	                e.printStackTrace();
	            } catch (SAXException e) {
	                e.printStackTrace();
	            }
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			httpclient.close();
		}
	}
}
