package br.furb.crawler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

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
//			extractHrefList("http://www.google.com.br");
//			
//			return;
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
//		links.put(str);
		List<String> hrefList = extractHrefList(str);
		for (String href : hrefList) {
			links.put(href);
		}

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

		try {
			Document doc = Jsoup.connect(url).get();
			Elements aElements = doc.select("a");
			for (int i = 0; i < aElements.size(); i++) {
				String href = aElements.get(i).attr("href");
				if (href.startsWith("http:")) {
					hrefList.add(href);
				}
			}
		} catch (Throwable t) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			sw.toString(); // stack trace as a string
			
			hrefList.add(sw.toString());
		}

		return hrefList;
	}
}
