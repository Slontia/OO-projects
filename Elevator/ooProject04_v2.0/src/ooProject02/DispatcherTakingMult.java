package ooProject02;

import java.text.DecimalFormat;

public class DispatcherTakingMult extends DispatcherTaking implements Runnable{
	final private long startTimeMill;
	final private Share share;
	
	public DispatcherTakingMult(int minFloor, int maxFloor, int elevatorNo, long startTimeMill, Share share) {
		super(null, minFloor, maxFloor, elevatorNo); // --- need not rQueue
		this.startTimeMill = startTimeMill;
		this.share = share;
	}
	
	public int getElevatorMotionAccum(){
		return this.elevator.getMotionAccum();
	}
	
	public void setTime(double time) {
		this.time = time;
	}
	
	public double getTime(){
		return this.time;
	}
	
	public void addRequestsList(Request request){
		this.requestsList.add(request);
	}
	
	public int getElevatorNo(){
		return this.elevator.getNo();
	}
	
	@Override
	protected void output() {
		int floor = this.elevator.getPosition();
		//Direction direction = this.elevator.getDirection();
		
		if (this.elevator.getButtonState(floor)){
			OutputHandler.output(System.currentTimeMillis() + ": " + this.elevator.getElevatorRequest(floor) + "/" + this.elevator);
		}
		if (this.floorList.getWaiting(floor, Direction.UP)){
			OutputHandler.output(System.currentTimeMillis() + ": " + this.floorList.getFloorRequest(floor, Direction.UP) + "/" + this.elevator);
		}	
		if (this.floorList.getWaiting(floor, Direction.DOWN)){
			OutputHandler.output(System.currentTimeMillis() + ": " + this.floorList.getFloorRequest(floor, Direction.DOWN) + "/" + this.elevator);
		}
	}
	
	@Override
	protected boolean repeatedRequestJudge(Request request) {
		if (request.getRequestSource().equals(RequestSource.ELEVATOR)){
			return elevator.repeatedElevatorRequestJudge((ElevatorRequest)request);
		}else if (request.getRequestSource().equals(RequestSource.FLOOR)){
			return floorList.repeatedFloorRequestJudge((FloorRequest)request);
		}else {
			ExpHandler.error("unknown request source!");
			return false;
		}
	}
	
	private double elevatorSystemTime(long currentTime){
		return (double)(currentTime - startTimeMill) / 1000.0;
	}
	
	@Override
	public boolean processRequest(Request request) {
		if (request == null){
			return false;
		}
		boolean rst = super.processRequest(request);
		this.refreshMainRequest(null);
		elevator.directionControl(floorList); // --- decide direction
		return rst;
	}

	@Override
	public void run() {
		time = elevatorSystemTime(System.currentTimeMillis());
		while (true){
			while (isFree()){
				synchronized (share) {
					if (share.getIsOver()){
						share.elevatorOver(this);
						return;
					}
					share.freeWait();
				}
				//time = elevatorSystemTime(System.currentTimeMillis());
			}
			elevator.directionControl(floorList); // --- decide direction	
			/**/InfoHandler.printDispatcherInfo(this.getElevatorNo(), "" + mainGoFloor);
			
			if (this.elevator.couldStop(floorList) || this.elevator.getPosition() == mainGoFloor){	
				this.elevator.stop();
				
				if (this.elevator.getDirection().equals(Direction.UP) || this.elevator.getDirection().equals(Direction.DOWN)){
					output();
				}
				time = this.elevator.door(time);
				/**/InfoHandler.printDispatcherInfo(this.getElevatorNo(), "=== DOOR === " + new DecimalFormat("0.0").format(time));
				if (this.elevator.getDirection().equals(Direction.STILL)){
					output();
				}
				
				cancel(); // --- (for repeat judge ABOVE)
				refreshMainRequest(null);
				/**/InfoHandler.printDispatcherInfo(this.getElevatorNo(), "Now MainRequest: " + mainRequest);
				
				synchronized (share) {
					/**/InfoHandler.printDispatcherInfo(this.getElevatorNo(), "getting delayedRequest...");
					share.takeDelayedReq(this); // after refresh mainRequest
				}

			}else {
				time = moveElevator(time);
				/**/InfoHandler.printDispatcherInfo(this.getElevatorNo(), "=== " + this.elevator.getPosition() + " === " + new DecimalFormat("0.0").format(time));
			}
		}
	}
}
