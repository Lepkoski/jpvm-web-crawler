package examples;

import jpvm.jpvmBuffer;
import jpvm.jpvmEnvironment;
import jpvm.jpvmException;
import jpvm.jpvmMessage;
import jpvm.jpvmTaskId;

public class ExemploJpvm {

	static int numWorkers = 4; 
	
    public static void main(String[] args) {
		try {
			jpvmEnvironment environment = new jpvmEnvironment();
			jpvmTaskId myid = environment.pvm_mytid();
			jpvmTaskId masterTaskId = environment.pvm_parent();
			
			System.out.println("Task id = " + myid.toString());
			
			//definir se é o master, ou se é algum escravo.
			if (masterTaskId == jpvmEnvironment.PvmNoParent) {
				executarMaster(environment);
			} else {
				executarSlave(environment, masterTaskId);
			}
			
			environment.pvm_exit();
		} catch (jpvmException e) {
			e.printStackTrace(System.out);
		}
	}

	private static void executarSlave(jpvmEnvironment environment, jpvmTaskId masterTaskId) throws jpvmException {
		System.out.println("Inicializado como escravo");
		
		jpvmMessage message = environment.pvm_recv();
		String str = message.buffer.upkstr();
		
		System.out.println("Recebeu: " + str);
		System.out.println("Com a tag: " + message.messageTag);
		System.out.println("Origem: " + message.sourceTid.toString());
		
		System.out.println("Preparando resposta...");
		jpvmBuffer buf = new jpvmBuffer();
		buf.pack(str + " cliente: " + environment.pvm_mytid().toString());
		environment.pvm_send(buf, masterTaskId, 0);
		
		System.out.println("Resposta enviada");
	}

	private static void executarMaster(jpvmEnvironment environment) throws jpvmException {
		System.out.println("Inicializado como master");
		jpvmTaskId taskIds[] = new jpvmTaskId[numWorkers];
		environment.pvm_spawn(ExemploJpvm.class.getName(), numWorkers, taskIds);
		
		imprimirWorkers(taskIds);
		
		for (int i = 0; i < numWorkers; i++) {
			System.out.println("\nMandando mensagem para o worker " + i);
			
			jpvmBuffer buf = new jpvmBuffer();
			buf.pack("Servidor, id: " + environment.pvm_mytid().toString());
			environment.pvm_send(buf, taskIds[i], i);
			
			System.out.println("Resposta recebida");
			jpvmMessage message = environment.pvm_recv();
			String str = message.buffer.upkstr();
			System.out.println("Recebeu: " + str);
			System.out.println("Com a tag: " + message.messageTag);
			System.out.println("Origem: " + message.sourceTid.toString());
		}
	}

	private static void imprimirWorkers(jpvmTaskId[] taskIds) {
		System.out.println("Workers tasks: ");
		for (int i = 0; i < numWorkers; i++) {
			System.out.println("\t" + taskIds[i].toString());
		}
	}
}
