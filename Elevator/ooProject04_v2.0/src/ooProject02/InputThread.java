package ooProject02;

import java.util.Scanner;

public class InputThread extends Thread{
	final static private String RUN = "over";
	final static private int MAX_REQUST_COUNT = 10;
	
	final private RequestQueue rQueue;
	final private long startTimeMill;
	
	Request request;
	double reqTime = 0.0d;
	
	public InputThread(RequestQueue rQueue, long startTimeMill) {
		this.rQueue = rQueue;
		this.startTimeMill = startTimeMill;
	}
	
	@Override
	public void run() {
		Scanner sc = new Scanner(System.in);
		while(true){		
			String str = null;
			try{
				str = sc.nextLine().replace(" ", "");
			}catch(Exception exception){
				ExpHandler.error("Hey! Guess what! We've caught a Ctrl+z!");
			}
			
			String[] reqstr = str.split(";");
			reqTime = (double)(System.currentTimeMillis() - startTimeMill) / 1000; // --- to second
			
			if (str.equals(RUN)){
				synchronized (rQueue) {
					rQueue.over();
				}
				break; // --- BEGIN RUNNING
			}
			
			int requestCount = 0;
			synchronized (rQueue) {
				for (int i = 0; i < reqstr.length; i++){
					request = InstrReader.read(reqstr[i], reqTime);
					if (request == null || requestCount >= MAX_REQUST_COUNT){
						OutputHandler.output(System.currentTimeMillis() + ": INVALID[" + reqstr[i] + "]");
						continue;
					}else {
						try{
							requestCount++;
							rQueue.push(request);
						}catch(Exception e){
							ExpHandler.error("Computation Error!"); 
						}		
					}	
				}
			}
		}
		InfoHandler.printInputInfo("*** OVER ***");
	}
}
