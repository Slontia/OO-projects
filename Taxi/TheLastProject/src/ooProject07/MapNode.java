package ooProject07;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

enum Direction {
	/*@Overview:
	 * Direction是一个表示二维方向的枚举类，它的成员包括上、下、左和右。
	 */
	UP, DOWN, LEFT, RIGHT;
	
	public Direction turnCentralRight(){
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: (this == UP) ==> \result == LEFT;
		//			(this == LEFT) ==> \result == DOWN;
		//			(this == DOWN) ==> \result == RIGHT;
		//			(this == RIGHT) ==> \result == UP;
		//			(this == null) ==> \result == null;
		switch (this) {
		case UP: 	return LEFT;
		case LEFT:	return DOWN;
		case DOWN:	return RIGHT; //right
		case RIGHT:	return UP;
		default:	return null;
		}
	}
	
	public Direction turnCentralLeft(){
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: (this == UP) ==> \result == RIGHT;
		//			(this == RIGHT) ==> \result == DOWN;
		//			(this == DOWN) ==> \result == LEFT;
		//			(this == LEFT) ==> \result == UP;
		//			(this == null) ==> \result == null;
		switch (this) {
		case UP: 	return RIGHT;
		case RIGHT:	return DOWN;
		case DOWN:	return LEFT; //left
		case LEFT:	return UP;
		default:	return null;
		}
	}
	
	public Direction turnOpposite(){
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: (this == UP) ==> \result == DOWN;
		//			(this == RIGHT) ==> \result == LEFT;
		//			(this == DOWN) ==> \result == UP;
		//			(this == LEFT) ==> \result == RIGHT;
		//			(this == null) ==> \result == null;
		switch (this) {
		case UP: 	return DOWN;
		case RIGHT:	return LEFT;
		case DOWN:	return UP; 
		case LEFT:	return RIGHT;
		default:	return null;
		}
	}
	
	static public Direction getRandomDirection(Random random){
		//@REQUIRES: random != null;
		//@MODIFIES: None;
		//@EFFECTS: \result == <random direction>;
		int rst = random.nextInt(4);
		switch (rst) {
		case 0:
			return UP;
		case 1:
			return DOWN;
		case 2:
			return LEFT;
		case 3:
			return RIGHT;
		default:
			return null;
		}
	}
}

public class MapNode {
	/*@Overview:
	 * MapNode是地图的一个节点，一个典型的节点是(i,j)。
	 */
	final static private int INFINITE = 200;
	final private int i;
	final private int j;
	final private Map<Direction, MapEdge> toEdge = new EnumMap<Direction, MapEdge>(Direction.class);
	final private LinkedList<CRrequest> CRrequestList = new LinkedList<CRrequest>();
	private TrafficLight trafficLight;
	private boolean trafficLightExists = false;
	
	//不变式	c.i>=1 && c.i<=80 && c.j>=1 && c.j<=80 && c.toEdge!=null && c.CRrequestList!=null;
	public boolean repOK(){
		//@EFFECTS: \result == invariant(this);
		return (i>=1 && i<=80 && j>=1 && j<=80 && toEdge!=null && CRrequestList!=null);
	}
	
	public MapNode(int i, int j) {
		//@REQUIRES: i和j为节点的合法坐标;
		//@MODIFIES: this.i; this.j; this.trafficLight;
		//@EFFECTS: 创建了一个新的MapNode对象并初始化;
		this.i = i;
		this.j = j;
		this.trafficLight = null;
	}
	
	public Direction getEdgeDirection(MapEdge edge){
		//@REQUIRES: edge != null;
		//@MODIFIES: None;
		//@EFFECTS: (toEdge.values().contains(edge)) ==> toEdge.get(\result) == edge;
		//@			(!toEdge.values().contains(edge)) ==> \result == null;
		Direction direction = null;
		for (Direction dir : toEdge.keySet()){
			if (toEdge.get(dir) == edge){
				direction = dir; 
			}
		}
		return direction;
	}
	
	public long taxiPass(Direction localDirection, MapEdge edge){
		//@REQUIRES: toEdge.values().contains(edge) && edge != null;
		//@MODIFIES: None;
		//@EFFECTS: \result == wait time;
		//@THREAD_EFFECTS: \locked(this);
		
		Direction direction = getEdgeDirection(edge);
		assert direction != null;
		if (trafficLightExists == false){
			return 0;
		}else if (direction == localDirection.turnCentralRight()){ // --- turn right
			return 0;
		}else if (direction == localDirection){ // --- back
			return 0;
		}
		long startMill = System.currentTimeMillis();
		synchronized (trafficLight) {
			while (
				trafficLightExists == true &&
				(((direction == Direction.UP || direction == Direction.DOWN) && trafficLight.getDirection() == EdgeDirection.HORIZONAL) ||
				((direction == Direction.RIGHT || direction == Direction.LEFT) && trafficLight.getDirection() == EdgeDirection.VERTICAL))
			){
				try {
					trafficLight.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		}
		return System.currentTimeMillis() - startMill;
	}
	
	public void buildTrafficLight(){
		//@REQUIRES:
		//@MODIFIES: trafficLight;
		//@EFFECTS: trafficLight == a new TrafficLight;
		//@			trafficLightExists <==> true;
		trafficLightExists = true;
		this.trafficLight = new TrafficLight(this);
	}
	
	public void removeTrafficLight(){
		//@REQUIRES:
		//@MODIFIES: trafficLight;
		//@EFFECTS:	(\old(trafficLight) != null) ==> trafficLight.stopFlag == true && trafficLight == null;
		//@			trafficLightExists <==> false;
		//@THREAD_EFFECTS: \locked(trafficLight);
		if (trafficLightExists == false){
			return;
		}
		trafficLightExists = false;
		synchronized (trafficLight) {
			trafficLight.notifyAll();
		}
		trafficLight.stop();
		trafficLight = null;
	}
	
	public void requestCastCR(CRrequest cr){
		//@REQUIRES: cr != null;
		//@MODIFIES: CRrequestList;
		//@EFFECTS: contains(cr);
		//@			(\all CRrequest r; r != cr; contains(r) <==> \old(contains(r)));
		//@			size == \old(size) + 1;
		//@THREAD_EFFECTS: \locked(this);
		synchronized (CRrequestList) {
			this.CRrequestList.add(cr);
		}
	}
	
	public void removeRequestCR(CRrequest cr){
		//@REQUIRES: \old(contains(cr));
		//@MODIFIES: CRrequestList;
		//@EFFECTS: !contains(cr);
		//@			(\all CRrequest r; r != cr; contains(r) <==> \old(contains(r)));
		//@			size == \old(size) - 1;
		//@THREAD_EFFECTS: \locked(this);
		synchronized (CRrequestList) {
			this.CRrequestList.remove(cr);
		}
		
	}
	
	synchronized public boolean isCrossing(){
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result == <this> is a crossing;
		int edgeCount = 0;
		for (MapEdge edge : toEdge.values()){
			if (edge != null && edge.exists()){
				edgeCount ++;
			}
		}
		assert edgeCount >= 1 && edgeCount <= 4;
		if (edgeCount >= 3){
			return true;
		}else {
			return false;
		}
	}
	
	public void setEdge(Direction direction, MapEdge mapEdge){
		//@REQUIRES: direction != null; get(direction) == null; mapEdge.node1 == this || mapEdge.node2 == this;
		//@MODIFIES: toEdge;
		//@EFFECTS: get(direction) == mapEdge;
		this.toEdge.put(direction, mapEdge);
	}
	
	public MapNode getNearbyNode(Direction direction){
		//@REQUIRES: direction != null;
		//@MODIFIES: None;
		//@EFFECTS: [mapEdge] == toEdge.get(direction);
		//@			([mapEdge] == null || !exists) ==> \result == null;
		//@			([mapEdge] != null && exists) ==> \result == otherNode;
		MapEdge mapEdge = this.toEdge.get(direction);
		if (mapEdge != null && mapEdge.exists()){
			return mapEdge.getOtherNode(this);
		}
		return null;
	}
	
	public MapEdge getNearbyExistedEdge(Direction direction){
		//@REQUIRES:
		//@MODIFIES:
		//@EFFECTS: (toEdge.get(direction) != null && existed) ==> \result == toEdge.get(direction);
		//@			(toEdge.get(direction) == null || !existed) ==> \result == null;
		MapEdge mapEdge = this.toEdge.get(direction);
		if (mapEdge != null && mapEdge.hasExisted()){
			return mapEdge;
		}
		return null;
	}
	
	public MapEdge getNearbyExistentEdge(Direction direction){
		//@REQUIRES: 
		//@MODIFIES: None
		//@EFFECTS: (toEdge.get(direction) != null && exists) ==> \result == toEdge.get(direction);
		//@			(toEdge.get(direction) == null || !exists) ==> \result == null;
		MapEdge mapEdge = this.toEdge.get(direction);
		if (mapEdge != null && mapEdge.exists()){
			return mapEdge;
		}
		return null;
	}
	
	public MapEdge getNearbyBestExistedEdgeRandomly(){
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: (isEmpty || (\all MapEdge edge; containsValue(edge); edge==null || !edge.existed)) 
		//@			==> \result == null;
		//@			(!isEmpty && (\exist MapEdge edge; containsValue(edge); edge!=null && edge.existed)) 
		//@			==> \result == ((ran)\min MapEdge edge; containsValue(edge) && edge!=null && edge.existed; edge);
		ArrayList<MapEdge> bestEdges = new ArrayList<MapEdge>();
		int min = INFINITE;
		Map<MapEdge, Integer> flowrates = new HashMap<MapEdge, Integer>();
		for (MapEdge edge : toEdge.values()){
			if (edge != null && edge.hasExisted()){
				flowrates.put(edge, edge.getFlowrate());
			}
		}
		
		for (MapEdge edge : flowrates.keySet()){
			if (flowrates.get(edge) < min){
				min = flowrates.get(edge);
			}
		}
		for (MapEdge edge : flowrates.keySet()){
			if (flowrates.get(edge) == min){
				bestEdges.add(edge);
			}
		}
		if (bestEdges.isEmpty()){
			return null; // --- no edges
		}else {
			int index = Runner.random.nextInt(bestEdges.size());
			return bestEdges.get(index);
		}
	}
	
	public MapEdge getNearbyBestExistentEdgeRandomly(){
		//@REQUIRES:
		//@MODIFIES: None
		//@EFFECTS: (isEmpty || (\all MapEdge edge; containsValue(edge); edge==null || !edge.exists)) 
		//@			==> \result == null;
		//@			(!isEmpty && (\exist MapEdge edge; containsValue(edge); edge!=null && edge.exist)) 
		//@			==> \result == ((ran)\min MapEdge edge; containsValue(edge) && edge!=null && edge.exist; edge);
		ArrayList<MapEdge> bestEdges = new ArrayList<MapEdge>();
		int min = INFINITE;
		Map<MapEdge, Integer> flowrates = new HashMap<MapEdge, Integer>();
		for (MapEdge edge : toEdge.values()){
			if (edge != null && edge.exists()){
				flowrates.put(edge, edge.getFlowrate());
			}
		}
		
		for (MapEdge edge : flowrates.keySet()){
			if (flowrates.get(edge) < min){
				min = flowrates.get(edge);
			}
		}
		for (MapEdge edge : flowrates.keySet()){
			if (flowrates.get(edge) == min){
				bestEdges.add(edge);
			}
		}
		if (bestEdges.isEmpty()){
			return null; // --- no edges
		}else {
			int index = Runner.random.nextInt(bestEdges.size());
			return bestEdges.get(index);
		}
	}
	
	
	
	public int getI() {
		//@REQUIRES: 
		//@MODIFIES: None;
		//@EFFECTS: \result == i;
		return i;
	}
	
	public int getJ() {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result == j;
		return j;
	}
	
	public LinkedList<CRrequest> getCRrequestList() {
		//@REQUIRES:
		//@MODIFIES:
		//@EFFECTS: \result == CRrequestList;
		return CRrequestList;
	}
	
	@Override
	public String toString() {
		//@REQUIRES:
		//@MODIFIES:
		//@EFFECTS: \result.equals("(" + this.i + "," + this.j + ")");
		String string = "(" + this.i + "," + this.j + ")";
		return string;
	}
}