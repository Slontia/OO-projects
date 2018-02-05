package ooProject07;

import java.util.ArrayList;
import java.util.LinkedList;

public class SearchMap {
	/*@Overview:
	 * SearchMap是一个负责搜索的地图。
	 */
	final static private int X = TaxiMap.X;
	final static private int Y = TaxiMap.Y;
	final SearchMapNode[][] searchMapNodes = new SearchMapNode[Y+1][X+1];
	final TaxiMap taxiMap;
	
	//不变式	c.searchMapNodes!=null && c.taxiMap!=null;
	public boolean repOK(){
		//@EFFECTS: \result == invariant(this);
		return (searchMapNodes!=null && taxiMap!=null);
	}
	
	public SearchMap(TaxiMap taxiMap) {
		//@REQUIRES: 所有参数一律不为null;
		//@MODIFIES: this.taxiMap; this.searchMapNodes;
		//@EFFECTS: 创建了一个新的SearchMap对象并初始化;
		//@			建立搜索图中的各个搜索节点;
		this.taxiMap = taxiMap;
		for (int i=1; i<=Y; i++){
			for (int j=1; j<=X; j++){
				this.searchMapNodes[i][j] = new SearchMapNode(taxiMap.getNode(i, j));
			}
		}
	}
	
	public MapWay searchWay(MapNode terminalNode, MapNode startingNode) {
		//@REQUIRES: terminalNode != null && startingNode != null;
		//@MODIFIES: searchMapNodes;
		//@EFFECTS: \result == way from startingNode to terminalNode;
		//@			nodes in searchMapNodes linked to next nodes;（searchMapNodes中的搜索节点和其邻接的下一个节点进行关联）
		LinkedList<SearchMapNode> searchNodeFindingList = new LinkedList<SearchMapNode>();
		SearchMapNode startingSearchNode = getSearchMapNode(startingNode);
		startingSearchNode.setDistance(0);
		startingSearchNode.flowrateSumSetZero();
		if (startingNode == terminalNode){
			return new MapWay(startingSearchNode);
		}
		searchNodeFindingList.add(startingSearchNode);
		startingSearchNode.turnFound(); // --- is found
		int finalDistance = -1;
		while (!searchNodeFindingList.isEmpty()){
			SearchMapNode nearestSearchNode = searchNodeFindingList.removeFirst();
			if (finalDistance != -1 && nearestSearchNode.getDistance() != finalDistance){
				break;
			}
			if (nearestSearchNode.visit(this, searchNodeFindingList, terminalNode)){
				finalDistance = nearestSearchNode.getDistance();
			}
		}
		return new MapWay(getSearchMapNode(terminalNode));
	}
	
	public MapWay searchExistedWay(MapNode terminalNode, MapNode startingNode) {
		//@REQUIRES: terminalNode != null && startingNode != null;
		//@MODIFIES: searchMapNodes;
		//@EFFECTS: \result == way from startingNode to terminalNode;
		//@			nodes in searchMapNodes linked to next nodes;（searchMapNodes中的搜索节点和其邻接的下一个节点进行关联）
		LinkedList<SearchMapNode> searchNodeFindingList = new LinkedList<SearchMapNode>();
		SearchMapNode startingSearchNode = getSearchMapNode(startingNode);
		startingSearchNode.setDistance(0);
		startingSearchNode.flowrateSumSetZero();
		if (startingNode == terminalNode){
			return new MapWay(startingSearchNode);
		}
		searchNodeFindingList.add(startingSearchNode);
		startingSearchNode.turnFound(); // --- is found
		int finalDistance = -1;
		while (!searchNodeFindingList.isEmpty()){
			SearchMapNode nearestSearchNode = searchNodeFindingList.removeFirst();
			if (finalDistance != -1 && nearestSearchNode.getDistance() != finalDistance){
				break;
			}
			if (nearestSearchNode.visitExisted(this, searchNodeFindingList, terminalNode)){
				finalDistance = nearestSearchNode.getDistance();
			}
		}
		return new MapWay(getSearchMapNode(terminalNode));
	}
	
	public SearchMapNode getSearchMapNode(MapNode mapNode){
		//@REQUIRES: mapNode != null;
		//@MODIFIES: None;
		//@EFFECTS: searchMapNodes[i][j];
		return this.searchMapNodes[mapNode.getI()][mapNode.getJ()];
	}
}
