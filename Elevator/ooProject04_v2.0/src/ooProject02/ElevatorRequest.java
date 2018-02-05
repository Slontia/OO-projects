package ooProject02;

import java.text.DecimalFormat;

class ElevatorRequest extends Request{
	final private int targetFloor;
	final private int elevatorNo;
	
	// [Constructed Function]
	public ElevatorRequest(int targetFloor, int elevatorNo, double time) {
		super(RequestSource.ELEVATOR, time);
		this.targetFloor = targetFloor;
		this.elevatorNo = elevatorNo;
	}
	
	public int getTargetFloor(){
		return this.targetFloor;
	}
	
	public int getElevatorNo(){
		return this.elevatorNo;
	}
	
	@Override
	public String toString() {
		return ("[ER,#" + this.elevatorNo + "," + this.targetFloor + "," + new DecimalFormat("0.0").format(time) + "]");
	}
	
	public boolean equals(Request request) {
		// TODO Auto-generated method stub
		if (request.getRequestSource().equals(RequestSource.ELEVATOR)){
			ElevatorRequest elevatorRequest = (ElevatorRequest)request;
			return (this.targetFloor == elevatorRequest.getTargetFloor() &&
					this.elevatorNo == elevatorRequest.getElevatorNo());
		}else {
			return false;
		}
	}
}