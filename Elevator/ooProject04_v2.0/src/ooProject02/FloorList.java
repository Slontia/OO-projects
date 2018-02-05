package ooProject02;

import org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID;

class FloorList {
	final private Floor[] floors;
	final private int minFloor;
	final private int maxFloor;
	
	
	private int getIndex(int floor){
		return (floor - minFloor);
	}
	
	// [Constructed Function]
	public FloorList(int minFloor, int maxFloor) {
		this.maxFloor = maxFloor;
		this.minFloor = minFloor;
		this.floors = new Floor[maxFloor - minFloor + 1];
		for (int i = minFloor; i <= maxFloor; i++){
			floors[getIndex(i)] = new Floor(i);
		}
	}
	
	public boolean repeatedFloorRequestJudge(FloorRequest fRequest){
		Floor f = floors[getIndex(fRequest.getSourceFloor())];
		if (f.getUpWaiting() && fRequest.getDirection().equals(Direction.UP)){
			return true;
		}else if (f.getDownWaiting() && fRequest.getDirection().equals(Direction.DOWN)){
			return true;
		}
		return false;
	}
	
	public boolean isFree(){
		for (int i = 0; i < floors.length; i++){
			if (floors[i].getDownWaiting() || floors[i].getUpWaiting()){
				// there are requests waiting
				return false;
			}
		}
		// no requests waiting
		return true;
	}
	
	public boolean processFloorRequest (FloorRequest fRequest){
		if (fRequest == null){
			return false;
		}else{
			if (this.getWaiting(fRequest.getSourceFloor(), fRequest.getDirection())){
				return false;
			}else {
				return this.floors[getIndex(fRequest.getSourceFloor())].pressDirection(fRequest);
			}
		}
	}
	
	public boolean cancel(int floor, Direction direction){
		return floors[getIndex(floor)].cancelDirection(direction);
	}
	
	public int getTopSourceFloor(){
		int topFL = minFloor - 1;
		for (int f = minFloor; f <= maxFloor; f ++){
			if (floors[getIndex(f)].getUpWaiting() || floors[getIndex(f)].getDownWaiting()){
				topFL = f;
			}
		}
		return topFL;
	}
	
	public int getBottomSourceFloor(){
		int botFL = maxFloor + 1;
		for (int f = maxFloor; f >= minFloor; f --){
			if (floors[getIndex(f)].getDownWaiting() || floors[getIndex(f)].getUpWaiting()){
				botFL = f;
			}
		}
		return botFL;
	}
	
	public boolean getWaiting(int floor, Direction direction){
		if (direction.equals(Direction.DOWN)){
			return floors[getIndex(floor)].getDownWaiting();
		}else if (direction.equals(Direction.UP)){
			return floors[getIndex(floor)].getUpWaiting();
		}else {
			return false;
		}
	}
	
	public boolean getWaiting(int floor){
		return (floors[getIndex(floor)].getDownWaiting() || floors[getIndex(floor)].getUpWaiting());
	}
	
	public FloorRequest getFloorRequest(int floor, Direction direction){
		return (floors[getIndex(floor)].getFloorRequest(direction));
	}
	
	public FloorRequest getEarlyFloorRequet(){
		FloorRequest fRequest = null;
		long minRequestNumber = -1;
		for (int i = 0; i < floors.length; i++){
			if (floors[i].getUpWaiting() && (floors[i].getFloorRequest(Direction.UP).getNumber() < minRequestNumber || minRequestNumber == -1)){
				fRequest = floors[i].getFloorRequest(Direction.UP);
				minRequestNumber = fRequest.getNumber();
			}
			if (floors[i].getDownWaiting() && (floors[i].getFloorRequest(Direction.DOWN).getNumber() < minRequestNumber || minRequestNumber == -1)){
				fRequest = floors[i].getFloorRequest(Direction.DOWN);
				minRequestNumber = fRequest.getNumber();
			}
		}
		return fRequest;
	}
	
	public FloorRequest getEarlyFloorRequet(Request mainRequest){
		FloorRequest fRequest = null;
		long minRequestNumber = -1;
		for (int i = 0; i < floors.length; i++){
			FloorRequest floorRequestUP = floors[i].getFloorRequest(Direction.UP);
			FloorRequest floorRequestDOWN = floors[i].getFloorRequest(Direction.DOWN);
			
			if (floors[i].getUpWaiting() && floorRequestUP.getNumber() != mainRequest.getNumber() && (floorRequestUP.getNumber() < minRequestNumber || minRequestNumber == -1)){
				fRequest = floorRequestUP;
				minRequestNumber = fRequest.getNumber();
			}
			if (floors[i].getDownWaiting() && floorRequestDOWN.getNumber() != mainRequest.getNumber() && (floorRequestDOWN.getNumber() < minRequestNumber || minRequestNumber == -1)){
				fRequest = floorRequestDOWN;
				minRequestNumber = fRequest.getNumber();
			}
		}
		return fRequest;
	}
}