package ooProject02;

import java.text.DecimalFormat;

class Elevator implements ElevatorMovement{
	final static private double MOVE_TIME = 3.0d;
	final static private double DOOR_TIME = 6.0d;
	
	final private int minFloor;
	final private int maxFloor;
	final private boolean[] floorButtonList;
	final private ElevatorRequest[] eRequests;
	final private int no;
	//private long startTimeMill;
	
	private int position;
	private Direction direction = Direction.STILL;
	private int preFloor = 1;
	private double time = 0.0d;
	private int motionAccum = 0;
	//private Request[] stopRequests = null; // the request elevator meets and then elevator stops
	
	
	static public double getMOVETIME(){
		return MOVE_TIME;
	}
	
	static public double getDOORTIME(){
		return DOOR_TIME;
	}
	
	public double door(double time){
		try {
			Thread.sleep((long) (DOOR_TIME * 1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.time = time + DOOR_TIME;
		return (time + DOOR_TIME);
	}
	
	private int getIndex(int floor){
		return (floor - minFloor);
	}
	
	public int getTopTargetFloor(){
		int topTF = minFloor - 1;
		for (int f = minFloor; f <= maxFloor; f ++){
			if (floorButtonList[getIndex(f)]){
				topTF = f;
			}
		}
		return topTF;
	}
	
	public int getBottomTargetFloor(){
		int botTF = maxFloor + 1;
		for (int f = maxFloor; f >= minFloor; f --){
			if (floorButtonList[getIndex(f)]){
				botTF = f;
			}
		}
		return botTF;
	}

	// [Constructed Function]
	public Elevator(int p, int minFloor, int maxFloor, int no) {
		this.maxFloor = maxFloor;
		this.minFloor = minFloor;
		this.position = p;
		this.floorButtonList = new boolean[maxFloor - minFloor + 1];
		this.eRequests = new ElevatorRequest[maxFloor - minFloor + 1];
		this.motionAccum = 0;
		this.no = no;
		//this.startTimeMill = 0;
	}
	
	public void setStartTimeMill(long startTimeMill){
		//this.startTimeMill = startTimeMill;
	}
	
	// move to the upper floor
	public double goUp(double time){
		try {
			Thread.sleep((long) (MOVE_TIME * 1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		position ++ ;
		direction = Direction.UP;
		if (position > maxFloor){
			ExpHandler.error("the elevator breached the top!");
		}
		this.time = time + MOVE_TIME;
		this.motionAccum ++;
		return (time + MOVE_TIME);
	}
	
	// move to the under floor
	public double goDown(double time){
		try {
			Thread.sleep((long) (MOVE_TIME * 1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		position -- ;
		direction = Direction.DOWN;
		if (position < minFloor){
			ExpHandler.error("the elevator breached the bottom!");
		}
		this.time = time + MOVE_TIME;
		this.motionAccum ++;
		return (time + MOVE_TIME);
	}

	public boolean repeatedElevatorRequestJudge(ElevatorRequest eRequest){
		return floorButtonList[getIndex(eRequest.getTargetFloor())];
	}
	
	public boolean isFree(){
		for (int i = minFloor; i <= maxFloor; i++){
			if (floorButtonList[getIndex(i)]){
				return false;
			}
		}
		return true;
	}
	
	public boolean processElevatorRequest (ElevatorRequest eRequest){
		if (eRequest == null){
			return false;
		}else{
			int index = getIndex(eRequest.getTargetFloor());
			if (floorButtonList[index]){
				return false;
			}else{
				floorButtonList[index] = true;
				eRequests[index] = eRequest;
				return true;
			}
		}
	}
	
	public boolean cancel (int floor){
		int index = getIndex(floor);
		floorButtonList[index] = false;
		eRequests[index] = null;
		return true;
	}
	
	public Direction getDirection(){
		return this.direction;
	} 
	
	public int getPosition(){
		return this.position;
	}
	
	public boolean couldStop(FloorList fList){
		if (fList.getWaiting(position, direction) || floorButtonList[getIndex(position)]){
			return true; // --- STAY false
		}else{
			if (direction.equals(Direction.UP)){
				return (position == Math.max(fList.getTopSourceFloor(), getTopTargetFloor()));
			} else if (direction.equals(Direction.DOWN)){
				return (position == Math.min(fList.getBottomSourceFloor(), getBottomTargetFloor()));
			} else if (direction.equals(Direction.STILL)){
				return (fList.getWaiting(position));
			} else {
				return false;
			}
		}
	}
	
	public void setTime(double time){
		this.time = time;
	}
	
	public void stop(){
		if (position > preFloor){
			direction = Direction.UP;
			
		}else if (position < preFloor){
			direction = Direction.DOWN;
			
		}else { // --- position == preFloor
			direction = Direction.STILL;
		}
		
		preFloor = position;
	}
	
	public void directionControl(FloorList fList){
		int topF = Math.max(fList.getTopSourceFloor(), getTopTargetFloor());
		int botF = Math.min(fList.getBottomSourceFloor(), getBottomTargetFloor());
		
		// free
		if (fList.isFree() && isFree()){
			direction = Direction.STILL;
			return;
		}
		
		// requests exist
		if (direction.equals(Direction.UP)){
			if (position == topF){
				direction = Direction.STILL;
			}else if (position > topF) {
				direction = Direction.DOWN;
			}
		}else if (direction.equals(Direction.DOWN)){
			if (position == botF){
				direction = Direction.STILL;
			}else if (position < botF){
				direction = Direction.UP;
			}
		}else if (direction.equals(Direction.STILL)){
			if (position > topF){
				direction = Direction.DOWN;
			}else if (position < botF){
				direction = Direction.UP;
			}else if (position > botF && position < topF){
				direction = Direction.UP; // --- UP first
			}
		}
	}
	
	@Override
	public String toString() {
		return ("(#" + this.no + "," + this.position + "," + this.direction.toString() + "," + this.motionAccum + "," + new DecimalFormat("#.0").format(this.time) + ")");
	}
	
	public String toString(double time) {
		return ("(#" + this.no + "," + this.position + "," + this.direction.toString() + "," + this.motionAccum + "," + new DecimalFormat("#.0").format(time) + ")");
	}
	
	public int getPreFloor(){
		return this.preFloor;
	}
	
	public ElevatorRequest getElevatorRequest(int floor){
		return eRequests[getIndex(floor)];
	}
	
	public boolean getButtonState (int floor){
		return this.floorButtonList[getIndex(floor)];
	}
	
	public ElevatorRequest getEarlyElevatorRequest (){
		ElevatorRequest eRequest = null;
		long minRequestNumber = -1;
		for (int i = 0; i < floorButtonList.length; i++){
			if (floorButtonList[i] && (eRequests[i].getNumber() < minRequestNumber || minRequestNumber == -1)){
				eRequest = eRequests[i];
				minRequestNumber = eRequest.getNumber();
			}
		}
		return eRequest;
	}
	
	public ElevatorRequest getEarlyElevatorRequest (Request mainRequest){
		ElevatorRequest eRequest = null;
		long minRequestNumber = -1;
		for (int i = 0; i < floorButtonList.length; i++){
			if (floorButtonList[i] && eRequests[i].getNumber() != mainRequest.getNumber() && (eRequests[i].getNumber() < minRequestNumber || minRequestNumber == -1)){
				eRequest = eRequests[i];
				minRequestNumber = eRequest.getNumber();
			}
		}
		return eRequest;
	}
	
	public int getMotionAccum(){
		return this.motionAccum;
	}
	
	public int getNo(){
		return this.no;
	}
}