package ooProject07;

import java.awt.Point;
import java.lang.invoke.SwitchPoint;
import java.util.LinkedList;
import java.util.NoSuchElementException;

enum EdgeDirection{
	/*@Overview:
	 * EdgeDirection是一个表示二维方向的枚举类，它的成员包括水平和垂直。
	 */
	HORIZONAL, VERTICAL;
	
	public EdgeDirection changeDirection(){
		//@REQUIRES: this != null;
		//@MODIFIES: None;
		//@EFFECTS: (this == HORIZONAL) ==> \result == VERTICAL;
		//@			(this == VERTICAL) ==> \result == HORIZONAL;
		if (this == HORIZONAL){
			return VERTICAL;
		}else {
			return HORIZONAL;
		}
	}
}

public class MapEdge implements Runnable{
	/*@Overview:
	 * MapEdge是地图的一个边。
	 */
	static final private long REFRESH_MILL = 200;
	final private MapNode node1;
	final private MapNode node2;
	final private EdgeDirection edgeDirection;
	private boolean exist = false;
	private boolean existed = false;
	LinkedList<Long> flowrateActiveMills = new LinkedList<Long>();
	private int stayingTaxisCount = 0;
	
	//不变式	c.node1!=null && c.node2!=null && c.edgeDirection!=null && c.flowrateActiveMills!=null && c.stayingTaxisCount>=0
	public boolean repOK(){
		//@EFFECTS: \result == invariant(this);
		return (node1!=null && node2!=null && edgeDirection!=null && flowrateActiveMills!=null && stayingTaxisCount>=0);
	}
	
	public MapEdge (MapNode node1, MapNode node2, EdgeDirection edgeDirection) {
		//@REQUIRES: 所有参数一律不为null;
		//@MODIFIES: this.node1; this.node2; this.edgeDirection;
		//@EFFECTS: 创建了一个新的MapEdge对象并初始化;
		//@			thread start;
		this.node1 = node1;
		this.node2 = node2;
		this.edgeDirection = edgeDirection;
		new Thread(this).start();
	}
	
	@Override
	public void run() {
		//@REQUIRES:
		//@MODIFIES: flowrateActiveMills;
		//@EFFECTS: (flowrateActiveMills.isEmpty) ==> wait;
		//@			(!flowrateActiveMills.isEmpty && System.currentTimeMillis >= flowrateActiveMills[0])
		//@			==> flowrateActiveMills.size == \old(flowrateActiveMills.size) - 1
		//@			&& !flowrateActiveMills.contains(\old(flowrateActiveMills)[0]);
		//@THREAD_EFFECTS: \locked(this);
		while(true){
			long sleepMill = 0;
			synchronized (this) {
				while(flowrateActiveMills.isEmpty()){
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				notifyAll();
				sleepMill = flowrateActiveMills.getFirst() - System.currentTimeMillis();
			}
			
			try {
				Thread.sleep(sleepMill);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
			
			}catch (NoSuchElementException elementException) {
				System.out.println(node1 + " " + node2);
				System.out.println(flowrateActiveMills.isEmpty());
				System.out.println(flowrateActiveMills.size());
			}
			synchronized (this) {
				flowrateActiveMills.removeFirst();
			}
		}
		
	}
	
	synchronized public void taxiEntry(long flowrateActiveMill){
		//@REQUIRES: 
		//@MODIFIES: stayingTaxisCount; flowrateActiveMills;
		//@EFFECTS: stayingTaxisCount == \old(stayingTaxisCount) + 1;
		//@			flowrateActiveMills.size == \old(flowrateActiveMills.size) + 1;
		//@			flowrateActiveMills.contains(flowrateActiveMill);
		//@THREAD_EFFECTS: \locked(this);
		this.stayingTaxisCount ++;
		this.flowrateActiveMills.add(flowrateActiveMill + REFRESH_MILL + System.currentTimeMillis());
		synchronized (this) {
			notifyAll();
		}
	}
	
	synchronized public void taxiExit(){
		//@REQUIRES: 
		//@MODIFIES: stayingTaxisCount;
		//@EFFECTS: stayingTaxisCount == \old(stayingTaxisCount) - 1;
		//@THREAD_EFFECTS: \locked();
		this.stayingTaxisCount --;
	}
	
	synchronized public boolean exists(){
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result <==> exist;
		//@THREAD_EFFECTS: \locked();
		return this.exist;
	}
	
	synchronized public boolean hasExisted(){
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result <==> existed;
		//@THREAD_EFFECTS: \locked();
		return this.existed;
	}
	
	synchronized public boolean build(){
		//@REQUIRES:
		//@MODIFIES: exist;
		//@EFFECTS: \result <==> !\old(exist);
		//@			exist <==> true;
		//@			existed <==> true;
		//@THREAD_EFFECTS: \locked();
		if (this.exist){
			return false;
		}
		this.exist = true;
		this.existed = true;
		return true;
	}
	
	synchronized public void reverse(){
		//@REQUIRES:
		//@MODIFIES: exist; gui; node1; node2;
		//@EFFECTS: (!\old(exist) || stayingTaxisCount == 0) ==> (exist == !\old(exist));
		//@			(!exist && node1.isCrossing) ==> node1.trafficLightExists == false;
		//@			(!exist && node2.isCrossing) ==> node2.trafficLightExists == false;
		//@			gui is refreshed;
		//@			existed <==> true;
		//@THREAD_EFFECTS: \locked();
		if (exist && stayingTaxisCount > 0){
			InfoHandle.info("Taxis on the edge!");
			return;
		}
		this.exist = !this.exist;
		this.existed = true;
		if (!exist){
			if (!node1.isCrossing()){
				node1.removeTrafficLight();
			}
			if (!node2.isCrossing()){
				node2.removeTrafficLight();
			}
		}
		Runner.gui.SetRoadStatus(new Point(node1.getI()-1,node1.getJ()-1), new Point(node2.getI()-1,node2.getJ()-1), this.exist ? 1 : 0);
	}
	
	public MapNode getOtherNode(MapNode node){
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: (node1 != node && node2 != node) ==> \result == null;
		//@			(node1 != node && node2 == node) ==> \result == node1;
		//@			(node1 == node) ==> \result == node2;
		if (this.node1 == node){
			return this.node2;
		}else if (this.node2 == node){
			return this.node1;
		}else {
			return null;
		}
	}
	
	synchronized public int getFlowrate() {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: exist ==> \result == flowrateActiveMills.size;
		//@			!exist ==> \result == 0;
		if (exist){
			return flowrateActiveMills.size();
		}else {
			return 0;
		}
		
	}
}
