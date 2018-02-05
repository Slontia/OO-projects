package ooProject02;

class Request {
	static private long count = 0;
	
	final private RequestSource rSource;
	final protected double time;
	final private long number;
	
	
	// [Constructed Function]
	public Request(RequestSource rs, double time){
		this.rSource = rs;
		this.time = time;
		this.number = count;
		count ++;
	}
	
	public double getTime(){
		return this.time;
	}
	
	public RequestSource getRequestSource (){
		return this.rSource;
	}
	
	public long getNumber(){
		return this.number;
	}
	
	@Override
	public String toString() {
		if (this.rSource.equals(RequestSource.ELEVATOR)){
			return ((ElevatorRequest)this).toString();
		}else if (this.rSource.equals(RequestSource.FLOOR)){
			return ((FloorRequest)this).toString();
		}else{
			ExpHandler.error("a null or unexpectied request toString");
			return null;
		}
	}
	
	public boolean equals(Request request) {
		if (this.rSource.equals(RequestSource.ELEVATOR)){
			return ((ElevatorRequest)this).equals(request);
		}else if (this.rSource.equals(RequestSource.FLOOR)){
			return ((FloorRequest)this).equals(request);
		}
		return false;
	}
}