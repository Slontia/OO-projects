package ooProject02;

interface ElevatorMovement {

	public double door(double time);
	
	public void directionControl(FloorList fList);

	public void stop();

	public boolean couldStop(FloorList fList);

	public double goDown(double time);

	public double goUp(double time);
	
}