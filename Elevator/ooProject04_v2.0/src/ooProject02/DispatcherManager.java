package ooProject02;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.IconifyAction;

public class DispatcherManager implements Runnable{
	final private int num;
	final private RequestQueue rQueue;
	final private long startTimeMill;
	
	final private Share share;
	final private DispatcherTakingMult[] dMults;
	
	public DispatcherManager(int n, RequestQueue requestQueue, long startTimeMill, Share share, DispatcherTakingMult[] dMults) {
		this.num = n;
		this.rQueue = requestQueue;
		//this.delayedRequests = new ArrayList<Request>();
		this.startTimeMill = startTimeMill;
		this.share = share;
		this.dMults = dMults;
	}
	
	static private void outputSameRequest(Request request){
		OutputHandler.output(System.currentTimeMillis() + ": SAME" + request);
	}
	
	
	private boolean repeatedRequestJudge(Request request){
		synchronized (share) {
			if(share.repeatedRequestJudge(request)){
				outputSameRequest(request);
				return true;
			}
		}
		
		if (request.getRequestSource().equals(RequestSource.ELEVATOR)){
			for (int i = 0; i < num; i++){
				if (dMults[i].getElevatorNo() == ((ElevatorRequest)request).getElevatorNo()){
					if (dMults[i].repeatedRequestJudge(request)){
						outputSameRequest(request);
						return true;
					}else {
						return false;
					}
				}
			}
			ExpHandler.error("invalid elevatorNo at repeatedRequestJudge in DispatcherManager");
			return false;
			
		}else if (request.getRequestSource().equals(RequestSource.FLOOR)){
			for (int i = 0; i < num; i++){
				if (dMults[i].repeatedRequestJudge(request)){
					outputSameRequest(request);
					return true;
				}
			}
			return false;
			
		}else {
			ExpHandler.error("unknown request source!");
			return false;
		}
	}
	
	static public boolean adoptableJudge(Request request, DispatcherTakingMult dMult){
		return !(request.getRequestSource().equals(RequestSource.ELEVATOR) &&
				dMult.getElevatorNo() != ((ElevatorRequest)request).getElevatorNo());
	}

	
	private boolean adoptRequest(Request request){	
		// to adopt the new request
		int minMotionAccum = -1;
		int adoptingElevatorIndex = -1; // which to adopt
		for (int i = 0; i < num; i++){
			if (!adoptableJudge(request, dMults[i])){
				continue; // not this elevator
			}
			// if can be taken
			if (dMults[i].takingJudge(request)){
				dMults[i].processRequest(request);
				/**/InfoHandler.printDispatcherManagerInfo("taken by Dispatcher " + dMults[i].getElevatorNo());
				return true;
			}else if (dMults[i].isFree() && (dMults[i].getElevatorMotionAccum() < minMotionAccum || minMotionAccum == -1)){
				minMotionAccum = dMults[i].getElevatorMotionAccum();
				adoptingElevatorIndex = i;
			}
		}
		
		if (adoptingElevatorIndex == -1){ // --- no elevators could respond
			synchronized (share) {
				share.pushDelayedReq(request); // --- if cannot respond it, store it
			}
			/**/InfoHandler.printDispatcherManagerInfo("could not be responded -> add to delay list.");
			return false;
			
		}else {
			dMults[adoptingElevatorIndex].processRequest(request);
			dMults[adoptingElevatorIndex].setTime(request.getTime());
			synchronized (share) {
				share.freeNotifyAll();
			}
			/**/InfoHandler.printDispatcherManagerInfo("responded by Dispatcher " + dMults[adoptingElevatorIndex].getElevatorNo());
			return true;
		}
	}
	
	public void run(){
		while (true){
			Request request;
			synchronized (rQueue) {
				InfoHandler.printDispatcherManagerInfo("Getting request...");
				request = rQueue.pop();
				if (rQueue.getIsOver() && rQueue.isEmpty()){
					InfoHandler.printDispatcherManagerInfo("No requests left!");
					break;
				}
				InfoHandler.printDispatcherManagerInfo("Got " + request + " !");
			}
			
			if (!repeatedRequestJudge(request)){
				adoptRequest(request);
			}else {
				InfoHandler.printDispatcherManagerInfo("Repeated request -> REMOVE");
			}
		}
		synchronized (share) {
			share.over();
		}
		InfoHandler.printDispatcherManagerInfo("*** OVER ***");
	}
}
