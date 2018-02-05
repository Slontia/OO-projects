package ooProject07;

import java.rmi.activation.ActivateFailedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

interface Request{
}

public class CRrequest implements Request{
	/*@Overview: 
	 * CRrequest是一个CR请求。
	 */
	final static long KEEP_TIMEMILL = 3000;
	final static int MIN_CREDIT = -1;
	final static int INDIFINATE_DISTANCE = 10000;
	final private TaxiMap taxiMap;
	final private OutputHandle outputHandle;
	private int no = -1;
	private MapNode startingNode;
	private MapNode terminalNode;
	final private Map<Integer, Taxi> grappingTaxis = new HashMap<Integer, Taxi>();
	private long activatingTimeMill;
	
	//不变式	c.taxiMap!=null && c.outputHandle!=null && c.startingNode!=null &&
	//		c.terminalNode!=null && c.activatingTimeMill>0 && c.no>=-1 &&
	//		c.grappingTaxis != null;
	public boolean repOK(){
		//@EFFECTS: \result == invariant(this);
		return (taxiMap!=null && outputHandle!=null && startingNode!=null &&
				terminalNode!=null && activatingTimeMill>0 && no>=-1 &&
				grappingTaxis != null);
	}
	
	public CRrequest(long activatingTimeMill, MapNode startingNode, MapNode terminalNode, TaxiMap taxiMap, OutputHandle outputHandle) {
		//@REQUIRES: 所有参数一律不为null; activatingTimeMill >= 0;
		//@MODIFIES: this.startingNode; this.terminalNode; this.taxiMap; this.outputHandle; this.activatingTimeMill;
		//@EFFECTS: 创建了一个新的CRrequest对象并初始化;
		this.startingNode = startingNode;
		this.terminalNode = terminalNode;
		this.taxiMap = taxiMap;
		this.outputHandle = outputHandle;
		this.activatingTimeMill = activatingTimeMill;
	}
	
	public void activate() {
		//@REQUIRES: this != null;
		//@MODIFIES: startingNode;
		//@EFFECTS: <this> is added into startingNode.CRrequestList;
		this.startingNode.requestCastCR(this);
	}
	
	public void kill() {
		//@REQUIRES: this != null;
		//@MODIFIES: <the most suitable taxi> (if exists) in grappingTaxis.values(); startingNode;
		//@EFFECTS: (\exist Taxi taxi; grappingTaxis.containsValue(taxi); taxiState == WAITING)
		//@			==> <this> is added into <the most suitable taxi>;
		//@EFFECTS: (\exist Taxi taxi; grappingTaxis.containsValue(taxi); taxiState == WAITING)
		//@			==> <the most suitable taxi>.taxiState == TO_PASSENGER;		
		//@EFFECTS: <this> is removed from startingNode
		//@THREAD_EFFECTS: \locked(this);
		synchronized (this) {
			select();
		}
		synchronized (this.startingNode.getCRrequestList()) {
			synchronized (this) {
				this.startingNode.removeRequestCR(this);
			}
		}
	}
	
	synchronized public void grab(Taxi taxi) {
		//@REQUIRES: taxi != null && !\old(contains(taxi));
		//@MODIFIES: grappingTaxis;
		//@EFFECTS: contains(taxi);
		//@EFFECTS: (\all Taxi t; t != taxi; contains(t) <==> \old(contains(t)));
		//@EFFECTS: size == \old(size) + 1;
		//@THREAD_EFFECTS: \locked();
		this.grappingTaxis.put(taxi.getNo(), taxi);
	}
	

	private void select() {
		//@REQUIRES:
		//@MODIFIES: <the most suitable taxi> (if exists) in grappingTaxis.values();
		//@EFFECTS: (\exist Taxi taxi; grappingTaxis.containsValue(taxi); taxiState == WAITING)
		//@			==> <this> is added into <the most suitable taxi>;
		//@EFFECTS: (\exist Taxi taxi; grappingTaxis.containsValue(taxi); taxiState == WAITING)
		//@			==> <the most suitable taxi>.taxiState == TO_PASSENGER;
		Taxi selectedTaxi = null;
		int selectedCredit = MIN_CREDIT;
		int selectedDistance = INDIFINATE_DISTANCE;
		outputHandle.writeLine(this, "<< All Grabbing Taxis >>");
		for (Taxi taxi : grappingTaxis.values()){
			outputHandle.writeLine(this, taxi + ""); // --- record grabbing taxis (no)
			if (taxi.getTaxiState() != TaxiState.WAITING){
				continue;
			}
			if (taxi.getCredit() > selectedCredit){
				selectedTaxi = taxi;
				selectedCredit = taxi.getCredit();
				selectedDistance = new SearchMap(taxiMap).searchWay(startingNode, taxi.getPositionNode()).getDistance();
			}else if (taxi.getCredit() == selectedCredit){
				int distance = new SearchMap(taxiMap).searchWay(startingNode, taxi.getPositionNode()).getDistance();
				if (distance < selectedDistance){
					selectedTaxi = taxi;
					selectedCredit = taxi.getCredit();
					selectedDistance = distance;
				}
			}
		}
		outputHandle.writeLine(this, "<< Taxi Selected >>");
		if (selectedTaxi == null){ // --- not found
			InfoHandle.info("No taxis respond.");
			outputHandle.writeLine(this, "No taxis respond.");
			outputHandle.saveToFile(this);
		}else {
			selectedTaxi.acceptCR(this);
			outputHandle.writeLine(this, selectedTaxi.getNo() + ""); // --- record taxi selected (no)
			selectedTaxi.setState(TaxiState.TO_PASSENGER);
		}
	}
	
	synchronized public boolean hasGrabbed(Taxi taxi) {
		//@REQUIRES: taxi != null;
		//@MODIFIES: None;
		//@EFFECTS: \result <==> grappingTaxis.containsKey(taxi.getNo());
		//@THREAD_EFFECTS: \locked();
		return this.grappingTaxis.containsKey(taxi.getNo());
	}
	
	public void setNo(int no) {
		//@REQUIRES:
		//@MODIFIRS: this.no
		//@EFFECTS: this.no == no;
		this.no = no;
	}
	
	public int getNo() {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result == no;
		return no;
	}
	
	public MapNode getStartingNode() {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result == startingNode;
		return startingNode;
	}
	
	public MapNode getTerminalNode() {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result == terminalNode;
		return terminalNode;
	}
	
	public long getActivatingTimeMill() {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result == activatingTimeMill;
		return activatingTimeMill;
	}
	
	public boolean equals(CRrequest obj) {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: (obj == null) ==> \result <==> false;
		//@EFFECTS: (obj != null) ==> \result <==> (this.startingNode == obj.startingNode && this.terminalNode == obj.terminalNode);
		if (obj == null){
			return false;
		}
		return (this.startingNode == obj.startingNode && this.terminalNode == obj.terminalNode);
	}
	
	@Override
	public String toString() {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result.equals("[CR," + this.startingNode + "," + this.terminalNode + "]");
		return "[CR," + this.startingNode + "," + this.terminalNode + "]";
	}
}
