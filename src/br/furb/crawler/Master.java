package br.furb.crawler;

import java.util.ArrayDeque;
import java.util.Queue;

import org.json.JSONArray;
import org.json.JSONObject;

import jpvm.jpvmBuffer;
import jpvm.jpvmEnvironment;
import jpvm.jpvmException;
import jpvm.jpvmMessage;
import jpvm.jpvmTaskId;

public class Master {
	private static int numWorkers = 4;
	private static jpvmEnvironment environment;
	private static jpvmTaskId taskId; 
	private static Queue<String> urls = new ArrayDeque<String>();
	
    public static void main(String[] args) throws Throwable {
		environment = new jpvmEnvironment();
		taskId = environment.pvm_mytid();
		
		System.out.println("Task id = " + taskId.toString());
		
		if (environment.pvm_parent() == jpvmEnvironment.PvmNoParent) {
			
			urls.add("http://www.google.com.br");
			

			while (urls.size() > 0) {
				executar();
			}
			
			environment.pvm_exit();
			
		} else {
			throw new Exception("A classe " + Master.class.getName() + " não pode ser executada como slave");
		}
	}

	private static void executar() throws jpvmException {
		System.out.println("Inicializado como master");
		jpvmTaskId taskIds[] = new jpvmTaskId[numWorkers];
		environment.pvm_spawn(Extractor.class.getName(), numWorkers, taskIds);
		
		imprimirWorkers(taskIds);
		
		for (int i = 0; i < numWorkers && urls.size() > 0; i++) {
			System.out.println("\nMandando mensagem para o worker #" + i + ": " + taskIds[i].toString());
			
			jpvmBuffer buffer = new jpvmBuffer();
			buffer.pack(urls.poll());
			environment.pvm_send(buffer, taskIds[i], i);
			
			jpvmMessage message = environment.pvm_recv();
			String str = message.buffer.upkstr();
			System.out.println("Resposta recebida");
//			System.out.println("Recebeu: " + str);
//			System.out.println("Com a tag: " + message.messageTag);
//			System.out.println("Origem: " + message.sourceTid.toString());
			
			JSONArray links = new JSONObject(str).getJSONArray("links");
			for (int j = 0; j < links.length(); j++) {
				String link = links.getString(j);
				System.out.println(link);
				urls.add(link);
			}
		}
	}

	private static void imprimirWorkers(jpvmTaskId[] taskIds) {
		System.out.println("Workers tasks: ");
		for (int i = 0; i < numWorkers; i++) {
			System.out.println("\t" + taskIds[i].toString());
		}
	}
}
