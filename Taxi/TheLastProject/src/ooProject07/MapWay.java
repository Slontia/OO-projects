package ooProject07;

import java.io.BufferedWriter;
import java.util.Iterator;
import java.util.LinkedList;

public class MapWay {
	/*@Overview:
	 * MapWay是一个地图路径，一个典型的地图路径为{e0,e1,...,en}，其中各个元素ei是顺序存储、从起点到终点的、首尾依次相接的边。
	 */
	final private int distance;
	final private LinkedList<MapEdge> mapEdges = new LinkedList<MapEdge>();
	final private Iterator<MapEdge> edgeIterator;

	//有效性	c.distance>=0 && c.mapEdges!=null;
	public boolean repOK(){
		//@EFFECTS: \result == invariant(this);
		return (this.distance>=0 && mapEdges!=null);
	}
	
	public MapWay(SearchMapNode searchMapNode) {
		//@REQUIRES: 所有参数一律不为null;
		//@MODIFIES: this.distance; this.mapEdges; this.edgeIterator;
		//@EFFECTS: 创建了一个新的MapWay对象并初始化;
		//@			顺着searchMapNode的记录，将边填入this.mapEdges中，并建立一个遍历器;
		this.distance = searchMapNode.getDistance();
		SearchMapNode tempSearchNode = searchMapNode;
		while (tempSearchNode != null){
			MapEdge edge = tempSearchNode.getBackEdge();
			if (edge != null){
				mapEdges.addFirst(edge);
			}
			tempSearchNode = tempSearchNode.getBackSearchNode();
		}
		edgeIterator = mapEdges.iterator();
	}
	
	public MapEdge getNextEdge(){
		//@REQUIRES:
		//@MODIFIES: edgeIterator
		//@EFFECTS: !hasNext ==> \result == null;
		//@			hasNext ==> \result == next;
		//@			edgeIterator.currentEle == \old(edgeIterator.nextEle);
		if (edgeIterator.hasNext()){
			return edgeIterator.next();
		}
		return null;
	}
	
	public int getDistance() {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result == distance;
		return distance;
	}
	
	public boolean containsEdge(MapEdge edge){
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result <==> contains(edge);
		return this.mapEdges.contains(edge);
	}
}
