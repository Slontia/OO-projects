package ooProject02;

class Floor {
	final private int number;
	
	private boolean upWaiting;
	private boolean downWaiting;
	private FloorRequest upFloorRequest;
	private FloorRequest downFloorRequest;
	
	// [Constructed Function]
	public Floor(int number) {
		this.number = number;
		this.upWaiting = false;
		this.downWaiting = false;
		this.upFloorRequest = null;
		this.downFloorRequest = null;
	}
	  
	public boolean getUpWaiting(){
		return this.upWaiting;
	}
	
	public boolean getDownWaiting(){
		return this.downWaiting;
	}
	
	public boolean pressDirection(FloorRequest floorRequest){
		if (floorRequest.getDirection().equals(Direction.UP)){
			if (this.upWaiting){ // --- if already enabled
				return false;
			}else{
				this.upWaiting = true;
				this.upFloorRequest = floorRequest; // -- store request
				return true;
			}
		}else if (floorRequest.getDirection().equals(Direction.DOWN)){
			if (this.downWaiting){ // --- if already enabled
				return false;
			}else {
				this.downWaiting = true;
				this.downFloorRequest = floorRequest; // -- store request
				return true;
			}
		}else{
			return false; // --- other occasions
		}
	}
	
	public boolean cancelDirection(Direction direction){
		if (direction.equals(Direction.UP)){
			this.upWaiting = false;
			this.upFloorRequest = null;
			return true;
		}else if (direction.equals(Direction.DOWN)){
			this.downWaiting = false;
			this.downFloorRequest = null;
			return true;
		}else{
			return false; // --- other occasions
		}
	}
	
	public FloorRequest getFloorRequest(Direction direction){
		if (direction.equals(Direction.UP)){
			return upFloorRequest;
		}else if (direction.equals(Direction.DOWN)){
			return downFloorRequest;
		}else{
			return null;
		}
	}
}