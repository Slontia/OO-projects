package ooProject07;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.stream.events.StartDocument;

public class TrafficLight implements Runnable{
	/*@Overview:
	 * TrafficLight是信号灯，通过direction表示可通过的方向。
	 */
	private EdgeDirection direction; // --- the passable direction
	private long term;
	private Thread thread ;
	private boolean stopFlag = false;
	final private Point point;
	
	//不变式	c.direction!=null && c.term>=50 && c.term<=100 && c.point!=null;
	public boolean repOK(){
		//@EFFECTS: \result == invariant(this);
		return (direction!=null && term>=50 && term<=100 && point!=null);
	}
	
	public TrafficLight(MapNode node) {
		//@REQUIRES: 
		//@MODIFIES: direction; term; thread; point;
		//@EFFECTS: 创建了一个新的TrafficLight对象并初始化;
		//@			thread start;
		this.direction = (Runner.random.nextInt(1) == 0) ? EdgeDirection.HORIZONAL : EdgeDirection.VERTICAL;
		this.term = Runner.random.nextInt(301) + 200;
		this.thread = new Thread(this);
		this.point = new Point(node.getI()-1, node.getJ()-1);
		this.thread.start();
	}
	
	synchronized public EdgeDirection getDirection() {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result == direction;
		//@THREAD_EFFECTS: \locked();
		return direction;
	}
	
	public void start(){
		//@REQUIRES: thread has not been started;
		//@MODIFIES: thread;
		//@EFFECTS:	thread start;
		this.thread.start();
	}
	
	public void stop(){
		//@REQUIRES: 
		//@MODIFIES: stopFlag;
		//@EFFECTS: stopFlag == true;
		this.stopFlag = true;
	}
	
	@Override
	public void run() {
		//@REQUIRES:
		//@MODIFIES: direction;
		//@EFFECTS: every term: (!stopFlag) ==> direction changes;
		//@THREAD_EFFECTS: \locked(this);
		while(true){
			try {
				Thread.sleep(term);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (stopFlag){
				Runner.gui.SetLightStatus(point, 0);
				break;
			}
			synchronized (this) {
				this.direction = this.direction.changeDirection();
				notifyAll();
			}
			if (direction == EdgeDirection.HORIZONAL){
				Runner.gui.SetLightStatus(point, 1);
			}else {
				Runner.gui.SetLightStatus(point, 2);
			}
		}
	}
}
