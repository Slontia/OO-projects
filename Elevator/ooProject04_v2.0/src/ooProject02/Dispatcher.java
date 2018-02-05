package ooProject02;

import java.util.ArrayList;
import java.util.Iterator;

class Dispatcher {
	final protected int minFloor;
	final protected int maxFloor;
	final protected RequestQueue rQueue;
	final protected ArrayList<Request> requestsList;
	final protected Elevator elevator;
	final protected FloorList floorList;
	
	protected double time;
	protected double nextTime;
	
	
	protected void cancel(boolean upPressed, boolean downPressed){
		Direction eDirection = elevator.getDirection();
		int floor = elevator.getPosition();
		
		// cancel floorList
		if (eDirection.equals(Direction.UP)){
			// --- first cancel the button in the same direction
			if (floorList.getWaiting(floor, Direction.UP)){
				floorList.cancel(floor, Direction.UP);
			}else {
				floorList.cancel(floor, Direction.DOWN);
			}
			
		}else if (eDirection.equals(Direction.DOWN)){
			// --- first cancel the button in the same direction
			if (floorList.getWaiting(floor, Direction.DOWN)){
				floorList.cancel(floor, Direction.DOWN);
			}else {
				floorList.cancel(floor, Direction.UP);
			}
			
		}else if (eDirection.equals(Direction.STILL)){
			// --- first cancel the button first pressed (up first)
			if (upPressed){
				floorList.cancel(floor, Direction.UP);
			}else if (downPressed){
				floorList.cancel(floor, Direction.DOWN);
			}
		}
		
		//cancel elevator
		elevator.cancel(floor);
	}

	protected void filterRequestsList(){
		Iterator<Request> rListIterator = requestsList.iterator();  
		while(rListIterator.hasNext()){  
		    Request r = rListIterator.next();  
		    if(repeatedRequestJudge(r)){
		    	System.out.println("SAME " + r);
		    	rListIterator.remove();  
		    }
		} 
	}
	
	protected double moveElevator(double t){
		// [WARNING] UPPER FIRST
		if (elevator.getDirection().equals(Direction.UP)){
			return elevator.goUp(t);
		}else if (elevator.getDirection().equals(Direction.DOWN)){
			return elevator.goDown(t);
		}else {
			return t;
		}
	}
	
	public boolean tryTimeLeap(){
		if(elevator.isFree() && floorList.isFree()){
			time = nextTime;
			return true;
		}else{
			return false;
		}
	}
	
	// [WARNING] rQueue is used
	protected boolean tryGettingRequest(){
		Request request;
		if (nextTime == -1){ // --- empty rQueue
			return false;
			
		}else if (time >= nextTime){ // --- gettable
			request = rQueue.pop();
			if (!repeatedRequestJudge(request)){
				requestsList.add(request); // --- put into requestsList if not repeated
			}else {
				System.out.println("SAME " + request);
			}
			nextTime = rQueue.isEmpty() ? -1 : rQueue.getNearestTime();
			return true;
			
		}else { // --- not gettable
			return false;
		}
	}
	
	public boolean processRequest(Request request){
		if (request.getRequestSource().equals(RequestSource.ELEVATOR)){
			return elevator.processElevatorRequest((ElevatorRequest)request);
		}else if(request.getRequestSource().equals(RequestSource.FLOOR)){
			return floorList.processFloorRequest((FloorRequest)request);
		}else {
			return false;
		}
	}
	
	protected boolean repeatedRequestJudge(Request request){
		if (request.getRequestSource().equals(RequestSource.ELEVATOR)){
			return elevator.repeatedElevatorRequestJudge((ElevatorRequest)request);
		}else if (request.getRequestSource().equals(RequestSource.FLOOR)){
			return floorList.repeatedFloorRequestJudge((FloorRequest)request);
		}else {
			ExpHandler.error("unknown request source!");
			return false;
		}
	}
	
	// [Construction]
	public Dispatcher(RequestQueue rQueue, int minFloor, int maxFloor, int elevatorNo) {
		this.maxFloor = maxFloor;
		this.minFloor = minFloor;
		this.rQueue = rQueue;
		time = 0;
		requestsList = new ArrayList<Request>();
		elevator = new Elevator(1, this.minFloor, this.maxFloor, elevatorNo);
		floorList = new FloorList(this.minFloor, this.maxFloor);
	}
	
	public boolean isFree(){
		return elevator.isFree() && floorList.isFree();
	}
	
	public void runElevator(){
		if (rQueue.isEmpty()){
			System.out.println("no requests");
			return;
		}else{
			nextTime = 0; // --- first time is 0
			while(tryGettingRequest()); // --- requestsList loads as much as possible
			processRequest(requestsList.remove(0)); // --- the first request
			filterRequestsList(); // --- remove the same requests
			elevator.directionControl(floorList); // --- decide direction
		}
		
		boolean upPressed, downPressed;
		while (true){
			if (elevator.couldStop(floorList)){
				// prepare for cancel floor requests when STAY
				upPressed = floorList.getWaiting(elevator.getPosition(), Direction.UP);
				downPressed = floorList.getWaiting(elevator.getPosition(), Direction.DOWN);
				
				elevator.stop();
				time = elevator.door(time);
				while(tryGettingRequest()); // --- 1.requestsList loads as much as possible
				cancel(upPressed, downPressed); // --- 2.reset buttons
				
				if (requestsList.isEmpty()){
					if (nextTime == -1){ // --- all empty
						break; // --- over
					}else {
						tryTimeLeap();
						while(tryGettingRequest()); // --- load again
					}
				}
				
				processRequest(requestsList.remove(0)); // --- 3.process the first request
				filterRequestsList(); // --- 4.remove the same requests
				elevator.directionControl(floorList); // --- 5.decide direction
				
			}else{
				time = moveElevator(time);
				while(tryGettingRequest());
			}
		}
	}
}