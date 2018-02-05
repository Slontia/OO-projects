package ooProject07;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

interface TwoWayIterator <T> {
	/*@Overview:
	 * TwoWayIterator是一个双向迭代器接口。
	 */
	public boolean hasNext();
	public boolean hasPrevious();
	public T next();
	public T previous();
}

public class TraceableTaxi extends Taxi{
	/*@Overview:
	 * TraceableTaxi是可追踪的出租车。
	 */
	private ArrayList<String> dataList = new ArrayList<String>();
	
	//不变式: super.repOK() && c.dataList!=null;
	@Override
	public boolean repOK() {
		return (super.repOK() && dataList!=null);
	}
	
	public TraceableTaxi(int no, TaxiMap taxiMap, long timeMill, Share share, Random random, TaxiGUI taxiGUI, OutputHandle outputHandle) {
		//@REQUIRES: 所有参数一律不为null; 0 <= no < 100; timeMill >= 0;
		//@MODIFIES: this.share; this.taxiMap; this.sysMill; this.startingTimeMill;
		//@EFFECTS: 创建了一个新的TraceableTaxi对象并初始化;
		super(no, taxiMap, timeMill, share, random, taxiGUI, outputHandle);
	}
	
	@Override
	protected void searchWay(){
		//@REQUIRES: 
		//@MODIFIES: way;
		//@EFFECTS: (terminalNode != null) ==> way == the shortest way from positionNode to terminalNode;
		if (this.terminalNode == null){
			return;
		}
		this.way = new SearchMap(taxiMap).searchExistedWay(this.terminalNode, this.positionNode);
	}
	
	@Override
	protected void randomMove(){
		//@REQUIRES: 
		//@MODIFIES: position; credit; localDirection; timeMill;
		//@EFFECTS: localDirection == positionNode.getEdgeDirection(nextEdge).turnOpposite();
		//@			timeMill is increased;
		//@			move to a random edge;
		MapEdge nextEdge = null;
		nextEdge = this.positionNode.getNearbyBestExistedEdgeRandomly();
		assert nextEdge != null;
		this.timeMill += this.positionNode.taxiPass(localDirection, nextEdge);
		this.localDirection = this.positionNode.getEdgeDirection(nextEdge).turnOpposite();
		moveToEdge(nextEdge);
	}
	
	@Override
	public TwoWayIterator<String> twoWayIterator(){
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result == <a new TaxiIterator>;
		return new TaxiIterator(dataList);
	}
	
	@Override
	protected void saveToTaxi() {
		//@REQUIRES:
		//@MODIFIES: dataList;
		//@EFFECTS: contains(<current data>);
		//			size == \old(size) + 1;
		//@THREAD_EFFECTS: \locked(this);
		synchronized (this.dataList) {
			this.dataList.add(outputHandle.getData(this.crTask));
		}
	}
	
	static private class TaxiIterator implements TwoWayIterator <String> {
		/*@Overview:
		 * TaxiIterator是TraceableTaxi的双向迭代器，迭代对象是dataList。
		 */
		private ArrayList<String> dataList;
		private int index = -1;
		
		//不变式: c.dataList!=null;
		public boolean repOK(){
			return (dataList!=null);
		}
		
		public TaxiIterator(ArrayList<String> dataList) {
			//@REQUIRES: dataList != null;
			//@MODIFIES: this.dataList;
			//@EFFECTS:	this.dataList == dataList;
			this.dataList = dataList;
		}
		
		@Override
		public boolean hasNext() {
			//@REQUIRES: 
			//@MODIFIES: None;
			//@EFFECTS: \result <==> index + 1 < dataList.size;
			//@THREAD_EFFECTS: \locked(this);
			synchronized (this.dataList) {
				return (index + 1 < this.dataList.size());
			}
		}

		@Override
		public boolean hasPrevious() {
			//@REQUIRES:
			//@MODIFIES: None;
			//@EFFECTS: \result <==> index - 1 >= 0;
			return (index - 1 >= 0);
		}

		@Override
		public String next() {
			//@REQUIRES:
			//@MODIFIES: index;
			//@EFFECTS: !\old(hasNext) ==> \result == null;
			//@			\old(hasNext) ==> \result == dataList[\old(index)+1] && index == \old(index)+1;
			//@THREAD_EFFECTS: \locked(this);
			synchronized (dataList) {
				if (hasNext()){
					index ++;
					return dataList.get(index);
				}else {
					return null;
				}
			}
		}

		@Override
		public String previous() {
			//@REQUIRES:
			//@MODIFIES: index;
			//@EFFECTS: !\old(hasPrevious) ==> \result == null;
			//@			\old(hasPrevious) ==> \result == dataList[\old(index)-1] && index == \old(index)-1;
			//@THREAD_EFFECTS: \locked(this);
			synchronized (dataList) {
				if (hasPrevious()){
					index --;
					return dataList.get(index);
				}else {
					return null;
				}
			}
		}
	}
}
