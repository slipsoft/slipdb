package zArchive.sj.demoSimple;


import java.util.Scanner;

import com.dant.utils.Log;

public class SimpleTCPDemo {
	
	public static void main(String[] args) {
		Log.info("Démarrage du serveur...");
		//SerialStructure.loadStructure();
		Log.info("Entrez quit pour quitter.");
		
		SimpleTCPDemoServRunnable servRunnable = new SimpleTCPDemoServRunnable();
		new Thread(servRunnable).start();
		
		Scanner scanner = new Scanner(System.in);
		/*String readLine = "";
		while(readLine.equals("quit") == false) {
			readLine = scanner.nextLine();
			
		}*/
		servRunnable.hasToStop.set(true);
		scanner.close();
		
	}
	
	
	
	
	
}
