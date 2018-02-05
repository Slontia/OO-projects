package ooProject02;

import java.util.Iterator;

import javax.print.attribute.standard.OutputDeviceAssigned;

public class DispatcherTaking extends Dispatcher{
	protected double doorTime;
	protected Request mainRequest;
	protected int mainGoFloor;
	
	public DispatcherTaking(RequestQueue rQueue, int minFloor, int maxFloor, int elevatorNo) {
		super(rQueue, minFloor, maxFloor, elevatorNo);
		doorTime = -1;
		mainRequest = null;
		mainGoFloor = -1;
	}
	
	public boolean takingJudge(Request request){
		// get main floor
		int mainGoFloor = 0;
		if (mainRequest == null){
			return false;
		}else if (mainRequest.getRequestSource().equals(RequestSource.ELEVATOR)){
			mainGoFloor = ((ElevatorRequest)mainRequest).getTargetFloor();
		}else if (mainRequest.getRequestSource().equals(RequestSource.FLOOR)){
			mainGoFloor = ((FloorRequest)mainRequest).getSourceFloor();
		}else {
			ExpHandler.error("unexpected request source at <takingJudge>");
			return false;
		}
		
		// if at the mainRequest floor -> cannot take
		if (this.elevator.getPosition() == mainGoFloor){
			return false;
		}
		
		// if free
		if (floorList.isFree() && elevator.isFree()){
			return false; // --- no requests waiting
		}
		
		// [ELEVATOR]
		if (request.getRequestSource().equals(RequestSource.ELEVATOR)){
			ElevatorRequest eRequest = (ElevatorRequest)request;
			if (eRequest.getElevatorNo() != elevator.getNo()){
				return false; // --- not the request from this elevator 
			}
			if (elevator.getDirection().equals(Direction.UP)){
				return (eRequest.getTargetFloor() > elevator.getPosition());
			}else if (elevator.getDirection().equals(Direction.DOWN)){
				return (eRequest.getTargetFloor() < elevator.getPosition());
			}else {
				return false; // --- STILL false
			}
		
		// [FLOOR]
		}else if (request.getRequestSource().equals(RequestSource.FLOOR)){
			FloorRequest fRequest = (FloorRequest)request;
			if (fRequest.getDirection().equals(elevator.getDirection())){
				if (elevator.getDirection().equals(Direction.UP)){
					return (fRequest.getSourceFloor() > elevator.getPosition()) && (fRequest.getSourceFloor() <= mainGoFloor);
				}else if (elevator.getDirection().equals(Direction.DOWN)){
					return (fRequest.getSourceFloor() < elevator.getPosition()) && (fRequest.getSourceFloor() >= mainGoFloor);
				}else {
					return false; // --- STILL false
				}
				
			}else {
				return false;
			}
			
		}else {
			return false;
		}
	}
	
	// including filter
	protected void takeRequests() {
		Iterator<Request> rListIterator = requestsList.iterator();  
		while(rListIterator.hasNext()){  
		    Request r = rListIterator.next();  
		    if(takingJudge(r)){
		    	processRequest(r);
		    	rListIterator.remove(); 
		    }
		} 
	}
	
	protected void takeRequests(double exceptiveTime){
		Iterator<Request> rListIterator = requestsList.iterator();  
		while(rListIterator.hasNext()){  
		    Request r = rListIterator.next();
		    if(r.getTime() != exceptiveTime && takingJudge(r)){
		    	processRequest(r);
		    	rListIterator.remove(); 
		    }
		} 
	}
	
	protected void cancel() {
		int floor = this.elevator.getPosition();
		//Direction direction = this.elevator.getDirection();
		
		if (this.elevator.getButtonState(floor)){
			this.elevator.cancel(floor);
		}
		if (this.floorList.getWaiting(floor, Direction.UP)){
			this.floorList.cancel(floor, Direction.UP);
		}	
		if (this.floorList.getWaiting(floor, Direction.DOWN)){
			this.floorList.cancel(floor, Direction.DOWN);
		}
	}
	
	protected void output(){
		int floor = this.elevator.getPosition();
		//Direction direction = this.elevator.getDirection();
		
		if (this.elevator.getButtonState(floor)){
			System.out.println(this.elevator.getElevatorRequest(floor) + "/" + this.elevator);
		}
		if (this.floorList.getWaiting(floor, Direction.UP)){
			System.out.println(this.floorList.getFloorRequest(floor, Direction.UP) + "/" + this.elevator);
		}	
		if (this.floorList.getWaiting(floor, Direction.DOWN)){
			System.out.println(this.floorList.getFloorRequest(floor, Direction.DOWN) + "/" + this.elevator);
		}
	}
	
	public boolean refreshMainRequest(Request mainRequest){
		ElevatorRequest eRequest;
		FloorRequest fRequest;
		if (mainRequest == null){
			eRequest = elevator.getEarlyElevatorRequest();
			fRequest = floorList.getEarlyFloorRequet();
		}else {
			eRequest = elevator.getEarlyElevatorRequest(mainRequest);
			fRequest = floorList.getEarlyFloorRequet(mainRequest);
		}
		
		if (eRequest == null && fRequest == null){
			this.mainRequest = null;
			return false;
			
		}else if (eRequest != null && fRequest == null){
			this.mainRequest = eRequest;
			mainGoFloor = eRequest.getTargetFloor();
			return true;
			
		}else if (eRequest == null && fRequest != null){
			this.mainRequest = fRequest;
			mainGoFloor = fRequest.getSourceFloor();
			return true;
			
		}else {
			if (eRequest.getNumber() < fRequest.getNumber()){
				this.mainRequest = eRequest;
				mainGoFloor = eRequest.getTargetFloor();
			}else {
				this.mainRequest = fRequest;
				mainGoFloor = fRequest.getSourceFloor();
			}
			return true;
		}
	}
	
	@Override
	public void runElevator() {
		if (rQueue.isEmpty()){
			System.out.println("no requests");
			return;
		}else{
			nextTime = 0; // --- first time is 0
			while(tryGettingRequest()); // --- requestsList loads as much as possible
			processRequest(requestsList.remove(0));
			filterRequestsList();
			elevator.directionControl(floorList); // --- decide direction
			refreshMainRequest(this.mainRequest);
			takeRequests();
		}
		
		while (true){
			if (this.elevator.couldStop(floorList) || this.elevator.getPosition() == mainGoFloor){
				this.elevator.stop();
				
				if (this.elevator.getDirection().equals(Direction.UP) || this.elevator.getDirection().equals(Direction.DOWN)){
					output();
				}
				
				//====== TIME REFRESH ======
				time = this.elevator.door(time);
				//==========================
				
				if (this.elevator.getDirection().equals(Direction.STILL)){
					output();
				}
				
				// requestsList loads
				while(tryGettingRequest()); 
				//takeRequests(time);	
				
				// reset buttons
				cancel();
				
				if (isFree()){
					// ========== FREE ==========
					if (requestsList.isEmpty()){
						if (nextTime == -1){ // --- all empty
							break; // --- over
						}else {
							tryTimeLeap();
							while(tryGettingRequest()); // --- load again
						}
					}
					
					processRequest(requestsList.remove(0));	
					filterRequestsList();
					// ===========================
				}
				
				elevator.directionControl(floorList); // --- decide direction	
				refreshMainRequest(this.mainRequest);
				takeRequests();	
				
			}else{
				takeRequests(); // [TEST]
				time = moveElevator(time);
				while(tryGettingRequest());
				//takeRequests();
			}
		}
	}
}