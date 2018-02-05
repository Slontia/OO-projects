package ooProject02;
  
import java.util.Scanner;


public class ElevatorRunner {
	final static private int ELEVATOR_NUMBER = 3;
	final static private int MINFLOOR = 1;
	final static private int MAXFLOOR = 20;
	
	
	public static void main(String[] args) {
		RequestQueue rQueue = new RequestQueue();
		long startTimeMill = System.currentTimeMillis();
		
		OutputHandler.init();
		
		Share share = new Share(ELEVATOR_NUMBER);
		DispatcherTakingMult[] dMults = new DispatcherTakingMult[ELEVATOR_NUMBER];
		DispatcherManager dispatcherManager = new DispatcherManager(ELEVATOR_NUMBER, rQueue, startTimeMill, share, dMults);
		
		Thread managerThread = new Thread(dispatcherManager);
		Thread inputThread = new InputThread(rQueue, startTimeMill);
		Thread[] dispatcherThreads = new Thread[ELEVATOR_NUMBER];
		
		managerThread.start();
		inputThread.start();
		for (int i = 0; i < ELEVATOR_NUMBER; i++){
			dMults[i] = new DispatcherTakingMult(MINFLOOR, MAXFLOOR, i+1, startTimeMill, share);
			dispatcherThreads[i] = new Thread(dMults[i]);
			dispatcherThreads[i].start();
		}
	}
}


enum RequestSource {ELEVATOR, FLOOR}
enum Direction {
	UP, DOWN, STILL;
	
	public Direction reverse(){
		if (this.equals(Direction.UP)){
			return Direction.DOWN;
		}else if (this.equals(Direction.DOWN)){
			return Direction.UP;
		}else {
			return this;
		}
		
	}
}