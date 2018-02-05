package ooProject02;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.DefaultStyledDocument.ElementBuffer;

class InstrReader {
	final static private Pattern P_ELEVATOR = Pattern.compile("^\\(ER,\\#(?<elevatorNoSign>[+-]?)(?<elevatorNo>\\d+),(?<targetFloorSign>[+-]?)(?<targetFloor>\\d+)\\)$");
	final static private Pattern P_FLOOR = Pattern.compile("^\\(FR,(?<sourceFloorSign>[+-]?)(?<sourceFloor>\\d+),(?<direction>UP|DOWN)\\)$");
	final static private String RUN = "run";
	final static private int FLOOR_MAX = 20;
	final static private int FLOOR_MIN = 1;
	final static private int ELEVATORNO_MAX = 3;
	final static private int ELEVATORNO_MIN = 1;
	
	
	static public int getFloorMax(){
		return FLOOR_MAX;
	}
	
	static public int getFloorMin(){
		return FLOOR_MIN;
	}
	
	static public Request read(String str, double reqTime){
		Matcher mElevator = P_ELEVATOR.matcher(str);
		Matcher mFloor = P_FLOOR.matcher(str);
		Request request = null;
		
		if (mElevator.matches()){
			request = readElevator(mElevator, reqTime);
		}
		else if (mFloor.matches()){
			request = readFloor(mFloor, reqTime);
		}
		return request;
	}
	
	static private Request readElevator(Matcher m, double reqTime){
		int targetFloor;
		int elevatorNo;
		
		try{
			targetFloor = signNumber(Integer.parseInt(m.group("targetFloor")), m.group("targetFloorSign"));
			elevatorNo = signNumber(Integer.parseInt(m.group("elevatorNo")), m.group("elevatorNoSign"));
		}catch(Exception e){
			//System.out.println("Illegal input.");
			return null;
		}
		
		if (!legalJudgeElevator(targetFloor, elevatorNo)){
			//System.out.println("Illegal input.");
			return null;
		}else{
			return new ElevatorRequest(targetFloor, elevatorNo, reqTime);
		}
	}
	
	static private Request readFloor(Matcher m, double reqTime){
		int sourceFloor;
		Direction direction;
		
		try{
			sourceFloor = signNumber(Integer.parseInt(m.group("sourceFloor")), m.group("sourceFloorSign"));
			if (m.group("direction").equals("UP")){
				direction = Direction.UP;
			}else if (m.group("direction").equals("DOWN")){
				direction = Direction.DOWN;
			}else {
				direction = null;
				//System.out.println("Illegal input.");
				return null;
			}
		}catch(Exception e){
			//System.out.println("Illegal input.");
			return null;
		}
		
		if (!legalJudgeFloor(sourceFloor, direction)){
			//System.out.println("Illegal input.");
			return null;
		}else{
			return new FloorRequest(sourceFloor, direction, reqTime);
		}
	}
	
	static private int signNumber(int value, String sign){
		if(sign.equals("-")){
			return value * (-1);
		}else if (sign.equals("+") || sign.equals("")){
			return value;
		}else{
			return 0;
		}
	}
	
	static private boolean legalJudgeFloor(int sf, Direction direction){
		return (sourceFloorLegalJugde(sf) && directionLegalJudge(sf, direction));
	}
	
	static private boolean legalJudgeElevator(int tf, int en){
		return (targetFloorLegalJudge(tf) && elevatorNoLegalJudge(en));
	}
	
	static private boolean elevatorNoLegalJudge(int en){
		if (en < ELEVATORNO_MIN || en > ELEVATORNO_MAX){
			return false;
		}else{
			return true;
		}
	}
	
	static private boolean targetFloorLegalJudge(int tf){
		if (tf < FLOOR_MIN || tf > FLOOR_MAX){
			return false;
		}else {
			return true;
		}
	}
	
	static private boolean sourceFloorLegalJugde(int sf){
		if (sf < FLOOR_MIN || sf > FLOOR_MAX){
			return false;
		}else {
			return true;
		}
	}
	
	static private boolean directionLegalJudge(int sf, Direction direction){
		if (sf == FLOOR_MIN && direction.equals(Direction.DOWN)){
			return false;
		}else if (sf == FLOOR_MAX && direction.equals(Direction.UP)){
			return false;
		}else{
			return true;
		}
	}
}