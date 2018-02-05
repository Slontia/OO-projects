package ooProject07;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class SearchMapNode {
	/*@Overview:
	 * SearchMapNode是搜索地图的一个节点，它存储着前一个节点和前一个边（如果存在的话），顺着它们可以得到一条路径。
	 */
	final static private int INFINITE = 10000;
	final private MapNode mapNode;
	private int distance = INFINITE;
	private int flowrateSum = INFINITE;
	private SearchMapNode backSearchNode = null;
	private MapEdge backEdge = null;
	private boolean found = false;
	
	//不变式	c.distance>=0 && c.flowrateSum>=0;
	public boolean repOK(){
		//@EFFECTS: \result == invariant(this);
		return (distance>=0 && flowrateSum>=0);
	}
	
	public SearchMapNode(MapNode mapNode) {
		//@REQUIRES: 所有参数一律不为null;
		//@MODIFIES: this.mapNode;
		//@EFFECTS: 创建了一个新的SearchMapNode对象并初始化;
		this.mapNode = mapNode;
	}
	
	public void setBackSearchNode(SearchMapNode backSearchNode) {
		//@REQUIRES: 
		//@MODIFIES: this.backSearchNode;
		//@EFFECRS: this.backSearchNode == backSearchNode;
		this.backSearchNode = backSearchNode;
	}
	
	public MapEdge getBackEdge() {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECRS: \result == backEdge;
		return backEdge;
	}
	
	public SearchMapNode getBackSearchNode() {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result == backSearchNode;
		return backSearchNode;
	}
	
	public void setDistance(int distance) {
		//@REQUIRES: 
		//@MODIFIES: this.distance;
		//@EFFECTS: this.distance == distance;
		this.distance = distance;
	}
	
	public int getDistance() {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result == distance;
		return distance;
	}
	
	public int getFlowrateSum() {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result == flowrateSum;
		return flowrateSum;
	}
	
	public boolean visit(SearchMap searchMap ,LinkedList<SearchMapNode> searchNodes, MapNode terminalNode){
		//@REQUIRES: searchMap != null && searchNodes != null && terminalNode != null;
		//@MODIFIES: [nearbySearchNode]（注：以下属性默认属于该实例）; searchNodes;
		//@EFFECTS: (!\old(found) || flowrateSum can be less) ==> backSearchNode == this && backEdge == the edge between this and [nearbySearchNode];
		//@			found;
		//@			(terminalNode != mapNode) ==> [nearbySearchNode] is added into searchNodes;
		//@			(\exist SearchMapNode [nearbySearchNode]; !found && terminalNode == mapNode) ==> result <==> true;
		//@			((\all MapEdge [existentEdge]; [existentEdge] == null) || \all SearchMapNode [nearbySearchNode]; found || terminalNode != mapNode)
		//@			==> \result <==> false;
		for (Direction direction : Direction.values()){
			MapEdge nearbyEdge = this.mapNode.getNearbyExistentEdge(direction);
			if (nearbyEdge != null){
				MapNode nearbyNode = nearbyEdge.getOtherNode(this.mapNode);
				SearchMapNode nearbySearchNode = searchMap.getSearchMapNode(nearbyNode);
				nearbySearchNode.distance = this.distance + 1;
				if (nearbySearchNode.found){
					if (nearbySearchNode.flowrateSum > nearbyEdge.getFlowrate() + this.flowrateSum){
						nearbySearchNode.backSearchNode = this; // --- record node
						nearbySearchNode.backEdge = nearbyEdge; // --- record edge
						nearbySearchNode.flowrateSum = nearbyEdge.getFlowrate() + this.flowrateSum;
					}
					continue;
				}
				nearbySearchNode.flowrateSum = nearbyEdge.getFlowrate() + this.flowrateSum;
				nearbySearchNode.backSearchNode = this; // --- record node
				nearbySearchNode.backEdge = nearbyEdge; // --- record edge
				if (terminalNode == nearbySearchNode.getMapNode()) {
					nearbySearchNode.turnFound();
					return true;
				}
				nearbySearchNode.turnFound();
				searchNodes.add(nearbySearchNode);
			}
		}
		return false;
	}
	
	
	public boolean visitExisted(SearchMap searchMap ,LinkedList<SearchMapNode> searchNodes, MapNode terminalNode){
		//@REQUIRES: searchMap != null && searchNodes != null && terminalNode != null;
		//@MODIFIES: [nearbySearchNode]（注：以下属性默认属于该实例）; searchNodes;
		//@EFFECTS: (!\old(found) || flowrateSum can be less) ==> backSearchNode == this && backEdge == the edge between this and [nearbySearchNode];
		//@			found;
		//@			(terminalNode != mapNode) ==> [nearbySearchNode] is added into searchNodes;
		//@			(\exist SearchMapNode [nearbySearchNode]; !found && terminalNode == mapNode) ==> result <==> true;
		//@			((\all MapEdge [existedEdge]; [existedEdge] == null) || \all SearchMapNode [nearbySearchNode]; found || terminalNode != mapNode)
		//@			==> \result <==> false;
		for (Direction direction : Direction.values()){
			MapEdge nearbyEdge = this.mapNode.getNearbyExistedEdge(direction);
			if (nearbyEdge != null){
				MapNode nearbyNode = nearbyEdge.getOtherNode(this.mapNode);
				SearchMapNode nearbySearchNode = searchMap.getSearchMapNode(nearbyNode);
				nearbySearchNode.distance = this.distance + 1;
				if (nearbySearchNode.found){
					if (nearbySearchNode.flowrateSum > nearbyEdge.getFlowrate() + this.flowrateSum){
						nearbySearchNode.backSearchNode = this; // --- record node
						nearbySearchNode.backEdge = nearbyEdge; // --- record edge
						nearbySearchNode.flowrateSum = nearbyEdge.getFlowrate() + this.flowrateSum;
					}
					continue;
				}
				nearbySearchNode.flowrateSum = nearbyEdge.getFlowrate() + this.flowrateSum;
				nearbySearchNode.backSearchNode = this; // --- record node
				nearbySearchNode.backEdge = nearbyEdge; // --- record edge
				if (terminalNode == nearbySearchNode.getMapNode()) {
					nearbySearchNode.turnFound();
					return true;
				}
				nearbySearchNode.turnFound();
				searchNodes.add(nearbySearchNode);
			}
		}
		return false;
	}
	
	public MapNode getMapNode() {
		//@REQUIRES: 
		//@MODIFIES: None;
		//@EFFECTS: \result == mapNode;
		return mapNode;
	}
	
	@Override
	public String toString() {
		//@REQUIRES:
		//@MODIFIES: None;
		//@EFFECTS: \result == mapNode.toString;
		return this.mapNode.toString();
	}
	
	public void turnFound() {
		//@REQUIRES:
		//@MODIFIES: found;
		//@EFFECTS: found == true;
		this.found = true;
	}
	
	public void flowrateSumSetZero(){
		//@REQUIRES:
		//@MODIFIES: flowrate;
		//@EFFECTS: flowrate == 0;
		this.flowrateSum = 0;
	}
}
