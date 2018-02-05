package ooProject02;

import java.util.LinkedList;

class RequestQueue {
	final private LinkedList<Request> requestsList;
	private boolean isOver;
	
	// [Constructed Function]
	public RequestQueue() {
		requestsList = new LinkedList<Request>();
		this.isOver = false;
	}
	
	public void push(Request r){
		requestsList.add(r);
		notifyAll();
	}
	
	public Request pop() {
		while (this.isEmpty()){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (this.isOver){
				return null;
			}
		}
		return requestsList.removeFirst();
	}
	
	public void over(){
		this.isOver = true;
		notifyAll();
	}
	
	public boolean getIsOver(){
		return this.isOver;
	}
	
	public double getNearestTime(){
		return requestsList.getFirst().getTime();
	}
	
	public boolean isEmpty(){
		return requestsList.isEmpty();
	}
}