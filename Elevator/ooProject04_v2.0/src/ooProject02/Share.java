package ooProject02;

import java.util.Iterator;
import java.util.LinkedList;

public class Share {
	final LinkedList<Request> delayedRequestList;
	final int elevatorNum;
	
	boolean isOver;
	int overCount;
	
	public Share(int num) {
		this.isOver = false;
		this.delayedRequestList = new LinkedList<Request>();
		this.overCount = 0;
		this.elevatorNum = num;
	}
	
	public void freeWait(){
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void freeNotifyAll(){
		notifyAll();
	}
	
	public void pushDelayedReq(Request delayedRequest){
		this.delayedRequestList.add(delayedRequest);
	}
	
	public Request popDelayedReq(){
		if (delayedRequestList.isEmpty()){
			return null;
		}else {
			return delayedRequestList.removeFirst();
		}
	}
	
	public void takeDelayedReq(DispatcherTakingMult dMult){
		Iterator<Request> rListIterator = delayedRequestList.iterator();  
		while(rListIterator.hasNext()){  
		    Request r = rListIterator.next();
		    // repeat judgment has been done in <DispatcherManager>
		    if (!DispatcherManager.adoptableJudge(r, dMult)){
		    	continue; // not this elevator
		    }
		    if (dMult.isFree() || dMult.takingJudge(r)){
		    	dMult.processRequest(r);
		    	rListIterator.remove();
		    	dMult.refreshMainRequest(null);
		    }
		} 
	}
	
	public boolean isEmpty(){
		return this.delayedRequestList.isEmpty();
	}

	
	public void over(){
		this.isOver = true;
		notifyAll();
	}
	
	public boolean getIsOver(){
		return this.isOver;
	}
	
	public boolean repeatedRequestJudge(Request request){
		for (int i = 0; i < delayedRequestList.size(); i++){
			if (delayedRequestList.get(i).equals(request)){
				return true;
			}
		}
		return false;
	}
	
	public void elevatorOver(DispatcherTakingMult dMult){
		this.overCount ++;
		InfoHandler.printDispatcherInfo(dMult.getElevatorNo(), "*** OVER (" + this.overCount + " of " + this.elevatorNum + ") ***");
		if (this.overCount == this.elevatorNum){
			System.out.println("ALL FINISHED");
			OutputHandler.close();
		}
	}
}
