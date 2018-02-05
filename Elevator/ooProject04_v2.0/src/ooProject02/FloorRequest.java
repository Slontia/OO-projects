package ooProject02;

import java.text.DecimalFormat;

class FloorRequest extends Request{
	final private int sourceFloor;
	final private Direction dir;
	
	
	// [Constructed Function]
	public FloorRequest(int sf, Direction dir, double time) {
		super(RequestSource.FLOOR, time);
		this.sourceFloor = sf;
		this.dir = dir;
	}

	public int getSourceFloor(){
		return this.sourceFloor;
	}
	
	public Direction getDirection(){
		return this.dir;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return ("[FR," + this.sourceFloor + "," + this.dir.toString() + "," + new DecimalFormat("0.0").format(this.time) + "]");
	}
	
	public boolean equals(Request request) {
		if (request.getRequestSource().equals(RequestSource.FLOOR)){
			FloorRequest floorRequest = (FloorRequest)request;
			return (this.sourceFloor == floorRequest.getSourceFloor() &&
					this.dir.equals(floorRequest.getDirection()));
		}else {
			return false;
		}
	}
}