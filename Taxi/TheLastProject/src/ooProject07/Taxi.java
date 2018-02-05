package ooProject07;

import java.awt.Point;
import java.sql.Time;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import org.omg.IOP.Codec;

enum TaxiState {
	/*@Overview:
	 * TaxiState是表示出租车状态的枚举类，其成员包括停止、服务、等待服务和准备服务四种状态。
	 */
	STOPPING(0), SERVING(1), WAITING(2), TO_PASSENGER(3);
	private int code;
	
	//不变式	c.code>=0 && c.code<=3;
	public boolean repOK(){
		return (code>=0 && code<=3);
	}
	
	private TaxiState(int code) {
		//@REQUIRES: 
		//@MODIFIES: this.code;
		//@EFFECTS: this.code == code;
		this.code = code;
	}
	
	public int getCode(){
		//@REQUIRES:
		//@MODIFEIS: None;
		//@EFFECTS: \result == code;
		return this.code;
	}
}

public class Taxi extends Thread{
	/*@Overview:
	 * Taxi是一辆出租车，每辆出租车有着不同的编号no。
	 */
	final static protected int MOVE_TIMEMILL = 200;
	final static protected int MAX_WAITING_TIMEMILL = 20000;
	final static protected int STOPPING_TIMEMILL = 1000;
	final static protected int ACCEPT_SIZE = 2;
	final static protected int GRAB_CREDIT = 1;
	final static protected int SERVE_CREDIT = 3;
	final protected Share share;
	final protected Random random;
	final protected TaxiGUI taxiGUI;
	final protected long startTimeMill;
	final protected LinkedList<CRrequest> crList= new LinkedList<CRrequest>();
	final protected TaxiMap taxiMap;
	final protected OutputHandle outputHandle;
	protected int no; 								// [info]
	protected MapNode positionNode; 					// [info]
	protected TaxiState state = TaxiState.WAITING; 	// [info]
	protected int credit = 0; 						// [info]
	protected MapNode terminalNode = null;
	protected MapWay way = null;
	protected long timeMill;
	protected long keepWaitingTimeMill = 0;
	protected CRrequest crTask = null;
	protected boolean toChangeWay = false;
	protected Direction localDirection = Direction.getRandomDirection(Runner.random);
	
	//不变式	c.share!=null && c.random!=null && c.taxiGUI!=null && c.startTimeMill>=0 && c.crList!=null &&
	//		c.taxiMap!=null && c.outputHandle!=null && c.no>=0 && c.positionNode!=null && c.state!=null &&
	//		c.credit>=0 && c.timeMill>=0 && c.keepWaitingTimeMill>=0 && c.localDirection!=null;
	public boolean repOK(){
		//@EFFECTS: \result == invariant(this);
		return (share!=null && random!=null && taxiGUI!=null && startTimeMill>=0 && crList!=null &&
				taxiMap!=null && outputHandle!=null && no>=0 && positionNode!=null && state!=null &&
				credit>=0 && timeMill>=0 && keepWaitingTimeMill>=0 && localDirection!=null);
	}
	
	public Taxi(int no, TaxiMap taxiMap, long timeMill, Share share, Random random, TaxiGUI taxiGUI, OutputHandle outputHandle) {
		//@REQUIRES: 所有参数一律不为null; 0 <= no < 100; timeMill >= 0;
		//@MODIFIES: this.share; this.taxiMap; this.sysMill; this.startingTimeMill;
		//@EFFECTS: 创建了一个新的Taxi对象并初始化;
		this.no = no;
		this.taxiMap = taxiMap;
		this.random = random;
		int positionI = random.nextInt(TaxiMap.Y) + 1;
		int positionJ = random.nextInt(TaxiMap.X) + 1;
		this.positionNode = this.taxiMap.getNode(positionI, positionJ);
		if (this.positionNode == null){
			System.out.println(positionI + " " + positionJ);
		}
		this.startTimeMill = timeMill;
		this.share = share;
		this.taxiGUI = taxiGUI;
		this.outputHandle = outputHandle;
		//this.timeLock = timeLock;
		this.timeMill = 0;
	}
	
	public void acceptCR(CRrequest cr){
		//@REQUIRES: cr != null
		//@MODIFIES: crList
		//@EFFECTS: contains(cr);
		//@			(\all CRrequest r; r != cr; contains(r) <==> \old(contains(r)));
		//@			size == \old(size) + 1;
		this.crList.add(cr);
	}
	
	protected void searchWay(){
		//@REQUIRES: 
		//@MODIFIES: way;
		//@EFFECTS: (terminalNode != null) ==> way == the shortest way from positionNode to terminalNode;
		if (this.terminalNode == null){
			return;
		}
		this.way = new SearchMap(taxiMap).searchWay(this.terminalNode, this.positionNode);
	}
	
	protected void randomMove(){
		//@REQUIRES: 
		//@MODIFIES: position; credit; localDirection; timeMill;
		//@EFFECTS: localDirection == positionNode.getEdgeDirection(nextEdge).turnOpposite();
		//@			timeMill is increased;
		//@			move to a random edge;
		MapEdge nextEdge = null;
		do {
			nextEdge = this.positionNode.getNearbyBestExistentEdgeRandomly();
			assert nextEdge != null;
			this.timeMill += this.positionNode.taxiPass(localDirection, nextEdge);
			this.localDirection = this.positionNode.getEdgeDirection(nextEdge).turnOpposite();
		} while (!nextEdge.exists());
		moveToEdge(nextEdge);
	}
	
	private void wayMove(){
		//@REQUIRES: 
		//@MODIFIES: position; way; outputHandle; toChangeWay; credit; localDirection; timeMill;
		//@EFFECTS:	position information is recorded in outputHandle;
		//@			\old(toChangeWay) ==> a new way got && !toChangeWay;
		//@			localDirection == positionNode.getEdgeDirection(nextEdge).turnOpposite();
		//@			nextEdge of way is changed;
		//@			timeMill is increased;
		//@			move to \old(nextEdge);
		MapEdge nextEdge = null;
		do{
			if (toChangeWay){
				searchWay();
				this.toChangeWay = false;
			}
			nextEdge = this.way.getNextEdge();
			assert nextEdge != null;
			this.timeMill += this.positionNode.taxiPass(localDirection, nextEdge);
			this.localDirection = this.positionNode.getEdgeDirection(nextEdge).turnOpposite();
		}while(toChangeWay);
		moveToEdge(nextEdge);
		outputHandle.writeLine(this.crTask, this.positionNode + ""); // --- record location
	}
	
	protected void moveToEdge(MapEdge edge){
		//@REQUIRES: edge != null && edge connects with position;
		//@MODIFIES: position; nodes; taxiGUI; edge; credit; timeLock; positionEdge;
		//@EFFECTS: CRrequests nearby are grabbed;
		//@			taxiGUI refreshed;
		//@			position == edge.otherNode;
		//@			edge is added into timeLock.passedEdge;
		//@			wait some time;
		//@			运行时positionEdge会先变edge再变回null;
		long sleepMill = this.timeMill + MOVE_TIMEMILL - (System.currentTimeMillis() - this.startTimeMill);
		edge.taxiEntry(sleepMill);
		try {
			sleep(sleepMill);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			//InfoHandle.error("Taxi " + this.no + " timeout: " + sleepMill);
		}
		edge.taxiExit();
		this.timeMill += MOVE_TIMEMILL;
		grabSingle(); // --- grab (old position)
		synchronized (this) {
			this.positionNode = edge.getOtherNode(this.positionNode); // --- change the position
		}
		assert this.positionNode != null;
		drawGUI();
		grabSingle(); // --- grab (new position)
	}
	
	public void changeWay(){
		//@REQUIRES:
		//@MODIFIES: toChangeWay;
		//@EFFECTS: toChangeWay <==> true;
		this.toChangeWay = true;
	}
	
	protected void drawGUI(){
		//@REQUIRES: 
		//@MODIIFES: taxiGUI;
		//@EFFECTS:	taxiStatus is drawn on the canvas;
		//@			\old(taxiStatus) is cleaned on the canvas;
		synchronized (taxiGUI) {
			try{
				taxiGUI.SetTaxiStatus(this.no, new Point(this.positionNode.getI()-1, this.positionNode.getJ()-1), this.state.getCode());
			}catch(Exception exception){
				System.out.println(this.state);
			}
		}
	}
	
	private void stopAndTurnTo(TaxiState taxiState){
		//@REQUIRES: taxiState != null;
		//@MODIFIES: keepWaitingTimeMill; state; taxiGUI; nodes; credit; timeLock;
		//@EFFECTS:	state == taxiState;
		//@			wait for some time;
		//@			taxiGUI is refreshed;
		//@			keepWaitingTimeMill == 0;
		//@			CRrequests nearby are grabbed;
		this.keepWaitingTimeMill = 0;
		synchronized (this) {
			this.state = TaxiState.STOPPING;
		}
		drawGUI();
		long sleepMill = this.timeMill + STOPPING_TIMEMILL - (System.currentTimeMillis() - this.startTimeMill);
		try {
			sleep(sleepMill);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			//InfoHandle.error("Taxi " + this.no + " timeout: " + sleepMill);
		}
		this.timeMill += STOPPING_TIMEMILL;
		synchronized (this) {
			this.state = taxiState;
		}
		grabSingle();
	}
	
	protected void grabSingle(){
		//@REQUIRES:
		//@MODIFIES: nodes; credit;
		//@EFFECTS: credit == \old(credit) + 1;
		//@			(\all CRrequest r; (\exist MapNode n; areaNode.contains(n) && n.CRrequestList.contains(r)); 
		//@			r.hasGrabbed(this));
		//@THREAD_EFFECTS: \locked(this);
		if (this.state != TaxiState.WAITING){
			return; // --- only in WAITING can taxi grab
		}
		ArrayList<MapNode> nodes = taxiMap.getAreaNodes(this.positionNode, ACCEPT_SIZE);
		for (MapNode node : nodes){
			synchronized (node.getCRrequestList()) {
				for (CRrequest cr : node.getCRrequestList()){
					if (!cr.hasGrabbed(this)){
						cr.grab(this);
						addCredit(GRAB_CREDIT); // --- add credit 1
					}
				}
			}
		}
	}
	
	public boolean wayContainsEdge(MapEdge edge){
		//@REQUIRES: taxiMap.contains(edge);
		//@MODIFIES: None;
		//@EFFECTS: (way != null) ==> \result <==> way.contains(edge);
		//			(way == null) ==> \result <==> false;
		if (this.way == null){
			return false;
		}else {
			return this.way.containsEdge(edge);
		}
	}
	
	protected void saveToTaxi(){
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS:
	}
	
	public TwoWayIterator<String> twoWayIterator(){
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result == null;
		return null;
	}
	
	@Override
	public void run() {
		//@REQUIRES:
		//@MODIFIES: nearly all properties;
		//@EFFECTS:	this function cannot stop and nearly all properties keep changing;
		while(true){
			switch (state) {
			case WAITING:
				if (this.state == TaxiState.WAITING && this.keepWaitingTimeMill >= MAX_WAITING_TIMEMILL){
					stopAndTurnTo(TaxiState.WAITING);
				}
				
				randomMove(); // --- move(sleep and grab)
				if (this.state == TaxiState.TO_PASSENGER){
					if (this.crTask == null && !this.crList.isEmpty()){
						this.crTask = this.crList.removeFirst();
						this.terminalNode = this.crTask.getStartingNode();
						searchWay();
						outputHandle.writeLine(this.crTask, "<< Taking Passenger >>");
						outputHandle.writeLine(this.crTask, this.positionNode + ""); // --- record location
						synchronized (this) {
							this.state = TaxiState.TO_PASSENGER;
						}
						continue;
					}else {
						assert false;
					}
				}
				keepWaitingTimeMill += MOVE_TIMEMILL;
				break;
				
			case STOPPING:
				assert false : "How do you get there?!";
				break;
			
			case TO_PASSENGER:
				if (this.positionNode == this.terminalNode){
					this.terminalNode = this.crTask.getTerminalNode();
					searchWay();
					outputHandle.writeLine(this.crTask, "<< Serving >>");
					stopAndTurnTo(TaxiState.SERVING); // --- find the passenger
				}else {
					wayMove();
				}
				break;
				
			case SERVING:
				if (this.positionNode == this.terminalNode){
					this.terminalNode = null;
					saveToTaxi();
					outputHandle.saveToFile(this.crTask);
					this.crTask = null;
					addCredit(SERVE_CREDIT); // --- add credit 3	
					stopAndTurnTo(TaxiState.WAITING);
					keepWaitingTimeMill = 0;
				}else {
					wayMove();
				}
				break;
				
			default:
				assert false : "Invalid state.";
				break;
			}
		}
	}
	
	public int getNo() {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result == no;
		return no;
	}
	
	synchronized public int getCredit() {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result == credit;
		//@THREAD_EFFECTS: \locked();
		return credit;
	}
	
	synchronized public void addCredit(int credit) {
		//@REQUIRES: credit >= 0;
		//@MODIFIES: this.credit;
		//@EFFECTS: this.credit == \old(this.credit) + credit;
		//@THREAD_EFFECTS: \locked();
		this.credit += credit;
	}
	
	synchronized public MapNode getPositionNode() {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS:	\result == positionNode;
		//@THREAD_EFFECTS: \locked();
		return positionNode;
	}
	
	synchronized public TaxiState getTaxiState() {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result == state;
		//@THREAD_EFFECTS: \locked();
		return this.state;
	}
	
	synchronized public void setState(TaxiState state) {
		//@REQUIRES: state != null;
		//@MODIFIES: this.state;
		//@EFFECTS: this.state == state;
		//@THREAD_EFFECTS: \locked();
		this.state = state;
	}
	
	@Override
	synchronized public String toString() {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS:	(this != null) ==> \result == taxi information;
		//@			(this == null) ==> \result == null;
		//@THREAD_EFFECTS: \locked();
		String string = "";
		string = string + "[Taxi " + this.no + "]\n";
		string = string + "Location: " + this.positionNode + "\n";
		string = string + "State: " + this.state + "\n";
		string = string + "Credit: " + this.credit;
		return string;
	}
}
